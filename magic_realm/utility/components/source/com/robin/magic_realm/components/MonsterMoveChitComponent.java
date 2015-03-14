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
import com.robin.magic_realm.components.attribute.*;

public class MonsterMoveChitComponent extends MonsterActionChitComponent {
	public MonsterMoveChitComponent(GameObject obj) {
		super(obj);
	}

	public String getName() {
		return "monster_move_chit";
	}

	public Integer getLength() {
		return null; // Not a FIGHT chit!
	}

	public Strength getMoveStrength() {
		return monster.getVulnerability();
	}
	
	public Speed getMoveSpeed() {
		return monster.getMoveSpeed();
	}
	
	public Speed getFlySpeed() {
		return monster.getFlySpeed();
	}

	public boolean hasAnAttack() {
		return false;
	}
	
	public Speed getAttackSpeed() {
		return null; // Not a FIGHT chit!
	}

	public Harm getHarm() {
		return null; // Not a FIGHT chit!
	}

	public String getMagicType() {
		return null; // Not a FIGHT chit!
	}

	public int getManeuverCombatBox() {
		return monster.getManeuverCombatBox(); // oops.... can't separate these, can I!!
	}

	public int getAttackCombatBox() {
		return 0; // Not a FIGHT chit!
	}
	public void paintComponent(Graphics g1) {
		super.paintComponent(g1);
		Graphics2D g = (Graphics2D)g1;
		int n = (getChitSize()>>1)+5;
		monster.paintMoveValues(g,n,n+5);
		
		TextType tt = new TextType("MOVE",S_CHIT_SIZE,"BOLD");
		tt.draw(g1,0,5,Alignment.Center);
	}
}