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
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.attribute.Spoils;
import com.robin.magic_realm.components.wrapper.CombatWrapper;

public class CombatTallyView {
	
	private static Font font = new Font("Dialog",Font.PLAIN,12);
	private static Font bold = new Font("Dialog",Font.BOLD,12);
	private static DecimalFormat formatter = new DecimalFormat("####.##");
	private static int WIDTH = 450;
	private static int ROW_HEIGHT = 14;
	
	private static int SPACER = 40;
	private static int ROUND_COL = 5;
	private static int FAME_COL = 50;
	private static int NOTORIETY_COL = 150;
	private static int GOLD_COL = 250;
	private static int KILL_COL = 320;
	
	private ArrayList<Integer> rounds;
	private ArrayList<GameObject> kills;
	private ArrayList<Spoils> spoils;
	
	private int width;
	private int height;
	
	public CombatTallyView(RealmComponent owner) {
		CombatWrapper combat = new CombatWrapper(owner.getGameObject());
		rounds = combat.getAllRounds();
		if (rounds!=null){ 
			kills = combat.getAllKills();
			spoils = combat.getAllSpoils();
		}
	}
	public CombatTallyView(ArrayList<Integer> rounds,ArrayList<GameObject> kills,ArrayList<Spoils> spoils) {
		this.rounds = rounds;
		this.kills = kills;
		this.spoils = spoils;
	}
	public boolean isValid() {
		return rounds!=null;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public void draw(Graphics2D g, Point anchor) {
		width = WIDTH;
		height = (rounds.size()+3) * ROW_HEIGHT;
		Rectangle r = new Rectangle(anchor.x - width,anchor.y - height,width,height);
		g.setColor(Color.white);
		g.fill(r);
		g.setColor(Color.black);
		g.draw(r);
		
		int y = ROW_HEIGHT;
		g.setFont(bold);
		g.drawString("Rnd",r.x+ROUND_COL,r.y+y+5);
		g.drawString("Fame",r.x+FAME_COL,r.y+y+5);
		g.drawString("Not.",r.x+NOTORIETY_COL,r.y+y+5);
		g.drawString("Gold",r.x+GOLD_COL,r.y+y+5);
		g.drawString("Kill",r.x+KILL_COL,r.y+y+5);
		
		y += ROW_HEIGHT;
		g.setColor(Color.cyan);
		g.drawLine(r.x+FAME_COL-5,r.y+2,r.x+FAME_COL-5,r.y+height-2);
		g.drawLine(r.x+NOTORIETY_COL-5,r.y+2,r.x+NOTORIETY_COL-5,r.y+height-2);
		g.drawLine(r.x+GOLD_COL-5,r.y+2,r.x+GOLD_COL-5,r.y+height-2);
		g.drawLine(r.x+KILL_COL-5,r.y+2,r.x+KILL_COL-5,r.y+height-2);
		g.setColor(Color.blue);
		g.drawLine(r.x+2,r.y+y-5,r.x+width-2,r.y+y-5);
		
		int lastRound = -1;
		g.setFont(font);
		g.setColor(Color.black);
		double runningFame = 0;
		double runningNot = 0;
		double runningGold = 0;
		for (int i=0;i<rounds.size();i++) {
			y += ROW_HEIGHT;
			int round = rounds.get(i);
			if (round!=lastRound) {
				if (lastRound!=-1) {
					g.setColor(Color.lightGray);
					g.drawLine(r.x+2,r.y+y-ROW_HEIGHT+2,r.x+width-2,r.y+y-ROW_HEIGHT+2);
					g.setColor(Color.black);
				}
				g.drawString(String.valueOf(round),r.x+ROUND_COL,r.y+y);
				lastRound = round;
			}
			
			Spoils s = spoils.get(i);
			int m = s.getMultiplier();
			s.setMultiplier(1);
			
			runningFame += (s.getFame()*m);
			g.drawString(formatter.format(s.getFame())+" (x"+m+")",r.x+FAME_COL,r.y+y);
			g.drawString("+= "+formatter.format(runningFame),r.x+FAME_COL+SPACER,r.y+y);
			
			runningNot += (s.getNotoriety()*m);
			g.drawString(formatter.format(s.getNotoriety())+" (x"+m+")",r.x+NOTORIETY_COL,r.y+y);
			g.drawString("+= "+formatter.format(runningNot),r.x+NOTORIETY_COL+SPACER,r.y+y);
			
			runningGold += (s.getGoldBounty()+s.getGoldRecord());
			g.drawString(formatter.format(s.getGoldBounty()+s.getGoldRecord()),r.x+GOLD_COL,r.y+y);
			g.drawString("+= "+formatter.format(runningGold),r.x+GOLD_COL+(SPACER>>1),r.y+y);
			
			GameObject kill = kills.get(i);
			g.drawString(kill.getName(),r.x+KILL_COL,r.y+y);
		}
	}
}