package com.robin.magic_realm.components.effect;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.RealmComponent;

public class FlyChitEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		// A Fly spell.  Create a Fly Chit
		GameObject spellObj = context.Spell.getGameObject();
		
		GameObject flyChit = spellObj.getGameData().createNewObject();
		flyChit.setName(spellObj.getName()+" Fly Chit ("+ context.Caster.getName()+")");
		flyChit.copyAttributeBlockFrom(spellObj,RealmComponent.FLY_CHIT);
		flyChit.renameAttributeBlock(RealmComponent.FLY_CHIT,"this");
		flyChit.setThisAttribute("spellID",spellObj.getStringId());
		flyChit.setThisAttribute("sourceSpell",spellObj.getName());
		spellObj.setThisAttribute("flyChitID",flyChit.getStringId());
		context.Target.getGameObject().add(flyChit);
		
		if (!context.Target.getGameObject().equals(context.Caster)) {
			RealmComponent flyChitRC = RealmComponent.getRealmComponent(flyChit);
			flyChitRC.setOwner(RealmComponent.getRealmComponent(context.Caster));
		}
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// A Fly spell.  Destroy the FLY Chit, and remove it from the target.
		GameObject spellObj = context.Spell.getGameObject();
		
		String chitId = spellObj.getThisAttribute("flyChitID");
		GameObject flyChit =spellObj.getGameData().getGameObject(Long.valueOf(chitId));
		context.Target.getGameObject().remove(flyChit);
		spellObj.removeThisAttribute("flyChitID");

	}

}
