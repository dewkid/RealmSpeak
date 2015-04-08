package com.robin.magic_realm.components.effect;

import java.util.HashMap;

import com.robin.magic_realm.components.utility.Constants;

public class SpellEffectFactory {
	public static HashMap<String, ISpellEffect[]> effectMap = new HashMap<String, ISpellEffect[]>();
	
	public static ISpellEffect[] create(String spellName){
		if(effectMap.isEmpty()){intitializeMap();}
		
		return effectMap.containsKey(spellName.toLowerCase())
				? effectMap.get(spellName.toLowerCase())
				: null;
	}
	
	private static void put(String key, ISpellEffect... effects){
		effectMap.put(key, effects);
	}
	
	private static void intitializeMap() {
		put("absorb essence",new TransmorphEffect("target"));
		put("animate", new AnimateEffect());
		put("ask demon", new AskDemonEffect());
		
		//put("bad luck", I AM NOT SURE WHAT GOES HERE);
		put("bewilder", new ApplyClearingEffect("bewildered"));
		put("blazing light", new ExtraCavePhaseEffect());
		put("blazing light x", new ApplyNamedEffect(Constants.TORCH_BEARER));
		
		put("blend into background", new ExtraActionEffect("H"));
		put("blunting", new ApplyClearingEffect("blunted"));
		put("blur", new FinalChitSpeedEffect());
		
		put("broomstick", new FlyChitEffect());
		
		put("control bats", new ControlEffect());
		put("curse", new CurseEffect());
		
		put("elven grace", new MoveSpeedChangeEffect());
		put("exorcise", new ExorciseEffect());
		
		put("deal with goblins", new PacifyEffect(0));
		put("disguise", new PacifyEffect(0));
		put("disjunction", new ApplyNamedEffect("no_w_fat"), new ApplyNamedEffect("no_ter_harm"));
		
		put("dissolve spell", new CancelEffect());
		put("divine might", new ApplyNamedEffect(Constants.STRONG_MF));
		put("divine protection", new ApplyNamedEffect("adds_armor"));
		
		put("elemental power", new ForcedEnchantEffect());
		put("elemental spirit", new ChitChangeEffect());
		put("elven grace", new MoveSpeedChangeEffect());
		//put("elven sight", I AM NOT SURE WHAT GOES HERE);
		
		put("enchant artifact", new EnchantEffect());
		put("fae guard", new SummonFairyEffect());
		put("faerie lights", new ChitChangeEffect());
		//put("fleet foot",  new ApplyNamedEffect(Constants.MOUNTAIN_MOVE_ADJ));
		
		put("fog", new ApplyNamedEffect(Constants.SP_NO_PEER));
		
		put("gravity", new ApplyClearingEffect("heavied"));
		put("guide spider or octopus", new ControlEffect());
		
		put("heal", new HealChitEffect());
		put("hop", new TeleportEffect("RandomClearing"));
		put("hurricane winds", new HurricaneWindsEffect());
		put("hypnotize", new ControlEffect());
		
		//put("illusion", I AM NOT SURE WHAT GOES HERE);
		//put("invisible guardian", ???);
		put("levitate", new NoWeightEffect());
		put("lost", new ApplyNamedEffect(Constants.SP_MOVE_IS_RANDOM));
		
		put("make whole", new MakeWholeEffect());
		put("melt into mist", new NullifyEffect(), new DisengageEffect(), new TransmorphEffect("mist"));
		put("miracle", new MiracleEffect());
		put("open gate", new TeleportEffect("KnownGate"));
		
		put("peace", new PeaceEffect());
		//put("peace with nature", new ApplyNamedEffect(Constants.PEACE_WITH_NATURE));
		
		put("pentangle", new NullifyEffect());
		put("persuade", new PacifyEffect(1));
		
		put("phantasm", new PhantasmEffect());
		put("poison", new AddSharpnessEffect());
		put("power of the pit", new PowerPitEffect());
		
		put("prayer", new ExtraActionEffect("R"));
		put("premonition", new ApplyNamedEffect(Constants.CHOOSE_TURN));
		put("prophecy", new ApplyNamedEffect(Constants.DAYTIME_ACTIONS));
		put("protection from magic", new PhaseChitEffect(), new NullifyEffect()); //also protection from magic

		put("raise dead", new SummonEffect("undead"));
		put("remedy", new CancelEffect());
		put("repair armor", new RepairEffect());
		put("reverse power", new ColorModEffect());
		
		put("see/change weather", new SeeChangeWeatherEffect());
		put("see hidden signs", new ExtraActionEffect("S"));
		put("send", new ControlEffect());
		
		put("sense danger", new ExtraActionEffect("A"));
		put("serpent tongue", new ControlEffect());
		put("shrink", new ApplyNamedEffect("shrink"));
		
		put("slow monster", new ApplyNamedEffect("slowed"));
		put("small blessing", new SmallBlessingEffect());
		put("sparkle", new UnassignEffect());
		
		put("stone gaze", new PetrifyEffect());
		
		put("summon animal", new SummonEffect("animal"));
		put("summon elemental", new SummonEffect("elemental"));
		put("sword song", new ApplyNamedEffect("alerted_weapon"), new AlertWeaponEffect());
		
		put("talk to wise bird", new InstantPeerEffect());
		put("teleport", new TeleportEffect("ChooseTileTwo"));
		
		//put("torch bearer", new ApplyNamedEffect(Constants.TORCH_BEARER));
		put("transform", new TransmorphEffect("roll"));
		
		put("unleash power", new ActionChangeEffect());
		
		put("vale walker", new ApplyNamedEffect(Constants.VALE_WALKER));
		put("violent storm", new ViolentStormEffect());
		put("vision", new DiscoverRoadEffect());
		
		put("whistle for monsters", new MoveSoundEffect());
		put("witch's brew", new ChitChangeEffect());
		
		//effectMap.put("world fades", I AM NOT SURE WHAT GOES HERE);
	}
}
