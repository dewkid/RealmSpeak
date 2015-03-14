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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.*;
import java.beans.PropertyVetoException;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.robin.game.objects.GameData;
import com.robin.magic_realm.components.attribute.ChatLine;
import com.robin.magic_realm.components.swing.CenteredMapView;
import com.robin.magic_realm.components.utility.Constants;
import com.robin.magic_realm.components.utility.RealmCalendar;
import com.robin.magic_realm.components.wrapper.*;

public class RealmInspectorFrame extends RealmSpeakInternalFrame {

	protected RealmSpeakFrame parentFrame;
	protected GameData gameData;
	protected CenteredMapView map;
	protected JSlider zoomSlider;
	protected GameWrapper game;
	protected HostPrefWrapper hostPrefs;
	protected RealmCalendar calendar;

	public RealmInspectorFrame(RealmSpeakFrame parentFrame,GameData data,GameWrapper game) {
		super("Game Map",true,false,true,true);
		this.parentFrame = parentFrame;
		this.gameData = data;
		this.game = game;
		hostPrefs = HostPrefWrapper.findHostPrefs(data);
		calendar = RealmCalendar.getCalendar(gameData);
		initComponents();
	}
	public CenteredMapView getMap() {
		return map;
	}
	public void setZoomSlider(boolean val) {
		zoomSlider.setVisible(val);
	}
	public void setClearingHighlight(boolean val) {
		map.setClearingHighlight(val);
	}
	public void setShowSeasonIcon(boolean val) {
		map.setShowSeasonIcon(val);
	}
	public void setShowChatLines(int val) {
		map.setShowChatLines(val);
	}
	public void redrawMap() {
		StringBuffer sb = new StringBuffer("Game Map");
		if (hostPrefs.isUsingSeasons()) {
			sb.append(" - ");
			sb.append(calendar.getSeasonName(game.getMonth()));
			if (hostPrefs.hasPref(Constants.OPT_WEATHER)) {
				sb.append(", ");
				sb.append(calendar.getWeatherName(game.getMonth()));
			}
		}
		if (hostPrefs.getBoardAutoSetup()) {
			sb.append(" - Rated ");
			sb.append(game.getCurrentMapRating());
		}
		setTitle(sb.toString());
		map.setShowEmbellishments(game.getGameStarted());
		map.setReplot(true);
		map.repaint();
	}
	private void initComponents() {
		setSize(640,480);
		getContentPane().setLayout(new BorderLayout());
		
		map = new CenteredMapView(gameData);
		map.setShowEmbellishments(game.getGameStarted());
		map.setHostMap(parentFrame.getGameHandler().isHostPlayer());
		map.setScale(0.7);
		map.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ev) {
				double scale = map.getScale();
				int val = (int)(scale*100.0);
				zoomSlider.setValue(val);
			}
		});

		getContentPane().add(map,"Center");
		addKeyListener(map.getShiftKeyListener());
		
		zoomSlider = new JSlider(10,100,70);
		zoomSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ev) {
				double val = (double)zoomSlider.getValue();
				double scale = val/100.0;
				map.setScale(scale);
			}
		});
		getContentPane().add(zoomSlider,"South");
		zoomSlider.setVisible(false);
		
		map.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent ev) {
				if (parentFrame.isActive() && !isSelected()) {
					try {
						setSelected(true);
					}
					catch(PropertyVetoException ex) {
						// vaaaahhhh??????
					}
					catch(ArrayIndexOutOfBoundsException ex) {
						// This is annoying, and I have no idea what causes it, but this exception is 
						// thrown inside of Container.getComponent()  somewhere....
						// Ignoring the exception is the only solution, as I see it!
					}
				}
			}
		});
		
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT,0),"dummy"); // this reserves the SHIFT key, somehow!?
		// There should really be an action mapping, but for some reason, its not used anyway!!!
	}
	/**
	 * Resize according to a set strategy
	 */
	public void organize(JDesktopPane desktop) {
		Dimension size = desktop.getSize();
		int w = size.width>>1;
		if (forceWidth!=null) {
			w = forceWidth.intValue();
		}
		setSize(w,size.height-BOTTOM_HEIGHT);
		setLocation(size.width-w,0);
		try {
			setIcon(false);
		}
		catch(PropertyVetoException ex) {
			ex.printStackTrace();
		}
		map.centerMap();
	}
	public boolean onlyOneInstancePerGame() {
		return true;
	}
	public String getFrameTypeName() {
		return "Map";
	}
	public void cleanup() {
		removeKeyListener(map.getShiftKeyListener());
	}
	public void addChatLine(ChatLine line) {
		map.addChatLine(line);
	}
}