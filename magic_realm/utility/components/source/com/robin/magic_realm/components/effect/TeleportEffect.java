package com.robin.magic_realm.components.effect;

import com.robin.magic_realm.components.utility.SpellUtility;
import com.robin.magic_realm.components.utility.SpellUtility.TeleportType;

public class TeleportEffect implements ISpellEffect {
	String _teleportType;
	
	public TeleportEffect(String type){
		_teleportType = type;
	}
	
	@Override
	public void apply(SpellEffectContext context) {
		SpellUtility.doTeleport(context.Parent,
				context.Spell.getGameObject().getName(),
				context.getCharacterTarget(),
				TeleportType.valueOf(_teleportType));
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub

	}

}
