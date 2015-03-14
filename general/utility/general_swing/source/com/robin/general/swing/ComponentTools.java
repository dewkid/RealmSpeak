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
import javax.swing.table.TableColumn;

public class ComponentTools {
	public static void setSystemLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	public static void setMetalLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	public static void lockDialogButtonSize(JButton button) {
		lockComponentSize(button,120,30);
	}
	public static void lockComponentSize(JComponent c,Dimension d) {
		lockComponentSize(c,d.width,d.height);
	}
	public static void lockComponentSize(JComponent c,int w,int h) {
		Dimension d = new Dimension(w,h);
		c.setMinimumSize(d);
		c.setMaximumSize(d);
		c.setPreferredSize(d);
	}
	public static void minimizeComponentSize(JComponent c,int w,int h) {
		Dimension d = new Dimension(w,h);
		c.setMinimumSize(d);
		c.setPreferredSize(d);
	}
	public static void lockColumnWidth(JTable table,int columnIndex,int width) {
		TableColumn column = table.getColumnModel().getColumn(columnIndex);
		column.setMinWidth(width);
		column.setMaxWidth(width);
		column.setPreferredWidth(width);
	}
	
	/**
	 * Use to center a frame or dialog on the screen
	 * 
	 * @deprecated		Use setLocationRelativeTo(null) instead
	 */
	public static void centerOnScreen(Window window) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension windowSize = window.getSize();
		int screenCenterW = screenSize.width>>1;
		int screenCenterH = screenSize.height>>1;
		int windowCenterW = windowSize.width>>1;
		int windowCenterH = windowSize.height>>1;
		int x = screenCenterW - windowCenterW;
		int y = screenCenterH - windowCenterH;
		window.setLocation(x,y);
	}
	/**
	 * Use to center a frame or dialog on the screen
	 */
	public static void centerOnScreenTop(Window window) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension windowSize = window.getSize();
		int screenCenterW = screenSize.width>>1;
//		int screenCenterH = screenSize.height>>1;
		int windowCenterW = windowSize.width>>1;
//		int windowCenterH = windowSize.height>>1;
		int x = screenCenterW - windowCenterW;
//		int y = screenCenterH - windowCenterH;
		window.setLocation(x,0);
	}
	/**
	 * Use to center a frame or dialog over another component
	 * 
	 * @deprecated		Use window.setLocationRelativeTo(component) instead
	 */
	public static void centerOnComponent(Window window,Component component) {
		Dimension componentSize = component.getSize();
		if (componentSize.width==0 || componentSize.height==0) {
			centerOnScreen(window);
		}
		else {
			Dimension windowSize = window.getSize();
			int componentCenterW = componentSize.width>>1;
			int componentCenterH = componentSize.height>>1;
			int windowCenterW = windowSize.width>>1;
			int windowCenterH = windowSize.height>>1;
			int x = component.getLocation().x + componentCenterW - windowCenterW;
			int y = component.getLocation().y + componentCenterH - windowCenterH;
			if (x < 0) x=0;
			if (y < 0) y=0;
			window.setLocation(x,y);
		}
	}
	/**
	 * Using the screen bounds, this method attempts to return the maximum size and best location that it can.
	 */
	public static Rectangle findPreferredRectangle() {
		return findPreferredRectangle(Integer.MAX_VALUE,Integer.MAX_VALUE);
	}
	/**
	 * Using the screen bounds, this method attempts to return the maximum size and best location that it can,
	 * not to exceed the provided width and height
	 */
	public static Rectangle findPreferredRectangle(int width,int height) {
		GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
		Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);
		Rectangle bounds = config.getBounds();
		int maxW = bounds.width - insets.left - insets.right;
		int maxH = bounds.height - insets.top - insets.bottom;
		
		int w = maxW<width?maxW:width;
		int h = maxH<height?maxH:height;
		
		int x = ((maxW - w) >> 1) + insets.left;
		int y = ((maxH - h) >> 1) + insets.top;
		
		return new Rectangle(x,y,w,h);
	}
	public static void maximize(JFrame frame) {
		Rectangle rect = findPreferredRectangle();
		frame.setSize(rect.width,rect.height);
		frame.setLocation(rect.x,rect.y);
	}

}