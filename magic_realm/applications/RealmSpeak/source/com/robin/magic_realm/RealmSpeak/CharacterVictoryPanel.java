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
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.*;

import com.robin.game.objects.GameObject;
import com.robin.general.graphics.GraphicsUtil;
import com.robin.general.swing.ComponentTools;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.attribute.DevelopmentProgress;
import com.robin.magic_realm.components.attribute.Score;
import com.robin.magic_realm.components.quest.Quest;
import com.robin.magic_realm.components.quest.QuestConstants;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class CharacterVictoryPanel extends CharacterFramePanel {
	
	private static final Font TITLE_FONT = new Font("Dialog",Font.BOLD,12);
	private static final Font VALUE_FONT = new Font("Dialog",Font.BOLD,36);
	
	protected JTable victoryTable;
	protected VictoryTableModel victoryTableModel;
	protected Box viewPanel;
	
	protected int focusRow = -1;
	protected int focusColumn = -1;
	protected ArrayList<Integer> calcColumns;
	protected boolean doUpdate = false;
	
	protected JLabel earnedVpsLabel;
	protected JLabel nextStageLabel;
	protected JLabel nextLevelLabel;
	
	public CharacterVictoryPanel(CharacterFrame parent) {
		super(parent);
		calcColumns = new ArrayList<Integer>();
		init();
	}
	private void init() {
		setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel(new BorderLayout());
		
		initializeTableRows(getHostPrefs());
		
		victoryTableModel = new VictoryTableModel(getHostPrefs());
		victoryTable = new JTable(victoryTableModel);
		for (int i=1;i<victoryTable.getColumnCount();i++) {
			victoryTable.getColumnModel().getColumn(i).setMaxWidth(50);
		}
		victoryTable.setDefaultRenderer(String.class,new ScoreCellRenderer(getHostPrefs().hasPref(Constants.QST_QUEST_CARDS)));
		victoryTable.setCellSelectionEnabled(true);
		victoryTable.getTableHeader().setDefaultRenderer(new ScoreHeaderRenderer());
		JScrollPane sp1 = new JScrollPane(victoryTable);
		ComponentTools.lockComponentSize(sp1,100,130);
		topPanel.add(sp1,"Center");
		add(topPanel,"North");
		
		viewPanel = Box.createHorizontalBox();
		JScrollPane sp2 = new JScrollPane(viewPanel);
		ComponentTools.lockComponentSize(sp2,100,100);
		add(sp2,"Center");
		
		earnedVpsLabel = null;
		if (getGameHandler().getHostPrefs().hasPref(Constants.EXP_DEVELOPMENT)) {
			JPanel vpProgressPanel = new JPanel(new GridLayout(1,6));
			vpProgressPanel.add(getTitleLabel("<html><center>Current VPs:</center></html>",MagicRealmColor.PALEYELLOW));
			earnedVpsLabel = getTitleLabel("",MagicRealmColor.PALEYELLOW);
			vpProgressPanel.add(earnedVpsLabel);
			vpProgressPanel.add(getTitleLabel("<html><center>VPs to next stage:</center></html>",MagicRealmColor.LIGHTBLUE));
			nextStageLabel = getTitleLabel("",MagicRealmColor.LIGHTBLUE);
			vpProgressPanel.add(nextStageLabel);
			vpProgressPanel.add(getTitleLabel("<html><center>VPs to next level:</center></html>",MagicRealmColor.LIGHTGREEN));
			nextLevelLabel = getTitleLabel("",MagicRealmColor.LIGHTGREEN);
			vpProgressPanel.add(nextLevelLabel);
			topPanel.add(vpProgressPanel,"South");
		}
	}
	protected JLabel getTitleLabel(String title,Color background) {
		JLabel label = new JLabel(title,JLabel.CENTER);
		label.setFont(TITLE_FONT);
		label.setOpaque(true);
		label.setBackground(background);
		return label;
	}
	protected void setFocus(int row,int col) {
		if (focusRow!=row || focusColumn!=col) {
			focusRow = row;
			focusColumn = col;
			updateView();
		}
	}
	private boolean isRestrictAssigned() {
		return !getHostPrefs().getRequiredVPsOff() && !getHostPrefs().hasPref(Constants.QST_QUEST_CARDS);
	}
	
	private void updateProgress() {
		if (earnedVpsLabel!=null) {
			DevelopmentProgress dp = DevelopmentProgress.createDevelopmentProgress(getGameHandler().getHostPrefs(),getCharacter());
			earnedVpsLabel.setText(String.valueOf(dp.getCurrentVps()));
			dp.updateStage();
			
			nextStageLabel.setText(String.valueOf(dp.getVpsToNextStage()));
			nextLevelLabel.setText(String.valueOf(dp.getVpsToNextLevel()));
		}
	}
	private void updateView() {
		viewPanel.removeAll();
		calcColumns.clear();
		revalidate();
		repaint();
		VictoryTableRow row = victoryTableModel.getTableRow(focusRow);
		if (row==null) return;
		if (!row.usesColumn(focusColumn)) return;
		Score score = row.getScore();
		if (score!=null) {
			viewPanel.add(Box.createHorizontalGlue());
			switch(focusColumn) {
				case COL_NEED:
					// Show POINTS times MULTIPLIER
					calcColumns.add(COL_POINTS);
					calcColumns.add(COL_MULTIPLIER);
					viewPanel.add(getDescriptorBlock("POINTS",score.getAssignedVictoryPoints()));
					viewPanel.add(getDescriptorBlock(" ","x "+score.getMultiplier()));
					break;
				case COL_RECORDED:
					switch(row.rowType) {
 						case Spells:
 							viewPanel.add(getDescriptorBlock("LEARNED",score.getRecordedPoints()));
 							break;
 						case Fame:
 							boolean disgust = getCharacter().hasCurse(Constants.DISGUST);
 							viewPanel.add(getDescriptorBlock(disgust?"DISGUST":"FAME",score.getRecordedPoints()));
 							break;
 						case Notoriety:
 							viewPanel.add(getDescriptorBlock("NOTORIETY",score.getRecordedPoints()));
 							break;
 						case Gold:
 							boolean ashes = getCharacter().hasCurse(Constants.ASHES);
 							viewPanel.add(getDescriptorBlock(ashes?"ASHES":"GOLD",score.getRecordedPoints()));
 							break;
					}
					break;
				case COL_OWNED:
					if (score.getScoringGameObjects()!=null) {
						// Show inventory and how it adds up
						boolean first = true;
						for (GameObject go:score.getScoringGameObjects()) {
							if (!first) {
								viewPanel.add(getDescriptorBlock("","+"));
							}
							RealmComponent rc = RealmComponent.getRealmComponent(go);
							if (rc==null) {
								viewPanel.add(getDescriptorBlockLongText(go.getName(),getValueFromInventory(row.getRowType(),go)));
							}
							else {
								viewPanel.add(getDescriptorBlock(rc.getIcon(),getValueFromInventory(row.getRowType(),go)));
							}
							first = false;
						}
					}
					else if (row.rowType==VictoryRowType.Gold) {
						viewPanel.add(getDescriptorBlock("<html>STARTING GOLD</html>",score.getOwnedPoints()));
					}
					break;
				case COL_TOTAL:
					// Show RECORDED + OWNED
					calcColumns.add(COL_RECORDED);
					calcColumns.add(COL_OWNED);
					viewPanel.add(getDescriptorBlock("RECORDED",score.getRecordedPoints()));
					viewPanel.add(getDescriptorBlock("","+"));
					viewPanel.add(getDescriptorBlock("OWNED",score.getOwnedPoints()));
					break;
				case COL_EARNED:
					if (!isRestrictAssigned() || score.getAssignedVictoryPoints()>0) {
						boolean excludeStartingWorth = row.rowType==VictoryRowType.Gold && victoryTableModel.getHostPrefs().hasPref(Constants.EXP_DEV_EXCLUDE_SW);
						calcColumns.add(COL_MULTIPLIER);
						calcColumns.add(COL_TOTAL);
						viewPanel.add(getDescriptorBlock("TOTAL",(excludeStartingWorth?"(":"")+score.getPoints()));
						if (excludeStartingWorth) {
							calcColumns.add(COL_OWNED);
							viewPanel.add(getDescriptorBlock("","+"));
							viewPanel.add(getDescriptorBlock("OWNED",-score.getOwnedPoints()+")"));
						}
						viewPanel.add(getDescriptorBlock("","/"));
						viewPanel.add(getDescriptorBlock("MULTIPLIER",score.getMultiplier()));
					}
					else {
						calcColumns.add(COL_POINTS);
					}
					break;
				case COL_SCORE:
					// show TOTAL - NEED, with a x3 penalty if the result is negative
					calcColumns.add(COL_TOTAL);
					calcColumns.add(COL_NEED);
					viewPanel.add(getDescriptorBlock("TOTAL",(score.hasPenalty()?"( ":"")+score.getPoints()));
					viewPanel.add(getDescriptorBlock("","-"));
					viewPanel.add(getDescriptorBlock("NEED",score.getRequired()+(score.hasPenalty()?" )":"")));
					if (score.hasPenalty()) {
						viewPanel.add(getDescriptorBlock("PENALTY","x 3"));
					}
					break;
				case COL_BASIC:
					// show SCORE / MULTIPLIER, rounded down
					calcColumns.add(COL_SCORE);
					calcColumns.add(COL_MULTIPLIER);
					viewPanel.add(getDescriptorBlock("SCORE",score.getScore()));
					viewPanel.add(getDescriptorBlock("","/"));
					viewPanel.add(getDescriptorBlock("MULTIPLIER",score.getMultiplier()));
					break;
				case COL_BONUS:
					// show POINTS * BASIC
					calcColumns.add(COL_POINTS);
					calcColumns.add(COL_BASIC);
					viewPanel.add(getDescriptorBlock("POINTS",score.getAssignedVictoryPoints()));
					viewPanel.add(getDescriptorBlock("","x"));
					viewPanel.add(getDescriptorBlock("BASIC",score.getBasicScore()));
					break;
				case COL_FINAL:
					// show BASIC + BONUS
					calcColumns.add(COL_BASIC);
					calcColumns.add(COL_BONUS);
					viewPanel.add(getDescriptorBlock("BASIC",score.getBasicScore()));
					viewPanel.add(getDescriptorBlock("","+"));
					viewPanel.add(getDescriptorBlock("BONUS",score.getBonusScore()));
					break;
			}
			viewPanel.add(Box.createHorizontalGlue());
		}
	}
	private int getValueFromInventory(VictoryRowType rowType,GameObject go) {
		switch(rowType) {
			case GreatTreasures:	return 1;
			case Fame:				return go.getThisInt("fame");
			case Notoriety:			return go.getThisInt("notoriety");
			case QuestPoints:		return go.getInt(Quest.QUEST_BLOCK,QuestConstants.VP_REWARD);
		}
		return 0;
	}
	private JPanel getDescriptorBlock(String text,int val) {
		return getDescriptorBlock(text,String.valueOf(val));
	}
	private JPanel getDescriptorBlock(String text,String val) {
		JPanel panel = getDescriptorBlock(text.length()>0);
		panel.add(getDescriptorLabel(text,TITLE_FONT));
		panel.add(getDescriptorLabel(val,VALUE_FONT));
		return panel;
	}
	private JPanel getDescriptorBlockLongText(String text,int val) {
		JPanel panel = getDescriptorBlock(text.length()>0);
		panel.add(getDescriptorArea(text,TITLE_FONT));
		panel.add(getDescriptorLabel(String.valueOf(val),VALUE_FONT));
		return panel;
	}
	private JPanel getDescriptorBlock(ImageIcon icon,int val) {
		return getDescriptorBlock(icon,String.valueOf(val));
	}
	private JPanel getDescriptorBlock(ImageIcon icon,String val) {
		JPanel panel = getDescriptorBlock(true);
		panel.add(new JLabel(icon));
		panel.add(getDescriptorLabel(val,VALUE_FONT));
		return panel;
	}
	private JPanel getDescriptorBlock(boolean wide) {
		JPanel panel = new JPanel(new GridLayout(2,1));
		ComponentTools.lockComponentSize(panel,wide?CardComponent.CARD_WIDTH:40,CardComponent.CARD_HEIGHT<<1);
		return panel;
	}
	private JLabel getDescriptorLabel(String text,Font font) {
		JLabel label = new JLabel(text,JLabel.CENTER);
		label.setFont(font);
		return label;
	}
	private JTextPane getDescriptorArea(String text,Font font) {
		JTextPane area = new JTextPane();
		area.setText(text);
		area.setEditable(false);
		area.setOpaque(false);
		ComponentTools.lockComponentSize(area,CardComponent.CARD_WIDTH,CardComponent.CARD_HEIGHT);
		area.setFont(font);
		
		StyledDocument doc = area.getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
		doc.setParagraphAttributes(0, doc.getLength(), center, false);
		return area;
	}
	public void updatePanel() {
		updateProgress();
		updateView();
	}
	
	private enum VictoryRowType {
		GreatTreasures,
		Spells,
		Fame,
		Notoriety,
		Gold,
		QuestPoints,
		Totals,
		;
	};
	
	private static final int COL_CATEGORY = 0;
	private static final int COL_POINTS = 1;
	private static final int COL_MULTIPLIER = 2;
	private static final int COL_NEED = 3;
	private static final int COL_RECORDED = 4;
	private static final int COL_OWNED = 5;
	private static final int COL_TOTAL = 6;
	private static final int COL_EARNED = 7;
	private static final int COL_SCORE = 8;
	private static final int COL_BASIC = 9;
	private static final int COL_BONUS = 10;
	private static final int COL_FINAL = 11;
	
	private ArrayList<VictoryTableRow> tableRows;
	private void initializeTableRows(HostPrefWrapper hostPrefs) {
		boolean showQuestPoints = hostPrefs.hasPref(Constants.QST_QUEST_CARDS);
		tableRows = new ArrayList<VictoryTableRow>();
		if (showQuestPoints) {
			tableRows.add(new ScoreRow(hostPrefs,"Quest Pts",VictoryRowType.QuestPoints) {
				public Score getScore() {
					return getCharacter().getQuestPointScore();
				}
			});
		}
		tableRows.add(new ScoreRow(hostPrefs,"GTs",VictoryRowType.GreatTreasures) {
			public Score getScore() {
				return getCharacter().getGreatTreasureScore();
			}
		});
		tableRows.add(new ScoreRow(hostPrefs,"Spells",VictoryRowType.Spells) {
			public Score getScore() {
				return getCharacter().getUsableSpellScore();
			}
		});
		tableRows.add(new ScoreRow(hostPrefs,"Fame",VictoryRowType.Fame) {
			public Score getScore() {
				return getCharacter().getFameScore();
			}
		});
		tableRows.add(new ScoreRow(hostPrefs,"Notoriety",VictoryRowType.Notoriety) {
			public Score getScore() {
				return getCharacter().getNotorietyScore();
			}
		});
		tableRows.add(new ScoreRow(hostPrefs,"Gold",VictoryRowType.Gold) {
			public Score getScore() {
				return getCharacter().getGoldScore();
			}
		});
		tableRows.add(new ScoreTotalRow(hostPrefs,"TOTALS"));
	}	
	private class VictoryTableModel extends AbstractTableModel {
		protected String[] columnNameNormal = {
			"Category","Points"," ","Need","Recrd","Own","Total","Earned","Score","Basic","Bonus","Final"
		};
		protected String[] columnNameQuestPoints = {
			"Category","Points"," ","Need","Recrd","Own","Total","Earned"
		};
		private boolean showQuestPoints;
		private HostPrefWrapper hostPrefs;
		public VictoryTableModel(HostPrefWrapper hostPrefs) {
			this.hostPrefs = hostPrefs;
			this.showQuestPoints = hostPrefs.hasPref(Constants.QST_QUEST_CARDS);
		}
		public HostPrefWrapper getHostPrefs() {
			return hostPrefs;
		}
		public int getRowCount() {
			return tableRows==null?0:tableRows.size();
		}
		public int getColumnCount() {
			return showQuestPoints?columnNameQuestPoints.length:columnNameNormal.length;
		}
		public String getColumnName(int column) {
			return showQuestPoints?columnNameQuestPoints[column]:columnNameNormal[column];
		}
		public Class getColumnClass(int column) {
			return String.class;
		}
		public VictoryTableRow getTableRow(int row) {
			if (row>=0 && row<tableRows.size()) {
				return tableRows.get(row);
			}
			return null;
		}
		public Object getValueAt(int row, int column) {
			VictoryTableRow tableRow = getTableRow(row);
			if (!tableRow.usesColumn(column)) return "";
			return tableRow.getValue(column);
		}
	}
	private class ScoreHeaderRenderer extends DefaultTableCellRenderer {
		private Border border = UIManager.getBorder("TableHeader.cellBorder");
		public ScoreHeaderRenderer() {
			setFont(UIManager.getFont("TableHeader.font"));
		}
		public void paintComponent(Graphics g) {
			Dimension size = getSize();
			border.paintBorder(this,g,0,0,size.width,size.height);
			g.setColor(getForeground());
			GraphicsUtil.drawCenteredString(g,0,0,size.width,size.height,getText());
		}
	}
	private class ScoreCellRenderer extends DefaultTableCellRenderer {
		private final Font NORMAL_FONT = new Font("Dialog",Font.PLAIN,12);
		private final Font BOLD_FONT = new Font("Dialog",Font.BOLD,12);
		private final Color NORMAL_BACKGROUND = UIManager.getColor("Table.background");
		private final Color PANEL_BACKGROUND = UIManager.getColor("Panel.background");
		private final Border NO_BORDER = BorderFactory.createEmptyBorder();
		private final Border SELECTED_BORDER = BorderFactory.createLineBorder(Color.blue,2);
		private final Border CALC_BORDER = BorderFactory.createLineBorder(Color.green,2);

		public ScoreCellRenderer(boolean showQuestPoints) {
			setOpaque(true);
		}
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int column) {
			setText(value.toString());
			setFont(NORMAL_FONT);
			setHorizontalAlignment(CENTER);
			setBackground(NORMAL_BACKGROUND);
			if (hasFocus) {
				setFocus(row,column);
			}
			boolean lastFocus = row==focusRow && column==focusColumn;
			setBorder(lastFocus?SELECTED_BORDER:NO_BORDER);
			if (row==focusRow && calcColumns.contains(column)) {
				setBorder(CALC_BORDER);
			}
			VictoryTableRow tableRow = tableRows.get(row);
			if (tableRow instanceof ScoreRow) {
				switch(column) {
					case COL_CATEGORY:
						setHorizontalAlignment(LEFT);
						break;
					case COL_POINTS:
					case COL_NEED:
						setBackground(MagicRealmColor.GOLD);
						break;
					case COL_MULTIPLIER:
						setBackground(PANEL_BACKGROUND);
						break;
					case COL_TOTAL:
						setFont(BOLD_FONT);
						setBackground(MagicRealmColor.PALEYELLOW);
						break;
					case COL_RECORDED:
					case COL_OWNED:
						setBackground(MagicRealmColor.PALEYELLOW);
						break;
					case COL_FINAL:
						setFont(BOLD_FONT);
						setBackground(MagicRealmColor.LIGHTBLUE);
						break;
					case COL_SCORE:
					case COL_BASIC:
					case COL_BONUS:
						setBackground(MagicRealmColor.LIGHTBLUE);
						break;
					case COL_EARNED:
						setBackground(MagicRealmColor.LIGHTBLUE);
						break;
				}
				if (!tableRow.usesColumn(column)) {
					setBackground(PANEL_BACKGROUND);
				}
			}
			else {
				setFont(BOLD_FONT);
				if (value==null || value.toString().trim().length()==0) {
					setBackground(PANEL_BACKGROUND);
				}
				else {
					if (column==COL_CATEGORY) {
						setHorizontalAlignment(LEFT);
					}
				}
			}
			return this;
		}
	}
	private abstract class VictoryTableRow {
		String header;
		HostPrefWrapper hostPrefs;
		VictoryRowType rowType;
		
		public abstract boolean usesColumn(int col);
		public abstract Score getScore();
		public abstract Object getValue(int column);
		
		public VictoryTableRow(HostPrefWrapper hostPrefs,String header,VictoryRowType rowType) {
			this.hostPrefs = hostPrefs;
			this.header = header;
			this.rowType = rowType;
		}
		public VictoryRowType getRowType() {
			return rowType;
		}
	}
	private abstract class ScoreRow extends VictoryTableRow {
		public ScoreRow(HostPrefWrapper hostPrefs,String header,VictoryRowType rowType) {
			super(hostPrefs,header,rowType);
		}
		public boolean usesColumn(int col) {
			if (col==COL_EARNED) {
				if (rowType==VictoryRowType.QuestPoints) return false;
				return (!isRestrictAssigned() || getScore().getAssignedVictoryPoints()>0);
			}
			if (rowType!=VictoryRowType.QuestPoints
					&& hostPrefs.hasPref(Constants.QST_QUEST_CARDS)
					&& (col==COL_SCORE || col==COL_BASIC || col==COL_BONUS || col==COL_FINAL)) {
				return false;
			}
			
			switch(rowType) {
				case Fame:
				case Notoriety:
				case Gold:
					return true;
				case GreatTreasures:
					return col!=COL_RECORDED;
				case Spells:
					return col!=COL_OWNED;
			}
			return true;
		}
		public Object getValue(int column) {
			Score score = getScore();
			switch(column) {
				case COL_CATEGORY:		return header;
				case COL_POINTS:		return String.valueOf(score.getAssignedVictoryPoints());
				case COL_MULTIPLIER:	return "x "+String.valueOf(score.getMultiplier());
				case COL_NEED:			return String.valueOf(score.getRequired()); // Need
				case COL_RECORDED:		return String.valueOf(score.getRecordedPoints()); // recorded
				case COL_OWNED:			return String.valueOf(score.getOwnedPoints()); // own
				case COL_TOTAL:			return String.valueOf(score.getPoints()); // total
				case COL_EARNED:
					boolean excludeStartingWorth = rowType==VictoryRowType.Gold && hostPrefs.hasPref(Constants.EXP_DEV_EXCLUDE_SW);
					return String.valueOf(score.getEarnedVictoryPoints(isRestrictAssigned(),excludeStartingWorth));
				case COL_SCORE:			return String.valueOf(score.getScore());
				case COL_BASIC:			return String.valueOf(score.getBasicScore());
				case COL_BONUS:			return String.valueOf(score.getBonusScore());
				case COL_FINAL:			return String.valueOf(score.getTotalScore());
			}
			return null;
		}
	}
	private class ScoreTotalRow extends VictoryTableRow {
		public ScoreTotalRow(HostPrefWrapper hostPrefs,String header) {
			super(hostPrefs,header,VictoryRowType.Totals);
		}
		public boolean usesColumn(int col) {
			return true;
		}
		public Score getScore() {
			return null;
		}
		public Object getValue(int column) {
			switch(column) {
				case COL_CATEGORY:		return header;
				case COL_POINTS:		return String.valueOf(getCharacter().getTotalAssignedVPs());
				case COL_EARNED:
					boolean excludeStartingWorth = hostPrefs.hasPref(Constants.EXP_DEV_EXCLUDE_SW);
					return String.valueOf(getCharacter().getTotalEarnedVps(isRestrictAssigned(),excludeStartingWorth));
				case COL_FINAL:			return String.valueOf(getCharacter().getTotalScore());
				default:				return "";
			}
		}
	}
}