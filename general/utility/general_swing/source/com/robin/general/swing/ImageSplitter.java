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

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.ImageIcon;

public class ImageSplitter {
	
	private ImageIcon icon;
	private int imagesPerRow;
	private int columnWidth;
	private ArrayList rowHeights;
	
	public ImageSplitter(String path,int columnWidth) {
		this.icon = IconFactory.findIcon(path);
		this.columnWidth = columnWidth;
		imagesPerRow = icon.getIconWidth()/columnWidth;
		this.rowHeights = new ArrayList();
	}
	public void addRow(int rowHeight) {
		rowHeights.add(new Integer(rowHeight));
	}
	public void addRows(int[] rowHeight) {
		for (int i=0;i<rowHeight.length;i++) {
			addRow(rowHeight[i]);
		}
	}
	private int getY(int row) {
		int y = 0;
		for (int i=0;i<(row-1);i++) {
			y += ((Integer)rowHeights.get(i)).intValue();
		}
		return y;
	}
	private int getH(int row) {
		int h = ((Integer)rowHeights.get(row-1)).intValue();
		return h;
	}
	public ImageIcon[] getImageIcons(int row) {
		if (row<1 || row>rowHeights.size()) {
			throw new IllegalArgumentException("invalid row");
		}
		ImageIcon[] ii = new ImageIcon[imagesPerRow];
		Image image = icon.getImage();
		int y = getY(row);
		int w = columnWidth;
		int h = getH(row);
		for (int n=0;n<imagesPerRow;n++) {
			int x = n*w;
			BufferedImage bi = new BufferedImage(w,h,BufferedImage.TYPE_4BYTE_ABGR);
			Graphics g = bi.getGraphics();
			g.drawImage(image,0,0,w-1,h-1,x,y,x+w-1,y+h-1,null);
			ii[n] = new ImageIcon(bi);
		}
		
		return ii;
	}
	public ImageIcon[] getImageIcons(int row1,int row2) {
		if (row1<1 || row1>rowHeights.size() || row2<1 || row2>rowHeights.size()) {
			throw new IllegalArgumentException("invalid row");
		}
		Image image = icon.getImage();
		ImageIcon[] ii = new ImageIcon[imagesPerRow];
		int w = columnWidth;
		
		int h = getH(row1);
		if (getH(row2)!=h) {
			throw new IllegalArgumentException("rows must be same height");
		}
		int y1 = getY(row1);
		int y2 = getY(row2);
		
		int tw = w*2;
		for (int n=0;n<imagesPerRow;n++) {
			int x = n*w;
			BufferedImage bi = new BufferedImage(tw,h,BufferedImage.TYPE_4BYTE_ABGR);
			Graphics g = bi.getGraphics();
//			g.setColor(Color.red);
//			g.fillRect(0,0,w*2,h);
			g.drawImage(image,0,0,w-1,h-1,x,y1,x+w-1,y1+h-1,null);
			g.drawImage(image,w,0,tw-1,h-1,x,y2,x+w-1,y2+h-1,null);
			ii[n] = new ImageIcon(bi);
		}
		
		return ii;
	}
//	private int[] MECHANIC_GIF = {64,64,32,32,64,64,64,32,32,32,64,64,64,32,32,32,32,32,32,32,32,32,64};
//	private int[] SYMBOL_GIF = {32,32,32,32,32,32,32,32,32,32,32,32,64,64,32,32,32,32,32,32,32,32,32,96,96,32};
//	public static void main(String[]args) {
//		JFileChooser chooser = new JFileChooser();
//		int ret = chooser.showOpenDialog(null);
//		if (ret==JFileChooser.APPROVE_OPTION) {
//			File file = chooser.getSelectedFile();
//			ImageSplitter splitter = new ImageSplitter(file.getPath(),32);
////			splitter.addRows()
//		}
//	}
}