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
package com.robin.magic_realm.RealmCharacterBuilder;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.robin.game.objects.GameData;
import com.robin.general.io.FileManager;
import com.robin.general.swing.*;
import com.robin.general.util.OrderedHashtable;
import com.robin.magic_realm.RealmCharacterBuilder.EditPanel.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class AdvantageEditDialog extends AggressiveDialog {
	
	private static int badgePicId = 0;
	
	private ArrayList<String> saveAttributes;
	private AdvantageEditPanel defaultEditPanel;
	private ArrayList<AdvantageEditPanel> panelList;
	private RealmCharacterBuilderModel model;
	private FileManager graphicsManager;
	private String levelKey;
	
	private JPanel mainPanel;
	private JLabel iconLabel;
	private JPanel detailPanel;
	
	private JList typeList;
	private AdvantageEditPanel currentEditPanel;
	
	private JTextField nameField;
	private JTextArea descriptionField;
	private JButton suggestButton;
	
	private JButton newButton;
	private JButton clearButton;
	private JButton doneButton;
	
	private Advantage advantage;
	private GameData magicRealmData;
	
	public AdvantageEditDialog(JFrame frame,RealmCharacterBuilderModel model,FileManager graphicsManager,String levelKey,Advantage advantage,GameData magicRealmData) {
		super(frame,"Edit Advantage",true);
		this.model = model;
		this.graphicsManager = graphicsManager;
		this.levelKey = levelKey;
		this.advantage = advantage;
		if (this.advantage==null) { // always assume a new advantage!
			this.advantage = createAdvantage();
		}
		this.magicRealmData = magicRealmData;
		saveAttributes = new ArrayList<String>(Arrays.asList(CharacterWrapper.DONT_COPY_ATTRIBUTES));
		saveAttributes.remove("badge_icon");
		initComponents();
		setLocationRelativeTo(frame);
	}
	private void initComponents() {
		setLayout(new BorderLayout());
		setSize(900,650);
		
		mainPanel = new JPanel(new BorderLayout());
			Box box = Box.createHorizontalBox();
			Box iconBox = Box.createVerticalBox();
			iconLabel = new JLabel("",JLabel.CENTER);
			ComponentTools.lockComponentSize(iconLabel,38,38);
			iconLabel.setBorder(BorderFactory.createEtchedBorder());
			iconBox.add(iconLabel);
			JButton editIconButton = new JButton("Edit");
			editIconButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					editIcon();
				}
			});
			ComponentTools.lockComponentSize(editIconButton,100,25);
			iconBox.add(editIconButton);
			JButton resetIconButton = new JButton("Reset");
			resetIconButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					resetIcon();
				}
			});
			ComponentTools.lockComponentSize(resetIconButton,100,25);
			iconBox.add(resetIconButton);
			iconBox.add(Box.createVerticalGlue());
			iconBox.setBorder(BorderFactory.createTitledBorder("Badge"));
			box.add(iconBox);
			JPanel infoPanel = new JPanel(new GridLayout(2,1));
				UniformLabelGroup group = new UniformLabelGroup();
				Box line = group.createLabelLine("Name");
				nameField = new JTextField();
				nameField.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						String text = nameField.getText().trim().toUpperCase();
						nameField.setText(text);
						advantage.setName(text);
						updateControls();
					}
				});
				nameField.addFocusListener(new FocusAdapter() {
					public void focusGained(FocusEvent ev) {
						nameField.selectAll();
					}
					public void focusLost(FocusEvent ev) {
						String text = nameField.getText().trim().toUpperCase();
						nameField.setText(text);
						advantage.setName(text);
						updateControls();
					}
				});
				ComponentTools.lockComponentSize(nameField,300,25);
				line.add(nameField);
				line.add(Box.createHorizontalGlue());
			infoPanel.add(line);
				line = group.createLabelLine("Description");
				descriptionField = new JTextArea();
				descriptionField.addFocusListener(new FocusAdapter() {
					public void focusGained(FocusEvent ev) {
						descriptionField.selectAll();
					}
					public void focusLost(FocusEvent ev) {
						String text = descriptionField.getText().trim();
						descriptionField.setText(text);
						advantage.setDescription(text);
						updateControls();
					}
				});
				descriptionField.setFont(new Font("Dialog",Font.PLAIN,10));
				descriptionField.setLineWrap(true);
				descriptionField.setWrapStyleWord(true);
				line.add(new JScrollPane(descriptionField));
				suggestButton = new JButton("Suggest");
				suggestButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						if (advantage!=null) {
							AdvantageEditPanel panel = (AdvantageEditPanel)typeList.getSelectedValue();
							if (panel!=null) {
								String text = panel.getSuggestedDescription();
								if (text!=null) {
									descriptionField.setText(text);
									advantage.setDescription(text);
									updateControls();
								}
								else {
									JOptionPane.showMessageDialog(AdvantageEditDialog.this,"There is no suggestion for this advantage.");
								}
							}
						}
					}
				});
				line.add(suggestButton);
				line.add(Box.createHorizontalGlue());
			infoPanel.add(line);
			infoPanel.setBorder(BorderFactory.createTitledBorder("Special Advantage"));
			box.add(infoPanel);
		mainPanel.add(box,"North");
		mainPanel.add(getDetailPanel(),"Center");
		
		add(mainPanel,"Center");
		
		Box controls = Box.createHorizontalBox();
		controls.add(Box.createHorizontalGlue());
		newButton = new JButton("New");
		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				advantage = createAdvantage();
				initControlsForAdvantage();
				updateControls();
			}
		});
		controls.add(newButton);
		controls.add(Box.createHorizontalGlue());
		clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				typeList.clearSelection();
				advantage = null;
				updateControls();
			}
		});
		controls.add(clearButton);
		controls.add(Box.createHorizontalGlue());
		doneButton = new JButton("Done");
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				clearLevelAdvantages();
				if (advantage!=null) {
					AdvantageEditPanel panel = (AdvantageEditPanel)typeList.getSelectedValue();
					if (panel!=null) {
						String fullString = advantage.getName()+": "+advantage.getDescription();
						model.getCharacter().getGameObject().addAttributeListItem(levelKey,"advantages",fullString);
						model.getCharacter().getGameObject().setAttribute(levelKey,"badge_icon",advantage.getBadgeName());
						panel.apply();
					}
				}
				setVisible(false);
			}
		});
		controls.add(doneButton);
		controls.add(Box.createHorizontalGlue());
		
		add(controls,"South");
		
		updateControls();
		initControlsForAdvantage();
	}
	private void editIcon() {
		File file = graphicsManager.getLoadPath();
		if (file!=null) {
			ImageIcon icon = IconFactory.findIcon(file.getAbsolutePath());
			if (icon!=null) {
				iconLabel.setIcon(icon);
				advantage.setBadgeName(RealmCharacterConstants.CUSTOM_ICON_BASE_PATH+"badges/badge"+(badgePicId++),icon);
				repaint();
			}
		}
	}
	private void resetIcon() {
		Advantage adv = createAdvantage();
		iconLabel.setIcon(adv.getBadge());
		advantage.setBadgeName(adv.getBadgeName(),adv.getBadge());
	}
	private void clearLevelAdvantages() {
		// Clears out all current advantages
		OrderedHashtable attributeBlock = model.getCharacter().getGameObject().getAttributeBlock(levelKey);
		ArrayList keys = new ArrayList(attributeBlock.keySet());
		for (Iterator i=keys.iterator();i.hasNext();) {
			String key = (String)i.next();
			if (!saveAttributes.contains(key)) {
				model.getCharacter().getGameObject().removeAttribute(levelKey,key);
			}
		}
	}
	private void initControlsForAdvantage() {
		if (advantage!=null) {
			iconLabel.setIcon(advantage.getBadge());
			nameField.setText(advantage.getName());
			descriptionField.setText(advantage.getDescription());
			AdvantageEditPanel panel = getCurrentAdvantageEditPanel();
			if (panel!=null) {
				typeList.setSelectedValue(panel,true);
			}
			else {
				typeList.setSelectedValue(defaultEditPanel,true);
			}
		}
		else {
			typeList.setSelectedValue(defaultEditPanel,true);
		}
	}
	private void updateControls() {
		mainPanel.setVisible(advantage!=null);
		newButton.setEnabled(advantage==null);
		clearButton.setEnabled(advantage!=null);
	}
	public Advantage createAdvantage() {
		int n = Integer.valueOf(levelKey.substring(levelKey.length()-1));
		String name = null;
		switch(n) {
			case 1:		name = "one"; break;
			case 2:		name = "two"; break;
			case 3:		name = "three"; break;
			case 4:		name = "four"; break;
		}
		return new Advantage(RealmCharacterConstants.CUSTOM_ICON_BASE_PATH+"badges/"+name);
	}
	private AdvantageEditPanel getCurrentAdvantageEditPanel() {
		for (int i=0;i<panelList.size();i++) {
			AdvantageEditPanel panel = panelList.get(i);
			if (panel.isCurrent()) {
				return panel;
			}
		}
		return null;
	}
	private JPanel getDetailPanel() {
		detailPanel = new JPanel(new BorderLayout());
		detailPanel.setBorder(BorderFactory.createTitledBorder("Details"));
		
		panelList = new ArrayList<AdvantageEditPanel>();
		panelList.add(defaultEditPanel = new BlankEditPanel(model.getCharacter(),levelKey));
		panelList.add(new ColorSourceEditPanel(model.getCharacter(),levelKey));
		panelList.add(new CompanionEditPanel(model.getCharacter(),levelKey));
		panelList.add(new DieModEditPanel(model.getCharacter(),levelKey));
		panelList.add(new ExtraActionEditPanel(model.getCharacter(),levelKey));
		panelList.add(new ExtraChitEditPanel(this,model.getCharacter(),levelKey));
		panelList.add(new GoldAdjustmentEditPanel(model.getCharacter(),levelKey));
		panelList.add(new ItemRestrictionsEditPanel(model.getCharacter(),levelKey));
		panelList.add(new MiscellaneousEditPanel(model.getCharacter(),levelKey));
		panelList.add(new MonsterInteractionEditPanel(model.getCharacter(),levelKey));
		panelList.add(new SpecialActionEditPanel(model.getCharacter(),levelKey));
		panelList.add(new StartingInventoryEditPanel(model.getCharacter(),levelKey,magicRealmData));
		panelList.add(new TacticsChangeEditPanel(model.getCharacter(),levelKey));
		
		typeList = new JList(panelList.toArray());
		typeList.setPreferredSize(new Dimension(100,100));
		typeList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent ev) {
				if (currentEditPanel!=null) {
					detailPanel.remove(currentEditPanel);
				}
				currentEditPanel = (AdvantageEditPanel)typeList.getSelectedValue();
				if (currentEditPanel!=null) {
					detailPanel.add(currentEditPanel,"Center");
				}
				detailPanel.revalidate();
				detailPanel.repaint();
			}
		});
		
		detailPanel.add(new JScrollPane(typeList),"West");
		return detailPanel;
	}
}