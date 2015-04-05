package com.robin.magic_realm.components.effect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.utility.ClearingUtility;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class PhantasmEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		GameObject spellObj = context.Spell.getGameObject();
		CharacterWrapper character = context.getCharacterTarget();
		
		String spawn = spellObj.getThisAttribute("spawn");
		
		if ("phantasm".equals(spawn)) {
			GameObject phantasm = spellObj.getGameData().createNewObject();
			
			phantasm.setName(character.getGameObject().getName()+"'s Phantasm");
			phantasm.setThisAttribute("phantasm");
			phantasm.setThisAttribute(Constants.ICON_TYPE,"phantasm");
			phantasm.setThisAttribute(Constants.ICON_FOLDER,"characters");
			phantasm.addThisAttributeListItem(Constants.SPECIAL_ACTION,"ENHANCED_PEER");
			
			character.getCurrentLocation().clearing.add(phantasm,null);
			CharacterWrapper charPhantasm = new CharacterWrapper(phantasm);
			
			charPhantasm.setPlayerName(character.getPlayerName());
			charPhantasm.setPlayerPassword(character.getPlayerPassword());
			charPhantasm.setPlayerEmail(character.getPlayerEmail());
			
			character.addMinion(phantasm);
			RealmComponent rc = RealmComponent.getRealmComponent(phantasm);
			rc.setOwner(RealmComponent.getRealmComponent(character.getGameObject()));
		}
	}

	@Override
	public void unapply(SpellEffectContext context) {
		CharacterWrapper character = context.getCharacterTarget();
		String spawn = context.Spell.getGameObject().getThisAttribute("spawn");
		
		if ("phantasm".equals(spawn)) {
			// Simply remove ALL phantasms - they could only have been the result of previous days casting
			Collection c = character.getMinions();
			if (c!=null) {
				ArrayList<GameObject> toRemove = new ArrayList<GameObject>();
							
				for (Iterator i=c.iterator();i.hasNext();) {
					GameObject minion = (GameObject)i.next();
					if (minion.hasThisAttribute("phantasm")) {
						toRemove.add(minion);
					}
				}
				
				for (Iterator<GameObject> i=toRemove.iterator();i.hasNext();) {
					GameObject minion = i.next();
					character.removeMinion(minion);
					ClearingUtility.moveToLocation(minion,null);
				}
			}
		}
	}

}
