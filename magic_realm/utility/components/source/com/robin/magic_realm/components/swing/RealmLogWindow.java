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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.swing.*;
import javax.swing.text.*;

import com.robin.general.io.FileUtilities;
import com.robin.magic_realm.components.utility.RealmLogging;

public class RealmLogWindow extends JFrame {
	public static final String INDENT_BLOCK = "    ";
	
	private static RealmLogWindow singleton = null;
	public static RealmLogWindow getSingleton() {
		if (singleton==null) {
			singleton = new RealmLogWindow();
		}
		return singleton;
	}
	public static void killSingleton() {
		if (singleton!=null) {
			singleton.setVisible(false);
			singleton.dispose();
			singleton = null;
		}
	}

	private JMenu fileMenu;
	private JMenuItem clearFileMenu;
	private JMenuItem saveLogFileMenu;
	private JMenuItem closeFileMenu;

	private JTextPane textPane;
	private StyledDocument doc;
	private ArrayList<String[]> list;
	
	private int indent = 0;

	private RealmLogWindow() {
		initComponents();
		list = new ArrayList<String[]>();
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String[] line:list) {
			sb.append(line[0]);
			sb.append(" - ");
			sb.append(line[1]);
			sb.append("\n");
		}
		return sb.toString();
	}
	private String getSaveFilePathFromFile(File rsGameFile) {
		String path = FileUtilities.getFilePathString(rsGameFile,true,false);
		return path+".rslog";
	}
	public ArrayList<String[]> getStringArrayList() {
		return list;
	}
	public boolean save(File rsGameFile) {
		String saveFilePath = getSaveFilePathFromFile(rsGameFile);
		try {
			FileOutputStream fileStream = new FileOutputStream(saveFilePath);
			DeflaterOutputStream deflater = new DeflaterOutputStream(fileStream);
			PrintStream stream = new PrintStream(deflater);
			ArrayList<String[]> safeList = new ArrayList<String[]>(list);
			stream.println(safeList.size());
			for (String[] line:safeList) {
				stream.println(line[0]);
				stream.println(line[1]);
			}
			stream.close();
			return true;
		}
		catch(IOException ex) {
			ex.printStackTrace();
			return false;
		}
	}
	public boolean load(File rsGameFile) {
		clearLog();
		String saveFilePath = getSaveFilePathFromFile(rsGameFile);
		try {
			FileInputStream fileStream = new FileInputStream(saveFilePath);
			InflaterInputStream inflater = new InflaterInputStream(fileStream);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inflater));
			int lines = Integer.valueOf(reader.readLine());
			String key;
			String message;
			for (int i=0;i<lines;i++) {
				key = reader.readLine();
				message = reader.readLine();
				addMessage(key,message);
			}
			reader.close();
			return true;
		}
		catch(Exception ex) {
			// Clear log on failed load!
			clearLog();
			return false;
		}
	}
	public String getHtmlString() {
		StringBuffer sb = new StringBuffer();
		for (String[] line:list) {
			sb.append(getHtml(line));
		}
		return sb.toString();
	}
	
	public void clearLog() {
		textPane.setText("");
		doc = textPane.getStyledDocument();
		list.clear();
	}
	
	private String getStyleName(String key) {
		if ("host".equals(key)) {
			return "bold";
		}
		else if (RealmLogging.BATTLE.equals(key)) {
			return "redbold";
		}
		return "blue";
	}
	private String getAliasName(String key) {
		if ("host".equals(key)) {
			return "RealmSpeak";
		}
		else if (RealmLogging.BATTLE.equals(key)) {
			return "RealmBattle";
		}
		return key;
	}

	private String getHtml(String[] line) {
		StringBuffer sb = new StringBuffer("\n<br>");
		String style = getStyleName(line[0]);
		String alias = getAliasName(line[0]);
		if ("bold".equals(style)) {
			sb.append("<b>");
			sb.append(alias);
			sb.append("</b>");
		}
		else if ("redbold".equals(style)) {
			sb.append("<font color=\"red\"><b>");
			sb.append(alias);
			sb.append("</b></font>");
		}
		else {
			sb.append("<font color=\"blue\">");
			sb.append(alias);
			sb.append("</font>");
		}
		sb.append(" - ");
		sb.append(line[1]);
		return sb.toString();
	}
	
	public void addMessage(String key, String message) {
		if (RealmLogging.LOG_INDENT.equals(key)) {
			if (RealmLogging.LOG_INDENT_CLEAR.equals(message)) {
				indent = 0;
			}
			else if (RealmLogging.LOG_INDENT_INCREMENT.equals(message)) {
				indent++;
			}
		}
		else {
			String[] line = new String[2];
			line[0] = key;
			line[1] = getIndent() + message;
			list.add(line);
			try {
					doc.insertString(doc.getLength(),getAliasName(key),doc.getStyle(getStyleName(key)));
					doc.insertString(doc.getLength(), " - " + line[1] + "\n", doc.getStyle("regular"));
		
					scrollToEnd();
			}
			catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}
	private String getIndent() {
		if (indent>0) {
			StringBuffer sb = new StringBuffer();
			for (int i=0;i<indent;i++) {
				sb.append(INDENT_BLOCK);
			}
			return sb.toString();
		}
		return "";
	}

	public void scrollToEnd() {
		// This makes it thread safe
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				_scrollToEnd();
			}
		});
	}

	private void _scrollToEnd() {
		textPane.scrollRectToVisible(new Rectangle(0, textPane.getHeight() - 2, 1, 1));
	}

	private void initComponents() {

		fileMenu = new JMenu("File");
		clearFileMenu = new JMenuItem("Clear");
		clearFileMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				clearLog();
			}
		});
		fileMenu.add(clearFileMenu);
		saveLogFileMenu = new JMenuItem("Save...");
		saveLogFileMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
			}
		});
		saveLogFileMenu.setEnabled(false);
		fileMenu.add(saveLogFileMenu);
		fileMenu.add(new JSeparator());
		closeFileMenu = new JMenuItem("Hide Log");
		closeFileMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				setVisible(false);
			}
		});
		fileMenu.add(closeFileMenu);
		JMenuBar bar = new JMenuBar();
		bar.add(fileMenu);
		setJMenuBar(bar);

		textPane = new JTextPane();
		textPane.setEditable(false);
		initStyles();
		setSize(500, 600);
		setLocationRelativeTo(null);
		setTitle("RealmSpeak Log");

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(new JScrollPane(textPane), "Center");
	}

	private void initStyles() {
		doc = textPane.getStyledDocument();
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		Style regular = doc.addStyle("regular", def);
		StyleConstants.setFontFamily(def, "SansSerif");

		Style s = doc.addStyle("blue", regular);
		StyleConstants.setForeground(s, Color.blue);

		s = doc.addStyle("bold", regular);
		StyleConstants.setBold(s, true);

		s = doc.addStyle("redbold", regular);
		StyleConstants.setForeground(s, Color.red);
		StyleConstants.setBold(s, true);
	}

	public static void main(String[] args) {
		final RealmLogWindow log = new RealmLogWindow();
		JButton button = new JButton("down");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				log.scrollToEnd();
			}
		});
		log.getContentPane().add(button, "South");
		log.setVisible(true);
		for (int i = 0; i < 100; i++) {
			log.addMessage("beef", "it's what's for dinner " + i);
		}
	}
}