package com.robin.magic_realm.components.effect;

import com.robin.magic_realm.components.ArmorChitComponent;

public class RepairEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		ArmorChitComponent armor = (ArmorChitComponent)context.Target;
		armor.setIntact(true);
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub

	}

}
