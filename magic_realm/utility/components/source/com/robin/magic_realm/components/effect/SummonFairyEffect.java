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
		
		TileLocation tl = character.getCurrentLocation();
		character.addHireling(summon);
		CombatWrapper combat = new CombatWrapper(summon);
		combat.setSheetOwner(true);
		if (tl!=null && tl.isInClearing()) {
			tl.clearing.add(summon,null);
		}
		character.getGameObject().add(summon); // so that you don't have to assign as a follower right away
		
		ArrayList list = context.Spell.getGameObject().getThisAttributeList("created");
		if (list==null) {
			list = new ArrayList();
		}
		for(GameObject go:creator.getMonstersCreated()) {
			list.add(go.getStringId());
		}
		context.Spell.getGameObject().setThisAttributeList("created",list);
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub
		SpellUtility.unsummonCompanions(context.Spell);
	}

}
