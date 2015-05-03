package com.robin.magic_realm.components.effect;

import java.util.ArrayList;
import com.robin.magic_realm.components.MonsterChitComponent;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.SpellUtility;
import com.robin.magic_realm.components.wrapper.CombatWrapper;

public class PacifyEffect implements ISpellEffect {
	int _level;
	
	public PacifyEffect(int level)
	{
		_level = level;
	}
	
	@Override
	public void apply(SpellEffectContext context) {
		CombatWrapper combat = context.getCombatTarget();
		RealmComponent target = context.Target;
		
		if(SpellUtility.TargetsAreBeingAttackedByHirelings(combat.getAttackers(), context.Caster)){
			context.Spell.expireSpell();
			return;
		}
	
		String pacifyBlock = Constants.PACIFY+ context.Spell.getGameObject().getStringId();
		target.getGameObject().addAttributeListItem("this","pacifyBlocks",pacifyBlock);	
		target.getGameObject().setAttribute(pacifyBlock,"pacifyType",_level);
		target.getGameObject().setAttribute(pacifyBlock,"pacifyChar", context.Spell.getCaster().getGameObject().getStringId());
		
		// Shouldn't clear target unless the target is the caster!
		RealmComponent targetTarget = target.getTarget();
		if (targetTarget!=null && targetTarget.getGameObject().equals(context.Spell.getCaster().getGameObject())) {
			target.clearTarget();
		}
		
		// If you made them watchful, make them unwatchful again
		combat.setWatchful(false);
		
		if (target.isMonster()) {
			MonsterChitComponent monster = (MonsterChitComponent)target;
			if (monster.isDarkSideUp()) { // Always flip to light side on control!
				monster.setLightSideUp();
			}
		}
	}

	@Override
	public void unapply(SpellEffectContext context) {
		String pacifyBlock = Constants.PACIFY+ context.Spell.getGameObject().getStringId();
		
		ArrayList inlist = context.Target.getGameObject().getAttributeList("this","pacifyBlocks");
		
		if (inlist!=null) { // might be null if the spell was cancelled partway through
			ArrayList<String> list = new ArrayList<String>(inlist);
			list.remove(pacifyBlock);
			if (list.isEmpty()) {
				context.Target.getGameObject().removeThisAttribute("pacifyBlocks");
			}
			else {
				context.Target.getGameObject().setThisAttributeList("pacifyBlocks",list);
			}
		}
		context.Target.getGameObject().removeAttributeBlock(pacifyBlock);
	}

}
