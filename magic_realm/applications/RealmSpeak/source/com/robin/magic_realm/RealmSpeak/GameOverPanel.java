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
package com.robin.magic_realm.RealmSpeak;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.ImageCache;
import com.robin.magic_realm.components.MagicRealmColor;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.RealmObjectMaster;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class GameOverPanel extends JPanel {
	
	public static final String TAB_NAME = "GameOver";
	private static Font tableFont = new Font("Dialog",Font.PLAIN,18);
	private static Font titleFont = new Font("Dialog",Font.BOLD,36);
	
	private ArrayList results;
	
	private GameObject owningChar;
	private HostPrefWrapper hostPrefs;
	
	private JTable resultTable;
	
	public GameOverPanel(GameObject aChar,HostPrefWrapper hostPrefs) {
		this.owningChar = aChar;
		this.hostPrefs = hostPrefs;
		buildResults();
		initComponents();
	}
	private void buildResults() {
		results = new ArrayList();
		Collection c = RealmObjectMaster.getRealmObjectMaster(owningChar.getGameData()).getPlayerCharacterObjects();
		for (Iterator i=c.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			CharacterWrapper cw = new CharacterWrapper(go);
			if (cw.isCharacter()) {
				if (go.hasAttributeBlock(CharacterWrapper.PLAYER_BLOCK)) { // was in the game at some point
					CharacterResult result = new CharacterResult(go);
					if (go.equals(owningChar)) {
						//result.setHighlight(true);
					}
					results.add(result);
				}
			}
		}
		
		Collections.sort(results,new Comparator() {
			public int compare(Object o1,Object o2) {
				CharacterResult r1 = (CharacterResult)o1;
				CharacterResult r2 = (CharacterResult)o2;
				int ret=0;
				if (usingQuests()) {
					ret = r2.getCharacter().getCompletedQuestCount() - r1.getCharacter().getCompletedQuestCount();
				}
				if (ret==0) ret = r2.getCharacter().getTotalScore() - r1.getCharacter().getTotalScore(); 
				return ret;
			}
		});
	}
	private boolean usingQuests() {
		return hostPrefs.hasPref(Constants.QST_BOOK_OF_QUESTS);
	}
	private void initComponents() {
		setLayout(new BorderLayout());
		
		resultTable = new JTable(new CharacterResultTableModel());
		resultTable.setRowHeight(40);
		resultTable.setDefaultRenderer(ImageIcon.class,new CharacterResultTableCellRenderer());
		resultTable.setDefaultRenderer(Integer.class,new ScoreCellRenderer());
		if (!usingQuests()) {
			resultTable.getColumnModel().getColumn(4).setMaxWidth(0);
		}
		resultTable.getColumnModel().getColumn(0).setMaxWidth(60);
		resultTable.getColumnModel().getColumn(1).setMaxWidth(120);
		resultTable.getColumnModel().getColumn(5).setMaxWidth(60);
		resultTable.getColumnModel().getColumn(6).setMaxWidth(60);
		
		resultTable.setFont(tableFont);
		
		add(new JScrollPane(resultTable),"Center");
		
		JLabel label = new JLabel("Game Over - Final Scores:");
		label.setIcon(ImageCache.getIcon("tab/gameover"));
		label.setOpaque(true);
		label.setBackground(MagicRealmColor.GOLD);
		label.setFont(titleFont);
		add(label,"North");
	}
	private class CharacterResult {
		
		private CharacterWrapper character;
		//private boolean highlight = false;
		
		public CharacterResult(GameObject aChar) {
			character = new CharacterWrapper(aChar);
		}
//		public void setHighlight(boolean val) {
//			highlight = val;
//		}
//		public boolean isHighlight() {
//			return highlight;
//		}
		public CharacterWrapper getCharacter() {
			return character;
		}
	}
	private class CharacterResultTableModel extends AbstractTableModel {
		protected String[] columnName = {
			" ",
			"Rank",
			"Player",
			"Character",
			"Quests",
			"VPs",
			"Score"
		};
		protected Class[] columnClass = {
			ImageIcon.class,
			String.class,
			String.class,
			String.class,
			String.class,
			String.class,
			Integer.class
		};
		public CharacterResultTableModel() {
		}
		public int getRowCount() {
			if (results!=null) {
				return results.size();
			}
			return 0;
		}
		public int getColumnCount() {
			return columnName.length;
		}
		public String getColumnName(int column) {
			return columnName[column];
		}
		public Class getColumnClass(int column) {
			return columnClass[column];
		}
		private int getTopScore() {
			CharacterResult result = (CharacterResult)results.get(0);
			CharacterWrapper character = result.getCharacter();
			return character.getTotalScore();
		}
		public Object getValueAt(int row, int column) {
			if (row<results.size()) {
				CharacterResult result = (CharacterResult)results.get(row);
				CharacterWrapper character = result.getCharacter();
				String rank = "";
				if (row==0 || character.getTotalScore()==getTopScore()) {
					rank = "Winner!";
				}
				switch(column) {
					case 0:
						return character.getMidSizedIcon();
					case 1:
						return rank;
					case 2:
						return character.getPlayerName();
					case 3:
						return character.getCharacterName();
					case 4:
						return character.getCompletedQuestCount()+" / "+character.getQuestCount();
					case 5:
						return character.isDead()?"DEAD":String.valueOf(character.getTotalAssignedVPs());
					case 6:
						return new Integer(character.getTotalScore());
				}
			}
			return null;
		}
	}
	private class CharacterResultTableCellRenderer extends DefaultTableCellRenderer {
		public CharacterResultTableCellRenderer() {
		}
		public Component getTableCellRendererComponent(JTable table,Object val,boolean isSelected,boolean hasFocus,int row,int col) {
			super.getTableCellRendererComponent(table,val,isSelected,hasFocus,row,col);
			setHorizontalAlignment(SwingConstants.CENTER);
			if (val instanceof ImageIcon) {
				setText("");
				setIcon((Icon)val);
			}
			else {
				setIcon(null);
			}
			return this;
		}
	}
	private static final Color HIGHLIGHT_NEG = new Color(255,200,200);
	private static final Color HIGHLIGHT_POS = new Color(200,255,200);
	private class ScoreCellRenderer extends DefaultTableCellRenderer {
		public ScoreCellRenderer() {
		}
		public Component getTableCellRendererComponent(JTable table,Object val,boolean isSelected,boolean hasFocus,int row,int col) {
			super.getTableCellRendererComponent(table,val,isSelected,hasFocus,row,col);
			setHorizontalTextPosition(SwingConstants.CENTER);
			Integer num = (Integer)val;
			if (num.intValue()<0) {
				setBackground(HIGHLIGHT_NEG);
			}
			else {
				setBackground(HIGHLIGHT_POS);
			}
			return this;
		}
	}
}