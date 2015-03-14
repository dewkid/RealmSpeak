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
package com.robin.magic_realm.components.swing;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.border.Border;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.general.graphics.GraphicsUtil;
import com.robin.general.swing.ComponentTools;
import com.robin.magic_realm.components.CharacterActionChitComponent;
import com.robin.magic_realm.components.ChitComponent;
import com.robin.magic_realm.components.utility.RealmLoader;
import com.robin.magic_realm.components.utility.RealmUtility;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public abstract class ChitBinPanel extends JComponent {
	private static final Color whiteout = new Color(255,255,255,200);
	
	private static final int INNER_CELL_SPACE = 3;
	private static final int INNER_GROUP_SPACE = 3;
	private static final int COLOR_MAGIC_SPACE = 10;
	private static final int PANEL_BORDER = 10;
	private static final int LABEL_WIDTH = 40;
	private ChitBinLayout layout;
	private Border border;
	private Border groupBorder;
	private int cellSize;
	private int maxCols = 3;
	private int maxRows = 7;

	public abstract void handleClick(Point p);
	public abstract boolean canClickChit(ChitComponent aChit);

	public ChitBinPanel(ChitBinLayout layout) {
		this(layout,3,7);
	}
	public ChitBinPanel(ChitBinLayout layout,int maxCols,int maxRows) {
		this.maxCols = maxCols;
		this.maxRows = maxRows;
		this.layout = layout;
		border = BorderFactory.createLoweredBevelBorder();
		groupBorder = BorderFactory.createEtchedBorder();
		updateSize();
		reset();
	}
	public void updateSize() {
		cellSize = ChitComponent.S_CHIT_SIZE + (INNER_CELL_SPACE << 1);
		int w = cellSize * maxCols + (PANEL_BORDER << 1) + LABEL_WIDTH;
		int h = cellSize * maxRows + (PANEL_BORDER << 1);
		ComponentTools.lockComponentSize(this, w, h);
	}

	public void reset() {
		layout.reset();
		revalidate();
		repaint();
	}

	public ChitComponent getClickedChit(Point p) {
		return layout.getChitAt(p);
	}

	public void addChits(ArrayList list) {
		for (int i=0;i<list.size();i++) {
			ChitComponent chit = (ChitComponent)list.get(i);
			addChit(chit, i);
		}
	}
	public void addChit(ChitComponent newChit, int position) {
		layout.setChit(position, newChit);
		repaint();
	}

	public int getPosition(ChitComponent aChit) {
		return layout.getChitIndex(aChit);
	}

	public void removeChitAt(int n) {
		layout.setChit(n,null);
		repaint();
	}

	public void paint(Graphics g1) {
		Graphics2D g = (Graphics2D)g1;
		ArrayList groups = layout.getGroups();
		int left = PANEL_BORDER + LABEL_WIDTH;
		int top = PANEL_BORDER;
		int r=0;
		int c=0;
		for (Iterator i=groups.iterator();i.hasNext();) {
			String group = (String)i.next();
			boolean isMagic = "MAGIC".equals(group);
			g.setColor(Color.black);
			GraphicsUtil.drawCenteredString(g,PANEL_BORDER,r*cellSize,LABEL_WIDTH,cellSize,group);
			ArrayList bins = layout.getBins(group);
			int rtop = (r*cellSize)+top;
			for (Iterator b=bins.iterator();b.hasNext();) {
				ChitBin bin = (ChitBin)b.next();
				Rectangle a = new Rectangle(c * cellSize, r * cellSize, cellSize, cellSize);
				bin.setRectangle(a);
				a.x += left;
				a.y += top;
				int x = a.x + INNER_CELL_SPACE;
				int y = a.y + INNER_CELL_SPACE;
				int s = ChitComponent.S_CHIT_SIZE;
				if (bin.getChit()==null) {
					border.paintBorder(this, g, x, y, s, s);
				}
				else {
					bin.getChit().paint(g.create(x,y,s,s));
					if (!canClickChit(bin.getChit())) {
						g.setColor(whiteout);
						g.fillRect(x,y,s,s);
					}
				}
				
				Color col = bin.getColor();
				if (col!=null) {
					int mx = x+5;//+(ChitComponent.S_CHIT_SIZE>>1)-(COLOR_MAGIC_SPACE>>1);
					int my = y+ChitComponent.S_CHIT_SIZE;
					g.setColor(Color.black);
					g.fillRoundRect(mx,my,ChitComponent.S_CHIT_SIZE-10,COLOR_MAGIC_SPACE,3,3);
					g.setColor(col);
					g.fillRoundRect(mx+1,my+1,ChitComponent.S_CHIT_SIZE-12,COLOR_MAGIC_SPACE-2,3,3);
				}
				
				c++;
				if (c==maxCols) {
					r++;
					c=0;
					if (isMagic) {
						top += COLOR_MAGIC_SPACE;
					}
				}
			}
			if (c>0) {
				r++;
				c=0;
				if (isMagic) {
					top += (COLOR_MAGIC_SPACE<<1);
				}
			}
			Rectangle section = new Rectangle(PANEL_BORDER,rtop,(cellSize*maxCols)+LABEL_WIDTH,(r*cellSize)+top-rtop);
			groupBorder.paintBorder(this,g,section.x,section.y,section.width,section.height);
			top += INNER_GROUP_SPACE;
		}
	}

	public void makeAllChitsFatigued() {
		for (Iterator i=layout.getAllChits().iterator();i.hasNext();) {
			ChitComponent chit = (ChitComponent)i.next();
			if (chit.isActionChit()) {
				CharacterActionChitComponent achit = (CharacterActionChitComponent)chit;
				achit.makeFatigued();
			}
		}
	}

	public void makeAllChitsActive() {
		for (Iterator i=layout.getAllChits().iterator();i.hasNext();) {
			ChitComponent chit = (ChitComponent)i.next();
			if (chit.isActionChit()) {
				CharacterActionChitComponent achit = (CharacterActionChitComponent)chit;
				if (!achit.isAlerted() && !achit.isColor()) { // ignore alerted and color chits
					achit.makeActive();
				}
			}
		}
	}

	public void makeAllChitsWounded() {
		for (Iterator i=layout.getAllChits().iterator();i.hasNext();) {
			ChitComponent chit = (ChitComponent)i.next();
			if (chit.isActionChit()) {
				CharacterActionChitComponent achit = (CharacterActionChitComponent)chit;
				achit.makeWounded();
			}
		}
	}
	
	public ArrayList<ChitComponent> getAllChits() {
		return layout.getAllChits();
	}
	public static void main(String[] args) {
		RealmUtility.setupTextType();
		ComponentTools.setSystemLookAndFeel();
		
		System.out.print("loading...");
		RealmLoader loader = new RealmLoader();
		System.out.println("Done");
		HostPrefWrapper.createDefaultHostPrefs(loader.getData());
		
		_testShow(loader.getData(),"White Knight");
		_testShow(loader.getData(),"Magician");
		_testShow(loader.getData(),"Amazon");
		_testShow(loader.getData(),"Witch King");
		_testShow(loader.getData(),"Witch");
		_testShow(loader.getData(),"Magician");
		_testShow(loader.getData(),"Black Knight");
		_testShow(loader.getData(),"Sorceror");
		_testShow(loader.getData(),"Woods Girl");
		_testShow(loader.getData(),"Dwarf");
		_testShow(loader.getData(),"Elf");
		_testShow(loader.getData(),"Berserker");
		_testShow(loader.getData(),"Druid");
		_testShow(loader.getData(),"Captain");
		_testShow(loader.getData(),"Swordsman");
		_testShow(loader.getData(),"Wizard");
	}
	private static void _testShow(GameData data,String name) {
		GameObject go = data.getGameObjectByName(name);
		CharacterWrapper character = new CharacterWrapper(go);
		GameObject f1 = data.getGameObjectByName("Test Fly Chit 1");
		go.add(f1);
		ArrayList chits = new ArrayList(character.getCompleteChitList());
		ChitBinLayout layout = new ChitBinLayout(chits);
		ChitBinPanel panel = new ChitBinPanel(layout) {
			public boolean canClickChit(ChitComponent aChit) {
				return true;
			}
			public void handleClick(Point p) {
			}
		};
		for (int i=0;i<chits.size();i++) {
			ChitComponent chit = (ChitComponent)chits.get(i);
			panel.addChit(chit, i);
		}
		JOptionPane.showMessageDialog(null, panel,name,JOptionPane.PLAIN_MESSAGE);
	}
}