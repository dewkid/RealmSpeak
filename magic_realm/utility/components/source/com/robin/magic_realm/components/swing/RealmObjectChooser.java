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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.utility.RealmLoader;
import com.robin.magic_realm.components.utility.RealmUtility;

public class RealmObjectChooser extends JDialog {
	protected RealmObjectPanel panel;
	protected boolean okay = false;
	protected ArrayList<GameObject> chosenObjects = null;
	protected GameData gameData; // for validation purposes
	protected int minCount = -1;
	protected int maxCount = -1;
	
	protected JButton cancelButton;
	protected JButton okayButton;
	protected JLabel instructions;
	protected JButton selectAllButton;
	
	boolean validateChosenObjects = true;
	
	public RealmObjectChooser(String title,GameData data,boolean singleSelection) {
		this(title,data,singleSelection,false);
	}
	public RealmObjectChooser(String title,GameData data,boolean singleSelection,boolean manualFlipEnabled) {
		setTitle(title);
		this.gameData = data;
		initComponents(singleSelection,manualFlipEnabled);
		setLocationRelativeTo(null);
		panel.addSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent ev) {
				updateControls();
			}
		});
		updateControls();
	}
	public void selectAll() {
		panel.selectAll();
	}
	public void setValidCount(int count) {
		if (count<=0) {
			throw new IllegalArgumentException("validCount must be a value greater than zero");
		}
		setValidRange(count,count);
	}
	public void setValidRange(int min, int max) {
		if (panel.getSelectionMode()==RealmObjectPanel.SINGLE_SELECTION) {
			throw new IllegalStateException("Cannot set a validRange on a chooser with single selection only");
		}
		if (min>max) {
			throw new IllegalStateException("Cannot set a validRange where min is higher than max");
		}
		if (min==0 || max==0) {
			throw new IllegalStateException("Cannot set a validRange where either min or max is zero");
		}
		minCount = min;
		maxCount = max;
		updateInstructions();
	}
	private void updateInstructions() {
		boolean singleSelection = panel.getSelectionMode()==RealmObjectPanel.SINGLE_SELECTION;
		if (!singleSelection) {
			StringBuilder sb = new StringBuilder("** ");
			if (minCount>1) {
				sb.append("You must select at least ");
				sb.append(minCount);
			}
			if (maxCount>1) {
				if (minCount>1) sb.append(", but "); else sb.append("Select ");
				sb.append("no more than ");
				sb.append(maxCount);
			}
			if (minCount>1 && minCount == maxCount) {
				sb = new StringBuilder("** You must select exactly ");
				sb.append(minCount);
			}
			if (minCount>1 || maxCount>1) {
				sb.append(". **");
				instructions.setText(sb.toString());
			}
			else {
				instructions.setText("** You may select more than one **");
			}
		}
		else if (panel.getManualFlipEnabled()) {
			instructions.setText("SHIFT-Click to flip chit/card.");
		}
	}
	private void updateControls() {
		int count = panel.getSelectedCount();
		boolean meetsMin = minCount<0 || count>=minCount;
		boolean meetsMax = maxCount<0 || count<=maxCount;
		okayButton.setEnabled(meetsMin && meetsMax && panel.getSelectedCount()>0);
	}
	private void initComponents(boolean singleSelection,boolean manualFlipEnabled) {
		getContentPane().setLayout(new BorderLayout());
			panel = new RealmObjectPanel(true,manualFlipEnabled);
			panel.setSelectionMode(singleSelection?RealmObjectPanel.SINGLE_SELECTION:RealmObjectPanel.MULTIPLE_SELECTION);
		getContentPane().add(new JScrollPane(panel),"Center");
			Box box = Box.createHorizontalBox();
			instructions = new JLabel("");
			instructions.setForeground(Color.red);
			box.add(instructions);
			updateInstructions();
			box.add(Box.createHorizontalGlue());
			if (!singleSelection) {
					selectAllButton = new JButton("Select All");
					selectAllButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							selectAll();
						}
					});
				box.add(selectAllButton);
				box.add(Box.createHorizontalGlue());
			}
				cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						close();
					}
				});
			box.add(cancelButton);
				okayButton = new JButton("Okay");
				okayButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						if (doOkay()) {
							panel.restoreFlipped();
							close();
						}
					}
				});
				getRootPane().setDefaultButton(okayButton);
			box.add(okayButton);
		getContentPane().add(box,"South");
		setModal(true);
		setSize(640,500);
	}
	public void addObjectsToChoose(Collection objects) {
		ArrayList list = new ArrayList(objects);
		for (Iterator i=list.iterator();i.hasNext();) {
			GameObject object = (GameObject)i.next();
			panel.addObject(object);
		}
		panel.revalidate();
	}
	public void addComponentsToChoose(Collection components) {
		for (Iterator i=components.iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			panel.add(rc);
		}
		panel.revalidate();
	}
	public void addComponentToChoose(RealmComponent rc) {
		panel.add(rc);
		panel.revalidate();
	}
	private boolean doOkay() {
		GameObject[] object = panel.getSelectedGameObjects();
		
		if (object.length>0) {
			okay = true;
			chosenObjects = new ArrayList<GameObject>();
			for (int i=0;i<object.length;i++) {
				// quickly verify that the chosenObject is still in gameData!
				if (validateChosenObjects && !gameData.validate(object[i])) {
					JOptionPane.showMessageDialog(this,"Oops, one object has changed!  Try again...");
					okay = false;
					chosenObjects = null;
					panel.removeGameObject(object[i]);
				}
				if (okay) {
				    chosenObjects.add(object[i]);
				}
			}
			return okay;
		}
		
		return false;
	}
	public void close() {
		setVisible(false);
		dispose();
	}
	public boolean pressedOkay() {
		return okay;
	}
	public GameObject getChosenObject() {
		return (chosenObjects!=null && !chosenObjects.isEmpty())?chosenObjects.get(0):null;
	}
	public ArrayList<GameObject> getChosenObjects() {
		return chosenObjects;
	}
	public static void main(String[] args) {
		RealmUtility.setupTextType();
		RealmLoader loader = new RealmLoader();
		GameData data = loader.getData();
		
		RealmObjectChooser chooser = new RealmObjectChooser("test",data,false,true);
		chooser.addObjectsToChoose(data.getGameObjects());
		//chooser.setValidCount(2);
		chooser.setValidRange(2,7);
		chooser.setVisible(true);
	}
	public boolean isValidateChosenObjects() {
		return validateChosenObjects;
	}
	public void setValidateChosenObjects(boolean validateChosenObjects) {
		this.validateChosenObjects = validateChosenObjects;
	}
}