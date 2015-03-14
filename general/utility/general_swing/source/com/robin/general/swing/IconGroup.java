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

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * A class for showing multiple icons as a single icon
 */
public class IconGroup extends ImageIcon {
	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;

	private int direction;
	private int spacing;
	private int maxWidth;
	private int maxHeight;

	private int finalWidth;
	private int finalHeight;
	private Image unscaledImage;

	public IconGroup(int direction, int spacing) {
		this(null, direction, spacing, 0, 0);
	}

	public IconGroup(int direction, int spacing, int maxWidth, int maxHeight) {
		this(null, direction, spacing, maxWidth, maxHeight);
	}

	public IconGroup(ImageIcon start, int direction, int spacing) {
		this(start, direction, spacing, 0, 0);
	}

	public IconGroup(ImageIcon start, int direction, int spacing, int maxWidth, int maxHeight) {
		this.direction = direction;
		this.spacing = spacing;
		this.maxWidth = maxWidth;
		if (start != null) {
			addIcon(start);
		}
	}

	public void addIcon(ImageIcon icon) {
		int w = finalWidth;
		int h = finalHeight;
		int iw = icon.getIconWidth();
		int ih = icon.getIconHeight();

		Image bi = null;
		if (direction == VERTICAL) {
			finalWidth = Math.max(w, iw);
			finalHeight = h + ih + spacing;
			bi = new BufferedImage(finalWidth, finalHeight, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics g = bi.getGraphics();
			if (unscaledImage != null && w > 0) {
				g.drawImage(unscaledImage, (finalWidth - w) >> 1, 0, null);
			}
			g.drawImage(icon.getImage(), (finalWidth - iw) >> 1, h + spacing, null);
		}
		else {
			finalWidth = w + iw + spacing;
			finalHeight = Math.max(h, ih);
			bi = new BufferedImage(w + iw + spacing, finalHeight, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics g = bi.getGraphics();
			if (unscaledImage != null && h > 0) {
				g.drawImage(unscaledImage, 0, (finalHeight - h) >> 1, null);
			}
			g.drawImage(icon.getImage(), w + spacing, (finalHeight - ih) >> 1, null);
		}
		unscaledImage = bi;
		double scaleForWidthAdj = 1.0;
		double scaleForHeightAdj = 1.0;
		if (maxWidth > 0 && finalWidth > maxWidth) {
			scaleForWidthAdj = (double) maxWidth / (double) finalWidth;
		}
		if (maxHeight > 0 && finalHeight > maxHeight) {
			scaleForHeightAdj = (double) maxHeight / (double) finalHeight;
		}
		double scale = Math.min(scaleForWidthAdj, scaleForHeightAdj);
		if (scale < 1.0) {
			int newWidth = (int) (finalWidth * scale);
			int newHeight = (int) (finalHeight * scale);
			bi = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = (Graphics2D) bi.getGraphics();

			RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			//renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHints(renderHints);

			g.drawImage(unscaledImage, AffineTransform.getScaleInstance(scale, scale), null);
		}
		setImage(bi);
	}

	public static void main(String[] args) {
		ImageIcon icon2 = IconFactory.findIcon("icons/arrow2.gif");
		ImageIcon icon4 = IconFactory.findIcon("icons/arrow4.gif");
		ImageIcon icon6 = IconFactory.findIcon("icons/arrow6.gif");
		ImageIcon icon8 = IconFactory.findIcon("icons/arrow8.gif");
		IconGroup group = new IconGroup(HORIZONTAL, 10, 100, 0);
		group.addIcon(icon2);
		group.addIcon(icon4);
		group.addIcon(icon6);
		group.addIcon(icon8);
		JOptionPane.showMessageDialog(null, "This is a test", "", JOptionPane.INFORMATION_MESSAGE, group);
	}
}