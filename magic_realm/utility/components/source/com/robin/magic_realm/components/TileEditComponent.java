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

import java.util.*;
import java.awt.*;

import com.robin.game.objects.*;
import com.robin.general.util.*;

public class TileEditComponent extends TileComponent {
	
	private boolean changed = false;
	
	public TileEditComponent(GameObject obj) {
		super(obj);
		imageTransparency = 50;
		setAlwaysPaint(true);
	}
	
	public Collection getClearingDetail() {
		return clearings[getFacingIndex()];
	}
	public void setClearingDetail(Collection c) {
		clearings[getFacingIndex()] = new ArrayList(c);
		changed = true;
		repaint();
	}
	public Collection getPathDetail() {
		return paths[getFacingIndex()];
	}
	public void setPathDetail(Collection c) {
		paths[getFacingIndex()] = new ArrayList(c);
		changed = true;
		repaint();
	}
	public Point getOffroadPos() {
		return offroadPos[getFacingIndex()];
	}
	public void setOffroadPos(Point pos) {
		offroadPos[getFacingIndex()] = pos;
		changed = true;
	}
	/**
	 * Translates paths and clearings detail back into the gameObject
	 */
	public void applyChanges() {
		String blockName = isEnchanted()?"enchanted":"normal";
		
		// First, rip out all clearing/path keys from the side
		OrderedHashtable hash = gameObject.getAttributeBlock(blockName);
		ArrayList keysToRemove = new ArrayList();
		for (Enumeration e=hash.keys();e.hasMoreElements();) {
			String key = (String)e.nextElement();
			if (key.startsWith("path") || key.startsWith("clearing")) {
				keysToRemove.add(key);
			}
		}
		for (Iterator i=keysToRemove.iterator();i.hasNext();) {
			String key = (String)i.next();
			hash.remove(key);
		}
		
		// Now add them back
		for (Iterator i=clearings[getFacingIndex()].iterator();i.hasNext();) {
			ClearingDetail detail = (ClearingDetail)i.next();
			String baseKey = detail.toString();
			gameObject.setAttribute(blockName,baseKey+"_type",detail.getType());
			gameObject.setAttribute(blockName,baseKey+"_xy",encodePoint(detail.getPosition()));
			
			StringBuffer magic = new StringBuffer();
			for (int m=ClearingDetail.MAGIC_WHITE;m<=ClearingDetail.MAGIC_BLACK;m++) {
				if (detail.getMagic(m)) {
					magic.append(ClearingDetail.MAGIC_CHAR[m]);
				}
			}
			if (magic.length()>0) {
				gameObject.setAttribute(blockName,baseKey+"_magic",magic.toString());
			}
		}
		
		int n=1;
		for (Iterator i=paths[getFacingIndex()].iterator();i.hasNext();) {
			PathDetail detail = (PathDetail)i.next();
			String baseKey = "path_"+n;
			gameObject.setAttribute(blockName,baseKey+"_from",detail.getFrom().toString());
			gameObject.setAttribute(blockName,baseKey+"_to",detail.getTo().toString());
			gameObject.setAttribute(blockName,baseKey+"_type",detail.getType());
			Point arc = detail.getArcPoint();
			if (arc!=null) {
				gameObject.setAttribute(blockName,baseKey+"_arc",encodePoint(arc));
			}
			n++;
		}
		
		// Don't forget the offroadPos detail
		gameObject.setAttribute(blockName,"offroad_xy",encodePoint(offroadPos[getFacingIndex()]));
		
		changed = false;
	}
	
	protected String encodePoint(Point p) {
		String px = new Double((p.x*100.0)/(double)TILE_WIDTH).toString()+". ";
		px = px.substring(0,px.indexOf(".")+2);
		String py = new Double((p.y*100.0)/(double)TILE_HEIGHT).toString()+". ";
		py = py.substring(0,py.indexOf(".")+2);
		return px+","+py;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// Draw offroad pos
		g.setColor(Color.black);
		g.drawRect(offroadPos[getFacingIndex()].x-25,offroadPos[getFacingIndex()].y-25,50,50);
	}
	public void didChange() {
		changed = true;
	}
	public boolean isChanged() {
		return changed;
	}
}