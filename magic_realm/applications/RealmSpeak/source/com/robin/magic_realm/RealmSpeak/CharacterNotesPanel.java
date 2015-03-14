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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import com.robin.magic_realm.components.attribute.Note;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class CharacterNotesPanel extends CharacterFramePanel {
	
	private NotesTableModel tableModel;
	private JTable table;
	private JButton addNoteButton;
	private JButton deleteNoteButton;

	public CharacterNotesPanel(CharacterFrame parent) {
		super(parent);
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		tableModel = new NotesTableModel(getCharacter());
		table = new JTable(tableModel);
		table.setRowHeight(50);
		tableModel.updateColumns(table);
		table.setDefaultRenderer(String.class,new NotesCellRenderer());
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				updateControls();
			}
		});
		
		add(new JScrollPane(table),"Center");
		
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
			addNoteButton = new JButton("Add Custom");
			addNoteButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					String note = JOptionPane.showInputDialog("Custom Note");
					getCharacter().addNote(getGameHandler().getClient().getClientName(),"Custom",note);
					updatePanel();
				}
			});
		box.add(addNoteButton);
		box.add(Box.createHorizontalGlue());
			deleteNoteButton = new JButton("Delete Custom");
			deleteNoteButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					Note note = getSelectedNote();
					if (note!=null) {
						getCharacter().deleteNote(note.getId());
						updatePanel();
					}
				}
			});
		box.add(deleteNoteButton);
		box.add(Box.createHorizontalGlue());
		add(box,"South");
		
		updateControls();
	}
	
	private Note getSelectedNote() {
		return tableModel.getNote(table.getSelectedRow());
	}
	
	private void updateControls() {
		Note note = tableModel.getNote(table.getSelectedRow());
		deleteNoteButton.setEnabled(note!=null && note.isCustom());
	}

	public void updatePanel() {
		tableModel.updateNotes();
		tableModel.fireTableDataChanged();
	}

	private static class NotesTableModel extends AbstractTableModel {
		private final String[] header = {" ","Source","Date","Event","Note"};
		private CharacterWrapper character;
		private ArrayList<Note> notes;
		public NotesTableModel(CharacterWrapper character) {
			this.character = character;
			updateNotes();
		}
		public void updateNotes() {
			notes = character.getNotes();
		}
		public int getRowCount() {
			return notes.size();
		}
		public Note getNote(int row) {
			if (row>=0 && row<notes.size()) {
				return notes.get(row);
			}
			return null;
		}
		public int getColumnCount() {
			return header.length;
		}
		public String getColumnName(int column) {
			return header[column];
		}
		public Class getColumnClass(int column) {
			return column==0?ImageIcon.class:String.class;
		}
		public Object getValueAt(int row, int column) {
			if (row<notes.size()) {
				Note note = notes.get(row);
				switch(column) {
					case 0:		return note.getIcon();
					case 1:		return note.getSourceName();
					case 2:		return note.getDate();
					case 3:		return note.getEvent();
					case 4:		return note.getNote();
				}
			}
			return null;
		}
		public void updateColumns(JTable table) {
			table.getColumnModel().getColumn(0).setMaxWidth(60);
			table.getColumnModel().getColumn(1).setMaxWidth(200);
			table.getColumnModel().getColumn(2).setMaxWidth(50);
			table.getColumnModel().getColumn(3).setMaxWidth(100);
		}
	}
	private static final Border border = BorderFactory.createEmptyBorder(0,5,0,0);
	private static final Font font = UIManager.getFont("Table.font");
	private static final Color background = UIManager.getColor("Table.background");
	private static final Color foreground = UIManager.getColor("Table.foreground");
	private static final Color selBackground = UIManager.getColor("Table.selectionBackground");
	private static final Color selForeground = UIManager.getColor("Table.selectionForeground");
	private class NotesCellRenderer extends JTextArea implements TableCellRenderer {
		public NotesCellRenderer() {
			setEditable(false);
			setLineWrap(true);
			setWrapStyleWord(true);
			setBorder(border);
		}
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			setFont(font);
			setBackground(isSelected?selBackground:background);
			setForeground(isSelected?selForeground:foreground);
			Rectangle rect = table.getCellRect(row,column,true);
			setSize(rect.width,rect.height);
			setText(value!=null?value.toString():"");
			return this;
		}
 	}
}