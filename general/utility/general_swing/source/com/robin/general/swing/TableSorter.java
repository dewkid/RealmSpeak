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

/*
 * @(#)TableSorter.java	1.5 97/12/17
 *
 * Copyright (c) 1997 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 *
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 */

/**
 * A sorter for TableModels. The sorter has a model (conforming to TableModel)
 * and itself implements TableModel. TableSorter does not store or copy
 * the data in the TableModel, instead it maintains an array of
 * integers which it keeps the same size as the number of rows in its
 * model. When the model changes it notifies the sorter that something
 * has changed eg. "rowsAdded" so that its internal array of integers
 * can be reallocated. As requests are made of the sorter (like
 * getValueAt(row, col) it redirects them to its model via the mapping
 * array. That way the TableSorter appears to hold another copy of the table
 * with the rows in a different order. The sorting algorthm used is stable
 * which means that it does not move around rows when its comparison
 * function returns 0 to denote that they are equivalent.
 *
 * @version 1.5 12/17/97
 * @author Philip Milne
 */

import java.util.*;

import java.awt.Graphics;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.event.TableModelEvent;

// Imports for picking up mouse events from the JTable.

import java.awt.Color;
import java.awt.Font;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;
import javax.swing.*;
import javax.swing.SwingConstants;
import javax.swing.border.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.robin.general.graphics.GraphicsUtil;

public class TableSorter extends TableMap
{
	protected int indexes[];
	protected Vector sortingColumns = new Vector();
	protected int compares;
	protected JTable theTable;
	protected MouseAdapter listMouseListener;
	private HeaderMark lit_up = new HeaderMark(true);
	private HeaderMark lit_dn = new HeaderMark(true);
	private HeaderMark unlit = new HeaderMark();

    public TableSorter()
    {
		indexes = new int[0]; // For consistency.
		init();
    }

    public TableSorter(TableModel model)
    {
        setModel(model);
        init();
    }

    public void init() {
		lit_up.setIcon(new ArrowGraphic(ArrowGraphic.UP));
		lit_dn.setIcon(new ArrowGraphic(ArrowGraphic.DOWN));
    }

    public void setModel(TableModel model) {
        super.setModel(model);
        reallocateIndexes();
    }

    public int convertRowIndexToModel(int index) {
        if (index >= 0 && index < indexes.length) {
            index = indexes[index];
        }
        if (model instanceof TableSorter) {
            index = ((TableSorter) model).convertRowIndexToModel(index);
        }
        return index;
    }

    public int convertRowIndexToView(int index) {
        if (index >= 0 && index < indexes.length) {
        	for (int i=0;i<indexes.length;i++) {
        		if (indexes[i]==index) {
        			index = i;
        			break;
        		}
        	}
        }
        if (model instanceof TableSorter) {
            index = ((TableSorter) model).convertRowIndexToView(index);
        }
        return index;
    }

    public int compareRowsByColumn(int row1, int row2, int column)
    {
        TableModel data = model;

        // Check for nulls

        Object o1 = data.getValueAt(row1, column);
        Object o2 = data.getValueAt(row2, column);

        // If both values are null return 0
        if (o1 == null && o2 == null) {
            return 0;
        }
        else if (o1 == null) { // Define null less than everything.
            return -1;
        }
        else if (o2 == null) {
            return 1;
        }
        Class type = model.getColumnClass(column);
/*		if (type == Object.class && o1.getClass() == o2.getClass()) {
			if (o1 instanceof Integer) type=Integer.class;
			else if (o1 instanceof String) type=String.class;
			else if (o1 instanceof Double) type=Double.class;
			else if (o1 instanceof java.util.Date) type=java.util.Date.class;
			else if (o1 instanceof Boolean) type=Boolean.class;
			else if (o1 instanceof Timestamp) type=Timestamp.class;
		}*/
                if ((o1.getClass() == java.util.Date.class && o2.getClass() == String.class) ||
                        (o2.getClass() == java.util.Date.class && o1.getClass() == String.class)) {
                        return compareDateAndStringObjects(o1,o2);
                } else if (o1.getClass() == java.util.Date.class && o2.getClass() == java.util.Date.class) {
                        return compareObjectsByType(o1,o2,java.util.Date.class);
                } else if (type!=o1.getClass() || o1.getClass()!=o2.getClass()) {
			return compareObjectsByType(o1.toString(),o2.toString(),String.class);
		}
        return compareObjectsByType(o1, o2, type);
	}

    public int compareDateAndStringObjects(Object o1, Object o2) {
        if (o1.getClass() == java.util.Date.class) return 1;
        else if (o1.getClass() == String.class) return -1;
        else return 0;
    }

    public int compareObjectsByType(Object o1, Object o2, Class type)
    {
/* We copy all returned values from the getValue call in case
an optimised model is reusing one object to return many values.
The Number subclasses in the JDK are immutable and so will not be used in
this way but other subclasses of Number might want to do this to save
space and avoid unnecessary heap allocation.
*/

		if (type == String.class) {
            //Make sure strings are compared ignoring case, even though they are comparable
			String s1 = (String)o1;
			String s2 = (String)o2;
			int result = s1.compareToIgnoreCase(s2);
			if (result<0) return -1;
			else if (result>0) return 1;
			else return 0;
		} else if (o1 instanceof Comparable) {
            	int result;
                try {
             		result=((Comparable)o1).compareTo(o2);
                } catch (ClassCastException e){
                    //If, by chance, o2 is not comparable, then use toString comparison
                    result = o1.toString().compareToIgnoreCase(o2.toString());
                }
            	if (result<0) return -1;
            	else if (result>0) return 1;
            	else return 0;
        }
		else if (type == Boolean.class) {
            //Special case: Boolean is non-Comparable class
			boolean b1 = ((Boolean)o1).booleanValue();
			boolean b2 = ((Boolean)o2).booleanValue();
			if (b1 == b2) return 0;
			else if (b1) return 1; // Define false < true
			else return -1;
		}
        else {
				int result = o1.toString().compareToIgnoreCase(o2.toString());
				if (result<0) return -1;
				else if (result>0) return 1;
				else return 0;
            }
		}

    public int compare(int row1, int row2)
    {
        compares++;
        for(int level = 0; level < sortingColumns.size(); level++)
            {
				ColumnSort cs = (ColumnSort)sortingColumns.elementAt(level);
                int result = compareRowsByColumn(row1, row2, cs.column);
                if (result != 0) {
                    return cs.ascending ? result : -result;
                }
            }
        return 0;
    }

	public void  reallocateIndexes()
	{
		int rowCount = model.getRowCount();

		// Set up a new array of indexes with the right number of elements
		// for the new data model.
		indexes = new int[rowCount];

		// Initialise with the identity mapping.
		for(int row = 0; row < rowCount; row++) {
			indexes[row] = row;
		}
	}

    public void tableChanged(TableModelEvent e) {
		int type = e.getType();
		switch(type) {
			case TableModelEvent.UPDATE:
				break;
			case TableModelEvent.INSERT:
			case TableModelEvent.DELETE:
		        reallocateIndexes();
				break;
		}
       	unmarkAllColumns();
        super.tableChanged(e);
    }

    public void checkModel()
    {
        if (indexes.length != model.getRowCount() || sortingColumns.size()==0) {
        	// Reallocate if there is an inconsistency or no sorting parameters
        	reallocateIndexes();
        }
    }

    public void sort(Object sender)
    {
        checkModel();

        compares = 0;
        // n2sort();
        // qsort(0, indexes.length-1);
        shuttlesort((int[])indexes.clone(), indexes, 0, indexes.length);
        //System.out.println("Compares: "+compares);
    }

    public void n2sort() {
        for(int i = 0; i < getRowCount(); i++) {
            for(int j = i+1; j < getRowCount(); j++) {
                if (compare(indexes[i], indexes[j]) == -1) {
                    swap(i, j);
                }
            }
        }
    }

    // This is a home-grown implementation which we have not had time
    // to research - it may perform poorly in some circumstances. It
    // requires twice the space of an in-place algorithm and makes
    // NlogN assigments shuttling the values between the two
    // arrays. The number of compares appears to vary between N-1 and
    // NlogN depending on the initial order but the main reason for
    // using it here is that, unlike qsort, it is stable.
    public void shuttlesort(int from[], int to[], int low, int high) {
        if (high - low < 2) {
            return;
        }
        int middle = (low + high)/2;
        shuttlesort(to, from, low, middle);
        shuttlesort(to, from, middle, high);

        int p = low;
        int q = middle;

        /* This is an optional short-cut; at each recursive call,
        check to see if the elements in this subset are already
        ordered.  If so, no further comparisons are needed; the
        sub-array can just be copied.  The array must be copied rather
        than assigned otherwise sister calls in the recursion might
        get out of sinc.  When the number of elements is three they
        are partitioned so that the first set, [low, mid), has one
        element and and the second, [mid, high), has two. We skip the
        optimisation when the number of elements is three or less as
        the first compare in the normal merge will produce the same
        sequence of steps. This optimisation seems to be worthwhile
        for partially ordered lists but some analysis is needed to
        find out how the performance drops to Nlog(N) as the initial
        order diminishes - it may drop very quickly.  */

        if (high - low >= 4 && compare(from[middle-1], from[middle]) <= 0) {
            for (int i = low; i < high; i++) {
                to[i] = from[i];
            }
            return;
        }

        // A normal merge.

        for(int i = low; i < high; i++) {
            if (q >= high || (p < middle && compare(from[p], from[q]) <= 0)) {
                to[i] = from[p++];
            }
            else {
                to[i] = from[q++];
            }
        }
    }

    public void swap(int i, int j) {
        int tmp = indexes[i];
        indexes[i] = indexes[j];
        indexes[j] = tmp;
    }

    // The mapping only affects the contents of the data rows.
    // Pass all requests to these rows through the mapping array: "indexes".

    public Object getValueAt(int aRow, int aColumn)
    {
		checkModel();
		if (aRow<indexes.length) {
			return model.getValueAt(indexes[aRow], aColumn);
		}
		return null;
    }
    public Object[] getRowAt(int aRow) {
    	checkModel();
        if (aRow<indexes.length) {
        	int cols = getColumnCount();
        	Object[] values = new Object[cols];
        	for (int c=0;c<cols;c++) {
        		values[c] = model.getValueAt(indexes[aRow],c);
        	}
        	return values;
        }
        return null;
    }

    public void setValueAt(Object aValue, int aRow, int aColumn)
    {
		checkModel();
		if (aRow<indexes.length) {
			model.setValueAt(aValue, indexes[aRow], aColumn);
		}
    }
    public void setRowAt(Object[] values,int aRow) {
    	checkModel();
        if (aRow<indexes.length) {
        	int cols = getColumnCount();
        	if (values.length==cols) {
        		for (int c=0;c<cols;c++) {
        			model.setValueAt(values[c],indexes[aRow],c);
        		}
        	}
    	}
    }

    public ColumnSort getMatchingColumnSort(ColumnSort cs) {
    	for (int i=0;i<sortingColumns.size();i++) {
    		ColumnSort ocs = (ColumnSort)sortingColumns.elementAt(i);
    		if (ocs.equals(cs)) {
    			return ocs;
    		}
    	}
    	return null;
    }

    public synchronized void sortByColumn(int column,boolean ascending) {
    	sortByColumn(new ColumnSort(column,ascending));
    }

    public synchronized void sortByColumn(ColumnSort cs) {
    	ColumnSort ocs = getMatchingColumnSort(cs);
    	if (ocs!=null) {
    		ocs.toggle();
    	}
    	else {
	        unmarkAllColumns();
	        sortingColumns.removeAllElements();
	        sortingColumns.addElement(cs);
		}
		reSort();
    }

    public synchronized void sortByAdditionalColumn(ColumnSort cs) {
    	// First verify that this "new" column isn't already on the sort agenda
    	ColumnSort ocs = getMatchingColumnSort(cs);
    	if (ocs!=null) {
    		sortingColumns.removeElement(ocs);
    	}
    	else {
    		// Fine, then go ahead and add it
	        sortingColumns.addElement(cs);
    	}

    	// in any event, resort
        reSort();
    }

    private void markColumn(ColumnSort cs) {
    	if (theTable!=null && cs.getColumn()<getColumnCount()) {
	    	TableColumn tc = theTable.getTableHeader().getColumnModel().getColumn(theTable.convertColumnIndexToView(cs.getColumn()));
	    	tc.setHeaderRenderer(cs.isAscending()?lit_dn:lit_up);
	    }
    }

	private void unmarkColumn(ColumnSort cs) {
		if (theTable!=null && cs.getColumn()<getColumnCount()) {
			TableColumn tc = theTable.getTableHeader().getColumnModel().getColumn(theTable.convertColumnIndexToView(cs.getColumn()));
	    	tc.setHeaderRenderer(unlit);
		}
	}

	public synchronized void reset() {
		if (theTable!=null) {
        	sortingColumns.removeAllElements();
			unmarkAllColumns();
		}
	}

	private void unmarkAllColumns() {
		if (theTable!=null) {
			for (int i=0;i<getColumnCount();i++) {
				unmarkColumn(new ColumnSort(i));
			}
			theTable.getTableHeader().repaint();
		}
	}

	private void remarkColumns() {
		if (theTable!=null) {
			unmarkAllColumns();
			for (int i=0;i<sortingColumns.size();i++) {
				ColumnSort mcs = (ColumnSort)sortingColumns.elementAt(i);
		        markColumn(mcs);
			}
			theTable.getTableHeader().repaint();
		}
	}

	public void reSort() {
		sort(this);
		fireTableDataChanged();
		remarkColumns();
	}

	// fire overrides
	public void fireTableCellUpdated(int row, int column) {
		unmarkAllColumns();
		super.fireTableCellUpdated(row,column);
	}
	public void fireTableChanged(TableModelEvent e) {
		unmarkAllColumns();
		super.fireTableChanged(e);
	}
	public void fireTableDataChanged() {
		unmarkAllColumns();
		super.fireTableDataChanged();
	}
	public void fireTableRowsDeleted(int firstRow, int lastRow) {
		unmarkAllColumns();
		super.fireTableRowsDeleted(firstRow,lastRow);
	}
	public void fireTableRowsInserted(int firstRow, int lastRow) {
		unmarkAllColumns();
		super.fireTableRowsInserted(firstRow,lastRow);
	}
	public void fireTableRowsUpdated(int firstRow, int lastRow) {
		unmarkAllColumns();
		super.fireTableRowsUpdated(firstRow,lastRow);
	}
	public void fireTableStructureChanged() {
		unmarkAllColumns();
		super.fireTableStructureChanged();
	}

        public void initSort() {
            final JTable tableView = theTable;
            tableView.setColumnSelectionAllowed(false);
            if (theTable.isEditing()) theTable.getCellEditor().stopCellEditing();
            tableView.getSelectionModel().clearSelection();
            TableColumnModel columnModel = tableView.getColumnModel();
            int x = 0;     // e.getX();
            int viewColumn = columnModel.getColumnIndexAtX(x);
            int column = tableView.convertColumnIndexToModel(viewColumn);
            if(column != -1 && getRowCount()>0) {
                    boolean ascending = true;
                    ColumnSort cs = new ColumnSort(column,ascending);
                    sortByColumn(cs);
            }
        }

	// There is nowhere else to put this.
	// Add a mouse listener to the Table to trigger a table sort
	// when a column heading is clicked in the JTable.
	public void addMouseListenerToHeaderInTable(JTable table) {
		theTable = table;
		final JTable tableView = table;
		tableView.setColumnSelectionAllowed(false);
		listMouseListener = new MouseAdapter() {
			// This has to be a clicked event instead of a pressed event, because
			// otherwise you can't resize the columns with selecting the header!
			public void mouseClicked(MouseEvent e) {
				if (theTable.isEditing()) theTable.getCellEditor().stopCellEditing();
				tableView.getSelectionModel().clearSelection();
				TableColumnModel columnModel = tableView.getColumnModel();
				int x = e.getX();
				int viewColumn = columnModel.getColumnIndexAtX(x);
				int column = tableView.convertColumnIndexToModel(viewColumn);
				if(column != -1 && getRowCount()>0) {
					int shiftPressed = e.getModifiers()&InputEvent.SHIFT_MASK;
					int ctrlPressed = e.getModifiers()&InputEvent.CTRL_MASK;
					boolean ascending = (shiftPressed == 0);
					ColumnSort cs = new ColumnSort(column,ascending);
					if (ctrlPressed!=0 && sortingColumns.size()>0) {
						sortByAdditionalColumn(cs);
					}
					else {
						sortByColumn(cs);
					}
				}
			}
		};
		JTableHeader th = tableView.getTableHeader();
		th.addMouseListener(listMouseListener);
	}

	public void removeMouseListenerToHeaderInTable(JTable table) {
		theTable=null;
		final JTable tableView = table;
		JTableHeader th = tableView.getTableHeader();
		th.removeMouseListener(listMouseListener);
	}

	// private classes
	private class ColumnSort {
		public int column;
		public boolean ascending;
		public ColumnSort(int column) {
			this(column,true);
		}
		public ColumnSort(int column,boolean ascending) {
			this.column = column;
			this.ascending = ascending;
		}
		public int getColumn() {
			return column;
		}
		public boolean isAscending() {
			return ascending;
		}
		public boolean equals(ColumnSort cs) {
			return (column==cs.getColumn());
		}
		public void toggle() {
			ascending = !ascending;
		}
	}
	private class ArrowGraphic extends ImageIcon {
		Color foreground;

		public static final int UP = 1;
		public static final int DOWN = 2;

		int width = 15;
		int height =15;
		int type;

//		public ArrowGraphic() {
//			this(DOWN);
//		}

		public ArrowGraphic(int type) {
			this.type = type;
			init();
		}

		public void init() {
			foreground = GraphicsUtil.convertColor(UIManager.getColor("Table.selectionBackground"),Color.black,20);
		}

		public int getIconWidth() {
			return width;
		}

		public int getIconHeight() {
			return height;
		}

		public void paintIcon(Component c,Graphics g,int x,int y) {
			int aw = width-4;
			g.setColor(foreground);
			if (type==DOWN) {
				// draw down arrow
				for (int i=5;i<=height-5;i++) {
					g.drawLine(i,i,i+aw,i);
					aw -= 2;
				}
			}
			else {
				// draw up arrow
				for (int i=5;i<=height-5;i++) {
					g.drawLine(i,height-1-i,i+aw,height-1-i);
					aw -= 2;
				}
			}
		}
	}

	private class HeaderMark extends DefaultTableCellRenderer {
		public Color cellBorderColor = null;
		public Color markedBackground = UIManager.getColor("Table.selectionBackground");
		public Color headerForeground = UIManager.getColor("TableHeader.foreground");
		public Color headerBackground = UIManager.getColor("TableHeader.background");
		public Font font = UIManager.getFont("TableHeader.font");

		public HeaderMark() {
			cellBorderColor = headerBackground;
		}

		public HeaderMark(boolean marked) {
			if (marked) {
				cellBorderColor = markedBackground;
			}
			else {
				cellBorderColor = headerBackground;
			}
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int col) {
			setText((String)value);
			setHorizontalAlignment(SwingConstants.CENTER);
			setHorizontalTextPosition(SwingConstants.CENTER);
			setFont(font);
			setForeground(headerForeground);
			setBorder(new BevelBorder(BevelBorder.RAISED,
					GraphicsUtil.convertColor(cellBorderColor,Color.white,50),
					GraphicsUtil.convertColor(cellBorderColor,Color.black,50)));
			setOpaque(true);
			setBackground(cellBorderColor);
			return this;
		}
	}

	public static void makeSortable(JTable table) {
		TableSorter ts = new TableSorter(table.getModel());
		table.setModel(ts);
		ts.addMouseListenerToHeaderInTable(table);
	}

	/**
	 * Retrieves the non-TableSorter model
	 */
	public static TableModel getBaseModel(JTable table) {
		int overload = 0;
		TableModel model = table.getModel();
		while(model!=null && model instanceof TableSorter) {
			model = ((TableSorter)model).getModel();
			if (overload++ > 20) {
				// only 20 iterations allowed!
				throw new IllegalArgumentException("Too many iterations in TableSorter.getModel()!");
			}
		}
		return model;
	}

	public static TableSorter getSorter(JTable table) {
		TableModel model = table.getModel();
		if (model instanceof TableSorter) {
			return (TableSorter)model;
		}
		return null; // not a sortable table!
	}
}