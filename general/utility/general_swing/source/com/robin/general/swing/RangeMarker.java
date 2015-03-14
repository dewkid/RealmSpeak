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

import com.robin.general.graphics.GraphicsUtil;

public class RangeMarker extends JComponent {

	private static final Color foreground = Color.black;
	private static final Color background = Color.white;
	
	private Color currentBackground;
	

	private Font font;
	private int boxSize;
	private int min;
	private int max;
	private int range;
	private int current;
	
	private boolean hideValue = false;
	
	public RangeMarker(Color color,Font font,int boxSize,int min,int max) {
		this.currentBackground = color;
		this.font = font;
		this.boxSize = boxSize;
		this.min = min;
		this.max = max;
		this.range = max-min+1;
		this.current = min;
		updateSize();
	}
	public void setHideValue(boolean val) {
		hideValue = val;
		repaint();
	}
	public void setValue(int val) {
		current = val;
		repaint();
	}
	public int getValue() {
		return current;
	}
	public void advance() {
		current++;
		if (current>max) {
			current = min;
		}
		hideValue = false; // never hide after an advance
		repaint();
	}
	private void updateSize() {
		int boxes = max-min+1;
		int w = boxSize*boxes;
		int h = boxSize;
		
		Dimension size = new Dimension(w,h);
		setMaximumSize(size);
		setMinimumSize(size);
		setPreferredSize(size);
	}
	public void paintComponent(Graphics g) {
		int boxes = max-min+1;
		int w = boxSize*boxes;
		int h = boxSize;
		g.setColor(background);
		g.fillRect(0,0,w,h);
		
		if (!hideValue) {
			g.setColor(currentBackground);
			g.fillRect((current-min)*boxSize,0,h-1,h-1);
		}
		
		g.setFont(font);
		g.setColor(foreground);
		for (int i=0;i<range;i++) {
			int x = i*boxSize;
			g.drawRect(x,0,h-1,h-1);
			GraphicsUtil.drawCenteredString(g,x,0,h-1,h-1,""+(i+min));
		}
	}
	/**
	 * For testing only
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("RangeMarker demo");
		frame.getContentPane().setLayout(new BorderLayout());
			final RangeMarker rm = new RangeMarker(new Color(0,0,255),new Font("Ariel",Font.BOLD,10),20,1,14);
			rm.setValue(14);
			rm.setHideValue(true);
			final JButton button = new JButton("advance");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					rm.advance();
				}
			});
//			rm.setBorder(BorderFactory.createRaisedBevelBorder());
//			rm.setBorder(BorderFactory.createLineBorder(Color.black));
//			rm.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent ev) {
//				}
//			});
		frame.getContentPane().add(rm,"North");
		frame.getContentPane().add(button,"South");
		frame.setSize(400,400);
		frame.setVisible(true);
	}
}