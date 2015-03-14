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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.regex.Pattern;

import javax.swing.*;

import com.robin.general.io.PreferenceManager;
import com.robin.magic_realm.components.utility.Constants;

public class OptionSetControl extends JPanel {
	//private static String OPTION_SETS = "_osns_";
	private static String OPTION_SET_PATH = "RealmSpeak/OptionSets";
	private static String CURRENT_OPTION_SET = "_osns_c_";
	
	private static String BLANK_OPTION_SET = " - Choose to Load Set -";
	
	protected JComboBox savedOptions;
	protected JButton saveOptionSet;
	protected JButton saveOptionSetAs;
	protected JButton clearOptionSet;
	
	private String currentOptionSetName;
	private ArrayList<String> optionSetNames;
	
	private HostGameSetupDialog hostGui;
	
	private boolean updatingItems = false;
	
	public OptionSetControl(HostGameSetupDialog hostGui) {
		this.hostGui = hostGui;
		initComponents();
		loadPrefs();
		updateControls();
	}
	public void showNoSet() {
		savedOptions.setSelectedIndex(0);
		currentOptionSetName = null;
		savePrefs();
		updateControls();
	}
	public void initComponents() {
		Box optionManagerBox = Box.createHorizontalBox();
		optionManagerBox.add(Box.createHorizontalGlue());
		JLabel label = new JLabel("Current Option Set:");
		label.setFont(Constants.RESULT_FONT);
		optionManagerBox.add(label);
		optionManagerBox.add(Box.createHorizontalStrut(10));
		savedOptions = new JComboBox();
		savedOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (!updatingItems && savedOptions.getSelectedItem()!=null) {
					doLoadOptionSet(savedOptions.getSelectedItem().toString());
					updateControls();
				}
			}
		});
		rebuildOptionSetComboBox();
		optionManagerBox.add(savedOptions);
		optionManagerBox.add(Box.createHorizontalStrut(10));
		saveOptionSet = new JButton("Save Set");
		saveOptionSet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				doSaveOptionSet(currentOptionSetName);
				updateControls();
			}
		});
		optionManagerBox.add(saveOptionSet);
		optionManagerBox.add(Box.createHorizontalStrut(10));
		saveOptionSetAs = new JButton("Save Set As...");
		saveOptionSetAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				doSaveOptionSet(null);
				updateControls();
			}
		});
		optionManagerBox.add(saveOptionSetAs);
		optionManagerBox.add(Box.createHorizontalStrut(10));
		clearOptionSet = new JButton("Clear");
		clearOptionSet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (savedOptions.getSelectedItem()!=null) {
					doClearOptionSet(savedOptions.getSelectedItem().toString());
					updateControls();
				}
			}
		});
		optionManagerBox.add(clearOptionSet);
		optionManagerBox.add(Box.createHorizontalGlue());
		
		setLayout(new BorderLayout());
		add(optionManagerBox,BorderLayout.CENTER);
	}
	private void updateControls() {
		saveOptionSet.setEnabled(currentOptionSetName!=null);
		clearOptionSet.setEnabled(currentOptionSetName!=null);
	}
	private void loadPrefs() {
		PreferenceManager prefMan = new PreferenceManager("RealmSpeak","optionnames.cfg");
		prefMan.loadPreferences();
		currentOptionSetName = null; 
		optionSetNames = findOptionFiles();
		sortOptionNames();
		currentOptionSetName = prefMan.get(CURRENT_OPTION_SET);
		rebuildOptionSetComboBox();
	}
	private ArrayList<String> findOptionFiles() {
		ArrayList<String> list = new ArrayList<String>();
		String[] files = PreferenceManager.getPreferenceFilesAt(OPTION_SET_PATH);
		for(String file:files) {
			if (file.toLowerCase().endsWith(".cfg")) {
				list.add(file.substring(0,file.length()-4));
			}
		}
		return list;
	}
	private void updateOptionNames() {
		sortOptionNames();
		savePrefs();
		rebuildOptionSetComboBox();
	}
	private void sortOptionNames() {
		Collections.sort(optionSetNames,new Comparator<String>() {
			public int compare(String s1,String s2) {
				return s1.compareToIgnoreCase(s2);
			}
		});
	}
	private void savePrefs() {
		PreferenceManager prefMan = new PreferenceManager("RealmSpeak","optionnames.cfg");
		if (currentOptionSetName!=null) {
			prefMan.set(CURRENT_OPTION_SET,currentOptionSetName);
		}
		else {
			prefMan.remove(CURRENT_OPTION_SET);
		}
		prefMan.savePreferences();
	}
	private void rebuildOptionSetComboBox() {
		updatingItems = true;
		savedOptions.removeAllItems();
		savedOptions.addItem(BLANK_OPTION_SET);
		savedOptions.setSelectedIndex(0);
		if (optionSetNames!=null && !optionSetNames.isEmpty()) {
			for(String optionSetName:optionSetNames) {
				savedOptions.addItem(optionSetName);
			}
			if (currentOptionSetName!=null) {
				savedOptions.setSelectedItem(currentOptionSetName);
			}
		}
		updatingItems = false;
	}
	private boolean validOptionName(String optionName) {
		if (optionName==null) return false;
		if (optionName.trim().length()==0) return false;
		Pattern pattern = Pattern.compile("^[a-zA-Z0-9 ]+$");
		return pattern.matcher(optionName).matches();
	}
	private void doLoadOptionSet(String optionSetName) {
		if (optionSetName==currentOptionSetName) return;
		currentOptionSetName = null;
		if (optionSetName==BLANK_OPTION_SET) return;
		PreferenceManager prefMan = new PreferenceManager(OPTION_SET_PATH,optionSetName+".cfg");
		if (!prefMan.canLoad()) {
			optionSetNames.remove(optionSetName);
			updateOptionNames();
			return;
		}
		currentOptionSetName = optionSetName;
		prefMan.loadPreferences();
		hostGui.loadPrefs(prefMan);
		savePrefs();
	}
	private void doSaveOptionSet(String optionSetName) {
		if (optionSetName==null) {
			optionSetName = JOptionPane.showInputDialog("Name of option set?");
			if (!validOptionName(optionSetName)) return;
			if (optionSetNames.contains(optionSetName)) { // should be case sensitive...
				int ret = JOptionPane.showConfirmDialog(
						this,
						"Overwrite existing option set \""+optionSetName+"\"?",
						"Option Set Name Exists",
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE);

				if (ret!=JOptionPane.YES_OPTION) {
					return;
				}
			}
			else {
				optionSetNames.add(optionSetName);
				updateOptionNames();
			}
		}
		PreferenceManager prefMan = new PreferenceManager(OPTION_SET_PATH,optionSetName+".cfg");
		hostGui.savePrefs(prefMan);
		prefMan.savePreferences();
		currentOptionSetName = optionSetName;
		savedOptions.setSelectedItem(currentOptionSetName);
		savePrefs();
	}
	private void doClearOptionSet(String optionSetName) {
		currentOptionSetName = null;
		if (optionSetName==BLANK_OPTION_SET) return;
		PreferenceManager prefMan = new PreferenceManager(OPTION_SET_PATH,optionSetName+".cfg");
		if (!prefMan.erasePreferences()) {
			JOptionPane.showMessageDialog(this,"Unable to delete option set \""+optionSetName+"\"/","Cannot Delete File",JOptionPane.ERROR_MESSAGE);
			return;
		}
		optionSetNames.remove(optionSetName);
		updateOptionNames();
	}
}