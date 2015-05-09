package com.robin.magic_realm.components.effect;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.CharacterActionChitComponent;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.CombatWrapper;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class ExorciseEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		CombatWrapper combat = context.getCombatTarget();
		
		if (context.Target.getGameObject().hasThisAttribute("demon")) {
			combat.setKilledBy(context.Caster);
		}
		else if (context.Target.isCharacter()) {
			CharacterWrapper targChar = new CharacterWrapper(context.Target.getGameObject());
			
			// Cancel Spellcasting (do NOT include this spell!!)
			GameObject castSpell = combat.getCastSpell();
			if (castSpell!=null && !castSpell.equals(context.Spell.getGameObject())) {
				SpellWrapper otherSpell = new SpellWrapper(castSpell);
				otherSpell.expireSpell();
			}
			
			// Cancel curses
			targChar.removeAllCurses();
			
			// Fatigue Color Chits
			targChar.getColorChits().stream()
				.map(c -> (CharacterActionChitComponent)c)
				.forEach(chit -> chit.makeFatigued());
		}
		else if (context.Target.isSpell()) {
			SpellWrapper otherSpell = new SpellWrapper(context.Target.getGameObject());
			otherSpell.expireSpell();
		}
		else {
			System.out.println("Invalid target?");
		}

	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub

	}

}
