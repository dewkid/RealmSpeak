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

import java.awt.Graphics;
import java.awt.Graphics2D;

import com.robin.game.objects.GameObject;
import com.robin.general.graphics.TextType;
import com.robin.general.graphics.TextType.Alignment;
import com.robin.magic_realm.components.attribute.Harm;
import com.robin.magic_realm.components.attribute.Speed;

public class MonsterFightChitComponent extends MonsterActionChitComponent {
	public MonsterFightChitComponent(GameObject obj) {
		super(obj);
	}

	public String getName() {
		return "monster_fight_chit";
	}

	public Integer getLength() {
		return monster.getLength();
	}

	public Speed getMoveSpeed() {
		return null; // Not a MOVE chit!
	}
	
	public Speed getFlySpeed() {
		return null;
	}
	
	public boolean hasAnAttack() {
		return getFaceAttributeString("strength").length()>0 || getMagicType().length()>0;
	}

	public Speed getAttackSpeed() {
		return monster.getAttackSpeed();
	}

	public Harm getHarm() {
		return monster.getHarm();
	}

	public String getMagicType() {
		return monster.getMagicType();
	}

	public int getManeuverCombatBox() {
		return 0; // Not a MOVE chit!
	}

	public int getAttackCombatBox() {
		return monster.getAttackCombatBox();
	}
	public void paintComponent(Graphics g1) {
		super.paintComponent(g1);
		Graphics2D g = (Graphics2D)g1;
		int n = (getChitSize()>>1)+5;
		monster.paintAttackValues(g,5,n+5,isLightSideUp()?lightColor:darkColor);
		
		TextType tt = new TextType("FIGHT",S_CHIT_SIZE,"BOLD");
		tt.draw(g1,0,5,Alignment.Center);
	}
}