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
import java.util.*;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.*;

import com.robin.game.objects.*;
import com.robin.general.swing.*;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.quest.*;

public class QuestLocationEditor extends GenericEditor {
	
	private static final String INVALID = " <== INVALID";
	
	private static ArrayList<String> suggestionWords;
	
	private JFrame parent;
	private Quest quest;
	private QuestLocation location;
	
	private JTextField name;
	private JComboBox type;
	private JComboBox clearingType;
	private JComboBox tileSideType;
	private JRadioButton sameClearing;
	private JRadioButton sameTile;
	private JLabel descriptionLabel;
	private SuggestionTextArea locationList;
	
	public QuestLocationEditor(JFrame parent,GameData realmSpeakData,Quest quest,QuestLocation location) {
		super(parent,realmSpeakData);
		this.parent = parent;
		this.quest = quest;
		this.location = location;
		if (suggestionWords==null) {
			initSuggestionWords(realmSpeakData);
		}
		initComponents();
		setLocationRelativeTo(parent);
		readLocation();
	}
	
	private static void initSuggestionWords(GameData realmSpeakData) {
		suggestionWords = new ArrayList<String>();
		GamePool pool = new GamePool(realmSpeakData.getGameObjects());
		String query = "!part,!summon,!spell,!tile,!character_chit,!virtual_dwelling,!season,!test,!character";
		for(GameObject go:pool.find(query)) {
			if (suggestionWords.contains(go.getName())) continue;
			suggestionWords.add(go.getName());
		}
		for(GameObject go:pool.find("tile")) {
			TileComponent tile = (TileComponent)RealmComponent.getRealmComponent(go);
			suggestionWords.add(tile.getTileCode());
			suggestionWords.add(go.getName());
			for(ClearingDetail clearing:tile.getClearings()) {
				String name = go.getName()+" "+clearing.getNum();
				suggestionWords.add(name);
			}
		}
		Collections.sort(suggestionWords);
	}
	
	private void readLocation() {
		name.setText(location.getName());
		type.setSelectedItem(location.getLocationType());
		clearingType.setSelectedItem(location.getLocationClearingType());
		tileSideType.setSelectedItem(location.getLocationTileSideType());
		sameClearing.setSelected(!location.isSameTile());
		sameTile.setSelected(location.isSameTile());
		if (location.getChoiceAddresses()!=null) {
			StringBuilder sb = new StringBuilder();
			for (Iterator i=location.getChoiceAddresses().iterator();i.hasNext();) {
				if (sb.length()>0) {
					sb.append("\n");
				}
				sb.append(i.next());
			}
			locationList.setText(sb.toString());
			verifyLocations();
		}
	}
	protected boolean isValidForm() {
		return true;
	}
	protected void save() {
		saveLocation();
	}
	private void saveLocation() {
		location.setName(name.getText());
		location.setLocationType((LocationType)type.getSelectedItem());
		location.setLocationClearingType((LocationClearingType)clearingType.getSelectedItem());
		location.setLocationTileSideType((LocationTileSideType)tileSideType.getSelectedItem());
		location.setSameTile(sameTile.isSelected());
		location.clearChoiceAddresses();
		for (String token:getLocationList()) {
			location.addChoiceAddresses(token);
		}
	}
	private void findLocations() {
		ListChooser chooser = new ListChooser(parent,"Locations:",suggestionWords);
		chooser.setDoubleClickEnabled(true);
		chooser.setLocationRelativeTo(this);
		chooser.setVisible(true);
		Vector list = chooser.getSelectedObjects();
		if (list==null) return;
		StringBuilder sb = new StringBuilder();
		for (String token:getLocationList()) {
			sb.append(token);
			if (!QuestLocation.validLocation(realmSpeakData,token)) {
				sb.append(INVALID);
			}
			sb.append("\n");
		}
		for(Object val:list) {
			sb.append(val.toString());
			sb.append("\n");
		}
		locationList.setText(sb.toString());
	}
	private void verifyLocations() {
		StringBuilder sb = new StringBuilder();
		for (String token:getLocationList()) {
			sb.append(token);
			if (!QuestLocation.validLocation(realmSpeakData,token)) {
				sb.append(INVALID);
			}
			sb.append("\n");
		}
		locationList.setText(sb.toString());
	}
	private ArrayList<String> getLocationList() {
		ArrayList<String> list = new ArrayList<String>();
		String text = locationList.getText();
		text = text.replaceAll(INVALID,"");
		StringTokenizer tokens = new StringTokenizer(text,",;:\t\n\r\f");
		while(tokens.hasMoreTokens()) {
			list.add(tokens.nextToken().trim());
		}
		return list;
	}
	private void initComponents() {
		setTitle("Quest Location");
		setSize(640,480);
		setLayout(new BorderLayout());
		add(buildForm(),BorderLayout.CENTER);
		add(buildOkCancelLine(),BorderLayout.SOUTH);
		
		updateControls();
	}
	private void updateControls() {
		String locName = name.getText();
		boolean conflict = false;
		for (QuestLocation loc:quest.getLocations()) {
			if (loc!=location && loc.getName().equals(locName)) {
				conflict = true;
				break;
			}
		}
		
		LocationType lt = (LocationType)type.getSelectedItem();
		descriptionLabel.setText(lt.getDescription());
		
		okButton.setEnabled(!conflict);
	}
	private Box buildForm() {
		Box form = Box.createVerticalBox();
		Box line;
		UniformLabelGroup group = new UniformLabelGroup();
		form.add(Box.createVerticalStrut(10));
		
		line = group.createLabelLine("Name");
		name = new JTextField();
		name.setDocument(new PlainDocument() {
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
		name.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				updateControls();
			}
		});
		ComponentTools.lockComponentSize(name,200,25);
		line.add(name);
		line.add(Box.createHorizontalStrut(10));
		line.add(new JLabel("(No spaces allowed)"));
		line.add(Box.createHorizontalGlue());
		form.add(line);
		form.add(Box.createVerticalStrut(10));
		
		line = group.createLabelLine("Clearing Type");
		clearingType = new JComboBox(LocationClearingType.values());
		ComponentTools.lockComponentSize(clearingType,100,25);
		clearingType.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updateControls();
			}
		});
		line.add(clearingType);
		line.add(Box.createHorizontalGlue());
		form.add(line);
		form.add(Box.createVerticalStrut(10));
		
		line = group.createLabelLine("Tile Side");
		tileSideType = new JComboBox(LocationTileSideType.values());
		ComponentTools.lockComponentSize(tileSideType,100,25);
		tileSideType.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updateControls();
			}
		});
		line.add(tileSideType);
		line.add(Box.createHorizontalGlue());
		form.add(line);
		form.add(Box.createVerticalStrut(10));
		
		line = group.createLabelLine("Clearing or Tile");
		ButtonGroup bg = new ButtonGroup();
		sameClearing = new JRadioButton("Same Clearing",true);
		bg.add(sameClearing);
		line.add(sameClearing);
		sameTile = new JRadioButton("Same Tile",false);
		bg.add(sameTile);
		line.add(sameTile);
		line.add(Box.createHorizontalGlue());
		form.add(line);
		form.add(Box.createVerticalStrut(10));
		
		line = group.createLabelLine("Type");
		type = new JComboBox(LocationType.values());
		ComponentTools.lockComponentSize(type,100,25);
		type.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updateControls();
			}
		});
		line.add(type);
		line.add(Box.createHorizontalStrut(10));
		descriptionLabel = new JLabel("");
		line.add(descriptionLabel);
		line.add(Box.createHorizontalGlue());
		form.add(line);
		form.add(Box.createVerticalStrut(10));
		
		line = group.createLabelLine("Location(s)");
		locationList = new SuggestionTextArea(10,40);
		locationList.setLineModeOn(true);
		locationList.setAutoSpace(false);
		locationList.setWords(suggestionWords);
		line.add(new JScrollPane(locationList));
		Box buttons = Box.createVerticalBox();
		JButton findButton = new JButton(IconFactory.findIcon("icons/search.gif"));
		findButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				findLocations();
			}
		});
		ComponentTools.lockComponentSize(findButton,80,50);
		buttons.add(findButton);
		buttons.add(Box.createVerticalGlue());
		JButton verifyButton = new JButton("Verify");
		verifyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				verifyLocations();
			}
		});
		ComponentTools.lockComponentSize(verifyButton,80,50);
		buttons.add(verifyButton);
		line.add(buttons);
		line.add(Box.createHorizontalGlue());
		form.add(line);
		
		form.add(Box.createVerticalGlue());
		
		return form;
	}
}