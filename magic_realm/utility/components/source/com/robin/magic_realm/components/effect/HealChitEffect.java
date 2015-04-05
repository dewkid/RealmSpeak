package com.robin.magic_realm.components.effect;

import com.robin.magic_realm.components.CharacterActionChitComponent;
import com.robin.magic_realm.components.RealmComponent;

public class HealChitEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		for(RealmComponent rc: context.Spell.getTargets()) {
			CharacterActionChitComponent chit = (CharacterActionChitComponent)rc;
			chit.makeActive();
		}
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub

	}

}
