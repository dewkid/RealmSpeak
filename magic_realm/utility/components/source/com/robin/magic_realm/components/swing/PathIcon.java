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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import com.robin.game.objects.GameObject;
import com.robin.general.graphics.TextType;
import com.robin.general.swing.IconFactory;
import com.robin.general.swing.IconGroup;
import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.utility.RealmLoader;
import com.robin.magic_realm.components.utility.RealmUtility;

public class PathIcon extends ImageIcon {
	private PathDetail path;
	public PathIcon(PathDetail path) {
		this.path = path;
		updateImage();
	}
	private void updateImage() {
		BufferedImage bi = new BufferedImage(48,48, BufferedImage.TYPE_4BYTE_ABGR);
		ImageIcon icon = null;
		String name;
		if (path.isSecret()) {
			icon = IconFactory.findIcon("pending/tables/secret.png");
			name = "Secret";
		}
		else if (path.isHidden()) {
			icon = IconFactory.findIcon("pending/tables/hidden.png");
			name = "Hidden";
		}
		else {
			icon = IconFactory.findIcon("pending/tables/normal.png");
			name = "Road";
		}
		Graphics g = bi.getGraphics();
		g.drawImage(icon.getImage(),2,2,44,44,null);
		g.setColor(Color.white);
		StringBuilder sb = new StringBuilder();
		sb.append(path.getFrom().getNumString());
		sb.append("-");
		sb.append(path.getTo().getNumString());
		String val = sb.toString();
		val = StringUtilities.findAndReplace(val,".","");
		
		TextType tt1 = new TextType(name,48,"WHITE_NOTE");
		tt1.draw(g,0,6);
		TextType tt2 = new TextType(val,48,"STAT_WHITE");
		tt2.draw(g,0,22);
		setImage(bi);
	}
	
	public static void main(String[] args) {
		RealmUtility.setupTextType();
		RealmLoader loader = new RealmLoader();
		GameObject go = loader.getData().getGameObjectByName("Ruins");
		TileComponent crag = (TileComponent)RealmComponent.getRealmComponent(go);
		crag.setDarkSideUp(); // enchanted
		ClearingDetail clearing = crag.getClearing(6); // this clearing has both a hidden path, as well as a secret path.
		IconGroup group = new IconGroup(IconGroup.VERTICAL,2);
		for(PathDetail path:clearing.getAllConnectedPaths()) {
			PathIcon icon = new PathIcon(path);
			group.addIcon(icon);
		}
		
		JOptionPane.showMessageDialog(null,group);
	}
}