package com.robin.magic_realm.components.effect;
import com.robin.magic_realm.components.utility.SpellUtility;

public class ApplyNamedEffect implements ISpellEffect {
	String _effectName;
	
	public ApplyNamedEffect(String effectName){
		_effectName = effectName;
	}
	
	@Override
	public void apply(SpellEffectContext context) {
		SpellUtility.ApplyNamedSpellEffectToTarget(_effectName, context.Target.getGameObject(), context.Spell);
	}

	@Override
	public void unapply(SpellEffectContext context) {
		if(context.Target.getGameObject().hasThisAttribute(_effectName)){
			context.Target.getGameObject().removeThisAttribute(_effectName);
		}
	}

}
