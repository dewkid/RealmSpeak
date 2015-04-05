package com.robin.magic_realm.components.effect;

import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class CancelEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		if (context.Target.isCharacter()) {
			String curse = context.Spell.getExtraIdentifier();
			context.getCharacterTarget().removeCurse(curse);
		}
		else {
			// Target is a spell
			SpellWrapper spell = new SpellWrapper(context.Target.getGameObject());
			spell.expireSpell();
		}
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub

	}

}
