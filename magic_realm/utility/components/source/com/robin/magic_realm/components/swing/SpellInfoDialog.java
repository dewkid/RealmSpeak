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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.*;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.SpellWrapper;

/**
 * This dialog will display all information about a spell.  This includes:
 * <ul><li>Spell image without embellishments
 * <li>Caster
 * <li>Incantation object
 * <li>Target(s)
 */
public class SpellInfoDialog extends AggressiveDialog {
	
	private static final Font TITLE_FONT = new Font("Dialog",Font.BOLD,18);
	private static final Font MESSAGE_FONT = new Font("Dialog",Font.BOLD,12);
	
	private SpellWrapper spell;
	
	private JPanel center;
	private JPanel castingInfo = null;
	private JPanel spellDetail = null;
	
	private JButton infoButton;
	private JButton okayButton;
	private JButton creditButton;
	
	private SpellInfoDialog(JDialog frame,SpellWrapper spell,boolean textOnly) {
		super(frame,spell.getName(),true);
		init(spell,textOnly);
	}
	private SpellInfoDialog(JFrame frame,SpellWrapper spell,boolean textOnly) {
		super(frame,spell.getName(),true);
		init(spell,textOnly);
	}
	private void init(SpellWrapper spell,boolean textOnly) {
		this.spell = spell;
		initComponents(textOnly);
		setLocationRelativeTo(parent);
	}
	private JPanel getDisplayBox(String title,String message,RealmComponent rc) {
		JPanel box = new JPanel(new BorderLayout());
		box.setBorder(BorderFactory.createEtchedBorder());
		JLabel titleL = new JLabel(title,JLabel.CENTER);
		titleL.setFont(TITLE_FONT);
		box.add(titleL,"North");
		if (rc!=null) {
			box.add(new JLabel(rc.getIcon()));
		}
		if (message!=null) {
			JLabel messageL = new JLabel(message,JLabel.CENTER);
			messageL.setFont(MESSAGE_FONT);
			messageL.setForeground(Color.blue);
			box.add(messageL,"South");
		}
		ComponentTools.lockComponentSize(box,100,100);
		return box;
	}
	private void initComponents(boolean textOnly) {
		setSize(900,480);
		setResizable(false);
		getContentPane().setLayout(new BorderLayout(5,5));
		
		center = new JPanel(new BorderLayout());
		getContentPane().add(center,"Center");
		
		SpellCardComponent sc = (SpellCardComponent)RealmComponent.getRealmComponent(spell.getGameObject());
		JLabel cardIcon = new JLabel(sc.getUnembellishedIcon());
		Box left = Box.createVerticalBox();
		if (spell.isVirtual()) {
			JLabel virtualLabel = new JLabel(" Virtual Instance ",JLabel.CENTER);
			virtualLabel.setFont(new Font("Dialog",Font.BOLD,11));
			virtualLabel.setOpaque(true);
			virtualLabel.setBackground(new Color(150,150,255));
			left.add(virtualLabel);
		}
		left.add(cardIcon);
		infoButton = new JButton("Full Text");
		infoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if ("Full Text".equals(infoButton.getText())) {
					showFullText();
				}
				else {
					showCastingInfo();
				}
			}
		});
		if (textOnly) {
			showFullText();
		}
		else {
			showCastingInfo();
			left.add(infoButton);
		}
		left.add(Box.createVerticalGlue());
		getContentPane().add(left,"West");
		
		Box bottom = Box.createHorizontalBox();
		creditButton = new JButton("Rule Credits...");
		creditButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				ImageIcon icon = IconFactory.findIcon("images/tables/credits3e.gif");
				JOptionPane.showMessageDialog(parent, "","3rd Edition Rule Credits",JOptionPane.PLAIN_MESSAGE,icon);
			}
		});
		bottom.add(creditButton);
		bottom.add(Box.createHorizontalGlue());
		okayButton = new JButton("Okay");
		okayButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				setVisible(false);
			}
		});
		bottom.add(okayButton);
		bottom.add(Box.createHorizontalGlue());
		getContentPane().add(bottom,"South");
	}
	private void showCastingInfo() {
		if (castingInfo == null) {
			castingInfo = new JPanel(new GridLayout(2,1));
			
			JPanel topRow = new JPanel(new GridLayout(1,3));
			
			// Owner
			GameObject owner = spell.getGameObject().getHeldBy();
			topRow.add(getDisplayBox("Owner",owner.getName(),RealmComponent.getRealmComponent(owner)));
			
			// Caster
			GameObject caster = spell.isAlive()?spell.getCaster().getGameObject():null;
			topRow.add(getDisplayBox("Caster",caster==null?"None":caster.getName(),caster==null?null:RealmComponent.getRealmComponent(caster)));
			
			// Incantation Object
			GameObject inc = spell.getIncantationObject();
			topRow.add(getDisplayBox("Incantation",inc==null?"Not Cast":inc.getName(),inc==null?null:RealmComponent.getRealmComponent(inc)));
			
			castingInfo.add(topRow);
			
			ArrayList targets = spell.getTargets();
			JPanel bottomRow = getDisplayBox("Target"+(targets.size()==1?"":"s"),null,null);
			RealmObjectPanel targetPanel = new RealmObjectPanel(false,false);
			if (!targets.isEmpty()) {
				for (Iterator i=spell.getTargets().iterator();i.hasNext();) {
					RealmComponent rc = (RealmComponent)i.next();
					ImageIcon icon;
					if (rc.isTile()) {
						TileComponent tile = (TileComponent)rc;
						icon = tile.getRepresentativeIconBigger();
					}
					else {
						icon = rc.getIcon();
					}
					
					JLabel label = new JLabel(rc.getGameObject().getName(),icon,JLabel.CENTER);
					label.setHorizontalTextPosition(JLabel.CENTER);
					label.setVerticalTextPosition(JLabel.BOTTOM);
					targetPanel.add(label);
				}
				GameObject animal = spell.getTransformAnimal();
				if (animal!=null) {
					RealmComponent rc = RealmComponent.getRealmComponent(animal);
					JLabel label = new JLabel(rc.getGameObject().getName(),rc.getIcon(),JLabel.CENTER);
					label.setHorizontalTextPosition(JLabel.CENTER);
					label.setVerticalTextPosition(JLabel.BOTTOM);
					targetPanel.add(label);
				}
			}
			bottomRow.add(new JScrollPane(targetPanel),"Center");
			castingInfo.add(bottomRow);
		}
		infoButton.setText("Full Text");
		center.removeAll();
		center.add(castingInfo,"Center");
		repaint();
	}
	private void showFullText() {
		if (spellDetail==null) {
			ImageIcon table = SpellUtility.getSpellDetailTable(spell.getGameObject());
			JEditorPane detail = new JEditorPane("text/rtf",SpellUtility.getSpellDetail(spell.getGameObject()));
			detail.setEditable(false);
			//detail.setText(sb.toString());
			JScrollPane sp = new JScrollPane(detail);
			detail.setCaretPosition(0);
			
			spellDetail = new JPanel(new BorderLayout());
			spellDetail.add(sp,"Center");
			
			if (table!=null) {
				spellDetail.add(new JLabel(table),"South");
			}
		}
		infoButton.setText("Casting Info");
		center.removeAll();
		center.add(spellDetail,"Center");
		repaint();
	}
	public static void showSpellInfo(Window window,SpellWrapper spell) {
		showSpellInfo(window,spell,false);
	}
	public static void showSpellInfo(Window window,SpellWrapper spell,boolean textOnly) {
		SpellInfoDialog dialog;
		if (window instanceof JFrame) {
			dialog = new SpellInfoDialog((JFrame)window,spell,textOnly);
		}
		else if (window instanceof JDialog) {
			dialog = new SpellInfoDialog((JDialog)window,spell,textOnly);
		}
		else {
			throw new IllegalArgumentException("window class "+window.getClass()+" is illegal");
		}
		dialog.setVisible(true);
	}
	
	/**
	 * For testing only.
	 */
	public static void main(String[] args) {
		ComponentTools.setSystemLookAndFeel();
		RealmUtility.setupTextType();
		
		RealmLoader loader = new RealmLoader();
		GameObject wiz = loader.getData().getGameObjectByName("Wizard");
		GameObject go = loader.getData().getGameObjectByName("Transform");
		wiz.add(go);
		SpellWrapper spell = new SpellWrapper(go);
		//spell.testVirtual();
		showSpellInfo(new JFrame(), spell);
		System.exit(0);
	}
}