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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.robin.game.objects.GameObject;
import com.robin.general.graphics.GraphicsUtil;
import com.robin.general.graphics.TextType;
import com.robin.general.graphics.TextType.Alignment;
import com.robin.general.swing.DieRoller;
import com.robin.general.util.HashLists;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.RollerResult;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.*;

public abstract class CombatSheet extends JLabel implements Scrollable {
	
	private static final int CHIT_OFFSET = 5;
	protected static final String[] horseRiderSplit = {"w/ Horse","Separate"};
	
	public abstract boolean hasUnpositionedDenizens();
	public abstract boolean usesMaxCombatBoxes();
	public abstract boolean needsTargetAssignment();
	
	protected abstract Point[] getPositions();
	protected abstract ImageIcon getImageIcon();
	protected abstract void updateHotSpots();
	protected abstract String[] splitHotSpot(int index);
	protected abstract void updateLayout();
	protected abstract void handleClick(int index,int swingConstant);
	protected abstract void drawRollers(Graphics g);
	protected abstract void drawOther(Graphics g);
	protected abstract int getDeadBoxIndex();
	
	private Point[] positions;		// Position of every hotspot
	private int[] offset;			// Token draw offset for every hotspot
	protected CombatFrame combatFrame;
	protected BattleModel model;
	protected RealmComponent sheetOwner;
	protected Collection sheetParticipants;
	protected HashLists layoutHash;
	protected Hashtable rollerHash; // indexes to rollers
	
	protected Integer mouseHoverIndex;
	protected boolean mouseHoverShift = false;
	protected ArrayList battleChitsWithRolls;
	
	protected Hashtable hotspotHash; // hotspot indices (Integer) are the key, and the value is a String
	
	protected RollerGroup redGroup;
	protected RollerGroup squareGroup;
	protected RollerGroup circleGroup;
	
	protected boolean interactiveFrame;
	
	public boolean alwaysSecret = false;
	
	/**
	 * Testing constructor ONLY!!!
	 */
	protected CombatSheet() {
		battleChitsWithRolls = new ArrayList();
		hotspotHash = new Hashtable();
		layoutHash = new HashLists();
		mouseHoverIndex = null;
		positions = getPositions();
		offset = new int[positions.length];
		setIcon(getImageIcon());
	}
	protected CombatSheet(CombatFrame frame,BattleModel model,RealmComponent participant,boolean interactiveFrame) {
		super("");
		this.combatFrame = frame;
		this.model = model;
		this.sheetOwner = participant;
		this.interactiveFrame = interactiveFrame;
		battleChitsWithRolls = new ArrayList();
		hotspotHash = new Hashtable();
		layoutHash = new HashLists();
		mouseHoverIndex = null;
		positions = getPositions();
		offset = new int[positions.length];
		setIcon(getImageIcon());
		updateRollers();
		addKeyListener(shiftKeyListener);
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT,0),"dummy"); // this reserves the SHIFT key, somehow!?
		// There should really be an action mapping, but for some reason, its not used anyway!!!
	}
	public boolean hasHotspots() {
		return hotspotHash.size()>0;
	}
	public RealmComponent getSheetOwner() {
		return sheetOwner;
	}
	public TileLocation getBattleLocation() {
		return combatFrame.getBattleModel().getBattleLocation();
	}
	/**
	 * Gets a list of all RCs in the three boxes.  Excludes any character attacks, horses, or parts
	 */
	protected ArrayList getAllBoxListFromLayout(int box1) {
		ArrayList all = new ArrayList();
		for (int i=0;i<3;i++) {
			ArrayList list = layoutHash.getList(new Integer(box1+i));
			if (list!=null) {
				for (Iterator n=list.iterator();n.hasNext();) {
					RealmComponent rc = (RealmComponent)n.next();
					if (!rc.isNativeHorse() && !rc.isMonsterPart() && !rc.isActionChit()) {
						all.add(rc);
					}
				}
			}
		}
		return all;
	}
	protected DieRoller makeRoller(String val) {
		return new DieRoller(val,25,6);
	}
	protected boolean usesMaxCombatBoxes(int index) {
		int total = 0;
		int boxCount = 0;
		
		for (int i=index;i<index+3;i++) {
			int count = countAttacks(i);
			if (count>0) boxCount++;
			total += count;
		}
		
		return total>2?boxCount==3:boxCount==total;
	}
	/**
	 * @return		The total number of attacks in the box:  doesn't include horses!
	 */
	protected int countAttacks(int index) {
		int count = 0;
		ArrayList list = layoutHash.getList(new Integer(index));
		if (list!=null) {
			for (Iterator i=list.iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				if (!rc.isNativeHorse() && !rc.isActionChit()) {
					count++;
				}
			}
		}
//System.out.println(index+" "+list+" count="+count);
		return count;
	}
	protected void updateBattleChitsWithRolls(CombatWrapper combat) {
//System.out.println("updateBattleChitsWithRolls from "+combat.getGameObject().getName());
		if ((combat.getMissileRolls()!=null && combat.getMissileRolls().size()>0)
				|| (combat.getFumbleRolls()!=null && combat.getFumbleRolls().size()>0)) {
			BattleChit bc = (BattleChit)RealmComponent.getRealmComponent(combat.getGameObject());
			battleChitsWithRolls.add(bc);
		}
	}
	private ArrayList getBattleRolls() {
		ArrayList battleRolls = new ArrayList();
		if (!battleChitsWithRolls.isEmpty()) {
			// Deduce and build all fumble/stumble rolls (in order)
			if (combatFrame.getCurrentRound()==1) {
				Collections.sort(battleChitsWithRolls,new BattleChitLengthComparator());
			}
			else {
				Collections.sort(battleChitsWithRolls,new BattleChitSpeedComparator());
			}
			for (Iterator i=battleChitsWithRolls.iterator();i.hasNext();) {
				BattleChit bc = (BattleChit)i.next();
				
				RealmComponent rc = (RealmComponent)bc;
				CombatWrapper combat = new CombatWrapper(bc.getGameObject());
				
				ArrayList missileResults = combat.getMissileRolls();
				ArrayList fumbleResults = combat.getFumbleRolls();
				
				String type;
				ArrayList rs,ss;
				if (fumbleResults!=null) {
					type = " Fumble";
					rs = fumbleResults;
					ss = combat.getFumbleRollSubtitles();
				}
				else {
					type = " Missile";
					rs = missileResults;
					ss = combat.getMissileRollSubtitles();
				}
				
				String prefix = (rc.isCharacter()?"":("B"+combat.getCombatBox()+" "));
				Iterator r=rs.iterator();
				Iterator s=ss.iterator();
				while(r.hasNext()) {
					// TODO Would be nice to recognize whether a roll refers to a target on this
					// sheet or not.
					RollerResult rr = new RollerResult(
							prefix+combat.getGameObject().getName()+type,
							(String)r.next(),
							(String)s.next());
					battleRolls.add(rr);
				}
			}
		}
			
		// lastly, add any serious wounds results
		CharacterWrapper character = combatFrame.getActiveCharacter();
		if (character!=null) {
			CombatWrapper combat = new CombatWrapper(character.getGameObject());
			ArrayList list = combat.getSeriousWoundRolls();
			if (list!=null) {
				for (Iterator i=list.iterator();i.hasNext();) {
					String result = (String)i.next();
					RollerResult rr = new RollerResult(character.getCharacterName()+" Serious Wound",result,"");
					battleRolls.add(rr);
				}
			}
		}
		return battleRolls;
	}
	public boolean hasBattleRolls() {
		return !getBattleRolls().isEmpty();
	}
	protected void updateRollerResults() {
//System.out.println("updateRollerResults: battleChitsWithRolls.isEmpty()=="+battleChitsWithRolls.isEmpty());
		if (combatFrame.getRollerResults()!=null) {
//System.out.println("doing it");
			combatFrame.getRollerResults().setBattleRolls(getBattleRolls());
		}
	}
	public void updateMouseHover(Point p) {
		updateMouseHover(p,false);
	}
	public void updateMouseHover(Point p,boolean isShiftDown) {
		Integer newIndex = null;
		if (p!=null) {
			for (Iterator i=layoutHash.keySet().iterator();i.hasNext();) {
				int range = HOTSPOT_SIZE>>1;
				Integer index = (Integer)i.next();
				Point test = positions[index.intValue()];
				if (test!=null) {
					int dx = Math.abs(test.x-p.x);
					int dy = Math.abs(test.y-p.y);
					if (dx<range && dy<range) {
						newIndex = index;
						break;
					}
				}
			}
		}
		boolean sameIndex = mouseHoverIndex==null?newIndex==null:mouseHoverIndex.equals(newIndex);
		if (!sameIndex || mouseHoverShift!=isShiftDown) {
			mouseHoverIndex = newIndex;
			mouseHoverShift = isShiftDown;
			repaint();
		}
	}
	public void handleClick(Point p) {
		for (Iterator i=hotspotHash.keySet().iterator();i.hasNext();) {
			int range = HOTSPOT_SIZE>>1;
			Integer index = (Integer)i.next();
			Point test = positions[index.intValue()];
			int dx = Math.abs(test.x-p.x);
			int dy = Math.abs(test.y-p.y);
			if (dx<range && dy<range) {
				int side = p.x<test.x?SwingConstants.LEFT:SwingConstants.RIGHT;
//System.out.println(p.x<test.x?"left":"right");
				handleClick(index.intValue(),side);
				combatFrame.updateHotspotIndicators();
				break;
			}
		}
	}
	public Collection getAllParticipantsOnSheet() {
		ArrayList list = new ArrayList();
		for (Iterator i=layoutHash.values().iterator();i.hasNext();) {
			ArrayList in = (ArrayList)i.next();
			for (Iterator n=in.iterator();n.hasNext();) {
				RealmComponent rc = (RealmComponent)n.next();
				if (rc.isMonster() || rc.isCharacter() || rc.isNative()) {
					list.add(rc);
				}
			}
		}
		return list;
	}
	public void paint(Graphics g) {
		//super.paint(g);
		g.drawImage(getImageIcon().getImage(),0,0,null);
		
		// Draw components
		Arrays.fill(offset,0);
		for (Iterator i=layoutHash.keySet().iterator();i.hasNext();) {
			Integer index = (Integer)i.next();
			ArrayList list = layoutHash.getList(index);
			Collections.sort(list);
			for (Iterator n=list.iterator();n.hasNext();) {
				RealmComponent rc = (RealmComponent)n.next();
				paintRealmComponent(g,rc,index.intValue());
			}
		}
		
		// Draw hotspots
		ArrayList hotspotKeys = new ArrayList(hotspotHash.keySet());
		Collections.sort(hotspotKeys);
		for (Iterator i=hotspotKeys.iterator();i.hasNext();) {
			Integer index = (Integer)i.next();
			String name = (String)hotspotHash.get(index);
			paintHotSpot(g,name,index.intValue());
		}
		
		// Draw Rollers (if any)
		drawRollers(g);
		
		// Other info
		drawOther(g);
		
		// Draw mouse hover
		if (mouseHoverIndex!=null) {
			Dimension maxSize = getSize();
			int contentsX = 5;
			int contentsY = 5;
			
			ArrayList c = layoutHash.getList(mouseHoverIndex);
			
			if (c!=null) {
				Rectangle[] plot = new Rectangle[c.size()];
				int n=0;
				for (Iterator i=c.iterator();i.hasNext();) {
					RealmComponent rc = (RealmComponent)i.next();
					Dimension d = rc.getSize();
					plot[n++] = new Rectangle(contentsX,contentsY,d.width,d.height);
					contentsX += d.width;
					contentsX += 5;
				}
				// Resize if the contents run off the edge
				Rectangle test = plot[plot.length-1];
				if ((test.x+test.width)>maxSize.width) {
					double scaling = ((double)(maxSize.width-test.width))/((double)(test.x));
					for (int i=0;i<plot.length;i++) {
						plot[i].x = (int)((double)plot[i].x*scaling);
					}
				}
				
				// Finally, draw them
				n=0;
				for (Iterator i=c.iterator();i.hasNext();) {
					RealmComponent rc = (RealmComponent)i.next();
					Rectangle r = plot[n++];
					if (mouseHoverShift && rc.isChit()) {
						// draw flipside
						ChitComponent cc = (ChitComponent)rc;
						g.drawImage(cc.getFlipSideImage(),r.x,r.y,null);
					}
					else {
						rc.paint(g.create(r.x,r.y,r.width,r.height));
					}
				}
			}
		}
		
		// Draw consecutive rounds of combat without damage/fatigue/spellcasting
		if (combatFrame!=null) {
			CombatWrapper tile = new CombatWrapper(combatFrame.getBattleModel().getBattleLocation().tile.getGameObject());
			int n = tile.getRoundsOfMissing();
			if (combatFrame.getActionState()>=Constants.COMBAT_RESOLVING) n--;
			if (n>0) {
				g.setColor(Color.red);
				g.setFont(Constants.HOTSPOT_FONT);
				g.drawString("There was no damage, fatigue, or spellcasting last round.",5,15);
			}
		}
	}
	protected String getSubtitleForReposition(DieRoller roller) {
		if (roller!=null) {
			switch(roller.getHighDieResult()) {
				case 1:		return "Switch Box 2 & 3";
				case 2:		return "Switch Box 1 & 3";
				case 3:		return "Switch Box 1 & 2";
				case 4:		return "No Change";
				case 5:		return "Move Down & Right";
				case 6:		return "Move Up & Left";
			}
		}
		return null;
	}
	protected String getSubtitleForTactics(DieRoller roller) {
		String ret = null;
		if (roller!=null) {
			ret = roller.getHighDieResult()==6?"Flipped!":null;
		}
		return ret;
	}
	protected void paintRoller(Graphics g,String title,String subtitle,DieRoller roller,int index,int xoff,int yoff) {
		if (roller!=null) {
			Dimension size = roller.getPreferredSize();
			
			Point p = positions[index];
			int x = p.x - (size.width>>1) + xoff;
			int y = p.y - (size.height>>1) + yoff;
			
			roller.paintComponent(g.create(x,y,size.width,size.height));
			
			Border lineBorder = new LineBorder(Color.black,1);
			Border titleBorder = BorderFactory.createTitledBorder(
					lineBorder,
					title,
					TitledBorder.CENTER,
					TitledBorder.TOP,
					Constants.HOTSPOT_FONT,
					Color.black);
			titleBorder.paintBorder(this,g,x,y-10,size.width,size.height+10);
			
			if (subtitle!=null) {
				g.setFont(Constants.RESULT_FONT);
				g.setColor(Color.yellow);
				GraphicsUtil.drawCenteredString(g,x+1,y+16,size.width,size.height+10,subtitle);
				g.setColor(Color.blue);
				GraphicsUtil.drawCenteredString(g,x,y+15,size.width,size.height+10,subtitle);
			}
		}
	}
	protected void placeParticipant(RealmComponent participant,int layoutIndex1) {
		placeParticipant(participant,layoutIndex1,false,false);
	}
	protected void placeParticipant(RealmComponent participant,int layoutIndex1,boolean secrecy,boolean horseSameBox) {
		CombatWrapper combat;
		int box;
		
		// Place horse (if any) first
		RealmComponent horse = (RealmComponent)participant.getHorse();
		if (horse!=null) {
			combat = new CombatWrapper(horse.getGameObject());
			box = combat.getCombatBox();
			if (box==0) {
				if (horseSameBox) {
					combat.setCombatBox(1);
					box = 1;
				}
				else {
					combat.setCombatBox(2);
					box = 2;
				}
			}
			if (secrecy) {
				box = 0;
			}
			layoutHash.put(new Integer(layoutIndex1+box-1),horse);
		}
		
		// Place participant
		combat = new CombatWrapper(participant.getGameObject());
		updateBattleChitsWithRolls(combat);
		
		box = combat.getCombatBox();
		if (box==0) {
			combat.setCombatBox(1);
			box = 1;
		}
		if (secrecy) {
			box = 0;
		}
		layoutHash.put(new Integer(layoutIndex1+box-1),participant);
		
		// Place weapon (if any)
		if (participant.isMonster()) {
			MonsterChitComponent monster = (MonsterChitComponent)participant;
			MonsterPartChitComponent weapon = monster.getWeapon();
			if (weapon!=null) {
				combat = new CombatWrapper(weapon.getGameObject());
				updateBattleChitsWithRolls(combat);
				box = combat.getCombatBox();
				if (box==0) {
					combat.setCombatBox(2);
					box = 2;
				}
				if (secrecy) {
					box = 0;
				}
				layoutHash.put(new Integer(layoutIndex1+box-1),weapon);
			}
		}
	}
	protected boolean addedToDead(RealmComponent rc) {
		CombatWrapper combat = new CombatWrapper(rc.getGameObject());
		if (combat.isDead()) {
			if (combatFrame.getActionState()<Constants.COMBAT_RESOLVING) {
				layoutHash.put(new Integer(getDeadBoxIndex()),rc);
				return true;
			}
			else if (combatFrame.getActionState()==Constants.COMBAT_RESOLVING) {
				if (combat.getCombatBox()==0) {
					layoutHash.put(new Integer(getDeadBoxIndex()),rc);
					return true;
				}
			}
		}
		return false;
	}
	protected void placeAllAttacks(int attackBox1,int weaponBox1,Collection excludeList) {
		boolean reveal = combatFrame.getActionState()>=Constants.COMBAT_RESOLVING;
		
		ArrayList all = new ArrayList(model.getAllBattleParticipants(true));
		
		// Sort by target index (lower first), to keep stack ordering correct
		Collections.sort(all,new TargetIndexComparator());
		
		/*
		 * Cycle through all participants that are in the model, that are targeting one of the monsters or
		 * natives in the target boxes.  (go into the attack boxes)
		 */
		for (Iterator i=all.iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			CombatWrapper rcCombat = new CombatWrapper(rc.getGameObject());
			RealmComponent target = rc.getTarget();
			GameObject spell = rcCombat.getCastSpell();
			
			// If targeting any of the sheetOwners targets, put them on the sheet in the attacker boxes
			boolean isInactiveSheetOwner = target==null && spell==null && sheetOwner.equals(rc);
			boolean castingASpell = spell!=null;
			boolean targetingSomeoneOnThisSheet = target!=null && (sheetParticipants.contains(target) || sheetOwner.equals(target));
			if (!targetingSomeoneOnThisSheet && target!=null) {
				RealmComponent targetsTarget = target.getTarget();
				if (targetsTarget!=null && sheetParticipants.contains(targetsTarget)) {
					// This is sloppy, but will work
					targetingSomeoneOnThisSheet = true;
				}
			}
			if (isInactiveSheetOwner || targetingSomeoneOnThisSheet || castingASpell) {
				if (excludeList==null || !excludeList.contains(rc)) {
					if (!rc.isCharacter() && sheetParticipants.contains(target)) {
						if (!addedToDead(rc)) {
							placeParticipant(rc,attackBox1);
							sheetParticipants.add(rc);
						}
					}
					else if (rc.isCharacter()) {
						updateBattleChitsWithRolls(rcCombat);
						CharacterWrapper character = new CharacterWrapper(rc.getGameObject());
						CharacterChitComponent characterChit = (CharacterChitComponent)rc;
						if (spell==null) {
							// Only show attacks if attacking a non-owned target, OR the attacker is the activeParticipant
							if (reveal 
									|| (target==null)
									|| (target!=null && target.getOwnerId()==null)
									|| (target!=null && rc.equals(combatFrame.getActiveParticipant()))) {
								
								MonsterChitComponent transmorph = characterChit.getTransmorphedComponent();
								if (transmorph==null) {
									// Cycle through character fight chits and weapons for attack
									RealmComponent weapon = character.getActiveWeapon();
									if (weapon!=null) {
										CombatWrapper combat = new CombatWrapper(weapon.getGameObject());
										int box = combat.getCombatBox();
										if (box>0) {
											layoutHash.put(new Integer(weaponBox1+box-1),weapon);
										}
									}
									for (Iterator n=character.getActiveFightChits().iterator();n.hasNext();) {
										RealmComponent chit = (RealmComponent)n.next();
										CombatWrapper combat = new CombatWrapper(chit.getGameObject());
										int box = combat.getCombatBox();
										if (box>0 && combat.getPlacedAsFight()) {
											layoutHash.put(new Integer(attackBox1+box-1),chit);
										}
									}
									
									// Look for gloves, and/or weapon inventory
									for (Iterator n=character.getActiveInventory().iterator();n.hasNext();) {
										GameObject go = (GameObject)n.next();
										RealmComponent item = RealmComponent.getRealmComponent(go);
										if (item.getGameObject().hasThisAttribute("gloves")) {
											CombatWrapper combat = new CombatWrapper(item.getGameObject());
											int box = combat.getCombatBox();
											if (box>0) {
												layoutHash.put(new Integer(attackBox1+box-1),item);
											}
										}
										else if (item.getGameObject().hasThisAttribute("attack")) {
											CombatWrapper combat = new CombatWrapper(item.getGameObject());
											int box = combat.getCombatBox();
											if (box>0) {
												layoutHash.put(new Integer(weaponBox1+box-1),item);
											}
										}
									}
								}
								else {
									CombatWrapper combat = new CombatWrapper(transmorph.getGameObject());
									updateBattleChitsWithRolls(combat);
									int box = combat.getCombatBox();
									if (box>0) {
										layoutHash.put(new Integer(attackBox1+box-1),transmorph.getFightChit());
									}
									
									// Add monster weapon here
									MonsterPartChitComponent monsterWeapon = transmorph.getWeapon();
									if (monsterWeapon!=null) {
										combat = new CombatWrapper(monsterWeapon.getGameObject());
										updateBattleChitsWithRolls(combat);
										box = combat.getCombatBox();
										if (box>0) {
											layoutHash.put(new Integer(attackBox1+box-1),monsterWeapon);
										}
									}
								}
							}
						}
						else {
							// Attack spells are placed here
							ArrayList targetTest = new ArrayList();
							targetTest.addAll(sheetParticipants);
							targetTest.add(sheetOwner);
							SpellWrapper sw = new SpellWrapper(spell);
							if (sw.isAttackSpell() && sw.isAlive()) {
								ArrayList targeted = sw.getTargetedRealmComponents(targetTest);
								boolean showAttack = targeted.size()>0;
								
								// If the attacker is NOT the active participant and...
								if (!rc.equals(combatFrame.getActiveParticipant())) {
									// ... any of the targeted are owned by the activeParticipant, then don't show attack (maintains secrecy)
									for (Iterator n=targeted.iterator();n.hasNext();) {
										RealmComponent test = (RealmComponent)n.next();
										if (test.getOwnerId()!=null && test.getOwner().equals(combatFrame.getActiveParticipant())) {
											showAttack = false;
											break;
										}
									}
								}
								if (reveal || showAttack) {
									GameObject incObj = sw.getIncantationObject();
									CombatWrapper combat = new CombatWrapper(incObj);
									int box = combat.getCombatBox();
									if (box>0) {
										layoutHash.put(new Integer(weaponBox1+box-1),RealmComponent.getRealmComponent(spell));
									}
								}
							}
						}
					}
				}
			}
		}
	}
	private static final int HOTSPOT_SIZE = 100;
	private static final Color HOTSPOT_TITLE_COLOR = Color.blue;
	private static final Color HOTSPOT_LINE_COLOR = Color.green;//new Color(0,0,255,120);
	private static final Color HOTSPOT_BACKING = new Color(0,255,0,40);
	private static final Color HOTSPOT_SPLIT_BACKING = new Color(255,255,255,190);
	private void paintHotSpot(Graphics g,String string,int index) {
		Point p = positions[index];
		int x = p.x - (HOTSPOT_SIZE>>1);
		int y = p.y - (HOTSPOT_SIZE>>1);
		
		g.setColor(HOTSPOT_BACKING);
		g.fillRect(x,y+5,HOTSPOT_SIZE,HOTSPOT_SIZE-5);
		Border lineBorder = new LineBorder(HOTSPOT_LINE_COLOR,4,true);
		Border titleBorder = BorderFactory.createTitledBorder(
				lineBorder,
				string,
				TitledBorder.CENTER,
				TitledBorder.TOP,
				Constants.HOTSPOT_FONT,
				HOTSPOT_TITLE_COLOR);
		titleBorder.paintBorder(this,g,x,y,HOTSPOT_SIZE,HOTSPOT_SIZE+5);
		
		String[] split = splitHotSpot(index);
		if (split!=null) {
			int half = HOTSPOT_SIZE>>1;
			g.setColor(HOTSPOT_LINE_COLOR);
			g.fillRect(x+half-2,y+16,4,HOTSPOT_SIZE-16);
			
			g.setColor(HOTSPOT_SPLIT_BACKING);
			g.fillRect(x-2,y+15,12,HOTSPOT_SIZE-20);
			g.fillRect(x+90,y+15,12,HOTSPOT_SIZE-20);
			
			g.setColor(Color.black);
			g.setFont(Constants.RESULT_FONT);
			TextType.drawText(g,split[0],x,y+5,HOTSPOT_SIZE,90,Alignment.Center);
			TextType.drawText(g,split[1],x+93,y+5,HOTSPOT_SIZE,90,Alignment.Center);
		}
	}
	private void paintRealmComponent(Graphics g,RealmComponent rc,int index) {
		ImageIcon icon = rc.getIcon();
		Point p = positions[index];
		int x = p.x - (icon.getIconWidth()>>1) - offset[index];
		int y = p.y - (icon.getIconHeight()>>1) - offset[index];
		g.drawImage(icon.getImage(),x,y,null);
		offset[index] += CHIT_OFFSET;
	}
	// Scrollable interface
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}
	public Dimension getPreferredScrollableViewportSize() {
		return null;
	}
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 300;
	}
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 20;
	}
	protected void updateRollers() {
		CombatWrapper combat = new CombatWrapper(sheetOwner.getGameObject());
		redGroup = buildRollerGroup(CombatWrapper.GROUP_RED,combat);
		circleGroup = buildRollerGroup(CombatWrapper.GROUP_CIRCLE,combat);
		squareGroup = buildRollerGroup(CombatWrapper.GROUP_SQUARE,combat);
	}
	protected RollerGroup buildRollerGroup(String prefix,CombatWrapper combat) {
		RollerGroup rg = null;
		int reposition = combat.getRepositionResult(prefix);
		if (reposition>0) {
			rg = new RollerGroup();
			rg.repositionRoller = new DieRoller();
			rg.repositionRoller.adjustDieSize(25, 6);
			rg.repositionRoller.addRedDie();
			rg.repositionRoller.setValue(0,reposition);
			
			String changeTacs1 = combat.getChangeTacticsResult(prefix,1);
			if (changeTacs1!=null) {
				rg.changeTacticsRoller1 = makeRoller(changeTacs1);
			}
			String changeTacs2 = combat.getChangeTacticsResult(prefix,2);
			if (changeTacs2!=null) {
				rg.changeTacticsRoller2 = makeRoller(changeTacs2);
			}
			String changeTacs3 = combat.getChangeTacticsResult(prefix,3);
			if (changeTacs3!=null) {
				rg.changeTacticsRoller3 = makeRoller(changeTacs3);
			}
		}
		return rg;
	}
	protected void drawRollerGroup(Graphics g,RollerGroup rg,int repRoll,int ctRoll) {
		paintRoller(g,"Repositioning Roll",getSubtitleForReposition(rg.repositionRoller),rg.repositionRoller,repRoll,0,0);
		paintRoller(g,"Change Tactics",getSubtitleForTactics(rg.changeTacticsRoller1),rg.changeTacticsRoller1,ctRoll,0,-50);
		paintRoller(g,"Change Tactics",getSubtitleForTactics(rg.changeTacticsRoller2),rg.changeTacticsRoller2,ctRoll+1,0,-50);
		paintRoller(g,"Change Tactics",getSubtitleForTactics(rg.changeTacticsRoller3),rg.changeTacticsRoller3,ctRoll+2,0,-50);
	}
	
	/**
	 * @return		true if attacked by another character (hirelings are ignored for this check, however)
	 */
	protected boolean isAttackedByCharacter() {
		for (Iterator i=model.getAttackersFor(sheetOwner).iterator();i.hasNext();) {
			RealmComponent attacker = (RealmComponent)i.next();
			if (attacker.isCharacter()) {
				return true;
			}
		}
		return false;
	}
	
	protected void showBattlingNatives() { // FIXME Write this code
//		if (sheetOwner.equals(combatFrame.getActiveParticipant())) {
//			CharacterWrapper character = combatFrame.getActiveCharacter();
//			character.addBattlingNative()
//		}
	}
	
	protected static class RollerGroup {
		public DieRoller repositionRoller;
		public DieRoller changeTacticsRoller1;
		public DieRoller changeTacticsRoller2;
		public DieRoller changeTacticsRoller3;
	}
	
	public boolean containsHorse(ArrayList list) {
		if (list!=null) {
			for (Iterator i=list.iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				if (rc.hasHorse()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean containsEnemy(RealmComponent attacker,ArrayList list) {
		if (list!=null) {
			for (Iterator i=list.iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				if (combatFrame.allowsTreachery() || !attacker.equals(rc.getOwner())) {
					return true;
				}
			}
		}
		return false;
	}
	public boolean containsFriend(RealmComponent attacker,ArrayList list) {
		if (list!=null) {
			for (Iterator i=list.iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				if (attacker.equals(rc.getOwner())) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Returns true, if the list contains at least one friend, or one unhired denizen.
	 */
	public static boolean containsFriendOrDenizen(RealmComponent attacker,ArrayList list) {
		if (list!=null) {
			for (Iterator i=list.iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				if (rc.isNative() || rc.isMonster() || rc.isCharacter() || rc.isActionChit()) {
					RealmComponent owner = rc.getOwner();
					if (owner==null || attacker.equals(rc.getOwner())) {
						return true;
					}
				}
			}
		}
		return false;
	}
	public static ArrayList filterEnemies(RealmComponent attacker,ArrayList list) {
		if (list!=null) {
			ArrayList ret = new ArrayList();
			for (Iterator i=list.iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				if (!attacker.equals(rc.getOwner())) {
					ret.add(rc);
				}
			}
			return ret;
		}
		return null;
	}
	public static ArrayList filterFriends(RealmComponent attacker,ArrayList list) {
		if (list!=null) {
			ArrayList ret = new ArrayList();
			for (Iterator i=list.iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				if (attacker.equals(rc.getOwner())) {
					ret.add(rc);
				}
			}
			return ret;
		}
		return null;
	}
	public static ArrayList filterFriendsAndDenizens(RealmComponent attacker,ArrayList list) {
		if (list!=null) {
			ArrayList ret = new ArrayList();
			for (Iterator i=list.iterator();i.hasNext();) {
				RealmComponent rc = (RealmComponent)i.next();
				RealmComponent owner = rc.getOwner();
				if (owner==null || attacker.equals(rc.getOwner())) {
					ret.add(rc);
				}
			}
			return ret;
		}
		return null;
	}
	public static CombatSheet createCombatSheet(CombatFrame frame,BattleModel currentBattleModel,RealmComponent rc,boolean interactiveFrame) {
		if (rc.isCharacter()) {
			return new CharacterCombatSheet(frame,currentBattleModel,rc,interactiveFrame);
		}
		else {
			return new DenizenCombatSheet(frame,currentBattleModel,rc,interactiveFrame);
		}
	}
	private KeyListener shiftKeyListener = new KeyListener() {
		public void keyTyped(KeyEvent e) {
		}

		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode()==KeyEvent.VK_SHIFT) {
				mouseHoverShift = true;
				repaint();
			}
		}

		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode()==KeyEvent.VK_SHIFT) {
				mouseHoverShift = false;
				repaint();
			}
		}
	};
}