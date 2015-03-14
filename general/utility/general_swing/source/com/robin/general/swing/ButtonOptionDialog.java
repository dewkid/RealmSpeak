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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.plaf.basic.BasicGraphicsUtils;

/**
 * Similar to JOptionPane.showInputDialog, except options are offered in buttons.
 */
public class ButtonOptionDialog extends AggressiveDialog {
	
	private static final Font font = UIManager.getFont("Label.font");

	private JTextArea area;
	private JPanel questionPanel;
	private Hashtable<String,Object> objectNameHash;
	private Object selectedObject;
	private Hashtable<String,JButton> buttons;
	private Hashtable<String,JLabel> labels;
	
	private ArrayList<Box> buttonBoxes;
	
	private boolean finished = false;
	
	private Font buttonFont = null;
	
	private int maxIconHeight = 0;
	private int columns;
	
	public ButtonOptionDialog(JFrame parent,Icon icon,String message,String title) {
		this(parent,icon,message,title,true);
	}
	public ButtonOptionDialog(JFrame parent,Icon icon,String message,String title,boolean includeCancel) {
		this(parent,icon,message,title,includeCancel,1);
	}
	public ButtonOptionDialog(JFrame parent,Icon icon,String message,String title,boolean includeCancel,int columns) {
		super(parent,title,true);
		this.columns = columns;
		buttons = new Hashtable<String,JButton>();
		labels = new Hashtable<String,JLabel>();
		buttonBoxes = new ArrayList<Box>();
		selectedObject = null;
		objectNameHash = new Hashtable<String,Object>();
		getContentPane().setLayout(new BorderLayout());
		
			questionPanel = new JPanel(new BorderLayout(10,10));
				area = new JTextArea(message);
				area.setFont(font);
				area.setEditable(false);
				area.setOpaque(false);
				area.setLineWrap(true);
				area.setWrapStyleWord(true);
				setBestAreaSize(area);
			questionPanel.add(area,"North");
			if (includeCancel) {
					JButton cancelButton = new JButton("Cancel");
					cancelButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							selectedObject = null;
							closeDialog();
						}
					});
					Box box = Box.createHorizontalBox();
					box.add(Box.createHorizontalGlue());
					box.add(cancelButton);
					box.add(Box.createHorizontalGlue());
				questionPanel.add(box,"South");
			}
			questionPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		getContentPane().add(questionPanel,"Center");
		
		if (icon!=null) {
			JLabel iconLabel = new JLabel(icon);
			getContentPane().add(iconLabel,"West");
		}
		
		setSize(400,400);
		
		setLocationRelativeTo(parent);
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}
	private void setBestAreaSize(JTextArea area) {
//		ComponentTools.lockComponentSize(area,200,70);
		area.setMinimumSize(new Dimension(20,20));
//		String text = area.getText();
//		//area.setColumns(text.length()>20?20:text.length());
//		area.setVisible(true);
//		Graphics g = area.getGraphics();
//		System.out.println(g);
//		TextType tt = new TextType(text,200,null);
//		int h = tt.getHeight(g);
//		area.setPreferredSize(new Dimension(200,h));
	}
	public int getSelectionObjectCount() {
		return objectNameHash.size();
	}
	public Object getSelectedObject() {
		return selectedObject;
	}
	public void addSelectionObjects(Collection c) {
		if (finished) {
			throw new IllegalStateException("You cannot add selection objects to finished panel.");
		}
		for (Iterator i=c.iterator();i.hasNext();) {
			addSelectionObject(i.next());
		}
	}
	public void addSelectionObjectArray(Object[] object) {
		if (finished) {
			throw new IllegalStateException("You cannot add selection objects to finished panel.");
		}
		for (int i=0;i<object.length;i++) {
			addSelectionObject(object[i]);
		}
	}
	public void addSelectionObject(Object object) {
		addSelectionObject(object,true);
	}
	public void addSelectionObject(Object object,boolean enabled) {
		addSelectionObject(object,enabled,null);
	}
	public void addSelectionObject(Object object,boolean enabled,String tooltip) {
		if (finished) {
			throw new IllegalStateException("You cannot add selection objects to finished panel.");
		}
		String key = object.toString();
		String string = key;
		if (string.indexOf("\n")>=0) {
			StringBuffer html = new StringBuffer();
			StringTokenizer tokens = new StringTokenizer(string,"\n");
			while(tokens.hasMoreTokens()) {
				if (html.length()==0) {
					html.append("<html><div align=\"center\">");
				}
				else {
					html.append("<br>");
				}
				html.append(tokens.nextToken());
			}
			if (html.length()>0) {
				html.append("</div></html>");
			}
			string = html.toString();
		}
		if (objectNameHash.get(key)==null) {
			JButton button = new JButton(string);
			if (buttonFont!=null) {
				button.setFont(buttonFont);
			}
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					JButton thisButton = (JButton)ev.getSource();
					String objectName = thisButton.getText();
					selectedObject = objectNameHash.get(objectName);
					closeDialog();
				}
			});
			button.setEnabled(enabled);
			if (tooltip!=null) {
				button.setToolTipText(tooltip);
			}
			JLabel label = new JLabel();
			buttons.put(key,button);
			labels.put(key,label);

			Box box = Box.createHorizontalBox();
			box.add(Box.createHorizontalStrut(10));
			box.add(button);
			box.add(Box.createHorizontalStrut(10));
			box.add(Box.createHorizontalGlue());
			box.add(label);

			buttonBoxes.add(box);
			objectNameHash.put(string,object);
			
			updateButtonSizes();
		}
		else {
			throw new IllegalStateException("You cannot add two objects with the same toString result!");
		}
	}
	public void setSelectionObjectIcon(Object object,Icon icon) {
		if (icon==null) return;
		String key = object.toString();
		JLabel label = labels.get(key);
		label.setIcon(icon);
		maxIconHeight = Math.max(maxIconHeight,icon.getIconHeight());
		updateButtonSizes();
	}
	private void buildButtonPanels() {
		int total = buttonBoxes.size();
		int rows = total/columns;
		int rem = total%columns;
		int remRows = rem==0?0:1;
		
		int b=0;
		JPanel buttonPanel = new JPanel(new GridLayout(1,columns));
		for(int i=0;i<columns;i++) {
			JPanel panel = new JPanel(new GridLayout(rows+remRows,1));
			int thisRows = rows + (rem==0?0:1);
			for(int n=0;n<thisRows;n++) {
				panel.add(buttonBoxes.get(b++));
			}
			if (rem==0 && remRows==1) {
				panel.add(Box.createGlue());
			}
			if (rem>0) rem--;
			buttonPanel.add(panel);
		}
		questionPanel.add(buttonPanel,BorderLayout.CENTER);
	}
	public void setVisible(boolean val) {
		buildButtonPanels();
		if (val && !finished) {
			finished = true;
		}
		super.pack();
		updateButtonSizes();
		setSize(getPreferredSize());
		
		super.setVisible(val);
	}
	private void updateButtonSizes() {
		int maxW = 0;
		int maxH = maxIconHeight;
		for (JButton button:buttons.values()) {
			Dimension size = BasicGraphicsUtils.getPreferredButtonSize(button,5);
			maxW = Math.max(maxW,size.width);
			maxH = Math.max(maxH,size.height);
		}
		for (JButton button:buttons.values()) {
			ComponentTools.lockComponentSize(button,maxW,maxH);
		}
	}
	private void closeDialog() {
		setVisible(false);
		dispose();
	}
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		
		// If the message is too long, it messes up the packing...
		String message = "This is a really long test, to see how the buttons are handled in this situation.  You see, if the title is TOO big, the buttons fall off the bottom of the dialog, and you have to resize it to see them!!!";
		ButtonOptionDialog dialog = new ButtonOptionDialog(new JFrame(),null,message,"title",false);
		dialog.addSelectionObject("Magic Sight");
		IconGroup group = new IconGroup(IconGroup.HORIZONTAL,2);
		group.addIcon(IconFactory.findIcon("icons/arrow2.gif"));
		group.addIcon(IconFactory.findIcon("icons/arrow4.gif"));
		group.addIcon(IconFactory.findIcon("icons/arrow6.gif"));
		group.addIcon(IconFactory.findIcon("icons/arrow8.gif"));
		//group.addIcon(IconFactory.findIcon("images/spoo.gif"));
		dialog.setSelectionObjectIcon("Magic Sight",group);
		dialog.addSelectionObject("Loot the Enchanted Meadow\n(2 left)");
		//JOptionPane.showMessageDialog(new JFrame(),dialog.questionPanel);
		dialog.setVisible(true);
		
		message = "foo";
		dialog = new ButtonOptionDialog(new JFrame(),null,message,"title",false);
		dialog.addSelectionObject("Magic Sight");
		dialog.addSelectionObject("Loot the Enchanted Meadow\n(2 left)");
		dialog.setVisible(true);
		System.out.println(dialog.getSelectedObject());
		
		System.exit(0);
	}
	/**
	 * @param buttonFont the buttonFont to set
	 */
	public void setButtonFont(Font buttonFont) {
		this.buttonFont = buttonFont;
	}
}