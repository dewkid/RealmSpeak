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
package com.robin.magic_realm.components.attribute;

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.robin.game.objects.GameObject;
import com.robin.general.util.HashLists;

public class ColorMod {
	
	private HashLists<Integer,Integer> conversions;
	
	private ColorMod(String mod) {	
		setMod(mod);
	}
	public boolean willAffect(ColorMagic cm) {
		return conversions.containsKey(cm.getColorNumber());
	}
	private void setMod(String mod) {
		// Like 1.2;1.3 (means white->grey, white->gold)
		boolean error = false;
		conversions = new HashLists<Integer,Integer>();
		StringTokenizer changes = new StringTokenizer(mod,";");
		try {
			while(changes.hasMoreTokens()) {
				String change = changes.nextToken();
				int dot = change.indexOf('.');
				int fromColor = Integer.valueOf(change.substring(0,dot));
				int toColor = Integer.valueOf(change.substring(dot+1));
				conversions.put(fromColor,toColor);
			}
		}
		catch(IndexOutOfBoundsException ex) {
			error = true;
		}
		catch(NumberFormatException ex){
			error = true;
		}
		if (error || conversions.isEmpty()) {
			throw new IllegalArgumentException("Cannot parse argument mod: "+mod);
		}
	}
	
	public ColorMagic convertColor(ColorMagic cm) {
		if (cm!=null && conversions.containsKey(cm.getColorNumber())) {
			for (int toColorNumber:conversions.getList(cm.getColorNumber())) {
				return new ColorMagic(toColorNumber,cm.isInfinite()); // Just return the first in this case...
			}
		}
		return null;
	}
	
	public ArrayList<ColorMagic> getModifiedColors(ArrayList<ColorMagic> colors) {
		ArrayList<ColorMagic> modColors = new ArrayList<ColorMagic>();
		
		for (ColorMagic fromColor:colors) {
			if (conversions.containsKey(fromColor.getColorNumber())) {
				for (int toColorNumber:conversions.getList(fromColor.getColorNumber())) {
					modColors.add(new ColorMagic(toColorNumber,fromColor.isInfinite()));
				}
			}
		}
		
		return modColors;
	}

	private ArrayList<ColorMagic> stripConvertedColors(ArrayList<ColorMagic> colors) {
		ArrayList<ColorMagic> filteredColors = new ArrayList<ColorMagic>();
		for(ColorMagic magic:colors) {
			if (!conversions.containsKey(magic.getColorNumber())) {
				filteredColors.add(magic);
			}
		}
		return filteredColors;
	}
	
	public static ArrayList<ColorMagic> getConvertedColorsForThings(ArrayList<GameObject> things,ArrayList<ColorMagic> colors) {
		ArrayList<ColorMod> list = createColorMods(things);
		if (!list.isEmpty()) {
			ArrayList<ColorMagic> modified = new ArrayList<ColorMagic>();
			for (ColorMod mod:list) {
				for (ColorMagic magic:mod.getModifiedColors(colors)) {
					if (!magic.isInfinite() || !modified.contains(magic)) {
						modified.add(magic);
					}
				}
			}
			for (ColorMod mod:list) {
				colors = mod.stripConvertedColors(colors);
			}
			colors.addAll(modified);
		}
		return colors;
	}
	private static ArrayList<ColorMod> createColorMods(ArrayList<GameObject> things) {
		ArrayList<ColorMod> list = new ArrayList<ColorMod>();
		for (GameObject thing:things) {
			ColorMod mod = createColorMod(thing);
			if (mod!=null) {
				list.add(mod);
			}
		}
		return list;
	}
	public static ColorMod createColorMod(GameObject thing) {
		ColorMod ret = null;
		if (thing.hasThisAttribute("color_mod")) {
			ret = new ColorMod(thing.getThisAttribute("color_mod"));
		}
		return ret;
	}
	public static ColorMod createColorMod(String colorMod) {
		return new ColorMod(colorMod);
	}
}