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

import javax.swing.*;
import javax.swing.event.*;

import com.robin.general.util.RandomNumber;

public class DieRollChooser extends JDialog {

	private ButtonGroup group;
	private IntegerField numberOfDiceField;
	private JRadioButton dice2DButton;
	private JRadioButton dice4DButton;
	private JRadioButton dice6DButton;
	private JRadioButton dice8DButton;
	private JRadioButton dice10DButton;
	private JRadioButton dice12DButton;
	private JRadioButton dice20DButton;
	private JRadioButton dice100DButton;
	private JRadioButton constantButton;
	private JRadioButton rangeButton;
	private IntegerField constantField;
	private IntegerField rangeFromField;
	private IntegerField rangeToField;
	
	private JButton okayButton;
	private JButton cancelButton;
	
	private String chosenString = null;

	public DieRollChooser(String title,String message) {
		setSize(350,300);
		setTitle(title);
		getContentPane().setLayout(new BorderLayout());
		
		Box line;
		Box column;
		
		Box mainBox = Box.createVerticalBox();
			line = Box.createHorizontalBox();
			line.add(new JLabel(message));
			line.add(Box.createHorizontalGlue());
		mainBox.add(line);
		mainBox.add(Box.createVerticalStrut(10));
			line = Box.createHorizontalBox();
			line.add(new JLabel("# of Dice:"));
			line.add(Box.createHorizontalStrut(5));
				numberOfDiceField = new IntegerField(1);
				numberOfDiceField.addCaretListener(new CaretListener() {
					public void caretUpdate(CaretEvent ev) {
						updateInterface();
					}
				});
				ComponentTools.lockComponentSize(numberOfDiceField,80,25);
			line.add(numberOfDiceField);
			line.add(Box.createHorizontalGlue());
		mainBox.add(line);
		mainBox.add(Box.createVerticalStrut(10));
		
		Box columns = Box.createHorizontalBox();

		group = new ButtonGroup();
		column = Box.createVerticalBox();
			line = Box.createHorizontalBox();
				dice2DButton = new JRadioButton("2-Sided Die");
				dice2DButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						updateInterface();
					}
				});
				group.add(dice2DButton);
			line.add(dice2DButton);
			line.add(Box.createHorizontalGlue());
		column.add(line);
			line = Box.createHorizontalBox();
				dice4DButton = new JRadioButton("4-Sided Die");
				dice4DButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						updateInterface();
					}
				});
				group.add(dice4DButton);
			line.add(dice4DButton);
			line.add(Box.createHorizontalGlue());
		column.add(line);
			line = Box.createHorizontalBox();
				dice6DButton = new JRadioButton("6-Sided Die",true);
				dice6DButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						updateInterface();
					}
				});
				group.add(dice6DButton);
			line.add(dice6DButton);
			line.add(Box.createHorizontalGlue());
		column.add(line);
			line = Box.createHorizontalBox();
				dice8DButton = new JRadioButton("8-Sided Die");
				dice8DButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						updateInterface();
					}
				});
				group.add(dice8DButton);
			line.add(dice8DButton);
			line.add(Box.createHorizontalGlue());
		column.add(line);
		
		columns.add(column);
		
		column = Box.createVerticalBox();
			line = Box.createHorizontalBox();
				dice10DButton = new JRadioButton("10-Sided Die");
				dice10DButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						updateInterface();
					}
				});
				group.add(dice10DButton);
			line.add(dice10DButton);
			line.add(Box.createHorizontalGlue());
		column.add(line);
			line = Box.createHorizontalBox();
				dice12DButton = new JRadioButton("12-Sided Die");
				dice12DButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						updateInterface();
					}
				});
				group.add(dice12DButton);
			line.add(dice12DButton);
			line.add(Box.createHorizontalGlue());
		column.add(line);
			line = Box.createHorizontalBox();
				dice20DButton = new JRadioButton("20-Sided Die");
				dice20DButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						updateInterface();
					}
				});
				group.add(dice20DButton);
			line.add(dice20DButton);
			line.add(Box.createHorizontalGlue());
		column.add(line);
			line = Box.createHorizontalBox();
				dice100DButton = new JRadioButton("100-Sided Die");
				dice100DButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						updateInterface();
					}
				});
				group.add(dice100DButton);
			line.add(dice100DButton);
			line.add(Box.createHorizontalGlue());
		column.add(line);
		
		columns.add(column);
		
		mainBox.add(columns);
			line = Box.createHorizontalBox();
				rangeButton = new JRadioButton("Range:");
				rangeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						updateInterface();
					}
				});
				group.add(rangeButton);
			line.add(rangeButton);
			line.add(Box.createHorizontalStrut(5));
				rangeFromField = new IntegerField();
				rangeFromField.addCaretListener(new CaretListener() {
					public void caretUpdate(CaretEvent ev) {
						updateInterface();
					}
				});
				ComponentTools.lockComponentSize(rangeFromField,80,25);
			line.add(rangeFromField);
			line.add(Box.createHorizontalStrut(5));
			line.add(new JLabel("to"));
			line.add(Box.createHorizontalStrut(5));
				rangeToField = new IntegerField();
				rangeToField.addCaretListener(new CaretListener() {
					public void caretUpdate(CaretEvent ev) {
						updateInterface();
					}
				});
				ComponentTools.lockComponentSize(rangeToField,80,25);
			line.add(rangeToField);
			line.add(Box.createHorizontalGlue());
			
		mainBox.add(line);
			line = Box.createHorizontalBox();
				constantButton = new JRadioButton("Constant:");
				constantButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						updateInterface();
					}
				});
				group.add(constantButton);
			line.add(constantButton);
			line.add(Box.createHorizontalStrut(5));
				constantField = new IntegerField();
				constantField.addCaretListener(new CaretListener() {
					public void caretUpdate(CaretEvent ev) {
						updateInterface();
					}
				});
				ComponentTools.lockComponentSize(constantField,80,25);
			line.add(constantField);
			line.add(Box.createHorizontalGlue());
		mainBox.add(line);
		mainBox.add(Box.createVerticalGlue());
		
		getContentPane().add(mainBox,"Center");
		
			line = Box.createHorizontalBox();
			line.add(Box.createHorizontalGlue());
				cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						chosenString = null;
						setVisible(false);
					}
				});
			line.add(cancelButton);
			line.add(Box.createHorizontalGlue());
				okayButton = new JButton("Okay");
				okayButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						chosenString = createDieRollString();
						setVisible(false);
					}
				});
			line.add(okayButton);
			line.add(Box.createHorizontalGlue());
		getContentPane().add(line,"South");
		
		getRootPane().setDefaultButton(okayButton);
		
		setModal(true);
		
		updateInterface();
	}
	private String createDieRollString() {
		StringBuffer sb = new StringBuffer();
		
		if (constantButton.isSelected()) {
			// Entered a constant number
			sb.append(constantField.getInt());
		}
		else if (rangeButton.isSelected()) {
			// Entered a range
			sb.append(rangeFromField.getInt());
			sb.append("-");
			sb.append(rangeToField.getInt());
		}
		else {
			// Entered a die roll
			
			sb.append(numberOfDiceField.getInt());
			sb.append("D");
			
			if (dice2DButton.isSelected()) {
				sb.append("2");
			}
			else if (dice4DButton.isSelected()) {
				sb.append("4");
			}
			else if (dice6DButton.isSelected()) {
				sb.append("6");
			}
			else if (dice8DButton.isSelected()) {
				sb.append("8");
			}
			else if (dice10DButton.isSelected()) {
				sb.append("10");
			}
			else if (dice12DButton.isSelected()) {
				sb.append("12");
			}
			else if (dice20DButton.isSelected()) {
				sb.append("20");
			}
			else if (dice100DButton.isSelected()) {
				sb.append("100");
			}
		}
		
		return sb.toString();
	}
	private void updateInterface() {
		numberOfDiceField.setEnabled(!constantButton.isSelected());
		rangeFromField.setEnabled(rangeButton.isSelected());
		rangeToField.setEnabled(rangeButton.isSelected());
		constantField.setEnabled(constantButton.isSelected());
System.out.println(validEntry());
		okayButton.setEnabled(validEntry());
	}
	private boolean validEntry() {
		if (constantButton.isSelected()) {
			if (constantField.getInt()>0) {
				return true;
			}
		}
		else if (rangeButton.isSelected()) {
			if (rangeFromField.getInt()>=0 && rangeToField.getInt()>rangeFromField.getInt()) {
				return true;
			}
		}
		else if (numberOfDiceField.getInt()>0) {
			return true;
		}
		return false;
	}
	public String getDieRollString() {
		return chosenString;
	}
	public boolean madeChoice() {
		return chosenString!=null;
	}
	public void setDieRollString(String dieRollString) {
		if (dieRollString.indexOf("D")>=0) {
			// Die roll(s)
			int[] n = splitInts(dieRollString,"D");
			switch(n[1]) {
				case 2:		dice2DButton.setSelected(true); break;
				case 4:		dice4DButton.setSelected(true); break;
				case 6:		dice6DButton.setSelected(true); break;
				case 8:		dice8DButton.setSelected(true); break;
				case 10:	dice10DButton.setSelected(true); break;
				case 12:	dice12DButton.setSelected(true); break;
				case 20:	dice20DButton.setSelected(true); break;
				case 100:	dice100DButton.setSelected(true); break;
			}
			numberOfDiceField.setText(String.valueOf(n[0]));
		}
		else if (dieRollString.indexOf("-")>=0) {
			// Range
			int[] n = splitInts(dieRollString,"-");
			rangeButton.setSelected(true);
			rangeFromField.setText(String.valueOf(n[0]));
			rangeToField.setText(String.valueOf(n[1]));
		}
		else {
			// Constant
			constantButton.setSelected(true);
			constantField.setText(dieRollString);
		}
	}
	/**
	 * There is no error checking in this method, but if the String is a result from this chooser, it should work.
	 */
	public static int getRoll(String dieRollString) {
		int ret = 0;
		if (dieRollString.indexOf("D")>=0) {
			// Die roll(s)
			int[] n = splitInts(dieRollString,"D");
			ret = 0;
			for (int i=0;i<n[0];i++) {
				ret += RandomNumber.getDieRoll(n[1]);
			}
		}
		else if (dieRollString.indexOf("-")>=0) {
			// Range
			int[] n = splitInts(dieRollString,"-");
			int range = n[1]-n[0]+1;
			ret = RandomNumber.getRandom(range) + n[0];
		}
		else {
			// Constant
			ret = Integer.valueOf(dieRollString).intValue();
		}
		return ret;
	}
	private static int[] splitInts(String val,String delim) {
		int index = val.indexOf(delim);
		String s1 = val.substring(0,index);
		String s2 = val.substring(index+1);
		
		int[] n = new int[2];
		
		n[0] = Integer.valueOf(s1).intValue();
		n[1] = Integer.valueOf(s2).intValue();
		
		return n;
	}
	public static void main(String[]args) {
		DieRollChooser chooser = new DieRollChooser("hey","Choose one");
		chooser.setVisible(true);
		
		if (chooser.madeChoice()) {
			String drs = chooser.getDieRollString();
			System.out.print(drs+":  ");
			for (int i=0;i<20;i++) {
				System.out.print(getRoll(drs)+" ");
			}
			System.out.println();
		}
		else {
			System.out.println("Cancelled");
		}
		System.exit(0);
	}
}