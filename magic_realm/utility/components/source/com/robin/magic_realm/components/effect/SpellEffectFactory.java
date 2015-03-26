package com.robin.magic_realm.components.effect;

import java.util.HashMap;

import com.robin.magic_realm.components.utility.Constants;

public class SpellEffectFactory {
	public static HashMap<String, ISpellEffect>effectMap = new HashMap<String, ISpellEffect>();
	
	public static ISpellEffect create(String spellName){
		if(effectMap.isEmpty()){intitializeMap();}
		
		return effectMap.containsKey(spellName.toLowerCase())
				? effectMap.get(spellName.toLowerCase())
				: null;
	}

	private static void intitializeMap() {
		effectMap.put("absorb essence", new TransmorphEffect("target"));
		effectMap.put("ask demon", new AskDemonEffect());
		
		effectMap.put("blazing light", new ExtraCavePhaseEffect());
		effectMap.put("blend into background", new ExtraActionEffect("H"));
		
		effectMap.put("control bats", new ControlEffect());
		effectMap.put("curse", new CurseEffect());
		
		effectMap.put("elven grace", new MoveSpeedChangeEffect());
		effectMap.put("exorcise", new ExorciseEffect());
		
		effectMap.put("deal with goblins", new PacifyEffect(0));
		effectMap.put("disguise", new PacifyEffect(0));
		effectMap.put("dissolve spell", new CancelEffect());
		
		effectMap.put("faerie lights", new ChitChangeEffect());
		effectMap.put("fog", new ApplyNamedEffect(Constants.SP_NO_PEER));
		
		effectMap.put("hurricane winds", new HurricaneWindsEffect());
		effectMap.put("lost", new ApplyNamedEffect(Constants.SP_MOVE_IS_RANDOM));
		
		effectMap.put("make whole", new MakeWholeEffect());
		effectMap.put("melt into mist", new TransmorphEffect("mist"));
		
		effectMap.put("peace", new PeaceEffect());
		effectMap.put("pentangle", new NullifyEffect());
		effectMap.put("persuade", new PacifyEffect(1));
		
		effectMap.put("phantasm", new PhantasmEffect());
		effectMap.put("poison", new AddSharpnessEffect());
		effectMap.put("power of the pit", new PowerPitEffect());
		
		effectMap.put("prayer", new ExtraActionEffect("R"));
		effectMap.put("premonition", new ApplyNamedEffect(Constants.CHOOSE_TURN));
		effectMap.put("prophecy", new ApplyNamedEffect(Constants.DAYTIME_ACTIONS));

		effectMap.put("remedy", new CancelEffect());
		effectMap.put("reverse power", new ColorModEffect());
		
		effectMap.put("see/change weather", new SeeChangeWeatherEffect());
		effectMap.put("see hidden signs", new ExtraActionEffect("S"));
		effectMap.put("sense danger", new ExtraActionEffect("A"));
		effectMap.put("small blessing", new SmallBlessingEffect());
		
		effectMap.put("talk to wise bird", new InstantPeerEffect());
		effectMap.put("torch bearer", new ApplyNamedEffect(Constants.TORCH_BEARER));
		effectMap.put("transform", new TransmorphEffect("roll"));
		
		effectMap.put("unleash power", new ActionChangeEffect());
		
		effectMap.put("vale walker", new ApplyNamedEffect(Constants.VALE_WALKER));
		effectMap.put("violent storm", new ViolentStormEffect());
		
		effectMap.put("witch's brew", new ChitChangeEffect());
	}
}
