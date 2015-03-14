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
package com.robin.magic_realm.RealmGm;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.io.File;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.filechooser.FileFilter;

import com.robin.game.objects.GameData;
import com.robin.general.io.FileUtilities;
import com.robin.general.io.PreferenceManager;
import com.robin.general.swing.ComponentTools;
import com.robin.magic_realm.RealmCharacterBuilder.RealmCharacterBuilderModel;
import com.robin.magic_realm.components.utility.RealmUtility;

public class RealmGmFrame extends JFrame {
	private static final String MetalLookAndFeel = "MLAF";
	private static final String PreferredFilePath = "PFP";
	
//	private static final String DefaultWidth = "DW";
//	private static final String DefaultHeight = "DH";
	
	private PreferenceManager prefs;
	private JDesktopPane desktop;
	private RealmGameEditor editor;
	
	private JMenuItem openGame;
	private JMenuItem closeGame;
	private JMenuItem saveGame;
	private JMenuItem saveAsGame;
	
	protected FileFilter saveGameFileFilter = new FileFilter() {
		public boolean accept(File f) {
			return f.isDirectory() || (f.isFile() && f.getPath().endsWith("rsgame"));
		}

		public String getDescription() {
			return "RealmSpeak Save Files (*.rsgame)";
		}
	};
	
	public RealmGmFrame() {
		prefs = new PreferenceManager("RealmSpeak","RealmGm");
		prefs.loadPreferences();
		if (!prefs.canLoad()) {
			//prefs.set(DefaultWidth,800);
			//prefs.set(DefaultHeight,600);
		}
		initComponents();
	}
	private void savePrefs() {
		//prefs.set(DefaultWidth,getSize().width);
		//prefs.set(DefaultHeight,getSize().height);
		prefs.savePreferences();
	}
	private void initComponents() {
		updateLookAndFeel();
		setTitle("RealmSpeak GM");
		setSize(1024,768);
		//setSize(prefs.getInt(DefaultWidth),prefs.getInt(DefaultHeight));
		setLocationRelativeTo(null);
		
		setJMenuBar(buildMenuBar());
		setLayout(new BorderLayout());
		desktop = new JDesktopPane();
		add(desktop,BorderLayout.CENTER);
		
		updateControls();
	}
	public void updateControls() {
		openGame.setEnabled(editor==null);
		closeGame.setEnabled(editor!=null);
		saveGame.setEnabled(editor!=null && editor.getGameData().isModified());
		saveAsGame.setEnabled(editor!=null);
	}
	private void updateLookAndFeel() {
		if (prefs.getBoolean(MetalLookAndFeel)) {
			ComponentTools.setMetalLookAndFeel();
		}
		else {
			ComponentTools.setSystemLookAndFeel();
		}
		SwingUtilities.updateComponentTreeUI(this);
	}
	private JMenuBar buildMenuBar() {
		JMenuBar menu = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		openGame = new JMenuItem("Open Game");
		openGame.setMnemonic(KeyEvent.VK_O);
		openGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				openGame();
			}
		});
		fileMenu.add(openGame);
		closeGame = new JMenuItem("Close Game");
		closeGame.setMnemonic(KeyEvent.VK_C);
		closeGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				closeGame();
			}
		});
		fileMenu.add(closeGame);
		saveGame = new JMenuItem("Save Game");
		saveGame.setMnemonic(KeyEvent.VK_S);
		saveGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_MASK));
		saveGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				saveGame(false);
			}
		});
		fileMenu.add(saveGame);
		saveAsGame = new JMenuItem("Save Game As...");
		saveAsGame.setMnemonic(KeyEvent.VK_A);
		saveAsGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				saveGame(true);
			}
		});
		fileMenu.add(saveAsGame);
		fileMenu.add(new JSeparator());
		JMenuItem exit = new JMenuItem("Exit");
		exit.setMnemonic(KeyEvent.VK_X);
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				close();
			}
		});
		fileMenu.add(exit);
		menu.add(fileMenu);
		JMenu optionMenu = new JMenu("Options");
		final JCheckBoxMenuItem toggleLookAndFeel = new JCheckBoxMenuItem("Cross Platform Look and Feel",prefs.getBoolean(MetalLookAndFeel));
		toggleLookAndFeel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				prefs.set(MetalLookAndFeel,toggleLookAndFeel.isSelected());
				updateLookAndFeel();
			}
		});
		optionMenu.add(toggleLookAndFeel);
		menu.add(optionMenu);
		return menu;
	}
	private void closeGame() {
		if (validateOkayToClose("Close Game")) {
			editor.setVisible(false);
			desktop.remove(editor);
			editor = null;
			updateControls();
		}
	}
	private boolean validateOkayToClose(String title) {
		if (editor.getGameData().isModified()) {
			int ret = JOptionPane.showConfirmDialog(this,"The current game hasn't been saved.  Save now?",title,JOptionPane.YES_NO_CANCEL_OPTION);
			if (ret==JOptionPane.YES_OPTION) {
				saveGame(false);
			}
			else if (ret==JOptionPane.CANCEL_OPTION) {
				return false;
			}
		}
		return true;
	}
	private void openGame() {
		JFileChooser chooser;
		String lastSaveGame = prefs.get(PreferredFilePath);
		if (lastSaveGame!=null) {
			String filePath = FileUtilities.getFilePathString(new File(lastSaveGame),false,false);
			chooser = new JFileChooser(new File(filePath));
		}
		else {
			chooser = new JFileChooser();
		}
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(saveGameFileFilter);
		if (chooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
			File file = FileUtilities.fixFileExtension(chooser.getSelectedFile(),".rsgame");
			prefs.set(PreferredFilePath,file.getAbsolutePath());
			GameData gameData = new GameData();
			gameData.zipFromFile(file);
//			gameData.setTracksChanges(true);
			addGame(FileUtilities.getFilename(file,true),gameData);
			
//			for (Iterator i=gameData.getObjectChanges().iterator();i.hasNext();) {
//				System.out.println(i.next());
//			}
		}
		updateControls();
	}
	private File queryFileName() {
		JFileChooser chooser;
		String lastSaveGame = prefs.get(PreferredFilePath);
		if (lastSaveGame!=null) {
			String filePath = FileUtilities.getFilePathString(new File(lastSaveGame),false,false);
			chooser = new JFileChooser(new File(filePath));
			chooser.setSelectedFile(new File(lastSaveGame));
		}
		else {
			chooser = new JFileChooser();
		}
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(saveGameFileFilter);
		if (chooser.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {
			File file = FileUtilities.fixFileExtension(chooser.getSelectedFile(),".rsgame");
			prefs.set(PreferredFilePath,file.getAbsolutePath());
			return file;
		}
		return null;
	}
	private void saveGame(boolean queryFilename) {
		File file;
		String lastSaveGame = prefs.get(PreferredFilePath);
		if (queryFilename || lastSaveGame==null) {
			file = queryFileName();
		}
		else {
			file = new File(lastSaveGame);
		}
		if (file!=null) {
			editor.setTitle(FileUtilities.getFilename(file,true));
			editor.getGameData().zipToFile(file);
			editor.getGameData().commit();
			updateControls();
		}
	}
	private void addGame(String title,GameData gameData) {
		if (editor!=null) {
			closeGame();
		}
		
		editor = new RealmGameEditor(this,title,gameData);
		editor.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		editor.addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosing(InternalFrameEvent e) {
				closeGame();
			}
		});
		desktop.add(editor);
		editor.setVisible(true);
		try {
			editor.setSelected(true);
			editor.setMaximum(true);
		}
		catch(PropertyVetoException ex) {
			ex.printStackTrace();
		}
	}
	private void close() {
		savePrefs();
		setVisible(false);
		System.exit(0);
	}
	public static void main(String[] args) {
		RealmCharacterBuilderModel.loadAllCustomCharacters();
		RealmUtility.setupTextType();
		final RealmGmFrame frame = new RealmGmFrame();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				frame.close();
			}
		});
		frame.setVisible(true);
	}
}