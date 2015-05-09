package com.robin.magic_realm.components.effect;

import java.util.Optional;
import java.util.function.Predicate;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.SetupCardUtility;
import com.robin.magic_realm.components.utility.SpellUtility;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.CombatWrapper;

public class MiracleEffect implements ISpellEffect {
	boolean oneTime;
	
	@Override
	public void apply(SpellEffectContext context) {	
		//CJM -- required because spell effects fire for EACH target of the spell, which in this case is all of
		//the members in the native group. We only want this spell to fire ONCE for the entire group. FilcherEffect
		//also uses this slightly crappy pattern.
		if(oneTime){return;} 

		//first time through -- check to see if someone here is dead
		CombatWrapper combat = context.getCombatTarget();

		if(SpellUtility.targetsAreBeingAttackedByHirelings(combat.getAttackers(), context.Caster)){
			context.Spell.expireSpell();
			return;
		}
		
		String nativeGroup = context.Target.getGameObject().getThisAttribute("native");
		Predicate<GameObject>isdead = go -> {return new CharacterWrapper(go).isDead();};
		
		Optional<GameObject> deadBuddy = SpellUtility.findNativeFromTheseGroups(nativeGroup, isdead, context.Game);
		
		if(deadBuddy.isPresent()){
			SetupCardUtility.resetDenizen(deadBuddy.get());		
			CharacterWrapper cc = new CharacterWrapper(context.Caster);
			cc.changeRelationship(Constants.GAME_RELATIONSHIP, nativeGroup, 1, true);
			
		}
		
		oneTime = true;
	}

	@Override
	public void unapply(SpellEffectContext context) {

	}

}
