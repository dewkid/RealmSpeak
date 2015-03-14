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
package com.robin.game.GameBuilder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.general.swing.ComponentTools;
import com.robin.general.swing.ListManagerPane;

public class GameObjectChooser extends JDialog {

	private String oldFilterString;
	private ArrayList oldExcludeList;

	protected GameObject object;
	protected GameData data;
	protected ListManagerPane objectsPane;
	protected JTextField objectsFilterField;
	
	protected ArrayList chosenObjects = null;
	
	public GameObjectChooser(Component component,GameObject object) {
		this(component,object,object.getGameData());
	}
	public GameObjectChooser(Component component,GameObject object,GameData data) {
		this.object = object;
		this.data = data;
		
		// Capture filter state, so we can return it to normal when we are done
		oldFilterString = data.getFilterString();
		oldExcludeList = data.getExcludeList();
		
		if (object!=null) {
			// Exclude this object
			data.setExcludeList(object);
		}
		
		setModal(true);
		initComponents(component);
	}
	public void setSelectionMode(int mode) {
		objectsPane.setSelectionMode(mode);
	}
	private void initComponents(Component component) {
		setTitle("Game Object Chooser");
		setSize(400,300);
		setLocationRelativeTo(component);
		
		getContentPane().setLayout(new BorderLayout());
			// Game Objects Pane
			Box box = Box.createHorizontalBox();
				JButton button = new JButton("Filter");
				ComponentTools.lockComponentSize(button,80,25);
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						String filter = objectsFilterField.getText();
						data.setFilterString(filter);
						objectsPane.fireChange();
					}
				});
			box.add(button);
				objectsFilterField = new JTextField();
				objectsFilterField.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						String filter = objectsFilterField.getText();
						data.setFilterString(filter);
						objectsPane.fireChange();
					}
				});
			box.add(objectsFilterField);
				button = new JButton("Clear");
				ComponentTools.lockComponentSize(button,80,25);
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						objectsFilterField.setText("");
						data.clearFilterAndExcludeList();
						objectsPane.fireChange();
					}
				});
			box.add(button);
		getContentPane().add(box,"North");
			objectsPane = new ListManagerPane(null,new GameObjectTableModel(data.getFilteredGameObjects()),false,false,false,false,false,false);
		getContentPane().add(objectsPane,"Center");
			box = Box.createHorizontalBox();
			box.add(Box.createHorizontalGlue());
				JButton okay = new JButton("Add");
				okay.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						chosenObjects = getSelectedObjects();
						close();
					}
				});
			box.add(okay);
				JButton cancel = new JButton("Cancel");
				cancel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						chosenObjects = null;
						close();
					}
				});
			box.add(cancel);
		getContentPane().add(box,"South");
	}
	private ArrayList getSelectedObjects() {
		ArrayList objects = data.getFilteredGameObjects();
		ArrayList selObjects = new ArrayList();
		int[] row = objectsPane.getSelectedRows();
		for (int i=0;i<row.length;i++) {
			selObjects.add(objects.get(row[i]));
		}
		return selObjects;
	}
	public ArrayList getChosenObjects() {
		return chosenObjects;
	}
	public void close() {
		data.setFilterString(oldFilterString);
		data.setExcludeList(oldExcludeList);
		setVisible(false);
	}
}