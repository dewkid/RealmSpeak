package com.robin.magic_realm.components.effect;

import com.robin.magic_realm.components.wrapper.CombatWrapper;

public class UnassignEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		// TODO Auto-generated method stub
		context.Target.clearTarget();
		CombatWrapper aCombat = new CombatWrapper(context.Target.getGameObject());
		aCombat.setSheetOwner(false);
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub

	}

}
