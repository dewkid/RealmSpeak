package com.robin.magic_realm.components.effect;

public class ApplyClearingEffect implements ISpellEffect {
	String _effect;
	
	public ApplyClearingEffect(String effect){
		_effect = effect;
	}
	
	@Override
	public void apply(SpellEffectContext context) {
		context.getClearingTarget().addSpellEffect(_effect);
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub
		context.getClearingTarget().removeSpellEffect(_effect);
	}

}
