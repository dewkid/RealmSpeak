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

import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

import com.robin.game.objects.*;

public class GameObjectTreeView extends JFrame {
	protected JTree tree;
	
	public GameObjectTreeView(Collection gameObjects) {
		init(gameObjects);
	}
	private void init(Collection gameObjects) {
		setSize(400,500);
		getContentPane().setLayout(new BorderLayout());
			DefaultMutableTreeNode top = new DefaultMutableTreeNode("top");
			
			// Add all base objects (not held by anything)
			Hashtable hash = new Hashtable();
			for (Iterator i=gameObjects.iterator();i.hasNext();) {
				GameObject object = (GameObject)i.next();
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(object);
				if (object.getHeldBy()==null) {
					top.add(node);
				}
				hash.put(object.toString(),node);
			}
			
			// Now use the hash to add all the branches
			for (Iterator i=gameObjects.iterator();i.hasNext();) {
				GameObject object = (GameObject)i.next();
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)hash.get(object.toString());
				for (Iterator c=object.getHold().iterator();c.hasNext();) {
					GameObject heldObject = (GameObject)c.next();
					DefaultMutableTreeNode child = (DefaultMutableTreeNode)hash.get(heldObject.toString());
					node.add(child);
				}
			}
			
			tree = new JTree(top);
			tree.setRootVisible(false);
			tree.setShowsRootHandles(true);
		getContentPane().add(new JScrollPane(tree));
	}
}