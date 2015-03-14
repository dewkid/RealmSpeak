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
package com.robin.magic_realm.RealmCharacterBuilder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.StringTokenizer;

import javax.swing.*;

import com.robin.general.graphics.GraphicsUtil;
import com.robin.general.io.FileManager;
import com.robin.general.swing.AggressiveDialog;
import com.robin.general.swing.IconFactory;

public class SymbolEditDialog extends AggressiveDialog {
	
	private JLabel mainIcon;
	private JLabel invertedIcon;
	private RealmCharacterBuilderModel model;
	private FileManager graphicsManager;
	
	private JCheckBox invertImageOption;
	private JButton editIconButton;
	private JButton generateIconButton;
	private JButton doneButton;
	
	public SymbolEditDialog(JFrame frame,RealmCharacterBuilderModel model,FileManager graphicsManager) {
		super(frame,"Edit Symbol",true);
		this.model = model;
		this.graphicsManager = graphicsManager;
		initComponents();
		setLocationRelativeTo(frame);
	}
	private ImageIcon getInvertedIcon() {
		ImageIcon icon = model.getCharacter().getFullSizedIcon();
		return new ImageIcon(CharacterInfoCard.getInvertedImage(icon));
	}
	private void initComponents() {
		setSize(250,250);
		setLayout(new BorderLayout());
		JPanel icons = new JPanel(new GridLayout(1,2));
		mainIcon = new JLabel(model.getCharacterToken().getIcon());
		icons.add(mainIcon);
		invertedIcon = new JLabel("");
		icons.add(invertedIcon);
		add(icons,"Center");
		
		JPanel buttons = new JPanel(new GridLayout(4,1));
		invertImageOption = new JCheckBox("Invert Icon on Black",!model.getCharacter().getGameObject().hasThisAttribute(CharacterInfoCard.NO_INVERT_ICON));
		invertImageOption.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (invertImageOption.isSelected()) {
					model.getCharacter().getGameObject().removeThisAttribute(CharacterInfoCard.NO_INVERT_ICON);
				}
				else {
					model.getCharacter().getGameObject().setThisAttribute(CharacterInfoCard.NO_INVERT_ICON);
				}
				updateImages();
			}
		});
		buttons.add(invertImageOption);
		editIconButton = new JButton("Load Symbol");
		editIconButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				loadSymbol();
			}
		});
		buttons.add(editIconButton);
		generateIconButton = new JButton("Generate Symbol");
		generateIconButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				generateSymbol();
			}
		});
		buttons.add(generateIconButton);
		doneButton = new JButton("Done");
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				setVisible(false);
			}
		});
		buttons.add(doneButton);
		add(buttons,"South");
		
		updateImages();
	}
	private void loadSymbol() {
		File file = graphicsManager.getLoadPath();
		if (file!=null) {
			ImageIcon icon = IconFactory.findIcon(file.getAbsolutePath());
			if (icon!=null) {
				model.updateSymbolIcon(icon);
				updateImages();
			}
		}
	}
	private void generateSymbol() {
		String symbol;
		String name = model.getCharacter().getCharacterLevelName(4);
		StringTokenizer tokens = new StringTokenizer(name," ");
		if (tokens.countTokens()==1) {
			symbol = tokens.nextToken().substring(0,2);
		}
		else if (tokens.countTokens()>1) {
			symbol = tokens.nextToken().substring(0,1) + tokens.nextToken().substring(0,1);
		}
		else {
			return;
		}
		
		BufferedImage bi = new BufferedImage(100,100,BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = (Graphics2D)bi.getGraphics();
		g.setFont(new Font("Dialog",Font.BOLD,56));
		g.setColor(Color.black);
		GraphicsUtil.drawCenteredString(g,0,0,100,100,symbol);
		ImageIcon icon = new ImageIcon(bi);
		model.updateSymbolIcon(icon);
		updateImages();
	}
	private void updateImages() {
		mainIcon.setIcon(model.getCharacterToken().getIcon());
		if (model.getCharacter().getGameObject().hasThisAttribute(CharacterInfoCard.NO_INVERT_ICON)) {
			ImageIcon icon = model.getCharacter().getFullSizedIcon();
			BufferedImage bi = new BufferedImage(icon.getIconWidth(),icon.getIconHeight(),BufferedImage.TYPE_3BYTE_BGR);
			Graphics big = bi.getGraphics();
			big.setColor(Color.black);
			big.fillRect(0,0,icon.getIconWidth(),icon.getIconHeight());
			big.drawImage(icon.getImage(),0,0,null);
			invertedIcon.setIcon(new ImageIcon(bi));
		}
		else {
			invertedIcon.setIcon(getInvertedIcon());
		}

		repaint();
	}
}