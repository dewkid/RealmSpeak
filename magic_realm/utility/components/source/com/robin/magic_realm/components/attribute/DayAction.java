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

public class DayAction {
	
	public enum ActionId {
		NoAction,
		Hide,
		Move,
		Search,
		Trade,
		Rest,
		Alert,
		Hire,
		Follow,
		Spell,
		SpellPrep,
		EnhPeer,
		Fly,
		RemSpell,
		Cache,
		Heal,
		Repair,
		Fortify,
	};
	
	public static DayAction HIDE_ACTION = new DayAction(ActionId.Hide,"Hide","H","hide");
	public static DayAction MOVE_ACTION = new DayAction(ActionId.Move,"Move","M","move"); // (clearing)
	public static DayAction SEARCH_ACTION = new DayAction(ActionId.Search,"Search","S","search");
	public static DayAction TRADE_ACTION = new DayAction(ActionId.Trade,"Trade","T","trade");
	public static DayAction REST_ACTION = new DayAction(ActionId.Rest,"Rest","R","rest"); 
	public static DayAction ALERT_ACTION = new DayAction(ActionId.Alert,"Alert","A","alert"); 
	public static DayAction HIRE_ACTION = new DayAction(ActionId.Hire,"Hire","HR","hire");
	public static DayAction FOLLOW_ACTION = new DayAction(ActionId.Follow,"Follow","F","follow");  // (individual)
	public static DayAction SPELL_ACTION = new DayAction(ActionId.Spell,"Spell","SP","spell"); 
	public static DayAction SPELL_PREP_ACTION = new DayAction(ActionId.SpellPrep,"Spell Prep","SPX","spell"); 
	public static DayAction ENH_PEER_ACTION = new DayAction(ActionId.EnhPeer,"Enh.Peer","P","peer"); // (clearing) 
	public static DayAction FLY_ACTION = new DayAction(ActionId.Fly,"Fly","FLY","fly"); // (tile) 
	public static DayAction REMOTE_SPELL_ACTION = new DayAction(ActionId.RemSpell,"Rem.Spell","RS","remotespell"); // (clearing)
	public static DayAction CACHE_ACTION = new DayAction(ActionId.Cache,"Cache","C","cache");
	
	// Special Actions (custom characters)
	public static DayAction HEAL_ACTION = new DayAction(ActionId.Heal,"Heal","HL","heal");				// Mostly like REST
	public static DayAction REPAIR_ACTION = new DayAction(ActionId.Repair,"Repair","RP","repair");		// Mostly like ALERT
	public static DayAction FORTIFY_ACTION = new DayAction(ActionId.Fortify,"Fortify","FT","fortify");	// Mostly like HIDE
	
	public static DayAction[] dayAction = {
		HIDE_ACTION,
		MOVE_ACTION,
		SEARCH_ACTION,
		TRADE_ACTION,
		REST_ACTION,
		ALERT_ACTION,
		HIRE_ACTION,
		FOLLOW_ACTION,
		SPELL_ACTION,
		SPELL_PREP_ACTION,
		ENH_PEER_ACTION,
		FLY_ACTION,
		REMOTE_SPELL_ACTION,
		CACHE_ACTION,
		
		HEAL_ACTION,
		REPAIR_ACTION,
		FORTIFY_ACTION,
	};
	
	public static DayAction getDayAction(ActionId id) {
		for (int i=0;i<dayAction.length;i++) {
			if (dayAction[i].id==id) {
				return dayAction[i];
			}
		}
		return null; // no action
	}
	
	private ActionId id;
	private String name;
	private String code;
	private String iconName;
	public DayAction(ActionId id) {
		this(id,null,null,null);
	}
	public DayAction(ActionId id,String name,String code,String iconName) {
		this.id = id;
		this.name = name;
		this.code = code;
		this.iconName = iconName;
	}
	public String getCode() {
		return code;
	}
	public ActionId getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getIconName() {
		return iconName;
	}
	/**
	 * @deprecated bob
	 */
	public boolean equals(Object o1) {
		return false;
	}
}