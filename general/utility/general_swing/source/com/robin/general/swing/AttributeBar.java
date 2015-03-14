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
import java.util.StringTokenizer;

import javax.swing.*;

import com.robin.general.graphics.GraphicsUtil;

public class AttributeBar extends JComponent {

	private static final int ABSOLUTE = 0;

	private static final int DIAMOND_REGION = 10;

	private Font font = UIManager.getFont("Label.font");

	private Color realColor = new Color(0,0,255);
	private Color projColor = new Color(0,0,255,50);
	
	private int valueState = ABSOLUTE;

	private int maxVal;
	private int realVal;
	private int projVal;
	
	private int goalVal;
	
	private String name;
	
	public AttributeBar(int max) {
		this.maxVal = max;
		realVal = 0;
		projVal = 0;
		setPreferredSize(new Dimension(200,25));
		setMaximumSize(new Dimension(5000,25));
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent ev) {
				Point p = ev.getPoint();
				if (p.x<DIAMOND_REGION) {
					toggleValueState();
				}
			}
		});
	}
	public String getKey() {
		return maxVal+"_"+realVal+"_"+projVal+"_"+goalVal;
	}
	public void readKey(String key) {
		StringTokenizer tokenz = new StringTokenizer(key,"_");
		maxVal = Integer.valueOf(tokenz.nextToken()).intValue();
		realVal = Integer.valueOf(tokenz.nextToken()).intValue();
		projVal = Integer.valueOf(tokenz.nextToken()).intValue();
		goalVal = Integer.valueOf(tokenz.nextToken()).intValue();
		normalize();
		repaint();
	}
	/**
	 * Assigns the bar a name for identification purposes
	 */
	public void setName(String val) {
		name = val;
	}
	public String getName() {
		return name;
	}
	public String toString() {
		return (name==null?"AttributeBar":name)+" "+realVal;
	}
	private void toggleValueState() {
		valueState = 1-valueState;
		repaint();
	}
	public void setRealValue(int val) {
		realVal = val;
		normalize();
		repaint();
	}
	public int getRealValue() {
		return realVal;
	}
	public void adjustRealValue(int val) {
		realVal += val;
		projVal += val;
		normalize();
		repaint();
	}
	public void resetProjectedValue() {
		projVal = realVal;
		normalize();
		repaint();
	}
	public void setProjectedValue(int val) {
		projVal = val;
		normalize();
		repaint();
	}
	public int getProjectedValue() {
		return projVal;
	}
	public void adjustProjectedValue(int val) {
		projVal += val;
		normalize();
		repaint();
	}
	public void setGoalValue(int val) {
		goalVal = val;
		normalize();
		repaint();
	}
	private void normalize() {
		realVal = normalize(realVal);
		projVal = normalize(projVal);
		goalVal = normalize(goalVal);
	}
	private int normalize(int val) {
		if (val<0) {
			return 0;
		}
		else if (val>maxVal) {
			return maxVal;
		}
		return val;
	}
	public boolean achievedGoal() {
		return goalVal>0 && realVal>=goalVal;
	}
	public void paintComponent(Graphics g1) {
		Graphics2D g = (Graphics2D)g1;
		Dimension size = getSize();
		g.setFont(font);
		
		Dimension maxTextBox = GraphicsUtil.getStringDimension(g,"-"+maxVal);
		maxTextBox.width+=10;
		
		Rectangle realValRect = new Rectangle(DIAMOND_REGION,0,maxTextBox.width-1,size.height-1);
		Rectangle projValRect = new Rectangle(size.width-maxTextBox.width,0,maxTextBox.width-1,size.height-1);
		
		Rectangle barRect = new Rectangle(realValRect.x+realValRect.width,0,size.width-realValRect.width-projValRect.width-DIAMOND_REGION-1,size.height-1);
		
		double unit = (double)barRect.width/(double)maxVal;
		
		Rectangle realBarRect = new Rectangle(barRect.x,barRect.y,(int)(unit*realVal),barRect.height);
		Rectangle projBarRect = new Rectangle(barRect.x,barRect.y,(int)(unit*projVal),barRect.height);
		
		g.setColor(Color.white);
		g.fill(barRect);

		if (projVal>realVal) {		
			g.setColor(projColor);
			g.fill(projBarRect); // should do a gradient fill
		}
		
		g.setColor(realColor);
		g.fill(realBarRect); // should do a gradient fill
		
		g.setColor(Color.black);
		g.draw(barRect);
		
		if (goalVal>0) {
			if (achievedGoal()) {
				g.setColor(Color.yellow);
			}
			else {
				g.setColor(Color.black);
			}
		
			int x = barRect.x + (int)(unit*goalVal) - 1;
			Polygon upper = new Polygon();
			upper.addPoint(x-4,1);
			upper.addPoint(x,5);
			upper.addPoint(x+4,1);
			g.fill(upper);
			
			Polygon lower = new Polygon();
			lower.addPoint(x-4,barRect.height);
			lower.addPoint(x,barRect.height-4);
			lower.addPoint(x+4,barRect.height);
			g.fill(lower);
			
			int w = maxTextBox.width;
			Rectangle goalValRect = new Rectangle(x-(w>>1),0,w,barRect.height);
			GraphicsUtil.drawCenteredString(g,goalValRect.x,goalValRect.y,goalValRect.width,goalValRect.height,""+goalVal);
		}
		
		GraphicsUtil.drawCenteredString(g,realValRect.x,realValRect.y,realValRect.width,realValRect.height,getStringForValue(realVal));
		if (projVal>realVal) {
			GraphicsUtil.drawCenteredString(g,projValRect.x,projValRect.y,projValRect.width,projValRect.height,getStringForValue(projVal));
		}
		
		// Diamond
		int mid = size.height>>1;
		int halfSize = DIAMOND_REGION>>1;
		Polygon diamond = new Polygon();
		diamond.addPoint(0,mid);
		diamond.addPoint(halfSize,mid-halfSize);
		diamond.addPoint(DIAMOND_REGION,mid);
		diamond.addPoint(halfSize,mid+halfSize);
		if (valueState == ABSOLUTE) {
			g.setColor(realColor);
			g.fill(diamond);
		}
		g.setColor(Color.black);
		g.draw(diamond);
	}
	private String getStringForValue(int val) {
		if (valueState==ABSOLUTE) {
			return ""+val;
		}
		else {
			int rel = val - goalVal;
			if (rel==0) {
				return "0";
			}
			return rel<0?("-"+(-rel)):("+"+rel);
		}
	}
	/**
	 * For testing only
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		final AttributeBar bar = new AttributeBar(100);
		bar.setRealValue(10);
		bar.setProjectedValue(20);
		bar.setGoalValue(60);
		frame.getContentPane().add(bar,"North");
		frame.setSize(400,400);
		frame.setVisible(true);
	}
}