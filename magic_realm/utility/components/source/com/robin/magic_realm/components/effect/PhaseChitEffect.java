package com.robin.magic_realm.components.effect;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.utility.SpellUtility;

public class PhaseChitEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		SpellUtility.createPhaseChit(context.Target,context.Spell.getGameObject());
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub
		// A Phase spell.  Ditch the phase chit.
		GameObject phaseChit = context.Spell
				.getGameObject()
				.getGameData()
				.getGameObject(Long.valueOf(context.Spell.getGameObject().getThisAttribute("phaseChitID")));
		context.getCharacterTarget().getGameObject().remove(phaseChit);
		context.Spell.getGameObject().removeThisAttribute("phaseChitID");
	}

}
