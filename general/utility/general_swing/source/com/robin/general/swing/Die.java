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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.Icon;

import com.robin.general.graphics.GraphicsUtil;

public class Die implements Icon {
	
	public static final String WHITE = "white";
	public static final String RED = "red";

	private static final int[][] DOT_ARRAY = {
		{0},
		{3,4},
		{0,3,4},
		{1,3,4,6},
		{0,1,3,4,6},
		{1,2,3,4,5,6}
	};
	
	
	private Point[] dotPos;
	private int dieSize;
	private int dotSize;
	private Color dieColor;
	private Color dotColor;
	private int face = 1;  // the dot value is equal to the face
	
	private String name = ""; // no name by default
	
	public Die(int dieSize,int dotSize) {
		this(dieSize,dotSize,Color.white,Color.black,1);
	}
	public Die(int dieSize,int dotSize,Color dieColor,Color dotColor) {
		this(dieSize,dotSize,dieColor,dotColor,1);
	}
	public Die(int dieSize,int dotSize,Color dieColor,Color dotColor,int face) {
		this.dieSize = dieSize;
		this.dotSize = dotSize;
		this.dieColor = dieColor;
		this.dotColor = dotColor;
		setFace(face);
		updateDots();
	}
	public Die(int dieSize,int dotSize,String val) {
		int line = val.indexOf('|');
		name = line>=0?val.substring(0,line):"";
		face = Integer.valueOf(val.substring(line+1));
		this.dieSize = dieSize;
		this.dotSize = dotSize;
		if (RED.equals(name)) {
			this.dieColor = Color.red;
			this.dotColor = Color.white;
		}
		else {//if (WHITE.equals(name)) {
			this.dieColor = Color.white;
			this.dotColor = Color.black;
		}
		updateDots();
	}
	public String getStringResult() {
		StringBuffer sb = new StringBuffer();
		sb.append(name);
		sb.append("|");
		sb.append(face);
		return sb.toString();
	}
	public void setColor(Color dieColor,Color dotColor) {
		this.dieColor = dieColor;
		this.dotColor = dotColor;
	}
	public Die getCopy() {
		return new Die(dieSize,dotSize,dieColor,dotColor,face);
	}
	private void updateDots() {
		dotPos = new Point[7];
		dotPos[0] = new Point(dieSize>>1,dieSize>>1);							// 0 center
		dotPos[1] = new Point(dieSize>>2,dieSize>>2);							// 1 upper-left
		dotPos[2] = new Point(dieSize>>2,dieSize>>1);							// 2 middle-left
		dotPos[3] = new Point(dieSize>>2,dieSize - (dieSize>>2));				// 3 lower-left
		dotPos[4] = new Point(dieSize - (dieSize>>2),dieSize>>2);				// 4 upper-right
		dotPos[5] = new Point(dieSize - (dieSize>>2),dieSize>>1);				// 5 middle-right
		dotPos[6] = new Point(dieSize - (dieSize>>2),dieSize - (dieSize>>2));	// 6 lower-right
	}
	public void setFace(int val) {
		face = val;
		if (face<1 || face>6) {
			throw new IllegalArgumentException("Invalid face argument");
		}
	}
	public int getIconHeight() {
		return dieSize;
	}
	public int getIconWidth() {
		return dieSize;
	}
	public int getValue() {
		return face;
	}
	private void paintDot(Graphics g,int x,int y) {
		x -= (dotSize>>1);
		y -= (dotSize>>1);
		
		g.setColor(GraphicsUtil.convertColor(dieColor,dotColor,50));
		g.fillOval(x,y,dotSize,dotSize);
		g.setColor(dotColor);
		g.fillOval(x+1,y+1,dotSize-2,dotSize-2);
	}
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Color edgeColor = GraphicsUtil.convertColor(Color.gray,dieColor,50);
		for (int i=0;i<=4;i++) {
			Color color = GraphicsUtil.convertColor(edgeColor,dieColor,(i*100)>>2);
			g.setColor(color);
			int size = dieSize-(i<<1);
			g.fillRoundRect(x+i,y+i,size,size,5,5);
		}
		
		int[] dotIndex = DOT_ARRAY[face-1];
		for (int i=0;i<dotIndex.length;i++) {
			Point p = dotPos[dotIndex[i]];
			paintDot(g,x+p.x,y+p.y);
		}
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}