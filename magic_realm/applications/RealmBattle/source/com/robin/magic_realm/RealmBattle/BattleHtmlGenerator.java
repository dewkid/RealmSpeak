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
package com.robin.magic_realm.RealmBattle;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

import javax.swing.ImageIcon;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.general.io.ImageFile;
import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.CharacterChitComponent;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.attribute.ColorMagic;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.swing.CharacterChooser;
import com.robin.magic_realm.components.swing.ChitStateViewer;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.CombatWrapper;

public class BattleHtmlGenerator extends HtmlGenerator {
	
	private static String COMBAT_SHEET_FOLDER = "CombatSheets";
	private static String ATTACKER_FOLDER = "Attackers";

	private static String COMBAT_MAIN = "index.html";
	
	private static String COMBAT_SHEET = "cs";

	private static final String UD_PREFIX = "ud";
	protected int attackerIndex = 0;
	/*
	 * Front page:
	 * 	- Picture of combat tile
	 *  - Day, Combat Round, and Combat Phase
	 *  - All participating characters, showing who's turn is up
	 *  - All combat sheet links - should summarize sheet owner, and all attackers
	 *  - Current Round info from detail log (link to log for entire evening)
	 * 
	 * Character page:
	 *  - Same as GameHtmlGenerator, minus the day activities (chits, inventory)
	 *  
	 * Combat sheet page:
	 *  - Graphic of combat sheet (nothing more or less)
	 * 
	 * Combat resolution (if there is one) summary
	 */
	
	private BattleModel battleModel;
	private int actionState;
	private ArrayList<CombatSheet> combatSheets;
	private RealmComponent activeParticipant;
	private String battleLog;
	private String roundLog;
	
	public BattleHtmlGenerator(GameData data, String detailLog, boolean highQuality,BattleModel battleModel,int actionState,RealmComponent activeParticipant) {
		super(data,highQuality);
		this.actionState = actionState;
		this.battleModel = battleModel;
		this.activeParticipant = activeParticipant;
		RealmLogParser parser = new RealmLogParser(detailLog);
		TileLocation current = battleModel.getBattleLocation();
		battleLog = parser.getBattleLogFor(game.getMonth(),game.getDay(),current);
		roundLog = parser.getBattleLogFor(game.getMonth(),game.getDay(),current,battleModel.getBattleRound(actionState));
	}
	public void setCombatSheets(ArrayList<CombatSheet> combatSheets) {
		this.combatSheets = combatSheets;
	}
	public void saveHtml(String path) {
		inventoryIndex = 0;
		attackerIndex = 0;
		saveBattle(new HtmlPath(path));
	}
	private String getTitle() {
		return "RealmSpeak - Month "+game.getMonth()+", Day "+game.getDay()+", Combat Round "+battleModel.getBattleRound(actionState);
	}
	private String getSubTitle() {
		String action = "";
		switch(actionState) {
			case Constants.COMBAT_PREBATTLE:	
				action = "Pre-battle";
				break;
			case Constants.COMBAT_LURE:	
				action = "Luring";
				break;
			case Constants.COMBAT_DEPLOY:	
				action = "Native Deployment and Character Charging";
				break;
			case Constants.COMBAT_ACTIONS:		
				action = "Performing Actions";
				break;
			case Constants.COMBAT_ASSIGN:
				action = "Target Assignment";
				break;
			case Constants.COMBAT_POSITIONING:
				action = "Positioning";
				break;
			case Constants.COMBAT_TACTICS:
				action = "Change Tactics";
				break;
			case Constants.COMBAT_RESOLVING:
				action = "Combat Resolution";
				break;
			case Constants.COMBAT_FATIGUE:
				action = "Fatigue/Wounds";
				break;
		}
		TileLocation current = battleModel.getBattleLocation();
		return action+" in "+current.clearing.getDescription();
	}
	private ArrayList<RealmComponent> getUnassignedDenizens() {
		ArrayList<RealmComponent> list = new ArrayList<RealmComponent>();
		BattleGroup denizenGroup = battleModel.getDenizenBattleGroup();
		if (denizenGroup!=null && denizenGroup.size()>0) {
			for (Iterator i=denizenGroup.getBattleParticipants().iterator();i.hasNext();) {
				RealmComponent denizen = (RealmComponent)i.next();
				CombatWrapper combat = new CombatWrapper(denizen.getGameObject());
				if (denizen.getTarget()==null && !combat.isSheetOwner()) {
					list.add(denizen);
				}
			}
		}
		return list;
	}
	private boolean combatSheetIsActive(CombatSheet combatSheet) {
		RealmComponent owner = combatSheet.getSheetOwner();
		if (owner.getOwnerId()!=null) {
			owner = owner.getOwner();
		}
		if (actionState<Constants.COMBAT_RESOLVING && owner!=null && owner.equals(activeParticipant)) {
			return true;
		}
		return false;
	}
	private void saveBattle(HtmlPath path) {
		StringBuilder sb = new StringBuilder();
		
		String title = getTitle();
		sb.append("<h1>"+title+"</h1>\n");
		sb.append("<h2>"+getSubTitle()+"</h2>\n");

		int round = battleModel.getBattleRound(actionState);
		saveLog(battleLog,path.toString(),"battle_log.html","Battle Log",COMBAT_MAIN);
		
		sb.append("<table cellpadding=\"5\" cellspacing=\"10\"><tr><td bgcolor=\"#CCFFFF\">");
		sb.append(generateLinkElement("battle_log.html","Battle Log"));
		for (int i=1;i<=round;i++) {
			sb.append("</td><td bgcolor=\"#CCFFFF\">");
			String fileName = "round"+i+"_log.html";
			saveLog(roundLog,path.toString(),fileName,"Round "+i+" Log",COMBAT_MAIN);
			sb.append(generateLinkElement(fileName,"Round "+i+" Log"));
			sb.append("</td>");
		}
		sb.append("</tr></table>");
		
		TileLocation current = battleModel.getBattleLocation();
		ArrayList<Integer> color = new ArrayList<Integer>(); // only need to show one instance of each
		for (ColorMagic cm:current.clearing.getAllSourcesOfColor(true)) {
			if (color.contains(cm.getColorNumber())) continue;
			color.add(cm.getColorNumber());
			exportImage(path.path(cm.getColorName()+".jpg"),cm.getIcon(),1.0f,Color.white);
			sb.append(generateImageElement(0,cm.getColorName(),cm.getColorName()+".jpg"));
		}
		if (color.size()>0) {
			sb.append("\n<br>\n");
		}
		
		for (CharacterChitComponent chit:battleModel.getAllParticipatingCharacters()) {
			CharacterWrapper character = new CharacterWrapper(chit.getGameObject());
			for (Iterator n=character.getBattlingNativeGroups().iterator();n.hasNext();) {
				String groupName = (String)n.next();
				sb.append("<table cellpadding=\"2\"><tr><td bgcolor=\"#FFFF00\">");
				sb.append("<b>The ");
				sb.append(StringUtilities.capitalize(groupName));
				sb.append(" are battling the ");
				sb.append(character.getGameObject().getName());
				sb.append(".");
				sb.append("</b></td></tr></table>\n<br>\n");
			}
		}
		
		ArrayList<RealmComponent> unassigned = getUnassignedDenizens();
		if (!unassigned.isEmpty()) {
			createRealmComponentSection(sb,path,"Unassigned Denizens",battleModel.getDenizenBattleGroup().getBattleParticipants());
		}
		
		if (combatSheets!=null) {
			HtmlPath combatSheetFolder = path.newDirectory(COMBAT_SHEET_FOLDER);
			
			int n=0;
			int maxWidth = 250;
			for (CombatSheet combatSheet:combatSheets) {
				String name = COMBAT_SHEET+(n++);
				String imageName = name+".jpg";
				String sheetPageName = name+".html";
				ImageIcon icon = convertCombatSheetToImage(combatSheet);
				exportImage(combatSheetFolder.path(imageName),icon,1.0f,null);
				double scale = (double)maxWidth/(double)icon.getIconWidth();
				int newHeight = (int)(icon.getIconHeight() * scale);
				String imagePath = path.relativePathTo(combatSheetFolder).path(imageName);
				String sheetPath = path.relativePathTo(combatSheetFolder).path(sheetPageName);
				StringBuilder isb = new StringBuilder();
				isb.append("<a href=\"");
				isb.append(sheetPath);
				isb.append("\"><img border=2 alt=\"Combat Sheet\" src=\"");
				isb.append(imagePath);
				isb.append("\" width=");
				isb.append(maxWidth);
				isb.append(" height=");
				isb.append(newHeight);
				isb.append(" /></a>\n");
				sb.append(isb.toString());
				saveSheet(combatSheetFolder.path(sheetPageName),combatSheet,name,combatSheetFolder);
			}
		}
		if (actionState==Constants.COMBAT_RESOLVING) {
			sb.append("<br>\n");
			BattleSummaryWrapper bsw = new BattleSummaryWrapper(game.getGameObject());
			BattleSummary bs = bsw.getBattleSummary();
			BattleSummaryIcon icon = new BattleSummaryIcon(bs);
			String resolutionImageName = "Resolution.jpg";
			
			BufferedImage bi = new BufferedImage(icon.getIconWidth(),icon.getIconHeight(),BufferedImage.TYPE_3BYTE_BGR);
			icon.paintIcon(activeParticipant,bi.getGraphics(),0,0);
			ImageFile.saveJpeg(bi,new File(path.path(resolutionImageName)),1.0f);
			
			sb.append(generateImageElement(0,"Battle Resolution",resolutionImageName));
		}
		writeString(path.path(COMBAT_MAIN),title,sb.toString(),COMBAT_MAIN);
	}
	private void saveSheet(String sheetPageName, CombatSheet combatSheet, String imageName, HtmlPath path) {
		RealmComponent owner = combatSheet.getSheetOwner();
		String pageFile = imageName+"_cs";
		StringBuilder sb = new StringBuilder();
		sb.append("<h1>");
		sb.append(owner.getGameObject().getName());
		sb.append("</h1>");
		CombatWrapper combat = new CombatWrapper(owner.getGameObject());
		ArrayList<RealmComponent> attackers = combat.getAttackersAsComponents();
		if (!attackers.isEmpty()) {
			createRealmComponentSection(sb,path,"Attackers",attackers);
		}
		GameObject spell = combat.getCastSpell();
		if (spell!=null) {
			sb.append("<table cellpadding=\"2\"><tr><td bgcolor=\"#FFFF00\">");
			sb.append("<b>Casting ");
			sb.append(spell.getName());
			sb.append("</b></td></tr></table><br>\n");
		}
		sb.append("<table border=\"1\" cellpadding=\"2\" cellspacing=\"2\" >\n");
		sb.append("<tr><td valign=\"top\" width=\"1\" rowspan=3>\n");
		StringBuilder isb = new StringBuilder();
		isb.append("<a href=\"..");
		isb.append(File.separator);
		isb.append(COMBAT_MAIN);
		isb.append("\"><img border=2 alt=\"Combat Sheet\" src=\"");
		isb.append(imageName);
		isb.append(".jpg");
		isb.append("\"></a></td>\n");
		sb.append(isb.toString());
		if (owner.isCharacter()) {
			CharacterWrapper character = new CharacterWrapper(owner.getGameObject());
			//sb.append("<table border=\"0\" cellpadding=\"2\" cellspacing=\"2\" width=\"100%\">\n");
			sb.append("<td valign=\"top\" align=\"left\" width=\"1\">\n");
			// picture here
			ImageIcon pic = CharacterChooser.getCharacterImage(character.getGameObject());
			pic = new ImageIcon(pic.getImage().getScaledInstance(pic.getIconWidth()>>1,pic.getIconHeight()>>1,Image.SCALE_SMOOTH));
			exportImage(path+File.separator+pageFile+".jpg",pic,1.0f,null);
			sb.append("<img alt=\""+character.getCharacterName()+"\" src=\""+pageFile+".jpg\" >");
			
			sb.append("</td></tr>\n<tr><td align=\"center\" valign=\"top\" width=\"1\">\n"); //<td align="center" valign="top" width="1">
			sb.append("<table><tr><th>Active Inventory</th></tr><tr><td>\n");
			populateInventory(path.toString(),sb,character,true);
			sb.append("</td></tr></table>");
			sb.append("</td></tr>\n<tr><td align=\"center\" valign=\"top\" width=\"1\">\n");
			sb.append("<table><tr><th>Inactive Inventory</th></tr><tr><td>\n");
			populateInventory(path.toString(),sb,character,false);
			sb.append("</td></tr></table>");
			sb.append("</td></tr>\n</table>\n");
			
			ChitStateViewer chits = new ChitStateViewer(null,character);
			chits.pack();
			saveComponentImage(path+File.separator+pageFile+"_chits.jpg",chits.getMasterPanel());
			sb.append("<img alt=\""+character.getCharacterName()+" Chits\" src=\""+pageFile+"_chits.jpg\" >");
		}
		else {
			sb.append("</tr></table>\n");
		}
		writeString(sheetPageName,owner.toString()+" Combat Sheet",sb.toString(),".."+File.separator+COMBAT_MAIN);
	}
	private static Color yourTurnForeground = new Color(0,0,255,180);
	private static Font yourTurnFont = new Font("Dialog",Font.BOLD,36);
	public ImageIcon convertCombatSheetToImage(CombatSheet sheet) {
		sheet.alwaysSecret = true;
		boolean active = combatSheetIsActive(sheet);
		
		ImageIcon ii = sheet.getImageIcon();
		BufferedImage bi = new BufferedImage(ii.getIconWidth(),ii.getIconHeight(),BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = bi.getGraphics();
		g.setClip(0,0,ii.getIconWidth(),ii.getIconHeight());
		sheet.paint(g);
		
		if (active) {
			g.setColor(yourTurnForeground);
			g.setFont(yourTurnFont);
			g.drawString("Playing",ii.getIconWidth()-175,ii.getIconHeight()-125);
		}
		sheet.alwaysSecret = false;
		return new ImageIcon(bi);
	}
	protected void createRealmComponentSection(StringBuilder sb,HtmlPath path,String title,ArrayList<RealmComponent> list) {
		sb.append("<table border=\"1\" cellpadding=\"2\" cellspacing=\"2\" >\n");
		sb.append("<tr><th bgcolor=\"#FFFF00\">");
		sb.append(title);
		sb.append("</th></tr>\n");
		sb.append("<tr><td>\n");
		populateRealmComponents(path,sb,list);
		sb.append("</td></tr></table>");
	}
	protected void populateRealmComponents(HtmlPath path,StringBuilder sb,ArrayList<RealmComponent> list) {
		HtmlPath attackerPath = path.newDirectory(ATTACKER_FOLDER);
		for (RealmComponent denizen:list) {
			String name = denizen.getName();
			ImageIcon icon = denizen.getIcon();
			String imageName = UD_PREFIX+attackerIndex+".jpg";
			exportImage(attackerPath.path(imageName),icon,1.0f,Color.white);
			sb.append("<img alt=\""+name+"\" src=\""+path.relativePathTo(attackerPath).path(imageName)+" \" >\n");
			attackerIndex++;
		}
	}
}