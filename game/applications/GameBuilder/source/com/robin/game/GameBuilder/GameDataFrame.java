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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.event.*;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.game.objects.GameSetup;
import com.robin.general.io.Closeable;
import com.robin.general.io.Modifyable;
import com.robin.general.io.Saveable;
import com.robin.general.swing.ComponentTools;
import com.robin.general.swing.ListManagerPane;

public class GameDataFrame extends JInternalFrame implements Modifyable,Saveable,Closeable {

	protected long cumulative_id = 0;
	private boolean zipfile;

	// Game
	protected JTextArea gameDescField;
	protected JLabel statusField;	
	
	protected File lastPath; // should this be static or not?
	protected File filePath;
	
	protected GameBuilderFrame parent;
	protected GameData data;
	protected Hashtable gameObjectFrames;
	protected Hashtable gameSetupFrames;
	
	public JTextField objectsFilterField;
	protected ListManagerPane objectsPane;
	
	protected ListManagerPane setupPane;
	
	public GameDataFrame(GameBuilderFrame parent,GameData data) {
		super("",true,true,true,true);
		this.parent = parent;
		this.data = data;
		lastPath = null;
		filePath = null;
		gameObjectFrames = new Hashtable();
		gameSetupFrames = new Hashtable();
		data.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ev) {
				updateControls();
			}
		});
		addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent ev) {
				GameDataFrame.this.parent.updateMenu();
			}
			public void focusLost(FocusEvent ev) {
				// nothing
			}
		});
		initComponents();
		setModified(true);
	}
	public GameData getGameData() {
		return data;
	}
	public void setModified(boolean val) {
		data.setModified(true);
	}
	public boolean isModified() {
		return data.isModified();
	}
	public void appClosing(Component component) {
		if (isModified()) {
			int ret = JOptionPane.showConfirmDialog(
					component,
					"Do you want to save changes to "+data.getGameName()+" before closing?",
					data.getGameName()+" has been modified",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (ret==JOptionPane.YES_OPTION) {
				save(component);
			}
		}
	}
	public void close(Component component) {
		if (isModified()) {
			int ret = JOptionPane.showConfirmDialog(
					component,
					"Save before closing?",
					data.getGameName()+" has been modified.",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
					
			if (ret==JOptionPane.YES_OPTION) {
				save(component);
			}
		}
		for (Iterator i=gameObjectFrames.values().iterator();i.hasNext();) {
			GameObjectFrame frame = (GameObjectFrame)i.next();
			frame.setVisible(false);
			parent.getDesktop().remove(frame);
		}
		setVisible(false);
		parent.removeDataFrame(this);
	}
	public void updateLastPath() {
		if (lastPath==null) {
			lastPath = parent.getLastPath();
		}
	}
	public boolean load(Component component) {
		updateLastPath();
		JFileChooser chooser = new JFileChooser(lastPath);
		if (chooser.showOpenDialog(component)==JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (file!=null) {
				if (data.loadFromFile(file)) {
					zipfile = false;
					updateControls();
					setFile(file);
					return true;
				}
				else if (data.zipFromFile(file)) {
					zipfile = true;
					updateControls();
					setFile(file);
					return true;
				}
			}
		}
		return false;
	}
	public boolean save(Component component) {
		updateLastPath();
		if (filePath!=null) {
			if (!zipfile) {
				return data.saveToFile(filePath);
			}
			else {
				return data.zipToFile(filePath);
			}
		}
		else {
			return saveAs(component);
		}
	}
	public boolean saveAs(Component component) {
		updateLastPath();
		JFileChooser chooser = new JFileChooser(lastPath);
		if (chooser.showSaveDialog(component)==JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (file!=null) {
				if (!zipfile) {
					if (data.saveToFile(file)) {
						setFile(file);
					}
				}
				else {
					if (data.zipToFile(file)) {
						setFile(file);
					}
				}
			}
		}
		return false;
	}
	public boolean revert(Component component) {
		updateLastPath();
		if (filePath!=null) {
			return data.loadFromFile(filePath);
		}
		return false;
	}
	private void setFile(File file) {
		filePath = file;
		String path = file.getPath();
		if (!path.endsWith(File.separator)) {
			int pathEnd = path.lastIndexOf(File.separator);
			if (pathEnd!=-1) {
				path = path.substring(0,pathEnd+1);
			}
		}
		lastPath = new File(path);
		parent.setLastPath(lastPath);
	}
	/**
	 * This is an ugly hack - how better to do it?  I'm getting an IllegalAccessException
	 * if I try to do a gameDescField.setText(...)  Maybe I need to disable the CaretListener...
	 */
	public void resetGameDescription() {
			Box top = Box.createVerticalBox();
				Box box = Box.createHorizontalBox();
				box.add(new JLabel("Game Description:"));
				box.add(Box.createHorizontalGlue());
			top.add(box);
				box = Box.createHorizontalBox();
					gameDescField = new JTextArea(data.getGameDescription());
					gameDescField.addCaretListener(new CaretListener() {
						public void caretUpdate(CaretEvent ev) {
							data.setGameDescription(gameDescField.getText());
						}
					});
					JScrollPane scroll = new JScrollPane(gameDescField);
					scroll.setMinimumSize(new Dimension(120,60));
					scroll.setPreferredSize(new Dimension(120,60));
				box.add(scroll);
			top.add(box);
		getContentPane().add(top,"North");
	}
	private void initComponents() {
		Box box;
		JPanel panel;
		JButton button;
		setSize(500,500);
		setContentPane(new JPanel());
		getContentPane().setLayout(new BorderLayout(5,5));
			Box top = Box.createVerticalBox();
				box = Box.createHorizontalBox();
				box.add(new JLabel("Game Description:"));
				box.add(Box.createHorizontalGlue());
			top.add(box);
				box = Box.createHorizontalBox();
					gameDescField = new JTextArea(data.getGameDescription());
					gameDescField.addCaretListener(new CaretListener() {
						public void caretUpdate(CaretEvent ev) {
							data.setGameDescription(gameDescField.getText());
						}
					});
					JScrollPane scroll = new JScrollPane(gameDescField);
					scroll.setMinimumSize(new Dimension(120,60));
					scroll.setPreferredSize(new Dimension(120,60));
				box.add(scroll);
			top.add(box);
		getContentPane().add(top,"North");
			JTabbedPane pane = new JTabbedPane();
			// Game Objects Pane
				panel = new JPanel(new BorderLayout());
					box = Box.createHorizontalBox();
						button = new JButton("Filter");
						ComponentTools.lockComponentSize(button,80,25);
						button.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ev) {
								String filter = objectsFilterField.getText();
								data.setFilterString(filter);
							}
						});
					box.add(button);
						objectsFilterField = new JTextField();
						objectsFilterField.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ev) {
								String filter = objectsFilterField.getText();
								data.setFilterString(filter);
							}
						});
					box.add(objectsFilterField);
						button = new JButton("Clear");
						ComponentTools.lockComponentSize(button,80,25);
						button.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ev) {
								objectsFilterField.setText("");
								data.clearFilterAndExcludeList();
							}
						});
					box.add(button);
				panel.add(box,"North");
					objectsPane = new ListManagerPane(null,new GameObjectTableModel(data.getFilteredGameObjects())) {
						public void add() {
							GameObject obj = data.createNewObject();
							GameObjectFrame of = getObjectFrame(obj);
							popUpInternalFrame(of);
						}
						public void duplicate() {
							int[] row = getSelectedRows();
							
							ArrayList objectsToDuplicate = new ArrayList();
							for (int i=0;i<row.length;i++) {
								GameObject obj = (GameObject)data.getFilteredGameObjects().get(row[i]);
								objectsToDuplicate.add(obj);
							}
							
							for (Iterator i=objectsToDuplicate.iterator();i.hasNext();) {
								GameObject selObj = (GameObject)i.next();
								GameObject obj = data.createNewObject();
								obj.copyAttributesFrom(selObj);
							}
							data.rebuildFilteredGameObjects();
							// no need to pop up frames
						}
						public void delete() {
							int[] row = getSelectedRows();
							
							// First get all selected objects
							ArrayList delObjects = new ArrayList();
							for (int i=0;i<row.length;i++) {
								GameObject obj = (GameObject)data.getFilteredGameObjects().get(row[i]);
								delObjects.add(obj);
							}
							
							// Now delete them
							for (Iterator i=delObjects.iterator();i.hasNext();) {
								GameObject obj = (GameObject)i.next();
								data.removeObject(obj);
								GameObjectFrame of = getObjectFrame(obj);
								if (parent.getDesktop().getIndexOf(of)!=-1) {
									parent.getDesktop().remove(of);
								}
							}
							setModified(true);
						}
						public void edit() {
							int row = getSelectedRow();
							GameObject obj = (GameObject)data.getFilteredGameObjects().get(row);
							GameObjectFrame of = getObjectFrame(obj);
							popUpInternalFrame(of);
						}
						public void globalEdit(boolean removingChange) {
							String blockName = JOptionPane.showInputDialog(this,"BlockName");
							if (blockName!=null) {
								String key = JOptionPane.showInputDialog(this,"Key");
								if (key!=null) {
									String val;
									if (removingChange) {
										val = "";
									}
									else {
										val = JOptionPane.showInputDialog(this,"Value");
									}
									
									if (val!=null) {
										ArrayList editObjects = new ArrayList(data.getFilteredGameObjects());
										int[] row = getSelectedRows();
										if (removingChange) {
											// Remove attribute from all selected objects
											for (int i=0;i<row.length;i++) {
												GameObject obj = (GameObject)editObjects.get(row[i]);
												obj.removeAttribute(blockName,key);
											}
										}
										else {
											// Add attribute to all selected objects
											for (int i=0;i<row.length;i++) {
												GameObject obj = (GameObject)editObjects.get(row[i]);
												obj.setAttribute(blockName,key,val);
											}
										}
										setModified(true);
									}
								}
							}
						}
						public void shiftBlock(int direction) {
							int[] row = getSelectedRows();
							
							// First get all selected objects
							int min = Integer.MAX_VALUE;
							int max = Integer.MIN_VALUE;
							ArrayList shiftObjects = new ArrayList();
							for (int i=0;i<row.length;i++) {
								GameObject obj = (GameObject)data.getFilteredGameObjects().get(row[i]);
								shiftObjects.add(obj);
								min = Math.min(row[i],min);
								max = Math.max(row[i],max);
							}
							
							if (direction==1) {
								// Down
								if ((max+1)<data.getFilteredGameObjects().size()) {
									GameObject obj = (GameObject)data.getFilteredGameObjects().get(max+1);
									data.moveObjectsAfter(shiftObjects,obj);
									updateSelection(shiftObjects);
								}
							}
							else {
								// Up
								if ((min-1)>=0) {
									GameObject obj = (GameObject)data.getFilteredGameObjects().get(min-1);
									data.moveObjectsBefore(shiftObjects,obj);
									updateSelection(shiftObjects);
								}
							}
							
						}
						public void updateSelection(ArrayList objects) {
							int[] row = new int[objects.size()];
							int n=0;
							for (Iterator i=objects.iterator();i.hasNext();) {
								row[n++] = data.getFilteredGameObjects().indexOf(i.next());
							}
							setSelectedRows(row);
						}
					};
				panel.add(objectsPane,"Center");
			pane.addTab("Objects",panel);
			// Game Setup Pane
				setupPane = new ListManagerPane(null,new GameSetupTableModel(data.getGameSetups())) {
					public void add() {
						GameSetup setup = data.createNewSetup();
						GameSetupFrame sf = getSetupFrame(setup);
						popUpInternalFrame(sf);
					}
					public void duplicate() {
						int row = getSelectedRow();
						GameSetup selSetup = (GameSetup)data.getGameSetups().get(row);
						GameSetup setup = data.createNewSetup();
						setup.copyCommandsFrom(selSetup);
						// no need to pop up a frame
					}
					public void delete() {
						int[] row = getSelectedRows();
						
						// First get all selected objects
						ArrayList delSetups = new ArrayList();
						for (int i=0;i<row.length;i++) {
							GameSetup setup = (GameSetup)data.getGameSetups().get(row[i]);
							delSetups.add(setup);
						}
						
						// Now delete them
						for (Iterator i=delSetups.iterator();i.hasNext();) {
							GameSetup setup = (GameSetup)i.next();
							data.removeSetup(setup);
							GameSetupFrame sf = getSetupFrame(setup);
							if (parent.getDesktop().getIndexOf(sf)!=-1) {
								parent.getDesktop().remove(sf);
							}
						}
						setModified(true);
					}
					public void edit() {
						int row = getSelectedRow();
						GameSetup setup = (GameSetup)data.getGameSetups().get(row);
						GameSetupFrame sf = getSetupFrame(setup);
						popUpInternalFrame(sf);
					}
				};
			pane.addTab("Setup",setupPane);
		getContentPane().add(pane,"Center");
			statusField = new JLabel(" ");
		getContentPane().add(statusField,"South");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosing(InternalFrameEvent ev) {
				close(parent);
			}
		});
	}
	public void popUpInternalFrame(JInternalFrame frame) {
		frame.setVisible(true);
		if (parent.getDesktop().getIndexOf(frame)==-1) {
			parent.getDesktop().add(frame);
		}
		frame.moveToFront();
		try {
			frame.setSelected(true);
		}
		catch(PropertyVetoException ex) {
		}
	}
	public void updateControls() {
		objectsPane.fireChange();
		setupPane.fireChange();
		setTitle(data.getGameName()+(data.isModified()?"*":""));
		statusField.setText("   "+data.getFilteredGameObjects().size()+" out of "+data.getGameObjects().size());
		parent.updateMenu();
	}
	public GameObjectFrame getObjectFrame(GameObject obj) {
		GameObjectFrame frame = null;
		if (obj!=null) {
			frame = (GameObjectFrame)gameObjectFrames.get(obj.getBarcode());
		}
		if (frame==null) {
			frame = createNewObjectFrame(obj);
		}
		return frame;
	}
	
	public GameObjectFrame createNewObjectFrame(GameObject obj) {
		GameObjectFrame frame = new GameObjectFrame(this,obj);
		Point p = getLocation();
		p.x += 20;
		p.y += 20;
		frame.setLocation(p);
		gameObjectFrames.put(obj.getBarcode(),frame);
		return frame;
	}
	public GameSetupFrame getSetupFrame(GameSetup setup) {
		GameSetupFrame frame = null;
		if (setup!=null) {
			frame = (GameSetupFrame)gameSetupFrames.get(setup.getBarcode());
		}
		if (frame==null) {
			frame = createNewSetupFrame(setup);
		}
		return frame;
	}
	
	public GameSetupFrame createNewSetupFrame(GameSetup setup) {
		GameSetupFrame frame = new GameSetupFrame(this,setup);
		Point p = getLocation();
		p.x += 20;
		p.y += 20;
		frame.setLocation(p);
		gameSetupFrames.put(setup.getBarcode(),frame);
		return frame;
	}
	public String toString() {
		return "GameDataFrame for "+data;
	}
}