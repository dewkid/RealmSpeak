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
package com.robin.magic_realm.RealmCharacterWeb;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.robin.general.io.ArgumentParser;
import com.robin.general.io.FileManager;
import com.robin.general.swing.ComponentTools;
import com.robin.magic_realm.RealmCharacterBuilder.CharacterInfoCard;
import com.robin.magic_realm.RealmCharacterWeb.build.*;

public class RealmCharacterWebFrame extends JFrame {
	private static final String WEB_LAYOUT_FILE = "webLayout.xml";
	
	private File characterFolder;
	
	private File webLayoutFile;
	private ArrayList<File> rscharFiles;
	private ArrayList<RscharLayout> layoutRecords;
	
	private JMenu fileMenu;
	private JMenuItem saveMenuItem;
	private JMenuItem exportWebMenuItem;
	private JMenuItem exitMenuItem;
	
	private JTable layoutTable;
	private RscharTableModel tableModel;
	
	private JTextArea webFolderText;
	private JButton setWebFolderTextButton;
	
	private FileManager fileManager;

	public RealmCharacterWebFrame(File characterFolder) {
		this.characterFolder = characterFolder;
		fileManager = new FileManager(this,"","");
		initComponents();
		refresh();
	}
	private void refresh() {
		fetchRscharFiles();
		layoutRecords = new ArrayList<RscharLayout>();
		if (webLayoutFile!=null) {
			if (!loadFromFile(webLayoutFile)) {
				webLayoutFile = null;
			}
		}
		buildLayoutRecords();
		tableModel.setRecords(layoutRecords);
	}
	private void initComponents() {
		setTitle("Character Web Builder");
		setSize(800,600);
		setLayout(new BorderLayout());
		
		tableModel = new RscharTableModel();
		layoutTable = new JTable(tableModel);
		tableModel.formatColumns(layoutTable);
		add(new JScrollPane(layoutTable),"Center");
		layoutTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				updateControls();
			}
		});
		layoutTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent ev) {
				if (ev.getClickCount()==2) {
					int row = layoutTable.getSelectedRow();
					if (row>=0) {
						RscharLayout layout = (RscharLayout)layoutRecords.get(row);
						CharacterInfoCard card = layout.getModel().getCard();
						ImageIcon icon = card.getImageIcon(true);
						JOptionPane.showMessageDialog(RealmCharacterWebFrame.this,new JLabel(icon));
					}
				}
			}
		});
		
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		setWebFolderTextButton = new JButton("Set");
		setWebFolderTextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				setWebFolders(webFolderText.getText());
				webFolderText.setText("");
			}
		});
		box.add(setWebFolderTextButton);
		webFolderText = new JTextArea();
		webFolderText.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent arg0) {
				updateControls();
			}
		});
		webFolderText.setBorder(BorderFactory.createLoweredBevelBorder());
		ComponentTools.lockComponentSize(webFolderText,200,25);
		box.add(webFolderText);
		box.add(Box.createHorizontalGlue());
		add(box,"South");
		
		JMenuBar menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		saveMenuItem = new JMenuItem("Save");
		saveMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				doSave();
			}
		});
		fileMenu.add(saveMenuItem);
		exportWebMenuItem = new JMenuItem("Export Web...");
		exportWebMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				doWebExport();
			}
		});
		fileMenu.add(exportWebMenuItem);
		fileMenu.add(new JSeparator());
		exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				setVisible(false);
				dispose();
				System.exit(0);
			}
		});
		fileMenu.add(exitMenuItem);
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);
		
		updateControls();
	}
	
	private void setWebFolders(String text) {
		int[] row = layoutTable.getSelectedRows();
		for (int i=0;i<row.length;i++) {
			RscharLayout rl = layoutRecords.get(row[i]);
			rl.setWebFolder(text);
		}
		layoutTable.clearSelection();
		Collections.sort(layoutRecords);
		layoutTable.repaint();
	}
	private void updateControls() {
		boolean sel = layoutTable.getSelectedRowCount()>0;
		setWebFolderTextButton.setEnabled(sel && webFolderText.getText().trim().length()>0);
	}
	public void doSave() {
		webLayoutFile = new File(characterFolder.getAbsolutePath()+File.separator+WEB_LAYOUT_FILE);
		
		Element layout = new Element("webLayout");
		for (RscharLayout rl:layoutRecords) {
			layout.addContent(rl.getElement());
		}
		
		// Save file
		try {
			FileOutputStream stream = new FileOutputStream(webLayoutFile);
			XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
			outputter.output(layout,stream);
			stream.close();
		}
		catch(FileNotFoundException ex) {
			ex.printStackTrace();
		}
		catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	private void repaintTable() {
		layoutTable.paint(layoutTable.getGraphics());
	}
	public void doWebExport() {
		File dir = fileManager.getSaveDirectory("Web Export");
		if (dir!=null) {
			if (!dir.exists()) {
				dir.mkdir();
			}
			File imagesDir = new File(dir.getAbsolutePath()+File.separator+"images");
			if (!imagesDir.exists()) {
				imagesDir.mkdir();
			}
			for (RscharLayout rec:layoutRecords) {
				rec.clearStatus();
			}
			repaintTable();
			
			Main main = new Main();
			main.create(dir);
			
			// Create all the folders
			Menu menu = new Menu();
			Hashtable<String,File> folders = new Hashtable<String,File>();
			for (RscharLayout rec:layoutRecords) {
				String folder = rec.getWebFolder();
				File webFolder;
				if (!folders.containsKey(folder)) {
					webFolder = new File(imagesDir.getAbsolutePath()+File.separator+folder);
					if (!webFolder.exists()) {
						if (!webFolder.mkdir()) {
							System.out.println("Unable to create: "+webFolder.getAbsolutePath());
							continue;
						}
					}
					folders.put(folder,webFolder);
				}
				else {
					webFolder = folders.get(folder);
				}
				Page page = new Page(rec);
				page.create(webFolder);
				rec.setStatus("Exported");
				menu.add(page);
				
				repaintTable();
			}
			menu.create(new File(dir.getAbsolutePath()+File.separator+"menu.htm"));
		}
	}
	public boolean loadFromFile(File file) {
		try {
			InputStream stream = new FileInputStream(file);
			return loadFromStream(stream);
		}
		catch(FileNotFoundException ex) {
			System.out.println("Problem loading file: "+ex);
		}
		return false;
	}
	public boolean loadFromStream(InputStream stream) {
		try {
			// Load file
			Document doc = new SAXBuilder().build(stream);
			
			// Read layout
			Element layout = doc.getRootElement();
			List list = layout.getChildren();
			for (Iterator i=list.iterator();i.hasNext();) {
				Element rschar = (Element)i.next();
				RscharLayout rl = new RscharLayout(characterFolder,rschar);
				layoutRecords.add(rl);
				if (rscharFiles.contains(rl.getFile())) {
					rscharFiles.remove(rl.getFile());
				}
			}
			return true;
		}
		catch(Exception ex) {
			JOptionPane.showMessageDialog(null,"Invalid file/format:\n\n"+ex,"Error",JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
		return false;
	}
	private void buildLayoutRecords() {
		for (File file:rscharFiles) {
			RscharLayout rl = new RscharLayout(file);
			layoutRecords.add(rl);
		}
		rscharFiles.clear();
		Collections.sort(layoutRecords);
	}
	private void fetchRscharFiles() {
		rscharFiles = new ArrayList<File>();
		File[] files = characterFolder.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if (file.isFile()) {
					String path = file.getAbsolutePath().toLowerCase();
					return path.endsWith(".rschar") || file.getAbsolutePath().endsWith(WEB_LAYOUT_FILE);
				}
				return false;
			}
		});
		for (int i=0;i<files.length;i++) {
			String path = files[i].getAbsolutePath().toLowerCase();
			if (path.endsWith(".rschar")) {
				rscharFiles.add(files[i]);
			}
			else {
				webLayoutFile = files[i];
			}
		}
	}
	private static final String[] REQUIRED_ARGS = {"characterFolder"};
	public static void main(String[] args) {
		ComponentTools.setSystemLookAndFeel();
		ArgumentParser ap = new ArgumentParser(args,REQUIRED_ARGS);
		if (ap.hasErrors()) {
			System.out.println(ap.getErrorString());
			System.exit(0);
		}
		String characterFolder = ap.getValueForKey("characterFolder");
		File file = new File(characterFolder);
		if (!file.exists() || !file.isDirectory()) {
			System.out.println("Cannot find characterFolder: "+characterFolder);
			System.exit(0);
		}
		RealmCharacterWebFrame frame = new RealmCharacterWebFrame(file);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				System.exit(0);
			}
		});
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}