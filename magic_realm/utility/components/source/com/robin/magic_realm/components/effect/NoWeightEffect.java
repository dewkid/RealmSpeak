package com.robin.magic_realm.components.effect;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class NoWeightEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		// TODO Auto-generated method stub
		//CJM -- this is checked during getWeight()
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// Make sure character inventory can handle the new weight that results from losing the NO_WEIGHT effect
		GameObject heldBy = context.Target.getGameObject().getHeldBy();
		if (heldBy.hasThisAttribute("character")) { // possible to have anything else?... yes a leader or controlled monster...
			CharacterWrapper character = new CharacterWrapper(heldBy);
			character.setNeedsInventoryCheck(true);
		}
	}

}
