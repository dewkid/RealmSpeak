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

import com.robin.general.util.*;

public class GameOptionPane extends JPanel implements ActionListener {

	private OrderedHashtable keyHash; // String key:String tabKey
	private OrderedHashtable tabHash; // String tabKey:OrderedHashtable options or String tabKey:Component c
	private Hashtable tabDescriptionHash; // String tabKey:String desc
	private boolean editMode;
	private int tabPlacement = JTabbedPane.TOP;
	private ArrayList<ActionListener> actionListeners;
	
	public GameOptionPane() {
		this(JTabbedPane.TOP,true);
	}
	public GameOptionPane(int tabPlacement) {
		this(tabPlacement,true);
	}
	public GameOptionPane(boolean editMode) {
		this(JTabbedPane.TOP,editMode);
	}
	public GameOptionPane(int tabPlacement,boolean editMode) {
		super(new BorderLayout());
		this.editMode = editMode;
		keyHash = new OrderedHashtable();
		tabHash = new OrderedHashtable();
		tabDescriptionHash = new Hashtable();
		this.tabPlacement = tabPlacement;
	}
	public void addActionListener(ActionListener actionListener) {
		if (actionListeners==null) {
			actionListeners = new ArrayList<ActionListener>();
		}
		if (!actionListeners.contains(actionListener)) {
			actionListeners.add(actionListener);
		}
	}
	public void removeActionListener(ActionListener actionListener) {
		if (actionListeners!=null) {
			actionListeners.remove(actionListener);
			if (actionListeners.isEmpty()) {
				actionListeners = null;
			}
		}
	}
	public String[] getTabKeys() {
		return (String[])tabHash.keySet().toArray(new String[0]);
	}
//	public ArrayList<String> getOrderedOptionKeys() {
//		ArrayList<String> list = new ArrayList<String>();
//		return list;
//	}
	public String[] getOptionDescriptions(String tabKey,boolean active) {
		Object obj = tabHash.get(tabKey);
		if (obj instanceof OrderedHashtable) {
			ArrayList rules = new ArrayList();
			OrderedHashtable options = (OrderedHashtable)obj;
			for (Iterator i=options.orderedKeys().iterator();i.hasNext();) {
				String key = (String)i.next();
				GameOption option = (GameOption)options.get(key);
				if (active == option.isActive()) {
					rules.add(option.getDescription());
				}
			}
			return (String[])rules.toArray(new String[rules.size()]);
		}
		return null;
	}
	
	/**
	 * Allows you to add a component to the GameOptionPane JTabbedPane, without making it
	 * a GameOption.
	 */
	public void addTab(String tabName,Component c) {
		tabHash.put(tabName,c);
	}
	
	public void setTabHtmlDescription(String tabKey,String htmlDescription) {
		tabDescriptionHash.put(tabKey,htmlDescription);
	}
	public void addOption(String tabKey,GameOption option) {
		if (!keyHash.containsKey(option.getKey())) {
			OrderedHashtable options = (OrderedHashtable)tabHash.get(tabKey);
			if (options==null) {
				options = new OrderedHashtable();
				tabHash.put(tabKey,options);
			}
			options.put(option.getKey(),option);
			keyHash.put(option.getKey(),tabKey);
			option.setGameOptionPane(this);
			option.setActionListener(this);
		}
		else {
			throw new IllegalArgumentException("Can't add an option with the same key twice!");
		}
	}
	public GameOption getGameOption(String optionKey) {
		GameOption option = null;
		String tabKey = (String)keyHash.get(optionKey);
		if (tabKey!=null) {
			OrderedHashtable options = (OrderedHashtable)tabHash.get(tabKey);
			option = (GameOption)options.get(optionKey);
		}
		return option;
	}
	public boolean getOption(String optionKey) {
		GameOption option = getGameOption(optionKey);
		if (option!=null) {
			return option.isActive();
		}
		throw new IllegalStateException("!!");
	}
	public void setOption(String optionKey,boolean val) {
		GameOption option = getGameOption(optionKey);
		if (option!=null) {
			option.setActive(val);
			return;
		}
		throw new IllegalStateException("!!");
	}
	public Collection getGameOptionKeys() {
		return keyHash.keySet();
	}
	public void buildPane() {
		JTabbedPane tabPane = new JTabbedPane(tabPlacement);
		if (tabPlacement==JTabbedPane.LEFT) {
			tabPane.setFont(new Font("Dialog",Font.BOLD,14));
		}
		for (Iterator i=tabHash.orderedKeys().iterator();i.hasNext();) {
			String tabKey = (String)i.next();
			
			JPanel tabPanel = new JPanel(new BorderLayout());
			
			Object obj = tabHash.get(tabKey);
			if (obj instanceof Component) {
				tabPanel.add((Component)obj,"Center");
			}
			else {
				Box box = Box.createVerticalBox();
			
				OrderedHashtable options = (OrderedHashtable)obj;
				for (Iterator o=options.values().iterator();o.hasNext();) {
					GameOption option = (GameOption)o.next();
					option.setEnabled(editMode);
					box.add(option.getPanel());
				}
				box.add(Box.createVerticalGlue());
				tabPanel.add(new JScrollPane(box),"Center");
			}
			
			// Add description
			String html = (String)tabDescriptionHash.get(tabKey);
			if (html!=null) {
				Box hbox = Box.createHorizontalBox();
					JEditorPane pane = new JEditorPane("text/html",html) {
						public boolean isFocusTraversable() {
							return false;
						}
					};
					pane.setEditable(false);
					pane.setOpaque(true);
					pane.setBackground(new Color(220,220,255));
					pane.setMinimumSize(new Dimension(50,50));
					pane.setPreferredSize(new Dimension(50,50));
				hbox.add(pane);
					Box vbox = Box.createVerticalBox();
					if (editMode) {
							TabActionButton allButton = new TabActionButton("All",tabKey);
							allButton.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent ev) {
									TabActionButton thisButton = (TabActionButton)ev.getSource();
									selectAll(thisButton.getTabKey());
								}
							});
						vbox.add(allButton);
							TabActionButton noneButton = new TabActionButton("None",tabKey);
							noneButton.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent ev) {
									TabActionButton thisButton = (TabActionButton)ev.getSource();
									selectNone(thisButton.getTabKey());
								}
							});
						vbox.add(noneButton);
					}
				hbox.add(vbox);
				
				tabPanel.add(hbox,"North");
			}
			tabPane.addTab(tabKey,tabPanel);
		}
		add(tabPane,"Center");
	}
	public void selectAll(String tabKey) {
		setActive(tabKey,true);
	}
	public void selectNone(String tabKey) {
		setActive(tabKey,false);
	}
	private void setActive(String tabKey,boolean active) {
		Object obj = tabHash.get(tabKey);
		if (obj instanceof OrderedHashtable) {
			OrderedHashtable hash = (OrderedHashtable)obj;
			for (Iterator i=hash.values().iterator();i.hasNext();) {
				GameOption option = (GameOption)i.next();
				option.setActive(active);
			}
		}
	}
	private class TabActionButton extends JButton {
		private String tabKey;
		public TabActionButton(String text,String tabKey) {
			super(text);
			this.tabKey = tabKey;
		}
		public String getTabKey() {
			return tabKey;
		}
	};
	public void dumpResults() {
		for (Iterator i=tabHash.keySet().iterator();i.hasNext();) {
			String tabKey = (String)i.next();
			System.out.println("TAB "+tabKey);
			
			OrderedHashtable options = (OrderedHashtable)tabHash.get(tabKey);
			for (Iterator o=options.values().iterator();o.hasNext();) {
				GameOption option = (GameOption)o.next();
				System.out.println("   GameOption "+option.getKey()+" is "+option.isActive());
			}
		}
	}
	
	/**
	 * For testing only
	 */
	public static void main(String[] args) {
		ComponentTools.setSystemLookAndFeel();
//		int night = 14;
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
			final GameOptionPane pane = new GameOptionPane(false);
			pane.addOption("Rules",new GameOption("TEST_KEY_1","This is just a test of the option thing",false));
			pane.addOption("Rules",new GameOption("TEST_KEY_2","Maybe you would like something different",false));
			pane.addOption("Rules",new GameOption("TEST_KEY_3","Why the heck would you want to?  Who knows, but this should be long enough to test the bit where text wraps around and fills up the box as much as possible.  Will it work?  Who knows!!  What if this got really really really really long?  Would it still work?",false));
			pane.addOption("Rules",new GameOption("TEST_KEY_4","Zerrrrba",true));
			pane.addOption("Zamfoo",new GameOption("TEST_KEY_5","One",false));
			pane.addOption("Zamfoo",new GameOption("TEST_KEY_6","Two",true));
			pane.addOption("Zamfoo",new GameOption("TEST_KEY_7","Three?",false));
			pane.addOption("Zamfoo",new GameOption("TEST_KEY_8","Four",true));
			pane.setTabHtmlDescription("Rules", "Beef, its whats for dinner!");
			pane.setTabHtmlDescription("Zamfoo", "So, whats a Zamfoo anyway?!");
			pane.buildPane();
		frame.getContentPane().add(pane,"Center");
			JButton button = new JButton("Done");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					System.out.println("-----------------------------------------------");
					pane.dumpResults();
					System.out.println("TEST_KEY_8="+pane.getOption("TEST_KEY_8"));
					System.exit(0);
				}
			});
		frame.getContentPane().add(button,"South");
		frame.setSize(400,400);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	public void actionPerformed(ActionEvent e) {
		fireActionPerformed();
	}
	private void fireActionPerformed() {
		if (actionListeners==null) return;
		ActionEvent ev = new ActionEvent(this,0,"");
		for (ActionListener actionListener:actionListeners) {
			actionListener.actionPerformed(ev);
		}
	}
}