package com.robin.magic_realm.components.effect;

import com.robin.magic_realm.components.CharacterActionChitComponent;

public class ActionChangeEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		CharacterActionChitComponent chit = (CharacterActionChitComponent)context.Target;
		
		String speedKey = "s"+chit.getGameObject().getThisAttribute("speed"); // use raw speed - ignores speed changes by treasures
		String strength = context.Spell.getGameObject().getThisAttribute(speedKey);
		if (strength==null) {
			throw new IllegalStateException("Undefined action_change_str!");
		}
		chit.getGameObject().setThisAttribute("action_change","M/F");
		chit.getGameObject().setThisAttribute("action_change_str",strength);
		context.Spell.getCaster().updateChitEffects();
	}

	@Override
	public void unapply(SpellEffectContext context) {
		context.Target.getGameObject().removeThisAttribute("action_change");
		context.Target.getGameObject().removeThisAttribute("action_change_str");
	}

}
