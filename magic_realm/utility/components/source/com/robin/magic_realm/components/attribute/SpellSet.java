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

import java.util.*;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

/**
 * A Class to encapsulate all the elements needed to cast a spell.
 */
public class SpellSet {
	
	private GameObject spell;
	private ArrayList<GameObject> validTypeObjects; // GameObjects
	private ArrayList<MagicChit> validColorChits;	// MagicChit objects
	private ColorMagic infiniteSource;	// duh!
	
	/**
	 * Sole constructor
	 */
	public SpellSet(GameObject spell) {
		this.spell = spell;
		validTypeObjects = new ArrayList<GameObject>();
		validColorChits = new ArrayList<MagicChit>();
		infiniteSource = null;
	}
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(spell.getName());
		sb.append(":");
		sb.append(validTypeObjects);
		sb.append(":");
		sb.append(validColorChits);
		sb.append(":");
		sb.append(infiniteSource);
		return sb.toString();
	}
	public boolean isAttackSpell() {
		SpellWrapper sw = new SpellWrapper(spell);
		return sw.isAttackSpell();
	}
	public boolean alreadyHasChit(CharacterActionChitComponent inCc) {
		for (GameObject to:validTypeObjects) {
			RealmComponent rc = RealmComponent.getRealmComponent(to);
			if (rc.isActionChit()) {
				CharacterActionChitComponent cc = (CharacterActionChitComponent)rc;
				if (cc.sameChitAttributes(inCc)) {
						return true;
				}
			}
		}
		return false;
	}
	public boolean equals(Object o1) {
		boolean ret = false;
		if (o1 instanceof SpellSet) {
			SpellSet ss = (SpellSet)o1;
			ret = toString().equals(ss.toString());
		}
		return ret;
	}
	
	/**
	 * Cycles through all type objects, and filters out those that don't equal or beat the provided speed.  This
	 * filtering process may make the spell uncastable!
	 */
	public void filterSpeed(Speed speedToBeat) {
		ArrayList<GameObject> toRemove = new ArrayList<GameObject>();
		for (GameObject type:validTypeObjects) {
			RealmComponent rc = RealmComponent.getRealmComponent(type);
			if (rc.isActionChit()) { // only action chits are concerned with speed (everything else is time 0)
				CharacterActionChitComponent chit = (CharacterActionChitComponent)rc;
				if (!chit.getMagicSpeed().fasterThanOrEqual(speedToBeat)) {
					toRemove.add(type);
				}
			}
		}
		validTypeObjects.removeAll(toRemove);
	}
	/**
	 * Returns the speed of the spell
	 */
	public Speed getSpeed() {
		for (GameObject type:validTypeObjects) {
			RealmComponent rc = RealmComponent.getRealmComponent(type);
			if (rc.isActionChit()) { // only action chits are concerned with speed (everything else is time 0)
				CharacterActionChitComponent chit = (CharacterActionChitComponent)rc;
				return chit.getMagicSpeed();
			}
		}
		return new Speed(0);
	}
	public static Speed getSpeedForType(GameObject type) {
		RealmComponent rc = RealmComponent.getRealmComponent(type);
		if (rc.isActionChit()) { // only action chits are concerned with speed (everything else is time 0)
			CharacterActionChitComponent chit = (CharacterActionChitComponent)rc;
			return chit.getMagicSpeed();
		}
		return new Speed(0);
	}
//	public String toString() {
//		return "SpellSet:"+spell.getName()+":"+validTypeObjects.size()+":"+validColorChits.size()+":"+infiniteSource;
//	}
	/**
	 * @return		The ColorMagic needed to supply this spell.   If null, then the spell can be supplied by any color.
	 */
	public ColorMagic getColorMagic() {
		return ColorMagic.makeColorMagic(spell.getThisAttribute("magic_color"),true);
	}
	/**
	 * @return		The incantation type (I,II,III, etc.) needed to cast this spell
	 */
	public String getCastMagicType() {
		return spell.getThisAttribute("spell");
	}
	/**
	 * @return		true if the conditions are met to cast this spell
	 */
	public boolean canBeCast() {
		return (validTypeObjects.size()>0 && (validColorChits.size()>0 || infiniteSource!=null));
	}
	/**
	 * Adds a magic type object.  Could be a chit or treasure.  It is assumed that the added object
	 * is sufficient to cast the spell.
	 */
	public void addTypeObject(GameObject obj) {
		if (!validTypeObjects.contains(obj)) {
			validTypeObjects.add(obj);
		}
	}
	public void addColorChits(Collection in) {
		for (Iterator i=in.iterator();i.hasNext();) {
			MagicChit chit = (MagicChit)i.next();
			addColorChit(chit);
		}
	}
	/**
	 * Adds a color chit (non-infinite source).  It is assumed that if the color is being added, it is sufficient
	 * to cast the spell.
	 */
	public void addColorChit(MagicChit chit) {
		for (MagicChit test:validColorChits) {
			if (test.sameChitAttributes(chit)) {
				// Already got it, so exit here!
				return;
			}
		}
		validColorChits.add(chit);
	}
	/**
	 * Set an infinite color source.  It is assumed that if the color is being added, it is sufficient
	 * to cast the spell.
	 */
	public void setInfiniteColor(ColorMagic color) {
		infiniteSource = color;
	}
	/**
	 * @return Returns the infiniteSource.
	 */
	public ColorMagic getInfiniteSource() {
		return infiniteSource;
	}
	/**
	 * @return Returns the spell.
	 */
	public GameObject getSpell() {
		return spell;
	}
	/**
	 * @return Returns the validColorChits.
	 */
	public ArrayList<MagicChit> getValidColorChits() {
		return validColorChits;
	}
	/**
	 * @return Returns the validTypeObjects.
	 */
	public ArrayList<GameObject> getValidTypeObjects() {
		return validTypeObjects;
	}
}