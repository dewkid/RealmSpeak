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
package com.robin.magic_realm.components.swing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;

import com.robin.game.objects.GameObject;
import com.robin.general.graphics.GraphicsUtil;
import com.robin.general.swing.ImageCache;
import com.robin.magic_realm.components.BattleHorse;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.attribute.PhaseManager;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class PhaseManagerIcon extends ImageIcon {
	private static final Font NUMBER_FONT = new Font("Dialog",Font.BOLD,12);
	private static final Font ACTION_FONT = new Font("Dialog",Font.BOLD,14);
	
	private PhaseManager pm;
	private List extraActions;
	private Rectangle ponyRect;
	
	public PhaseManagerIcon(PhaseManager pm) {
		this.pm = pm;
		updateImage();
	}
	public void handleClick(CharacterWrapper character,Point p) {
		if (ponyRect!=null && ponyRect.contains(p)) {
			boolean ponyLock = !character.isPonyLock(); // toggle
			character.setPonyLock(ponyLock);
			pm.setPonyLock(ponyLock);
			updateImage();
		}
	}
	public String getText(int x) {
		if (x<32) {
			return pm.getBasic()+" Basic";
		}
		else if (x<64) {
			return pm.getSunlight()+" Sunlight";
		}
		else if (x<96) {
			return pm.getSheltered()+" Sheltered";
		}
		else {
			int n = (x-96)>>5;
			if (n<extraActions.size()) {
				Object[] obj = (Object[])extraActions.get(n);
				String action = (String)obj[0];
				GameObject go = (GameObject)obj[1];
				return "Extra "+action+" phase from "+go.getName();
			}
		}
		return null;
	}
	private String getNonNegString(int val) {
		if (val>0) {
			return String.valueOf(val);
		}
		return "0";
	}
	private void updateImage() {
		int width = 96;
		extraActions = pm.getExtraActionsList();
		width += (extraActions.size()*32);
		
		ponyRect = null;
		
		ImageIcon basic = ImageCache.getIcon("phases/basic");
		ImageIcon sunlight = ImageCache.getIcon("phases/sunshine");
		ImageIcon sheltered = ImageCache.getIcon("phases/sheltered");
		BufferedImage image = new BufferedImage(width,32,BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = (Graphics2D)image.getGraphics();
		g.drawImage(basic.getImage(),0,0,null);
		g.drawImage(sunlight.getImage(),32,0,null);
		g.drawImage(sheltered.getImage(),64,0,null);
		g.setFont(NUMBER_FONT);
		g.setColor(Color.black);
		GraphicsUtil.drawCenteredString(g,0,0,32,30,getNonNegString(pm.getBasic()));
		GraphicsUtil.drawCenteredString(g,32,0,32,30,getNonNegString(pm.getSunlight()));
		GraphicsUtil.drawCenteredString(g,64,0,32,30,getNonNegString(pm.getSheltered()));
		
		int pos = 96;
		g.setFont(ACTION_FONT);
		for (Iterator i=extraActions.iterator();i.hasNext();) {
			Object[] obj = (Object[])i.next();
			String action = (String)obj[0];
			GameObject go = (GameObject)obj[1]; // could draw this as a RealmComponent small image...
if (go==null) {
	System.out.println("why is go null for "+action);
}
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			if (rc!=null) {
				int dx = pos+(rc.isCard()?3:0);
				int dy = 0;
				g.drawImage(rc.getPhaseImage(),dx,dy,null);
				if (rc.isHorse() || rc.isNativeHorse()) {
					BattleHorse horse = (BattleHorse)rc;
					if (horse.doublesMove()) {
						ponyRect = new Rectangle(dx,dy,32,32);
						if (pm.isPonyLock()) {
							Stroke old = g.getStroke();
							g.setStroke(Constants.THICK_STROKE);
							g.setColor(Color.blue);
							g.drawLine(dx,dy,dx+ponyRect.width,dy+ponyRect.height);
							g.drawLine(dx,dy+ponyRect.height,dx+ponyRect.width,dy);
							g.setStroke(old);
						}
					}
				}
			}
			else { // testing
				g.setColor(Color.white);
				g.fillRect(pos,0,32,32);
				g.setColor(Color.black);
				g.drawRect(pos,0,32,32);
			}
			g.setColor(Color.black);
			GraphicsUtil.drawCenteredString(g,pos,0,32,30,action);
			g.setColor(Color.red);
			GraphicsUtil.drawCenteredString(g,pos-1,-1,32,30,action);
			pos += 32;
		}
		
		setImage(image);
	}
	public static void main(String[] args) {
		PhaseManager pm = new PhaseManager(null,null,2,2,0);
		GameObject go = new GameObject(null);
		pm.addFreeAction("M",go);
		pm.addFreeAction("TR",go);
		pm.addFreeAction("S",go);
		pm.addFreeAction("FLY",go);
		PhaseManagerIcon pmv = new PhaseManagerIcon(pm);
		JLabel label = new JLabel(pmv);
		JFrame frame = new JFrame();
		frame.setSize(300,100);
		frame.setLocationRelativeTo(null);
		frame.getContentPane().add(label);
		frame.setVisible(true);
	}
}