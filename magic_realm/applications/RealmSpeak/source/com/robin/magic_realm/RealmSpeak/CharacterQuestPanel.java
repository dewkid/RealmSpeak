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
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.*;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.quest.*;
import com.robin.magic_realm.components.quest.requirement.QuestRequirementParams;
import com.robin.magic_realm.components.swing.*;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.RealmLogging;

public class CharacterQuestPanel extends CharacterFramePanel {

	private JButton activateQuestButton;
	private JButton discardQuestButton;
	private JButton drawQuestsButton;
	private JButton viewAllPlayCardsButton;
	
	private ArrayList<Quest> characterQuests;
	
	/// New design
	private QuestView questView;
	private RealmObjectPanel questHandPanel;
	private RealmObjectPanel completedQuestsPanel;
	
	ActionListener activateQuestListener = new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				Quest quest = (Quest)ev.getSource();
				doActivateQuest(quest);
				FrameManager.getFrameManager().disposeFrame(FrameManager.DEFAULT_FRAME_KEY);
			}
		};

	protected CharacterQuestPanel(CharacterFrame parent) {
		super(parent);
		initComponents();
	}
	
	private void initComponents() {
		setLayout(new BorderLayout(10, 10));
		JLabel ins = new JLabel("Right-click quest for more info",JLabel.CENTER);
		ins.setOpaque(true);
		ins.setBackground(MagicRealmColor.PALEYELLOW);
		ins.setFont(new Font("Dialog",Font.BOLD,14));
		add(ins,"North");
		if (getHostPrefs().hasPref(Constants.QST_QUEST_CARDS)) {
			add(createQuestCardPanel());
		}
		else {
			add(createQuestViewPanel());
		}
		updateControls();
	}
	private JPanel createQuestViewPanel() {
		questView = new QuestView();
		return questView;
	}
	private void showQuestInfo(Component c) {
		if (c==null || !(c instanceof QuestCardComponent)) return;
		
		QuestCardComponent qc = (QuestCardComponent)c;
		Quest quest = new Quest(qc.getGameObject());
		QuestState state = quest.getState();
		
		QuestView view = new QuestView(state==QuestState.Assigned && (!quest.isAllPlay() || quest.isActivateable())?activateQuestListener:null);
		view.updatePanel(quest);
		ComponentTools.lockComponentSize(view,640,480);
		FrameManager.showDefaultManagedFrame(getMainFrame(), view, qc.getGameObject().getName(), qc.getFaceUpIcon(), true);
	}
	private JPanel createQuestCardPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		questHandPanel = new RealmObjectPanel(true,false);
		questHandPanel.setBorder(BorderFactory.createTitledBorder("Quest Hand"));
		questHandPanel.setSelectionMode(RealmObjectPanel.SINGLE_SELECTION);
		questHandPanel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent ev) {
				if (MouseUtility.isRightOrControlClick(ev)) {
					showQuestInfo(questHandPanel.getComponentAt(ev.getPoint()));
				}
			}
		});
		questHandPanel.addSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				updateControls();
			}
		});
		
		completedQuestsPanel = new RealmObjectPanel(true,false);
		completedQuestsPanel.setBorder(BorderFactory.createTitledBorder("Completed Quests"));
		completedQuestsPanel.setSelectionMode(RealmObjectPanel.SINGLE_SELECTION);
		completedQuestsPanel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent ev) {
				if (MouseUtility.isRightOrControlClick(ev)) {
					showQuestInfo(completedQuestsPanel.getComponentAt(ev.getPoint()));
				}
			}
		});
		completedQuestsPanel.addSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				updateControls();
			}
		});
		
		JSplitPaneImproved splitPane = new JSplitPaneImproved(JSplitPane.VERTICAL_SPLIT,new JScrollPane(questHandPanel),new JScrollPane(completedQuestsPanel));
		splitPane.setDividerLocation(0.5);
		
		panel.add(splitPane,BorderLayout.CENTER);
		
		Box controls = Box.createHorizontalBox();
		controls.add(Box.createHorizontalStrut(10));
		activateQuestButton = new JButton("Activate");
		activateQuestButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				Quest selQuest = getSelectedQuest();
				if (selQuest != null) {
					doActivateQuest(selQuest);
				}
			}
		});
		controls.add(activateQuestButton);
		controls.add(Box.createHorizontalStrut(10));
		discardQuestButton = new JButton("Discard");
		discardQuestButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				doDiscardQuestCards();
			}
		});
		controls.add(discardQuestButton);
		controls.add(Box.createHorizontalStrut(10));

		drawQuestsButton = new JButton("Draw");
		drawQuestsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				doDrawQuests();
			}
		});
		controls.add(drawQuestsButton);

		viewAllPlayCardsButton = new JButton("All-Play Cards");
		viewAllPlayCardsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				//QuestDeck deck = QuestDeck.findDeck(getCharacter().getGameData());
				
				boolean added = false;
				RealmObjectPanel panel = new RealmObjectPanel();
				for(Quest quest:characterQuests) {
					if (quest.isAllPlay() && !quest.getState().isFinished()) {
						panel.addObject(quest.getGameObject());
						added = true;
					}
				}
				panel.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent ev) {
						RealmObjectPanel panel = (RealmObjectPanel)ev.getSource();
						showQuestInfo(panel.getComponentAt(ev.getPoint()));
					}
				});

				ComponentTools.lockComponentSize(panel,640,480);
				
				if (added) {
					FrameManager.showDefaultManagedFrame(getMainFrame(), new JScrollPane(panel), "Available All-Play Cards", ImageCache.getIcon("quests/token"), true);
				}
				else {
					JOptionPane.showMessageDialog(getMainFrame(), "No All-Play cards left.", "None Left", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		controls.add(Box.createHorizontalGlue());
		controls.add(viewAllPlayCardsButton);
		
		panel.add(controls,BorderLayout.SOUTH);
		
		return panel;
	}
	
	private void doActivateQuest(Quest quest) {
		String dayKey = getCharacter().getCurrentDayKey();
		RealmLogging.logMessage(getCharacter().getName(),"Activated Quest Card: "+quest.getName());
		quest.setState(QuestState.Active, getCharacter().getCurrentDayKey(), getCharacter());
		if (quest.testRequirements(getMainFrame(), getCharacter(), new QuestRequirementParams())) {
			getCharacter().testQuestRequirements(getMainFrame()); // Make sure that all quests get updated (auto-journal)
			getCharacterFrame().updateCharacter();
			getMainFrame().getGameHandler().getInspector().redrawMap();
		}
		if (quest.getState()!=QuestState.Assigned && quest.isAllPlay()) {
			quest.revertAllPlay(dayKey,getCharacter());
			quest.clearAllPlay();
			getCharacterFrame().updateCharacter();
		}
		questHandPanel.repaint();
	}

	private void doDiscardQuestCards() {
		RealmObjectChooser chooser = new RealmObjectChooser("Discard Which Cards?", getGame().getGameData(), false);
		ArrayList<GameObject> list = new ArrayList<GameObject>();
		for (Quest quest : getCharacter().getAllQuests()) {
			if (quest.getState() == QuestState.Assigned && !quest.isAllPlay()) {
				list.add(quest.getGameObject());
			}
		}
		if (list.isEmpty()) {
			JOptionPane.showMessageDialog(getMainFrame(), "There are no quests to discard.", "Discard", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		chooser.addObjectsToChoose(list);
		chooser.setVisible(true);
		if (chooser.pressedOkay()) {
			questHandPanel.clearSelected();
			ArrayList<GameObject> chosen = chooser.getChosenObjects();
			if (chosen.isEmpty()) {
				JOptionPane.showMessageDialog(getMainFrame(), "No quests were chosen for discard.", "Discard", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			QuestDeck deck = QuestDeck.findDeck(getGame().getGameData());
			for (GameObject go : chosen) {
				Quest quest = new Quest(go);
				getCharacter().removeQuest(quest);
				deck.discardCard(quest);
				RealmLogging.logMessage(getCharacter().getName(),"Discarded Quest Card: "+quest.getName());
			}
			getCharacter().setDiscardedQuests(true);
			updatePanel();
			updateControls();
		}
	}

	private void doDrawQuests() {
		QuestDeck deck = QuestDeck.findDeck(getGame().getGameData());
		int cardsDrawn = deck.drawCards(getMainFrame(),getCharacter());
		StringBuilder sb = new StringBuilder();
		sb.append("Drew ");
		sb.append(cardsDrawn);
		sb.append(" new quest card");
		sb.append(cardsDrawn==1?"":"s");
		sb.append(".");
		getCharacter().setDiscardedQuests(true); // to make sure character doesn't discard after drawing cards!
		RealmLogging.logMessage(getCharacter().getName(),sb.toString());
		updatePanel();
		updateControls();
	}

	private Quest getSelectedQuest() {
		RealmComponent rc = questHandPanel.getSelectedComponent();
		if (rc!=null && rc instanceof QuestCardComponent) {
			return new Quest(rc.getGameObject());
		}
		return null;
	}

	private void updateControls() {
		if (getHostPrefs().hasPref(Constants.QST_QUEST_CARDS)) {
			Quest selQuest = getSelectedQuest();
			boolean gameStarted = getGame().getGameStarted();
			activateQuestButton.setEnabled(gameStarted && selQuest != null && selQuest.getState() == QuestState.Assigned && !selQuest.isAllPlay());

			boolean canDiscardQuests = !getCharacter().alreadyDiscardedQuests() && gameStarted;
			boolean characterIsAtDwelling = getCharacter().getCurrentLocation().isAtDwelling(true);
			boolean isBirdsong = getGameHandler().getGame().isRecording();
			discardQuestButton.setEnabled(canDiscardQuests && characterIsAtDwelling && isBirdsong && selQuest!=null && selQuest.isDiscardable());

			boolean hasAvailableSlots = (getCharacter().getQuestSlotCount() - getCharacter().getUnfinishedQuestCount()) > 0;
			drawQuestsButton.setEnabled(characterIsAtDwelling && isBirdsong && hasAvailableSlots && getCharacter().isCharacter());
		}
	}

	public void updatePanel() {
		characterQuests = getCharacter().getAllQuests();
		
		if (getHostPrefs().hasPref(Constants.QST_QUEST_CARDS)) {
			int slots = getCharacter().getQuestSlotCount();
			questHandPanel.removeAll();
			completedQuestsPanel.removeAll();
			for(Quest quest:characterQuests) {
				if (quest.getState().isFinished()) {
					if  (quest.getInt(QuestConstants.VP_REWARD)>0) {
						completedQuestsPanel.addObject(quest.getGameObject());
					}
				}
				else if (!quest.isAllPlay()) {
					questHandPanel.addObject(quest.getGameObject());
					slots--;
				}
			}
			for(int i=0;i<slots;i++) {
				questHandPanel.add(new EmptyCardComponent());
			}
		}
		else {
			if (characterQuests.size() == 1) {
				questView.updatePanel(characterQuests.get(0));
			}
		}
	}

	public static void main(String[] args) {
		JPanel panel = new JPanel(new BorderLayout(10, 10));

		JScrollPane sp = new JScrollPane(new JTable());
		sp.setPreferredSize(new Dimension(80, 500));
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		panel.add(sp, BorderLayout.WEST);

		JPanel green = new JPanel();
		green.setBackground(Color.green);
		panel.add(green, BorderLayout.CENTER);

		JOptionPane.showMessageDialog(new JFrame(), panel);
		System.exit(0);
	}
}