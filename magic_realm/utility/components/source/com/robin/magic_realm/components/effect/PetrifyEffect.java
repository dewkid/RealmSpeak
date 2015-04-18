package com.robin.magic_realm.components.effect;

import javax.swing.JOptionPane;

import com.robin.magic_realm.components.utility.DieRollReporter;
import com.robin.magic_realm.components.utility.RollResult;
import com.robin.magic_realm.components.utility.SpellUtility;

public class PetrifyEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		RollResult result = SpellUtility.rollResult(context, "Petrify");
		
		if(result.roll == 6) {
			//the target looks away and is unaffected
			String msg = "The " + context.Target.getName() + " looks away.";
			DieRollReporter.showMessageDialog(result.roller, context.Parent, "Petrify", msg, JOptionPane.INFORMATION_MESSAGE);
			context.Spell.expireSpell();
		} else {	
			String msg = "The " + context.Target.getName() + " is turned into a statue.";
			DieRollReporter.showMessageDialog(result.roller, context.Parent, "Petrify", msg, JOptionPane.INFORMATION_MESSAGE);
			ISpellEffect transmorph = new TransmorphEffect("statue");
			transmorph.apply(context);
		}
	}
	


	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub
		ISpellEffect transmorph = new TransmorphEffect("statue");
		transmorph.unapply(context);
	}

}
