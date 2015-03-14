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
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.event.MouseInputAdapter;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.general.graphics.GraphicsUtil;
import com.robin.general.swing.MouseUtility;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.TileComponent;
import com.robin.magic_realm.components.utility.RealmObjectMaster;

/**
 * This is my attempt to improve on CenteredMapView!
 * 
 * For now, I give up.  As messed up as CenteredMapView is, it works pretty danged well.
 */
public class FastMapView extends JComponent {
	
	protected static final int MAP_BORDER = 10;
	protected Dimension TILE_SIZE = new Dimension(TileComponent.TILE_WIDTH,TileComponent.TILE_HEIGHT);
	
	// Data
	private GameData gameData;
	private Hashtable mapGrid;
	
	// Map Drawing
	private double scale = 1.0;
	private Point offset;
	private Rectangle normalMapRect;
	private Rectangle borderRect;
	
	// Mouse information
	private Point sticky;
	private Point mouseHover;
	
	public FastMapView(GameData data) {
		setDoubleBuffered(true);
		gameData = data;
		setupMouse();
		updateGrid();
		if (mouseHover==null) ; // so there are no warnings!!  Sheesh.  (Delete this line to see what I mean)
	}
	public void setupMouse() {
		MouseInputAdapter mia = new MouseInputAdapter() {
			public void mousePressed(MouseEvent ev) {
				sticky = ev.getPoint();
			}
			public void mouseDragged(MouseEvent ev) {
				Point p = ev.getPoint();
				mouseHover = p;
				if (sticky==null) { // not sure how this can happen, but it did once!
					sticky = p;
				}
				offset.x += (p.x - sticky.x);
				offset.y += (p.y - sticky.y);
				sticky = p;
				repaint();
//				fixOffset();
//				repaint();
//				System.out.println(offset);
			}
			public void mouseReleased(MouseEvent ev) {
				sticky = null;
			}
			public void mouseClicked(MouseEvent ev) {
//				if (MouseUtility.isRightOrControlClick(ev)) { // right mouse click
//					if (tileBeingPlaced!=null) {
//						int r = tileBeingPlaced.getRotation();
//						r = (r+1) % 6;
//						tileBeingPlaced.setRotation(r);
//						updateTileBeingPlaced();
//					}
//					else {
//						mapRightClickMenu.show(CenteredMapView.this,ev.getPoint().x,ev.getPoint().y);
//					}
//				}
//				else {
//					if (tileBeingPlaced!=null) {
//						// Place tile and fire listener if over a valid placement location
//						placeTile(ev.getPoint());
//					}
//					else {
//						TileLocation tl = getTileLocationAtPoint(ev.getPoint());
//						if (tl!=null && tl.isInClearing()) {
//							fireActionPerformed(CLICK_CLEARING_ACTION,tl.asKey());
//						}
//					}
//				}
			}
			public void mouseMoved(MouseEvent ev) {
				mouseHover = ev.getPoint();
//				currentTileLocation = getTileLocationAtPoint(mouseHover);
//				updateShiftFlip(ev.isShiftDown());
			}
			public void mouseExited(MouseEvent ev) {
				mouseHover = null;
//				currentTileLocation = null;
//				updateShiftFlip(false);
			}
		};
		addMouseListener(mia);
		addMouseMotionListener(mia);
		addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				int d = e.getWheelRotation();
				scale = scale - (d*0.1*scale);
				repaint();
//				setScale(scale - (d*0.1*scale));
//				fireStateChanged();
			}
		});
	}
	public void updateGrid() {
		mapGrid = new Hashtable();
		Collection tileObjects = RealmObjectMaster.getRealmObjectMaster(gameData).getTileObjects();
		for (Iterator i=tileObjects.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			TileComponent tile = (TileComponent)RealmComponent.getRealmComponent(go);
			tile.setAlwaysPaint(true);
			String pos = (String)go.getAttribute("mapGrid","mapPosition");
			String rot = (String)go.getAttribute("mapGrid","mapRotation");
			
			if (pos!=null && rot!=null) {
				tile.setRotation(Integer.valueOf(rot).intValue());
				Point gp = GraphicsUtil.asPoint(pos);
				mapGrid.put(gp,tile);
			}
		}
		updateExtents();
	}
	private void updateExtents() {
		int colWidth = (TILE_SIZE.width*3)>>2;
		int rowHeight = TILE_SIZE.height;
		int rowAdjust = TILE_SIZE.height>>1;
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		for (Enumeration e=mapGrid.keys();e.hasMoreElements();) {
			Point pos = (Point)e.nextElement();
			int x = pos.x * colWidth;
			int y = (pos.x * rowAdjust) + (pos.y * rowHeight);
			if (x<minX) {
				minX = x;
			}
			if (x>maxX) {
				maxX = x;
			}
			if (y<minY) {
				minY = y;
			}
			if (y>maxY) {
				maxY = y;
			}
		}
		if (mapGrid.isEmpty()) {
			minX = 1;
			maxX = 1;
			minY = 1;
			maxY = 1;
		}
		normalMapRect = new Rectangle(minX,minY,maxX+TILE_SIZE.width-minX,maxY+TILE_SIZE.height-minY);
		borderRect = new Rectangle(
				normalMapRect.x - MAP_BORDER,
				normalMapRect.y - MAP_BORDER,
				normalMapRect.width + (MAP_BORDER<<1),
				normalMapRect.height + (MAP_BORDER<<1));
		
		offset = new Point(borderRect.x + (borderRect.width>>1),borderRect.y + (borderRect.height>>1));
	}
	public Point convertGridToCoordinate(Point gridPoint) {
		int colWidth = (TILE_SIZE.width*3)/4;
		int rowHeight = TILE_SIZE.height;
		int rowAdjust = TILE_SIZE.height>>1;
		int x = (gridPoint.x * colWidth) - borderRect.x;
		int y = ((gridPoint.x * rowAdjust) + (gridPoint.y * rowHeight)) - borderRect.y;
		return new Point(x,y);
	}
	
	public void paintComponent(Graphics g1) {
		Graphics2D g = (Graphics2D)g1;
		
		AffineTransform at = AffineTransform.getScaleInstance(scale,scale);
		g.setTransform(at);
		
		for (Iterator i=mapGrid.keySet().iterator();i.hasNext();) {
			Point gp = (Point)i.next();
			Point p = convertGridToCoordinate(gp);
//System.out.println("p="+p);
			TileComponent tile = (TileComponent)mapGrid.get(gp);
			tile.paintTo(g,p.x+offset.x,p.y+offset.y,TILE_SIZE.width,TILE_SIZE.height);
		}
	}
	
	public static void main(String[] args) {
		GameData data = new GameData();
		File file = (new File("../RealmSpeak/autosave.rsgame")).getAbsoluteFile();
		System.out.print("Load...");
		data.zipFromFile(file);
		System.out.println("Done.");
		
		JFrame frame = new JFrame("FastMapView Test");
		frame.setSize(800,600);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(new FastMapView(data),"Center");
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}