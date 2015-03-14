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
import java.util.*;

import javax.swing.ImageIcon;

import com.robin.game.objects.GameObject;
import com.robin.general.graphics.*;
import com.robin.general.graphics.TextType.Alignment;
import com.robin.magic_realm.components.attribute.ColorMagic;
import com.robin.magic_realm.components.attribute.Strength;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class TreasureCardComponent extends CardComponent implements MagicChit {
	protected static final int SPOT_DIAM = 20;
	private static final Color COMMITTED_BACKING = new Color(255,200,200,220);

	public TreasureCardComponent(GameObject obj) {
		super(obj);
	}
	public String getName() {
	    return TREASURE;
	}
	public Color getBackingColor() {
		return new Color(0,0,128);
	}
	public String getCardTypeName() {
		return "Treasure";
	}
	public void drawSpot(Graphics g,int x,int y,Color c) {
		int rad = SPOT_DIAM>>1;
		Color alias = GraphicsUtil.convertColor(c,Color.white,50);
		g.setColor(alias);
		g.fillOval(x-rad,y-rad,SPOT_DIAM,SPOT_DIAM);
		
		g.setColor(c);
		g.fillOval(x-rad+1,y-rad+1,SPOT_DIAM-2,SPOT_DIAM-2);
	}
	public void paintComponent(Graphics g1) {
		super.paintComponent(g1);
		Graphics2D g = (Graphics2D)g1;
		if (isFaceUp()) { // only need to draw if face up!
			int pos;
			TextType tt;
			
			// Draw spots first (text can overwrite without harm)
			
			String tSize = (String)gameObject.getAttribute("this","treasure");
			boolean large = tSize.equals("large");
			boolean great = gameObject.hasKey("great");
			
			pos = (CARD_HEIGHT>>1) + (CARD_HEIGHT>>3); // 3/4 down
			if (!isColor()) {
				if (large && great) {
					drawSpot(g,(CARD_WIDTH>>1)-SPOT_DIAM,pos,MagicRealmColor.RED);
					drawSpot(g,(CARD_WIDTH>>1)+SPOT_DIAM,pos,MagicRealmColor.GOLD);
				}
				else if (large || great) {
					if (large) {
						drawSpot(g,CARD_WIDTH>>1,pos,MagicRealmColor.GOLD);
					}
					else {
						drawSpot(g,CARD_WIDTH>>1,pos,MagicRealmColor.RED);
					}
				}
			}
			
			// Add the NOT READY flag, if needed
			if (gameObject.hasThisAttribute("notready")) {
				tt = new TextType("UNFINISHED",PRINT_WIDTH,"NOTREADY");
				tt.setRotate(25);
				tt.draw(g,PRINT_MARGIN,CARD_HEIGHT>>1,Alignment.Center);
			}
			
			pos = 0;
			
			// Save a line position for the word "Potion"			
			tt = new TextType("Potion",PRINT_WIDTH,"ITALIC");
			if (gameObject.hasKey("potion")) {
				// Draw the word "Potion" if the treasure is a potion
				tt.draw(g,PRINT_MARGIN,pos,Alignment.Center);
				pos += tt.getHeight(g); // no, dont save the line!
			}
			
			// Draw the title
			tt = new TextType(gameObject.getName(),PRINT_WIDTH,"TITLE");
			tt.draw(g,PRINT_MARGIN,pos,Alignment.Center);
			pos += tt.getHeight(g);
			
			if (isColor()) {
				ImageIcon icon = getColorMagic().getIcon();
				int ix = (CARD_WIDTH - icon.getIconWidth())>>1;
				g.drawImage(icon.getImage(),ix,50,null);
			}
			else {			
				// Draw the description
				String desc = (String)gameObject.getAttribute("this","text");
				if (desc!=null) {
					tt = new TextType(desc,PRINT_WIDTH,"NORMAL");
					tt.draw(g,PRINT_MARGIN,pos,Alignment.Center);
					pos += tt.getHeight(g);
				}
				
				// Draw CURSED
				if (gameObject.hasThisAttribute(Constants.CURSED)) {
					tt = new TextType("CURSED",PRINT_WIDTH,"TITLE_RED");
					tt.draw(g,PRINT_MARGIN+1,pos+1,Alignment.Center);
					tt = new TextType("CURSED",PRINT_WIDTH,"BOLD");
					tt.draw(g,PRINT_MARGIN,pos,Alignment.Center);
					pos += tt.getHeight(g);
				}
				if (gameObject.hasThisAttribute(Constants.DESTROYED)) {
					tt = new TextType("DESTROYED",PRINT_WIDTH,"DESTROYED_FONT");
					tt.draw(g,PRINT_MARGIN,pos,Alignment.Center);
					pos += tt.getHeight(g);
				}
				
				// Draw TWT designation
				String twt = (String)gameObject.getAttribute("this","treasure_within_treasure");
				if (twt!=null) {
					tt = new TextType("P"+twt,PRINT_WIDTH,"TITLE_RED");
					tt.draw(g,PRINT_MARGIN,CARD_HEIGHT - 50,Alignment.Center);
					pos += tt.getHeight(g);
				}
				
				// Draw magic
				String magic = (String)gameObject.getThisAttribute("magic");
				if (magic!=null) {
					ArrayList<String> tiedMagic = readTiedMagicTypes(getGameObject());
					ArrayList enchantedMagic = gameObject.getThisAttributeList(Constants.ARTIFACT_ENHANCED_MAGIC);
					ArrayList<String> finalList = new ArrayList<String>();
					finalList.add(magic);
					if (enchantedMagic!=null) {
						TreeSet unique = new TreeSet();
						unique.addAll(enchantedMagic);
						finalList.addAll(unique);
					}
					StringBuffer sb = new StringBuffer();
					for (String mt:finalList) {
						if (sb.length()>0) sb.append(",");
						boolean tied = tiedMagic.remove(mt);
						if (tied) sb.append("(");
						sb.append(mt);
						if (tied) sb.append(")");
					}
					magic = sb.toString();
					
					tt = new TextType(magic,PRINT_WIDTH,"TITLE_RED");
					tt.draw(g,PRINT_MARGIN,CARD_HEIGHT - 66,Alignment.Center);
					pos += tt.getHeight(g);
				}
				
				// Draw color
				String color = SpellUtility.getColorSourceName(this);
				if (color!=null) {
					tt = new TextType(ColorMagic.getColorName(color.toUpperCase()),PRINT_WIDTH,"TITLE_RED");
					tt.draw(g,PRINT_MARGIN,CARD_HEIGHT - 66,Alignment.Center);
					pos += tt.getHeight(g);
				}
				
				// Draw the stats
				pos = CARD_HEIGHT - 20;
				
				if (gameObject.hasKey(Constants.CANNOT_MOVE)) {
					// Special CANNOT_MOVE text
					tt = new TextType("CANNOT MOVE",PRINT_WIDTH,"BOLD");
					tt.draw(g,PRINT_MARGIN,pos,Alignment.Center);
				}
				else {
					// Weight
					Strength weight = getWeight();
					tt = new TextType(weight.getChar(),PRINT_WIDTH,"BOLD");
					tt.draw(g,PRINT_MARGIN+5,pos,Alignment.Left);
					
					// Gold
					String gold = (String)gameObject.getThisAttribute("base_price");
					gold = gold==null?"0":gold;
					tt = new TextType(gold,PRINT_WIDTH,"BOLD");
					tt.draw(g,CARD_WIDTH-PRINT_MARGIN-5-tt.getWidth(g),pos,Alignment.Left);
					
					// Notoriety
					String notoriety = (String)gameObject.getThisAttribute("notoriety");
					notoriety = notoriety==null?"":("N: "+notoriety);
					tt = new TextType(notoriety,PRINT_WIDTH,"NORMAL");
					tt.draw(g,PRINT_MARGIN,pos,Alignment.Center);
					
					// Fame
					String fame = (String)gameObject.getThisAttribute("fame");
					String nativeCode = (String)gameObject.getThisAttribute("native");
					if (nativeCode==null) {
						fame = fame==null?"":("FAME: "+fame);
					}
					else {
						fame = fame==null?"":(nativeCode+": "+fame+"F");
					}
					tt = new TextType(fame,PRINT_WIDTH,"NORMAL");
					pos -= tt.getHeight(g);
					tt.draw(g,PRINT_MARGIN,pos,Alignment.Center);
					
					// Show awakened/not awakened
					if ((magic!=null && magic.length()>0) || getGameObject().hasThisAttribute("book")) {
						int total = SpellUtility.getSpellCount(getGameObject(),null,false);
						int awakened = SpellUtility.getSpellCount(getGameObject(),Boolean.TRUE,false);
						String awake = "Awakened "+awakened+"/"+total;
						tt = new TextType(awake,PRINT_WIDTH,"INFO_GREEN");
						pos -= tt.getHeight(g);
						tt.draw(g,PRINT_MARGIN,pos,Alignment.Center);
					}
				}
				
				// If attack card (Alchemists Mixture), show damage
				if (getGameObject().hasThisAttribute("attack")) {
					String strength = gameObject.getThisAttribute("strength");
					String speed = gameObject.getThisAttribute("attack_speed");
					if (speed==null) speed="";
					tt = new TextType(strength+speed,PRINT_WIDTH,"CLOSED_RED");
					pos -= tt.getHeight(g);
					int sharpness = gameObject.getThisInt("sharpness");
					sharpness += gameObject.getThisInt(Constants.ADD_SHARPNESS);
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
				
				if (gameObject.hasThisAttribute(Constants.NEEDS_OPEN)) {
					tt = new TextType("CLOSED",PRINT_WIDTH,"CLOSED_RED");
					tt.setRotate(25);
					tt.draw(g,PRINT_MARGIN,CARD_HEIGHT>>2,Alignment.Center);
				}
				
				if (gameObject.hasThisAttribute(Constants.CAST_SPELL_ON_INIT)) {
					for (Iterator i=gameObject.getHold().iterator();i.hasNext();) {
						GameObject sgo = (GameObject)i.next();
						if (sgo.hasThisAttribute("spell")) {
							SpellWrapper spell = new SpellWrapper(sgo);
							if (spell.isInert()) {
								tt = new TextType("INERT",PRINT_WIDTH,"CLOSED_RED");
								int x = PRINT_MARGIN;
								int y = CARD_HEIGHT>>1;
								tt.draw(g,x,y,Alignment.Center);
							}
						}
					}
				}
			}
			if (getGameObject().hasThisAttribute(SpellWrapper.INCANTATION_TIE) && isCommittable()) {
				g.setColor(COMMITTED_BACKING);
				int x = PRINT_MARGIN;
				int y = (CARD_HEIGHT>>2);
				g.fillRect(x,y+2,PRINT_WIDTH,12);
				
				tt = new TextType("Committed",PRINT_WIDTH,"BOLD");
				tt.draw(g,x,y,Alignment.Center);
			}
			
			drawDamageAssessment(g);
		}
	}
	public String getAdditionalInfo() {
		String tSize = gameObject.getThisAttribute("treasure");
		return tSize.equals("large")?"Large":"Small";
	}
	public boolean isCommittable() {
		return !getAllMagicNumbers(8).isEmpty();
	}
	// MagicChit Interface
	public boolean isEnchantable() {
		return !isColor() && !getEnchantableNumbers().isEmpty();
	}
	public boolean isColor() {
		return getGameObject().hasThisAttribute(Constants.ENCHANTED_COLOR);
	}
	public boolean compatibleWith(ColorMagic cm) {
		ArrayList<String> types = readAvailableMagicTypes(null,getGameObject());
		for(String type:types) {
			int mn = CharacterActionChitComponent.getMagicNumber(type);
			if (cm.getColorNumber()==mn) {
				return true;
			}
		}
		return false;
	}
	public ColorMagic getColorMagic() {
		if (isColor()) {
			int color = getGameObject().getThisInt(Constants.ENCHANTED_COLOR);
			return new ColorMagic(color,false);
		}
		return null;
	}
	public ArrayList<Integer> getEnchantableNumbers() {
		 // only 1-5 can be enchanted colors!
		return getAllMagicNumbers(5);
	}
	public ArrayList<Integer> getAllMagicNumbers(int maximum) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		ArrayList<String> types = readAvailableMagicTypes(null,getGameObject());
		for(String type:types) {
			int mn = CharacterActionChitComponent.getMagicNumber(type);
			if (mn>0 && mn<=maximum) {
				list.add(mn);
			}
		}
			
		return list;
	}
	public void enchant(int magicNumber) {
		getGameObject().setThisAttribute(Constants.ENCHANTED_COLOR,magicNumber);
	}
	public void makeFatigued() {
		// artifacts don't really "fatigue" in the same sense as a chit!
		getGameObject().removeThisAttribute(Constants.ENCHANTED_COLOR);
	}
	public boolean sameChitAttributes(MagicChit chit) {
		return false; // ALWAYS false, because every treasure is different!
	}
	public String asKey() {
		return toString();
	}
	
	public void validateColor() {
		if (isColor()) {
			int color = getGameObject().getThisInt(Constants.ENCHANTED_COLOR);
			if (!getEnchantableNumbers().contains(color)) {
				makeFatigued();
			}
		}
	}
	
	private static ArrayList<String> readTiedMagicTypes(GameObject treasure) {
		ArrayList<String> magicTypes = new ArrayList<String>();
		
		// for backward compatibility
		Object test = treasure.getThisAttributeBlock().get(SpellWrapper.INCANTATION_TIE); 
		if (test instanceof String) {
			SpellWrapper spell = new SpellWrapper(treasure.getGameObjectFromThisAttribute(SpellWrapper.INCANTATION_TIE));
			ArrayList newList = new ArrayList();
			newList.add(spell.getCastMagicType());
			treasure.removeThisAttribute(SpellWrapper.INCANTATION_TIE);
			treasure.setThisAttributeList(SpellWrapper.INCANTATION_TIE,newList);
		}
		
		// Ok, back to normal now
		ArrayList list = treasure.getThisAttributeList(SpellWrapper.INCANTATION_TIE);
		if (list!=null) {
			magicTypes.addAll(list);
		}
		return magicTypes;
	}

	public static ArrayList<String> readAvailableMagicTypes(String dayKey,GameObject treasure) {
		ArrayList<String> possMagicTypes = new ArrayList<String>();
		
		String magic = treasure.getThisAttribute("magic");
		if (magic!=null) {
			possMagicTypes.add(magic);
		}
		
		// Include any enchantments
		ArrayList enchants = treasure.getThisAttributeList(Constants.ARTIFACT_ENHANCED_MAGIC);
		if (enchants!=null) {
			for (Iterator i=enchants.iterator();i.hasNext();) {
				String enchant = (String)i.next();
				if (!possMagicTypes.contains(enchant)) {
					possMagicTypes.add(enchant);
				}
			}
		}
		
		// Figure out which magic types have already been used on this artifact today and remove them from possibilities
		String usedKey = treasure.getThisAttribute(Constants.USED_SPELL);
		if (usedKey!=null && usedKey.equals(dayKey)) {
			ArrayList list = treasure.getThisAttributeList(Constants.USED_MAGIC_TYPE_LIST);
			if (list!=null) {
				for (Iterator n=list.iterator();n.hasNext();) {
					String chitType = (String)n.next();
					possMagicTypes.remove(chitType);
				}
			}
		}
		
		// Finally, remove any tied magic types
		possMagicTypes.remove(readTiedMagicTypes(treasure));

		return possMagicTypes;
	}

}