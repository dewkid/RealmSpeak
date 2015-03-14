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
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.swing.*;

import com.robin.general.io.PreferenceManager;
import com.robin.magic_realm.components.utility.RealmUtility;

public class WindowLayoutManager {
	
	private static final int[] keys = {4,5,6,7,8,9,0};
	private static final String LAST_LAYOUT = "last";
	private static final String MAIN_WINDOW = "main";
	
	private RealmSpeakFrame mainFrame;
	private JDesktopPane desktop;
	private JMenuItem clearCustomLayoutsItem;
	private JMenu setterSubMenu;
	private JMenu getterSubMenu;
	private Hashtable<Integer,LayoutMenuItem> getters; 
	private PreferenceManager preferenceManager;
	
	public WindowLayoutManager(RealmSpeakFrame mainFrame,JDesktopPane desktop) {
		this.mainFrame = mainFrame;
		this.desktop = desktop;
		preferenceManager = RealmUtility.getWindowLayoutPrefs();
		preferenceManager.loadPreferences();
		generateMenus();
		applyLastLayout();
	}
	public void applyLastLayout() {
		int layout = getLastLayout();
		if (layout>0) {
			applyLayout(layout);
		}
	}
	// Key pattern:   <Layout#>_<Window><mod>
	// Value will be:   <x>_<y>_<width>_<height>
	private String getKeyFor(int layoutNumber,String windowName,int modifier) {
		StringBuilder key = new StringBuilder();
		key.append(layoutNumber);
		key.append("_");
		key.append(windowName);
		if (modifier>0) {
			key.append(modifier);
		}
		return key.toString();
	}
	private void clearLayouts() {
		preferenceManager.clear();
		preferenceManager.savePreferences();
	}
	private void setLayout(int layoutNumber,String windowName,int modifier,Rectangle location) {
		String key = getKeyFor(layoutNumber,windowName,modifier);
		StringBuilder value = new StringBuilder();
		value.append(location.x);
		value.append("_");
		value.append(location.y);
		value.append("_");
		value.append(location.width);
		value.append("_");
		value.append(location.height);
		preferenceManager.set(key.toString(),value.toString());
		preferenceManager.savePreferences();
	}
	private Rectangle getLayout(int layoutNumber,String windowName,int modifier) {
		String key = getKeyFor(layoutNumber,windowName,modifier);
		String value = preferenceManager.get(key);
		if (value==null) return null;
		StringTokenizer tokens = new StringTokenizer(value,"_");
		if (tokens.countTokens()!=4) return null;
		int x = Integer.valueOf(tokens.nextToken());
		int y = Integer.valueOf(tokens.nextToken());
		int width = Integer.valueOf(tokens.nextToken());
		int height = Integer.valueOf(tokens.nextToken());
		return new Rectangle(x,y,width,height);
	}
	private void setLayoutName(int layoutNumber,String name) {
		preferenceManager.set(String.valueOf(layoutNumber),name);
	}
	private String getLayoutName(int layoutNumber) {
		return preferenceManager.get(String.valueOf(layoutNumber));
	}
	public void clearLastLayout() {
		int last = getLastLayout();
		if (last>0) {
			preferenceManager.remove(LAST_LAYOUT);
			preferenceManager.savePreferences();
			updateControls();
		}
	}
	private void setLastLayout(int layoutNumber) {
		int last = getLastLayout();
		if (last!=layoutNumber) {
			preferenceManager.set(LAST_LAYOUT,layoutNumber);
			preferenceManager.savePreferences();
			updateControls();
		}
	}
	public int getLastLayout() {
		return preferenceManager.getInt(LAST_LAYOUT); // returns 0 if none found
	}
	private void generateMenus() {
		getters = new Hashtable<Integer,LayoutMenuItem>();
		clearCustomLayoutsItem = new JMenuItem("Clear All Custom Layouts");
		clearCustomLayoutsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				int ret = JOptionPane.showConfirmDialog(desktop,"This is not reversible.  Are you sure?","Clear All Custom Layouts",JOptionPane.YES_NO_OPTION);
				if (ret==JOptionPane.NO_OPTION) return;
				clearLayouts();
				updateControls();
			}
		});
		setterSubMenu = new JMenu("Save Custom Layout");
		getterSubMenu = new JMenu("Load Custom Layout");
		for (int key:keys) {
			setterSubMenu.add(createSetterMenuItem(key));
			LayoutMenuItem getter = createGetterMenuItem(key);
			getterSubMenu.add(getter);
			getters.put(key,getter);
		}
		updateControls();
	}
	private LayoutMenuItem createSetterMenuItem(int num) {
		if (num==10) num=0;
		LayoutMenuItem item = new LayoutMenuItem(num);
		item.setLayoutName(null,false);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				String layoutName = JOptionPane.showInputDialog("Name of window layout?");
				if (layoutName==null) return;
				
				LayoutMenuItem thisItem = (LayoutMenuItem)ev.getSource();
				if (!captureCurrentLayout(thisItem.layoutNumber,layoutName)) {
					JOptionPane.showMessageDialog(desktop,"There are no windows to capture!");
				}
				updateControls();
			}
		});
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0+num,InputEvent.SHIFT_MASK|InputEvent.CTRL_MASK));
		return item;
	}
	private boolean applyLayout(int layoutNumber) {
		int windowsRestored = 0;
		Hashtable<String,Integer> instanceCount = new Hashtable<String,Integer>();
		Rectangle mr = getLayout(layoutNumber,MAIN_WINDOW,0);
		if (mr!=null) {
			mainFrame.setLocation(mr.x,mr.y);
			mainFrame.setSize(mr.width,mr.height);
		}
		for (Component component:desktop.getComponents()) {
			if (!(component instanceof RealmSpeakInternalFrame)) continue;
			RealmSpeakInternalFrame frame = (RealmSpeakInternalFrame)component;
			boolean singular = frame.onlyOneInstancePerGame();
			String name = frame.getFrameTypeName();
			int modifier = 0;
			if (!singular && instanceCount.containsKey(name)) {
				modifier = instanceCount.get(name);
				modifier++;
			}
			instanceCount.put(name,modifier);
			Rectangle rect = getLayout(layoutNumber,name,modifier);
			if (rect==null && modifier>0) {
				rect = getLayout(layoutNumber,name,0); // default to the location of the first one (but only if this isn't already the first one!)
			}
			if (rect!=null) {
				frame.setLocation(rect.x,rect.y);
				frame.setSize(rect.width,rect.height);
				windowsRestored++;
			}
		}
		return windowsRestored>0;
	}
	private boolean captureCurrentLayout(int layoutNumber,String layoutName) {
		int windowsCaptured = 0;
		Hashtable<String,Integer> instanceCount = new Hashtable<String,Integer>();
		for (Component component:desktop.getComponents()) {
			if (!(component instanceof RealmSpeakInternalFrame)) continue;
			RealmSpeakInternalFrame frame = (RealmSpeakInternalFrame)component;
			boolean singular = frame.onlyOneInstancePerGame();
			String name = frame.getFrameTypeName();
			int modifier = 0;
			if (!singular && instanceCount.containsKey(name)) {
				modifier = instanceCount.get(name);
				modifier++;
			}
			instanceCount.put(name,modifier);
			Point p = frame.getLocation();
			Dimension s = frame.getSize();
			Rectangle rect = new Rectangle(p.x,p.y,s.width,s.height);
			setLayout(layoutNumber,name,modifier,rect);
			windowsCaptured++;
		}
		if (windowsCaptured==0) return false;
		
		Point mp = mainFrame.getLocation();
		Dimension ms = mainFrame.getSize();
		Rectangle mr = new Rectangle(mp.x,mp.y,ms.width,ms.height);
		setLayout(layoutNumber,MAIN_WINDOW,0,mr);
		
		setLayoutName(layoutNumber,layoutName);
		return true;
	}
	private LayoutMenuItem createGetterMenuItem(int num) {
		if (num==10) num=0;
		LayoutMenuItem item = new LayoutMenuItem(num);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				LayoutMenuItem thisItem = (LayoutMenuItem)ev.getSource();
				applyLayout(thisItem.layoutNumber);
				setLastLayout(thisItem.layoutNumber);
			}
		});
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0+num,InputEvent.CTRL_MASK));
		return item;
	}
	public JMenuItem getClearMenuItem() {
		return clearCustomLayoutsItem;
	}
	public JMenu getSetterSubMenu() {
		return setterSubMenu;
	}
	public JMenu getGetterSubMenu() {
		return getterSubMenu;
	}
	public void updateControls() {
		int current = getLastLayout();
		for (int key:keys) {
			LayoutMenuItem item = getters.get(key);
			String layoutName = getLayoutName(key);
			if (layoutName!=null) {
				item.setEnabled(true);
				item.setLayoutName(layoutName,key==current);
			}
			else {
				item.setEnabled(false);
				item.setLayoutName("EMPTY",false);
			}
		}
	}
	private class LayoutMenuItem extends JMenuItem {
		private int layoutNumber;
		public LayoutMenuItem(int layoutNumber) {
			this.layoutNumber = layoutNumber;
		}
		public void setLayoutName(String name,boolean current) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			sb.append(layoutNumber);
			sb.append("]");
			if (name!=null) {
				sb.append(" - ");
				sb.append(name);
			}
			if (current) {
				sb.append(" (current)");
			}
			setText(sb.toString());
		}
	}
}