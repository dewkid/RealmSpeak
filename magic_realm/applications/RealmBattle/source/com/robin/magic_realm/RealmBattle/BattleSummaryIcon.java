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
package com.robin.magic_realm.RealmBattle;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.Icon;

public class BattleSummaryIcon implements Icon {
	
	private static final Color color1 = Color.white;
	private static final Color color2 = new Color(220,255,255);
	
	private ArrayList<BattleSummaryRow> rows;
	
	public BattleSummaryIcon() {
		rows = new ArrayList<BattleSummaryRow>();
	}
	public BattleSummaryIcon(BattleSummary bs) {
		rows = bs.getSummaryRows(); 
	}
	public void addRow(BattleSummaryRow row) {
		rows.add(row);
	}

	public int getIconHeight() {
		return rows.size()*BattleSummaryRow.HEIGHT;
	}

	public int getIconWidth() {
		return BattleSummaryRow.WIDTH;
	}

	public void paintIcon(Component c, Graphics g1, int x, int y) {
		boolean white = true;
		int n=0;
		Graphics2D g = (Graphics2D)g1;
		for (BattleSummaryRow row:rows) {
			row.draw(g,x,y+n,white?color1:color2);
			n += BattleSummaryRow.HEIGHT;
			white = !white;
		}
	}
}