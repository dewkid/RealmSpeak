package com.robin.magic_realm.components.effect;

public class ExtraActionEffect implements ISpellEffect {
	String _action;
	
	public ExtraActionEffect(String action){
		_action = action;
	}
	
	@Override
	public void apply(SpellEffectContext context) {
		context
			.getCharacterTarget()
			.addSpellExtraAction(_action, context.Spell.getGameObject());	
	}

	@Override
	public void unapply(SpellEffectContext context) {
		context.getCharacterTarget().removeSpellExtraAction(_action);
	}

}
