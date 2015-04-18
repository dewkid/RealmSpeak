package com.robin.magic_realm.components.effect;

import com.robin.magic_realm.components.utility.SpellUtility;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class MakeWholeEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		CharacterWrapper character = context.getCharacterTarget();
		SpellUtility.heal(character);
		SpellUtility.repair(character);
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub

	}

}
