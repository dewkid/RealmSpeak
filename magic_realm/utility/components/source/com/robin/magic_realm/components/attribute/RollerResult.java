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
package com.robin.magic_realm.components.attribute;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.robin.general.graphics.GraphicsUtil;
import com.robin.general.swing.DieRoller;
import com.robin.magic_realm.components.swing.RollerResults;
import com.robin.magic_realm.components.utility.Constants;

public class RollerResult {
	
	private static final Color ALTERNATING_COLOR1 = new Color(120,120,255,50);
	private static final Color ALTERNATING_COLOR2 = new Color(120,255,120,100);
	
	private String title;
	private String result;
	private String subtitle;
	public RollerResult(String title,String result,String subtitle) {
		this.title = title;
		this.result = result;
		this.subtitle = subtitle;
	}
	public void draw(JPanel parent,Graphics g,int px,int py,boolean useAltColor,boolean altColor) {
		DieRoller roller = new DieRoller(result,25,6);
		Dimension size = roller.getPreferredSize();
		
		int x = px - (size.width>>1);
		int y = py - (size.height>>1);
		
		if (useAltColor) {
			g.setColor(altColor?ALTERNATING_COLOR1:ALTERNATING_COLOR2);
			g.fillRect(px-80,y-10,160,RollerResults.ROLLER_OFFSET-3);
		}
		
		roller.paintComponent(g.create(x,y,size.width,size.height));
		
		Border lineBorder = new LineBorder(Color.black,1);
		Border titleBorder = BorderFactory.createTitledBorder(
				lineBorder,
				title,
				TitledBorder.CENTER,
				TitledBorder.TOP,
				Constants.HOTSPOT_FONT,
				Color.black);
		titleBorder.paintBorder(parent,g,x,y-10,size.width,size.height+10);
		
		if (subtitle!=null) {
			g.setFont(Constants.RESULT_FONT);
			g.setColor(Color.yellow);
			GraphicsUtil.drawCenteredString(g,x+1,y+16,size.width,size.height+10,subtitle);
			g.setColor(Color.blue);
			GraphicsUtil.drawCenteredString(g,x,y+15,size.width,size.height+10,subtitle);
		}
	}
	/**
	 * @return Returns the result.
	 */
	public String getResult() {
		return result;
	}
	/**
	 * @return Returns the subtitle.
	 */
	public String getSubtitle() {
		return subtitle;
	}
	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}
}