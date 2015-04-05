package com.robin.magic_realm.components.effect;

import com.robin.magic_realm.components.CharacterActionChitComponent;
import com.robin.magic_realm.components.attribute.ColorMagic;

public class ForcedEnchantEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		// TODO Auto-generated method stub
		CharacterActionChitComponent chit = (CharacterActionChitComponent)context.Target;
		String change = context.Spell.getGameObject().getThisAttribute(chit.getMagicType());
		if (change==null) {
			throw new IllegalStateException("Undefined chit_change!");
		}
		chit.enchant(ColorMagic.makeColorMagic(change,true).getColorNumber());
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub
		CharacterActionChitComponent chit = (CharacterActionChitComponent)context.Target;
		if (chit.isColor()) {
			// Not sure this is what Deric Page wanted, but it's consistent
			chit.makeFatigued();
		}
	}

}
