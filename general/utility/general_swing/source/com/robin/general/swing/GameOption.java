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
package com.robin.general.swing;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

public class GameOption {
	public static final Color ACTIVE_COLOR = new Color(210,255,210);
	public static final Color INACTIVE_COLOR = Color.lightGray;
	
	private String key;
	private String description;
	private JTextArea area;
	private JCheckBox activeCB;
	private JPanel panel;
	
	private ArrayList overrideKeys;
	private ArrayList includeKeys;
	private ArrayList cantHaveKeys;
	
	private GameOptionPane gameOptionPane;
	
	private ActionListener listener = null;
	
	public GameOption(String key,String description,boolean active) {
		this(key,description,active,null,null);
	}
	public GameOption(String inKey,String description,boolean active,String[] overrides,String[] includes) {
		this(inKey,description,active,overrides,includes,null);
	}
	public GameOption(String inKey,String description,boolean active,String[] overrides,String[] includes,String[] cantHaves) {
		this.key = inKey;
		this.description = description;
		overrideKeys = new ArrayList();
		if (overrides!=null) {
			overrideKeys.addAll(Arrays.asList(overrides));
		}
		includeKeys = new ArrayList();
		if (includes!=null) {
			includeKeys.addAll(Arrays.asList(includes));
		}
		cantHaveKeys = new ArrayList();
		if (cantHaves!=null) {
			cantHaveKeys.addAll(Arrays.asList(cantHaves));
		}
		panel = new JPanel(new BorderLayout());
			area = new JTextArea(description);
			area.setLineWrap(true);
			area.setWrapStyleWord(true);
			area.setEditable(false);
			area.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent ev) {
					if (activeCB.isEnabled()) {
						setActive(!activeCB.isSelected());
//						activeCB.setSelected(!activeCB.isSelected());
//						updateOthers();
//						updateColor();
					}
				}
			});
			area.setBorder(BorderFactory.createEtchedBorder());
		panel.add(area,"Center");
			Box box = Box.createVerticalBox();
				activeCB = new JCheckBox();
				activeCB.setSelected(active);
				activeCB.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						setActive(activeCB.isSelected());
						//updateOthers();
						//updateColor();
					}
				});
			box.add(activeCB);
			box.add(Box.createVerticalGlue());
		panel.add(box,"West");
		panel.add(Box.createVerticalStrut(30),"East");
//		panel.setMinimumSize(new Dimension(1,1));
//		panel.setPreferredSize(new Dimension(1,1));
//		panel.setMaximumSize(new Dimension(1,1));
		panel.setMaximumSize(new Dimension(2000,50));
//		panel.setPreferredSize(new Dimension(2000,50));
		updateColor();
	}
	public void setActionListener(ActionListener listener) {
		this.listener = listener;
	}
	public void setEnabled(boolean val) {
		activeCB.setEnabled(val);
	}
	private void updateOthers() {
		if (activeCB.isSelected()) {
			// turn off overrides
			for (Iterator i=getOverrideKeys().iterator();i.hasNext();) {
				String overrideKey = (String)i.next();
				if (!overrideKey.equals(key)) {
					gameOptionPane.setOption(overrideKey,false);
				}
			}
			
			// turn on includes
			for (Iterator i=getIncludeKeys().iterator();i.hasNext();) {
				String includeKey = (String)i.next();
				if (!includeKey.equals(key)) {
					gameOptionPane.setOption(includeKey,true);
				}
			}
		}
		else {
			// turn off cant haves
			for (Iterator i=getCantHaveKeys().iterator();i.hasNext();) {
				String cantHaveKey = (String)i.next();
				if (!cantHaveKey.equals(key)) {
					gameOptionPane.setOption(cantHaveKey,false);
				}
			}
		}
	}
	public Collection getOverrideKeys() {
		return overrideKeys;
	}
	public Collection getIncludeKeys() {
		return includeKeys;
	}
	public Collection getCantHaveKeys() {
		return cantHaveKeys;
	}
	private void updateColor() {
		area.setBackground(activeCB.isSelected()?ACTIVE_COLOR:INACTIVE_COLOR);
	}
	public String getKey() {
		return key;
	}
	public String getDescription() {
		return description;
	}
	public void setActive(boolean val) {
		activeCB.setSelected(val);
		updateOthers();
		updateColor();
		if (listener!=null) {
			listener.actionPerformed(new ActionEvent(GameOption.this,0,""));
		}
	}
	public boolean isActive() {
		return activeCB.isSelected();
	}
	public JPanel getPanel() {
		return panel;
	}
	public void setGameOptionPane(GameOptionPane gameOptionPane) {
		this.gameOptionPane = gameOptionPane;
	}
}