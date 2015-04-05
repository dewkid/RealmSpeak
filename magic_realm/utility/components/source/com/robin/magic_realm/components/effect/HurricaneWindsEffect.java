package com.robin.magic_realm.components.effect;

import com.robin.magic_realm.components.utility.Constants;

public class HurricaneWindsEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		context.Target.getGameObject().setThisAttribute(Constants.BLOWS_TARGET, context.Spell.getGameObject().getStringId());
	}

	@Override
	public void unapply(SpellEffectContext context) {
		context.Target.getGameObject().removeThisAttribute(Constants.BLOWS_TARGET);
	}

}
