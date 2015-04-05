package com.robin.magic_realm.components.effect;

import com.robin.magic_realm.components.TreasureCardComponent;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class EnchantEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		// Add the secondary target spell and chit type to the artifact
		SpellWrapper spellToAdd = new SpellWrapper(context.Spell.getSecondaryTarget());
		if (!spellToAdd.getGameObject().hasThisAttribute(Constants.ARTIFACT_ENHANCED_MAGIC)) {
			// First time this casting has energized, so make a copy of the requisite spell
			spellToAdd = spellToAdd.makeCopy();
			context.Spell.setSecondaryTarget(spellToAdd.getGameObject());
			spellToAdd.getGameObject().setThisAttribute(Constants.ARTIFACT_ENHANCED_MAGIC);
			spellToAdd.getGameObject().setThisAttribute(Constants.SPELL_AWAKENED);
		}
		context.Target.getGameObject().addThisAttributeListItem(Constants.ARTIFACT_ENHANCED_MAGIC,spellToAdd.getCastMagicType());
		context.Target.getGameObject().add(spellToAdd.getGameObject());
	
	}
	

	@Override
	public void unapply(SpellEffectContext context) {
		// Remove the secondary target spell and chit type from the artifact
		SpellWrapper spellToAdd = new SpellWrapper(context.Spell.getSecondaryTarget());
		context.Target.getGameObject().removeThisAttributeListItem(Constants.ARTIFACT_ENHANCED_MAGIC,spellToAdd.getCastMagicType());
		context.Target.getGameObject().remove(spellToAdd.getGameObject());
		if (context.Target.isEnchanted() && context.Target.isTreasure()) {
			TreasureCardComponent card = (TreasureCardComponent)context.Target;
			card.makeFatigued();
		}
	}

}
