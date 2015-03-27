package com.robin.magic_realm.components.effect;

import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class DiscoverRoadEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		CharacterWrapper character = context.getCharacterTarget();
	
		character
			.getCurrentClearing()
			.getConnectedPaths()
			.forEach(path -> character.updatePathKnowledge(path));
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub

	}

}
