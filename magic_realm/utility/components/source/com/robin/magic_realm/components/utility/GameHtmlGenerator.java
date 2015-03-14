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
package com.robin.magic_realm.components.utility;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.swing.ImageIcon;

import com.robin.game.objects.*;
import com.robin.general.swing.DieRoller;
import com.robin.general.swing.GameOptionPane;
import com.robin.general.util.*;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.RelationshipType;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.swing.*;
import com.robin.magic_realm.components.wrapper.*;

public class GameHtmlGenerator extends HtmlGenerator {
	
	private static final String DAY_PAGE = "index.html";
	private static final String RULE_SUMMARY_PAGE = "RuleSummary.html";
	//private static final String LAST_DAY_LOG_PAGE = "DayLogPage.html";
	private static final String MAP_PAGE = "MapPage.html";
	
	private static final String DAY_PAGE_MAP = "DayPageMap.jpg";
	private static final String SEASON_ICON = "SeasonIcon.jpg";
	private static final String DAY_PAGE_SETUP = "SetupCard";
	
	private static final String CHAR_DIR = "/characters";
	private static final String DIE_PREFIX = "d";
	private static final String CHAR_PREFIX = "c";
	private static final String HIRELING_PREFIX = "h";
	private static final String KILL_PREFIX = "k";
	
	private static final String CLEARING_DIR = "/clearings";
	private static final String CLEARING_PREFIX = "tc";
	
	private static final String SETUP_IMAGE = "setupcard.jpg";
	private static final String SETUP_HTML = "setupcard.html";
	private static final String SETUP_PREFIX = "s";
	
	/* Generate a home page that shows a link to the detail log, and possibly a list of days...
	 * 	- Clicking a day shows a new page with links:
	 * 		- Map
	 * 		- Characters
	 * 		- Treasure setup card
	 * 
	 * Problems:
	 * - Map doesn't change??
	 * - Controlled monsters don't appear!
	 */
	
	
			/*
			 * IMAGE MAPPING
			 * 
		<DIV ALIGN=CENTER>
		
		<MAP NAME="map1">
		<AREA
		   HREF="contacts.html" ALT="Contacts" TITLE="Contacts"
		   SHAPE=RECT COORDS="6,116,97,184">
		<AREA
		   HREF="products.html" ALT="Products" TITLE="Products"
		   SHAPE=CIRCLE COORDS="251,143,47">
		<AREA
		   HREF="new.html" ALT="New!" TITLE="New!"
		   SHAPE=POLY COORDS="150,217, 190,257, 150,297,110,257">
		</MAP>
		
		<IMG SRC="testmap.gif"
		   ALT="map of GH site" BORDER=0 WIDTH=300 HEIGHT=300
		   USEMAP="#map1"><BR>
		
		[ <A HREF="contacts.html" ALT="Contacts">Contacts</A> ]
		[ <A HREF="products.html" ALT="Products">Products</A> ]
		[ <A HREF="new.html"      ALT="New!">New!</A> ]
		</DIV>	 */
	
	private String detailLog;
	private Dimension mapSize;
	private ArrayList<String> setupCardNames;
	
	private int hirelingIndex = 0;
	private int clearingIndex = 0;
	private int killIndex = 0;
	private int setupIndex = 0;
	
	private boolean highQuality;
	
	public GameHtmlGenerator(GameData data,String detailLog,boolean highQuality) {
		super(data,highQuality);
		this.data = data;
		this.detailLog = detailLog;
		this.game = GameWrapper.findGame(data);
		this.calendar = RealmCalendar.getCalendar(data);
		this.hostPrefs = HostPrefWrapper.findHostPrefs(data);
		this.highQuality = highQuality;
	}
	public void saveHtml(String path) {
		inventoryIndex = 0;
		hirelingIndex = 0;
		clearingIndex = 0;
		killIndex = 0;
		setupIndex = 0;
		
		File dest = new File(path);
		if (!dest.exists()) {
			dest.mkdir();
		}
		generateImages(path);
		saveDay(path);
	}
	private void writeString(String path,String title,String string) {
		writeString(path,title,string,DAY_PAGE);
	}
	private void generateSetupCard(TreasureSetupCardView view,String path,String name) {
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdir();
		}
		
		saveComponentImage(path+File.separator+SETUP_IMAGE,view);
		
		// Map HTML
		StringBuilder sb = new StringBuilder();
		sb.append("<h1>");
		sb.append(getTitle()+" - "+name);
		sb.append("</h1>\n");
		sb.append("<i><font size=\"+1\" color=\"blue\">Hint - Click on boxes for more detail</font></i><br><br>\n");

		sb.append("<DIV ALIGN=LEFT>\n");
		sb.append("<MAP NAME=\"map1\">\n");
		Dimension size = view.getSize();
		int width = size.width;
		int height = size.height;
		
		int n=0;
		Iterator i=view.getDrawContainerList().iterator();
		for (Rectangle r:view.getDrawRectList()) {
			GameObject container = (GameObject)i.next();
			if (container.getHoldCount()>0) {
				boolean allMonsters = true;
				for (Iterator j=container.getHold().iterator();j.hasNext();) {
					RealmComponent test = RealmComponent.getRealmComponent((GameObject)j.next());
					if (!test.isMonster()) {
						allMonsters = false;
						break;
					}
				}
				if (!allMonsters) {
					String destPage = "box"+(n++)+".html";
					sb.append("<AREA HREF=\"");
					sb.append(destPage);
					sb.append("\" ALT=\"");
					sb.append(container.getName());
					sb.append("\" TITLE=\"");
					sb.append(container.getName());
					sb.append("\" SHAPE=RECT COORDS=\"");
					sb.append(r.x);
					sb.append(",");
					sb.append(r.y);
					sb.append(",");
					sb.append(r.x + r.width);
					sb.append(",");
					sb.append(r.y + r.height);
					sb.append("\">\n");
					createContainerBoxPage(container,path,destPage);
				}
			}
		}//<area shape="rect" coords="0,0,82,126" href="sun.htm" alt="Sun" />
		sb.append("</MAP>\n");
		
		sb.append("<IMG SRC=\"");
		sb.append(SETUP_IMAGE);
		sb.append("\" ALT=\"");
		sb.append(name);
		sb.append("\" BORDER=0 WIDTH=");
		sb.append(width);
		sb.append(" HEIGHT=");
		sb.append(height);
		sb.append(" USEMAP=\"#map1\"><br><br>\n");
		sb.append("</DIV>");
		
		writeString(path+File.separator+SETUP_HTML,name,sb.toString(),"../"+DAY_PAGE);
	}
	private void createContainerBoxPage(GameObject container,String path,String page) {
		StringBuilder sb = new StringBuilder();
		sb.append("<h1>");
		sb.append(container.getName());
		sb.append("</h1>\n");
		for (Iterator i=container.getHold().iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			
			ImageIcon icon = rc.getIcon();
			String altName = rc.getGameObject().getName();
			if (rc.isTreasure()) {
				altName = "Treasure";
			}
			else if (rc.isSpell()) {
				altName = "Type "+go.getThisAttribute("spell")+" Spell";
				SpellCardComponent spell = (SpellCardComponent)rc;
				spell.setNextAddInfo(true);
				icon = new ImageIcon(spell.getFaceDownImage());
			}
			
			exportImage(path+File.separator+SETUP_PREFIX+setupIndex+".jpg",icon,0.5f,Color.white);
			sb.append("<img alt=\""+altName+"\" src=\""+SETUP_PREFIX+setupIndex+".jpg\" >\n");
			setupIndex++;
		}
		writeString(path+File.separator+page,container.getName(),sb.toString(),SETUP_HTML);
	}
	private void generateMap(String path) {
		// Map
		CenteredMapView map = CenteredMapView.getSingleton();
		double scale = map.getScale();
		Point2D.Double offset = map.getOffset();
		double saveScale = highQuality?1.0:0.50;
		map.setScale(saveScale);
		Rectangle rect = map.getNormalMapRectangle();
		map.setOffset(new Point2D.Double(0,0));
		mapSize = new Dimension(rect.width,rect.height);
		mapSize.width *= saveScale;
		mapSize.height *= saveScale;
		BufferedImage bi = new BufferedImage(mapSize.width,mapSize.height,BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = (Graphics2D)bi.getGraphics();
		g.setColor(Color.lightGray);
		g.fillRect(0,0,rect.width,rect.height);
		map.clearMapAttentionMessage();
		map.clearMarkClearingAlertText();
		map.setShowEmbellishments(game.getGameStarted());
		map.setReplot(true);
		map.paint(g);
		map.setScale(scale);
		map.setOffset(offset);
		exportImage(path+File.separator+DAY_PAGE_MAP,new ImageIcon(bi),0.5f,null);
		
		File dir = new File(path+CLEARING_DIR);
		if (!dir.exists()) {
			dir.mkdir();
		}
		
		// Map HTML
		StringBuilder sb = new StringBuilder();
		sb.append("<h1>");
		sb.append(getTitle()+" - Game Map");
		sb.append("</h1>\n");
		int width = bi.getWidth();
		int height = bi.getHeight();
		if (game.getGameStarted()) {
			sb.append("<i><font size=\"+1\" color=\"blue\">Hint - Click on clearings for more detail</font></i><br><br>\n");
	
			sb.append("<DIV ALIGN=LEFT>\n");
			sb.append("<MAP NAME=\"map1\">\n");
			
			ArrayList<ClearingDetail> clearings = map.getAllOccupiedClearings();
			
			int radius=25;
			for (ClearingDetail clearing:clearings) {
				String clearingName = clearing.shortString();
				String destPage = clearingName+".html";
				int x = (int)(clearing.getAbsolutePosition().x * saveScale);
				int y = (int)(clearing.getAbsolutePosition().y * saveScale);
				sb.append("<AREA HREF=\"");
				sb.append("."+CLEARING_DIR+"/"+destPage);
				sb.append("\" ALT=\"");
				sb.append(clearingName);
				sb.append("\" TITLE=\"");
				sb.append(clearingName);
				sb.append("\" SHAPE=CIRCLE COORDS=\"");
				sb.append(x);
				sb.append(",");
				sb.append(y);
				sb.append(",");
				sb.append(radius);
				sb.append("\">\n");
				
				createClearingPage(clearing,path+File.separator+CLEARING_DIR,destPage);
			}
			
			sb.append("</MAP>\n");
			
			sb.append("<IMG SRC=\"");
			sb.append(DAY_PAGE_MAP);
			sb.append("\" ALT=\"Game Map\" BORDER=0 WIDTH=");
			sb.append(width);
			sb.append(" HEIGHT=");
			sb.append(height);
			sb.append(" USEMAP=\"#map1\"><br><br>\n");
			
			for (ClearingDetail clearing:clearings) {
				String clearingName = clearing.shortString();
				String destPage = clearingName+".html";
				sb.append("[ <A HREF=\"");
				sb.append("."+CLEARING_DIR+"/"+destPage);
				sb.append("\" ALT=\"");
				sb.append(clearingName);
				sb.append("\">");
				sb.append(clearingName);
				sb.append("</A> ]\n");
			}
			
			sb.append("</DIV>");
		}
		else {
			sb.append("<IMG SRC=\"");
			sb.append(DAY_PAGE_MAP);
			sb.append("\" ALT=\"Game Map\" BORDER=0 WIDTH=");
			sb.append(width);
			sb.append(" HEIGHT=");
			sb.append(height);
			sb.append("><br><br>\n");
		}
			
		writeString(path+File.separator+MAP_PAGE,"Game Map",sb.toString());
	}
	private void createClearingPage(ClearingDetail clearing,String path,String file) {
		StringBuilder sb = new StringBuilder();
		sb.append("<h1>");
		sb.append(clearing.getDescription());
		sb.append("</h1>\n");
		ArrayList<RealmComponent> components = clearing.getClearingComponents();
		for (RealmComponent rc:components) {
			exportImage(path+File.separator+CLEARING_PREFIX+clearingIndex+".jpg",rc.getIcon(),0.5f,Color.white);
			
			String altName = rc.getGameObject().getName();
			if (rc.isTreasure()) {
				altName = "Treasure"; 
			}
			
			sb.append("<img alt=\""+altName+"\" src=\""+CLEARING_PREFIX+clearingIndex+".jpg\" >\n");
			clearingIndex++;
		}
		writeString(path+File.separator+file,clearing.getDescription(),sb.toString(),"../"+MAP_PAGE);
	}
	private void generateImages(String path) {
		generateMap(path);
		
		// Setup Card
		setupCardNames = new ArrayList<String>();
		TreasureSetupCardView[] treasureSetupCardView;
		if (hostPrefs.getMultiBoardEnabled()) {
			int count = hostPrefs.getMultiBoardCount();
			treasureSetupCardView = new TreasureSetupCardView[count];
			treasureSetupCardView[0] = new TreasureSetupCardView(data,"","!"+Constants.BOARD_NUMBER);
			setupCardNames.add("Treasure Setup Card A");
			for (int n=1;n<count;n++) {
				String boardNumber = Constants.MULTI_BOARD_APPENDS.substring(n-1,n);
				treasureSetupCardView[n] = new TreasureSetupCardView(data,"",Constants.BOARD_NUMBER+"="+boardNumber);
				setupCardNames.add("Treasure Setup Card "+boardNumber);
			}
		}
		else {
			treasureSetupCardView = new TreasureSetupCardView[1];
			treasureSetupCardView[0] = new TreasureSetupCardView(data,"");
			setupCardNames.add("Treasure Setup Card");
		}
		for (int i=0;i<treasureSetupCardView.length;i++) {
			treasureSetupCardView[i].reset();
			String setupCardPath = path+File.separator+DAY_PAGE_SETUP+i;
			//saveComponentImage(setupCardPath,treasureSetupCardView[i]);
			generateSetupCard(treasureSetupCardView[i],setupCardPath,setupCardNames.get(i));
		}
		
		// Other?
	}
	private String getTitle() {
		return "RealmSpeak - Month "+game.getMonth()+", Day "+game.getDay();
	}
	private void saveDay(String path) {
		StringBuilder sb = new StringBuilder();
		
		String title = getTitle();
		saveLog(detailLog,path,LOG_PAGE,title+" - Detail Log",DAY_PAGE);
		saveRuleSummary(path,title);
		sb.append("<h1>"+title+"</h1>\n");
		
		if (hostPrefs.isUsingSeasons()) {
			sb.append("<table border=\"0\" cellpadding=\"2\" cellspacing=\"2\">\n");
			sb.append("<tr><td valign=\"top\">");
			ImageIcon seasonIcon = calendar.getSeasonIcon(game.getMonth());
			exportImage(path+File.separator+SEASON_ICON,seasonIcon,1.0f,null);
			sb.append("<img alt=\"season\" src=\""+SEASON_ICON+"\" ></td>\n");
			sb.append("<td valign=\"top\" bgcolor=\"#ddeeff\">\n<b>");
			sb.append(calendar.getSeasonName(game.getMonth()));
			if (hostPrefs.hasPref(Constants.OPT_WEATHER)) {
				sb.append("<br><i>");
				sb.append(calendar.getWeatherName(game.getMonth()));
				sb.append("</i>");
			}
			sb.append("</b></td><td>");
			
			sb.append("\n<table bgcolor=\"#ddeeff\"><tr><td align=\"right\">");
			sb.append("<b>Days in Week:</b></td><td>");
			sb.append(calendar.getDays(game.getMonth()));
			sb.append("</td></tr>\n<tr><td align=\"right\">");
			
			sb.append("<b>Basic Phases:</b></td><td>");
			sb.append(calendar.getBasicPhases(game.getMonth()));
			sb.append("</td></tr>\n<tr><td align=\"right\">");
			
			sb.append("<b>Sunlight Phases:</b></td><td>");
			sb.append(calendar.getSunlightPhases(game.getMonth()));
			sb.append("</td></tr>\n<tr><td align=\"right\">");
			
			sb.append("<b>Sheltered Phases:</b></td><td>");
			sb.append(calendar.getShelteredPhases(game.getMonth()));
			sb.append("</td></tr>\n");
			
			String desc = calendar.getSeasonDescription(game.getMonth());
			if (desc!=null && desc.length()>0) {
				sb.append("<tr><td align=\"right\"><b>Special:</b></td><td>");
				sb.append(desc);
			}
			
			sb.append("</td></tr>\n</table>");
			
			sb.append("</td></tr></table>");
		}
		
		sb.append("<table border=\"0\" cellpadding=\"2\" cellspacing=\"2\" width=\"100%\">\n");
		sb.append("<tr><td valign=\"top\" width=\"1\">\n");
		sb.append("<a href=\"./"+MAP_PAGE+"\"><img alt=\"Game Map\" src=\"./"+DAY_PAGE_MAP+"\" width=\""+(mapSize.width/5)+"\" height=\""+(mapSize.height/5)+"\" align=\"left\"></a></td>\n");
		sb.append("<td valign=\"top\">\n");
		int n=0;
		for (String name:setupCardNames) {
			sb.append("<br><a href=\"./"+DAY_PAGE_SETUP+n+"/"+SETUP_HTML+"\">"+name+"</a>\n");
			n++;
		}
		sb.append("<br><a href=\"./"+LOG_PAGE+"\">Full Detail Log</a>\n");
		sb.append("<br><a href=\"./"+RULE_SUMMARY_PAGE+"\">Game Options Summary</a>\n");
		sb.append("</td></tr></table>\n");
		
		GamePool pool = new GamePool(RealmObjectMaster.getRealmObjectMaster(data).getPlayerCharacterObjects());
		Collection characterGameObjects = pool.extract(CharacterWrapper.getKeyVals());
		File dir = new File(path+CHAR_DIR);
		if (!dir.exists()) {
			dir.mkdir();
		}
		ArrayList<String> playerNames = new ArrayList<String>();
		ArrayList<String> employerNames = new ArrayList<String>();
		HashLists characterHash = new HashLists();
		HashLists minionHash = new HashLists();
		for (Iterator i = characterGameObjects.iterator(); i.hasNext();) {
			GameObject go = (GameObject) i.next();
			CharacterWrapper character = new CharacterWrapper(go);
			if (character.isCharacter()) {
				String playerName = character.getPlayerName();
				if (!playerNames.contains(playerName)) {
					playerNames.add(playerName);
				}
				characterHash.put(playerName,character);
			}
			else {
				if (!character.isDead()) {
					String employer = character.getEmployer().getName();
					if (!employerNames.contains(employer)) {
						employerNames.add(employer);
					}
					minionHash.put(employer,character);
				}
			}
		}
		createDieImages(path+CHAR_DIR);
		Collections.sort(playerNames);
		sb.append("<h2>Characters:</h2>\n");
		sb.append("<table border=\"0\" cellpadding=\"2\" cellspacing=\"2\">\n");
		for (String playerName:playerNames) {
			sb.append("<tr><td valign=\"top\" bgcolor=\"#cccccc\"><h3>");
			sb.append(playerName);
			sb.append("</h3></td><td valign=\"top\">\n");
			ArrayList list = characterHash.getList(playerName);
			if (list==null || list.isEmpty()) continue;
			Collections.sort(list,new Comparator<CharacterWrapper>() {
				public int compare(CharacterWrapper o1, CharacterWrapper o2) {
					// Sort by start date
					DayKey key1 = new DayKey((String)o1.getAllDayKeys().get(0));
					DayKey key2 = new DayKey((String)o2.getAllDayKeys().get(0));
					int ret = key1.compareTo(key2);
					if (ret==0) {
						ret = o1.getCharacterName().compareTo(o2.getCharacterName());
					}
					return ret;
				}
			});
			for (Iterator i=list.iterator();i.hasNext();) {
				CharacterWrapper character = (CharacterWrapper)i.next();
				String charHtmlPath = CHAR_PREFIX+character.getGameObject().getId();
				saveCharacter(path+CHAR_DIR,charHtmlPath,character,title);
				sb.append("<a href=\"."+CHAR_DIR+"/"+charHtmlPath+".html\">");
				sb.append(character.getCharacterName());
				sb.append("</a>");
				if (character.isDead()) {
					sb.append(" (Dead)");
				}
				sb.append(" - From ");
				ArrayList dayKeys = character.getAllDayKeys();
				if (dayKeys!=null && dayKeys.size()>0) {
					sb.append((new DayKey((String)dayKeys.get(0))).getReadable());
					sb.append(" to ");
					sb.append((new DayKey((String)dayKeys.get(dayKeys.size()-1))).getReadable());
					sb.append(" (");
					sb.append(character.getAllDayKeys().size());
					sb.append(" days)");
				}
				sb.append("\n<br>");
			}
			sb.append("</td></tr>");
		}
		sb.append("</table>\n");
		
		sb.append("<h2>Hired Leaders:</h2>\n");
		sb.append("<table border=\"0\" cellpadding=\"2\" cellspacing=\"2\">\n");
		for (String employerName:employerNames) {
			sb.append("<tr><td valign=\"top\" bgcolor=\"#cccccc\"><h3>");
			sb.append(employerName);
			sb.append("</h3></td><td valign=\"top\">\n");
			ArrayList list = minionHash.getList(employerName);
			if (list==null || list.isEmpty()) continue;
			Collections.sort(list,new Comparator<CharacterWrapper>() {
				public int compare(CharacterWrapper o1, CharacterWrapper o2) {
					// Sort by start date
					ArrayList list1 = o1.getAllDayKeys();
					ArrayList list2 = o2.getAllDayKeys();
					DayKey key1 = list1==null?new DayKey(1,1):new DayKey((String)list1.get(0));
					DayKey key2 = list2==null?new DayKey(1,1):new DayKey((String)list2.get(0));
					int ret = key1.compareTo(key2);
					if (ret==0) {
						ret = o1.getCharacterName().compareTo(o2.getCharacterName());
					}
					return ret;
				}
			});
			for (Iterator i=list.iterator();i.hasNext();) {
				CharacterWrapper character = (CharacterWrapper)i.next();
				String charHtmlPath = CHAR_PREFIX+character.getGameObject().getId();
				saveCharacter(path+CHAR_DIR,charHtmlPath,character,title);
				sb.append("<a href=\"."+CHAR_DIR+"/"+charHtmlPath+".html\">");
				sb.append(character.getCharacterName());
				sb.append("</a>");
				ArrayList dayKeys = character.getAllDayKeys();
				if (dayKeys!=null && dayKeys.size()>0) {
					sb.append(" - From ");
					sb.append((new DayKey((String)dayKeys.get(0))).getReadable());
					sb.append(" to ");
					sb.append((new DayKey((String)dayKeys.get(dayKeys.size()-1))).getReadable());
					sb.append(" (");
					sb.append(character.getAllDayKeys().size());
					sb.append(" days)");
				}
				else {
					sb.append(" - New Hire");
				}
				sb.append("\n<br>");
			}
			sb.append("</td></tr>");
		}
		sb.append("</table>\n");
		
		writeString(path+File.separator+DAY_PAGE,title,sb.toString());
	}
	private void createDieImages(String path) {
		for (int i=1;i<=6;i++) {
			DieRoller dr = new DieRoller(String.valueOf(i),20,5);
			dr.setAllRed();
			exportImage(path+File.separator+DIE_PREFIX+i+".jpg",dr.getIcon(),1.0f,Color.lightGray);
		}
	}
	private void saveCharacter(String path,String filename,CharacterWrapper character,String title) {
		StringBuilder sb = new StringBuilder();
		sb.append("<h1>");
		sb.append(character.getCharacterName()+" ("+character.getPlayerName()+")");
		if (character.isDead()) {
			sb.append(" - ");
			sb.append("<font color=\"red\">");
			String reason = character.getDeathReason();
			sb.append(reason==null?"DEAD":reason);
			sb.append("</font>");
		}
		sb.append("</h1>\n");
		if (character.isCharacter()) {
			sb.append("<table border=\"0\" cellpadding=\"2\" cellspacing=\"2\" width=\"100%\">\n");
			sb.append("<tr><td valign=\"top\" width=\"1\">\n");
			// picture here
			ImageIcon pic = CharacterChooser.getCharacterImage(character.getGameObject());
			pic = new ImageIcon(pic.getImage().getScaledInstance(pic.getIconWidth()>>1,pic.getIconHeight()>>1,Image.SCALE_SMOOTH));
			exportImage(path+File.separator+filename+".jpg",pic,1.0f,null);
			sb.append("<img alt=\""+character.getCharacterName()+"\" src=\""+filename+".jpg\" >");
			sb.append("</td><td valign=\"top\">\n");
			ChitStateViewer chits = new ChitStateViewer(null,character);
			chits.pack();
			saveComponentImage(path+File.separator+filename+"_chits.jpg",chits.getMasterPanel());
			sb.append("<img alt=\""+character.getCharacterName()+" Chits\" src=\""+filename+"_chits.jpg\" >");
			sb.append("</td></tr></table>\n");
		}
		
		// Stats and stuff
		sb.append("<table border=\"0\" cellpadding=\"2\" cellspacing=\"2\" width=\"100%\">\n");
		sb.append("<th bgcolor=\"#33ccff\">Stats</th><th bgcolor=\"#ffcc66\">Active Inventory</th><th bgcolor=\"#aaaaaa\">Inactive Inventory</th><th bgcolor=\"#66ff99\">Hirelings</th>\n<tr>\n");
		sb.append("<td valign=\"top\" width=\"25%\">\n");
		populateStats(sb,character);
		sb.append("</td>\n<td align=\"center\" valign=\"top\" width=\"25%\">\n");
		populateInventory(path,sb,character,true);
		sb.append("</td>\n<td align=\"center\" valign=\"top\" width=\"25%\">\n");
		populateInventory(path,sb,character,false);
		sb.append("</td>\n");
		if (character.isCharacter()) {
			sb.append("<td align=\"center\" valign=\"top\" width=\"25%\">\n");
			populateHirelings(path,sb,character);
			sb.append("</td>\n");
		}
		sb.append("</tr></table>");
		
		// Summary
		sb.append("<table border=\"0\" cellpadding=\"2\" cellspacing=\"2\" width=\"100%\">");
		sb.append("<th bgcolor=\"#cccccc\" colspan=\"6\"><font size=\"+1\">Event Log</font></th>");
		sb.append("<tr><td width=\"60\" bgcolor=\"#cccccc\"><b>Month</b></td>" +
				"<td width=\"60\" bgcolor=\"#cccccc\"><b>Day</b></td>" +
				"<td width=\"60\"bgcolor=\"#cccccc\"><b>Monster Roll</b></td>" +
				"<td bgcolor=\"#cccccc\"><b>Actions</b></td>" +
				"<td bgcolor=\"#cccccc\"><b>Summary</b></td>" +
				"<td bgcolor=\"#cccccc\"><b>Kills</b></td></tr>");
		ArrayList dayKeys = character.getAllDayKeys();
		boolean grayed = false;
		String currentDay = DayKey.getString(game.getMonth(),game.getDay());
		if (dayKeys!=null) {
			for (Iterator i=dayKeys.iterator();i.hasNext();) { // Got a NPE here??  Why does THAT happen?  dayKeys must be null, but how?
				String key = (String)i.next();
				
				// Only show the currentDay if the character has played their turn!
				if (currentDay!=null && currentDay.equals(key)) {
					if (game.isRecording() || (game.isDaylight() && character.getPlayOrder()>0)) {
						break;
					}
				}
				
				if (grayed) {
					sb.append("<tr bgcolor=\"ffdddd\">");
				}
				else {
					sb.append("<tr>");
				}
				sb.append("<td valign=\"top\">");
				sb.append(DayKey.getMonth(key));
				sb.append("</td><td valign=\"top\">");
				sb.append(DayKey.getDay(key));
				sb.append("</td><td valign=\"top\">");
				DieRoller roller = character.getMonsterRoll(key);
				if (roller!=null) {
					for (int d=0;d<roller.getNumberOfDice();d++) {
						int roll = roller.getValue(d);
						sb.append("<img alt=\"");
						sb.append(roll);
						sb.append("\" src=\"");
						sb.append(DIE_PREFIX);
						sb.append(roll);
						sb.append(".jpg\" />");
					}
				}
				sb.append("</td><td valign=\"top\">");
				sb.append(getActionString(character,key));
				sb.append("</td><td valign=\"top\">");
				sb.append(getSummary(character,key));
				sb.append("</td><td valign=\"top\">");
				
				populateKills(path,sb,character,key);
				sb.append("</td></tr>");
				grayed = !grayed;
			}
		}
		sb.append("</table>");
		
		writeString(path+File.separator+filename+".html",title+" - "+character.getCharacterName(),sb.toString(),"../"+DAY_PAGE);
	}
	private String getActionString(CharacterWrapper character,String dayKey) {
		StringBufferedList sbl = new StringBufferedList(" , ","");
		ArrayList list = character.getActions(dayKey);
		if (list!=null) {
			for (Iterator i=list.iterator();i.hasNext();) {
				String val = (String)i.next();
				sbl.append(val);
			}
		}
		String ret = sbl.toString();
		if (ret.trim().length()==0) {
			ret = "No actions.";
		}
		return ret;
	}
	private String getSummary(CharacterWrapper character,String dayKey) {
		StringBuilder sb = new StringBuilder();
		ArrayList list = character.getActions(dayKey);
		if (list!=null) {
			String lastMove = null;
			for (Iterator i=list.iterator();i.hasNext();) {
				String val = (String)i.next();
				if ("BLOCKED".equals(val)) {
					break;
				}
				if (val.startsWith("M-")) {
					int comma = val.indexOf(',');
					if (comma>0) {
						lastMove = val.substring(2,comma);
					}
					else {
						lastMove = val.substring(2);
					}
				}
			}
			// moved to
			if (lastMove!=null) {
				sb.append("Moved to ");
				TileLocation tl = TileLocation.parseTileLocation(data,lastMove);
				sb.append(tl.toString());
				sb.append(".  ");
			}
		}
		return sb.toString();
	}
	private void populateKills(String path,StringBuilder sb,CharacterWrapper character,String dayKey) {
		// kills
		ArrayList<GameObject> kills = character.getKills(dayKey);
		if (!kills.isEmpty()) {
			for (GameObject kill:kills) {
				RealmComponent rc = RealmComponent.getRealmComponent(kill);
				exportImage(path+File.separator+KILL_PREFIX+killIndex+".jpg",rc.getIcon(),1.0f,Color.white);
				sb.append("<img alt=\""+kill.getName()+"\" src=\""+KILL_PREFIX+killIndex+".jpg\" >\n");
				killIndex++;
			}
		}
	}
	private void populateStats(StringBuilder sb,CharacterWrapper character) {
		sb.append("<table border=\"0\" cellpadding=\"2\" cellspacing=\"2\" width=\"100%\">\n");
		// Fame
		sb.append("<tr><td valign=\"top\" align=\"right\" bgcolor=\"#cccccc\"><b>Fame:</b></td><td valign=\"top\">");
		sb.append(character.getFameString());
		sb.append("</td></tr>\n");
		// Notoriety
		sb.append("<tr><td valign=\"top\" align=\"right\" bgcolor=\"#cccccc\"><b>Notoriety:</b></td><td valign=\"top\">");
		sb.append(character.getNotorietyString());
		sb.append("</td></tr>\n");
		// Curses
		sb.append("<tr><td valign=\"top\" align=\"right\" bgcolor=\"#cccccc\"><b>Curses:</b></td><td valign=\"top\">");
		if (character.hasCurses()) {
			StringBufferedList sbList = new StringBufferedList(", ",", ");
			if (character.hasCurse(Constants.ASHES)) sbList.append("Ashes");
			if (character.hasCurse(Constants.DISGUST)) sbList.append("Disgust");
			if (character.hasCurse(Constants.EYEMIST)) sbList.append("Eyemist");
			if (character.hasCurse(Constants.ILL_HEALTH)) sbList.append("Ill Health");
			if (character.hasCurse(Constants.SQUEAK)) sbList.append("Squeak");
			if (character.hasCurse(Constants.WITHER)) sbList.append("Wither");
			sb.append(sbList.toString());
		}
		else {
			sb.append("None");
		}
		sb.append("</td></tr>\n");
		// Advantages
		populateAdvantages(sb,"Special Adv",character.getGameObject().getThisAttributeList("advantages"));
		populateAdvantages(sb,"Optional Adv",character.getGameObject().getAttributeList("optional","advantages"));
		// Relationships
		populatePolitics(sb,"Ally",character,RelationshipType.ALLY);
		populatePolitics(sb,"Friendly",character,RelationshipType.FRIENDLY);
		populatePolitics(sb,"Unfriendly",character,RelationshipType.UNFRIENDLY);
		populatePolitics(sb,"Enemy",character,RelationshipType.ENEMY);

		sb.append("</table>");
	}
	private void populateAdvantages(StringBuilder sb,String title,ArrayList list) {
		sb.append("<tr><td valign=\"top\" align=\"right\" bgcolor=\"#cccccc\"><b>"+title+":</b></td><td valign=\"top\">");
		if (list==null || list.isEmpty()) {
			sb.append("None");
		}
		else {
			sb.append("<ol>");
			for (int i=0;i<list.size();i++) {
				String adv = (String)list.get(i);
				sb.append("<li>");
				sb.append(adv);
				sb.append("</li>\n");
			}
			sb.append("</ol>");
		}
		sb.append("</td></tr>\n");
	}
	private void populatePolitics(StringBuilder sb,String title,CharacterWrapper character,int rel) {
		ArrayList<String> list = character.getRelationshipList(Constants.GAME_RELATIONSHIP,rel);
		if (!list.isEmpty()) {
			sb.append("<tr><td valign=\"top\" align=\"right\" bgcolor=\"#cccccc\"><b>"+title+":</b></td><td valign=\"top\">");
			sb.append(RealmUtility.getHTMLPoliticsString(list));
			sb.append("</td></tr>\n");
		}
	}
	private void populateHirelings(String path,StringBuilder sb,CharacterWrapper character) {
		for (RealmComponent rc:character.getAllHirelings()) {
			exportImage(path+File.separator+HIRELING_PREFIX+hirelingIndex+".jpg",rc.getIcon(),1.0f,Color.white);
			sb.append("<img alt=\""+rc.getGameObject().getName()+"\" src=\""+HIRELING_PREFIX+hirelingIndex+".jpg\" >\n");
			hirelingIndex++;
		}
	}
	private void saveRuleSummary(String path,String title) {
		StringBuilder sb = new StringBuilder();
		HostGameSetupDialog dialog = new HostGameSetupDialog(null,"",data);
		dialog.loadPrefsFromData();
		GameOptionPane gop = dialog.getGameOptionPane();
		sb.append("<h1>Game Options Summary</h1>");
		sb.append("<table border=\"0\" cellpadding=\"2\" cellspacing=\"2\" width=\"100%\">\n");
		sb.append("<th bgcolor=\"#33ff33\">ACTIVE</th><th bgcolor=\"#ff6666\">NOT USED</th>\n<tr>\n");
		sb.append("<td valign=\"top\" width=\"50%\">\n");
		populateRules(sb,gop,true);
		sb.append("</td>\n<td valign=\"top\" width=\"50%\">\n");
		populateRules(sb,gop,false);
		sb.append("</td>\n");
		sb.append("</tr></table>");
		writeString(path+File.separator+RULE_SUMMARY_PAGE,title+" - Rule Summary",sb.toString());
	}
	private void populateRules(StringBuilder sb,GameOptionPane gop,boolean active) {
		String[] tabKeys = gop.getTabKeys();
		for (int i=0;i<tabKeys.length;i++) {
			String[] options = gop.getOptionDescriptions(tabKeys[i],active);
			if (options!=null && options.length>0) {
				sb.append("<h3>");
				sb.append(tabKeys[i]);
				sb.append("</h3>\n");
				sb.append("<ul>");
				for (int n=0;n<options.length;n++) {
					sb.append("<li>");
					sb.append(options[n]);
					sb.append("</li>");
				}
				sb.append("</ul>");
			}
		}
	}
}