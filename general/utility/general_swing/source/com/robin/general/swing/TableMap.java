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
 * @(#)TableMap.java	1.4 97/12/17
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
 * In a chain of data manipulators some behaviour is common. TableMap
 * provides most of this behavour and can be subclassed by filters
 * that only need to override a handful of specific methods. TableMap 
 * implements TableModel by routing all requests to its model, and
 * TableModelListener by routing all events to its listeners. Inserting 
 * a TableMap which has not been subclassed into a chain of table filters 
 * should have no effect.
 *
 * @version 1.4 12/17/97
 * @author Philip Milne */

import javax.swing.table.*; 
import javax.swing.event.TableModelListener; 
import javax.swing.event.TableModelEvent; 

public class TableMap extends AbstractTableModel implements TableModelListener
{
    protected TableModel model; 

    public TableModel  getModel() {
        return model;
    }

    public void  setModel(TableModel model) {
        this.model = model; 
        model.addTableModelListener(this); 
    }

    // By default, Implement TableModel by forwarding all messages 
    // to the model. 

    public Object getValueAt(int aRow, int aColumn) {
        return model.getValueAt(aRow, aColumn); 
    }
	
    public void setValueAt(Object aValue, int aRow, int aColumn) {
        model.setValueAt(aValue, aRow, aColumn); 
    }

    public int getRowCount() {
        return (model == null) ? 0 : model.getRowCount(); 
    }

    public int getColumnCount() {
        return (model == null) ? 0 : model.getColumnCount(); 
    }
	
    public String getColumnName(int aColumn) {
        return model.getColumnName(aColumn); 
    }

    public Class getColumnClass(int aColumn) {
        return model.getColumnClass(aColumn); 
    }
	
    public boolean isCellEditable(int row, int column) { 
         return model.isCellEditable(row, column); 
    }

	/**
	 * Returns the matrix of cell values at the coordinates specified by arrays of
	 * row and column indices. Note that while the returned matrix is contiguous,
	 * the rows and columns from which the values are taken do not have to be.<p>
	 *
	 * In general, if v is the returned matrix, <code>v[i][j]</code> = cell value at 
	 * <code>(rows[i], cols[j])</code>.<p>
	 *
	 * @return 			Matrix of values from the specified cells.
	 * @param rows		Array of row indices.
	 * @param cols		Array of column indices.
	 *
	 * @exception ArrayIndexOutOfBoundsException		
	 *		If any of the array indices are out of bounds.
	 *
	 * @exception NullPointerException
	 *		If either <code>rows</code> or <code>cols</code> is null.
	 */
	public Object[][] getValuesAt(int[] rows, int[] cols) {
		Object[][] values = new Object[rows.length][cols.length];
		for (int i = 0; i < rows.length; i++) {
			for (int j = 0; j < cols.length; j++) {
				values[i][j] = getValueAt(rows[i], cols[j]);
			}
		}	
		return values;
	}

	public void setValuesAt(Object[][] values, int[] rows, int[] cols) {
		int vRows = values.length;
		int vCols = values[0].length;
		for (int i = 0; i < rows.length; i++) {
			for (int j = 0; j < cols.length; j++) {
				setValueAt(values[i%vRows][j%vCols],rows[i], cols[j]);
			}
		}	
	}

	public boolean isCellValueValid(int row, int col) {
		return true;
	}
    
//
// Implementation of the TableModelListener interface, 
//

    // By default forward all events to all the listeners. 
    public void tableChanged(TableModelEvent e) {
        fireTableChanged(e);
    }
}