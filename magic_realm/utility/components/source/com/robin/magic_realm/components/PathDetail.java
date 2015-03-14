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
package com.robin.magic_realm.components;

import java.awt.*;
import java.awt.geom.*;

import com.robin.magic_realm.map.Tile;


public class PathDetail implements Comparable {
	protected TileComponent parent;
	protected int num;
	protected String from;
	protected String to;
	protected ClearingDetail c1;
	protected ClearingDetail c2;
	protected Point arc;
	protected String type;
	
	protected String tileSideName;
	
	public PathDetail(TileComponent parent,int num,String from,String to,ClearingDetail c1,ClearingDetail c2,Point arc,String type,String tileSideName) {
		this.parent = parent;
		this.num = num;
		this.from = from;
		this.to = to;
		this.c1 = c1;
		this.c2 = c2;
		this.arc = arc;
		this.type = type;
		this.tileSideName = tileSideName;
	}
	public int compareTo(Object o1) {
		int ret = 0;
		if (o1 instanceof PathDetail) {
			PathDetail p = (PathDetail)o1;
			ret = getParent().getTileName().compareTo(p.getParent().getTileName());
			if (ret==0) {
				ret = p.tileSideName.compareTo(tileSideName);
				if (ret==0) {
					ret = getClearingConnectionString().compareTo(p.getClearingConnectionString());
				}
			}
		}
		return ret;
	}
	/**
	 * Returns a connected ClearingDetail, or null if none
	 */
	public ClearingDetail findConnection(ClearingDetail clearing) {
		if (c1==clearing) { // compare pointers
			if (c2.isEdge()) {
				String edge = Tile.convertEdge(c2.getType(),parent.getRotation());
				TileComponent connectedTile = parent.getAdjacentTile(edge);
				if (connectedTile!=null) {
					PathDetail connectedPath = connectedTile.getEdgePath(Tile.matchingEdge(edge));
					return connectedPath.getEdgeClearing();
				}
			}
			else {
				return c2;
			}
		}
		else if (c2==clearing) { // compare pointers
			if (c1.isEdge()) {
				String edge = Tile.convertEdge(c2.getType(),parent.getRotation());
				TileComponent connectedTile = parent.getAdjacentTile(edge);
				if (connectedTile!=null) {
					PathDetail connectedPath = connectedTile.getEdgePath(Tile.matchingEdge(edge));
					return connectedPath.getEdgeClearing();
				}
			}
			else {
				return c1;
			}
		}
		return null;
	}
	public boolean connectsToAnEdge() {
		return getEdge()!=null;
	}
	/**
	 * A map edge is an edge that doesn't lead to another tile.
	 */
	public boolean connectsToMapEdge() {
		if (getEdge()!=null) { // Connects to a tile edge and
			if (findConnection(getEdgeClearing())==null) { // doesn't have a connected clearing
				return true;
			}
		}
		return false;
	}
	/**
	 * Get's the path (if any) connected to the edge on the connecting tile.
	 */
	public PathDetail getEdgePathFromOtherTile() {
		// Start with the clearing that connects to the edge (null if none)
		ClearingDetail edgeClearing = getEdgeClearing();
		if (edgeClearing!=null) {
			// Now find the clearing on the other tile (null if none)
			ClearingDetail otherClearing = findConnection(edgeClearing);
			if (otherClearing!=null) {
				// Iterate through all paths that connect to that clearing, and find the one that connects back to this one!  (sounds a bit loopy...)
				for (PathDetail pd:otherClearing.getParent().findConnections(otherClearing)) {
					if (pd.findConnection(otherClearing).equals(edgeClearing)) {
						return pd;
					}
				}
			}
		}
		return null;
	}
	public boolean hasClearing(ClearingDetail clearing) {
		return c1.equals(clearing) || c2.equals(clearing);
	}
	/**
	 * Returns the edge this path is connected to (ie., NW) or null if not
	 */
	public String getEdge() {
		if (c1.isEdge()) {
			return c1.getType();
		}
		else if (c2.isEdge()) {
			return c2.getType();
		}
		return null;
	}
	/**
	 * @return		The ClearingDetail object that represents the tile edge.
	 */
	public ClearingDetail getEdgeAsClearing() {
		if (c1.isEdge()) {
			return c1;
		}
		else if (c2.isEdge()) {
			return c2;
		}
		return null;
	}
	/**
	 * @return		The ClearingDetail object that connects to the tile edge.
	 */
	public ClearingDetail getEdgeClearing() {
		if (c1.isEdge()) {
			return c2;
		}
		else if (c2.isEdge()) {
			return c1;
		}
		return null;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Shape getShape() {
		Point p1 = c1.getPosition();
		Point p2 = c2.getPosition();
		if (arc!=null) {
			return new QuadCurve2D.Float(
					(float)p1.x,
					(float)p1.y,
					(float)arc.x,
					(float)arc.y,
					(float)p2.x,
					(float)p2.y);
		}
		else {
			return new Line2D.Float(
					(float)p1.x,
					(float)p1.y,
					(float)p2.x,
					(float)p2.y);
		}
	}
	public void setArcPoint(Point p) {
		this.arc = p;
	}
	public Point getArcPoint() {
		return arc;
	}
	public boolean requiresDiscovery() {
		return isSecret() || isHidden();
	}
	public boolean isSecret() {
		return type.equals("secret");
	}
	public boolean isHidden() {
		return type.equals("hidden");
	}
	public Color getColor() {
		if (type.equals("caves")) {
			return Color.black;
		}
		else if (type.equals("secret")) {
			return Color.black;
		}
		else if (type.equals("hidden")) {
			return MagicRealmColor.BROWN;
		}
		return MagicRealmColor.TAN;
	}
	public boolean isDotted() {
		return type.equals("caves");
	}
	public boolean isNarrow() {
		return isSecret() || isHidden();
	}
	public ClearingDetail getFrom() {
		return c1;
	}
	public ClearingDetail getTo() {
		return c2;
	}
	public String toString() {
		return "path_"+num+" = "+from+" to "+to;
	}
	/**
	 * @return Returns the parent.
	 */
	public TileComponent getParent() {
		return parent;
	}
	public String getDescription(ClearingDetail fromClearing) {
		ClearingDetail toClearing = findConnection(fromClearing);
		StringBuffer sb = new StringBuffer();
		sb.append("a ");
		if (isHidden()) {
			sb.append("hidden ");
		}
		else if (isSecret()) {
			sb.append("secret ");
		}
		sb.append("path to ");
		sb.append(toClearing.getDescription());
		sb.append(".");
		return sb.toString();
	}
	
	public String getTileSideName() {
		return tileSideName;
	}
	/**
	 * Returns a string like "2-4" indicating clearings 2 and 4 are connected by this path.  If EDGE is returned, then
	 * this path connects to an edge.
	 */
	public String getClearingConnectionString() {
		int n1 = c1.getNum();
		int n2 = c2.getNum();
		if (n1>0 && n2>0) {
			return n1<n2?(n1+"-"+n2):(n2+"-"+n1);
		}
		StringBuffer sb = new StringBuffer();
		sb.append(n1>0?n1:n2);
		sb.append("-");
		ClearingDetail c = n1>0?c1:c2;
		sb.append(c.parent.translateEdgeBasedOnRotation(getEdge()));
		return sb.toString();
//		return "EDGE"+num; // num is here to keep this key unique in the getFullPathKey() method
	}
	/**
	 * Returns a unique string that describes this path
	 */
	public String getFullPathKey() {
		// Needs to be human readable, so that sharing discoveries is easy...
		return parent.getTileName()+" ("+tileSideName+") "+getClearingConnectionString();
	}
}