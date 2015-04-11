package com.robin.magic_realm.components.effect;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;

import com.robin.game.objects.GameObject;
import com.robin.general.util.Extensions;
import com.robin.magic_realm.components.attribute.RelationshipType;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.SetupCardUtility;
import com.robin.magic_realm.components.utility.SpellUtility;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.GameWrapper;

public class SummonAidEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		CharacterWrapper character = context.getCharacterTarget();
		
		//This spell requires 1 gold as a "sacrifice"
		if(character.getGold() < 1){
			context.Spell.expireSpell();
			return;
		}
		
		character.setGold(character.getGold() - 1);

		//find all possible groups to summon from
		ArrayList<String>friends = character.getRelationshipList(Constants.GAME_RELATIONSHIP, RelationshipType.FRIENDLY);
		ArrayList<String>allies = character.getRelationshipList(Constants.GAME_RELATIONSHIP, RelationshipType.ALLY);
		Predicate<GameObject>notdead = go -> {
				CharacterWrapper cw = new CharacterWrapper(go);
				return !cw.isDead() && cw.getCurrentTile() != null;
		};
		
		//find a guy to summon
		Optional<GameObject>buddy = Extensions.coalesce(
				SpellUtility.findNativeFromTheseGroups(allies, notdead, context.Game),
				SpellUtility.findNativeFromTheseGroups(friends, notdead, context.Game));
		
		if(!buddy.isPresent()){
			//no one to summon
			context.Spell.expireSpell();
			return;
		}
		
		context.Spell.getGameObject().setThisAttribute("SummonedNative", buddy.get().getStringId());
		
		//bring the summon to you
		SpellUtility.bringSummonToClearing(character, buddy.get(), context.Spell, null);
	}

	@Override
	public void unapply(SpellEffectContext context) {
		//this is a hack -- context.Game is null now -- we need to pass it, but that means changing 50 call sites -- cjm
		GameWrapper theGame = GameWrapper.findGame(context.Spell.getGameData());
		
		
		long id = Long.parseLong(context.Spell.getGameObject().getThisAttribute("SummonedNative"));
		
		GameObject buddy = theGame.getGameData().getGameObjects().stream()
							.filter(go -> go.equalsId(id))
							.findFirst()
							.get();
		
		String nativeGroup = buddy.getThisAttribute("native");
		
		CharacterWrapper cw = new CharacterWrapper(buddy);
		CharacterWrapper casterCharacter = new CharacterWrapper(context.Caster);
		
		if(cw.isDead()){
			casterCharacter.changeRelationship(Constants.GAME_RELATIONSHIP, nativeGroup, -1, false);
		} else
		{
			casterCharacter.removeHireling(buddy);
			SetupCardUtility.resetDenizen(buddy);
		}
		

		
	}

}
