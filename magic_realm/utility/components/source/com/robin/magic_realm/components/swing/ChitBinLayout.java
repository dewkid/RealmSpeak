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

import java.awt.Point;
import java.util.*;

import com.robin.general.util.HashLists;
import com.robin.magic_realm.components.CharacterActionChitComponent;
import com.robin.magic_realm.components.ChitComponent;

public class ChitBinLayout {
	public static final int INNER_CELL_SPACE = 3;
	private static final String[] GROUP = {
		"FLY",
		"OTHER",
		"M/F",
		"MOVE",
		"FIGHT",
		"MAGIC",
	};
	
	private ArrayList groups;
	private ArrayList chitBins;
	private HashLists hashLists;
	
	public ChitBinLayout(List allChits) {
		Collections.sort(allChits);
		groups = new ArrayList();
		chitBins = new ArrayList();
		hashLists = new HashLists();
		for (Iterator i=allChits.iterator();i.hasNext();) {
			ChitComponent chit = (ChitComponent)i.next();
			if (chit.isActionChit()) {
				CharacterActionChitComponent achit = (CharacterActionChitComponent)chit;
				if (achit.isMoveFight()) {
					addChit("M/F",achit);
				}
				else if (achit.isMove()) {
					addChit("MOVE",achit);
				}
				else if (achit.isFight() || achit.isFightAlert()) {
					addChit("FIGHT",achit);
				}
				else if (achit.isFly()) {
					addChit("FLY",achit);
				}
				else if (achit.isMagic()) {
					addChit("MAGIC",achit);
				}
				else {
					addChit("OTHER",achit);
				}
			}
			else {
				// Spell Fly chit
				addChit("FLY",null);
			}
		}
		
		ArrayList sorted = new ArrayList(Arrays.asList(GROUP));
		sorted.retainAll(groups);
		groups = sorted;
	}
	public ArrayList getGroups() {
		return groups;
	}
	public ArrayList getBins(String group) {
		return hashLists.getList(group);
	}
	public ChitComponent getChit(int index) {
		if (index<0 || index>=chitBins.size()) {
			throw new IllegalStateException("No chit bin at position " + index);
		}
		ChitBin bin = (ChitBin)chitBins.get(index);
		return bin.getChit();
	}
	public void setChit(int index,ChitComponent chit) {
		if (index<0 || index>=chitBins.size()) {
			throw new IllegalStateException("No chit bin at position " + index);
		}
		ChitBin bin = (ChitBin)chitBins.get(index);
		bin.setChit(chit);
	}
	public ArrayList<ChitComponent> getAllChits() {
		ArrayList<ChitComponent> list = new ArrayList<ChitComponent>();
		for (Iterator i=chitBins.iterator();i.hasNext();) {
			ChitBin bin = (ChitBin)i.next();
			ChitComponent chit = bin.getChit();
			if (chit!=null) {
				list.add(chit);
			}
		}
		return list;
	}
	private void addChit(String type,CharacterActionChitComponent chit) {
		ChitBin bin = new ChitBin();
		if (chit!=null && chit.isMagic()) {
			bin.setColorMagic(chit.getEnchantedColorMagic());
		}
		hashLists.put(type, bin);
		chitBins.add(bin);
		if (!groups.contains(type)) {
			groups.add(type);
		}
	}
	public void reset() {
		for (Iterator i=chitBins.iterator();i.hasNext();) {
			ChitBin bin = (ChitBin)i.next();
			bin.setChit(null);
		}
	}
	public ChitComponent getChitAt(Point p) {
		for (Iterator i=chitBins.iterator();i.hasNext();) {
			ChitBin bin = (ChitBin)i.next();
			if (bin.getRectangle().contains(p)) {
				return bin.getChit();
			}
		}
		return null;
	}
	public int getChitIndex(ChitComponent chit) {
		for (int i=0;i<chitBins.size();i++) {
			ChitBin bin = (ChitBin)chitBins.get(i);
			if (chit==bin.getChit()) { // testing pointer equality is good enough for here
				return i;
			}
		}
		return -1;
	}
}