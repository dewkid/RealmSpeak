package com.robin.magic_realm.components.effect;

import com.robin.general.swing.DieRoller;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.DieRollBuilder;
import com.robin.magic_realm.components.utility.RealmLogging;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class ViolentStormEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		// Need to roll on the Violent Storm table...
		CharacterWrapper castingCharacter = context.Spell.getCaster();
		
		DieRoller roller = DieRollBuilder.getDieRollBuilder(context.Parent,castingCharacter,context.Spell.getRedDieLock()).createRoller("ViolentStorm");
		roller.rollDice("Violent Storm");
		int phasesLost;
		int t = roller.getHighDieResult();
		if (t<=1) {
			phasesLost = 4;
		}
		else if (t<=3) {
			phasesLost = 3;
		}
		else if (t<=5) {
			phasesLost = 2;
		}
		else {
			phasesLost = 1;
		}
		RealmLogging.logMessage(context.Caster.getName(),"Violent Storm roll: "+roller.getDescription());
		RealmLogging.logMessage(context.Caster.getName(),"Violent Storm result: "+phasesLost+" phase"+(phasesLost==1?"":"s")+" lost on entry");
		context.Target.getGameObject().setThisAttribute(Constants.SP_STORMY,phasesLost);
	}

	@Override
	public void unapply(SpellEffectContext context) {
		context.Target.getGameObject().removeThisAttribute(Constants.SP_STORMY);
	}

}
