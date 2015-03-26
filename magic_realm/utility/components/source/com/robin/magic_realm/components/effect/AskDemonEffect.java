package com.robin.magic_realm.components.effect;

import com.robin.magic_realm.components.utility.Constants;

public class AskDemonEffect implements ISpellEffect {

	@Override
	public void apply(SpellEffectContext context) {
		// The target (demon) is actually irrelevant here: we use only the extra identifier
		String string = context.Spell.getExtraIdentifier();
		int index = string.indexOf(Constants.DEMON_Q_DELIM);
		String playerName = string.substring(0,index);
		String question = string.substring(index+Constants.DEMON_Q_DELIM.length());
		context.Game.addQuestion(context.Spell.getCaster().getPlayerName(),playerName,question);
	}

	@Override
	public void unapply(SpellEffectContext context) {
		// TODO Auto-generated method stub

	}

}
