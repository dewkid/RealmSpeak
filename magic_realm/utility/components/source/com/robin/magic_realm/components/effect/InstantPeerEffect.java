package com.robin.magic_realm.components.effect;

public class InstantPeerEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		context.getCharacterTarget().setDoInstantPeer(true);
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub

	}

}
