package com.robin.magic_realm.components.effect;

import com.robin.magic_realm.components.ClearingDetail;
import com.robin.magic_realm.components.utility.Constants;

public class ExtraCavePhaseEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		// TODO Auto-generated method stub
		ClearingDetail clearing = context.Spell.getTargetAsClearing(context.Target);
		clearing.addFreeAction(Constants.EXTRA_CAVE_PHASE, context.Spell.getGameObject());
	}

	@Override
	public void unapply(SpellEffectContext context) {
		ClearingDetail clearing = context.Spell.getTargetAsClearing(context.Target);
		clearing.removeFreeAction(Constants.EXTRA_CAVE_PHASE);
	}

}
