package com.robin.magic_realm.components.effect;

public interface ISpellEffect {
	void apply(SpellEffectContext context);
	void unapply(SpellEffectContext context);
}
