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
package com.robin.magic_realm.RealmSpeak;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

import com.robin.general.graphics.GraphicsUtil;
import com.robin.general.swing.ImageCache;

public class ActionIcon extends ImageIcon {
	
	public static final int ACTION_ICON_NORMAL = 1;
	public static final int ACTION_ICON_MEDIUM = 2;
	public static final int ACTION_ICON_SMALL = 3;
	public static final int ACTION_ICON_FULL_TEXT = 4;
	public static final int ACTION_ICON_ABBREV_TEXT = 5;
	
	private String iconName;
	private String text;
	private String code;
	
	private boolean warningOn = false;
	
	public ActionIcon(String iconName,String text,String code) {
		this.iconName = iconName;
		this.text = text;
		this.code = code;
		setStyle(ACTION_ICON_NORMAL);
	}
	
	public void setStyle(int val) {
		String iconPath = "actions/"+iconName;
		if (iconName.equals("pony")) {
			iconPath = "../extraimages/actions/pony";
		}
		switch(val) {
			case ACTION_ICON_NORMAL:
				setImage(ImageCache.getIcon(iconPath).getImage());
				break;
			case ACTION_ICON_MEDIUM:
				setImage(ImageCache.getIcon(iconPath,24,24).getImage());
				break;
			case ACTION_ICON_SMALL:
				setImage(ImageCache.getIcon(iconPath,16,16).getImage());
				break;
			case ACTION_ICON_FULL_TEXT:
				setImage(makeIcon(text,false).getImage());
				break;
			case ACTION_ICON_ABBREV_TEXT:
				setImage(makeIcon(code,true).getImage());
				break;
			default:
				throw new IllegalArgumentException("Invalid argument: "+val);
		}
	}
	private static final Color BORDER_COLOR = new Color(0,0,0,40);
	private static final Font WORD_FONT = UIManager.getFont("Label.font");
	private static final Font CODE_FONT = WORD_FONT.deriveFont(Font.BOLD,WORD_FONT.getSize2D()+5);
	private ImageIcon makeIcon(String val,boolean big) {
		int n = big?1:-1;
		int h = big?28:18;
		int w = val.length()*(big?15:7);
		BufferedImage bi = new BufferedImage(w,h,BufferedImage.TYPE_4BYTE_ABGR);
		Graphics g = bi.getGraphics();
		g.setColor(BORDER_COLOR);
		g.drawRoundRect(1,1,w-2,h-2,8,8);
		g.setFont(big?CODE_FONT:WORD_FONT);
		g.setColor(Color.black);
		GraphicsUtil.drawCenteredString(g,n,n,w,h-4,val);
		if (big) {
			g.setColor(Color.blue);
			GraphicsUtil.drawCenteredString(g,n-1,n-1,w,h-4,val);
		}
		return new ImageIcon(bi);
	}

	private static final Color WARNING_COLOR = Color.red;
	public void paintIcon(Component c, Graphics g, int x, int y) {
		if (warningOn) {
			g.setColor(WARNING_COLOR);
			g.fillRoundRect(0,0,getIconWidth()+(x<<1),getIconHeight()+(y<<1),10,10);
		}
		super.paintIcon(c,g,x,y);
	}
	public boolean isWarningOn() {
		return warningOn;
	}
	public void setWarningOn(boolean warningOn) {
		this.warningOn = warningOn;
	}
	public String getText() {
		return text;
	}
}