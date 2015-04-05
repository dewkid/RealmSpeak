package com.robin.magic_realm.components.effect;

import com.robin.general.swing.FrameManager;
import com.robin.magic_realm.components.WeatherChit;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class SeeChangeWeatherEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		String type = context.Spell.getExtraIdentifier();
		
		if (type.toLowerCase().startsWith("change")) {

			context.Game.updateWeatherChit();
			FrameManager.showDefaultManagedFrame(context.Parent,"The weather chit has been changed.","Change Weather",null,true);
		}
		else {
			// See the weather chit
			int wc = context.Game.getWeatherChit();
			WeatherChit chit = new WeatherChit(wc);
			
			FrameManager.showDefaultManagedFrame(context.Parent,"The weather chit is a "+wc,"See Weather",chit.getIcon(),true);
			
			CharacterWrapper character = new CharacterWrapper(context.Caster);
			character.addNote(context.Caster,"See Weather","The weather chit is a "+wc);
		}
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub

	}

}
