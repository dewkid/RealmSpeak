package com.robin.magic_realm.components.effect;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.general.swing.DieRoller;
import com.robin.general.swing.FrameManager;
import com.robin.general.swing.IconGroup;
import com.robin.magic_realm.components.MonsterChitComponent;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.DieRollBuilder;
import com.robin.magic_realm.components.utility.RealmLogging;
import com.robin.magic_realm.components.utility.TreasureUtility;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.CombatWrapper;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

public class TransmorphEffect implements ISpellEffect {
	String transmorph;
	
	public TransmorphEffect(String type){
		transmorph = type;
	}
	
	@Override
	public void apply(SpellEffectContext context) {
		RealmComponent target = context.Target;
		SpellWrapper spell = context.Spell;
		CombatWrapper combat = context.getCombatTarget();
		
		if ("target".equals(transmorph)) {
			doTransmorphTarget(target, spell, combat);
		}
		else if ("statue".equals(transmorph)){
			GameObject transformStatue = prepareTransformation("statue", target, spell, context.Parent);
			doActualTransformation(target, spell, combat, transformStatue);
		}
		else if ("roll".equals(transmorph) || "mist".equals(transmorph)) {
			GameObject transformAnimal = spell.getTransformAnimal();
			
			// In this case, the target is the one that gets transformed
			if (transformAnimal==null) {
				String transformBlock;
				DieRoller roller = null;
				if ("roll".equals(transmorph)) {
					// hasn't rolled yet
					roller = DieRollBuilder.getDieRollBuilder(context.Parent,spell.getCaster(),spell.getRedDieLock()).createRoller("transform");
					int die = roller.getHighDieResult();
					int mod = spell.getGameObject().getThisInt(Constants.SPELL_MOD);
					die += mod;
					if (die<1) die=1;
					if (die>6) die=6;
					transformBlock = "roll"+die;
					
					RealmLogging.logMessage(spell.getCaster().getGameObject().getName(),"Transform roll: "+roller.getDescription());
				}
				else {
					transformBlock = "mist";
				}
				
				transformAnimal = prepareTransformation(transformBlock, target, spell, context.Parent);
			} 
			
			doActualTransformation(target, spell, combat, transformAnimal);
		}
		
		if (spell.getCaster().isTransmorphed()) {
			// Cancel combat spell, if any, and then only if the cast spell is NOT THIS one!
			CombatWrapper casterCombat = new CombatWrapper(context.Caster);
			GameObject cast = casterCombat.getCastSpell();
			if (cast!=null && !cast.equals(spell.getGameObject())) {
				casterCombat.clearCastSpell();
			}
		}
	}
	
	@Override
	public void unapply(SpellEffectContext context) {
		RealmComponent target = context.Target;
		SpellWrapper spell = context.Spell;

		if ("target".equals(transmorph)) {
			spell.getCaster().setTransmorph(null);
			
			ArrayList<GameObject> inv = spell.getCaster().getInventory();
			
			// Restore active state of items
			GameData data = spell.getGameObject().getGameData();
			ArrayList list = spell.getList(Constants.ACTIVATED_ITEMS);
			
			if (list!=null) {
				for (Iterator i=list.iterator();i.hasNext();) {
					String id = (String)i.next();
					GameObject go = data.getGameObject(Long.valueOf(id));
					if (go!=null && inv.contains(go)) { // only do this if the item still exists in inventory
						TreasureUtility.doActivate(null, spell.getCaster(),go,new ChangeListener() {
							public void stateChanged(ChangeEvent ev) {
								// does nothing - is that okay here?
							}
						},false);
					}
				}
			
				// Clear the list
				spell.setBoolean(Constants.ACTIVATED_ITEMS,false);
			}
		}
		else if ("roll".equals(transmorph) || "mist".equals(transmorph) || "statue".equals(transmorph)) {
			if (target.isCharacter()) {
				CharacterWrapper character = new CharacterWrapper(target.getGameObject());
				character.setTransmorph(null);
			}
			else {
				if (!target.getGameObject().hasAttributeBlock("this_h")) {
					// This is not good!!  Stop here....
					// This happens when casting Pentangle on a Transformed native - putting a return here fixes the problem.
					return;
				}
				// Copy any SPOILS tags (otherwise they get lost!)	
				for (Iterator i=target.getGameObject().getThisAttributeBlock().keySet().iterator();i.hasNext();) {
					String key = (String)i.next();
					if (key.startsWith(Constants.SPOILS_)) {
						target.getGameObject().setAttribute("this_h", key);
					}
				}
				
				// preserve the clearing, so that the monster doesn't return to site of transmorph!!!
				int clearing = target.getGameObject().getThisInt("clearing");
				
				// drop this,light,dark
				target.getGameObject().removeAttributeBlock("this");
				target.getGameObject().removeAttributeBlock("light");
				target.getGameObject().removeAttributeBlock("dark");
				
				// rename
				target.getGameObject().renameAttributeBlock("this_h","this");
				target.getGameObject().renameAttributeBlock("light_h","light");
				target.getGameObject().renameAttributeBlock("dark_h","dark");
				
				target.getGameObject().setThisAttribute("clearing",clearing);
			}
			if (target.isPlayerControlledLeader()) {
				CharacterWrapper character = new CharacterWrapper(target.getGameObject());
				
				// Untransmorph gold
				double gold = spell.getDouble("transmorphed_gold");
				if (gold>0) {
					spell.setBoolean("transmorphed_gold",false);
					character.addGold(gold); // add gold, in case transmorphed character picked up some gold!
				}
				
				spell.getGameObject().getHoldAsGameObjects().stream()
					.filter(go -> RealmComponent.getRealmComponent(go).isItem())
					.forEach(i -> character.getGameObject().add(i));
			}
		}
	}
	
	private GameObject prepareTransformation(String transformName, RealmComponent target, SpellWrapper spell, JFrame frame){
		GameData data = spell.getGameObject().getGameData();
		GameObject trans = data.createNewObject();
		
		SpellWrapper.copyTransformToObject(spell.getGameObject(), transformName, trans);
		spell.getGameObject().add(trans);
		
		IconGroup group = new IconGroup(RealmComponent.getRealmComponent(trans).getIcon(),IconGroup.VERTICAL,1);
		
		String pronoun = getPronoun(trans.getName());
		String message = "The " + target.getGameObject().getName()+" was transformed into " + pronoun + transformName + ".";
		RealmLogging.logMessage(RealmLogging.BATTLE, message);
		FrameManager.showDefaultManagedFrame(frame, message, "Transform", group, true);
		
		return trans;
	}
	
	private String getPronoun(String transformName){
		// Fix the pronoun
		String pronoun = "a ";
		if (transformName.startsWith("E")) {
			pronoun = "an ";
		}
		else if (transformName.equals("Mist")) {
			pronoun = "";
		}
		
		return pronoun;
	}
	
	private void doActualTransformation(RealmComponent target, SpellWrapper spell, CombatWrapper combat, GameObject transformObj){
		// Do the actual transform
		if (target.isCharacter()) {
			CharacterWrapper character = new CharacterWrapper(target.getGameObject());
			character.setTransmorph(transformObj);
		}
		else {
			if (target.getGameObject().hasAttributeBlock("this_h")) { // I think this happens when the target is stuck in an enchanted tile
//				unaffectTargets(); // This does NOT!! work!
				return; // This seems VERY wrong to me, but it works...
			}
			
			// rename this to this_hidden, light to light_hidden, and dark to dark_hidden
			target.getGameObject().renameAttributeBlock("this","this_h");
			target.getGameObject().renameAttributeBlock("light","light_h");
			target.getGameObject().renameAttributeBlock("dark","dark_h");
				
			// Copy this,light,dark from transformAnimal
			target.getGameObject().copyAttributeBlockFrom(transformObj,"this");
			target.getGameObject().copyAttributeBlockFrom(transformObj,"light");
			target.getGameObject().copyAttributeBlockFrom(transformObj,"dark");
			if (target.getGameObject().hasAttribute("this_h","clearing")) {
				target.getGameObject().setThisAttribute("clearing",target.getGameObject().getAttribute("this_h","clearing"));
			}
			if (target.getGameObject().hasAttribute("this_h","monster_die")) {
				target.getGameObject().setThisAttribute("monster_die",target.getGameObject().getAttribute("this_h","monster_die"));
			}
			if (target.getGameObject().hasAttribute("this_h","base_price")) {
				target.getGameObject().setThisAttribute("base_price",target.getGameObject().getAttribute("this_h","base_price"));
			}
			if (target.getGameObject().hasAttribute("this_h","fame")) {
				target.getGameObject().setThisAttribute("fame",target.getGameObject().getAttribute("this_h","fame"));
			}
			if (target.getGameObject().hasAttribute("this_h","notoriety")) {
				target.getGameObject().setThisAttribute("notoriety",target.getGameObject().getAttribute("this_h","notoriety"));
			}
			if (target.getGameObject().hasAttribute("this_h","native")) {
				target.getGameObject().setThisAttribute("native",target.getGameObject().getAttribute("this_h","native"));
			}
		}
		if (target.isPlayerControlledLeader()) {
			CharacterWrapper character = new CharacterWrapper(target.getGameObject());
			
			// Transmorph gold
			double gold = character.getGold();
			if (gold>0) {
				spell.setDouble("transmorphed_gold",gold);
				character.setGold(0.0);
			}
			
			// Move all inventory to the spell, so it doesn't appear in window anymore,
			// but will on double-click of the spell.  This should also disable inventory
			// without changing its active/inactive location			
			character.getInventory().stream()
				.filter(go -> RealmComponent.getRealmComponent(go).isItem())
				.forEach(i -> spell.getGameObject().add(i));
		}
		if (target.isMistLike()) {
			// Mists cannot have a target!
			target.clearTarget();
		
		}
	}
			
	private void doTransmorphTarget(RealmComponent target, SpellWrapper spell, CombatWrapper combat){
		if (target.isMonster()) {
			MonsterChitComponent monster = (MonsterChitComponent)target;
			if (monster.isDarkSideUp()) { // Always flip to light side on absorb!
				monster.setLightSideUp();
			}
		}
		target.clearOwner();
		RealmComponent targetsTarget = target.getTarget();
		if (targetsTarget!=null) {
			// Make sure that the target isn't already an attacker somewhere else
			CombatWrapper ttc = new CombatWrapper(targetsTarget.getGameObject());
			ttc.removeAttacker(target.getGameObject());
			
			if (targetsTarget.targeting(target)) {
				// Only clear targetsTarget if actually targeting the target (that's not confusing at all)
				targetsTarget.clearTarget();
			}
			
			// Clear its target
			target.clearTarget();
		}
		if (!spell.getGameObject().getHold().contains(target.getGameObject())) {
			spell.getGameObject().add(target.getGameObject());
			target.getGameObject().removeThisAttribute("clearing");
			combat.removeAllAttackers();
			RealmLogging.logMessage(spell.getCaster().getGameObject().getName(),"Absorbed the "+target.getGameObject().getName());
		}
		else {
			RealmLogging.logMessage(spell.getCaster().getGameObject().getName(),"Turns into the "+target.getGameObject().getName());
		}
		// Record which belongings are active, before inactivating them
		ArrayList<GameObject> inactivated = spell.getCaster().inactivateAllBelongings();
		for (GameObject go:inactivated) {
			spell.addListItem(Constants.ACTIVATED_ITEMS,go.getStringId());
		}
		spell.getCaster().setTransmorph(target.getGameObject());
	}

	

}
