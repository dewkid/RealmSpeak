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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;

import com.robin.game.objects.GameObject;
import com.robin.general.graphics.StarShape;
import com.robin.general.graphics.TextType;
import com.robin.general.graphics.TextType.Alignment;
import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.attribute.ColorMagic;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class SpellCardComponent extends CardComponent {
	private static final Color INERT_BACKING = new Color(200,200,255,220);
	private static final Color ALIVE_BACKING = new Color(200,255,200,220);
	
	public SpellCardComponent(GameObject obj) {
		super(obj);
	}
	public String getName() {
	    return SPELL;
	}
	public Color getBackingColor() {
		return MagicRealmColor.PURPLE;
	}
	public String getCardTypeName() {
		return "Spell";
	}
	public ImageIcon getUnembellishedIcon() {
		Dimension size = getComponentSize();
		BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_4BYTE_ABGR);
		paintComponent(image.getGraphics(),false);
		return new ImageIcon(image);
	}
	public void paintComponent(Graphics g1) {
		paintComponent(g1,true);
	}
	public void paintComponent(Graphics g1,boolean includeEmbellishments) {
		super.paintComponent(g1);
		Graphics2D g = (Graphics2D)g1;
		if (isFaceUp()) { // only need to draw if face up!
			int pos;
			TextType tt;
			
			SpellWrapper sw = new SpellWrapper(gameObject);
			if (sw.isVirtual()) {
				g.setColor(Color.blue);
				g.drawRect(0,0,size.width-1,size.height-1);
			}
			if (sw.isAlive()) {
				g.setColor(ALIVE_BACKING);
				int x = PRINT_MARGIN;
				int y = (CARD_HEIGHT>>1)+28;
				g.fillRect(x,y+2,PRINT_WIDTH,24);
				
				tt = new TextType("ALIVE",PRINT_WIDTH,"BOLD");
				tt.draw(g,x,y,Alignment.Center);
			}
				
			// Add the NOT READY flag, if needed
			if (gameObject.hasThisAttribute("notready")) {
				tt = new TextType("UNFINISHED",PRINT_WIDTH,"NOTREADY");
				tt.setRotate(25);
				tt.draw(g,PRINT_MARGIN,CARD_HEIGHT>>1,Alignment.Center);
			}
			
			pos = 0;
			// Spell type
			String duration = gameObject.getThisAttribute("duration");
			String dur = duration==null?"?":(StringUtilities.capitalize(duration)+" Spell");
			tt = new TextType(dur,PRINT_WIDTH,"ITALIC");
			tt.draw(g,PRINT_MARGIN,pos,Alignment.Center);
			pos += tt.getHeight(g);
			
			// Spell target
			String target = gameObject.getThisAttribute("target");
			tt = new TextType(target,PRINT_WIDTH,"NORMAL");
			tt.draw(g,PRINT_MARGIN,pos,Alignment.Center);
			pos += tt.getHeight(g);
			
			// Draw the title
			tt = new TextType(gameObject.getName(),PRINT_WIDTH,"TITLE");
			tt.draw(g,PRINT_MARGIN,pos,Alignment.Center);
			pos += tt.getHeight(g);
			
			// Draw the description
			String desc = (String)gameObject.getAttribute("this","text");
			if (desc!=null) {
				tt = new TextType(desc,PRINT_WIDTH,"NORMAL");
				tt.draw(g,PRINT_MARGIN,pos,Alignment.Center);
				pos += tt.getHeight(g);
			}
			
			// If attack card, show damage
			if ("attack".equals(duration)) {
				String strength = gameObject.getThisAttribute("strength");
				if (!"RED".equals(strength)) {
					String speed = gameObject.getThisAttribute("attack_speed");
					if (speed==null) speed="";
					tt = new TextType(strength+speed,PRINT_WIDTH,"CLOSED_RED");
					int sharpness = gameObject.getThisInt("sharpness");
					int x = PRINT_MARGIN+20;
					x += (3-sharpness)*5;
					tt.draw(g,x,pos,Alignment.Left);
					x += tt.getWidth(g)+5;
					int y = pos+tt.getHeight(g)-8;
					for (int i=0;i<sharpness;i++) {
						StarShape star = new StarShape(x,y,5,7);
						g.fill(star);
						x += 10;
					}
				}
			}
			
			// Draw the stats
			pos = CARD_HEIGHT - 18;
			
			// Spell Type
			String spell = gameObject.getAttribute("this","spell");
			String magic_color = ColorMagic.getColorName(gameObject.getAttribute("this","magic_color"));
			tt = new TextType("("+spell+","+magic_color.toUpperCase()+")",PRINT_WIDTH,"TITLE");
			tt.draw(g,PRINT_MARGIN,pos,Alignment.Center);
			
			// If the spell is alive, and a chit was used, the chit will be shown here
			if (includeEmbellishments) {
				ArrayList list = getGameObject().getHold();
				for (Iterator i=list.iterator();i.hasNext();) {
					GameObject held = (GameObject)i.next();
					RealmComponent rc = RealmComponent.getRealmComponent(held);
					if (rc.isActionChit()) {
						rc.paint(g.create(20,(CARD_HEIGHT>>1)-13,ChitComponent.M_CHIT_SIZE,ChitComponent.M_CHIT_SIZE));
					}
				}
			}
				
			// If the spell is INERT, show it here
			if (sw.isInert()) {
				g.setColor(INERT_BACKING);
				int x = PRINT_MARGIN;
				int y = 4;//(CARD_HEIGHT>>1)-20;
				g.fillRect(x,y,PRINT_WIDTH,20);
				
				tt = new TextType("INERT",PRINT_WIDTH,"CLOSED_RED");
				tt.draw(g,x,y,Alignment.Center);
			}
		}
	}
	public String getAdditionalInfo() {
		return gameObject.getThisAttribute("spell");
	}
}