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
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.event.HyperlinkEvent.EventType;

public class ScrollingText extends JComponent implements ActionListener {
	
	private Timer scrollTimer;
	private int scrollSpeed = 1;
	private int currentScroll = 0;
	
	private boolean scrollHold = false;
	private int delayScroll = 0;
	
	private Dimension size;
	private ImageIcon backgroundImage;
	private Insets insets;
	private Color backgroundColor;
	
	private Point mouseHover;
	private Point mouseDragStart;
	private int currentScrollAtDragStart;
	
	private ArrayList<ScrollLine> lines;
	private ArrayList<HyperlinkListener> listeners;
	
	public ScrollingText(ImageIcon backgroundImage,Insets insets) {
		this(backgroundImage.getIconWidth(),backgroundImage.getIconHeight(),null);
		this.backgroundImage = backgroundImage;
		this.insets = insets;
		setCursor(new Cursor(Cursor.HAND_CURSOR));
	}
	
	public ScrollingText(int width,int height,Color backgroundColor) {
		lines = new ArrayList<ScrollLine>();
		size = new Dimension(width,height);
		setPreferredSize(size);
		this.backgroundColor = backgroundColor;
		scrollTimer = new Timer(30,this);
		setDoubleBuffered(true);
		insets = null;
		setupMouse();
	}
	private void setupMouse() {
		mouseHover = null;
		MouseInputAdapter input = new MouseInputAdapter() {
			public void mouseMoved(MouseEvent ev) {
				mouseHover = getAdjustedPoint(ev.getPoint());
			}
			public void mouseEntered(MouseEvent ev) {
				mouseHover = getAdjustedPoint(ev.getPoint());
			}
			public void mouseExited(MouseEvent ev) {
				mouseHover = null;
				scrollHold = false;
				delayScroll = 0;
			}
			public void mousePressed(MouseEvent ev) {
				scrollHold = true;
				mouseHover = null;
				mouseDragStart = getAdjustedPoint(ev.getPoint());
				currentScrollAtDragStart = currentScroll;
			}
			public void mouseDragged(MouseEvent ev) {
				Point p = getAdjustedPoint(ev.getPoint());
				if (mouseDragStart!=null) {
					int dy = p.y - mouseDragStart.y;
					currentScroll = currentScrollAtDragStart - dy;
				}
			}
			public void mouseReleased(MouseEvent ev) {
				scrollHold = false;
				delayScroll = 100;
				mouseDragStart = null;
				mouseHover = getAdjustedPoint(ev.getPoint());
			}
			public void mouseClicked(MouseEvent ev) {
				Point p = getAdjustedPoint(ev.getPoint());
				String link = getLinkUrl(p);
				if (link!=null) {
					scrollHold = false;
					delayScroll = 0;
					fireHyperlinkUpdate(link);
				}
			}
		};
		
		addMouseListener(input);
		addMouseMotionListener(input);
	}
	public void addHyperlinkListener(HyperlinkListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<HyperlinkListener>();
		}
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	public void removeHyperlinkListener(HyperlinkListener listener) {
		if (listeners!=null && listeners.contains(listener)) {
			listeners.remove(listener);
			if (listeners.isEmpty()) {
				listeners = null;
			}
		}
	}
	private void fireHyperlinkUpdate(String link) {
		if (listeners==null) return;
		try {
			URL url = new URL(link);
			HyperlinkEvent ev = new HyperlinkEvent(this,EventType.ACTIVATED,url);
			for (HyperlinkListener listener:listeners) {
				listener.hyperlinkUpdate(ev);
			}
		}
		catch(MalformedURLException ex) {
			System.err.println("Malformed URL: "+link);
		}
	}
	
	public void start() {
		scrollTimer.start();
	}
	
	public void addLine(ScrollLine scrollLine) {
		lines.add(scrollLine);
	}
	public void actionPerformed(ActionEvent arg0) {
		if (!isDisplayable()) {
			scrollTimer.stop();
		}
		if (delayScroll>0) {
			delayScroll--;
		}
		else if (!scrollHold) {
			currentScroll += scrollSpeed;
		}
		repaint();
	}
	
	private Point getAdjustedPoint(Point p) {
		if (p!=null && insets!=null) {
			p = new Point(p.x - insets.left,p.y - insets.top);
		}
		return p;
	}
	
	public String getLinkUrl(Point p) {
		if (p!=null) {
			for (ScrollLine line:lines) {
				String link = line.linkAtPoint(p);
				if (link!=null) return link;
			}
		}
		return null;
	}
	
	public void paintComponent(Graphics g1) {
		Graphics2D g = (Graphics2D)g1;
		if (backgroundColor!=null) {
			g.setColor(backgroundColor);
			g.fillRect(0,0,size.width,size.height);
		}
		if (backgroundImage!=null) {
			g.drawImage(backgroundImage.getImage(),0,0,null);
		}
		
		int width = size.width;
		int height = size.height;
		if (insets!=null) {
			width = size.width-insets.left-insets.right;
			height = size.height-insets.top-insets.bottom;
			g = (Graphics2D)g.create(insets.left,insets.top,width,height);
		}
		
		int extra = lines.size()>0?lines.get(0).getHeight(g):0;
		int y = height + extra - currentScroll;
		for (ScrollLine line:lines) {
			line.draw(g,line.getLeftInset(g,width),y);
			y += line.getHeight(g);
			if (y>height+extra) break;
		}
		if (y<0) currentScroll = 0;
		
		String linkUrl = getLinkUrl(mouseHover);
		if (linkUrl!=null) {
			g1.setColor(getForeground());
			g1.setFont(getFont());
			g1.drawString(linkUrl,5,size.height-5);
		}
//		if (mouseHover!=null) {
//			g.setColor(getForeground());
//			g.fillOval(mouseHover.x-5,mouseHover.y-5,10,10);
//		}
	}
	
	public static void main(String[] args) {
		//ImageIcon ii = new ImageIcon("c:/parchment.png");
		
		String fontName = "Dialog";
		Font header = new Font(fontName,Font.BOLD,22);
		Font listing = new Font(fontName,Font.BOLD,12);
		Font url = new Font(fontName,Font.PLAIN,10);
		
		//ScrollingText scrolly = new ScrollingText(ii,new Insets(25,0,25,0));
		ScrollingText scrolly = new ScrollingText(300,400,Color.lightGray);
		scrolly.setFont(url);
		scrolly.setForeground(Color.blue);
		
		scrolly.addLine(new ScrollLine("Coding",header,Color.green,Color.black,2));
		scrolly.addLine(new ScrollLine("Dave One",listing,Color.black));
		scrolly.addLine(new ScrollLine("Mark Twosome",listing,Color.black,null,0,SwingConstants.CENTER,"http://www.google.com"));
		scrolly.addLine(new ScrollLine("Fred Threebay",listing,Color.black));
		scrolly.addLine(new ScrollLine("Steve Fourplay",listing,Color.black));
		scrolly.addLine(new ScrollLine("Gary Fivish",listing,Color.black));
		scrolly.addLine(new ScrollLine());
		scrolly.addLine(new ScrollLine("Testing",header,Color.green,Color.black,2));
		scrolly.addLine(new ScrollLine("Dave One",listing,Color.black));
		scrolly.addLine(new ScrollLine("Mark Twosome",listing,Color.black));
		scrolly.addLine(new ScrollLine("Fred Threebay",listing,Color.black));
		scrolly.addLine(new ScrollLine("Steve Fourplay",listing,Color.black,null,0,SwingConstants.CENTER,"http://www.google.com"));
		scrolly.addLine(new ScrollLine("Gary Fivish",listing,Color.black));
		
		scrolly.start();
		JOptionPane.showMessageDialog(new JFrame(),scrolly);
		System.exit(0);
	}
}