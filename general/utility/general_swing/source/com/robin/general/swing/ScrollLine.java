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
import java.awt.geom.Rectangle2D;

import javax.swing.SwingConstants;

public class ScrollLine {
	private Rectangle lastRect;
	private String text;
	private Font font;
	private Color color;
	private Color shadowColor;
	private int shadowOffset;
	private int alignment;
	private String linkUrl;
	public ScrollLine() {
		this("");
	}
	public ScrollLine(String text) {
		this(text,new Font("Dialog",Font.PLAIN,12),Color.black,null,0,SwingConstants.CENTER,null);
	}
	public ScrollLine(String text,Font font,Color color) {
		this(text,font,color,null,0,SwingConstants.CENTER,null);
	}
	public ScrollLine(String text,Font font,Color color,Color shadowColor,int shadowOffset) {
		this(text,font,color,shadowColor,shadowOffset,SwingConstants.CENTER,null);
	}
	public ScrollLine(String text,Font font,Color color,Color shadowColor,int shadowOffset,int alignment,String linkUrl) {
		this.text = text;
		this.font = font;
		this.color = color;
		this.alignment = alignment;
		this.shadowColor = shadowColor;
		this.shadowOffset = shadowOffset;
		this.linkUrl = linkUrl;
		if (linkUrl!=null) {
			this.color = Color.blue;
		}
	}
	public int getLeftInset(Graphics2D g,int drawWidth) {
		switch(alignment) {
			case SwingConstants.CENTER:
				return (drawWidth - getWidth(g))>>1;
		}
		return 0;
	}
	public String linkAtPoint(Point p) {
		return linkUrl!=null && lastRect!=null && lastRect.contains(p)?linkUrl:null;
	}
	public int getWidth(Graphics2D g) {
		g.setFont(font);
		return (int)g.getFontMetrics().getStringBounds(text,g).getWidth();
	}
	public int getHeight(Graphics2D g) {
		g.setFont(font);
		return (int)g.getFontMetrics().getStringBounds(text,g).getHeight();
	}
	public void draw(Graphics2D g,int x,int y) {
		Rectangle2D bounds = g.getFontMetrics().getStringBounds(text,g);
		lastRect = new Rectangle(x,y-(int)bounds.getHeight(),(int)bounds.getWidth(),(int)bounds.getHeight());
		g.setFont(font);
		if (shadowColor!=null) {
			g.setColor(shadowColor);
			g.drawString(text,x+shadowOffset,y+shadowOffset);
		}
		g.setColor(color);
		g.drawString(text,x,y);
		if (linkUrl!=null) {
			g.drawLine(x,y+1,x+lastRect.width,y+1);
		}
	}
}