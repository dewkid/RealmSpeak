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
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;

import com.robin.general.graphics.GraphicsUtil;

public class ColorPicker extends JComponent {

	private static final int ADJUST = 25;
	private static final int SIZE = 128;
	private static final int SPACE = 2;
	
	private static final int MODE_NONE			= 0;
	private static final int MODE_PICKER_FIELD	= 1;
	private static final int MODE_COLOR_ADJUST	= 2;

	private BufferedImage pickerField;			// This is the main color field
	private BufferedImage colorAdjustField;		// This is the bar on the right side that adjusts the mainColor
	
	private int mode = MODE_NONE;
	
	private Color mainColor = Color.blue;
	private int mainColorHeight;
	
	private Color pickColor = Color.blue;
	private Point pickColorPoint;

	public ColorPicker(Color pickColor) {
		Dimension d = new Dimension(SIZE+SPACE+ADJUST,SIZE+SPACE+ADJUST);
		setMaximumSize(d);
		setMinimumSize(d);
		setPreferredSize(d);
		updateColorAdjustField();
		
		setPickColor(pickColor);
		
		MouseInputAdapter mia = new MouseInputAdapter() {
			public void mousePressed(MouseEvent ev) {
				setMode(ev.getPoint());
				updatePicker(ev.getPoint());
			}
			public void mouseDragged(MouseEvent ev) {
				updatePicker(ev.getPoint());
			}
		};
		
		addMouseListener(mia);
		addMouseMotionListener(mia);
	}
	public boolean selectedColor(Point p) {
		return p.y>SIZE+SPACE;
	}
	public void setPickColor(Color val) {
		pickColor = val;
		initializeInterface();
		updatePickPoint();
	}
	public Color getPickColor() {
		return pickColor;
	}
	private void updateColorAdjustField() {
		colorAdjustField = new BufferedImage(ADJUST,SIZE,BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = colorAdjustField.createGraphics();
		
		for (int y=0;y<SIZE;y++) {
			float hue = (float)y/SIZE;
			Color c = Color.getHSBColor(hue,1f,1f);
			g.setColor(c);
			g.drawLine(0,SIZE-y-1,ADJUST,SIZE-y-1);
		}
	}
	private void updatePickerField() {
		pickerField = new BufferedImage(SIZE,SIZE,BufferedImage.TYPE_3BYTE_BGR);
		
		float[]hsb = new float[3];
		Color.RGBtoHSB(mainColor.getRed(),mainColor.getGreen(),mainColor.getBlue(),hsb);
		
		for (int s=0;s<SIZE;s++) {
			for (int b=0;b<SIZE;b++) {
				float sat = (float)s/(float)SIZE;
				float br = (float)b/(float)SIZE;
				Color f = Color.getHSBColor(hsb[0],sat,br);
				pickerField.setRGB(b,SIZE-s-1,f.getRGB());
			}
		}
	}
	private void setMode(Point p) {
		if (p.x>SIZE+SPACE && p.x<SIZE+SPACE+ADJUST && p.y>=0 && p.y<SIZE) {
			mode = MODE_COLOR_ADJUST;
		}
		else if (p.x>=0 && p.x<SIZE && p.y>=0 && p.y<SIZE) {
			mode = MODE_PICKER_FIELD;
		}
		else {
			mode = MODE_NONE;
		}
	}
	public void updatePicker(Point p) {
		int rgb;
		Point a;
		switch(mode) {
			case MODE_NONE:
				break;
			case MODE_COLOR_ADJUST:
				// Set main
				a = fixPoint(p,SIZE+SPACE,0,ADJUST,SIZE);
				rgb = colorAdjustField.getRGB(0,a.y);
				mainColorHeight = a.y;
				mainColor = new Color(rgb);
				updatePickerField();
				
				// Update the pick based on the same position with the new color field
				rgb = pickerField.getRGB(pickColorPoint.x,pickColorPoint.y);
				pickColor = new Color(rgb);
				
				repaint();
				break;
			case MODE_PICKER_FIELD:
				// Set pick
				a = fixPoint(p,0,0,SIZE,SIZE);
				rgb = pickerField.getRGB(a.x,a.y);
				pickColor = new Color(rgb);
				pickColorPoint = a;
				repaint();
				break;
		}
	}
	private Point fixPoint(Point p,int x,int y,int w,int h) {
		int a = p.x;
		int b = p.y;
		
		if (a<x) a=x;
		if (a>(w-1)) a=w-1;
		if (b<y) b=y;
		if (b>(h-1)) b=h-1;
		
		return new Point(a,b);
	}
	/**
	 * This is only called when the pickColor is new, and we need
	 * to setup the interface
	 */
	private void initializeInterface() {
		float[]hsb = new float[3];
		Color.RGBtoHSB(pickColor.getRed(),pickColor.getGreen(),pickColor.getBlue(),hsb);
		mainColor = Color.getHSBColor(hsb[0],1f,1f);
		mainColorHeight = SIZE - (int)(hsb[0]*SIZE) - 1;
		updatePickerField();
		repaint();
	}
	private void updatePickPoint() {
		float[]hsb = new float[3];
		Color.RGBtoHSB(pickColor.getRed(),pickColor.getGreen(),pickColor.getBlue(),hsb);
		
		int s = (int)(hsb[1]*SIZE)-1;
		int b = (int)(hsb[2]*SIZE)-1;
		
		pickColorPoint = new Point(b,SIZE-s-1);
		
		while(pickColorPoint.x>=SIZE) pickColorPoint.x--;
		while(pickColorPoint.y>=SIZE) pickColorPoint.y--;
		
		repaint();
	}
	public void paintComponent(Graphics g1) {
		Graphics2D g = (Graphics2D)g1;
		
		g.drawImage(pickerField,0,0,null);
		g.drawImage(colorAdjustField,SIZE+SPACE,0,null);
		
		g.setColor(pickColor);
		g.fillRect(0,SIZE+SPACE,SIZE+SPACE+ADJUST,ADJUST);
		
		if (pickColorPoint!=null) {
			g.setColor(GraphicsUtil.inverseColor(pickColor));
			g.drawOval(pickColorPoint.x-1,pickColorPoint.y-1,3,3);
		}
		
		g.setColor(GraphicsUtil.inverseColor(mainColor));
		g.drawLine(SIZE+SPACE,mainColorHeight,SIZE+SPACE+ADJUST,mainColorHeight);
	}
	public static Color chooseColor(Component c,Color startingColor) {
		ColorPicker picker = new ColorPicker(startingColor);
		if (JOptionPane.showConfirmDialog(c,picker,"Choose color:",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION) {
			return picker.getPickColor();
		}
		return null;
	}
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setSize(500,500);
		frame.getContentPane().setLayout(new BorderLayout());
		final ColorPicker picker = new ColorPicker(Color.blue);
		frame.getContentPane().add(picker);
		
		JButton button = new JButton("test");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				picker.setPickColor(new Color(100,52,221));
			}
		});
		
		frame.getContentPane().add(button,"South");
		
		frame.setVisible(true);
	}
}