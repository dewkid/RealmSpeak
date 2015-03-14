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
package com.robin.game.GameBuilder;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;

import com.robin.game.objects.*;
import com.robin.general.swing.ComponentTools;
import com.robin.general.swing.IntegerField;
import com.robin.general.swing.UniformLabelGroup;

public class GameCommandDialog extends JDialog {

	protected GameCommand originalCommand;
	protected GameCommand modelCommand; // This is the model that allows us to know which controls are showing
	
	protected JComboBox type;
	protected JTextField newPool;
	protected JComboBox from;
	protected JComboBox to;
	protected JLabel targetObject;
	protected IntegerField count;
	protected JComboBox transferType;
	protected JTextField keyVals;
	
	protected Box newPoolBox;
	protected Box toBox;
	protected Box fromBox;
	protected Box targetObjectBox;
	protected Box countBox;
	protected Box transferTypeBox;
	protected Box keyValsBox;
	
	public GameCommandDialog(ArrayList allCommands,GameCommand originalCommand) {
		this.originalCommand = originalCommand;
		modelCommand = GameCommand.getCommandForName(originalCommand.getGameSetup(),originalCommand.getTypeName());
		modelCommand.copyFrom(originalCommand);
		setModal(true);
		initComponents(findAvailablePoolNames(allCommands));
	}
	private void updateControls() {
		String typeString = (String)type.getSelectedItem();
		GameCommand newCommand = GameCommand.getCommandForName(originalCommand.getGameSetup(),typeString);
		newCommand.copyFrom(modelCommand);
		modelCommand = newCommand;
		
		newPoolBox.setVisible(modelCommand.usesNewPool());
		toBox.setVisible(modelCommand.usesTo());
		fromBox.setVisible(modelCommand.usesFrom());
		targetObjectBox.setVisible(modelCommand.usesTargetObject());
		countBox.setVisible(modelCommand.usesCount());
		transferTypeBox.setVisible(modelCommand.usesTransferType());
		keyValsBox.setVisible(modelCommand.usesKeyVals());
		
		getContentPane().validate();
		getContentPane().repaint();
	}
	private ArrayList findAvailablePoolNames(ArrayList allCommands) {
		ArrayList previousPoolNames = new ArrayList();
		previousPoolNames.add("ALL");
		for (Iterator i=allCommands.iterator();i.hasNext();) {
			GameCommand prev = (GameCommand)i.next();
			if (prev==originalCommand) {
				// no longer previous!
				break;
			}
			if (prev.isCreate()) {
				previousPoolNames.add(prev.getNewPool());
			}
		}
		return previousPoolNames;
	}
	private void initComponents(ArrayList poolNames) {
		Box line;
		UniformLabelGroup group = new UniformLabelGroup();
		setSize(310,210);
		setTitle("Command edit");
		getContentPane().setLayout(new BorderLayout());
			Box box = Box.createVerticalBox();
				line = group.createLabelLine("Type");
					type = new JComboBox();
					type.addItem(GameCommandCreate.NAME);
					if (poolNames.size()>1) {
						type.addItem(GameCommandExtract.NAME);
						type.addItem(GameCommandMove.NAME);
						type.addItem(GameCommandDistribute.NAME);
						type.addItem(GameCommandAddTo.NAME);
					}
					type.setSelectedItem(modelCommand.getTypeName());
					ComponentTools.lockComponentSize(type,150,25);
				line.add(type);
				line.add(Box.createHorizontalGlue());
			box.add(line);
				newPoolBox = group.createLabelLine("New Pool");
					newPool = new JTextField(modelCommand.getNewPool());
					ComponentTools.lockComponentSize(newPool,150,25);
				newPoolBox.add(newPool);
				newPoolBox.add(Box.createHorizontalGlue());
			box.add(newPoolBox);
				fromBox = group.createLabelLine("From");
					from = new JComboBox(poolNames.toArray());
					from.setSelectedItem(modelCommand.getFrom());
					ComponentTools.lockComponentSize(from,150,25);
				fromBox.add(from);
				fromBox.add(Box.createHorizontalGlue());
			box.add(fromBox);
				toBox = group.createLabelLine("To");
					to = new JComboBox(poolNames.toArray());
					to.setSelectedItem(modelCommand.getTo());
					ComponentTools.lockComponentSize(to,150,25);
				toBox.add(to);
				toBox.add(Box.createHorizontalGlue());
			box.add(toBox);
				targetObjectBox = group.createLabelLine("Target Object");
					GameObject object = modelCommand.getTargetObject();
					String objName = "";
					if (object!=null) {
						objName = object.toString();
					}
					targetObject = new JLabel(objName);
					ComponentTools.lockComponentSize(targetObject,150,25);
				targetObjectBox.add(targetObject);
					JButton button = new JButton("Pick");
					button.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							GameObjectChooser chooser = new GameObjectChooser(GameCommandDialog.this,null,modelCommand.getGameSetup().getGameData());
							chooser.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							chooser.setVisible(true);
							ArrayList chosenObjects = chooser.getChosenObjects();
							if (chosenObjects!=null && chosenObjects.size()==1) {
								GameObject go = (GameObject)chosenObjects.iterator().next();
								modelCommand.setTargetObject(go);
								targetObject.setText(go.toString());
							}
						}
					});
				targetObjectBox.add(button);
				targetObjectBox.add(Box.createHorizontalGlue());
			box.add(targetObjectBox);
				countBox = group.createLabelLine("Count");
					count = new IntegerField(modelCommand.getCount());
					ComponentTools.lockComponentSize(count,150,25);
				countBox.add(count);
				countBox.add(Box.createHorizontalGlue());
			box.add(countBox);
				transferTypeBox = group.createLabelLine("Transfer");
					transferType = new JComboBox();
					transferType.addItem(GamePool.RANDOM_NAME);
					transferType.addItem(GamePool.FROM_BEGINNING_NAME);
					transferType.addItem(GamePool.FROM_END_NAME);
					ComponentTools.lockComponentSize(transferType,150,25);
					transferType.setSelectedItem(GamePool.getTransferName(modelCommand.getTransferType()));
				transferTypeBox.add(transferType);
				transferTypeBox.add(Box.createHorizontalGlue());
			box.add(transferTypeBox);
				keyValsBox = group.createLabelLine("KeyVals");
					keyVals = new JTextField(modelCommand.getKeyValString());
					ComponentTools.lockComponentSize(keyVals,150,25);
				keyValsBox.add(keyVals);
				keyValsBox.add(Box.createHorizontalGlue());
			box.add(keyValsBox);
			box.add(Box.createVerticalGlue());
				Box controls = Box.createHorizontalBox();
				controls.add(Box.createHorizontalGlue());
					JButton okay = new JButton("Okay");
					okay.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							GameCommand newCommand = createCommand();
							originalCommand.getGameSetup().updateCommand(originalCommand,newCommand);
							close();
						}
					});
				controls.add(okay);
					JButton cancel = new JButton("Cancel");
					cancel.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							close();
						}
					});
				controls.add(cancel);
			box.add(controls);
				
		getContentPane().add(box,"Center");
		type.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				updateControls();
			}
		});
		updateControls();
	}
	private GameCommand createCommand() {
		String selType = (String)type.getSelectedItem();
		
		GameCommand newCommand = GameCommand.getCommandForName(modelCommand.getGameSetup(),selType);
		
		newCommand.setNewPool(newPool.getText());
//		command.setType((String)type.getSelectedItem());
		newCommand.setFrom((String)from.getSelectedItem());
		newCommand.setTo((String)to.getSelectedItem());
		try {
			Integer n = Integer.valueOf(count.getText());
			newCommand.setCount(n.intValue());
		}
		catch(NumberFormatException ex) {
			// Ignore
		}
		newCommand.setTargetObject(modelCommand.getTargetObject());
		newCommand.setTransferType(GamePool.getTransferType((String)transferType.getSelectedItem()));
		newCommand.setKeyValString(keyVals.getText());
		newCommand.setModified(true);
		return newCommand;
	}
	public void close() {
		setVisible(false);
		dispose();
	}
}