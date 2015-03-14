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
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.robin.general.swing.AggressiveDialog;
import com.robin.general.swing.ComponentTools;
import com.robin.general.swing.IntegerField;
import com.robin.magic_realm.components.MagicRealmColor;

public class DemonResponseDialog extends AggressiveDialog {
	
	private static final Font BUTTON_FONT = new Font("Dialog",Font.BOLD,20);
	private static final Font LABEL_FONT = new Font("Dialog",Font.BOLD,12);
	
	private JTextArea questionArea;
	private JButton yesButton;
	private JButton noButton;
	private JButton numberButton;
	private IntegerField numberField;
	
	private String answer = null;
	
	public DemonResponseDialog(JFrame parent,String title,String question) {
		super(parent,title,true);
		initComponents(question);
	}
	public String getAnswer() {
		return answer;
	}
	private void initComponents(String question) {
		setSize(300,200);
		getContentPane().setLayout(new BorderLayout(5,5));
		
		JPanel topPanel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("You MUST answer the question TRUTHFULLY:");
		label.setOpaque(true);
		label.setFont(LABEL_FONT);
		label.setBackground(Color.orange);
		topPanel.add(label,"North");
		questionArea = new JTextArea(question);
		questionArea.setEditable(false);
		questionArea.setOpaque(false);
		questionArea.setFont(LABEL_FONT);
		questionArea.setForeground(Color.blue);
		questionArea.setLineWrap(true);
		questionArea.setWrapStyleWord(true);
		topPanel.add(questionArea,"Center");
		getContentPane().add(topPanel,"North");
		
		JPanel mainPanel = new JPanel(new GridLayout(2,1));
		
		JPanel yesNoPanel = new JPanel(new GridLayout(1,2));
		yesButton = new JButton("YES");
		yesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				answer = "YES";
				setVisible(false);
			}
		});
		yesButton.setFont(BUTTON_FONT);
		yesButton.setForeground(MagicRealmColor.FORESTGREEN);
		yesNoPanel.add(yesButton);
		
		noButton = new JButton("NO");
		noButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				answer = "NO";
				setVisible(false);
			}
		});
		noButton.setFont(BUTTON_FONT);
		noButton.setForeground(Color.red);
		yesNoPanel.add(noButton);
		
		mainPanel.add(yesNoPanel);
		
		numberField = new IntegerField();
		numberButton = new JButton("NUMBER");
		numberButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				int ret = JOptionPane.showConfirmDialog(DemonResponseDialog.this,numberField,"Enter a number",JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
				if (ret==JOptionPane.OK_OPTION) {
					if (numberField.getText().trim().length()>0) {
						answer=numberField.getText();
						setVisible(false);
					}
				}
			}
		});
		numberButton.setFont(BUTTON_FONT);
		mainPanel.add(numberButton);
		
		getContentPane().add(mainPanel,"Center");
	}
	public static void main(String[] args) {
		ComponentTools.setSystemLookAndFeel();
		DemonResponseDialog dialog = new DemonResponseDialog(new JFrame(),"Robin Asks Demon","Does your Sorceror character have the Flying Carpet?");
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		System.out.println(dialog.getAnswer());
		System.exit(0);
	}
}