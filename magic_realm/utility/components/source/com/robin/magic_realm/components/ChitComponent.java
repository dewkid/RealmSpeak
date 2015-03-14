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
package com.robin.magic_realm.components;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.ImageIcon;

import com.robin.game.objects.GameObject;
import com.robin.general.graphics.GraphicsUtil;
import com.robin.general.graphics.TextType;
import com.robin.general.graphics.TextType.Alignment;
import com.robin.general.swing.ImageCache;
import com.robin.magic_realm.components.utility.BattleUtility;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.CombatWrapper;

public abstract class ChitComponent extends RealmComponent {
	public static final Color BACKING = new Color(255,255,255,220);
	protected static Color TRANSPARENT_RED = new Color(255,0,0,120);
	protected static Color TRANSPARENT_BLUE = new Color(0,0,255,120);
	
	protected static final Stroke thickLine = new BasicStroke(4);
	private static final Font woundFont = new Font("Dialog",Font.BOLD,18);

	public static final String LIGHT_SIDE_UP = "light";
	public static final String DARK_SIDE_UP = "dark";

	public static final int T_CHIT_SIZE = 100;	// 1"
	public static final int H_CHIT_SIZE = 88;	// 7/8"
	public static final int M_CHIT_SIZE = 75;	// 3/4"
	public static final int S_CHIT_SIZE = 50;	// 1/2"

	public static final int BORDER_WIDTH = 4;
	
	protected Color lightColor = MagicRealmColor.TAN;
	protected Color darkColor = MagicRealmColor.TAN;
	
	protected String imageExtension = ".gif";
	private boolean showFlipSide = false;
	private boolean ignoreDamage = false;
	
	protected ChitComponent(GameObject obj) {
		super(obj);
		updateSize();
		updateChit();
	}
	public static Dimension getDimensionForSize(String size) {
		if ("T".equals(size)) {
			return new Dimension(T_CHIT_SIZE,T_CHIT_SIZE);
		}
		else if ("H".equals(size)) {
			return new Dimension(H_CHIT_SIZE,H_CHIT_SIZE);
		}
		else if ("M".equals(size)) {
			return new Dimension(M_CHIT_SIZE,M_CHIT_SIZE);
		}
		else if ("L".equals(size) || "S".equals(size)) {
			return new Dimension(S_CHIT_SIZE,S_CHIT_SIZE);
		}
		else if ("S+".equals(size)) {
			return new Dimension(S_CHIT_SIZE+10,S_CHIT_SIZE+10);
		}
		else if ("HCARD".equals(size)) {
			return new Dimension(114,92);
		}
		throw new IllegalArgumentException("Invalid size: "+size);
	}
	private static int SHADOW_BORDER = 4;
	protected void updateSize() {
		if (getSize().width == getChitSize()+SHADOW_BORDER) return;
		int chitSize = getChitSize()+SHADOW_BORDER;
		Dimension size = new Dimension(chitSize,chitSize);
		setSize(size);
		setPreferredSize(size);
		setMaximumSize(size);
		setMinimumSize(size);
	}
	public void updateChit() {
		// This implementation does nothing
	}
	public void changeTactics() {
		// this implementation does nothing
	}
	public void setLightSideUp() {
		setFacing(LIGHT_SIDE_UP);
	}
	public void setDarkSideUp() {
		setFacing(DARK_SIDE_UP);
	}
	public boolean isLightSideUp() {
		String val = gameObject.getThisAttribute(Constants.FACING_KEY);
		boolean lightSideUp = val==null || val.equals(LIGHT_SIDE_UP);
		if (showFlipSide) {
			lightSideUp = !lightSideUp;
		}
		return lightSideUp;
	}
	public boolean isDarkSideUp() {
		return !isLightSideUp();
//		String val = gameObject.getThisAttribute(FACING_KEY);
//		return val!=null && val.equals(DARK_SIDE_UP);
	}
	public String getStatSide() {
		return isLightSideUp()?getLightSideStat():getDarkSideStat();
	}
	public abstract String getLightSideStat();
	public abstract String getDarkSideStat();
	public String getFaceAttributeString(String val) {
		return getAttribute(getStatSide(),val);
	}
	public int getFaceAttributeInt(String val) {
		Integer n = getFaceAttributeInteger(val);
		return n==null ? 0 : n;
	}
	public Integer getFaceAttributeInteger(String val) {
	    String ret = getFaceAttributeString(val);
	    try {
	        Integer n = Integer.valueOf(ret);
	        return n;
	    }
	    catch(NumberFormatException ex) {
	        return null;
	    }
	}
	public boolean hasFaceAttribute(String val) {
		return hasAttribute(getStatSide(),val);
	}
	public String getThisAttribute(String key) {
		return getGameObject().getThisAttribute(key);
	}
	public int getThisInt(String key) {
		return getGameObject().getThisInt(key);
	}
	/**
	 * Flip the chit over
	 */ 
	public void flip() {
		setFacing(isLightSideUp()?DARK_SIDE_UP:LIGHT_SIDE_UP);
	}
	public Dimension getComponentSize() {
		int s = getChitSize();
		return new Dimension(s+4,s+4); // this includes the shadow
	}
	public abstract int getChitSize();
	public abstract Shape getShape(int x,int y,int size);
	
	public ImageIcon getFlipSideIcon() {
		return new ImageIcon(getFlipSideImage());
	}
	public Image getFlipSideImage() {
		// This is dangerous, but I need to temporarily flip the icon without notifying the server
		boolean lightSide = isLightSideUp(); // save the current
		gameObject.getAttributeBlock("this").put(Constants.FACING_KEY,lightSide?DARK_SIDE_UP:LIGHT_SIDE_UP); // under the hood!
		updateSize();
		
		Dimension size = getSize();
		BufferedImage image = new BufferedImage(size.width,size.height,BufferedImage.TYPE_4BYTE_ABGR);
		
		// paint the image
		paintComponent(image.getGraphics());
		
		// and then flip it back!
		gameObject.getAttributeBlock("this").put(Constants.FACING_KEY,lightSide?LIGHT_SIDE_UP:DARK_SIDE_UP); // under the hood!
		updateSize();
		
		// (whew!)
		
		return image;
	}
	protected void drawIcon(Graphics g,String type,String name,double size) {
		drawIcon(g,type,name,size,0,0,null);
	}
	protected void drawIcon(Graphics g,String type,String name,double size,int offsetx,int offsety,Color backing) {
//		Color backing = isLightSideUp()?lightColor:darkColor;
		String filename = type+"/"+name;
		ImageIcon icon = ImageCache.getIcon(filename);
if (icon==null) {
//	System.out.println(gameObject.getXMLString());
	throw new IllegalArgumentException("icon can not be null: "+filename);
}
		drawIcon(g,icon,size,offsetx,offsety,backing);
	}
	
	protected void drawIcon(Graphics g,ImageIcon icon,double size,int offsetx,int offsety,Color backing) {
		int sx = (int)((double)icon.getIconWidth()*size);
		int sy = (int)((double)icon.getIconHeight()*size);
		
		int dx = ((getChitSize()-sx)>>1)+offsetx;
		int dy = ((getChitSize()-sy)>>1)+offsety;
		
		if (backing!=null) {
			g.setColor(backing);
			g.fillRect(dx-2,dy-2,sx+4,sy+4);
		}
		
		g.drawImage(icon.getImage(),dx,dy,sx,sy,null);
	}
	
	public void paintComponent(Graphics g1) {
		Graphics2D g = (Graphics2D)g1;
		int chitSize = getChitSize();
		
		Color mainColor;
		Color edgeColor;
		
		if (shadow && fullDetail) {
			// Draw shadow
			mainColor = Color.darkGray;
			edgeColor = Color.lightGray;
			
			Color shadowColor = new Color(0,0,0,40);
			g.setColor(shadowColor);
			for (int i=0;i<4;i++) {
				Shape shape = getShape(i+4,i+4,chitSize-(i<<1));
				g.fill(shape);
			}
		}
		else {
			// No shadow?  Draw a border instead.
			Shape shape = getShape(0,0,chitSize);
			g.setColor(Color.black);
			g.draw(shape);
		}
		
		// Draw Chit Backing
		mainColor = isLightSideUp()?lightColor:darkColor;
		edgeColor = GraphicsUtil.convertColor(mainColor,Color.black,10);
		
		for (int i=0;i<=BORDER_WIDTH;i++) {
			Shape shape = getShape(i,i,chitSize-(i<<1));
			Color color = GraphicsUtil.convertColor(edgeColor,mainColor,(i*100)/BORDER_WIDTH);
			g.setColor(color);
			g.fill(shape);
		}
		String extraShadingType = getExtraBoardShadingType();
		if (extraShadingType!=null) {
			g.setColor(edgeColor);
			g.fill(getShape(10,10,chitSize-20));
			if ("C".equals(extraShadingType)) {
				g.setColor(mainColor);
				int mid = chitSize>>1;
				g.fillRect(10,mid-5,chitSize-20,10);
			}
			else if ("D".equals(extraShadingType)) {
				g.setColor(mainColor);
				int mid = chitSize>>1;
				g.fillRect(mid-5,10,10,chitSize-20);
			}
		}
		boolean newItem = getGameObject().hasThisAttribute(Constants.TREASURE_NEW);
		if (newItem) {
			g.setColor(Color.yellow);
			g.draw(getShape(0,0,chitSize));
		}
	}
	protected String getExtraBoardShadingType() {
		return gameObject.getThisAttribute(Constants.BOARD_NUMBER);
	}
	protected void drawEmployer(Graphics g) {
		CharacterChitComponent owner = (CharacterChitComponent)getOwner();
		if (owner!=null) {
			ImageIcon brand = owner.getSmallSymbol();
			int ix = 4;
			int iy = 20;
			g.setColor(BACKING);
			g.fillRect(ix-2,iy-2,brand.getIconWidth()+4,brand.getIconHeight()+4);
			g.drawImage(brand.getImage(),ix,iy,null);
		}
	}
	protected void drawHiddenStatus(Graphics g) {
		if (gameObject.hasThisAttribute(Constants.HIDDEN)) {
			TextType tt = new TextType("HIDDEN",getChitSize(),"INFO_GREEN");
			int x = 0;
			int y = (getChitSize()>>1) + 5 - tt.getHeight(g);
			g.setColor(BACKING);
			g.fillRect(x+10,y,getChitSize()-20,15);
			tt.draw(g,x,y,Alignment.Center);
		}
	}
	protected void drawAttentionMarkers(Graphics g1) {
		Graphics2D g = (Graphics2D)g1;
		if (CombatWrapper.hasCombatInfo(getGameObject())) {
			int offset = (getChitSize()-32)>>1;
			Collection attackers = (new CombatWrapper(getGameObject())).getAttackers();
			if (!attackers.isEmpty()) {
				for (Iterator i=attackers.iterator();i.hasNext();) {
					GameObject go = (GameObject)i.next();
					RealmComponent rc = RealmComponent.getRealmComponent(go);
					if (rc.isCharacter()) { // only show character markers (everything else is based on position on sheets)
						CharacterWrapper character = new CharacterWrapper(go);
						CharacterActionChitComponent attention = BattleUtility.getAttentionMarker(character);
						g.drawImage(attention.getPhaseImage(),offset,2,null);
						offset -= 5;
					}
				}
			}
		}
	}
	protected void drawDamageAssessment(Graphics g1) {
		if (!ignoreDamage && CombatWrapper.hasCombatInfo(getGameObject())) {
			Graphics2D g = (Graphics2D)g1;
			CombatWrapper combat = new CombatWrapper(getGameObject());
			int size = getChitSize();
			boolean dead = combat.getKilledBy()!=null;
			if (dead) {
				g.setColor(TRANSPARENT_RED);
				
				// Draw a red X
				g.setStroke(thickLine);
				g.drawLine(0,0,size,size);
				g.drawLine(0,size,size,0);
				
				// TODO Could show who killed it...
				// TODO Could show how much fame/notoriety was scored here... (if killed by character)
			}
			else {
				g.setColor(Color.red);
				int wounds = combat.getNewWounds();
				if (wounds>0) {
					// Draw a red number (or red tally marks....?)
					g.setFont(woundFont);
					GraphicsUtil.drawCenteredString(g,0,0,size,size,String.valueOf(wounds));
				}
				
				if (combat.isPeaceful()) {
					g.setColor(TRANSPARENT_BLUE);
					g.fillRect(0,30,getChitSize(),20);
					
					g.setColor(Color.white);
					g.setFont(Constants.HOTSPOT_FONT);
					GraphicsUtil.drawCenteredString(g,0,28,getChitSize(),20,"Peace");
				}
				else if (combat.isWatchful()) {
					g.setFont(woundFont);
					GraphicsUtil.drawCenteredString(g,0,0,size>>2,size,"!!!");
				}
			}
		}
	}
	public boolean isShowFlipSide() {
		return showFlipSide;
	}
	public void setShowFlipSide(boolean showFlipSide) {
		this.showFlipSide = showFlipSide;
	}
	public boolean isIgnoreDamage() {
		return ignoreDamage;
	}
	public void setIgnoreDamage(boolean ignoreDead) {
		this.ignoreDamage = ignoreDead;
	}
}