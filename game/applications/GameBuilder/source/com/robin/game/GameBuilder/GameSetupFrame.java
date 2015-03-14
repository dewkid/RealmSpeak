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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.robin.game.objects.*;
import com.robin.general.io.Closeable;
import com.robin.general.io.Modifyable;
import com.robin.general.io.Saveable;
import com.robin.general.swing.ComponentTools;
import com.robin.general.swing.ListManagerPane;

public class GameSetupFrame extends JInternalFrame implements Modifyable,Saveable,Closeable {

	protected JTextField nameField;
	protected JButton applyNameButton;
	protected JLabel statusField;
	protected ListManagerPane commandsPane;
	
	protected String currentBlockName = null;
	
	protected GameSetup setup;
	
	protected GameDataFrame parent;
	
	public GameSetupFrame(GameDataFrame parent,GameSetup setup) {
		super("",true,true,true,true);
		this.parent = parent;
		this.setup = setup;
		setup.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ev) {
				updateControls();
			}
		});
		initComponents();
	}
	private void updateControls() {
		commandsPane.fireChange();
	}
	private void initComponents() {
		Box box;
//		JInternalFrame frame = new JInternalFrame("",true,true,true,true);
		setSize(800,600);
		setMinimumSize(new Dimension(400,200));
		setContentPane(new JPanel());
		setTitle(setup.getFullTitle());
		getContentPane().setLayout(new BorderLayout(5,5));
			box = Box.createHorizontalBox();
			box.add(new JLabel("Setup name:"));
				nameField = new JTextField(setup.getName());
				nameField.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						applyName();
					}
				});
				ComponentTools.lockComponentSize(nameField,120,25);
			box.add(nameField);
			box.add(Box.createHorizontalStrut(5));
				applyNameButton = new JButton("Apply");
				applyNameButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						applyName();
					}
				});
				ComponentTools.lockComponentSize(applyNameButton,80,25);
			box.add(applyNameButton);
			box.add(Box.createHorizontalGlue());
		getContentPane().add(box,"North");
			commandsPane = new ListManagerPane("Commands:",new GameCommandTableModel(setup.getGameCommands()),true,true,true,true,false,true) {
				public void add() {
					int row = getSelectedRow();
					GameCommand command = setup.createNewCommand(row);
					GameCommandDialog chooser = new GameCommandDialog(setup.getGameCommands(),command);
					chooser.setLocationRelativeTo(this);
					chooser.setVisible(true);
					setModified(true);
				}
				public void duplicate() {
					int row = getSelectedRow();
					GameCommand command = (GameCommand)setup.getGameCommands().get(row);
					GameCommand dupCommand = setup.createNewCommand();
					dupCommand.copyFrom(command);
					setModified(true);
				}
				public void delete() {
					int[] row = getSelectedRows();
					
					// First get all selected commands
					ArrayList delCommands = new ArrayList();
					for (int i=0;i<row.length;i++) {
						GameCommand command = (GameCommand)setup.getGameCommands().get(row[i]);
						delCommands.add(command);
					}
					
					// Now delete them
					for (Iterator i=delCommands.iterator();i.hasNext();) {
						GameCommand command = (GameCommand)i.next();
						setup.removeCommand(command);
					}
					setModified(true);
				}
				public void edit() {
					int row = commandsPane.getSelectedRow();
					GameCommand command = (GameCommand)setup.getGameCommands().get(row);
					GameCommandDialog chooser = new GameCommandDialog(setup.getGameCommands(),command);
					chooser.setLocationRelativeTo(this);
					chooser.setVisible(true);
				}
				public void shiftBlock(int direction) {
					int[] row = getSelectedRows();
					
					// First get all selected objects
					int min = Integer.MAX_VALUE;
					int max = Integer.MIN_VALUE;
					ArrayList shiftObjects = new ArrayList();
					for (int i=0;i<row.length;i++) {
						GameCommand command = (GameCommand)setup.getGameCommands().get(row[i]);
						shiftObjects.add(command);
						min = Math.min(row[i],min);
						max = Math.max(row[i],max);
					}
					
					if (direction==1) {
						// Down
						if ((max+1)<setup.getGameCommands().size()) {
							GameCommand command = (GameCommand)setup.getGameCommands().get(max+1);
							setup.moveObjectsAfter(shiftObjects,command);
							updateSelection(shiftObjects);
						}
					}
					else {
						// Up
						if ((min-1)>=0) {
							GameCommand command = (GameCommand)setup.getGameCommands().get(min-1);
							setup.moveObjectsBefore(shiftObjects,command);
							updateSelection(shiftObjects);
						}
					}
					setModified(true);
				}
				public void updateSelection(ArrayList objects) {
					int[] row = new int[objects.size()];
					int n=0;
					for (Iterator i=objects.iterator();i.hasNext();) {
						row[n++] = setup.getGameCommands().indexOf(i.next());
					}
					setSelectedRows(row);
				}
			};
		getContentPane().add(commandsPane,"Center");
			statusField = new JLabel(" "); // Do I really need this for anything?
		getContentPane().add(statusField,"South");
	}
	public void applyName() {
		String newName = nameField.getText().trim();
		if (newName.length()>0) {
			setup.setName(newName);
			setTitle(setup.getFullTitle());
		}
		else {
			// revert to old
			nameField.setText(setup.getName());
		}
		statusField.grabFocus();
	}
	
	// Modifyable interface
	public void setModified(boolean val) {
		setup.setModified(val);
	}
	public boolean isModified() {
		return setup.isModified();
	}
	
	// Saveable interface
	public boolean save(Component component) {
		return parent.save(component);
	}
	public boolean saveAs(Component component) {
		return parent.saveAs(component);
	}
	
	// Closeable interface
	public void close(Component component) {
		parent.close(component);
	}
}