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
package com.robin.magic_realm.RealmCharacterBuilder;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.*;
import java.util.*;

import javax.swing.ImageIcon;

import com.robin.general.swing.MultiFormatString;
import com.robin.general.util.StringBufferedList;
import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.CharacterActionChitComponent;
import com.robin.magic_realm.components.attribute.RelationshipType;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.RealmUtility;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class CharacterInfoCard {
	public static final String NO_INVERT_ICON = "no_invert_icon";
	public static final int PICTURE_WIDTH = 328;
	public static final int PICTURE_HEIGHT = 467;
	public static final String fontName = "Dialog";
	public static final Font titleFont = new Font(fontName, 1, 24);
	public static final Font boldFont = new Font(fontName, Font.BOLD, 12);
	public static final Font plainFont = new Font(fontName, Font.PLAIN, 12);
	public static final Font italicFont = new Font(fontName, Font.ITALIC, 12);
	public static final Font boldItalicFont = new Font(fontName, Font.BOLD|Font.ITALIC, 12);
	
	public static final Font chitFont = new Font(fontName, 1, 12);
	public static final Color darkGreen = new Color(0, 130, 0);
	
	private CharacterWrapper character;
	private ImageIcon picture = null;
	
	public CharacterInfoCard(CharacterWrapper character) {
		this.character = character;
	}
	public void setPicture(ImageIcon picture) {
		this.picture = picture;
	}
	
	public ImageIcon getImageIcon(boolean includePicture) {
		int detailX = 0;
		int width = 388;
		if (includePicture) {
			detailX = width;
			width *= 2;
		}
		BufferedImage image = new BufferedImage(width, 607,BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = image.getGraphics();
		drawDetailImage(g.create(detailX,0,388,607));
		if (includePicture) {
			drawPictureImage(g);
		}
		return new ImageIcon(image);
	}
	private void drawPictureImage(Graphics g1) {
		Graphics2D g = (Graphics2D)g1;
		g.setColor(Color.black);
		g.fillRect(0,0,388,607);
		int bx = 30;
		int by = 70;
		int bw = PICTURE_WIDTH; //(388-(bx<<1));
		int bh = PICTURE_HEIGHT; //(607-(by<<1));
		Shape ellipse = new Ellipse2D.Double((double)bx,(double)by,(double)bw,(double)bh);
		
		g.setColor(Color.white);
		g.setFont(titleFont);
		g.drawString(character.getGameObject().getName().toUpperCase(),bx,by-20);
		
		// Invert the icon
		ImageIcon icon = character.getFullSizedIcon();
//		byte[] invert = new byte[256];
//		for (int i = 0; i < 256; i++)
//			invert[i] = (byte)(255 - i);
//		BufferedImageOp invertOp = new LookupOp(new ByteLookupTable(0, invert), null);
//		BufferedImage bi = new BufferedImage(icon.getIconWidth(),icon.getIconHeight(),BufferedImage.TYPE_3BYTE_BGR);
//		Graphics big = bi.getGraphics();
//		big.setColor(Color.white);
//		big.fillRect(0,0,icon.getIconWidth(),icon.getIconHeight());
//		big.drawImage(icon.getImage(),0,0,null);
//		BufferedImage bif = invertOp.filter(bi,null);
		
		if (character.getGameObject().hasThisAttribute(NO_INVERT_ICON)) {
			g.drawImage(icon.getImage(), 288, 10, null);
		}
		else {
			g.drawImage(getInvertedImage(icon), 288, 10, null);
		}
		
		if (picture!=null) {
			String artCredit = character.getGameObject().getThisAttribute("artcredit");
			if (artCredit!=null) {
				g.setFont(boldItalicFont);
				g.setColor(Color.white);
				g.drawString("Art Credit: " + artCredit, 15, 590);
			}
			
			g.clip(ellipse);
			g.drawImage(picture.getImage(),bx,by,bw,bh,null);
		}
		else {
			g.setColor(Color.lightGray);
			g.fill(ellipse);
		}
	}
	
	private void drawDetailImage(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, 388, 607);
		int i = 35;
		g.setColor(Color.black);
		g.setFont(titleFont);
		String charName = character.getGameObject().getName().toUpperCase();
		g.drawString(charName, 15, i);
		i += 15;
		g.setFont(italicFont);
		String creator = character.getGameObject().getThisAttribute("creator");
		if (creator!=null) {
			g.drawString("Creator: " + creator, 15, i);
			i += g.getFontMetrics().getHeight();
		}
		g.setFont(boldFont);
		String meaning = character.getGameObject().getThisAttribute("meaning");
		g.drawString("Meaning of Symbol:  \"" + (meaning==null?"":meaning) + "\"", 15, i);
		i += 15;
		g.drawImage(character.getFullSizedIcon().getImage(), 288, 10, null);
		g.drawString("WEIGHT/VULNERABILITY:  "+character.getVulnerability().fullString(), 15, i);
		i += 20;
		i = addAdvantagesToImage(g, "Special Advantages:", getAllLevelList("advantages"), i);
		i += 5;
		i = addAdvantagesToImage(g, "Optional Advantages:", getAllLevelList("optional"), i);
		
		String extraNotes = character.getGameObject().getThisAttribute("extra_notes");
		if (extraNotes!=null && extraNotes.trim().length()>0) {
			i += 5;
			g.setFont(boldItalicFont);
			String s = "Notes:";
			g.drawString(s, 15, i);
			int l = g.getFontMetrics().stringWidth(s) + 10;
			int n = 0;
			MultiFormatString multiformatstring = new MultiFormatString(extraNotes);
			multiformatstring.setFontSize(12);
			i += multiformatstring.draw(g, 15 + l, i, getCardPrintableWidth() - l);
			n++;
		}
		i += 5;
		
		g.setColor(darkGreen);
		g.setFont(plainFont);
		g.drawString("Development:", 15, i);
		i += 20;
		StringBuffer startingEquipment = new StringBuffer();
		ArrayList<CharacterActionChitComponent> actionChits = character.getAllActionChitsSorted(4);
		int startingGold = 10;
		for (int level = 0; level < 4; level++) {
			g.setFont(boldFont);
			g.drawString(character.getCharacterLevelName(level+1).toUpperCase(), 15, i);
			i += 15;
			String levelKey = "level_"+(level+1);
			String armor = character.getGameObject().getAttribute(levelKey,"armor");
			if (armor!=null && armor.trim().length()==0) {
				armor = null;
			}
			String weapon = character.getGameObject().getAttribute(levelKey,"weapon");
			startingEquipment = new StringBuffer();
			startingEquipment.append((armor==null?"":armor.toUpperCase()) + (weapon==null?"":(armor==null?"":",")+weapon.toUpperCase()));
			
			int spellCount = character.getGameObject().getInt(levelKey,"spellcount");
			if (spellCount>0) {
				ArrayList list = character.getGameObject().getAttributeList(levelKey,"spelltypes");
				Collections.sort(list,new Comparator() {
					public int compare(Object o1,Object o2) {
						String s1 = (String)o1;
						String s2 = (String)o2;
						int n1 = CharacterActionChitComponent.getMagicNumber(s1);
						int n2 = CharacterActionChitComponent.getMagicNumber(s2);
						return n1-n2;
					}
				});
				StringBufferedList sbl = new StringBufferedList(", ","and/or ");
				sbl.appendAll(list);
				StringBuffer sb = new StringBuffer();
				sb.append(RealmCharacterConstants.SPELL_COUNT_STRING[spellCount]);
				sb.append(" <i>Spell");
				sb.append(spellCount==1?"":"s");
				sb.append("</i> (type");
				sb.append(list.size()==1?"":"s");
				sb.append(" ");
				sb.append(sbl.toString());
				sb.append(")");
				if (startingEquipment.length()>0) {
					startingEquipment.append(", ");
				}
				startingEquipment.append(sb.toString());
			}
			
			if (character.getGameObject().hasAttribute(levelKey,Constants.STARTING_GOLD)) {
				startingGold = character.getGameObject().getInt(levelKey,Constants.STARTING_GOLD);
			}
			StringBuffer gold = new StringBuffer();
			if (startingEquipment.length()>0) {
				gold.append(", ");
			}
			gold.append(startingGold);
			gold.append("g");
			
			MultiFormatString multiformatstring1 = new MultiFormatString(startingEquipment.toString()+gold.toString());
			multiformatstring1.setFontAttributes(fontName,12);
			i += multiformatstring1.draw(g, 15, i, getCardPrintableWidth());
			
			int l = 20;
			if (level > 0) {
				g.setFont(plainFont);
				g.drawString("add", l, i + 10);
				l += 40;
			}
			int baseStage = level * 3;
			for (int index = 0; index < 3; index++) {
				addChitToImage(g,l,i-15,actionChits.get(baseStage+index));
				l += 58;
			}

			i += 40;
		}

		i = 507;
		g.setColor(Color.black);
		String[] startingLoc = character.getStartingLocations(false);
		StringBuffer sb = new StringBuffer("Start at");
		for (int n=0;n<startingLoc.length;n++) {
			if (n>0) {
				if (n==startingLoc.length-1) {
					sb.append(" or");
				}
				else {
					sb.append(",");
				}
			}
			sb.append(" <b>");
			sb.append(startingLoc[n]);
			sb.append("</b>");
		}
		sb.append(" with ");
		sb.append(startingGold);
		sb.append("g");
		if (startingEquipment.length()>0) {
			sb.append(" plus");
		}
		
		MultiFormatString multiformatstring = new MultiFormatString(sb.toString());
		multiformatstring.setFontAttributes("Serif", 14);
		i += multiformatstring.draw(g, 15, i, getCardPrintableWidth());
		i += 5;
		multiformatstring = new MultiFormatString(startingEquipment.toString());
		multiformatstring.setFontAttributes("Serif", 14);
		i += multiformatstring.draw(g, 15, i, getCardPrintableWidth());
		i += 5;
		i = addPoliticsToImage(g, "ALLIES", character.getRelationshipList(Constants.GAME_RELATIONSHIP,RelationshipType.ALLY), i);
		i = addPoliticsToImage(g, "FRIENDLY", character.getRelationshipList(Constants.GAME_RELATIONSHIP,RelationshipType.FRIENDLY), i);
		i = addPoliticsToImage(g, "UNFRIENDLY", character.getRelationshipList(Constants.GAME_RELATIONSHIP,RelationshipType.UNFRIENDLY), i);
		i = addPoliticsToImage(g, "ENEMY", character.getRelationshipList(Constants.GAME_RELATIONSHIP,RelationshipType.ENEMY), i);
	}
	private ArrayList<String> getAllLevelList(String key) {
		ArrayList<String> allList = new ArrayList<String>();
		for (int n=1;n<=4;n++) {
			String levelKey = "level_"+n;
			ArrayList list = character.getGameObject().getAttributeList(levelKey,key);
			if (list!=null) {
				for (Iterator i=list.iterator();i.hasNext();) {
					String val =(String)i.next();
					allList.add(val);
				}
			}
		}
		return allList;
	}
	private void addChitToImage(Graphics g,int x,int y,CharacterActionChitComponent chit) {
		g.setClip(x,y, 45, 45);
		g.setFont(chitFont);
		g.setColor(darkGreen);
		String s = chit.getAction();
		String s1 = (chit.isMagic()?chit.getMagicType():chit.getStrength().toString())
						+chit.getSpeed().getNum()
						+StringUtilities.getRepeatString("*",chit.getEffortAsterisks());
		g.drawString(s, x + (45 - g.getFontMetrics().stringWidth(s) >> 1), ((y + 22 + (g.getFontMetrics().getAscent() >> 1)) - g.getFontMetrics().getAscent()) + 4);
		g.drawString(s1, x + (45 - g.getFontMetrics().stringWidth(s1) >> 1), y + 22 + (g.getFontMetrics().getAscent() >> 1) + 4);
		g.setClip(null);
	}
	public int addAdvantagesToImage(Graphics g, String s, ArrayList<String> advantages, int i) {
		if (!advantages.isEmpty()) {
			g.setFont(boldItalicFont);
			g.drawString(s, 15, i);
			i += 15;
			int n = 0;
			for (String advantage:advantages) {
				g.setFont(boldFont);
				String s1 = (new Integer(n + 1)).toString() + ".)";
				g.drawString(s1, 15, i);
				int l = g.getFontMetrics().stringWidth(s1) + 10;
				String s2 = formatAdvantage(advantage);
				MultiFormatString multiformatstring = new MultiFormatString(s2);
				multiformatstring.setFontSize(12);
				i += multiformatstring.draw(g, 15 + l, i, getCardPrintableWidth() - l);
				n++;
			}

		}
		return i;
	}
	private String formatAdvantage(String adv) {
		StringTokenizer tokens = new StringTokenizer(adv,":");
		if (tokens.countTokens()==2) {
			String name = tokens.nextToken();
			String desc = tokens.nextToken();
			StringBuffer sb = new StringBuffer();
			sb.append("<b>");
			sb.append(name);
			sb.append(":</b>  ");
			sb.append(desc);
			return sb.toString();
		}
		return adv;
	}

	public int addPoliticsToImage(Graphics g, String s, ArrayList<String> list, int i) {
		if (!list.isEmpty()) {
			g.setColor(Color.black);
			MultiFormatString multiformatstring = new MultiFormatString("<b>" + s + ": </b>" + RealmUtility.getHTMLPoliticsString(list));
			multiformatstring.setFontAttributes("Serif", 14);
			i += multiformatstring.draw(g, 15, i, getCardPrintableWidth());
		}
		return i;
	}
	public static int getCardPrintableWidth() {
		return 358;
	}
	public static Image getInvertedImage(ImageIcon icon) {
		byte[] invert = new byte[256];
		for (int i = 0; i < 256; i++)
			invert[i] = (byte)(255 - i);
		BufferedImageOp invertOp = new LookupOp(new ByteLookupTable(0, invert), null);
		BufferedImage bi = new BufferedImage(icon.getIconWidth(),icon.getIconHeight(),BufferedImage.TYPE_3BYTE_BGR);
		Graphics big = bi.getGraphics();
		big.setColor(Color.white);
		big.fillRect(0,0,icon.getIconWidth(),icon.getIconHeight());
		big.drawImage(icon.getImage(),0,0,null);
		BufferedImage bif = invertOp.filter(bi,null);
		return bif;
	}
}