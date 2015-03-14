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
package com.robin.magic_realm.RealmSpeak;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.general.io.ArgumentParser;
import com.robin.general.io.ResourceFinder;
import com.robin.general.swing.ButtonOptionDialog;
import com.robin.general.swing.ComponentTools;
import com.robin.magic_realm.components.ClearingDetail;
import com.robin.magic_realm.components.PathDetail;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.TileEditComponent;
import com.robin.magic_realm.components.utility.RealmUtility;

public class TileEditFrame extends JFrame {

	protected GameData data;
	
	protected JButton saveButton;
	
	protected JPanel tileView;
	protected TileEditComponent activeTile;
	protected JButton flipButton;
	protected JButton applyButton;
	protected JButton toggleDetailButton;
	protected JList tileList;
	
	protected boolean selectionLock = false;
	protected boolean editOffroad = false;
	protected boolean editClearing = false;
	protected boolean editPath = false;
	
	protected JLabel changeWarningLabel;
	
	protected JPanel clearingView;
		protected JList clearingList;
		protected Box clearingControls;
			protected ButtonGroup clearingTypeGroup;
				protected JRadioButton normalClearingType;
				protected JRadioButton woodsClearingType;
				protected JRadioButton mountainClearingType;
				protected JRadioButton caveClearingType;
			
				protected JCheckBox whiteClearingMagic;		// W
				protected JCheckBox grayClearingMagic;		// Y
				protected JCheckBox goldClearingMagic;		// G
				protected JCheckBox purpleClearingMagic;	// P
				protected JCheckBox blackClearingMagic;		// B
				
	protected JPanel pathView;
		protected JList pathList;
		protected JPanel pathControls;
			protected ButtonGroup pathTypeGroup;
				protected JRadioButton normalPathType;
				protected JRadioButton hiddenPathType;
				protected JRadioButton secretPathType;
				protected JRadioButton cavePathType;
				protected JButton addPathButton;
				protected JButton removePathButton;
			protected JButton moveUp;
			protected JButton moveDn;
			protected JButton clearArc;
	protected JToggleButton markOffroad;
	
	private boolean changed = false;

	public TileEditFrame(GameData data) {
		this.data = data;
		initComponents();
	}
	private void initComponents() {
		setTitle("Tile Editor");
		setSize(900,600);
		getContentPane().setLayout(new BorderLayout());
		setLocation(50,50);
		
		Box box;
		JScrollPane sp;
			
		tileList = new JList(getTiles());
		tileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tileList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent ev) {
				if (activeTile!=null && activeTile.isChanged()) {
					int ret = 
						JOptionPane.showConfirmDialog(TileEditFrame.this,"You haven't applied your changes.  Apply them now?","Warning!",JOptionPane.YES_NO_OPTION);
					if (ret==JOptionPane.YES_OPTION) {
						applyButton.doClick();
					}
				}
				updateTileView();
				editOffroad = false;
				editClearing = false;
				editPath = false;
			}
		});
		sp = new JScrollPane(tileList);
		ComponentTools.lockComponentSize(sp,200,100);
		getContentPane().add(sp,"West");
		
			JPanel editPanel = new JPanel(new GridLayout(2,1));
				JPanel clearingEditPanel = new JPanel(new BorderLayout());
				clearingEditPanel.add(new JLabel("Clearings:"),"North");
					clearingList = new JList();
					clearingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					clearingList.addListSelectionListener(new ListSelectionListener() {
						public void valueChanged(ListSelectionEvent ev) {
							if (!selectionLock) {
								selectionLock = true;
								updateClearingButtons();
								pathList.clearSelection();
								markOffroad.setSelected(false);
								editOffroad = false;
								editClearing = true;
								editPath = false;
								selectionLock = false;
							}
						}
					});
					sp = new JScrollPane(clearingList);
					ComponentTools.lockComponentSize(sp,220,100);
				clearingEditPanel.add(sp,"Center");
					clearingControls = Box.createVerticalBox();
						JPanel clearingTypeButtons = new JPanel(new GridLayout(2,2));
							clearingTypeGroup = new ButtonGroup();
								normalClearingType = new JRadioButton("Normal");
								normalClearingType.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent ev) {
										updateClearings();
									}
								});
							clearingTypeGroup.add(normalClearingType);
							clearingTypeButtons.add(normalClearingType);
								woodsClearingType = new JRadioButton("Woods");
								woodsClearingType.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent ev) {
										updateClearings();
									}
								});
							clearingTypeGroup.add(woodsClearingType);
							clearingTypeButtons.add(woodsClearingType);
								mountainClearingType = new JRadioButton("Mountain");
								mountainClearingType.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent ev) {
										updateClearings();
									}
								});
							clearingTypeGroup.add(mountainClearingType);
							clearingTypeButtons.add(mountainClearingType);
								caveClearingType = new JRadioButton("Caves");
								caveClearingType.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent ev) {
										updateClearings();
									}
								});
							clearingTypeGroup.add(caveClearingType);
							clearingTypeButtons.add(caveClearingType);
					clearingControls.add(clearingTypeButtons);
					clearingControls.add(new JSeparator());
						JPanel clearingColorButtons = new JPanel(new GridLayout(2,3));
							whiteClearingMagic = new JCheckBox("White");
							whiteClearingMagic.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent ev) {
									updateClearings();
								}
							});
						clearingColorButtons.add(whiteClearingMagic);
							grayClearingMagic = new JCheckBox("Gray");
							grayClearingMagic.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent ev) {
									updateClearings();
								}
							});
						clearingColorButtons.add(grayClearingMagic);
							goldClearingMagic = new JCheckBox("Gold");
							goldClearingMagic.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent ev) {
									updateClearings();
								}
							});
						clearingColorButtons.add(goldClearingMagic);
							purpleClearingMagic = new JCheckBox("Purple");
							purpleClearingMagic.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent ev) {
									updateClearings();
								}
							});
						clearingColorButtons.add(purpleClearingMagic);
							blackClearingMagic = new JCheckBox("Black");
							blackClearingMagic.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent ev) {
									updateClearings();
								}
							});
						clearingColorButtons.add(blackClearingMagic);
						clearingColorButtons.add(Box.createGlue());
					clearingControls.add(clearingColorButtons);
				clearingEditPanel.add(clearingControls,"South");
			editPanel.add(clearingEditPanel);
				JPanel pathEditPanel = new JPanel(new BorderLayout());
				pathEditPanel.add(new JLabel("Paths:"),"North");
					pathList = new JList();
					pathList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					pathList.addListSelectionListener(new ListSelectionListener() {
						public void valueChanged(ListSelectionEvent ev) {
							if (!selectionLock) {
								selectionLock = true;
								updatePathButtons();
								clearingList.clearSelection();
								markOffroad.setSelected(false);
								editOffroad = false;
								editClearing = false;
								editPath = true;
								selectionLock = false;
							}
						}
					});
					sp = new JScrollPane(pathList);
					ComponentTools.lockComponentSize(sp,220,100);
				pathEditPanel.add(sp,"Center");
					pathControls = new JPanel(new BorderLayout());
						JPanel pathButtons = new JPanel(new GridLayout(3,2));
							pathTypeGroup = new ButtonGroup();
								normalPathType = new JRadioButton("Normal");
								normalPathType.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent ev) {
										updatePaths();
									}
								});
							pathTypeGroup.add(normalPathType);
							pathButtons.add(normalPathType);
								hiddenPathType = new JRadioButton("Hidden");
								hiddenPathType.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent ev) {
										updatePaths();
									}
								});
							pathTypeGroup.add(hiddenPathType);
							pathButtons.add(hiddenPathType);
								secretPathType = new JRadioButton("Secret");
								secretPathType.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent ev) {
										updatePaths();
									}
								});
							pathTypeGroup.add(secretPathType);
							pathButtons.add(secretPathType);
								cavePathType = new JRadioButton("Caves");
								cavePathType.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent ev) {
										updatePaths();
									}
								});
							pathTypeGroup.add(cavePathType);
							pathButtons.add(cavePathType);
								addPathButton = new JButton("Add");
								addPathButton.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent ev) {
										addPath();
									}
								});
							pathTypeGroup.add(addPathButton);
							pathButtons.add(addPathButton);
								removePathButton = new JButton("Remove");
								removePathButton.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent ev) {
										removePath();
									}
								});
							pathTypeGroup.add(removePathButton);
							pathButtons.add(removePathButton);
					pathControls.add(pathButtons,"Center");
						box = Box.createHorizontalBox();
							moveUp = new JButton("UP");
							moveUp.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent ev) {
									movePathUp();
								}
							});
							moveDn = new JButton("DOWN");
							moveDn.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent ev) {
									movePathDown();
								}
							});
							clearArc = new JButton("ClearARC");
							clearArc.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent ev) {
									addPathArcPoint(null);
								}
							});
						box.add(moveUp);
						box.add(moveDn);
						box.add(clearArc);
					pathControls.add(box,"South");
				pathEditPanel.add(pathControls,"South");
			editPanel.add(pathEditPanel);
		getContentPane().add(editPanel,"East");
		
			tileView = new JPanel(new BorderLayout());
				box = Box.createHorizontalBox();
					flipButton = new JButton("Flip Tile");
					flipButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							if (activeTile!=null) {
								activeTile.flip();
								updateClearingList();
								updatePathList();
								repaint();
							}
						}
					});
				box.add(flipButton);
				box.add(Box.createHorizontalGlue());
				changeWarningLabel = new JLabel("Tile has changes!");
				changeWarningLabel.setFont(new Font("Dialog",Font.BOLD,18));
				changeWarningLabel.setForeground(Color.red);
				changeWarningLabel.setVisible(false);
				box.add(changeWarningLabel);
				box.add(Box.createHorizontalGlue());
					toggleDetailButton = new JButton("Toggle Detail");
					toggleDetailButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							RealmComponent.fullDetail = !RealmComponent.fullDetail;
							repaint();
						}
					});
				box.add(toggleDetailButton);
			tileView.add(box,"North");
				box = Box.createHorizontalBox();
					saveButton = new JButton("Save File");
					saveButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							changed = false;
							saveFile();
							updateControls();
						}
					});
				box.add(saveButton);
				box.add(Box.createHorizontalGlue());
					markOffroad = new JToggleButton("Mark Offroad",false);
					markOffroad.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							if (activeTile!=null) {
								selectionLock = true;
								clearingList.clearSelection();
								pathList.clearSelection();
								editOffroad = true;
								editClearing = false;
								editPath = false;
								selectionLock = false;
								
								updateClearingList();
								updatePathList();
							}
						}
					});
				box.add(markOffroad);
				box.add(Box.createHorizontalGlue());
					applyButton = new JButton("Apply");
					applyButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							if (activeTile!=null) {
								activeTile.applyChanges();
								if (activeTile.getFacingIndex()==1) {
									activeTile.flip();
									activeTile.repaint();
								}
								changed = true;
								updateControls();
							}
						}
					});
				box.add(applyButton);
			tileView.add(box,"South");
			MouseInputAdapter mouse = new MouseInputAdapter() {
				public void mousePressed(MouseEvent ev) {
					Point origin = activeTile.getLocation();
					Point mp = ev.getPoint();
					mp.x -= origin.x;
					mp.y -= origin.y;
					if (editClearing) {
						changeClearingPos(mp);
					}
					else if (editPath) {
						addPathArcPoint(mp);
					}
					else if (editOffroad) {
						setOffroadPos(mp);
					}
				}
				public void mouseDragged(MouseEvent ev) {
					mousePressed(ev);
				}
			};
			tileView.addMouseListener(mouse);
			tileView.addMouseMotionListener(mouse);
		getContentPane().add(tileView,"Center");
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				System.exit(0);
			}
		});
		
		updateControls();
	}
	public void updateControls() {
		updateClearingButtons();
		updatePathButtons();
		changeWarningLabel.setVisible(activeTile!=null && activeTile.isChanged());
		
		saveButton.setEnabled(changed);
		applyButton.setEnabled(activeTile!=null && activeTile.isChanged());
	}
	public void updateTileView() {
		if (activeTile!=null) {
			tileView.remove(activeTile);
		}
		GameObject tile = (GameObject)tileList.getSelectedValue();
		if (tile!=null) {
			activeTile = new TileEditComponent(tile);
			activeTile.initSize();
			tileView.add(activeTile,"Center");
			tileView.revalidate();
		}
		else {
			activeTile=null;
		}
		updatePathList();
		updateClearingList();
		updateControls();
		repaint();
	}
	public void updateClearingList() {
		if (activeTile!=null) {
			clearingList.setListData(new Vector(activeTile.getClearingDetail()));
		}
		else {
			clearingList.setListData(new Vector());
		}
		clearingList.revalidate();
		clearingList.repaint();
	}
	public void updateClearingButtons() {
		ClearingDetail selected = null;
		if (activeTile!=null) {
			selected = (ClearingDetail)clearingList.getSelectedValue();
			if (selected!=null) {
				String type = selected.getType();
				if (type.equals("normal")) {
					normalClearingType.setSelected(true);
				}
				else if (type.equals("woods")) {
					woodsClearingType.setSelected(true);
				}
				else if (type.equals("mountain")) {
					mountainClearingType.setSelected(true);
				}
				else if (type.equals("caves")) {
					caveClearingType.setSelected(true);
				}
				
				whiteClearingMagic.setSelected(selected.getMagic(ClearingDetail.MAGIC_WHITE));
				grayClearingMagic.setSelected(selected.getMagic(ClearingDetail.MAGIC_GRAY));
				goldClearingMagic.setSelected(selected.getMagic(ClearingDetail.MAGIC_GOLD));
				purpleClearingMagic.setSelected(selected.getMagic(ClearingDetail.MAGIC_PURPLE));
				blackClearingMagic.setSelected(selected.getMagic(ClearingDetail.MAGIC_BLACK));
			}
		}
		
		normalClearingType.setEnabled(activeTile!=null && selected!=null);
		woodsClearingType.setEnabled(activeTile!=null && selected!=null);
		mountainClearingType.setEnabled(activeTile!=null && selected!=null);
		caveClearingType.setEnabled(activeTile!=null && selected!=null);
		
		whiteClearingMagic.setEnabled(activeTile!=null && selected!=null);
		grayClearingMagic.setEnabled(activeTile!=null && selected!=null);
		goldClearingMagic.setEnabled(activeTile!=null && selected!=null);
		purpleClearingMagic.setEnabled(activeTile!=null && selected!=null);
		blackClearingMagic.setEnabled(activeTile!=null && selected!=null);
	}
	public void updateClearings() {
		ClearingDetail selected = null;
		if (activeTile!=null) {
			selected = (ClearingDetail)clearingList.getSelectedValue();
			if (selected!=null) {
				if (normalClearingType.isSelected()) {
					selected.setType("normal");
				}
				else if (woodsClearingType.isSelected()) {
					selected.setType("woods");
				}
				else if (mountainClearingType.isSelected()) {
					selected.setType("mountain");
				}
				else if (caveClearingType.isSelected()) {
					selected.setType("caves");
				}
				
				selected.setMagic(ClearingDetail.MAGIC_WHITE,whiteClearingMagic.isSelected());
				selected.setMagic(ClearingDetail.MAGIC_GRAY,grayClearingMagic.isSelected());
				selected.setMagic(ClearingDetail.MAGIC_GOLD,goldClearingMagic.isSelected());
				selected.setMagic(ClearingDetail.MAGIC_PURPLE,purpleClearingMagic.isSelected());
				selected.setMagic(ClearingDetail.MAGIC_BLACK,blackClearingMagic.isSelected());
				
				activeTile.repaint();
				
				activeTile.didChange();
				updateControls();
			}
		}
	}
	public void changeClearingPos(Point p) {
		if (activeTile!=null) {
			ClearingDetail selected = (ClearingDetail)clearingList.getSelectedValue();
			if (selected!=null) {
				activeTile.didChange();
				updateControls();
				selected.setPosition(p);
				activeTile.repaint();
			}
		}
	}
	public void updatePathList() {
		updatePathList(-1);
	}
	public void updatePathList(int newIndex) {
		if (activeTile!=null) {
			pathList.setListData(new Vector(activeTile.getPathDetail()));
		}
		else {
			pathList.setListData(new Vector());
		}
		if (newIndex>=0) {
			pathList.setSelectedIndex(newIndex);
		}
		pathList.revalidate();
		pathList.repaint();
	}
	public void updatePathButtons() {
		PathDetail selected = null;
		if (activeTile!=null) {
			selected = (PathDetail)pathList.getSelectedValue();
			if (selected!=null) {
				String type = selected.getType();
				if (type.equals("normal")) {
					normalPathType.setSelected(true);
				}
				else if (type.equals("hidden")) {
					hiddenPathType.setSelected(true);
				}
				else if (type.equals("secret")) {
					secretPathType.setSelected(true);
				}
				else if (type.equals("caves")) {
					cavePathType.setSelected(true);
				}
			}
		}
		
		normalPathType.setEnabled(activeTile!=null && selected!=null);
		hiddenPathType.setEnabled(activeTile!=null && selected!=null);
		secretPathType.setEnabled(activeTile!=null && selected!=null);
		cavePathType.setEnabled(activeTile!=null && selected!=null);
		
		addPathButton.setEnabled(activeTile!=null);
		removePathButton.setEnabled(activeTile!=null && selected!=null);
	}
	public void updatePaths() {
		PathDetail selected = null;
		if (activeTile!=null) {
			selected = (PathDetail)pathList.getSelectedValue();
			if (selected!=null) {
				if (normalPathType.isSelected()) {
					selected.setType("normal");
				}
				else if (hiddenPathType.isSelected()) {
					selected.setType("hidden");
				}
				else if (secretPathType.isSelected()) {
					selected.setType("secret");
				}
				else if (cavePathType.isSelected()) {
					selected.setType("caves");
				}
				activeTile.repaint();
				
				activeTile.didChange();
				updateControls();
			}
		}
	}
	public void addPath() {
		// TODO Working here
		if (activeTile!=null) {
			ArrayList list = new ArrayList(activeTile.getClearingDetail());
			ButtonOptionDialog chooser = new ButtonOptionDialog(this,null,"From which clearing?","");
			chooser.addSelectionObjects(list);
			chooser.setVisible(true);
			if (chooser.getSelectedObject()!=null) {
				ClearingDetail c1 = (ClearingDetail)chooser.getSelectedObject();
				
				// FIXME Should eliminate paths that are already there!
				chooser = new ButtonOptionDialog(this,null,"To which clearing/edge?","");
				list.add("N");
				list.add("NE");
				list.add("SE");
				list.add("S");
				list.add("SW");
				list.add("NW");
				chooser.addSelectionObjects(list);
				chooser.setVisible(true);
				if (chooser.getSelectedObject()!=null) {
					String c2Name;
					ClearingDetail c2;
					Object o2 = chooser.getSelectedObject();
					if (o2 instanceof ClearingDetail) {
						c2 = (ClearingDetail)o2;
						c2Name = c2.getName();
					}
					else {
						String edge = (String)o2;
						c2Name = edge;
						Hashtable edgePositionHash = activeTile.getEdgePositionHash();
						c2 = new ClearingDetail(activeTile,edge,(Point)edgePositionHash.get(edge),activeTile.getFacingIndex());
					}
					ArrayList paths = new ArrayList(activeTile.getPathDetail());
					PathDetail path = new PathDetail(activeTile,paths.size()+1,c1.getName(),c2Name,c1,c2,null,"normal",activeTile.getFacingName());
					paths.add(path);
					activeTile.setPathDetail(paths);
					updatePathList(paths.size()-1);
				}
			}
		}
	}
	public void removePath() {
		if (activeTile!=null) {
			int index = pathList.getSelectedIndex();
			if (index>=0) {
				ArrayList list = new ArrayList(activeTile.getPathDetail());
				list.remove(index);
				activeTile.setPathDetail(list);
				updatePathList(index);
			}
		}
	}
	public void movePathUp() {
		if (activeTile!=null) {
			int index = pathList.getSelectedIndex();
			
			if (index>0) {
				ArrayList list = new ArrayList(activeTile.getPathDetail());
				
				PathDetail selected = (PathDetail)list.get(index);
				PathDetail toSwap = (PathDetail)list.get(index-1);
				
				list.set(index-1,selected);
				list.set(index,toSwap);
				
				activeTile.setPathDetail(list);
				
				updatePathList(index-1);
			}
		}
	}
	public void movePathDown() {
		if (activeTile!=null) {
			int index = pathList.getSelectedIndex();
			
			if ((index+1)<pathList.getModel().getSize()) {
				ArrayList list = new ArrayList(activeTile.getPathDetail());
				
				PathDetail selected = (PathDetail)list.get(index);
				PathDetail toSwap = (PathDetail)list.get(index+1);
				
				list.set(index+1,selected);
				list.set(index,toSwap);
				
				activeTile.setPathDetail(list);
				
				updatePathList(index+1);
			}
		}
	}
	public void addPathArcPoint(Point p) {
		if (activeTile!=null) {
			PathDetail selected = (PathDetail)pathList.getSelectedValue();
			if (selected!=null) {
				activeTile.didChange();
				updateControls();
				selected.setArcPoint(p);
				activeTile.repaint();
			}
		}
	}
	public Vector getTiles() {
		Vector tiles = new Vector();
		for (Iterator i=data.getGameObjects().iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			if (go.hasKey("tile")) {
				tiles.addElement(go);
			}
		}
		return tiles;
	}
	public void setOffroadPos(Point pos) {
		if (activeTile!=null) {
			activeTile.didChange();
			updateControls();
			activeTile.setOffroadPos(pos);
			activeTile.repaint();
		}
	}
	public void saveFile() {
		data.saveToFile(new File(dataFilename));
	}
	public static String dataFilename = null;
	public static void main(String[]args) {
		RealmUtility.setupTextType();
		ArgumentParser ap = new ArgumentParser(args);
		dataFilename = ap.getValueForKey("file");
		GameData data = new GameData();
		
		if (dataFilename==null) {
			dataFilename = "data/MagicRealmData.xml";
			data.loadFromStream(ResourceFinder.getInputStream(dataFilename));
		}
		else {
			data.loadFromFile(new File(dataFilename));
		}
		
//		StringBuffer result = new StringBuffer();
//		data.doSetup(result,"standard_game"); // don't do a setup!  this is very bad!
//		System.out.println(result.toString());		
	
		new TileEditFrame(data).setVisible(true);
	}
}