package com.robin.magic_realm.components.effect;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.DieRollReporter;
import com.robin.magic_realm.components.utility.RollResult;
import com.robin.magic_realm.components.utility.SpellUtility;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class FilcherEffect implements ISpellEffect {	
	private boolean oneTime;
	
	@Override
	public void apply(SpellEffectContext context) {
		if(oneTime)return;
		
		String nativeGroup = context.Target.getGameObject().getThisAttribute("native");
		CharacterWrapper cc = new CharacterWrapper(context.Caster);
		String msg;
		
		RollResult result = SpellUtility.rollResult(context, "Filcher");
		
		//TEST
		result.roll = 5;
		
		switch(result.roll){
			case 1:
			case 2:
			case 3:
			case 4:
				//
				msg = "I have not yet implemented this yet!!!";
				DieRollReporter.showMessageDialog(result.roller, context.Parent, "Filcher", msg, JOptionPane.INFORMATION_MESSAGE);
				break;
				
			case 5:
				//Suspect -- lose 1 friendliness with group
				cc.changeRelationship(Constants.GAME_RELATIONSHIP, nativeGroup, -1, false);
				msg = "You fail to steal anything and the " + nativeGroup + " are suspicious of you.";
				DieRollReporter.showMessageDialog(result.roller, context.Parent, "Filcher", msg, JOptionPane.INFORMATION_MESSAGE);
				break;
			case 6:
				//Caught, you are enemies with the native group
				cc.changeRelationship(Constants.GAME_RELATIONSHIP, nativeGroup, 0, true);
				msg = "You are caught red-handed by the " + nativeGroup + " and they are now your enemy!";
				DieRollReporter.showMessageDialog(result.roller, context.Parent, "Filcher", msg, JOptionPane.INFORMATION_MESSAGE);
				
				context.Spell.getTargets().stream().forEach(n -> cc.addBattlingNative(n.getGameObject()));
				break;
		}
		
		oneTime = true; //don't run through this for each native in the group
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub

	}
	
	public static ISpellEffect create(){
		return new FilcherEffect();
	}

}
