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
package com.robin.magic_realm.components.swing;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.robin.general.swing.AggressiveDialog;
import com.robin.general.swing.ComponentTools;
import com.robin.magic_realm.components.quest.*;

public class QuestChooser extends AggressiveDialog {

	private Quest chosenQuest;
	
	private ArrayList<Quest> listOfQuests;
	private JList questList;
	private QuestView questView;
	private JButton okButton;
	private JButton cancelButton;
	
	public QuestChooser(JFrame frame,ArrayList<Quest> listOfQuests) {
		super(frame,"Quest Chooser",true);
		this.listOfQuests = listOfQuests;
		initComponents();
		questList.setSelectedIndex(0);
	}
	public Quest getChosenQuest() {
		return chosenQuest;
	}
	private void initComponents() {
		setSize(800,600);
		setLayout(new BorderLayout());
		questList = new JList(listOfQuests.toArray());
		questList.setBackground(new Color(200,255,255));
		questList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		questList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int selRow = questList.getSelectedIndex();
				Quest quest = selRow==-1?null:listOfQuests.get(selRow);
				questView.updatePanel(quest);
				updateControls();
			}
		});
		JScrollPane sp = new JScrollPane(questList);
		ComponentTools.lockComponentSize(sp,200,1000);
		add(sp,BorderLayout.WEST);
		
		questView = new QuestView();
		add(questView,BorderLayout.CENTER);
		
		JPanel bottom = new JPanel(new GridLayout(1,4));
		bottom.add(Box.createGlue());
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					chosenQuest = null;
					setVisible(false);
					dispose();
				}
			});
		bottom.add(cancelButton);
		bottom.add(Box.createGlue());
			okButton = new JButton("Ok");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					chosenQuest = listOfQuests.get(questList.getSelectedIndex());
					setVisible(false);
					dispose();
				}
			});
		bottom.add(okButton);
		add(bottom,BorderLayout.SOUTH);
	}
	public void updateControls() {
		okButton.setEnabled(questList.getSelectedIndex()>=0);
	}

	public static Quest chooseQuest(JFrame frame,ArrayList<Quest> quests) {
		if (quests.size()==0) return null;
		QuestChooser chooser = new QuestChooser(frame,quests);
		chooser.setLocationRelativeTo(null);
		chooser.setVisible(true);
		return chooser.getChosenQuest();
	}
	
	public static void main(String[] args) {
		ComponentTools.setSystemLookAndFeel();
		Quest quest = QuestChooser.chooseQuest(new JFrame(),QuestLoader.loadAllQuestsFromQuestFolder());
		System.out.println(quest);
		System.exit(0);
	}
}