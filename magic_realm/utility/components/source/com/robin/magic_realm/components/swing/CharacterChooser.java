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
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.robin.game.objects.GameObject;
import com.robin.game.objects.GamePool;
import com.robin.general.swing.*;
import com.robin.general.util.RandomNumber;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class CharacterChooser extends AggressiveDialog {
	
	private static ImageIcon randomIcon = ImageCache.getIcon("tab/turn",16,16);
	private static ImageIcon fighterIcon = ImageCache.getIcon("actions/alert",16,16);
	private static ImageIcon magicuserIcon = ImageCache.getIcon("actions/spell",16,16);
	
	private HostPrefWrapper hostPrefs;
	
	private ArrayList<GameObject> allCharacterObjects;
	private ArrayList<GameObject> availableCharacterObjects;
	private ArrayList<GameObject> availableFighters;
	private ArrayList<GameObject> availableMagicUsers;
	private JLabel characterDisplay;
	private CharacterListModel listModel;
	private JList characterList;
	
	private JCheckBox showRegularOption;
	private JCheckBox showCustomOption;
	
	private JButton selectRandomButton;
	private JButton selectRandomFighterButton;
	private JButton selectRandomMagicUserButton;
	private JButton cancelButton;
	private JButton okayButton;
	
	private boolean allowCustom;
	private GameObject chosenCharacter = null;
	
	public CharacterChooser(JFrame frame,ArrayList<GameObject> characterObjects,HostPrefWrapper hostPrefs) {
		super(frame,"Choose a character",true);
		this.hostPrefs = hostPrefs;
		this.allowCustom = hostPrefs.hasPref(Constants.EXP_CUSTOM_CHARS);
		this.allCharacterObjects = characterObjects;
		initComponents();
		buildLists();
		setLocationRelativeTo(frame);
	}
	private void buildLists() {
		availableCharacterObjects = new ArrayList<GameObject>();
		availableFighters = new ArrayList<GameObject>();
		availableMagicUsers = new ArrayList<GameObject>();
		for (GameObject go:allCharacterObjects) {
			boolean custom = go.hasThisAttribute(Constants.CUSTOM_CHARACTER);
			boolean okayToAdd = false;
			if (allowCustom && custom && showCustomOption.isSelected()) {
				// Make sure the character is in the hosts stack
				String key = CustomCharacterLibrary.getSingleton().getCharacterUniqueKey(go);
				if (hostPrefs.hasCharacterKey(key)) {
					okayToAdd = true;
				}
			}
			else if (!custom && showRegularOption.isSelected()) {
				okayToAdd = true;
			}
			if (okayToAdd) {
				availableCharacterObjects.add(go);
				if (go.hasThisAttribute("fighter")) {
					availableFighters.add(go);
				}
				if (go.hasThisAttribute("magicUser")) {
					availableMagicUsers.add(go);
				}
			}
		}
		listModel.fireChanged();
		characterList.revalidate();
		characterList.repaint();
		updateControls();
	}
	private void initComponents() {
		setLayout(new BorderLayout());
		
		JPanel left = new JPanel(new BorderLayout());
		listModel = new CharacterListModel();
		characterList = new JList(listModel);
		characterList.setBackground(new Color(200,255,255));
		left.add(new JScrollPane(characterList),"Center");
		showRegularOption = new JCheckBox("Regular",true);
		showCustomOption = new JCheckBox("Custom",true);
		if (allowCustom) {
			JPanel leftBottom = new JPanel(new GridLayout(2,1));
			showRegularOption.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					buildLists();
				}
			});
			leftBottom.add(showRegularOption);
			showCustomOption.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					buildLists();
				}
			});
			leftBottom.add(showCustomOption);
			leftBottom.setBorder(BorderFactory.createTitledBorder("Show Characters"));
			left.add(leftBottom,"North");
		}
		add(left,"West");
		characterList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				updateImage();
			}
		});
		characterList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent ev) {
				if (ev.getClickCount()==2) {
					okayButton.doClick();
				}
			}
		});
		characterList.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		characterList.setCellRenderer(new CharacterListCellRenderer());
		
		characterDisplay = new JLabel();
		ComponentTools.lockComponentSize(characterDisplay,760,600);
		add(characterDisplay,"Center");
		
		Box controls = Box.createHorizontalBox();
		controls.add(Box.createGlue());
		selectRandomButton = new JButton("Random Character",randomIcon);
		selectRandomButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				int index = RandomNumber.getRandom(availableCharacterObjects.size());
				characterList.setSelectedValue(availableCharacterObjects.get(index).getName(),true);
			}
		});
		controls.add(selectRandomButton);
		controls.add(Box.createHorizontalGlue());
		selectRandomFighterButton = new JButton("Random Fighter",fighterIcon);
		selectRandomFighterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				int index = RandomNumber.getRandom(availableFighters.size());
				characterList.setSelectedValue(availableFighters.get(index).getName(),true);
			}
		});
		controls.add(selectRandomFighterButton);
		controls.add(Box.createHorizontalGlue());
		selectRandomMagicUserButton = new JButton("Random MagicUser",magicuserIcon);
		selectRandomMagicUserButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				int index = RandomNumber.getRandom(availableMagicUsers.size());
				characterList.setSelectedValue(availableMagicUsers.get(index).getName(),true);
			}
		});
		controls.add(selectRandomMagicUserButton);
		controls.add(Box.createHorizontalGlue());
		
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				cleanExit();
			}
		});
		controls.add(cancelButton);
		okayButton = new JButton("Okay");
		okayButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				int index = characterList.getSelectedIndex();
				if (index>=0) {
					chosenCharacter = (GameObject)availableCharacterObjects.get(index);
					cleanExit();
				}
			}
		});
		controls.add(okayButton);
		add(controls,"South");
		
		setDefaultCloseOperation(AggressiveDialog.DO_NOTHING_ON_CLOSE);
		
		pack();
		
		updateImage();
	}
	private void updateControls() {
		selectRandomButton.setEnabled(!availableCharacterObjects.isEmpty());
		selectRandomFighterButton.setEnabled(!availableFighters.isEmpty());
		selectRandomMagicUserButton.setEnabled(!availableMagicUsers.isEmpty());
		okayButton.setEnabled(!availableCharacterObjects.isEmpty());
	}
	/**
	 * @return Returns the chosenCharacter.
	 */
	public GameObject getChosenCharacter() {
		return chosenCharacter;
	}
	private void cleanExit() {
		setVisible(false);
		dispose();
	}
	private void updateImage() {
		int index = characterList.getSelectedIndex();
		GameObject go = null;
		if (index>=0) {
			go = (GameObject)availableCharacterObjects.get(index);
		}
		if (go!=null) {
			characterDisplay.setIcon(getCharacterImage(go));
			okayButton.setEnabled(true);
		}
		else {
			characterDisplay.setIcon(null);
			okayButton.setEnabled(false);
		}
		System.gc();
	}
	private class CharacterListModel extends AbstractListModel {
		public int getSize() {
			return availableCharacterObjects==null?0:availableCharacterObjects.size();
		}
		public Object getElementAt(int index) {
			if (index<getSize()) {
				GameObject go = (GameObject)availableCharacterObjects.get(index);
				return go.getName();
			}
			return null;
		}
		public void fireChanged() {
			fireContentsChanged(this,0,1000);
		}
	}
	private static Font NORMAL = new Font("Dialog",Font.BOLD,12);
	private static Font ITALIC = new Font("Dialog",Font.BOLD|Font.ITALIC,12);
	private class CharacterListCellRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
			super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
			GameObject go = (GameObject)availableCharacterObjects.get(index);
			setIcon(go.hasThisAttribute("fighter")?fighterIcon:magicuserIcon);
			setFont(go.hasThisAttribute(Constants.CUSTOM_CHARACTER)?ITALIC:NORMAL);
			return this;
		}
	}
	public static ImageIcon getCharacterImage(GameObject go) {
		if (go.hasThisAttribute(Constants.CUSTOM_CHARACTER)) {
			return CustomCharacterLibrary.getSingleton().getCharacterImage(go.getAttribute("level_4","name"));
		}
		String iconType = go.getThisAttribute("icon_type");
		return IconFactory.findIcon("images/characterdetail/"+iconType+".jpg");
	}
	
	public static void main(String[] args) {
		ComponentTools.setSystemLookAndFeel();
		
		System.out.print("loading...");
		RealmLoader loader = new RealmLoader();
		System.out.println("Done");
		GamePool pool = new GamePool(loader.getData().getGameObjects());
		ArrayList<GameObject> availChars = pool.find("character");
		HostPrefWrapper hostPrefs = HostPrefWrapper.createDefaultHostPrefs(loader.getData());
		Collections.sort(availChars,new Comparator<GameObject>() {
			public int compare(GameObject go1,GameObject go2) {
				return go1.getName().compareTo(go2.getName());
			}
		});
		CharacterChooser chooser = new CharacterChooser(new JFrame(),availChars,hostPrefs);
		chooser.setVisible(true);
	}
}