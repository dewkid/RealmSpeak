package com.robin.magic_realm.components.effect;

import com.robin.magic_realm.components.utility.SpellUtility;

public class SummonEffect implements ISpellEffect {
	String _summonType;
	
	public SummonEffect(String type){
		_summonType = type;
	}
	
	@Override
	public void apply(SpellEffectContext context) {
		SpellUtility.summonRandomCompanions(
				context.Parent,
				context.Caster,
				context.getCharacterTarget(),
				context.Spell,
				_summonType);
	}

	@Override
	public void unapply(SpellEffectContext context) {
		SpellUtility.unsummonCompanions(context.Spell);
	}

}
