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
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.*;

import javax.swing.*;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameSetup;
import com.robin.general.io.PreferenceManager;
import com.robin.general.swing.ComponentTools;

public class GameBuilderFrame extends JFrame {
	public static final String LAST_DIR = "last_dir";
	protected PreferenceManager prefs;

	protected JMenu fileMenu;
		protected JMenuItem newFile;
		protected JMenuItem openFile;
		protected JMenuItem closeFile;
		protected JMenuItem saveFile;
		protected JMenuItem saveAllFile;
		protected JMenuItem saveAsFile;
		protected JMenuItem revertFile;
		protected JMenuItem exitFile;
	protected JMenu viewMenu;
		protected JMenuItem treeView;
	protected JMenu toolMenu;
		protected JMenuItem renumberTool;
	protected JMenu setupMenu;
		protected JMenuItem doSetupSetup;
		
	protected JDesktopPane desktop;
	protected int desktopWindowCount;
	protected ArrayList gameDataFrames;
	protected ArrayList openGameNames;
	
	protected File lastPath;
	
	public GameBuilderFrame() {
		gameDataFrames = new ArrayList();
		openGameNames = new ArrayList();
		prefs = new PreferenceManager("GameBuilder","GameBuilder.cfg") {
			protected void createDefaultPreferences(Properties props) {
				props.put(LAST_DIR,System.getProperty("user.home"));
			}
		};
		prefs.loadPreferences();
		lastPath = new File(prefs.get(LAST_DIR));
		initComponents();
	}
	public JDesktopPane getDesktop() {
		return desktop;
	}
	public File getLastPath() {
		return lastPath;
	}
	public void setLastPath(File path) {
		lastPath = path;
	}
	public JMenuBar createMenu() {
		JMenuBar bar = new JMenuBar();
			fileMenu = new JMenu("File");
			fileMenu.setMnemonic(KeyEvent.VK_F);
				newFile = new JMenuItem("New Game...");
				newFile.setMnemonic(KeyEvent.VK_N);
				newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,Event.CTRL_MASK));
				newFile.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						newGame();
					}
				});
			fileMenu.add(newFile);
				openFile = new JMenuItem("Open Game...");
				openFile.setMnemonic(KeyEvent.VK_O);
				openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,Event.CTRL_MASK));
				openFile.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						openGame();
					}
				});
			fileMenu.add(openFile);
				closeFile = new JMenuItem("Close Game");
				closeFile.setMnemonic(KeyEvent.VK_C);
				closeFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,Event.CTRL_MASK));
				closeFile.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						closeGame();
					}
				});
			fileMenu.add(closeFile);
			fileMenu.add(new JSeparator());
				saveFile = new JMenuItem("Save Game");
				saveFile.setMnemonic(KeyEvent.VK_S);
				saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,Event.CTRL_MASK));
				saveFile.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						saveGame();
					}
				});
			fileMenu.add(saveFile);
				saveAllFile = new JMenuItem("Save All Games");
				saveAllFile.setMnemonic(KeyEvent.VK_L);
				saveAllFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,Event.CTRL_MASK|Event.SHIFT_MASK));
				saveAllFile.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						saveAllGame();
					}
				});
			fileMenu.add(saveAllFile);
				saveAsFile = new JMenuItem("Save Game As...");
				saveAsFile.setMnemonic(KeyEvent.VK_A);
				saveAsFile.addActionListener(new ActionListener() { 
					public void actionPerformed(ActionEvent ev) {
						saveAsGame();
					}
				});
			fileMenu.add(saveAsFile);
				revertFile = new JMenuItem("Revert...");
				revertFile.setMnemonic(KeyEvent.VK_R);
				revertFile.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						revertGame();
					}
				});
			fileMenu.add(revertFile);
			fileMenu.add(new JSeparator());
				exitFile = new JMenuItem("Exit");
				exitFile.setMnemonic(KeyEvent.VK_X);
				exitFile.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						exitApp();
					}
				});
			fileMenu.add(exitFile);
		bar.add(fileMenu);
			viewMenu = new JMenu("View");
			viewMenu.setMnemonic(KeyEvent.VK_V);
				treeView = new JMenuItem("View tree");
				treeView.setMnemonic(KeyEvent.VK_T);
				treeView.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						GameDataFrame frame = getFrontDataFrame();
						GameObjectTreeView view = new GameObjectTreeView(frame.getGameData().getGameObjects());
						view.setVisible(true);
					}
				});
			viewMenu.add(treeView);
		bar.add(viewMenu);
		
			toolMenu = new JMenu("Tools");
			toolMenu.setMnemonic(KeyEvent.VK_T);
				renumberTool = new JMenuItem("Renumber objects by name");
				renumberTool.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						GameDataFrame frame = getFrontDataFrame();
						GameData data = frame.getGameData();
						data.renumberObjectsByName();
						updateMenu();
					}
				});
			toolMenu.add(renumberTool);
		bar.add(toolMenu);
			setupMenu = new JMenu("Setup");
			setupMenu.setMnemonic(KeyEvent.VK_S);
				doSetupSetup = new JMenuItem("Test Setup...");
				doSetupSetup.setMnemonic(KeyEvent.VK_T);
				doSetupSetup.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						GameDataFrame frame = getFrontDataFrame();
						GameData data = frame.getGameData();
						
						GameSetup setup = (GameSetup)JOptionPane.showInputDialog(
													GameBuilderFrame.this,
													"Setup Name",
													"Test Setup",
													JOptionPane.QUESTION_MESSAGE,
													null,
													data.getGameSetups().toArray(),
													data.getGameSetups().iterator().next());

						if (setup==null) return;
						StringBuffer result = new StringBuffer();
						ArrayList<String> keyVals = new ArrayList<String>();
						//keyVals.add("original_game");
						//keyVals.add("rw_expansion_1"); // TODO These keyVals need to be available with each setup somehow
						GameObjectTreeView view = new GameObjectTreeView(data.doSetup(result,setup,keyVals));
						view.setTitle("Test Setup");
						view.setVisible(true);
						
						showResults("Test Setup output",result.toString());
					}
				});
			setupMenu.add(doSetupSetup);
		bar.add(setupMenu);
		
		return bar;
	}
	public void initComponents() {
		setTitle("GBuilder");
		ComponentTools.maximize(this);
		setLocation(0,0);
		desktop = new JDesktopPane();
		desktopWindowCount = 0;
		setContentPane(desktop);
		setJMenuBar(createMenu());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				exitApp();
			}
		});
		desktop.addContainerListener(new ContainerListener() {
			public void componentAdded(ContainerEvent ev) {
				desktopWindowCount++;
				updateMenu();
			}
			public void componentRemoved(ContainerEvent ev) {
				desktopWindowCount--;
				updateMenu();
			}
		});
		updateMenu();
	}
	public void showResults(String title,String result) {
		JFrame results = new JFrame(title);
		results.setSize(400,400);
		results.getContentPane().setLayout(new BorderLayout());
		results.getContentPane().add(new JScrollPane(new JTextArea(result)),"Center");
		results.setVisible(true);
	}
	public void newGame() {
		String name = JOptionPane.showInputDialog("Name for new game?");
		if (name!=null && name.trim().length()>0) {
			if (!openGameNames.contains(name)) {
				GameData data = new GameData(name);
				data.ignoreRandomSeed = true;
				addDataFrame(new GameDataFrame(this,data));
			}
			else {
				JOptionPane.showMessageDialog(this,"Game of same name is already open.","Game Name Already Used",JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	public void openGame() {
		GameData data = new GameData();
		data.ignoreRandomSeed = true;
		data.reportFormatErrors = false;
		data.setFilter(true);
		GameDataFrame frame = new GameDataFrame(this,data);
		if (frame.load(this)) {
			frame.resetGameDescription();
			if (!openGameNames.contains(data.getGameName())) {
				addDataFrame(frame);
			}
			else {
				JOptionPane.showMessageDialog(this,"Game of same name is already open.","Game Name Already Used",JOptionPane.ERROR_MESSAGE);
			}
		}
		else {
			JOptionPane.showMessageDialog(this,"Unable to read data file.","Invalid File",JOptionPane.ERROR_MESSAGE);
		}
	}
	public void closeGame() {
		GameDataFrame activeDataFrame = getFrontDataFrame();
		activeDataFrame.close(this);
		updateMenu();
	}
	public void saveGame() {
		GameDataFrame activeDataFrame = getFrontDataFrame();
		if (!activeDataFrame.save(this)) {
			JOptionPane.showMessageDialog(this,"Unable to save data file!","File Save Error",JOptionPane.ERROR_MESSAGE);
		}
		
		updateMenu();
	}
	public void saveAllGame() {
		for (Iterator i=gameDataFrames.iterator();i.hasNext();) {
			GameDataFrame frame = (GameDataFrame)i.next();
			if (frame.isModified()) {
				frame.save(this);
			}
		}
		updateMenu();
	}
	public void saveAsGame() {
		// Find active GameData window
//		Saveable obj = (Saveable)desktop.getSelectedFrame();
		GameDataFrame activeDataFrame = getFrontDataFrame();
		activeDataFrame.saveAs(this);
		updateMenu();
	}
	public void revertGame() {
		if (JOptionPane.showConfirmDialog(this,"Reverting to old version will lose any changes made since last save.\nContinue?","Revert to saved version",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
			GameDataFrame activeDataFrame = getFrontDataFrame();
			if (activeDataFrame.revert(this)) {
				JOptionPane.showMessageDialog(this,"Game successfully reverted.","Done",JOptionPane.INFORMATION_MESSAGE);
			}
			updateMenu();
		}
	}
	public void exitApp() {
		// Should check all GameData windows, and verify that changes have been saved
		prefs.set(LAST_DIR,lastPath.getPath());
		prefs.savePreferences();
		setVisible(false);
		dispose();
		System.out.println("Safe exit.");
		System.exit(0);
	}
	public GameDataFrame getFrontDataFrame() {
		for (Iterator i=gameDataFrames.iterator();i.hasNext();) {
			GameDataFrame frame = (GameDataFrame)i.next();
			if (frame.getLayer()==0) {
				return frame;
			}
		}
		return null;
	}
	public void addDataFrame(GameDataFrame frame) {
		Dimension size = desktop.getSize();
		int width = Math.min(600,size.width);
		int height = size.height;
		frame.setSize(new Dimension(width,height));
		openGameNames.add(frame.getGameData().getGameName());
		gameDataFrames.add(frame);
		frame.setVisible(true);
		desktop.add(frame);
		try {
			frame.setSelected(true);
		}
		catch(PropertyVetoException ex) {
			ex.printStackTrace();
		}
		updateMenu();
	}
	public void removeDataFrame(GameDataFrame frame) {
		desktop.remove(frame);
		openGameNames.remove(frame.getGameData().getGameName());
		gameDataFrames.remove(frame);
		repaint();
	}
	public void updateMenu() {
		GameDataFrame activeDataFrame = getFrontDataFrame();
		closeFile.setEnabled(activeDataFrame!=null);
		saveFile.setEnabled(activeDataFrame!=null && activeDataFrame.isModified());
		boolean foundModified = false;
		for (Iterator i=gameDataFrames.iterator();i.hasNext();) {
			GameDataFrame frame = (GameDataFrame)i.next();
			if (frame.isModified()) {
				// Only the first modified file is needed to set enabled status
				foundModified = true;
				break;
			}
		}
		saveAllFile.setEnabled(foundModified);
		treeView.setEnabled(activeDataFrame!=null);
		doSetupSetup.setEnabled(activeDataFrame!=null && activeDataFrame.getGameData().getGameSetups().size()>0);
	}
	public static void main(String[]args) {
		ComponentTools.setSystemLookAndFeel();
		GameBuilderFrame frame = new GameBuilderFrame();
		frame.setVisible(true);
	}
}