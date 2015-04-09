package com.robin.magic_realm.components.effect;

import java.util.ArrayList;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.utility.MonsterCreator;
import com.robin.magic_realm.components.utility.SpellUtility;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.CombatWrapper;

public class SummonFairyEffect implements ISpellEffect {
//TODO: I really need to take a look at the summon effect to see if I can generalize -- cjm
	
	
	@Override
	public void apply(SpellEffectContext context) {
		// TODO Auto-generated method stub
		CharacterWrapper character = context.getCharacterTarget();
		GameData data = character.getGameObject().getGameData();
		MonsterCreator creator = new MonsterCreator("summoned_fairy");
		GameObject summon = creator.createOrReuseMonster(data);
		
		creator.setupGameObject(summon, "Fairy", "fairy", "L", false);
		creator.setupSide(summon, "light", null, 0, 0, 0, 1, "lightgreen");
		creator.setupSide(summon, "dark", null, 0, 0, 0, 1, "forestgreen");
		
		SpellUtility.bringSummonToClearing(character, summon, context.Spell, creator.getMonstersCreated());
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub
		SpellUtility.unsummonCompanions(context.Spell);
	}

}
