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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;

import com.robin.general.swing.AggressiveDialog;
import com.robin.magic_realm.components.attribute.TileLocation;

public class TileLocationChooser extends AggressiveDialog {
	private static Rectangle lastDisplayArea = null;
	
	private CenteredMapView viewer;
	private TileLocation selectedLocation;
	
	private MouseAdapter mouseListener = new MouseAdapter() {
		public void mouseClicked(MouseEvent ev) {
			TileLocation tl = viewer.getTileLocationAtPoint(ev.getPoint());
			if (tl!=null) {
				if (tl.tile.isMarked()) {
					tl.clearing = null; // clicking a marked tile nullifies any clearing result
					selectedLocation = tl;
					cleanClose();
				}
				if (tl.hasClearing() && tl.clearing.isMarked()) {
					selectedLocation = tl;
					cleanClose();
				}
			}
		}
	};

	public TileLocationChooser(JFrame parent,CenteredMapView map,TileLocation center) { // May want to include a cancel option here...
		this(parent,map,center,"Clearing Chooser");
	}
	
	public TileLocationChooser(JFrame parent,CenteredMapView map,TileLocation center,String title) { // May want to include a cancel option here...
		super(parent,title,true);
		this.viewer = map;
		selectedLocation = null;
		initComponents();
		if (lastDisplayArea == null) {
			setSize(800,600);
			setLocationRelativeTo(parent);
		}
		else {
			setSize(lastDisplayArea.width,lastDisplayArea.height);
			setLocation(lastDisplayArea.x,lastDisplayArea.y);
		}
		initMapSize();
		setDefaultCloseOperation(AggressiveDialog.DO_NOTHING_ON_CLOSE);
		if (center!=null) {
			viewer.centerOn(center);
		}
		else {
			viewer.centerMap();
		}
	}
	private void initMapSize() {
		Dimension s = getSize();
		Insets i = getInsets();
		int w = s.width-i.left-i.right;
		int h = s.height-i.top-i.bottom;
		viewer.setSize(w,h);
	}
	public TileLocation getSelectedLocation() {
		return selectedLocation;
	}
	private void initComponents() {
		getContentPane().setLayout(new BorderLayout());
		viewer.addMouseListener(mouseListener);
		getContentPane().add(viewer,"Center");
 	}
	private void cleanClose() {
		viewer.removeMouseListener(mouseListener);
		lastDisplayArea = new Rectangle(getLocation().x,getLocation().y,getSize().width,getSize().height);
		setVisible(false);
		dispose();
	}
}