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
package com.robin.magic_realm.RealmQuestBuilder;

import java.util.ArrayList;

import javax.swing.JComponent;

public class QuestPropertyBlock {

	public enum FieldType {
		Boolean, 
		ChitType, 
		GameObjectWrapperSelector, 
		NoSpacesTextLine,
		Number, 
		Regex,
		SmartTextLine,
		SmartTextArea,
		StringSelector, 
		TextArea, 
		TextLine,
		CompanionSelector,
	}

	private String keyName;
	private String fieldName;
	private FieldType fieldType;
	private Object[] selections;
	private String[] keyVals;
	
	private JComponent component;
	
	public QuestPropertyBlock(String keyName, String fieldName, FieldType fieldType) {
		this(keyName, fieldName, fieldType, null);
	}

	public QuestPropertyBlock(String keyName, String fieldName, FieldType fieldType, Object[] selections) {
		this(keyName, fieldName, fieldType, selections, null);
	}

	public QuestPropertyBlock(String keyName, String fieldName, FieldType fieldType, Object[] selections, String[] keyVals) {
		this.keyName = keyName;
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.selections = selections;
		this.keyVals = keyVals;
	}

	public String getKeyName() {
		return keyName;
	}

	public String getFieldName() {
		return fieldName;
	}

	public FieldType getFieldType() {
		return fieldType;
	}

	public Object[] getSelections() {
		return selections;
	}
	public ArrayList<String> getSelectionsAsStrings() {
		ArrayList<String> list = new ArrayList<String>();
		if (selections!=null) {
			for (Object val:selections) {
				list.add(val.toString());
			}
		}
		return list;
	}
	
	public String[] getKeyVals() {
		return keyVals;
	}

	public JComponent getComponent() {
		return component;
	}

	public void setComponent(JComponent component) {
		this.component = component;
	}
}