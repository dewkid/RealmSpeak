package com.robin.magic_realm.components.effect;

public class FinalChitSpeedEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		// TODO Auto-generated method stub
		context.Spell.getCaster().updateChitEffects();
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub

	}

}
