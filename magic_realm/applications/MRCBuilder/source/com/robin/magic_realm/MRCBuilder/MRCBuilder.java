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
package com.robin.magic_realm.MRCBuilder;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Vector;

import javax.swing.*;

import Acme.JPM.Encoders.GifEncoder;

import com.robin.general.swing.UniformLabelGroup;

public class MRCBuilder extends JFrame implements ActionListener {

	public MRCBuilder() {
		newCharacter = true;
		changedSinceLastSave = false;
		lastSaveFile = null;
		mShown = false;
	}

	public static void standardComponentSize(JComponent jcomponent) {
		standardComponentSize(jcomponent, 350, 25);
	}

	public static void standardComponentSize(JComponent jcomponent, int i, int j) {
		Dimension dimension = new Dimension(i, j);
		jcomponent.setMinimumSize(dimension);
		jcomponent.setMaximumSize(dimension);
		jcomponent.setPreferredSize(dimension);
	}

	public void initComponents() {
		jMenuBar = new JMenuBar();
		jMenuFile = new JMenu("File");
		jMenuFileNew = new JMenuItem("New Character");
		jMenuFileOpen = new JMenuItem("Open Character");
		jMenuFileSave = new JMenuItem("Save Character");
		jMenuFileSaveAs = new JMenuItem("Save Character As...");
		jMenuFileView = new JMenuItem("Preview Character Sheet");
		jMenuFileImport = new JMenuItem("Import Excel");
		jMenuFileExport = new JMenuItem("Export Gif");
		jMenuFileExit = new JMenuItem("Exit");
		jMenuBar.add(jMenuFile);
		jMenuFile.add(jMenuFileNew);
		jMenuFile.add(new JSeparator());
		jMenuFile.add(jMenuFileOpen);
		jMenuFile.add(jMenuFileSave);
		jMenuFile.add(jMenuFileSaveAs);
		jMenuFile.add(new JSeparator());
		jMenuFile.add(jMenuFileView);
		jMenuFile.add(jMenuFileImport);
		jMenuFile.add(jMenuFileExport);
		jMenuFile.add(new JSeparator());
		jMenuFile.add(jMenuFileExit);
		setJMenuBar(jMenuBar);
		jMenuFile.setMnemonic(70);
		jMenuFileOpen.setAccelerator(KeyStroke.getKeyStroke(79, 2));
		jMenuFileOpen.setMnemonic(79);
		jMenuFileSave.setAccelerator(KeyStroke.getKeyStroke(83, 2));
		jMenuFileSave.setMnemonic(83);
		jMenuFileExit.setMnemonic(88);
		jMenuFileNew.addActionListener(this);
		jMenuFileOpen.addActionListener(this);
		jMenuFileSave.addActionListener(this);
		jMenuFileSaveAs.addActionListener(this);
		jMenuFileView.addActionListener(this);
		jMenuFileImport.addActionListener(this);
		jMenuFileExport.addActionListener(this);
		jMenuFileExit.addActionListener(this);
		creatorField = new JTextField();
		nameField = new JTextField();
		symbolPathField = new JTextField();
		reloadSymbol = new JButton("Reload");
		reloadSymbol.addActionListener(this);
		symbolMeaningField = new JTextField();
		weightVulnerability = new JComboBox(vulNames);
		specialAdvantagesList = new OutlineList();
		optionalAdvantagesList = new OutlineList();
		startingInfoField = new JTextField();
		nativeFriendlinessTable = new NativeFriendlinessTable(natNames);
		devName = new JTextField[4];
		devAdd = new JTextField[4];
		for (int i = 0; i < 4; i++) {
			devName[i] = new JTextField();
			standardComponentSize(devName[i]);
			devAdd[i] = new JTextField();
			standardComponentSize(devAdd[i]);
		}

		chitButton = new ChitPickerButton[12];
		for (int j = 0; j < 12; j++)
			chitButton[j] = new ChitPickerButton();

		JTabbedPane jtabbedpane = new JTabbedPane();
		JPanel jpanel = new JPanel(new BorderLayout());
		JPanel jpanel1 = new JPanel(new BorderLayout());
		JPanel jpanel2 = new JPanel(new BorderLayout());
		JPanel jpanel3 = new JPanel(new BorderLayout());
		UniformLabelGroup uniformlabelgroup = new UniformLabelGroup();
		Box box = Box.createVerticalBox();
		JLabel jlabel = new JLabel("Character Attributes", 0);
		jlabel.setOpaque(true);
		jlabel.setBackground(new Color(180, 250, 100));
		Box box1 = uniformlabelgroup.createLabelLine("Creator");
		standardComponentSize(creatorField);
		box1.add(creatorField);
		box1.add(Box.createHorizontalGlue());
		box.add(box1);
		box1 = uniformlabelgroup.createLabelLine("Name");
		standardComponentSize(nameField);
		box1.add(nameField);
		box1.add(Box.createHorizontalGlue());
		box.add(box1);
		box1 = uniformlabelgroup.createLabelLine("Symbol Path");
		standardComponentSize(symbolPathField);
		box1.add(symbolPathField);
		box1.add(reloadSymbol);
		box1.add(Box.createHorizontalGlue());
		box.add(box1);
		box1 = uniformlabelgroup.createLabelLine("Symbol Meaning");
		standardComponentSize(symbolMeaningField);
		box1.add(symbolMeaningField);
		box1.add(Box.createHorizontalGlue());
		box.add(box1);
		box1 = uniformlabelgroup.createLabelLine("Weight/Vulnerability");
		standardComponentSize(weightVulnerability);
		box1.add(weightVulnerability);
		box1.add(Box.createHorizontalGlue());
		box.add(box1);
		Box box2 = Box.createHorizontalBox();
		box2.add(box);
		box2.add(Box.createHorizontalGlue());
		symbol = new JLabel();
		box2.add(symbol);
		box2.add(Box.createHorizontalGlue());
		box = Box.createVerticalBox();
		box.add(box2);
		box1 = uniformlabelgroup.createLabelLine("* Starting Info");
		standardComponentSize(startingInfoField);
		box1.add(startingInfoField);
		box1.add(Box.createHorizontalGlue());
		box.add(box1);
		box.add(Box.createVerticalGlue());
		jpanel.add(box, "Center");
		box = Box.createVerticalBox();
		box1 = uniformlabelgroup.createLabelLine("* Special Advantages");
		standardComponentSize(specialAdvantagesList, 550, 80);
		box1.add(specialAdvantagesList);
		box1.add(Box.createHorizontalGlue());
		box.add(box1);
		box1 = uniformlabelgroup.createLabelLine("* Optional Advantages");
		standardComponentSize(optionalAdvantagesList, 550, 80);
		box1.add(optionalAdvantagesList);
		box1.add(Box.createHorizontalGlue());
		box.add(box1);
		box.add(Box.createVerticalGlue());
		jpanel1.add(box, "Center");
		box = Box.createVerticalBox();
		for (int k = 0; k < 4; k++) {
			if (k > 0)
				box.add(new JSeparator());
			Box box3 = Box.createVerticalBox();
			Box box4 = Box.createHorizontalBox();
			box1 = uniformlabelgroup.createLabelLine("Dev Stage " + k + " Name");
			box1.add(devName[k]);
			box3.add(box1);
			box1 = uniformlabelgroup.createLabelLine("* Starting Eq");
			box1.add(devAdd[k]);
			box3.add(box1);
			box4.add(box3);
			box4.add(Box.createHorizontalGlue());
			int l = k * 3;
			box4.add(chitButton[l]);
			box4.add(chitButton[l + 1]);
			box4.add(chitButton[l + 2]);
			box4.add(Box.createHorizontalGlue());
			box.add(box4);
		}

		box.add(Box.createVerticalGlue());
		jpanel3.add(box, "Center");
		box = Box.createVerticalBox();
		box1 = uniformlabelgroup.createLabelLine("Native friendliness");
		box1.add(new JScrollPane(nativeFriendlinessTable));
		box.add(box1);
		box.add(Box.createVerticalGlue());
		jpanel2.add(box, "Center");
		box2 = Box.createHorizontalBox();
		box2.add(Box.createHorizontalGlue());
		box2.add(new JLabel("* = can use bold/italic formatting", 2));
		jtabbedpane.addTab("General", jpanel);
		jtabbedpane.addTab("Advantages", jpanel1);
		jtabbedpane.addTab("Development", jpanel3);
		jtabbedpane.addTab("Politics", jpanel2);
		setLocation(new Point(0, 0));
		setTitle("MRC Builder");
		getContentPane().setLayout(new BorderLayout(5, 5));
		setSize(new Dimension(700, 350));
		getContentPane().add(jlabel, "North");
		getContentPane().add(jtabbedpane, "Center");
		getContentPane().add(box2, "South");
		updateControls();
		setResizable(false);
		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent windowevent) {
				thisWindowClosing(windowevent);
			}

			public void windowActivated(WindowEvent windowevent) {
				repaint();
			}

		});
	}

	public void updateControls() {
	}

	public void addNotify() {
		super.addNotify();
		if (mShown)
			return;
		JMenuBar jmenubar = getJMenuBar();
		if (jmenubar != null) {
			int i = jmenubar.getPreferredSize().height;
			Dimension dimension = getSize();
			dimension.height += i;
			setSize(dimension);
		}
		mShown = true;
	}

	void thisWindowClosing(WindowEvent windowevent) {
		cleanExit(0);
	}

	public void cleanExit(int i) {
		System.exit(i);
	}

	public String makeCommaDelimitedString(Vector vector) {
		if (vector != null) {
			StringBuffer stringbuffer = new StringBuffer();
			for (int i = 0; i < vector.size(); i++) {
				if (i > 0)
					stringbuffer.append(", ");
				stringbuffer.append((String) vector.elementAt(i));
			}

			return stringbuffer.toString();
		}
		else {
			return "";
		}
	}

	public int addAdvantagesToImage(Graphics g, String s, Vector vector, int i) {
		Font font = new Font("Serif", 1, 12);
		Font font1 = new Font("Serif", 3, 12);
		int j = vector.size();
		if (j > 0) {
			g.setFont(font1);
			g.drawString(s, 15, i);
			i += 15;
			for (int k = 0; k < j; k++) {
				OutlineEntry outlineentry = (OutlineEntry) vector.elementAt(k);
				g.setFont(font);
				String s1 = (new Integer(k + 1)).toString() + ".)";
				g.drawString(s1, 15, i);
				int l = g.getFontMetrics().stringWidth(s1) + 10;
				String s2 = "<b>" + outlineentry.getHeader().toUpperCase() + ":</b>  " + outlineentry.getContent();
				MultiFormatString multiformatstring = new MultiFormatString(s2);
				multiformatstring.setFontSize(12);
				i += multiformatstring.draw(g, 15 + l, i, getCardPrintableWidth() - l);
			}

		}
		return i;
	}

	public int addPoliticsToImage(Graphics g, String s, String s1, int i) {
		if (s1 != null && s1.length() > 0) {
			g.setColor(Color.black);
			MultiFormatString multiformatstring = new MultiFormatString("<b>" + s + ": </b>" + s1);
			multiformatstring.setFontAttributes("Serif", 14);
			i += multiformatstring.draw(g, 15, i, getCardPrintableWidth());
		}
		return i;
	}

	public ImageIcon createImage(MRCharacter mrcharacter) {
		Image image = createImage(388, 607);
		Graphics g = image.getGraphics();
		Font font = new Font("Serif", 1, 24);
		Font font1 = new Font("Serif", 1, 12);
//		Font font2 = new Font("Serif", 3, 12);
		Font font3 = new Font("Serif", 0, 10);
//		Font font4 = new Font("Serif", 0, 14);
//		Font font5 = new Font("Serif", 1, 14);
		g.setColor(Color.white);
		g.fillRect(0, 0, 388, 607);
		int i = 35;
		g.setColor(Color.black);
		g.setFont(font);
		g.drawString(mrcharacter.getName(), 15, i);
		i += 15;
		int j = g.getFontMetrics().stringWidth(mrcharacter.getName());
		g.setFont(new Font("Serif", 2, 10));
		g.drawString("Creator: " + mrcharacter.getCreator(), 15 + j + 10, i - 20);
		g.setFont(font1);
		g.drawString("Meaning of Symbol:", 15, i);
		i += 15;
		g.drawString("\"" + mrcharacter.getSymbolMeaning() + "\"", 15, i);
		i += 15;
		loadSymbolIcon();
		g.drawImage(((ImageIcon) symbol.getIcon()).getImage(), 288, 0, null);
		g.drawString("WEIGHT/VULNERABILITY:", 15, i);
		g.drawString(mrcharacter.getVulnerability(), 185, i);
		i += 20;
		i = addAdvantagesToImage(g, "Special Advantages:", mrcharacter.getSpecialAdvantages(), i);
		i += 5;
		i = addAdvantagesToImage(g, "Optional Advantages:", mrcharacter.getOptionalAdvantages(), i);
		g.setColor(Chit.darkGreen);
		g.setFont(font3);
		g.drawString("Development:", 15, i);
		i += 15;
		for (int k = 0; k < 4; k++) {
			g.setFont(font1);
			g.drawString(mrcharacter.getDevName(k), 15, i);
			i += 15;
			String s = mrcharacter.getDevAdd(k);
			if (s != null) {
				MultiFormatString multiformatstring1 = new MultiFormatString(s);
				i += multiformatstring1.draw(g, 15, i, getCardPrintableWidth());
			}
			int l = 20;
			if (k > 0) {
				g.setFont(font3);
				g.drawString("add", l, i + 10);
				l += 40;
			}
			int i1 = k * 3;
			for (int j1 = 0; j1 < 3; j1++) {
				Chit.draw(g, l, i - 15, mrcharacter.getChit(i1 + j1).getTextLine(0), mrcharacter.getChit(i1 + j1).getTextLine(1));
				l += 58;
			}

			i += 40;
		}

		i = 507;
		g.setColor(Color.black);
		MultiFormatString multiformatstring = new MultiFormatString(mrcharacter.getStartingInfo());
		multiformatstring.setFontAttributes("Serif", 14);
		i += multiformatstring.draw(g, 15, i, getCardPrintableWidth());
		i += 5;
		multiformatstring = new MultiFormatString(mrcharacter.getDevAdd(3));
		multiformatstring.setFontAttributes("Serif", 14);
		i += multiformatstring.draw(g, 15, i, getCardPrintableWidth());
		i += 5;
		i = addPoliticsToImage(g, "ALLIES", mrcharacter.getAllied(), i);
		i = addPoliticsToImage(g, "FRIENDLY", mrcharacter.getFriendly(), i);
		i = addPoliticsToImage(g, "UNFRIENDLY", mrcharacter.getUnfriendly(), i);
		i = addPoliticsToImage(g, "ENEMY", mrcharacter.getEnemy(), i);
		return new ImageIcon(image);
	}

	public static int getCardPrintableWidth() {
		return 358;
	}

	public void showImage(Icon icon) {
		repaint();
		JFrame jframe = new JFrame();
		jframe.getContentPane().setLayout(new BorderLayout());
		jframe.setLocation(new Point(0, 0));
		jframe.setTitle("Character");
		jframe.setSize(new Dimension(400, 640));
		JLabel jlabel = new JLabel(icon);
		JPanel jpanel = new JPanel(new BorderLayout());
		jpanel.add(jlabel, "Center");
		jframe.getContentPane().add(jpanel, "Center");
		jframe.setVisible(true);
	}

	public boolean exportImage(File file, Image image) {
		try {
			System.out.println("writing file...");
			FileOutputStream fileoutputstream = new FileOutputStream(file);
			GifEncoder gifencoder = new GifEncoder(image, fileoutputstream);
			gifencoder.encode();
			fileoutputstream.close();
			System.out.println("done writing file.");
			return true;
		}
		catch (IOException _ex) {
			return false;
		}
	}

	public void clearAllFields() {
		MRCharacter mrcharacter = new MRCharacter();
		mrcharacter.setFields(this);
		repaint();
	}

	public static ImageIcon getIcon(JFrame jframe, String s) {
		java.net.URL url = jframe.getClass().getResource(s);
		if (url != null) {
			Image image = Toolkit.getDefaultToolkit().getImage(url);
			return new ImageIcon(image);
		}
		else {
			return new ImageIcon(s);
		}
	}

	public void loadSymbolIcon() {
		ImageIcon imageicon = null;
		String s = symbolPathField.getText().trim();
		if (s != null && s.length() > 0 && (new File(s)).exists())
			imageicon = getIcon(this, s);
		setSymbolGraphics(imageicon);
	}

	public void setSymbolGraphics(ImageIcon imageicon) {
		if (imageicon == null) {
			Image image = createImage(100, 100);
			Graphics g = image.getGraphics();
			g.setColor(Color.white);
			g.fillRect(0, 0, 100, 100);
			g.setColor(Color.black);
			g.drawRect(0, 0, 99, 99);
			imageicon = new ImageIcon(image);
		}
		symbol.setIcon(imageicon);
	}

	public void openFile() {
		if (lastSaveFile == null)
			lastSaveFile = new File("./chars");
		JFileChooser jfilechooser = new JFileChooser(lastSaveFile);
		jfilechooser.setDialogTitle("Open character file:");
		if (jfilechooser.showOpenDialog(this) == 0) {
			File file = jfilechooser.getSelectedFile();
			MRCharacter mrcharacter = new MRCharacter();
			mrcharacter.load(file);
			lastSaveFile = file;
			mrcharacter.setFields(this);
		}
		else {
			lastSaveFile = null;
		}
	}

	public void saveFile() {
		JFileChooser jfilechooser;
		if (lastSaveFile == null) {
			jfilechooser = new JFileChooser(new File("./chars"));
		}
		else {
			jfilechooser = new JFileChooser(lastSaveFile);
			jfilechooser.setSelectedFile(lastSaveFile);
		}
		jfilechooser.setDialogTitle("Save character file:");
		if (jfilechooser.showSaveDialog(this) == 0) {
			lastSaveFile = addTextExtension(jfilechooser.getSelectedFile());
			MRCharacter mrcharacter = new MRCharacter();
			mrcharacter.readFields(this);
			mrcharacter.save(lastSaveFile);
		}
	}

	public void saveAsFile() {
		lastSaveFile = null;
		saveFile();
	}

	public File addTextExtension(File file) {
		return addExtension(file, ".txt");
	}

	public File addGifExtension(File file) {
		return addExtension(file, "_final.gif");
	}

	public File addExtension(File file, String s) {
		String s1 = file.getPath().toLowerCase();
		int i = s1.indexOf(".");
		if (i >= 0)
			s1 = s1.substring(0, i);
		s1 = s1 + s;
		return new File(s1);
	}

	public void actionPerformed(ActionEvent actionevent) {
		Object obj = actionevent.getSource();
		if (obj == jMenuFileNew) {
			lastSaveFile = null;
			clearAllFields();
			setSymbolGraphics(null);
		}
		else if (obj == jMenuFileOpen) {
			openFile();
			loadSymbolIcon();
		}
		else if (obj == jMenuFileSave)
			saveFile();
		else if (obj == jMenuFileSaveAs)
			saveAsFile();
		else if (obj == jMenuFileView) {
			MRCharacter mrcharacter = new MRCharacter();
			mrcharacter.readFields(this);
			showImage(createImage(mrcharacter));
		}
		else if (obj == jMenuFileImport) {
			if (lastSaveFile == null)
				lastSaveFile = new File("./chars");
			JFileChooser jfilechooser = new JFileChooser(lastSaveFile);
			jfilechooser.setDialogTitle("Open tab-delimited excel file:");
			if (jfilechooser.showOpenDialog(this) == 0) {
				File file1 = jfilechooser.getSelectedFile();
				ExcelFormatParser excelformatparser = new ExcelFormatParser(file1);
				MRCharacter mrcharacter2 = excelformatparser.getCharacter();
				mrcharacter2.setFields(this);
			}
		}
		else if (obj == jMenuFileExport) {
			if (lastSaveFile != null) {
				File file = addGifExtension(lastSaveFile);
				if (JOptionPane.showConfirmDialog(this, "Save as " + file.getPath() + "?", "Export gif", 0) == 0) {
					MRCharacter mrcharacter1 = new MRCharacter();
					mrcharacter1.readFields(this);
					exportImage(addGifExtension(lastSaveFile), createImage(mrcharacter1).getImage());
				}
			}
			else {
				System.out.println("save your file first");
			}
		}
		else if (obj == jMenuFileExit)
			cleanExit(0);
		else if (obj == reloadSymbol)
			loadSymbolIcon();
		updateControls();
	}

	public static String matchVul(String s) {
		if (s != null) {
			for (int i = 1; i < vulNames.length; i++)
				if (vulNames[i].startsWith(s.toUpperCase()))
					return vulNames[i];

		}
		return "NEGLIGIBLE";
	}

	public static String matchNat(String s) {
		if (s != null) {
			for (int i = 0; i < natNames.length; i++)
				if (natNames[i].toUpperCase().indexOf(s.toUpperCase()) >= 0)
					return natNames[i];

		}
		return null;
	}

	public static void main(String args[]) {
		MRCBuilder mrcbuilder = new MRCBuilder();
		mrcbuilder.initComponents();
		mrcbuilder.setLocationRelativeTo(null);
		mrcbuilder.setVisible(true);
	}

	public static final String defaultDirectory = "./chars";
	public static final int CARD_WIDTH = 388;
	public static final int CARD_HEIGHT = 607;
	public static final int CARD_MARGIN = 15;
	public static final int CHIT_SPACE = 58;
	public static final int SYMBOL_SIZE = 100;
	public static final String vulNames[] = { "NEGLIGIBLE", "LIGHT", "MEDIUM", "HEAVY", "TREMENDOUS" };
	public static final String natNames[] = { "COMPANY", "BASHKARS", "WOODFOLK", "LANCERS", "PATROL", "GUARD", "ORDER", "SOLDIERS", "ROGUES", "<i>Crone</i>", "<i>Shaman</i>", "<i>Warlock</i>", "<i>Scholar</i>" };
	JMenuBar jMenuBar;
	JMenu jMenuFile;
	JMenuItem jMenuFileNew;
	JMenuItem jMenuFileOpen;
	JMenuItem jMenuFileSave;
	JMenuItem jMenuFileSaveAs;
	JMenuItem jMenuFileView;
	JMenuItem jMenuFileImport;
	JMenuItem jMenuFileExport;
	JMenuItem jMenuFileExit;
	JTextField creatorField;
	JTextField nameField;
	JTextField symbolPathField;
	JLabel symbol;
	JButton reloadSymbol;
	JTextField symbolMeaningField;
	JComboBox weightVulnerability;
	OutlineList specialAdvantagesList;
	OutlineList optionalAdvantagesList;
	JTextField devName[];
	JTextField devAdd[];
	ChitPickerButton chitButton[];
	JTextField startingInfoField;
	NativeFriendlinessTable nativeFriendlinessTable;
	boolean newCharacter;
	boolean changedSinceLastSave;
	File lastSaveFile;
	private boolean mShown;

}