package com.robin.magic_realm.components.effect;

import com.robin.magic_realm.components.utility.Constants;

public class SpellEffectFactory {	
	public static ISpellEffect[] create(String spellName){
		switch(spellName.toLowerCase()){
			case "absorb essence":return new ISpellEffect[]{new TransmorphEffect("target")};
			case "animate": return new ISpellEffect[]{new AnimateEffect()};
			case "ask demon": return new ISpellEffect[]{new AskDemonEffect()};
		
			//case "bad luck": I AM NOT SURE WHAT GOES HERE);
			case "bewilder": return new ISpellEffect[]{new ApplyClearingEffect("bewildered")};
			case "blazing light": return new ISpellEffect[]{new ExtraCavePhaseEffect()};
			case "blazing light x": return new ISpellEffect[]{new ApplyNamedEffect(Constants.TORCH_BEARER)};
		
			case "blend into background": return new ISpellEffect[]{new ExtraActionEffect("H")};
			case "blend into background x": return new ISpellEffect[]{new ExtraActionEffect("H")};
			case "blunting": return new ISpellEffect[]{new ApplyClearingEffect("blunted")};
			case "blur": return new ISpellEffect[]{new FinalChitSpeedEffect()};
		
			case "broomstick": return new ISpellEffect[]{new FlyChitEffect()};
		
			case "control bats": return new ISpellEffect[]{new ControlEffect()};
			case "curse": return new ISpellEffect[]{new CurseEffect()};
	
			case "exorcise": return new ISpellEffect[]{new ExorciseEffect()};
		
			case "deal with goblins": return new ISpellEffect[]{new PacifyEffect(0)};
			case "disguise": return new ISpellEffect[]{new PacifyEffect(0)};
			case "disjunction": return new ISpellEffect[]{new ApplyNamedEffect("no_w_fat"),new ApplyNamedEffect("no_ter_harm")};
		
			case "dissolve spell": return new ISpellEffect[]{new CancelEffect()};
			case "divine might": return new ISpellEffect[]{new ApplyNamedEffect(Constants.STRONG_MF)};
			case "divine protection": return new ISpellEffect[]{new ApplyNamedEffect("adds_armor")};
		
			case "elemental power": return new ISpellEffect[]{new ForcedEnchantEffect()};
			case "elemental spirit": return new ISpellEffect[]{new ChitChangeEffect()};
			case "elven grace": return new ISpellEffect[]{new MoveSpeedChangeEffect()};
		
			case "enchant artifact": return new ISpellEffect[]{new EnchantEffect()};
			case "eternal servant": return new ISpellEffect[]{new NoWeightEffect()};
		
			case "fae guard": return new ISpellEffect[]{new SummonFairyEffect()};
			case "faerie lights": return new ISpellEffect[]{new ChitChangeEffect()};
			case "filcher": return new ISpellEffect[]{new FilcherEffect()};
			
			case "flame staff": return new ISpellEffect[]{new AddSharpnessEffect(2)};
			case "fog": return new ISpellEffect[]{new ApplyNamedEffect(Constants.SP_NO_PEER)};
		
			case "gravity": return new ISpellEffect[]{new ApplyClearingEffect("heavied")};
			case "guide spider or octopus": return new ISpellEffect[]{new ControlEffect()};
		
			case "heal": return new ISpellEffect[]{new HealChitEffect()};
			case "hop": return new ISpellEffect[]{new TeleportEffect("RandomClearing")};
			case "hurricane winds": return new ISpellEffect[]{new HurricaneWindsEffect()};
			case "hypnotize": return new ISpellEffect[]{new ControlEffect()};
		
			case "levitate": return new ISpellEffect[]{new NoWeightEffect()};
			case "lost": return new ISpellEffect[]{new ApplyNamedEffect(Constants.SP_MOVE_IS_RANDOM)};
			case "mage guard": return new ISpellEffect[]{new MageGuardEffect()};
		
			case "make whole": return new ISpellEffect[]{new MakeWholeEffect()};
			case "melt into mist": return new ISpellEffect[]{new NullifyEffect(),new DisengageEffect(), new TransmorphEffect("mist")};
			case "miracle": return new ISpellEffect[]{new MiracleEffect()};
			case "open gate": return new ISpellEffect[]{new TeleportEffect("KnownGate")};
		
			case "peace": return new ISpellEffect[]{new PeaceEffect()};
			//case "peace with nature": return new ISpellEffect[]{new ApplyNamedEffect(Constants.PEACE_WITH_NATURE)};
		
			case "pentangle": return new ISpellEffect[]{new NullifyEffect()};
			case "persuade": return new ISpellEffect[]{new PacifyEffect(1)};
		
			case "phantasm": return new ISpellEffect[]{new PhantasmEffect()};
			case "poison": return new ISpellEffect[]{new AddSharpnessEffect(1)};
			case "power of the pit": return new ISpellEffect[]{new PowerPitEffect()};
		
			case "prayer": return new ISpellEffect[]{new ExtraActionEffect("R")};
			case "premonition": return new ISpellEffect[]{new ApplyNamedEffect(Constants.CHOOSE_TURN)};
			case "prophecy": return new ISpellEffect[]{new ApplyNamedEffect(Constants.DAYTIME_ACTIONS)};
			case "protection from magic": return new ISpellEffect[]{new PhaseChitEffect(), new NullifyEffect()}; //also protection from magic

			case "raise dead": return new ISpellEffect[]{new SummonEffect("undead")};
			case "remedy": return new ISpellEffect[]{new CancelEffect()};
			case "repair armor": return new ISpellEffect[]{new RepairEffect()};
			case "reverse power": return new ISpellEffect[]{new ColorModEffect()};
		
			case "see/change weather": return new ISpellEffect[]{new SeeChangeWeatherEffect()};
			case "see hidden signs": return new ISpellEffect[]{new ExtraActionEffect("S")};
			case "see hidden signs x": return new ISpellEffect[]{new ExtraActionEffect("S")};
			case "send": return new ISpellEffect[]{new ControlEffect()};
		
			case "sense danger": return new ISpellEffect[]{new ExtraActionEffect("A")};
			case "serpent tongue": return new ISpellEffect[]{new ControlEffect()};
			case "shrink": return new ISpellEffect[]{new ApplyNamedEffect("shrink")};
		
			case "slow monster": return new ISpellEffect[]{new ApplyNamedEffect("slowed")};
			case "small blessing": return new ISpellEffect[]{new SmallBlessingEffect()};
			case "sparkle": return new ISpellEffect[]{new UnassignEffect()};
		
			case "spirit guide": return new ISpellEffect[]{new ApplyNamedEffect(Constants.SPIRIT_GUIDE)};
			case "staff to snake": return new ISpellEffect[]{new ChangeToCompanionEffect()};
			case "stone gaze": return new ISpellEffect[]{new PetrifyEffect()};
		
			case "summon aid": return new ISpellEffect[]{new SummonAidEffect()};
			case "summon animal": return new ISpellEffect[]{new SummonEffect("animal")};
			case "summon elemental": return new ISpellEffect[]{new SummonEffect("elemental")};
			case "sword song": return new ISpellEffect[]{new ApplyNamedEffect("alerted_weapon"), new AlertWeaponEffect()};
		
			case "talk to wise bird": return new ISpellEffect[]{new InstantPeerEffect()};
			case "teleport": return new ISpellEffect[]{new TeleportEffect("ChooseTileTwo")};
		
			case "transform": return new ISpellEffect[]{new TransmorphEffect("roll")};
		
			case "unleash power": return new ISpellEffect[]{new ActionChangeEffect()};
		
			case "vale walker": return new ISpellEffect[]{new ApplyNamedEffect(Constants.VALE_WALKER)};
			case "violent storm": return new ISpellEffect[]{new ViolentStormEffect()};
			case "vision": return new ISpellEffect[]{new DiscoverRoadEffect()};
		
			case "whistle for monsters": return new ISpellEffect[]{new MoveSoundEffect()};
			case "witch's brew": return new ISpellEffect[]{new ChitChangeEffect()};
			
			default: return null;
		}
	}
}
		

	

