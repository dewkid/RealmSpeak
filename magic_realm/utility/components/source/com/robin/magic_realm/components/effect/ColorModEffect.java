package com.robin.magic_realm.components.effect;

import com.robin.magic_realm.components.CharacterActionChitComponent;
import com.robin.magic_realm.components.attribute.ColorMagic;
import com.robin.magic_realm.components.attribute.ColorMod;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.SpellUtility;

public class ColorModEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		ColorMod colorMod = ColorMod.createColorMod(context.Spell.getGameObject());
		if (context.Target.isTile()) {
			context.Target.getGameObject().setThisAttribute(Constants.MOD_COLOR_SOURCE, context.Spell.getGameObject().getThisAttribute(Constants.COLOR_MOD));
		}
		else {
			ColorMagic cm;
			if (context.Target.isActionChit()) {
				cm = ((CharacterActionChitComponent)context.Target).getColorMagic();
			}
			else{
				cm = SpellUtility.getColorMagicFor(context.Target);
			}
			cm = colorMod.convertColor(cm);
			if (cm!=null) {
				context.Target.getGameObject().setThisAttribute(Constants.MOD_COLOR_SOURCE,cm.getColorName().toLowerCase());
			}
		}

	}

	@Override
	public void unapply(SpellEffectContext context) {
		context.Target.getGameObject().removeThisAttribute(Constants.MOD_COLOR_SOURCE);
	}

}
