package com.robin.magic_realm.components.effect;

import java.util.ArrayList;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.utility.RealmLogging;
import com.robin.magic_realm.components.wrapper.CombatWrapper;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class PeaceEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		CombatWrapper combat = context.getCombatTarget();
		
		boolean attacked = false;
		ArrayList<GameObject> attackers = combat.getAttackers();
		for (GameObject go:attackers) {
			if (!go.equals(context.Caster)) {
				attacked = true;
			}
		}
		if (attacked) {
			RealmLogging.logMessage(
					context.Caster.getName(),
					context.Spell.getGameObject().getName()+" was cancelled because the "
					+ context.Target.getGameObject().getName()
					+" is being attacked by someone other than the "+ context.Caster.getName()+"!");
		}
		else {
			combat.setPeace(true);
			context.Target.clearTarget();
			if (context.Target.isCharacter()) {
				// Cancel any cast spells
				GameObject go = combat.getCastSpell();
				if (go!=null) {
					SpellWrapper spell = new SpellWrapper(go);
					spell.expireSpell();
					RealmLogging.logMessage(
							spell.getCaster().getGameObject().getName(),
							spell.getGameObject().getName()+" was cancelled because of PEACE spell!");
				}
			}
		}
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub

	}

}
