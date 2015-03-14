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

import javax.swing.ImageIcon;

import com.robin.game.objects.GameObject;
import com.robin.general.graphics.TextType;
import com.robin.general.graphics.TextType.Alignment;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CombatWrapper;

public abstract class CardComponent extends RealmComponent {
	private static final Stroke thickLine = new BasicStroke(4);
	
	public static final int CARD_WIDTH = 92;
	public static final int CARD_HEIGHT = 114;
	
	protected static final int PRINT_WIDTH = 88;
	protected static final int PRINT_MARGIN = 2;
	
	protected static final Font backFont = new Font("Dialog",Font.BOLD|Font.ITALIC,14);
	
	protected static final Dimension size = new Dimension(CARD_WIDTH,CARD_HEIGHT);
	
	public static final String FACE_UP = "face_up";		// Card details visible
	public static final String FACE_DOWN = "face_dn";		// Card backing visible
	
	protected boolean nextAddInfo = false;
	
	protected CardComponent(GameObject obj) {
		super(obj);
		setSize(size);
		setPreferredSize(size);
		setMaximumSize(size);
		setMinimumSize(size);
//		setBorder(BorderFactory.createLineBorder(getBackingColor(),3));
	}
	public ImageIcon getFlipSideIcon() {
		return new ImageIcon(getFlipSideImage());
	}
	public Image getFaceUpImage() {
		if (!isFaceUp()) {
			return getFlipSideImage();
		}
		return getImage();
	}
	public Image getFaceDownImage() {
		if (isFaceUp()) {
			return getFlipSideImage();
		}
		return getImage();
	}
	public Image getFlipSideImage() {
		Dimension size = getSize();
		BufferedImage image = new BufferedImage(size.width,size.height,BufferedImage.TYPE_4BYTE_ABGR);
		
		// This is dangerous, but I need to temporarily flip the icon without notifying the server
		boolean faceUp = isFaceUp(); // save the current
		gameObject.getAttributeBlock("this").put(Constants.FACING_KEY,faceUp?FACE_DOWN:FACE_UP); // under the hood!
		
		// paint the image
		paintComponent(image.getGraphics());
		
		// and then flip it back!
		gameObject.getAttributeBlock("this").put(Constants.FACING_KEY,faceUp?FACE_UP:FACE_DOWN); // under the hood!
		
		// (whew!)
		
		return image;
	}
	public Image getPhaseImage() {
		return getImage().getScaledInstance(26,32, Image.SCALE_DEFAULT);
	}
	public Dimension getComponentSize() {
		return new Dimension(CARD_WIDTH,CARD_HEIGHT);
	}
	public int getSortOrder() {
		return 900; // Want this on the bottom... but not as bottom as DwellingChitComponents!
	}
	public String getFacing() {
		String currentFacing = gameObject.getThisAttribute(Constants.FACING_KEY);
		return currentFacing==null?FACE_UP:currentFacing;
	}
	public boolean isFaceUp() {
		String val = gameObject.getThisAttribute(Constants.FACING_KEY);
		return val==null || val.equals(FACE_UP);
	}
	public void setFaceDown() {
		setFacing(FACE_DOWN);
	}
	public void setFaceUp() {
		setFacing(FACE_UP);
	}
	/**
	 * Flip the card over
	 */ 
	public void flip() {
		setFacing(isFaceUp()?FACE_DOWN:FACE_UP);
	}
	public abstract Color getBackingColor();
	public abstract String getCardTypeName();
	public abstract String getAdditionalInfo();
	public void paintComponent(Graphics g) {
		if (isFaceUp()) {
			g.setColor(Color.white);
			g.fillRect(0,0,size.width-1,size.height-1);
		}
		else {
			g.setColor(getBackingColor());
			g.fillRect(0,0,size.width-1,size.height-1);
			g.setFont(backFont);
			g.setColor(Color.white);
			TextType.drawText(g,getCardTypeName(),PRINT_MARGIN,CARD_HEIGHT>>1,PRINT_WIDTH,0,Alignment.Center);
			if (nextAddInfo) {
				TextType.drawText(g,getAdditionalInfo(),PRINT_MARGIN,(CARD_HEIGHT>>1)+20,PRINT_WIDTH,0,Alignment.Center);
			}
		}
		paintBorder(g);
		g.setColor(Color.black);
		g.drawRect(0,0,size.width-1,size.height-1);
		boolean newItem = getGameObject().hasThisAttribute(Constants.TREASURE_NEW);
		if (newItem) {
			g.setColor(Color.yellow);
			g.drawRect(1,1,size.width-3,size.height-3);
			g.drawRect(2,2,size.width-5,size.height-5);
		}
		String author = getGameObject().getThisAttribute(Constants.AUTHOR);
		if (author!=null && isFaceUp()) {
			TextType tt = new TextType(author,CARD_HEIGHT,"AUTHOR");
			tt.setRotate(90);
			tt.draw(g,5,-5,Alignment.Left);
		}
		nextAddInfo = false;
	}
	protected void drawDamageAssessment(Graphics g1) {
		if (CombatWrapper.hasCombatInfo(getGameObject())) {
			Graphics2D g = (Graphics2D)g1;
			CombatWrapper combat = new CombatWrapper(getGameObject());
			boolean dead = combat.getKilledBy()!=null;
			g.setColor(Color.red);
			if (dead) {
				// Draw a red X
				g.setStroke(thickLine);
				g.drawLine(0,0,CARD_WIDTH,CARD_HEIGHT);
				g.drawLine(0,CARD_HEIGHT,CARD_WIDTH,0);
				
				// TODO Could show who killed it...
				// TODO Could show how much fame/notoriety was scored here... (if killed by character)
			}
		}
	}
	public void setNextAddInfo(boolean val) {
		nextAddInfo = val;
	}
	public static int getMediumCardImageWidth() {
		return CARD_WIDTH>>1;
	}
	public static int getMediumCardImageHeight() {
		return CARD_HEIGHT>>1;
	}
	public ImageIcon getNotesIcon() {
		if (!isFaceUp()) {
			return new ImageIcon(getFlipSideImage().getScaledInstance(CARD_WIDTH>>1,CARD_HEIGHT>>1,Image.SCALE_SMOOTH));
		}
		return getMediumIcon();
	}
	public Image getMediumImage() {
		return getImage().getScaledInstance(CARD_WIDTH>>1,CARD_HEIGHT>>1, Image.SCALE_SMOOTH);
	}
}