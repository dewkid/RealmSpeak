package com.robin.magic_realm.components.effect;

import com.robin.magic_realm.components.utility.SpellUtility;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class MoveSpeedChangeEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		if (context.Target.isCharacter()) {
			CharacterWrapper character = context.getCharacterTarget();
			
			character.getAllChits().stream()
				.filter(chit -> "MOVE".equals(chit.getAction()))
				.forEach(chit -> SpellUtility.setAlteredSpeed(chit, "strength", context.Spell));
		}
		else if (context.Target.isMonster() || context.Target.isNative()) {
			SpellUtility.setAlteredSpeed(context.Target, "vulnerability", context.Spell);
		}
	}

	@Override
	public void unapply(SpellEffectContext context) {
		if (context.Target.isCharacter()) {
			CharacterWrapper character = context.getCharacterTarget();
			
			character.getAllChits().stream()
				.filter(chit -> "MOVE".equals(chit.getAction()))
				.forEach(chit -> chit.getGameObject().removeThisAttribute("move_speed_change"));
		}
		else if (context.Target.isMonster() || context.Target.isNative()) {
			context.Target.getGameObject().removeThisAttribute("move_speed_change");
		}
	}

}
