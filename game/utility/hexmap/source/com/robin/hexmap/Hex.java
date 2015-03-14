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
package com.robin.hexmap;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import com.robin.general.swing.ImageCache;
import com.robin.general.graphics.GraphicsUtil;

public class Hex {

	public static long unique_hex_id_sequence = 0;

	public static final String WALLS = "_WALLS";
	public static final Font font = new Font("Dialog",Font.BOLD,12);

	public static Image[] wallIcon = {
		ImageCache.getIcon("Wall0").getImage(),
		ImageCache.getIcon("Wall1").getImage(),
		ImageCache.getIcon("Wall2").getImage(),
		ImageCache.getIcon("Wall3").getImage(),
		ImageCache.getIcon("Wall4").getImage(),
		ImageCache.getIcon("Wall5").getImage()
	};
	
	private long unique_hex_id;
	private String id;
	private String name;
	private String label;
	protected ImageIcon icon;
	private boolean suppressID;
	private boolean[] currentWall;
	private boolean[] wallPosition;
	private boolean active = true;
	private int cwTurns = 0;
	private Color idColor = Color.white;
	
	private ArrayList keywords;
	
	public Hex(String id,String name) {
		this(id,name,false);
	}
	public Hex(String id,String name,boolean suppressID) {
		unique_hex_id = _newHexId();
		this.id = id;
		this.name = name;
		this.icon = ImageCache.getIcon(name);
		this.suppressID = suppressID;
		this.label = null;
		keywords = new ArrayList();
		resetWalls();
	}
	private long _newHexId() {
		return (unique_hex_id_sequence++);
	}
	public void setName(String name) {
		this.name = name;
		this.icon = ImageCache.getIcon(name);
	}
	public boolean equals(Object obj) {
		if (obj!=null && obj instanceof Hex) {
			Hex hex = (Hex)obj;
			return hex.unique_hex_id==unique_hex_id;
		}
		return false;
	}
	public String getId() {
		return id;
	}
	public void setLabel(String val) {
		label = val;
	}
	public String getLabel() {
		return label;
	}
	public String toString() {
		return name+" "+keywords;
	}
	public void setActive(boolean val) {
		active = val;
	}
	public boolean isActive() {
		return active;
	}
	public String getName() {
		return name;
	}
	public void addKeyword(String keyword) {
		keywords.add(keyword.toUpperCase());
	}
	public boolean hasKeyword(String val) {
		String check = val.toUpperCase();
		for (int i=0;i<keywords.size();i++) {
			String keyword = (String)keywords.get(i);
			if (keyword.equals(check)) {
				return true;
			}
		}
		return false;
	}
	public boolean hasKeywordStartWith(String val) {
		String check = val.toUpperCase();
		for (int i=0;i<keywords.size();i++) {
			String keyword = (String)keywords.get(i);
			if (keyword.toUpperCase().startsWith(check)) {
//System.out.println(keyword+" startswith "+check);
				return true;
			}
		}
		return false;
	}
	public boolean hasKeywordContains(String val) {
		String check = val.toUpperCase();
		for (int i=0;i<keywords.size();i++) {
			String keyword = (String)keywords.get(i);
			if (keyword.toUpperCase().indexOf(check)>=0) {
//System.out.println(keyword+" startswith "+check);
				return true;
			}
		}
		return false;
	}
	public void resetWalls() {
		currentWall = new boolean[wallIcon.length];
		wallPosition = new boolean[wallIcon.length];
		for (int i=0;i<wallIcon.length;i++) {
			currentWall[i]=false;
			wallPosition[i]=false;
		}
	}
	public void setWall(int position) {
		if (position>=0 && position<currentWall.length) {
			currentWall[position]=true;
			wallPosition[position]=true;
		}
	}
	/**
	 * Returns an array of indices indicating wall positions.  An array of size zero
	 * would indicate no walls.
	 */
	public int[] getWallPositions() {
		ArrayList walls = new ArrayList();
		
		for (int i=0;i<wallPosition.length;i++) {
			if (wallPosition[i]==true) {
				walls.add(new Integer(i));
			}
		}
		
		int[] ret = new int[walls.size()];
		for (int i=0;i<ret.length;i++) {
			Integer index = (Integer)walls.get(i);
			ret[i] = index.intValue();
		}
		return ret;
	}
	public void rotate(int cwTurns) {
		this.cwTurns = cwTurns;
		boolean[] newWallSettings = new boolean[currentWall.length];
		for (int i=0;i<currentWall.length;i++) {
			int n = (i+cwTurns)%currentWall.length;
			newWallSettings[n] = wallPosition[i];
		}
		currentWall = newWallSettings;
	}
	public void updateImageRotation(boolean useRotation) {
		if (useRotation) {
			icon = ImageCache.getRotatedIcon(name,100,cwTurns*60);
			icon = GraphicsUtil.overlayImages(icon,ImageCache.getIcon("HexBorder"));
		}
		else {
			icon = ImageCache.getIcon(name);
		}
	}
	public void draw(Graphics g,int x,int y,boolean showNumbering) {
		g.drawImage(icon.getImage(),x,y,null);
		for (int i=0;i<currentWall.length;i++) {
			if (currentWall[i]) {
				g.drawImage(wallIcon[i],x,y,null);
			}
		}
		if (showNumbering && !suppressID && id!=null) {
			g.setColor(idColor);
			g.setFont(font);
			GraphicsUtil.drawCenteredString(g,x,y,icon.getIconWidth(),icon.getIconHeight()>>1,id);
		}
	}
	public boolean hasWall(int direction) {
		return currentWall[direction];
	}
	public Color getIdColor() {
		return idColor;
	}
	public void setIdColor(Color idColor) {
		this.idColor = idColor;
	}
}