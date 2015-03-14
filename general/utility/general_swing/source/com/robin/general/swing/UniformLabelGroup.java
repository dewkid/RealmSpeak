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
import javax.swing.*;
import java.util.*;

public class UniformLabelGroup {

	private Font labelFont = null;
	private Vector labelGroup = null;
	private int maxPixelWidth = 0;
	private int borderWidth = 0;
	private String longest; // only really useful for debugging purposes

	public UniformLabelGroup() {
		this(5);
	}
	
	public UniformLabelGroup(int border) {
		borderWidth = border;
	}
	
	public void setLabelFont(Font font) {
		labelFont = font;
	}
	
	public int labelCount() {
		return labelGroup.size();
	}
	
	public String longestLabel() {
		return longest;
	}
	
	public void add(JLabel jLabel) {
		if (labelGroup==null) {
			labelGroup = new Vector();
		}
		labelGroup.addElement(jLabel);
		
		FontMetrics metrics = jLabel.getFontMetrics(jLabel.getFont());
		int stringWidth = metrics.stringWidth(jLabel.getText());
		if (stringWidth>maxPixelWidth) {
			setMaxPixelWidth(stringWidth);
			longest = jLabel.getText();
		}
		updateLabels();
	}
	
	private void updateLabels() {
		if (labelGroup!=null) {
			Dimension d = new Dimension(maxPixelWidth+borderWidth,20);
			for (int i=0;i<labelGroup.size();i++) {
				JLabel jLabel = (JLabel)labelGroup.elementAt(i);
				jLabel.setMinimumSize(d);
				jLabel.setMaximumSize(d);
				jLabel.setPreferredSize(d);
			}
		}
	}
	
	public void setMaxPixelWidth(int val) {
		maxPixelWidth = val;
	}
	public int getMaxPixelWidth() {
		return maxPixelWidth;
	}
	
	public JLabel createLabel(String label) {
		JLabel jLabel = new JLabel(label,JLabel.RIGHT);
		if (labelFont!=null) jLabel.setFont(labelFont);
		add(jLabel);
		return jLabel;
	}
	
	public Box createLabelLine(String label) {
		return createLabelLine(label,null);
	}
	public Box createLabelLine(String label,Font font) {
		Box line=Box.createHorizontalBox();
		//Box labelBox = Box.createVerticalBox();
		JLabel jLabel = createLabel(label+":");
		if (font!=null) {
			jLabel.setFont(font);
		}
		line.add(jLabel);
		//labelBox.add(jLabel);
		//labelBox.add(Box.createVerticalGlue());
		//line.add(labelBox);
		line.add(Box.createHorizontalStrut(borderWidth));
		return line;
	}
	public Box createLine() {
		Box line=Box.createHorizontalBox();
		line.add(createLabel(" "));
		line.add(Box.createHorizontalStrut(borderWidth));
		return line;
	}
}