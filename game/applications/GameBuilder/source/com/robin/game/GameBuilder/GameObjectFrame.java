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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;

import com.robin.game.objects.GameObject;
import com.robin.general.io.*;
import com.robin.general.swing.ComponentTools;
import com.robin.general.swing.ListManagerPane;
import com.robin.general.util.OrderedHashtable;

public class GameObjectFrame extends JInternalFrame implements Modifyable,Saveable,Closeable {
	
	private static Font ID_FONT = new Font("Dialog",Font.BOLD,12);

	protected JTextField nameField;
	protected JButton applyNameButton;
	protected JLabel statusField;
	protected ListManagerPane blocksPane;
	protected ListManagerPane attributesPane;
	protected ListManagerPane containsPane;
	
	protected JLabel currentIdLabel;
	protected JLabel heldByLabel;
	
	protected String currentBlockName = null;
	
	protected GameObject object;
	
	protected GameDataFrame parent;
	
	public GameObjectFrame(GameDataFrame parent,GameObject object) {
		super("",true,true,true,true);
		this.parent = parent;
		this.object = object;
		object.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ev) {
				updateControls();
			}
		});
		initComponents();
		updateControls();
		blocksPane.setSelectedRow(0);
	}
	private void updateControls() {
		blocksPane.fireChange();
		attributesPane.fireChange();
		currentIdLabel.setText(object.getStringId());
		heldByLabel.setText(object.getHeldBy()==null?"":object.getHeldBy().toString());
	}
	private void initComponents() {
		Box box;
		setSize(420,600);
		setMinimumSize(new Dimension(400,200));
		setContentPane(new JPanel());
		setTitle(object.getFullTitle());
		getContentPane().setLayout(new BorderLayout(5,5));
			JPanel top = new JPanel(new GridLayout(2,1));
			box = Box.createHorizontalBox();
			currentIdLabel = new JLabel("",JLabel.CENTER);
			currentIdLabel.setFont(ID_FONT);
			currentIdLabel.setBackground(Color.white);
			currentIdLabel.setOpaque(true);
			currentIdLabel.setBorder(BorderFactory.createEtchedBorder());
			ComponentTools.lockComponentSize(currentIdLabel,50,25);
			box.add(currentIdLabel);
			box.add(Box.createHorizontalStrut(10));
			box.add(new JLabel("Object name:"));
			box.add(Box.createHorizontalStrut(5));
				nameField = new JTextField(object.getName());
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
			top.add(box);
			box = Box.createHorizontalBox();
			box.add(Box.createHorizontalStrut(10));
			box.add(new JLabel("Held By:"));
			box.add(Box.createHorizontalStrut(5));
			heldByLabel = new JLabel();
			box.add(heldByLabel);
			top.add(box);
		getContentPane().add(top,"North");
			JTabbedPane pane = new JTabbedPane();
				JPanel panel = new JPanel(new BorderLayout());
					blocksPane = new ListManagerPane("Blocks",new GameBlockTableModel(object.getAttributeBlocks()),true,false,true,true,false,false) {
						public void add() {
							String blockName = JOptionPane.showInputDialog("Block Name?");
							if (blockName!=null) {
								if (object.getAttributeBlocks().get(blockName)==null) {
									object.getAttributeBlock(blockName);
									blocksPane.fireChange();
									setModified(true);
									// should I launch an attributes "add" here?
								}
								else {
									JOptionPane.showMessageDialog(this,"Duplicate block name","Error",JOptionPane.ERROR_MESSAGE);
								}
							}
						}
						public void delete() {
							int[] rows = blocksPane.getSelectedRows();
							ArrayList blocksToDelete = new ArrayList();
							for (int i=0;i<rows.length;i++) {
								blocksToDelete.add(object.getAttributeBlocks().getKey(rows[i]));
							}
							for (Iterator i=blocksToDelete.iterator();i.hasNext();) {
								object.getAttributeBlocks().remove(i.next());
							}
							blocksPane.fireChange();
							currentBlockName = null;
							attributesPane.fireChange();
							setModified(true);
							blocksPane.setSelectedRow(rows[0]); // this doesn't friggen work... why??!?!
						}
						public void edit() {
							String newBlockName = (String)JOptionPane.showInputDialog(this,"Block Name?","Input",JOptionPane.QUESTION_MESSAGE,null,null,currentBlockName);
							if (newBlockName!=null && newBlockName.length()>0 && !currentBlockName.equals(newBlockName)) {
								OrderedHashtable block = (OrderedHashtable)object.getAttributeBlocks().get(currentBlockName);
								int index = object.getAttributeBlocks().indexOf(block);
								object.getAttributeBlocks().remove(currentBlockName);
								object.getAttributeBlocks().replace(index,newBlockName,block);
								currentBlockName = newBlockName;
								blocksPane.fireChange();
								setModified(true);
							}
						}
					};
					ComponentTools.minimizeComponentSize(blocksPane,160,25);
					blocksPane.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				panel.add(blocksPane,"West");
					attributesPane = new ListManagerPane("Attributes",new DefaultTableModel(),true,false,true,true,false,false) {
						public void add() {
							OrderedHashtable block = (OrderedHashtable)object.getAttributeBlocks().get(currentBlockName);
							String key = JOptionPane.showInputDialog(this,"Key");
							if (key!=null) {
								if (AttributeEditor.editBlock(parent.parent,this,"Value",block,key)) {
									setModified(true);
								}
							}
						}
						public void delete() {
							OrderedHashtable block = (OrderedHashtable)object.getAttributeBlocks().get(currentBlockName);
							
							int[] rows = attributesPane.getSelectedRows();
							ArrayList blocksToDelete = new ArrayList();
							for (int i=0;i<rows.length;i++) {
								blocksToDelete.add(block.getKey(rows[i]));
							}
							for (Iterator i=blocksToDelete.iterator();i.hasNext();) {
								block.remove(i.next());
							}
							setModified(true);
						}
						public void edit() {
							OrderedHashtable block = (OrderedHashtable)object.getAttributeBlocks().get(currentBlockName);
							int row = attributesPane.getSelectedRow();
							String key = (String)block.getKey(row);
							if (AttributeEditor.editBlock(parent.parent,this,"New Value",block,key)) {
								setModified(true);
							}
						}
					};
					ComponentTools.minimizeComponentSize(attributesPane,160,25);
					blocksPane.addListSelectionListener(new ListSelectionListener() {
						public void valueChanged(ListSelectionEvent ev) {
							int row = blocksPane.getSelectedRow();
							if (row!=-1) {
								currentBlockName = (String)object.getAttributeBlocks().getKey(row);
								OrderedHashtable block = (OrderedHashtable)object.getAttributeBlocks().getValue(row);
								attributesPane.setModel(new GameAttributeTableModel(block));
							}
							else {
								currentBlockName = null;
								attributesPane.setModel(new DefaultTableModel());
							}
						}
					});
				panel.add(attributesPane,"Center");
			pane.addTab("Attribute Blocks",panel);
				containsPane = new ListManagerPane(null,new GameObjectTableModel(object.getHold()),true,false,true,false,false,false) {
					public void add() {
						GameObjectChooser chooser = new GameObjectChooser(this,object);
						chooser.setVisible(true);
						ArrayList chosenObjects = chooser.getChosenObjects();
						if (chosenObjects!=null) {
							for (Iterator i=chosenObjects.iterator();i.hasNext();) {
								GameObject chosenObject = (GameObject)i.next();
								object.add(chosenObject);
							}
							setModified(true);
						}
					}
					public void delete() {
						ArrayList hold = (ArrayList)object.getHold();
						
						int[] rows = containsPane.getSelectedRows();
						ArrayList holdToDelete = new ArrayList();
						for (int i=0;i<rows.length;i++) {
							holdToDelete.add(hold.get(rows[i]));
						}
						for (Iterator i=holdToDelete.iterator();i.hasNext();) {
							object.remove((GameObject)i.next());
						}
						setModified(true);
					}
				};
			pane.addTab("Contains",containsPane);
		getContentPane().add(pane,"Center");
			statusField = new JLabel(" "); // Do I really need this for anything?
		getContentPane().add(statusField,"South");
	}
	public void applyName() {
		String newName = nameField.getText().trim();
		if (newName.length()>0) {
			object.setName(newName);
			setTitle(object.getFullTitle());
		}
		else {
			// revert to old
			nameField.setText(object.getName());
		}
		statusField.grabFocus();
	}
	
	// Modifyable interface
	public void setModified(boolean val) {
		object.setModified(val);
	}
	public boolean isModified() {
		return object.isModified();
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