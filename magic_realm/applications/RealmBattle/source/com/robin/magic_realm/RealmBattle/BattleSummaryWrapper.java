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

import java.util.ArrayList;
import java.util.Iterator;

import com.robin.game.objects.*;
import com.robin.magic_realm.components.BattleChit;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class BattleSummaryWrapper extends GameObjectWrapper {
	
	private static final String ATTACKERS = "as";
	private static final String TARGETS = "ts";
	
	public BattleSummaryWrapper(GameObject go) {
		super(go);
	}
	public String getBlockName() {
		return "_BSUM_";
	}
	public void initFromBattleChits(ArrayList battleChits) {
		clearBattleSummary();
		
		for (Iterator i=battleChits.iterator();i.hasNext();) {
			BattleChit bp = (BattleChit)i.next();
			if (bp instanceof SpellWrapper) {
				SpellWrapper spell = (SpellWrapper)bp;
				for (Iterator n=spell.getTargets().iterator();n.hasNext();) {
					BattleChit target = (BattleChit)n.next();
					addBattleSummaryKill(bp.getGameObject(),target.getGameObject());
				}
			}
			else {
				BattleChit target = (BattleChit)bp.getTarget();
				if (target==null) {
					// this happens with the monster weapons
					RealmComponent monster = RealmComponent.getRealmComponent(bp.getGameObject().getHeldBy());
					target = (BattleChit)monster.getTarget();
				}
				if (target!=null) {
					addBattleSummaryKill(bp.getGameObject(),target.getGameObject());
				}
			}
		}
	}
	public BattleSummary getBattleSummary() {
		BattleSummary bs = new BattleSummary();
		ArrayList attackers = getList(ATTACKERS);
		ArrayList targets = getList(TARGETS);
		GameData data = getGameObject().getGameData();
		if (attackers!=null && attackers.size()>0) {
			Iterator k = attackers.iterator();
			Iterator d = targets.iterator();
			while(k.hasNext()) {
				String kid = (String)k.next();
				String did = (String)d.next();
				GameObject kGo = data.getGameObject(Long.valueOf(kid));
				GameObject dGo = data.getGameObject(Long.valueOf(did));
				bs.addAttackerTarget(kGo,dGo);
			}
		}
		return bs;
	}
	private void clearBattleSummary() {
		getGameObject().removeAttributeBlock(getBlockName());
	}
	private void addBattleSummaryKill(GameObject attacker,GameObject target) {
		addListItem(ATTACKERS,attacker.getStringId());
		addListItem(TARGETS,target.getStringId());
	}
}