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

import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.*;

/**
 * This class manages onscreen frames, and allows an application to guarantee that there is only one of each type,
 * as specified by a unique key.
 */
public class FrameManager {
	
	public static String DEFAULT_FRAME_KEY = "default";
	
	private ManagedFrame mostRecentFrame;
	private HashMap frameHash;
	
	private FrameManager() {
		frameHash = new HashMap();
	}
	private void _dispose(ManagedFrame frame) {
		frame.setVisible(false);
		frame.dispose();
		frame.cleanup();
	}
	private void _show(ManagedFrame frame) {
		frame.setVisible(true);
		frame.toFront();
	}
	/**
	 * Adds a frame to the manager.  If there is already a frame registered to the frameKey, that frame is hidden, and disposed.
	 */
	public void addFrame(ManagedFrame frame) {
		disposeFrame(frame.getKey());
		frameHash.put(frame.getKey(),frame);
		mostRecentFrame = frame;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				_show(mostRecentFrame);
			}
		});
	}
	/**
	 * Forces a frame registered to the frameKey to display.  Returns false if there is not.
	 */
	public boolean showFrame(String frameKey) {
		ManagedFrame cached = (ManagedFrame)frameHash.get(frameKey);
		if (cached!=null) {
			_show(cached);
			return true;
		}
		return false;
	}
	public void disposeFrame(String frameKey) {
		ManagedFrame cached = (ManagedFrame)frameHash.get(frameKey);
		if (cached!=null) {
			_dispose(cached);
			frameHash.remove(frameKey);
		}
	}
	public void refresh() {
		for(Iterator i=frameHash.values().iterator();i.hasNext();) {
			ManagedFrame frame = (ManagedFrame)i.next();
			frame.toFront();
		}
	}
	private static FrameManager singleton = null;
	public static FrameManager getFrameManager() {
		if (singleton==null) {
			singleton = new FrameManager();
		}
		return singleton;
	}
	public static void refreshAll() {
		FrameManager fm = getFrameManager();
		fm.refresh();
	}
	public static void showDefaultManagedFrame(JFrame parent,String message,String title,Icon icon,boolean modalLike) {
		JTextArea cm = new JTextArea(message);
		cm.setEditable(false);
		cm.setOpaque(false);
		cm.setFont(UIManager.getFont("Label.font"));
//		cm.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		showDefaultManagedFrame(parent,cm,title,icon,modalLike);
	}
	public static void showDefaultManagedFrame(JFrame parent,JComponent message,String title,Icon icon,boolean modalLike) {
		ManagedFrame mf = getDefaultManagedFrame(parent,message,title,icon,modalLike);
		getFrameManager().addFrame(mf);
	}
	private static ManagedFrame getDefaultManagedFrame(JFrame parent,JComponent message,String title,Icon icon,boolean modalLike) {
		DefaultManagedFrame mf = new DefaultManagedFrame(parent,message,title,icon,modalLike);
		return mf;
	}
	/**
	 * The default managed frame behaves much like a modal dialog, without locking down the parent frame.  It is
	 * aggressive, and will stay on top.  It will NOT, however, lock the thread that calls it, so if this is the
	 * desired behavior, use JOptionPane instead.
	 */
	private static class DefaultManagedFrame extends JFrame implements ManagedFrame,ActionListener,WindowListener {
		
		private JFrame parent;
		private JButton okButton;
		
		public DefaultManagedFrame(JFrame parent,JComponent message,String title,Icon icon,boolean modalLike) {
			super(title);
			if (parent!=null && modalLike) {
				parent.addWindowListener(this);
			}
			this.parent = parent;
			setLayout(new BorderLayout());
			JPanel panel = new JPanel(new BorderLayout(10,10));
			panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
			panel.add(message,"Center");
			if (icon!=null) {
				panel.add(new JLabel(icon),"West");
			}
			Box box = Box.createHorizontalBox();
			okButton = new JButton("Close");
			okButton.addActionListener(this);
			getRootPane().setDefaultButton(okButton);
			box.add(Box.createHorizontalGlue());
			box.add(okButton);
			box.add(Box.createHorizontalGlue());
			panel.add(box,"South");
			add(panel,"Center");
			pack();
			setLocationRelativeTo(parent);
		}
		public void cleanup() {
			okButton.removeActionListener(this);
			if (parent!=null) {
				parent.removeWindowListener(this);
			}
		}
		public String getKey() {
			return DEFAULT_FRAME_KEY;
		}
		public void actionPerformed(ActionEvent e) {
			// The only way this method is called, is on a close event
			FrameManager.getFrameManager().disposeFrame(getKey());
		}
		public void windowActivated(WindowEvent e) {
			toFront();
		}
		public void windowClosed(WindowEvent e) {
		}
		public void windowClosing(WindowEvent e) {
		}
		public void windowDeactivated(WindowEvent e) {
		}
		public void windowDeiconified(WindowEvent e) {
		}
		public void windowIconified(WindowEvent e) {
		}
		public void windowOpened(WindowEvent e) {
		}
	}
}