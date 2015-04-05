package com.robin.magic_realm.components.effect;

import com.robin.magic_realm.components.CharacterActionChitComponent;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.SpellMasterWrapper;

public class ChitChangeEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		CharacterActionChitComponent chit = (CharacterActionChitComponent)context.Target;
		String change = context.Spell.getGameObject().getThisAttribute(chit.getMagicType());
		if (change==null) {
			throw new IllegalStateException("Undefined chit_change!");
		}
		chit.getGameObject().setThisAttribute(Constants.MAGIC_CHANGE,change);
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub
		CharacterActionChitComponent chit = (CharacterActionChitComponent)context.Target;
		if (chit.isColor()) {
			// If the converted chit was enchanted, it fatigues at the end of the spell (Rule 43.5)
			chit.makeFatigued();
		}
		// BUG 1554 - If committed to a spell, spells need to end here
		SpellMasterWrapper sm = SpellMasterWrapper.getSpellMaster(context.Spell.getGameObject().getGameData());
		sm.expireIncantationSpell(chit.getGameObject());
		context.Target.getGameObject().removeThisAttribute(Constants.MAGIC_CHANGE);
	}

}
