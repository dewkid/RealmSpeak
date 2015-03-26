package com.robin.magic_realm.components.effect;

import com.robin.general.swing.DieRoller;
import com.robin.magic_realm.components.table.Curse;
import com.robin.magic_realm.components.utility.DieRollBuilder;
import com.robin.magic_realm.components.utility.RealmLogging;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class CurseEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		CharacterWrapper character = context.getCharacterTarget();
		
		Curse curse = new Curse(context.Parent);
		DieRoller roller = DieRollBuilder.getDieRollBuilder(context.Parent,character,context.Spell.getRedDieLock()).createRoller(curse);
		roller.rollDice("Curse");
		String result = curse.apply(character,roller);
		RealmLogging.logMessage(context.Caster.getName(),"Curse roll: "+roller.getDescription());
		RealmLogging.logMessage(context.Caster.getName(),"Curse result: "+result);

	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub

	}

}
