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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.*;
import javax.swing.text.*;

import com.robin.game.objects.*;
import com.robin.general.swing.*;
import com.robin.magic_realm.RealmQuestBuilder.QuestPropertyBlock.FieldType;
import com.robin.magic_realm.components.ChitComponent;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.quest.QuestConstants;
import com.robin.magic_realm.components.quest.reward.QuestRewardItem;
import com.robin.magic_realm.components.swing.RealmObjectChooser;
import com.robin.magic_realm.components.utility.TemplateLibrary;

public abstract class QuestBlockEditor extends GenericEditor {
	
	protected abstract String getEditorTitle();
	protected abstract ArrayList<QuestPropertyBlock> createPropertyBlocks();
	
	private JFrame parent;
	private ArrayList<QuestPropertyBlock> propertyBlocks;
	private GameObjectWrapper go;
	private ChitTypePanel chitTypePanel; // important to remember this one, in case it is used
	
	public QuestBlockEditor(JFrame parent, GameData realmSpeakData, GameObjectWrapper go) {
		super(parent, realmSpeakData);
		this.parent = parent;
		this.go = go;
	}
	public void setVisible(boolean val) {
		if (!propertyBlocks.isEmpty()) {
			super.setVisible(val);
		}
	}
	public boolean needsEdit() {
		return !propertyBlocks.isEmpty();
	}
	protected void read() {
		propertyBlocks = createPropertyBlocks();
		initComponents();
	}
	protected boolean isValidForm() {
		for (QuestPropertyBlock block:propertyBlocks) {
			if (block.getFieldType()==FieldType.NoSpacesTextLine) {
				String val = ((JTextField)block.getComponent()).getText();
				if (val==null || val.trim().length()==0) {
					JOptionPane.showMessageDialog(parent,"Value required for field \""+block.getFieldName()+"\"");
					return false;
				}
			}
			else if (block.getFieldType()==FieldType.Number) {
				int val = ((IntegerField)block.getComponent()).getInt();
				if (val<=0) {
					JOptionPane.showMessageDialog(parent,"Field \""+block.getFieldName()+"\" must be greater than zero.");
					return false;
				}
			}
		}
		return true;
	}
	protected void save() {
		for (QuestPropertyBlock block:propertyBlocks) {
			switch(block.getFieldType()) {
				case TextLine:
				case NoSpacesTextLine:
				case SmartTextLine:
				case Regex:
					go.setString(block.getKeyName(),((JTextField)block.getComponent()).getText().trim());
					break;
				case SmartTextArea:
				case TextArea:
					go.setString(block.getKeyName(),((JTextArea)block.getComponent()).getText().trim());
					break;
				case Number:
					go.setInt(block.getKeyName(),((IntegerField)block.getComponent()).getInt());
					break;
				case StringSelector:
				case GameObjectWrapperSelector:
					saveSelection(block,(JComboBox)block.getComponent());
					break;
				case Boolean:
					go.setBoolean(block.getKeyName(),((JCheckBox)block.getComponent()).isSelected());
					break;
				case ChitType:
					go.setList(block.getKeyName(),((ChitTypePanel)block.getComponent()).getChitTypeList());
					break;
				case CompanionSelector:
					go.setString(QuestConstants.KEY_PREFIX+block.getKeyName(),((JLabel)block.getComponent()).getText());
					go.setString(QuestConstants.VALUE_PREFIX+block.getKeyName(),((JLabel)block.getComponent()).getToolTipText());
					break;
			}
		}
	}
	private void initComponents() {
		setTitle(getEditorTitle());
		setSize(400,300);
		setLayout(new BorderLayout());
		add(buildForm(),BorderLayout.CENTER);
		add(buildOkCancelLine(),BorderLayout.SOUTH);
	}
	private Box buildForm() {
		UniformLabelGroup group = new UniformLabelGroup();
		Box box = Box.createVerticalBox();
		for (QuestPropertyBlock block:propertyBlocks) {
			Box line = group.createLabelLine(block.getFieldName());
			JComponent component = null;
			JButton button = null;
			boolean useScrollPane = false;
			switch(block.getFieldType()) {
				case TextLine:
					component = new JTextField(go.getString(block.getKeyName()));
					ComponentTools.lockComponentSize(component,150,25);
					break;
				case SmartTextLine:
					SuggestionTextField sta = new SuggestionTextField();
					sta.setText(go.getString(block.getKeyName()));
					sta.setWords(block.getSelectionsAsStrings());
					sta.setFont(UIManager.getFont("TextArea.font"));
					sta.setLineModeOn(true);
					sta.setAutoSpace(false);
					component = sta;
					ComponentTools.lockComponentSize(component,150,25);
					break;
				case SmartTextArea:
					SuggestionTextArea star = new SuggestionTextArea();
					star.setText(go.getString(block.getKeyName()));
					star.setWords(block.getSelectionsAsStrings());
					star.setFont(UIManager.getFont("TextArea.font"));
					star.setLineModeOn(false);
					star.setAutoSpace(false);
					star.setWrapStyleWord(true);
					star.setLineWrap(true);
					component = star;
					ComponentTools.lockComponentSize(component,200,100);
					break;
				case NoSpacesTextLine:
					JTextField tf = new JTextField();
					component = tf;
					ComponentTools.lockComponentSize(component,150,25);
					((JTextField)component).setDocument(new PlainDocument() {
						public void insertString (int offset, String  str, AttributeSet attr) throws BadLocationException {
							if (str == null) return;
							StringBuffer temp=new StringBuffer();
							for (int i=0;i<str.length();i++) {
								if (!Character.isWhitespace(str.charAt(i))) {
									temp.append(str.charAt(i));
								}
							}
							if (temp.length()>0)
								super.insertString(offset,temp.toString(),attr);
						}
					});				
					tf.setText(go.getString(block.getKeyName()));
					break;
				case TextArea:
					JTextArea ta = new JTextArea(15,40);
					ta.setLineWrap(true);
					ta.setWrapStyleWord(true);
					ta.setText(go.getString(block.getKeyName()));
					useScrollPane = true;
					component = ta;
					break;
				case Number:
					int n = go.getInt(block.getKeyName());
					component = new IntegerField(n==0?1:n);
					ComponentTools.lockComponentSize(component,150,25);
					break;
				case StringSelector:
				case GameObjectWrapperSelector:
					JComboBox cb = new JComboBox(block.getSelections());
					component = cb;
					readSelection(block,cb);
					ComponentTools.lockComponentSize(component,200,25);
					break;
				case Boolean:
					JCheckBox yesBox = new JCheckBox("",go.getBoolean(block.getKeyName()));
					yesBox.setFocusable(false);
					component = yesBox;
					break;
				case Regex:
					component = new JTextField(go.getString(block.getKeyName()));
					ComponentTools.lockComponentSize(component,150,25);
					button = new PropertyButton("...",component,block);
					button.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							PropertyButton me = (PropertyButton)ev.getSource();
							JTextField field = (JTextField)me.getMyComponent();
							String text = launchRegexHelper(field.getText(),me.getBlock());
							if (text!=null) {
								field.setText(text);
							}
						}
					});
					break;
				case ChitType:
					chitTypePanel = new ChitTypePanel(go.getList(block.getKeyName()));
					component = chitTypePanel;
					break;
				case CompanionSelector:
					JLabel label = new JLabel();
					component = label;
					String current = go.getString(block.getKeyName());
					if (current==null) {
						current = go.getString(QuestConstants.KEY_PREFIX+block.getKeyName());
					}
					if (current!=null && current.length()>0) {
						for(KeyValuePair kv:(KeyValuePair[])block.getSelections()) {
							if (!kv.getKey().equals(current)) continue;
							GameObject go = TemplateLibrary.getSingleton().getCompanionTemplate(kv.getKey(),kv.getValue());
							ChitComponent chit = (ChitComponent)RealmComponent.getRealmComponent(go);
							label.setIcon(chit.getIcon());
							label.setText(kv.getKey());
							label.setToolTipText(kv.getValue());
							break;
						}
					}
					button = new PropertyButton("...",component,block);
					button.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							PropertyButton me = (PropertyButton)ev.getSource();
							JLabel field = (JLabel)me.getMyComponent();
							
							ChitComponent chit = selectCompanion((KeyValuePair[])me.getBlock().getSelections());
							if (chit!=null) {
								field.setIcon(chit.getIcon());
								field.setText(chit.getGameObject().getName());
								field.setToolTipText(chit.getGameObject().getThisAttribute("query")); // just a hacky way to capture the info
							}
						}
					});
					break;
			}
			block.setComponent(component);
			if (useScrollPane) {
				line.add(new JScrollPane(component));
			}
			else {
				line.add(component);
			}
			if (button!=null) {
				line.add(Box.createHorizontalStrut(10));
				line.add(button);
			}
			line.add(Box.createHorizontalGlue());
			box.add(line);
		}
		box.add(Box.createVerticalGlue());
		return box;
	}
	private ChitComponent selectCompanion(KeyValuePair[] keyVals) {
		ArrayList<GameObject> list = new ArrayList<GameObject>();
		for(KeyValuePair kv:keyVals) {
			GameObject go = TemplateLibrary.getSingleton().getCompanionTemplate(kv.getKey(),kv.getValue());
			list.add(go);
		}
		
		RealmObjectChooser chooser = new RealmObjectChooser("Choose one:",realmSpeakData,true,true);
		chooser.setValidateChosenObjects(false);
		chooser.addObjectsToChoose(list);
		chooser.setVisible(true);
		GameObject go = chooser.getChosenObject();
		return go==null?null:(ChitComponent)RealmComponent.getRealmComponent(go);
	}
	private String launchRegexHelper(String text,QuestPropertyBlock block) {
		ArrayList<String> objectNames = new ArrayList<String>();
		if (chitTypePanel!=null) {
			ArrayList<GameObject> objects = QuestRewardItem.getObjectList(realmSpeakData.getGameObjects(),chitTypePanel.getChitItemTypes(),null); 
			for(GameObject go:objects) {
				if (!objectNames.contains(go.getName())) {
					objectNames.add(go.getName());
				}
			}
		}
		else {
			String[] keyVals = block.getKeyVals();
			if (keyVals!=null && keyVals.length>0) {
				GamePool pool = new GamePool(realmSpeakData.getGameObjects());
				for (String keyVal:keyVals) {
					for(GameObject go:pool.find(keyVal)) {
						if (objectNames.contains(go.getName())) continue;
						objectNames.add(go.getName());
					}
				}
			}
			else {
				objectNames = new ArrayList<String>(realmSpeakData.getAllGameObjectNames());
			}
		}
		Collections.sort(objectNames);
		RealmRegexHelper helper = new RealmRegexHelper(parent,text,objectNames);
		helper.setLocationRelativeTo(this);
		helper.setVisible(true);
		return helper.getText();
	}
	private void readSelection(QuestPropertyBlock block,JComboBox cb) {
		Object[] selections =block.getSelections(); 
		if (selections==null || selections.length==0) return;
		boolean kvPair = selections[0] instanceof KeyValuePair;
		
		int selIndex = 0; // default
		String current = null;
		if (block.getFieldType()==FieldType.GameObjectWrapperSelector) {
			String id = go.getString(block.getKeyName());
			if (id!=null) {
				GameObject ref = go.getGameData().getGameObject(Long.valueOf(id));
				current = ref.getName();
			}
		}
		else {
			current = kvPair?go.getString(QuestConstants.KEY_PREFIX+block.getKeyName()):go.getString(block.getKeyName());
		}
		if (current!=null) {
			for (int i=0;i<selections.length;i++) {
				Object selection = selections[i];
				if (selection==null) continue;
				if (selection.toString().equals(current)) {
					selIndex = i;
					break;
				}
			}
		}
		cb.setSelectedIndex(selIndex);
	}
	private void saveSelection(QuestPropertyBlock block,JComboBox cb) {
		if (block.getFieldType()==FieldType.GameObjectWrapperSelector) {
			GameObjectWrapper gow = (GameObjectWrapper)cb.getSelectedItem();
			if (gow==null) {
				go.clear(block.getKeyName());
			}
			else {
				go.setString(block.getKeyName(),gow.getGameObject().getStringId());
			}
		}
		else {
			Object selected = cb.getSelectedItem();
			if (selected instanceof KeyValuePair) {
				KeyValuePair kv = (KeyValuePair)selected;
				go.setString(QuestConstants.KEY_PREFIX+block.getKeyName(),kv.getKey());
				go.setString(QuestConstants.VALUE_PREFIX+block.getKeyName(),kv.getValue());
			}
			else {
				go.setString(block.getKeyName(),selected.toString());
			}
		}
	}
	
	private static class PropertyButton extends ComponentButton {
		private QuestPropertyBlock block;
		public PropertyButton(String text, JComponent component,QuestPropertyBlock block) {
			super(text, component);
			this.block = block;
		}
		public QuestPropertyBlock getBlock() {
			return block;
		}
	}
}