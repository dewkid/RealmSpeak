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
package com.robin.magic_realm.components.quest;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.SwingConstants;


public class QuestStepToken {
	QuestStep step;
	int viewRank;
	int displayOrder;
	boolean virtual;
	int drawX;
	int drawY;
	boolean selected; 
	
	public QuestStepToken(QuestStep step) {
		this.step = step;
		virtual = false;
	}
	public String toString() {
		return step.getName()+": Rank "+viewRank+", displayOrder "+displayOrder+(virtual?" (virtual)":"");
	}
	public QuestStep getStep() {
		return step;
	}
	public int getViewRank() {
		return viewRank;
	}
	public void setViewRank(int viewRank) {
		this.viewRank = viewRank;
	}
	public int getDisplayOrder() {
		return displayOrder;
	}
	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}
	public boolean isVirtual() {
		return virtual;
	}
	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}
	public int getDrawX() {
		return drawX;
	}
	public int getDrawY() {
		return drawY;
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public boolean allRequiredPresent(ArrayList<QuestStepToken> tokens) {
		ArrayList all = new ArrayList();
		ArrayList required = step.getRequiredSteps();
		ArrayList onfail = step.getFailSteps();
		if (required!=null) all.addAll(required);
		if (onfail!=null) all.addAll(onfail);
		for(Iterator i=all.iterator();i.hasNext();) {
			String requiredId = (String)i.next();
			boolean found = false;
			for(QuestStepToken token:tokens) {
				if (!token.isVirtual() && token.getStep().getGameObject().getStringId().equals(requiredId)) {
					found = true;
					break;
				}
			}
			if (!found) return false;
		}
		return true;
	}
	
	public void initOrientation(int orientation,int border,int square,int displayOrderSize,int tokensThisRank) {
		int rankPos = (int)(viewRank*square*1.3)+border;
		int space = displayOrderSize-(border<<1);
		int totalSpaceUsed = tokensThisRank*square;
		int spaceLeft = space - totalSpaceUsed;
		int betweenSpace = spaceLeft/(tokensThisRank+1);
		int displayOrderpos = border+betweenSpace+(displayOrder*(square+betweenSpace));
		if (orientation==SwingConstants.HORIZONTAL) {
			drawX = rankPos;
			drawY = displayOrderpos;
		}
		else {
			drawX = displayOrderpos;
			drawY = rankPos;
		}
	}
}