package com.robin.magic_realm.components.effect;

import com.robin.general.swing.DieRoller;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.DieRollBuilder;
import com.robin.magic_realm.components.utility.RealmLogging;

public class PetrifyEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		int roll = rollResult(context);
		if(roll == 6) {
			//the target looks away and is unaffected
			context.Spell.expireSpell();
		} else {
			ISpellEffect transmorph = new TransmorphEffect("statue");
			transmorph.apply(context);
		}
	}
	
	private int rollResult(SpellEffectContext context){
		DieRoller roller = DieRollBuilder
				.getDieRollBuilder(context.Parent, context.Spell.getCaster(),context.Spell.getRedDieLock())
				.createRoller("petrify");
		
		int die = roller.getHighDieResult();
		int mod = context.Spell.getGameObject().getThisInt(Constants.SPELL_MOD);
		
		die += mod;
		if (die>=6) die=6;

		
		RealmLogging.logMessage(context.Spell.getCaster().getGameObject().getName(),"Petrify roll: "+roller.getDescription());
		return die;
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub
		ISpellEffect transmorph = new TransmorphEffect("statue");
		transmorph.unapply(context);
	}

}
