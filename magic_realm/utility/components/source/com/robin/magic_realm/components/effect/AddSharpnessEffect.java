package com.robin.magic_realm.components.effect;

import com.robin.magic_realm.components.utility.Constants;

public class AddSharpnessEffect implements ISpellEffect {
	
	private int magnatude;

	public AddSharpnessEffect(int mag){
		magnatude = mag;
	}

	
	@Override
	public void apply(SpellEffectContext context) {
		int val = context.Target.getGameObject().getThisInt(Constants.ADD_SHARPNESS) + magnatude;
		context.Target.getGameObject().setThisAttribute(Constants.ADD_SHARPNESS,val);
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// Decrement sharpness by one
		int val = context.Target.getGameObject().getThisInt(Constants.ADD_SHARPNESS) - magnatude;
		
		if (val==0) {
			context.Target.getGameObject().removeThisAttribute(Constants.ADD_SHARPNESS);
		}
		else {
			context.Target.getGameObject().setThisAttribute(Constants.ADD_SHARPNESS,val);
		}
	}

}
