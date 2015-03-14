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
package com.robin.general.swing;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JSplitPane;

public class JSplitPaneImproved extends JSplitPane {
	private boolean isPainted = false;
	private boolean hasProportionalLocation = false;
	private double proportionalLocation;

	public JSplitPaneImproved() {
		super();
	}

	public JSplitPaneImproved(int orientation) {
		super(orientation);
	}

	public JSplitPaneImproved(int orientation, boolean newContinuousLayout) {
		super(orientation, newContinuousLayout);
	}

	public JSplitPaneImproved(int orientation, Component leftComponent, Component rightComponent) {
		super(orientation, leftComponent, rightComponent);
	}

	public JSplitPaneImproved(int orientation, boolean newContinuousLayout, Component leftComponent, Component rightComponent) {
		super(orientation, newContinuousLayout, leftComponent, rightComponent);
	}

	public void setDividerLocation(double proportionalLocation) {
		isPainted = false;
		hasProportionalLocation = true;
		this.proportionalLocation = proportionalLocation;
	}

	public void paint(Graphics g) {
		if (!isPainted) {
			if (hasProportionalLocation)
				super.setDividerLocation(proportionalLocation);
			isPainted = true;
		}
		super.paint(g);
	}
}