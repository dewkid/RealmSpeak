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
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.*;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.general.io.FileManager;
import com.robin.general.swing.ComponentTools;
import com.robin.general.swing.ImageCache;
import com.robin.magic_realm.components.utility.*;

public class RealmCharacterBuilderFrame extends JFrame {
	
	private JMenu fileMenu;
	private JMenuItem newCharacterItem;
	private JMenuItem loadCharacterItem;
	private JMenuItem importCharacterItem;
	private JMenuItem closeCharacterItem;
	private JMenuItem saveCharacterItem;
	private JMenuItem saveAsCharacterItem;
	private JMenuItem exportCharacterCardItem;
	private JMenuItem exportCharacterCardWithoutPictureItem;
	private JMenuItem exportCharacterChitsAndGearItem;
	private JMenuItem exportAllCharacterGraphicsItem;
	private JMenuItem exitItem;
	
	private JMenu helpMenu;
	private JMenuItem graphicsHelpItem;
	
	private JPanel blankPanel;
	private RealmCharacterBuilderModel model;
	private RealmCharacterBuilderPanel buildPanel;
	
	private FileManager fileManager;
	
	private File savedFile = null;
	
	private GameData magicRealmData;
	
	public RealmCharacterBuilderFrame() {
		RealmLoader loader = new RealmLoader();
		magicRealmData = loader.getData();
		fileManager = new FileManager(this,"RealmSpeak Character Files (*.rschar)","rschar");
		File file = new File("./characters");
		if (!file.exists()) {
			// attempt to create the folder if it isn't there
			file.mkdir();
		}
		if (file.exists()) {
			fileManager.setCurrentDirectory(file);
		}
		initComponents();
	}
	private void initComponents() {
		setTitle("Realm Character Builder");
		setSize(1050,700);
//		setResizable(false);
		
		JMenuBar menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
			newCharacterItem = new JMenuItem("New Character");
			newCharacterItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					startNewCharacter();
					updateControls();
				}
			});
			newCharacterItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,InputEvent.CTRL_MASK));
		fileMenu.add(newCharacterItem);
			loadCharacterItem = new JMenuItem("Load Character");
			loadCharacterItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					load();
					updateControls();
				}
			});
			loadCharacterItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,InputEvent.CTRL_MASK));
		fileMenu.add(loadCharacterItem);
			importCharacterItem = new JMenuItem("Import Character");
			importCharacterItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					importCharacter();
					updateControls();
				}
			});
		fileMenu.add(importCharacterItem);
		fileMenu.add(new JSeparator());
			closeCharacterItem = new JMenuItem("Close Character");
			closeCharacterItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					closeCharacter();
				}
			});
		fileMenu.add(closeCharacterItem);
		fileMenu.add(new JSeparator());
			saveCharacterItem = new JMenuItem("Save Character");
			saveCharacterItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					save(false);
				}
			});
			saveCharacterItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_MASK));
		fileMenu.add(saveCharacterItem);
			saveAsCharacterItem = new JMenuItem("Save Character As...");
			saveAsCharacterItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					save(true);
				}
			});
		fileMenu.add(saveAsCharacterItem);
		fileMenu.add(new JSeparator());
			exportCharacterCardItem = new JMenuItem("Export Character Card...");
			exportCharacterCardItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					exportCard(true);
				}
			});
		fileMenu.add(exportCharacterCardItem);
			exportCharacterCardWithoutPictureItem = new JMenuItem("Export Character Card Without Picture...");
			exportCharacterCardWithoutPictureItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					exportCard(false);
				}
			});
		fileMenu.add(exportCharacterCardWithoutPictureItem);
			exportCharacterChitsAndGearItem = new JMenuItem("Export Gear...");
			exportCharacterChitsAndGearItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					exportStuff();
				}
			});
		fileMenu.add(exportCharacterChitsAndGearItem);
			exportAllCharacterGraphicsItem = new JMenuItem("Export All Character Graphics...");
			exportAllCharacterGraphicsItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					exportGraphics();
				}
			});
		fileMenu.add(exportAllCharacterGraphicsItem);
		fileMenu.add(new JSeparator());
			exitItem = new JMenuItem("Exit");
			exitItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					close();
				}
			});
		fileMenu.add(exitItem);
		
		helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);
			graphicsHelpItem = new JMenuItem("Graphics Help");
			graphicsHelpItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					showGraphicsHelp();
				}
			});
		helpMenu.add(graphicsHelpItem);
			
		setJMenuBar(menuBar);
		
		setLayout(new BorderLayout());
		blankPanel = new JPanel();
		blankPanel.setBackground(Color.gray);
		add(blankPanel,"Center");
		
		updateControls();
	}
	private void showGraphicsHelp() {
		StringBuffer sb = new StringBuffer();
		sb.append("<html><body>");
		sb.append("<h2>Format</h2>");
		sb.append("<div width=\"600\">You'll need to save all your graphics as GIF files.  I apologize if this is an inconvenience, but its the only way ");
		sb.append("to keep the image cache under control.  There are lots of tools out there (IrfanView, GIMP, etc) that will let you ");
		sb.append("convert from jpg/png/bmp to gif.</div><br>");
		sb.append("<div width=\"600\">For the symbols and weapons, you'll need to use a transparent background, or they wont translate ");
		sb.append("correctly onto the chits.</div><br>");
		sb.append("<h2>Recommended Image Sizes</h2>");
		sb.append("<table width=\"600\">");
		sb.append("<tr><td align=\"right\"><b>Character Portrait:</b></td><td>");
		sb.append(CharacterInfoCard.PICTURE_WIDTH);
		sb.append(" x ");
		sb.append(CharacterInfoCard.PICTURE_HEIGHT);
		sb.append("</td><td>The builder will automatically resize the picture to this if you don't!</td></tr>");
		sb.append("<tr><td align=\"right\"><b>Character Symbol Icon:</b></td><td>");
		sb.append("90 x 90 or smaller</td><td>Use transparent background</td></tr>");
		sb.append("<tr><td align=\"right\"><b>Weapon Icon:</b></td><td>");
		sb.append("90 x 90 or smaller</td><td>Use transparent background</td></tr>");
		sb.append("<tr><td align=\"right\"><b>Badge/Advantage Icon:</b></td><td>");
		sb.append("32 x 32</td><td>This should be exact</td></tr>");
		sb.append("</table>");
		sb.append("</html></body>");
		JOptionPane.showMessageDialog(this,sb.toString(),"Graphics Help",JOptionPane.INFORMATION_MESSAGE);
	}
	private void updateControls() {
		newCharacterItem.setEnabled(model==null);
		loadCharacterItem.setEnabled(model==null);
		importCharacterItem.setEnabled(model==null);
		closeCharacterItem.setEnabled(model!=null);
		saveCharacterItem.setEnabled(model!=null);
		saveAsCharacterItem.setEnabled(model!=null);
		exportCharacterCardItem.setEnabled(model!=null);
		exportCharacterCardWithoutPictureItem.setEnabled(model!=null);
		exportCharacterChitsAndGearItem.setEnabled(model!=null);
		exportAllCharacterGraphicsItem.setEnabled(model!=null);
	}
	private void importCharacter() {
		ArrayList<String> list = TemplateLibrary.getSingleton().getAllCharacterTemplateNames();
		Collections.sort(list);
		String name = (String)JOptionPane.showInputDialog(this,"Choose a character to import:","Import Character",JOptionPane.PLAIN_MESSAGE,null,list.toArray(),list.get(0));
		if (name!=null && name.trim().length()>0) {
			GameObject template = TemplateLibrary.getSingleton().getCharacterTemplate(name);
			model = new RealmCharacterBuilderModel(RealmCharacterBuilderModel.createCharacterFromTemplate(template));
			
			// Need to update the symbol icon here
			ImageIcon icon = model.getCharacter().getFullSizedIcon();
			model.updateSymbolIcon(icon,template.getThisAttribute(Constants.ICON_TYPE));
			
			savedFile = null;
			startEditCharacter();
		}
	}
	private void load() {
		File currentPath = fileManager.getLoadPath();
		if (currentPath==null) {
			return;
		}
		
		ImageCache.resetCache();

		try {
			model= RealmCharacterBuilderModel.createFromFile(currentPath);
		}
		catch(RealmCharacterException ex) {
			JOptionPane.showMessageDialog(null,"There was a problem loading the file ("+currentPath.getName()+"):\n\n     "+ex.getMessage());
		}
		if (model!=null) {
			savedFile = currentPath;
			startEditCharacter();
		}
	}
	private void save(boolean forceNew) {
		if (savedFile==null || forceNew) {
			String val = savedFile==null?model.getCharacter().getCharacterLevelName(4):savedFile.getName();
			savedFile = fileManager.getSavePath(val);
			if (savedFile==null) {
				return;
			}
		}
		
		if (model.saveToFile(savedFile,false)) {
			Window window = new Window(this);
			window.setSize(160,80);
			window.setLayout(new BorderLayout());
			JLabel label = new JLabel("Saved",JLabel.CENTER);
			label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),BorderFactory.createLineBorder(Color.green,4)));
			label.setFont(new Font("Dialog",Font.BOLD,18));
			window.add(label,"Center");
			window.setLocationRelativeTo(this);
			window.setVisible(true);
			window.paint(window.getGraphics());
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			window.setVisible(false);
		}
	}
	private void exportCard(boolean includePicture) {
		File path = buildPanel.getGraphicsManager().getSavePath();
		if (path==null) {
			return;
		}
		buildPanel.saveCardTo(path,includePicture);
	}
	private void exportStuff() {
		File path = buildPanel.getGraphicsManager().getSavePath();
		if (path==null) {
			return;
		}
		buildPanel.saveStuffTo(path);
	}
	private void exportGraphics() {
		File path = buildPanel.getGraphicsManager().getSaveDirectory("Export Graphics");
		if (path==null) {
			return;
		}
		File finalPath = new File(path.getAbsolutePath()+File.separator+model.getCharacter().getCharacterLevelName(4)+"_export"+File.separator);
		if (!finalPath.exists()) {
			finalPath.mkdir();
		}
		model.saveToFile(finalPath,true);
	}
	private void close() {
		System.exit(0);
	}
	private void startNewCharacter() {
		savedFile = null;
		model = new RealmCharacterBuilderModel(RealmCharacterBuilderModel.createEmptyCharacter());
		startEditCharacter();
	}
	private void startEditCharacter() {
		buildPanel = new RealmCharacterBuilderPanel(this,model,magicRealmData);
		remove(blankPanel);
		add(buildPanel,"Center");
		buildPanel.revalidate();
		repaint();
	}
	private void closeCharacter() {
		remove(buildPanel);
		add(blankPanel,"Center");
		repaint();
		model = null;
		buildPanel = null;
		updateControls();
	}
	public static void main(String[] args) {
		ComponentTools.setSystemLookAndFeel();
		RealmUtility.setupTextType();
		final RealmCharacterBuilderFrame frame = new RealmCharacterBuilderFrame();
		frame.setLocationRelativeTo(null);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				frame.close();
			}
		});
		frame.setVisible(true);
//		frame.startNewCharacter();
//		frame.updateControls();
	}
}