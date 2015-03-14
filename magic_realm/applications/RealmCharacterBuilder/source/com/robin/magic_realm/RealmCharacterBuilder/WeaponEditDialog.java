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
package com.robin.magic_realm.RealmCharacterBuilder;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;

import javax.swing.*;

import com.robin.game.objects.GameObject;
import com.robin.general.io.FileManager;
import com.robin.general.swing.*;
import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.WeaponChitComponent;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.TemplateLibrary;

public class WeaponEditDialog extends AggressiveDialog {
	
	private RealmCharacterBuilderModel model;
	private GameObject weapon = null;
	private WeaponChitComponent weaponComponent = null;
	private boolean reservedWeapon;
	
	private Box infoPanel;
	private JPanel sideEditPanels;
	
	private JLabel[] sideLabel;
	private JButton loadIconButton;
	private SideEditPanel[] sideEditPanel;
	private JButton clearButton;
	private JButton pickButton;
	private JButton doneButton;
	private JButton newButton;
	
	private JLabel nameField;
	private ButtonPanel weightChoice;
	private IntegerField lengthField;
	private IntegerField priceField;
	private ButtonPanel rangedOption;
	
	private static final String[] STARTING_LOCATION_OPTION = {
		"Guard",
		"House",
		"Bashkar Dwelling",
		"Inn",
		"Company Dwelling",
		"Woodfolk Dwelling",
		"Chapel",
		"Lancer Dwelling",
		"Patrol Dwelling",
		"Scholar",
		"Shaman",
		"Warlock",
		"Crone",
	};
	private ArrayList<JRadioButton> slButtons;
	
	private FileManager graphicsManager;
	
	public WeaponEditDialog(JFrame frame,RealmCharacterBuilderModel model,FileManager graphicsManager,String weaponName) {
		super(frame,"Edit Weapon",true);
		this.model = model;
		this.graphicsManager = graphicsManager;
		initComponents();
		setLocationRelativeTo(frame);
		setWeapon(weaponName);
	}
	private void initComponents() {
		setSize(500,490);
		setLayout(new BorderLayout());
		
		JPanel mainPanel = new JPanel(new GridLayout(2,1));
		
		JPanel topPanel = new JPanel(new BorderLayout());
		JPanel fullChitDisplay = new JPanel(new BorderLayout());
		JPanel chitDisplay = new JPanel(new GridLayout(2,1));
		sideLabel = new JLabel[2];
		for (int i=0;i<2;i++) {
			sideLabel[i] = new JLabel();
			ComponentTools.lockComponentSize(sideLabel[i],90,80);
			sideLabel[i].setBorder(BorderFactory.createEtchedBorder());
			chitDisplay.add(sideLabel[i]);
		}
		fullChitDisplay.add(chitDisplay,"Center");
		loadIconButton = new JButton("Load Icon");
		loadIconButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				loadIcon();
				updateImages();
			}
		});
		fullChitDisplay.add(loadIconButton,"South");
		topPanel.add(fullChitDisplay,"West");
		Box line;
		int w = 300;
		UniformLabelGroup group = new UniformLabelGroup();
		infoPanel = Box.createVerticalBox();
		infoPanel.add(Box.createVerticalGlue());
			line = group.createLabelLine("Weight");
			weightChoice = new ButtonPanel(RealmCharacterConstants.STRENGTHS_PLUS_NEG);
			weightChoice.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					weapon.setThisAttribute(Constants.WEIGHT,ev.getActionCommand());
					sideEditPanel[0].updateWeight();
					sideEditPanel[1].updateWeight();
				}
			});
			ComponentTools.lockComponentSize(weightChoice,w,25);
			line.add(weightChoice);
			line.add(Box.createHorizontalGlue());
		infoPanel.add(line);
		infoPanel.add(Box.createVerticalStrut(5));
		infoPanel.add(Box.createVerticalGlue());
			line = group.createLabelLine("Length");
			lengthField = new IntegerField();
			lengthField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					weapon.setThisAttribute("length",lengthField.getText());
				}
			});
			lengthField.addFocusListener(new FocusAdapter() {
				public void focusGained(FocusEvent ev) {
					lengthField.selectAll();
				}
				public void focusLost(FocusEvent ev) {
					weapon.setThisAttribute("length",lengthField.getText());
				}
			});
			ComponentTools.lockComponentSize(lengthField,40,25);
			line.add(lengthField);
			line.add(Box.createHorizontalStrut(10));
			line.add(new JLabel("Base Price:"));
			line.add(Box.createHorizontalStrut(5));
			priceField = new IntegerField();
			priceField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					weapon.setThisAttribute("base_price",priceField.getText());
				}
			});
			priceField.addFocusListener(new FocusAdapter() {
				public void focusGained(FocusEvent ev) {
					priceField.selectAll();
				}
				public void focusLost(FocusEvent ev) {
					weapon.setThisAttribute("base_price",priceField.getText());
				}
			});
			ComponentTools.lockComponentSize(priceField,40,25);
			line.add(priceField);
			line.add(Box.createHorizontalStrut(10));
			line.add(new JLabel("Missile Weapon:"));
			line.add(Box.createHorizontalStrut(5));
			rangedOption = new ButtonPanel(RealmCharacterConstants.YESNO);
			rangedOption.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					boolean ranged = "YES".equals(rangedOption.getSelectedItem());
					if (ranged) {
						weapon.setThisAttribute("missile");
					}
					else {
						weapon.removeThisAttribute("missile");
					}
				}
			});
			ComponentTools.lockComponentSize(rangedOption,100,25);
			line.add(rangedOption);
			line.add(Box.createHorizontalGlue());
		infoPanel.add(line);
			ButtonGroup slGroup = new ButtonGroup();
			JPanel startingLocationPanel = new JPanel(new GridLayout(5,3));
			ActionListener al = new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					JRadioButton button = (JRadioButton)ev.getSource();
					weapon.setThisAttribute(Constants.WEAPON_START_LOCATION,button.getText());
				}
			};
			slButtons = new ArrayList<JRadioButton>();
			for (int i=0;i<STARTING_LOCATION_OPTION.length;i++) {
				JRadioButton button = new JRadioButton(STARTING_LOCATION_OPTION[i],i==0);
				startingLocationPanel.add(button);
				slGroup.add(button);
				button.addActionListener(al);
				slButtons.add(button);
			}
			startingLocationPanel.setBorder(BorderFactory.createTitledBorder("Starting Location (when not in use)"));
		infoPanel.add(startingLocationPanel);

		infoPanel.add(Box.createVerticalGlue());
		topPanel.add(infoPanel,"Center");
		mainPanel.add(topPanel);
		sideEditPanels = new JPanel(new GridLayout(1,2));
		sideEditPanel = new SideEditPanel[2];
		sideEditPanel[0] = new SideEditPanel(sideLabel[0],"unalerted");
		sideEditPanels.add(sideEditPanel[0]);
		sideEditPanel[1] = new SideEditPanel(sideLabel[1],"alerted");
		sideEditPanels.add(sideEditPanel[1]);
		mainPanel.add(sideEditPanels);
		add(mainPanel,"Center");
		
		Box buttonsPanel = Box.createHorizontalBox();
		buttonsPanel.add(Box.createHorizontalGlue());
		newButton = new JButton("New");
		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				createNewWeapon();
			}
		});
		buttonsPanel.add(newButton);
		buttonsPanel.add(Box.createHorizontalGlue());
		pickButton = new JButton("Pick");
		pickButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				ArrayList<String> list = TemplateLibrary.getSingleton().getAllWeaponNames();
				for (String val:model.getAllWeaponNames()) {
					if (!list.contains(val)) {
						list.add(val);
					}
				}
				Collections.sort(list);
				String weaponName = (String)JOptionPane.showInputDialog(
						parent,"Select a weapon:","Pick Weapon",JOptionPane.PLAIN_MESSAGE,null,list.toArray(),list.get(0));
				if (weaponName!=null) {
					setWeapon(weaponName);
				}
			}
		});
		buttonsPanel.add(pickButton);
		buttonsPanel.add(Box.createHorizontalGlue());
		clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				clearControls();
				weapon = null;
				weaponComponent = null;
				updateControls();
			}
		});
		buttonsPanel.add(clearButton);
		buttonsPanel.add(Box.createHorizontalGlue());
		doneButton = new JButton("Done");
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				// Save to weapon library
				// If same name is already there, then ask if overwrite is okay
				setVisible(false);
			}
		});
		buttonsPanel.add(doneButton);
		buttonsPanel.add(Box.createHorizontalGlue());
		add(buttonsPanel,"South");
		
		nameField = new JLabel("",JLabel.CENTER);
		ComponentTools.lockComponentSize(nameField,450,35);
		nameField.setFont(new Font("Dialog",Font.BOLD,32));
		add(nameField,"North");
		
		updateControls();
	}
	private void loadIcon() {
		File file = graphicsManager.getLoadPath();
		if (file!=null) {
			ImageIcon icon = IconFactory.findIcon(file.getAbsolutePath());
			if (icon!=null) {
				model.updateWeaponIcon(weapon,icon);
				repaint();
			}
		}
	}
	public String getWeaponName() {
		if (weapon!=null) {
			return weapon.getName();
		}
		return null;
	}
	private void createNewWeapon() {
		String name = JOptionPane.showInputDialog("Weapon Name?");
		if (name!=null && name.trim().length()>0) {
			StringTokenizer tokens = new StringTokenizer(name.trim()," ");
			StringBuffer sb = new StringBuffer();
			while(tokens.hasMoreTokens()) {
				if (sb.length()>0) {
					sb.append(" ");
				}
				sb.append(StringUtilities.capitalize(tokens.nextToken()));
			}
			setWeapon(sb.toString());
		}
	}
	private void setWeapon(String name) {
		if (name!=null) {
			reservedWeapon = false;
			
			// Check model first
			if (model.hasWeapon(name)) {
				weapon  = model.getWeapon(name);
				if (TemplateLibrary.getSingleton().hasWeaponTemplate(name)) {
					reservedWeapon = true;
				}
			}
			else if (TemplateLibrary.getSingleton().hasWeaponTemplate(name)) {
				// See if the weapon is already present in the model
				GameObject template = TemplateLibrary.getSingleton().getWeaponTemplate(name);
				weapon = model.getData().createNewObject();
				weapon.copyAttributesFrom(template);
				reservedWeapon = true;
			}
			else {
				// Must be a new weapon
				weapon = model.getData().createNewObject();
				weapon.setName(name);
				weapon.setThisAttribute("weapon");
				weapon.setThisAttribute("icon_type","question");
				weapon.setThisAttribute("icon_folder",RealmCharacterConstants.CUSTOM_ICON_BASE_PATH+"weapons");
				weapon.setThisAttribute(Constants.WEIGHT,"L");
				weapon.setThisAttribute("length","0");
				weapon.setThisAttribute("base_price","1");
				weapon.setAttribute("unalerted","chit_color","white");
				weapon.setAttribute("unalerted","strength","L");
				weapon.setAttribute("alerted","chit_color","red");
				weapon.setAttribute("alerted","strength","L");
				weapon.setThisAttribute(Constants.WEAPON_START_LOCATION,"Guard");
			}
			
			model.addWeapon(name,weapon);
			
			initWeaponControls();
			updateControls();
		}
		else {
			weapon = null;
			weaponComponent = null;
		}
	}
	private void clearControls() {
		nameField.setText("");
		weightChoice.setSelectedItem("L");
		lengthField.setText("");
		priceField.setText("");
		rangedOption.setSelectedItem("NO");
		sideLabel[0].setIcon(null);
		sideLabel[1].setIcon(null);
	}
	private void updateImages() {
		for (int i=0;i<2;i++) {
			sideEditPanel[i].updateImage();
		}
	}
	private void initWeaponControls() {
		weaponComponent = (WeaponChitComponent)RealmComponent.getRealmComponent(weapon);
		nameField.setText(weapon.getName());
		weightChoice.setSelectedItem(weapon.getThisAttribute(Constants.WEIGHT));
		lengthField.setText(weapon.getThisAttribute("length"));
		priceField.setText(weapon.getThisAttribute("base_price"));
		rangedOption.setSelectedItem(weapon.hasThisAttribute("missile")?"YES":"NO");
		for (int i=0;i<2;i++) {
			sideEditPanel[i].updateImage();
			sideEditPanel[i].initSideControls();
		}
		String weaponStart = weapon.getThisAttribute(Constants.WEAPON_START_LOCATION);
		if (weaponStart!=null) {
			for (JRadioButton button:slButtons) {
				if (weaponStart.equals(button.getText())) {
					button.setSelected(true);
					break;
				}
			}
		}
	}
	public void updateControls() {
		infoPanel.setVisible(weapon!=null && !reservedWeapon);
		sideEditPanels.setVisible(weapon!=null && !reservedWeapon);
		loadIconButton.setEnabled(weapon!=null && !reservedWeapon);
		
		newButton.setEnabled(weapon==null);
		pickButton.setEnabled(weapon==null);
		clearButton.setEnabled(weapon!=null);
	}
	
	private class SideEditPanel extends JPanel {
		
		private JLabel iconLabel;
		private String sideName;
		
		private ButtonPanel strengthChoice;
		private ButtonPanel speedChoice;
		private ButtonPanel sharpChoice;
		
		public SideEditPanel(JLabel iconLabel,String sideNameIn) {
			this.iconLabel = iconLabel;
			this.sideName = sideNameIn;
			
			setLayout(new BorderLayout());
			UniformLabelGroup group = new UniformLabelGroup();
			Box line;
			Box box = Box.createVerticalBox();
			box.add(Box.createVerticalGlue());
				line = group.createLabelLine("Strength");
				strengthChoice = new ButtonPanel(RealmCharacterConstants.ONOFF);
				strengthChoice.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						updateWeight();
					}
				});
				line.add(strengthChoice);
			box.add(line);
			box.add(Box.createVerticalGlue());
				line = group.createLabelLine("Speed");
				speedChoice = new ButtonPanel(RealmCharacterConstants.SPEEDS_W_NEG);
				speedChoice.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						String val = ev.getActionCommand();
						weapon.setAttribute(sideName,"attack_speed",val);
						updateImage();
					}
				});
				line.add(speedChoice);
			box.add(line);
			box.add(Box.createVerticalGlue());
				line = group.createLabelLine("Sharpness");
				sharpChoice = new ButtonPanel(RealmCharacterConstants.SHARPNESS);
				sharpChoice.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						String val = ev.getActionCommand();
						if (" ".equals(val)) {
							weapon.removeAttribute(sideName,"sharpness");
						}
						else {
							weapon.setAttribute(sideName,"sharpness",val);
						}
						updateImage();
					}
				});
				line.add(sharpChoice);
			box.add(line);
			box.add(Box.createVerticalGlue());
			
			add(box,"Center");
			
			setBorder(BorderFactory.createTitledBorder(StringUtilities.capitalize(sideName)));
		}
		public void initSideControls() {
			strengthChoice.setSelectedItem(weapon.hasAttribute(sideName,"strength")?"ON":"OFF");
			String speed = weapon.getAttribute(sideName,"attack_speed");
			speedChoice.setSelectedItem(speed==null?" ":speed);
			String sharpness = weapon.getAttribute(sideName,"sharpness");
			sharpChoice.setSelectedItem(sharpness==null?" ":sharpness);
		}
		public void updateWeight() {
			String weight = weapon.getThisAttribute(Constants.WEIGHT);
			String val = strengthChoice.getSelectedItem();
			if ("ON".equals(val)) {
				weapon.setAttribute(sideName,"strength",weight);
			}
			else {
				weapon.removeAttribute(sideName,"strength");
			}
			updateImage();
		}
		public void setEnabled(boolean val) {
			strengthChoice.setEnabled(val);
			speedChoice.setEnabled(val);
			sharpChoice.setEnabled(val);
		}
		public void updateImage() {
			if ("unalerted".equals(sideName)) {
				iconLabel.setIcon(weaponComponent.getIcon());
			}
			else {
				iconLabel.setIcon(weaponComponent.getFlipSideIcon());
			}
		}
	}
}