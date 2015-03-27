package com.robin.magic_realm.components.effect;

import java.util.ArrayList;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.wrapper.CombatWrapper;

public class DisengageEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		CombatWrapper combat = context.getCombatTarget();
		
		// Remove all attackers and targets
		ArrayList<GameObject> attackers = combat.getAttackers();
		
		attackers.stream()
			.map(a -> RealmComponent.getRealmComponent(a))
			.forEach(rc -> rc.clearTarget());
	
		attackers.stream()
			.map(a -> new CombatWrapper(a))
			.filter(a -> a.getAttackerCount() > 0)
			.forEach(a -> a.setSheetOwner(true));
		
		combat.removeAllAttackers();
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub

	}

}
