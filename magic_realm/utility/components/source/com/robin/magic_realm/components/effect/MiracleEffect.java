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
		//there was no one to rez and the spell has no effect
		if(oneTime){return;}

		//first time through -- check to see if someone here is dead
		CombatWrapper combat = context.getCombatTarget();

		if(SpellUtility.TargetsAreBeingAttackedByHirelings(combat.getAttackers(), context.Caster)){
			context.Spell.expireSpell();
			return;
		}
		
		String nativeGroup = context.Target.getGameObject().getThisAttribute("native");
		Predicate<GameObject>isdead = go -> {return new CharacterWrapper(go).isDead();};
		
		Optional<GameObject> deadBuddy = SpellUtility.findNativeFromTheseGroups(nativeGroup, isdead, context.Game);
		
		//CJM -- this is pretty ugly
		if(deadBuddy.isPresent()){
			SetupCardUtility.resetDenizen(deadBuddy.get());

			//pacify the denizen that was the original first target of the spell
//			PacifyEffect pacify = new PacifyEffect(1);
//			pacify.apply(context);
			
			//pacify the newly raised denizen
//			SpellEffectContext newContext = new SpellEffectContext(context.Parent, context.Game, RealmComponent.getRealmComponent(deadBuddy.get()), context.Spell, context.Caster);
//			pacify.apply(newContext);
			
			CharacterWrapper cc = new CharacterWrapper(context.Caster);
			cc.changeRelationship(Constants.GAME_RELATIONSHIP, nativeGroup, 1, true);
			
		}
		
		oneTime = true;
	}

	@Override
	public void unapply(SpellEffectContext context) {

	}

}
