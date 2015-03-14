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
package com.robin.magic_realm.components.utility;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.StringTokenizer;

import javax.swing.*;

import com.robin.general.swing.ComponentTools;

public class ChangesToHTML extends JFrame {
	
	private static final Font FONT = new Font("Monospaced",Font.PLAIN,12);
	private static final String LINK_PREFIX = "<a href=\"http://mantis.dewkid.com/current/view.php?id=";
	private static final String LINK_POSTFIX = "\">[";
	private static final String LINK_CLOSE = "]</a>";
	private static final String BUG_TAG_FRONT = "BUG ";
	private static final String BUG_TAG_TAIL = " - ";
	private static final String NOTE_TAG = "NOTE:";
	
	private static final String[] BULLET = {
		"# ", // level 1
		"- ", // level 2
		"> ", // level 3 (rare)
	};
	
	private JButton convertTextButton;
	private JButton finishButton;
	
	private JTextArea inputText;
	private JTextArea outputText;
	
	public ChangesToHTML() {
		initComponents();
		setLocationRelativeTo(null);
	}
	private void initComponents() {
		setTitle("Tool for making HTML from changes.txt");
		setSize(800,800);
		getContentPane().setLayout(new BorderLayout());
		JPanel mainPane = new JPanel(new GridLayout(2,1));
		getContentPane().add(mainPane,"Center");
		
		inputText = new JTextArea();
		inputText.setFont(FONT);
		mainPane.add(new JScrollPane(inputText));
		
		outputText = new JTextArea();
		outputText.setFont(FONT);
		outputText.setEditable(false);
		mainPane.add(new JScrollPane(outputText));
		
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		convertTextButton = new JButton("Convert");
		convertTextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				String converted = convertText(inputText.getText());
				outputText.setText(converted);
				outputText.setCaretPosition(0);
			}
		});
		box.add(convertTextButton);
		box.add(Box.createHorizontalGlue());
		finishButton = new JButton("Done");
		finishButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				System.exit(0);
			}
		});
		box.add(finishButton);
		getContentPane().add(box,"South");
	}
	private String convertText(String in) {
		StringBuffer sb = new StringBuffer();
		StringTokenizer lines = new StringTokenizer(in,"\n");
		int currentLevel = 0;
		while(lines.hasMoreTokens()) {
			String line = lines.nextToken().trim();
			String front = line.substring(0,2);
			int level = getBulletLevel(front);
			if (level==-1) { // NOT a bullet
				// Add ul closes
				for (int i=0;i<currentLevel;i++) {
					sb.append("</ul>");
				}
				currentLevel = 0;
				sb.append("\n");
				boolean note = line.startsWith(NOTE_TAG);
				if (note) {
					sb.append("<font color=\"red\">");
				}
				sb.append("<b>");
				sb.append(line);
				sb.append("</b>");
				if (note) {
					sb.append("</font>");
				}
			}
			else {
				closeListTags(sb,level,currentLevel);
				sb.append("<li>");
				sb.append(addLinks(line.substring(2)));
				currentLevel = level;
			}
		}
		closeListTags(sb,0,currentLevel);
		return sb.toString().substring(1); // get rid of first linefeed
	}
	private String addLinks(String in) {
		int front = in.indexOf(BUG_TAG_FRONT);
		if (front>=0) {
			int tail = in.indexOf(BUG_TAG_TAIL,front);
			if (tail>=0) {
				StringBuffer sb = new StringBuffer();
				sb.append(in.substring(0,front));
				
				// insert links here
				String numbers = in.substring(front+BUG_TAG_FRONT.length(),tail);
				StringTokenizer nums = new StringTokenizer(numbers,",");
				boolean first = true;
				while(nums.hasMoreTokens()) {
					String num = nums.nextToken();
					if (!first) {
						sb.append(",");
					}
					sb.append(LINK_PREFIX);
					sb.append(num);
					sb.append(LINK_POSTFIX);
					sb.append(num);
					sb.append(LINK_CLOSE);
					first = false;
				}
				
				sb.append(in.substring(tail));
				return sb.toString();
			}
		}
		return in;
	}
	private void closeListTags(StringBuffer sb,int level,int currentLevel) {
		int levelDiff = level-currentLevel;
		if (levelDiff>0) {
			sb.append("\n");
			for (int i=0;i<levelDiff;i++) {
				sb.append("<ul>");
			}
		}
		else {
			for (int i=0;i<(-levelDiff);i++) {
				sb.append("</ul>");
			}
			sb.append("\n");
		}
	}
	private int getBulletLevel(String front) {
		for (int i=0;i<BULLET.length;i++) {
			if (BULLET[i].equals(front)) {
				return i+1;
			}
		}
		return -1;
	}
//	private void _loadTestData() {
//		StringBuffer sb = new StringBuffer();
//		sb.append("ISSUES FIXED:\n");
//		sb.append("# Yada yada\n");
//		sb.append("- one\n");
//		sb.append("- two\n");
//		sb.append("- three\n");
//		sb.append("# BUG 221 - Yep\n");
//		inputText.setText(sb.toString());
//	}
	public static void main(String[] args) {
		ComponentTools.setSystemLookAndFeel();
		ChangesToHTML app = new ChangesToHTML();
//		app._loadTestData();
		app.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				System.exit(0);
			}
		});
		app.setVisible(true);
		
	}
}