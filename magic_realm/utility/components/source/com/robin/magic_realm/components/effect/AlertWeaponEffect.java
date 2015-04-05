package com.robin.magic_realm.components.effect;

import com.robin.magic_realm.components.WeaponChitComponent;

public class AlertWeaponEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		// TODO Auto-generated method stub
		WeaponChitComponent weapon = (WeaponChitComponent)context.Target;
		if (!weapon.isAlerted()) {
			weapon.setAlerted(true);
		}
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub

	}

}
