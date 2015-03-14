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
package com.robin.general.io;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.swing.*;
import java.awt.image.*;

public class ImageZip {
	private static final int BUFFER = 2048;
	
	private static String image2String(Image image) {
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		BufferedImage bi = new BufferedImage(w,h,BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = bi.createGraphics();
		g.drawImage(image,0,0,null);
		StringBuffer sb = new StringBuffer();
		sb.append(w);
		sb.append(",");
		sb.append(h);
		sb.append(",");
		for (int x=0;x<w;x++) {
			for (int y=0;y<h;y++) {
				sb.append(",");
				int rgb = bi.getRGB(x,y);
				sb.append(rgb);
			}
		}
		return sb.toString();
	}
	private static Image string2Image(String string) {
		try {
			ArrayList integers = new ArrayList();
			StringTokenizer st = new StringTokenizer(string,",");
			while(st.hasMoreTokens()) {
				String val = st.nextToken();
				Integer n = Integer.valueOf(val);
				integers.add(n);
			}
			
			int w = ((Integer)integers.remove(0)).intValue();
			int h = ((Integer)integers.remove(0)).intValue();
			BufferedImage bi = new BufferedImage(w,h,BufferedImage.TYPE_4BYTE_ABGR);
			for (int x=0;x<w;x++) {
				for (int y=0;y<h;y++) {
					int rgb = ((Integer)integers.remove(0)).intValue();
					bi.setRGB(x,y,rgb);
				}
			}
			return bi;
		}
		catch(NumberFormatException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	public static void zipImages(String filename,Image[] image) throws FileNotFoundException {
		zipImages(new FileOutputStream(filename),image);
	}
	public static void zipImages(OutputStream outputStream,Image[] image) {
		try {
			ZipOutputStream zo = new ZipOutputStream(outputStream);
			zo.setMethod(ZipOutputStream.DEFLATED);
			
			for (int i=0;i<image.length;i++) {
				String iData = image2String(image[i]);
				byte[]data = iData.getBytes();
				ZipEntry ze = new ZipEntry("image"+i+".dat");
				zo.putNextEntry(ze);
				zo.write(data,0,data.length);
				zo.closeEntry();
			}
			
			zo.close();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	public static Image[] unzipImages(String filename) throws FileNotFoundException {
		return unzipImages(new FileInputStream(filename));
	}
	public static Image[] unzipImages(InputStream inputStream) {
		ArrayList images = new ArrayList();
		try {
			ZipInputStream zi = new ZipInputStream(inputStream);
//			ZipEntry ze;
			while ((zi.getNextEntry())!=null) {
				byte[]data = new byte[BUFFER];
				ArrayList allBytes = new ArrayList();
				int size;
				while((size=zi.read(data,0,BUFFER))!=-1) {
					for (int i=0;i<size;i++) {
						allBytes.add(new Byte(data[i]));
					}
				}
				data = new byte[allBytes.size()];
				int n=0;
				for (Iterator i=allBytes.iterator();i.hasNext();) {
					Byte b = (Byte)i.next();
					data[n++] = b.byteValue();
				}
				
				zi.closeEntry();
				String iData = new String(data);
				Image image = string2Image(iData);
				images.add(image);
			}
			zi.close();
			
			return (Image[])images.toArray(new Image[images.size()]);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	/**
	 * For testing only
	 */
	public static void main(String[] args) {
		Image[] image = new Image[10];
		for (int i=0;i<10;i++) {
			image[i] = (new ImageIcon(i%2==0?"test1.gif":"test2.gif")).getImage();
			
		}
	
		try {
			zipImages("test.zip",image);
			Image[] rImage = unzipImages("test.zip");
			
			for (int i=0;i<rImage.length;i++) {
				JFrame frame = new JFrame();
				frame.setSize(100,100);
				frame.setLocation(200+(i*50),200+(i*50));
				frame.getContentPane().add(new JLabel(new ImageIcon(rImage[i])));
				frame.setVisible(true);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}