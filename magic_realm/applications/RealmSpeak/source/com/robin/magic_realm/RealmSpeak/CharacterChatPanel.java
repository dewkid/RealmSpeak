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
package com.robin.magic_realm.RealmSpeak;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.text.*;

import com.robin.magic_realm.components.attribute.ChatLine;
import com.robin.magic_realm.components.attribute.ChatStyle;

public class CharacterChatPanel extends CharacterFramePanel {
	
	private static ArrayList<CharacterChatPanel> allChatPanels = new ArrayList<CharacterChatPanel>();
	public static void updateAllChatPanels(ChatLine line) {
		if (allChatPanels==null)return;
		for (CharacterChatPanel panel:allChatPanels) {
			panel.addChatLine(line);
		}
	}
	
	private JTextPane chatPane;
	private JTextField chatField;
	private StyledDocument chatDoc;
	private ArrayList<ChatLine> list;
	
	public CharacterChatPanel(CharacterFrame parent) {
		super(parent);
		list = new ArrayList<ChatLine>();
		initComponents();
		allChatPanels.add(this);
	}
	protected void cleanup() {
		allChatPanels.remove(this);
	}
	private void initComponents() {
		setLayout(new BorderLayout());
		
		chatPane = new JTextPane();
		chatPane.setEditable(false);
		add(new JScrollPane(chatPane),BorderLayout.CENTER);
		
		JPanel bottom = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Chat:");
		label.setFont(new Font("Dialog",Font.BOLD,14));
		bottom.add(label,BorderLayout.WEST);
		chatField = new JTextField();
		chatField.setFont(new Font("Dialog",Font.PLAIN,14));
		chatField.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				int code = e.getKeyCode();
				if ((code>=KeyEvent.VK_A && code<=KeyEvent.VK_Z)
						|| (code>=KeyEvent.VK_0 && code<=KeyEvent.VK_9)) {
					e.consume();
				}
				else if (code==KeyEvent.VK_ENTER) {
					sendChat();
					e.consume();
				}
			}
			public void keyReleased(KeyEvent e) {
			}
			public void keyTyped(KeyEvent e) {
			}
		});
		bottom.add(chatField,BorderLayout.CENTER);
		
		add(bottom,BorderLayout.SOUTH);
		initStyles();
	}
	private void initStyles() {
		chatDoc = chatPane.getStyledDocument();
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		Style regular = chatDoc.addStyle("regular", def);
		StyleConstants.setFontFamily(def, "SansSerif");
		StyleConstants.setFontSize(def,12);
		
		Style s;
		for (ChatStyle style:ChatStyle.styles) {
			s = chatDoc.addStyle(ChatLine.BOLD_PREFIX+style.getStyleName(),regular);
			StyleConstants.setForeground(s,style.getColor());
			StyleConstants.setBold(s, true);
			
			s = chatDoc.addStyle(style.getStyleName(),regular);
			StyleConstants.setForeground(s,style.getColor());
		}
	}
	public void clearLog() {
		chatPane.setText("");
		chatDoc = chatPane.getStyledDocument();
		list.clear();
	}
	private void sendChat() {
		getGameHandler().broadcastChat(getCharacter(),chatField.getText());
		chatField.setText("");
	}
	
	private void addChatLine(ChatLine line) {
		list.add(line);
		try {
			chatDoc.insertString(chatDoc.getLength(),line.getHeader(),chatDoc.getStyle(line.getHeaderStyleName()));
			chatDoc.insertString(chatDoc.getLength(), " - " + line.getText() + "\n", chatDoc.getStyle(line.getTextStyleName()));

			scrollToEnd();
		}
		catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	public void scrollToEnd() {
		// This makes it thread safe
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				_scrollToEnd();
			}
		});
	}
	private void _scrollToEnd() {
		chatPane.scrollRectToVisible(new Rectangle(0, chatPane.getHeight() - 2, 1, 1));
	}
	public void updatePanel() {
		// Nothing to do here
	}
}