package com.robin.magic_realm.components.effect;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.table.Loot;
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
		String dwellingName = context.Target.getGameObject().getThisAttribute("setup_start");
		
		CharacterWrapper cc = new CharacterWrapper(context.Caster);
		String msg;
		
		RollResult result = SpellUtility.rollResult(context, "Filcher");
		
		switch(result.roll){
			case 1:
			case 2:
			case 3:
			case 4: //success, you get an item from the natives	
				stealFromDwelling(context, nativeGroup, dwellingName, cc, result, false);
				break;
				
			case 5: //Suspect -- lose 1 friendliness with group, but you still get a roll to steal
				cc.changeRelationship(Constants.GAME_RELATIONSHIP, nativeGroup, -1, false);
				stealFromDwelling(context, nativeGroup, dwellingName, cc, result, true);
				break;
				
			case 6: //Caught, you are enemies with the native group
				cc.changeRelationship(Constants.GAME_RELATIONSHIP, nativeGroup, 0, true);
				msg = "You are caught red-handed by the " + nativeGroup + " and they are now your enemy!";
				DieRollReporter.showMessageDialog(result.roller, context.Parent, "Filcher", msg, JOptionPane.INFORMATION_MESSAGE);
				
				context.Spell.getTargets().stream().forEach(n -> cc.addBattlingNative(n.getGameObject()));
				break;
		}
		
		oneTime = true; //don't run through this for each native in the group
	}

	private void stealFromDwelling(SpellEffectContext context, String nativeGroup, String dwellingName, CharacterWrapper cc, RollResult result, boolean suspicious) {
		String msg;
		GameObject dwelling = context.Game.getGameData().getGameObjectByName(dwellingName);
		
		ArrayList<GameObject>stuff = dwelling.getHoldAsGameObjects().stream()
			.filter(go -> RealmComponent.getRealmComponent(go).isItem())
			.filter(go -> !RealmComponent.getRealmComponent(go).isHorse())
			.collect(Collectors.toCollection(ArrayList::new));
		
		RollResult stealRoll = SpellUtility.rollResult(context, "Steal");
		Optional<GameObject> stolenItem = stuff.stream().skip(stealRoll.roll - 1).findFirst();
		
		String suspiciousMsg = suspicious 
				? ", but they become suspicious"
				: "";
				
		if(stolenItem.isPresent()){
			msg = "You stole the " + stolenItem.get().getName() + " from the " + nativeGroup + suspiciousMsg + ".";
			Loot.addItemToCharacter(context.Parent, null, cc, stolenItem.get());
		} else {
			msg = "You stole 5 gold from the " + nativeGroup + suspiciousMsg + ".";
			cc.addGold(5);
		}
					
		DieRollReporter.showMessageDialog(result.roller, context.Parent, "Filcher", msg, JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub

	}
}
