package com.robin.magic_realm.components.effect;

import com.robin.general.swing.DieRoller;
import com.robin.magic_realm.components.table.Wish;
import com.robin.magic_realm.components.utility.DieRollBuilder;
import com.robin.magic_realm.components.utility.RealmLogging;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class SmallBlessingEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		CharacterWrapper character = context.getCharacterTarget();
		
		Wish wish = new Wish(context.Parent);
		DieRoller roller = DieRollBuilder.getDieRollBuilder(context.Parent,character, context.Spell.getRedDieLock()).createRoller(wish);
		roller.rollDice("Wish");
		String result = wish.apply(character,roller);
		
		RealmLogging.logMessage(context.Caster.getName(),"Wish roll: "+roller.getDescription());
		RealmLogging.logMessage(context.Caster.getName(),"Wish result: "+result);
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub

	}

}
