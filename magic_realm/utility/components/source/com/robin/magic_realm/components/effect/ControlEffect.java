package com.robin.magic_realm.components.effect;

import java.util.ArrayList;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.ChitComponent;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.utility.RealmLogging;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.CombatWrapper;

public class ControlEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		GameObject caster = context.Caster;
		GameObject spellObj = context.Spell.getGameObject();
		CombatWrapper combat = context.getCombatTarget();
		RealmComponent target = context.Target;
		
			// Make sure none of the caster's hirelings are attacking the monster/native or this spell is cancelled for that monsters
			ArrayList<GameObject> attackers = combat.getAttackers();
			for (GameObject go:attackers) {
				if (!go.equals(caster)) {
					RealmComponent gorc = RealmComponent.getRealmComponent(go);
					if (gorc.getOwnerId().equals(caster.getStringId())) {
						RealmLogging.logMessage(
								caster.getName(),
								spellObj.getName()+" was cancelled because the "
								+caster.getName()+"'s hirelings are already attacking the "
								+target.getGameObject().getName());
						
						// Remove target manually
						ArrayList targetids = new ArrayList(context.Spell.getList("target_ids"));
						targetids.remove(target.getGameObject().getStringId());
						if (targetids.isEmpty()) {
							context.Spell.expireSpell();
						}
						else {
							context.Spell.setList("target_ids",targetids);
						}
						return;
					}
				}
			}
			
			// For now, clear the target, though this isn't totally right (see rule 45.5)
			target.clearTarget();
			if (target.isMonster() || target.isNative()) {
				ChitComponent chit = (ChitComponent)target;
				if (chit.isDarkSideUp()) { // Always flip to light side on control!
					chit.setLightSideUp();
				}
			}
			CharacterWrapper controlledMonster = new CharacterWrapper(target.getGameObject());
			controlledMonster.setPlayerName(context.Spell.getCaster().getPlayerName());
			controlledMonster.setWantsCombat(context.Spell.getCaster().getWantsCombat()); // same default
			target.setOwner(RealmComponent.getRealmComponent(context.Spell.getCaster().getGameObject()));
			combat.setSheetOwner(true);
			combat.setWatchful(false);
//			combat.removeAllAttackers();
		
	}

	@Override
	public void unapply(SpellEffectContext context) {
		context.Spell.getCaster().removeHireling(context.Target.getGameObject()); // this does all the work we need!
	}

}
