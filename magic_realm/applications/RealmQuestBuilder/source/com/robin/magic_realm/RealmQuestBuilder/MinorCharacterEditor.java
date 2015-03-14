/* 
 * RealmSpeak is the Java application for playing the board game Magic Realm.
 * Copyright (c) 2005-2015 Robin Warren
 * E-mail: robin@dewkid.com
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 *
 * http://www.gnu.org/licenses/
 */
package com.robin.magic_realm.RealmQuestBuilder;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.general.swing.*;
import com.robin.magic_realm.RealmQuestBuilder.AbilityEditor.AbilityType;
import com.robin.magic_realm.components.quest.Quest;
import com.robin.magic_realm.components.quest.QuestMinorCharacter;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class MinorCharacterEditor extends GenericEditor {
	
	private JFrame parent;
	private Quest quest;
	private QuestMinorCharacter minorCharacter;

	private JTextField name;
	private JCheckBox virtual;
	private JTextArea description;
	private JTable abilityTable;
	private QuestTableEditorPanel abilityEditor;
	
	private ArrayList<String> abilityBlockNames;
	
	public MinorCharacterEditor(JFrame parent, GameData realmSpeakData,Quest quest,QuestMinorCharacter minorCharacter) {
		super(parent, realmSpeakData);
		this.parent = parent;
		this.quest = quest;
		this.minorCharacter = minorCharacter;
		initComponents();
		setLocationRelativeTo(parent);
		readMinorCharacter();
	}
	
	private void initComponents() {
		setTitle("Minor Character");
		setSize(640,480);
		setLayout(new BorderLayout());
		add(buildForm(),BorderLayout.CENTER);
		add(buildOkCancelLine(),BorderLayout.SOUTH);
		
		updateControls();
	}
	
	private void updateControls() {
		String locName = name.getText();
		boolean conflict = false;
		for (QuestMinorCharacter mc:quest.getMinorCharacters()) {
			if (mc!=minorCharacter && mc.getName().equals(locName)) {
				conflict = true;
				break;
			}
		}
		okButton.setEnabled(!conflict);
		
		boolean selected = abilityTable.getSelectedRowCount()>0;
		abilityEditor.getDeleteAction().setEnabled(selected);
		abilityEditor.getEditAction().setEnabled(selected);
	}

	protected boolean isValidForm() {
		return true;
	}
	
	protected void save() {
		saveMinorCharacter();
	}
	
	private void saveMinorCharacter() {
		minorCharacter.setName(name.getText());
		minorCharacter.setVirtual(virtual.isSelected());
		minorCharacter.setDescription(description.getText());
	}

	private void readMinorCharacter() {
		name.setText(minorCharacter.getName());
		virtual.setSelected(minorCharacter.isVirtual());
		description.setText(minorCharacter.getDescription());
		abilityBlockNames = minorCharacter.getAllAbilityBlockNames();
	}
	
	private Box buildForm() {
		Box form = Box.createVerticalBox();
		Box line;
		UniformLabelGroup group = new UniformLabelGroup();
		form.add(Box.createVerticalStrut(10));
		
		line = group.createLabelLine("Name");
		name = new JTextField();
		name.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				updateControls();
			}
		});
		ComponentTools.lockComponentSize(name,200,25);
		line.add(name);
		virtual = new JCheckBox("Virtual - Check this if you don't want it to appear in inventory");
		line.add(Box.createHorizontalStrut(10));
		virtual.setToolTipText("If a minor character is virtual, it won't appear in the character inventory.");
		line.add(virtual);
		line.add(Box.createHorizontalGlue());
		form.add(line);
		form.add(Box.createVerticalStrut(10));
		
		line = group.createLabelLine("Description");
		description = new JTextArea(15,40);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		line.add(new JScrollPane(description));
		line.add(Box.createHorizontalGlue());
		form.add(line);
		form.add(Box.createVerticalStrut(10));
		
		line = group.createLabelLine("Abilities");
		abilityTable = new JTable(new AbilityTableModel());
		abilityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		abilityTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent ev) {
				if (ev.getClickCount()==2) {
					abilityEditor.getEditAction().actionPerformed(new ActionEvent(abilityTable,0,"edit"));
				}
			}
		});
		abilityTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				updateControls();
			}
		});
		abilityEditor = new QuestTableEditorPanel("Abilities",abilityTable){
			public void add() {
				ButtonOptionDialog dialog = new ButtonOptionDialog(parent, null, "Choose an ability type:", "New Ability", true);
				for (AbilityType rt : AbilityType.values()) {
					dialog.addSelectionObject(rt);
				}
				dialog.setLocationRelativeTo(this);
				dialog.setVisible(true);

				AbilityType selectedType = (AbilityType)dialog.getSelectedObject();
				if (selectedType != null) {
					String abilityBlock = getNextAbilityBlock();
					AbilityEditor editor = new AbilityEditor(parent,"New Ability",selectedType,new CharacterWrapper(GameObject.createEmptyGameObject()));
					editor.setLocationRelativeTo(this);
					editor.setVisible(true);
					if (!editor.getCanceledEdit()) {
						editor.update(minorCharacter.getGameObject(),abilityBlock);
						abilityBlockNames.add(abilityBlock);
					}
					abilityTable.clearSelection();
					abilityTable.revalidate();
					updateControls();
				}			}
			public void delete() {
				int row = abilityTable.getSelectedRow();
				String abilityBlock = abilityBlockNames.get(row);
				abilityBlockNames.remove(row);
				minorCharacter.getGameObject().removeAttributeBlock(abilityBlock);
				abilityTable.clearSelection();
				abilityTable.revalidate();
				updateControls();
			}
			public void edit() {
				int row = abilityTable.getSelectedRow();
				String abilityBlock = abilityBlockNames.get(row);
				AbilityType type = AbilityType.valueOf(minorCharacter.getGameObject().getAttribute(abilityBlock,QuestMinorCharacter.ABILITY_TYPE));
				CharacterWrapper template = new CharacterWrapper(GameObject.createEmptyGameObject());
				template.getGameObject().copyAttributeBlockFrom(minorCharacter.getGameObject(),abilityBlock);
				template.getGameObject().copyAttributeBlock(abilityBlock,AbilityEditor.TEMPLATE_ABILITY_BLOCK);
				AbilityEditor editor = new AbilityEditor(parent,"Edit Ability",type,template);
				editor.setLocationRelativeTo(this);
				editor.setVisible(true);
				if (!editor.getCanceledEdit()) {
					editor.update(minorCharacter.getGameObject(),abilityBlock);
					abilityTable.revalidate();
				}
			}
		};
		line.add(abilityEditor);
		form.add(line);
		
		form.add(Box.createVerticalGlue());
		
		return form;
	}
	private String getNextAbilityBlock() {
		int n=0;
		while(true) {
			String blockName = QuestMinorCharacter.ABILITY_BLOCK_NAME+n;
			if (!minorCharacter.getGameObject().hasAttributeBlock(blockName)) {
				return blockName;
			}
			n++;
		}
	}
	
	private class AbilityTableModel extends AbstractTableModel {
		public int getColumnCount() {
			return 1;
		}
		public String getColumnName(int colIndex) {
			return "Description";
		}
		public int getRowCount() {
			return abilityBlockNames==null?0:abilityBlockNames.size();
		}
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex<getRowCount()) {
				String blockName = abilityBlockNames.get(rowIndex);
				return minorCharacter.getGameObject().getAttribute(blockName,QuestMinorCharacter.ABILITY_DESCRIPTION);
			}
			return null;
		}
	}
}