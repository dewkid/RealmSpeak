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
package com.robin.magic_realm.RealmQuestBuilder;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import com.robin.game.objects.GameData;
import com.robin.general.swing.*;
import com.robin.magic_realm.components.quest.*;
import com.robin.magic_realm.components.quest.requirement.QuestRequirement;
import com.robin.magic_realm.components.quest.requirement.QuestRequirement.RequirementType;
import com.robin.magic_realm.components.quest.reward.QuestReward;
import com.robin.magic_realm.components.quest.reward.QuestReward.RewardType;

public class QuestStepPanel extends JPanel {

	private QuestBuilderFrame parent;
	private QuestStepView questStepView; 

	private GameData realmSpeakData;
	private Quest quest;
	private QuestStep step;

	private JTextField stepName;
	private SuggestionTextArea stepDescription;
	private JTable stepLogicTable;
	private JRadioButton andLogicButton;
	private JRadioButton orLogicButton;
	
	private JTable requirementsTable;
	private QuestTableEditorPanel requirementPanel;
	private JRadioButton andReqButton;
	private JRadioButton orReqButton;
	
	private JTable rewardsTable;
	private QuestTableEditorPanel rewardPanel;
	
	public QuestStepPanel(QuestBuilderFrame parent,QuestStepView questStepView, GameData realmSpeakData, Quest quest, QuestStep step) {
		this.parent = parent;
		this.questStepView = questStepView;
		this.realmSpeakData = realmSpeakData;
		this.quest = quest;
		this.step = step;
		initComponents();
		readStep();
		updateControls();
	}
	
	public QuestStep getStep() {
		return step;
	}

	public void readStep() {
		stepName.setText(step.getName());
		stepDescription.setText(step.getDescription());
		stepDescription.setCaretPosition(0);
		
		andLogicButton.setSelected(step.getLogicType()==QuestStepType.And);
		orLogicButton.setSelected(step.getLogicType()==QuestStepType.Or);
		
		andReqButton.setSelected(step.getReqType()==QuestStepType.And);
		orReqButton.setSelected(step.getReqType()==QuestStepType.Or);
	}

	private void updateControls() {
		requirementPanel.getEditAction().setEnabled(requirementsTable.getSelectedRowCount()==1);
		requirementPanel.getDeleteAction().setEnabled(requirementsTable.getSelectedRowCount()==1);
		requirementPanel.getMoveUpAction().setEnabled(requirementsTable.getSelectedRow()>0);
		requirementPanel.getMoveDownAction().setEnabled(requirementsTable.getSelectedRow()>=0 && requirementsTable.getSelectedRow()<step.getRequirements().size()-1);
		
		rewardPanel.getEditAction().setEnabled(rewardsTable.getSelectedRowCount()==1);
		rewardPanel.getDeleteAction().setEnabled(rewardsTable.getSelectedRowCount()==1);
		rewardPanel.getMoveUpAction().setEnabled(rewardsTable.getSelectedRow()>0);
		rewardPanel.getMoveDownAction().setEnabled(rewardsTable.getSelectedRow()>=0 && rewardsTable.getSelectedRow()<step.getRewards().size()-1);
	}
	
	private void initComponents() {
		setLayout(new GridLayout(2,2));

		add(buildForm());
		add(buildRequirementsPanel());
		add(buildStepLogicPanel());
		add(buildRewardsPanel());
	}

	private Box buildForm() {
		Box line;
		UniformLabelGroup group = new UniformLabelGroup();
		group.setLabelFont(QuestBuilderFrame.LabelFont);

		Box form = Box.createVerticalBox();
		form.add(Box.createVerticalStrut(10));

		line = group.createLabelLine("Step Name");
		stepName = new JTextField();
		stepName.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				step.setName(stepName.getText());
			}
		});
		ComponentTools.lockComponentSize(stepName, 300, 25);
		line.add(stepName);
		line.add(Box.createHorizontalGlue());
		form.add(line);
		form.add(Box.createVerticalStrut(10));

		group = new UniformLabelGroup();
		group.setLabelFont(QuestBuilderFrame.LabelFont);
		line = group.createLabelLine("Design Notes (not used in game)");
		line.add(Box.createHorizontalGlue());
		form.add(line);
		
		line = Box.createHorizontalBox();
		line.add(Box.createHorizontalStrut(10));
		stepDescription = new SuggestionTextArea(15,40);
		stepDescription.setFont(QuestGuiConstants.QuestDescriptionFont);
		stepDescription.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent ev) {
				stepDescription.setWords(quest.getLocationTags());
			}
			public void focusLost(FocusEvent ev) {
				step.setDescription(stepDescription.getText());
			}
		});
		stepDescription.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				step.setDescription(stepDescription.getText());
				questStepView.setSelectedStep(step);
			}
		});
		stepDescription.setLineWrap(true);
		stepDescription.setWrapStyleWord(true);
		line.add(new JScrollPane(stepDescription));
		line.add(Box.createHorizontalStrut(10));
		form.add(line);
		form.add(Box.createVerticalStrut(10));

		return form;
	}

	private JPanel buildStepLogicPanel() {
		JPanel stepLogic = new JPanel(new BorderLayout());
		JPanel typePanel = new JPanel(new GridLayout(1,2));
		ButtonGroup group = new ButtonGroup();
		andLogicButton = new JRadioButton("All prereq steps (AND)",true);
		andLogicButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				step.setLogicType(QuestStepType.And);
			}
		});
		typePanel.add(andLogicButton);
		orLogicButton = new JRadioButton("Any prereq step (OR)");
		orLogicButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				step.setLogicType(QuestStepType.Or);
			}
		});
		typePanel.add(orLogicButton);
		group.add(andLogicButton);
		group.add(orLogicButton);
		stepLogic.add(typePanel,BorderLayout.SOUTH);
		stepLogicTable = new JTable(new StepLogicTableModel());
		stepLogicTable.setFocusable(false);
		stepLogicTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent ev) {
				questStepView.updateSteps(quest.getSteps());
				questStepView.setSelectedStep(step);
			}
		});
		stepLogicTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent ev) {
				stepLogicTable.clearSelection();
			}
		});
		ComponentTools.lockColumnWidth(stepLogicTable, 0, 50);
		ComponentTools.lockColumnWidth(stepLogicTable, 1, 50);
		ComponentTools.lockColumnWidth(stepLogicTable, 2, 50);
		ComponentTools.lockColumnWidth(stepLogicTable, 3, 30);
		stepLogic.add(new JScrollPane(stepLogicTable), BorderLayout.CENTER);
		stepLogic.setBorder(BorderFactory.createTitledBorder("Step Logic"));
		return stepLogic;
	}

	private JPanel buildRequirementsPanel() {
		requirementsTable = new JTable(new RequirementTableModel());
		requirementsTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent ev) {
				if (ev.getClickCount()==2) {
					requirementPanel.edit();
				}
			}
		});
		requirementsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent ev) {
				updateControls();
			}
		});
		ComponentTools.lockColumnWidth(requirementsTable,0,100);

		requirementPanel = new QuestTableEditorPanel("Requirements",requirementsTable,true) {
			public void add() {
				ButtonOptionDialog dialog = new ButtonOptionDialog(parent, null, "Choose a requirement type:", "New Requirement", true,3);
				for (RequirementType rt : RequirementType.values()) {
					boolean enabled = rt!=RequirementType.OccupyLocation || quest.getLocations().size()>0;
					dialog.addSelectionObject(rt,enabled,rt.getDescription());
				}
				dialog.setVisible(true);

				RequirementType selectedType = (RequirementType) dialog.getSelectedObject();
				if (selectedType != null) {
					QuestRequirement req = step.createRequirement(selectedType);
					QuestRequirementEditor editor = new QuestRequirementEditor(parent,realmSpeakData,quest,req);
					editor.setVisible(true);
					if (editor.getCanceledEdit()) {
						step.deleteRequirement(req);
					}
					requirementsTable.revalidate();
					quest.updateActivatePossible();
					parent.updateCard();
					updateControls();
				}
			}
			public void edit() {
				int selRow = requirementsTable.getSelectedRow();
				QuestRequirement req = step.getRequirements().get(selRow);
				QuestRequirementEditor editor = new QuestRequirementEditor(parent,realmSpeakData,quest,req);
				editor.setVisible(true);
				requirementsTable.revalidate();
				updateControls();
			}
			public void delete() {
				int selRow = requirementsTable.getSelectedRow();
				QuestRequirement req = step.getRequirements().get(selRow);
				step.deleteRequirement(req);
				requirementsTable.revalidate();
				quest.updateActivatePossible();
				parent.updateCard();
				updateControls();
			}
			public void moveUp() {
				moveRequirement(-1);
			}
			public void moveDown() {
				moveRequirement(1);
			}
			private void moveRequirement(int dir) {
				int selRow = requirementsTable.getSelectedRow();
				int newPos = selRow+dir;
				ArrayList<QuestRequirement> reqs = step.getRequirements();
				QuestRequirement sel = reqs.remove(selRow);
				int curr = 0;
				for(QuestRequirement req:reqs) {
					if (curr==newPos) step.getGameObject().add(sel.getGameObject());
					step.getGameObject().add(req.getGameObject());
					curr++;
				}
				if (curr==newPos) step.getGameObject().add(sel.getGameObject());
				step.refresh();
				requirementsTable.clearSelection();
				((AbstractTableModel)requirementsTable.getModel()).fireTableDataChanged();
				requirementsTable.addRowSelectionInterval(newPos,newPos);
				requirementsTable.revalidate();
				updateControls();
			}
		};
		
		JPanel typePanel = new JPanel(new GridLayout(1,2));
		ButtonGroup group = new ButtonGroup();
		andReqButton = new JRadioButton("All requirements (AND)",true);
		andReqButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				step.setReqType(QuestStepType.And);
			}
		});
		typePanel.add(andReqButton);
		orReqButton = new JRadioButton("Any requirement (OR)");
		orReqButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				step.setReqType(QuestStepType.Or);
			}
		});
		typePanel.add(orReqButton);
		group.add(andReqButton);
		group.add(orReqButton);
		
		requirementPanel.add(typePanel,BorderLayout.SOUTH);
		
		requirementPanel.setBorder(BorderFactory.createTitledBorder("Step Requirements"));
		return requirementPanel;
	}

	private JPanel buildRewardsPanel() {
		rewardsTable = new JTable(new RewardTableModel());
		rewardsTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent ev) {
				if (ev.getClickCount()==2) {
					rewardPanel.edit();
				}
			}
		});
		rewardsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent ev) {
				updateControls();
			}
		});
		ComponentTools.lockColumnWidth(rewardsTable,0,50);		
		ComponentTools.lockColumnWidth(rewardsTable,1,100);
		
		rewardPanel = new QuestTableEditorPanel("Rewards",rewardsTable,true) {
			public void add() {
				ButtonOptionDialog dialog = new ButtonOptionDialog(parent, null, "Choose a reward type:", "New Reward", true,3);
				for (RewardType rt : RewardType.values()) {
					boolean enabled = !rt.requiresLocations() || quest.getLocations().size()>0;
					dialog.addSelectionObject(rt,enabled,rt.getDescription());
				}
				dialog.setVisible(true);

				RewardType selectedType = (RewardType) dialog.getSelectedObject();
				if (selectedType != null) {
					QuestReward reward = step.createReward(selectedType);
					QuestRewardEditor editor = new QuestRewardEditor(parent,realmSpeakData,quest,reward);
					editor.setVisible(true);
					if (editor.getCanceledEdit()) {
						step.deleteReward(reward);
					}
					rewardsTable.revalidate();
					updateControls();
				}
			}
			public void edit() {
				int selRow = rewardsTable.getSelectedRow();
				QuestReward reward = step.getRewards().get(selRow);
				QuestRewardEditor editor = new QuestRewardEditor(parent,realmSpeakData,quest,reward);
				editor.setVisible(true);
				rewardsTable.revalidate();
				updateControls();
			}
			public void delete() {
				int selRow = rewardsTable.getSelectedRow();
				QuestReward reward = step.getRewards().get(selRow);
				step.deleteReward(reward);
				rewardsTable.revalidate();
				updateControls();
			}
			public void moveUp() {
				moveReward(-1);
			}
			public void moveDown() {
				moveReward(1);
			}
			private void moveReward(int dir) {
				int selRow = rewardsTable.getSelectedRow();
				int newPos = selRow+dir;
				ArrayList<QuestReward> rews = step.getRewards();
				QuestReward sel = rews.remove(selRow);
				int curr = 0;
				for(QuestReward rew:rews) {
					if (curr==newPos) step.getGameObject().add(sel.getGameObject());
					step.getGameObject().add(rew.getGameObject());
					curr++;
				}
				if (curr==newPos) step.getGameObject().add(sel.getGameObject());
				step.refresh();
				rewardsTable.clearSelection();
				((AbstractTableModel)rewardsTable.getModel()).fireTableDataChanged();
				rewardsTable.addRowSelectionInterval(newPos,newPos);
				rewardsTable.revalidate();
				updateControls();
			}
		};
		rewardPanel.setBorder(BorderFactory.createTitledBorder("Step Rewards"));
		return rewardPanel;
	}

	private static String[] StepLogicHeader = { "Req", "OnFail", "Stops", "#", "Step Name", };
	private static Class[] StepLogicClass = { Boolean.class, Boolean.class, Boolean.class, String.class, String.class, };

	private class StepLogicTableModel extends DefaultTableModel {

		public int getColumnCount() {
			return StepLogicHeader.length;
		}

		public String getColumnName(int index) {
			return StepLogicHeader[index];
		}

		public Class getColumnClass(int index) {
			return StepLogicClass[index];
		}

		public int getRowCount() {
			return quest.getSteps().size() - 1;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex < getRowCount()) {
				int skip = quest.getSteps().indexOf(step);
				if (rowIndex >= skip)
					rowIndex++;
				QuestStep rowStep = quest.getSteps().get(rowIndex);
				ArrayList steps;
				switch (columnIndex) {
					case 0:
						steps = step.getRequiredSteps();
						return steps != null && steps.contains(rowStep.getGameObject().getStringId());
					case 1:
						steps = step.getFailSteps();
						return steps != null && steps.contains(rowStep.getGameObject().getStringId());
					case 2:
						steps = step.getPreemptedSteps();
						return steps != null && steps.contains(rowStep.getGameObject().getStringId());
					case 3:
						return rowStep.getId();
					case 4:
						return rowStep.getName();
				}
			}
			return null;
		}

		public boolean isCellEditable(int row, int col) {
			return Boolean.class.equals(getColumnClass(col));
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (rowIndex < getRowCount()) {
				int skip = quest.getSteps().indexOf(step);
				int stepIndex = (rowIndex >= skip) ? rowIndex + 1 : rowIndex;
				QuestStep rowStep = quest.getSteps().get(stepIndex);

				boolean set = ((Boolean) aValue);
				if (columnIndex == 0) {
					if (set) {
						step.addRequiredStep(rowStep);
						step.removeFailStep(rowStep);
						step.removePreemptedStep(rowStep);
						fireTableCellUpdated(rowIndex, 1);
						fireTableCellUpdated(rowIndex, 2);
					}
					else {
						step.removeRequiredStep(rowStep);
					}
				}
				else if (columnIndex == 1) {
					if (set) {
						step.addFailStep(rowStep);
						step.removeRequiredStep(rowStep);
						step.removePreemptedStep(rowStep);
						fireTableCellUpdated(rowIndex, 0);
						fireTableCellUpdated(rowIndex, 2);
					}
					else {
						step.removeFailStep(rowStep);
					}
				}
				else if (columnIndex == 2) {
					if (set) {
						step.addPreemptedStep(rowStep);
						step.removeFailStep(rowStep);
						step.removeRequiredStep(rowStep);
						fireTableCellUpdated(rowIndex, 0);
						fireTableCellUpdated(rowIndex, 1);
					}
					else {
						step.removePreemptedStep(rowStep);
					}
				}
			}
		}
	}
	
	private static String[] RequirementHeader = { "Type", "Description", };
	private class RequirementTableModel extends AbstractTableModel {

		public int getColumnCount() {
			return RequirementHeader.length;
		}

		public String getColumnName(int index) {
			return RequirementHeader[index];
		}

		public Class getColumnClass(int index) {
			return String.class;
		}

		public int getRowCount() {
			return step.getRequirements().size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex < getRowCount()) {
				QuestRequirement requirement = step.getRequirements().get(rowIndex);
				switch (columnIndex) {
					case 0:
						return requirement.getRequirementType().toString();
					case 1:
						return requirement.getDescription();
				}
			}
			return null;
		}
	}
	
	private static String[] RewardHeader = { "Group", "Type", "Description", };

	private class RewardTableModel extends AbstractTableModel {
		
		public int getColumnCount() {
			return RewardHeader.length;
		}

		public String getColumnName(int index) {
			return RewardHeader[index];
		}

		public Class getColumnClass(int index) {
			return String.class;
		}

		public int getRowCount() {
			return step.getRewards().size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex < getRowCount()) {
				QuestReward reward = step.getRewards().get(rowIndex);
				switch (columnIndex) {
					case 0:
						return reward.getRewardGroup();
					case 1:
						return reward.getRewardType().toString();
					case 2:
						return reward.getDescription();
				}
			}
			return null;
		}
	}
}