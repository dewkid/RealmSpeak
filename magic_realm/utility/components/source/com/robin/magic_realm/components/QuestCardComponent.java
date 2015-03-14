/* 
 * RealmSpeak is the Java application for playing the board game Magic Realm.
 * Copyright (c) 2005-2015 Robin Warren
 * E-mail: robin@dewkid.com
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 *
 * http://www.gnu.org/licenses/
 */
package com.robin.magic_realm.components;

import java.awt.*;

import javax.swing.ImageIcon;

import com.robin.game.objects.GameObject;
import com.robin.general.graphics.TextType;
import com.robin.general.graphics.TextType.Alignment;
import com.robin.general.swing.IconFactory;
import com.robin.magic_realm.components.quest.*;

public class QuestCardComponent extends CardComponent {

	public QuestCardComponent(GameObject obj) {
		super(obj);
	}

	public String getAdditionalInfo() {
		return "";
	}

	public Color getBackingColor() {
		return MagicRealmColor.GOLD;
	}

	public String getCardTypeName() {
		return "Quest";
	}

	public String getName() {
		return QUEST;
	}

	public void paintComponent(Graphics g1) {
		super.paintComponent(g1);
		Graphics2D g = (Graphics2D) g1;
		if (!isFaceUp())
			return;

		TextType tt;
		
		// Draw icon first
		String val = getGameObject().getAttribute(Quest.QUEST_BLOCK, Quest.STATE);
		QuestState state = val == null ? QuestState.New : QuestState.valueOf(val);
		ImageIcon icon = state.getIcon();
		int x = CARD_WIDTH >> 1;
		int y = (CARD_HEIGHT >> 1) - 16;
		g.drawImage(icon.getImage(), x - (icon.getIconWidth() >> 1), y, this);

		// Draw the title
		int pos = 5;
		tt = new TextType(gameObject.getName(), PRINT_WIDTH, "TITLE");
		tt.draw(g, PRINT_MARGIN, pos, Alignment.Center);

		// Stats (if any)
		pos = CARD_HEIGHT - 20;
		int vps = getGameObject().getInt(Quest.QUEST_BLOCK, QuestConstants.VP_REWARD);
		if (vps > 0) {
			String award = vps + " VP" + (vps == 1 ? "" : "s");
			tt = new TextType(award, PRINT_WIDTH, "BOLD");
			tt.draw(g, PRINT_MARGIN, pos, Alignment.Center);
			pos -= tt.getHeight(g1);
		}
		if (getGameObject().hasAttribute(Quest.QUEST_BLOCK, QuestConstants.ACTIVATEABLE) && getGameObject().hasAttribute(Quest.QUEST_BLOCK, QuestConstants.QTR_ALL_PLAY))
			if (state==QuestState.New || state==QuestState.Assigned)
				g.drawImage(IconFactory.findIcon("icons/plus.gif").getImage(), CARD_WIDTH - PRINT_MARGIN-16, CARD_HEIGHT - 20, this);

		// Show steps completed/failed (but only if the quest is unfinished)
		if (!state.isFinished()) {
			int finished = getGameObject().getThisInt(Quest.QUEST_FINISHED_STEP_COUNT);
			if (finished > 0) {
				drawSteps(g, QuestState.Complete.getSmallIcon(), finished, 10);
			}
		}

		// If ALL PLAY then show it
		if (getGameObject().hasAttribute(Quest.QUEST_BLOCK, QuestConstants.QTR_ALL_PLAY)) {
			tt = new TextType("ALL PLAY", PRINT_WIDTH, "TITLE_RED");
			tt.draw(g, PRINT_MARGIN + 1, pos, Alignment.Center);
		}
		
		if (getGameObject().hasAttribute(Quest.QUEST_BLOCK, QuestConstants.FLAG_TESTING)) {
			tt = new TextType("TESTING",PRINT_WIDTH,"NOTREADY");
			tt.setRotate(90);
			tt.draw(g,PRINT_MARGIN+2,(CARD_HEIGHT>>3),Alignment.Center);
		}
		if (getGameObject().hasAttribute(Quest.QUEST_BLOCK, QuestConstants.FLAG_BROKEN)) {
			tt = new TextType("BROKEN",PRINT_WIDTH,"TITLE_RED");
			tt.setRotate(90);
			tt.draw(g,PRINT_MARGIN+13,(CARD_HEIGHT>>3),Alignment.Center);
		}
	}

	private void drawSteps(Graphics2D g, ImageIcon icon, int count, int x) {
		int y = (CARD_HEIGHT>>2)+3;
		int step = (CARD_HEIGHT - y - 10)/count;
		if (step>15) step=15;
		
		for (int i = 0; i < count; i++) {
			g.drawImage(icon.getImage(), x - (icon.getIconWidth() >> 1), y, this);
			y+=step;
		}
	}
}