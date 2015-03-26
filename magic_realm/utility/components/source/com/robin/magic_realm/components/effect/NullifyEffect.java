package com.robin.magic_realm.components.effect;

import com.robin.magic_realm.components.wrapper.SpellMasterWrapper;

public class NullifyEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		if (context.Target.isCharacter()) {
			context.getCharacterTarget().nullifyCurses();
		}
		
		SpellMasterWrapper sm = SpellMasterWrapper.getSpellMaster(context.Spell.getGameObject().getGameData());
		sm.nullifyBewitchingSpells(context.Target.getGameObject(),context.Spell);

	}

	@Override
	public void unapply(SpellEffectContext context) {	
		if (context.Target.isCharacter()) {
			context.getCharacterTarget().restoreCurses();
		}
		
		SpellMasterWrapper sm = SpellMasterWrapper.getSpellMaster(context.Spell.getGameObject().getGameData());
		sm.restoreBewitchingNullifiedSpells(context.Target.getGameObject(),context.Spell);
	}
}
