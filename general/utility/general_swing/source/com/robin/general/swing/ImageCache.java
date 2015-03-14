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
import java.io.File;
import java.util.Hashtable;

import javax.swing.*;

public class ImageCache {
	private static class ImagePath {
		private String folder;
		private String ext;
		public ImagePath(String folder,String ext) {
			this.folder = folder;
			this.ext = ext;
		}
		public String getPath(String name) {
			return folder+"/"+name+ext;
		}
	}
	
	private static ImagePath[] validPaths = { // do pending before images, in case there are new images to use!
		new ImagePath("pending",".gif"),
		new ImageCache.ImagePath("pending",".png"),
		new ImagePath("images",".gif"),
		new ImageCache.ImagePath("images",".png"),
	};
	
	private static Hashtable cache = new Hashtable();
	
	/**
	 * This method is a sneaky way to trick the cache into believing the icon has already been fetched.
	 */
	public static void _placeImage(String name,ImageIcon icon) {
		cache.put(name,icon);
	}
	
	public static void resetCache() {
		cache.clear();
		cache = new Hashtable();
	}
	public static ImageIcon getIcon(String name) { // why does this fail with custom characters?  (RealmSpeak)
		ImageIcon ii = null;
		if (name!=null) {
			ii = (ImageIcon)cache.get(name);
			if (ii==null) {
				String iconPath = null;
				for (ImagePath ip:validPaths) {
					iconPath = ip.getPath(name);
					ii = IconFactory.findIcon(iconPath);
					if (ii!=null) return ii;
				}
				File file = new File("./"+iconPath);
				System.err.println("Unable to locate image: "+name);
				JOptionPane.showMessageDialog(null,"Unable to locate an image: "
						+name
						+"\n\nLast absolute path searched was:\n\n"
						+file.getAbsolutePath()
						+"\n\nYou may need to redownload the program, or get the latest resource pack from the download site.",
						"Image Not Found",
						JOptionPane.ERROR_MESSAGE);
				
				throw new ImageCacheException("No image found for: "+name);
			}
		}
		return ii;
	}
	public static ImageIcon getIcon(String name,Color tint) {
		return getIcon(name,tint,0.4f); // default
	}
	public static ImageIcon getIcon(String name,Color tint,float percent) {
		if (name!=null) {
			String key = name+":c"+tint.toString()+":"+percent;
			ImageIcon ii = (ImageIcon)cache.get(key);
			if (ii==null) {
				ii = getIcon(name);
				BufferedImage bi = new BufferedImage(ii.getIconWidth(),ii.getIconHeight(),BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D g = (Graphics2D)bi.getGraphics();
				g.drawImage(ii.getImage(),0,0,null);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,percent));
				g.setColor(tint);
				g.fillRect(0,0,ii.getIconWidth(),ii.getIconHeight());
				ii = new ImageIcon(bi);
				cache.put(key,ii);
			}
			return ii;
		}
		return null;
	}
	public static ImageIcon getIcon(String name,int width,int height) {
		if (name!=null) {
			String key = name+":"+width+","+height;
			ImageIcon ii = (ImageIcon)cache.get(key);
			if (ii==null) {
				ii = getIcon(name);
				Image i = ii.getImage().getScaledInstance(width,height,Image.SCALE_SMOOTH);
				ii = new ImageIcon(i,key);
				cache.put(key,ii);
			}
			return ii;
		}
		return null;
	}
	public static ImageIcon getIcon(String name,int percent) {
		if (name!=null) {
			if (percent==100) {
				// No point doing scale logic if full size!
				return getIcon(name);
			}
			else {
				String key = name+":"+percent;
				ImageIcon ii = (ImageIcon)cache.get(key);
				if (ii==null) {
					ii = getIcon(name);
					int w = (ii.getIconWidth()*percent)/100;
					int h = (ii.getIconHeight()*percent)/100;
					Image i = ii.getImage().getScaledInstance(w,h,Image.SCALE_SMOOTH);
					ii = new ImageIcon(i,key);
					cache.put(key,ii);
				}
				return ii;
			}
		}
		return null;
	}
	public static ImageIcon getRotatedIcon(String name,int percent,int degrees) {
		ImageIcon normal = getIcon(name,percent);
		ImageIcon rotated = null;
		
		if (degrees!=0) {
			BufferedImage bi = new BufferedImage(normal.getIconWidth(),normal.getIconHeight(),BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = (Graphics2D)bi.getGraphics();
			double cx = ((double)normal.getIconWidth())/2.0;
			double cy = ((double)normal.getIconHeight())/2.0;
			AffineTransform rotation = AffineTransform.getRotateInstance(Math.toRadians((double)degrees),cx,cy);
			g.drawImage(normal.getImage(),rotation,null);
			rotated = new ImageIcon(bi);
		}
		else {
			rotated = normal;
		}
		
		return rotated;
	}
	public static void main(String[] args) {
		JLabel label1 = new JLabel(getIcon("spoo"));
		JOptionPane.showMessageDialog(null,label1);
		JLabel label2 = new JLabel(getIcon("spoo",Color.red,0.5f));
		JOptionPane.showMessageDialog(null,label2);
	}
}