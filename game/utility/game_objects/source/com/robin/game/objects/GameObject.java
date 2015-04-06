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
package com.robin.game.objects;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import org.jdom.*;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.robin.general.io.ModifyableObject;
import com.robin.general.util.OrderedHashtable;

public class GameObject extends ModifyableObject implements Serializable {

	public static final int NO_ID_ASSIGNED = -1;

	protected static XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

	private static final String THIS = "this";
	protected OrderedHashtable attributeBlocks; // Holds Hashtables linked by a type key
	protected GameObject heldBy; // Can only be held by one parent
	protected ArrayList hold; // All GameObjects contained by this object

	protected GameObject uncommitted; // a skeleton game object to manage uncommitted changes (tracks attributes AND hold)

	protected long id;
	protected long version;
	protected String name;
	protected boolean needHoldResolved;

	protected GameData parent;
	
	private GameObject() {
		this(null, NO_ID_ASSIGNED);
	}

	public GameObject(GameData parent) {
		this(parent, NO_ID_ASSIGNED);
	}

	public GameObject(GameData parentData, long assign_id) {
		parent = parentData;
		uncommitted = null;
		id = assign_id;
		version = 0;

		if (parent != null) { // FIXME I'm not sure I WANT this all the time!
			addChangeListener(parent.getModifyListener());
//			addChangeListener(new ChangeListener() {
//				public void stateChanged(ChangeEvent ev) {
//					parent.setModified(true);
//				}
//			});
		}
		attributeBlocks = new OrderedHashtable();
		heldBy = null;
		hold = new ArrayList();
		reset();
		revertNameToDefault();
		setModified(true);
	}
	
	public GameObject getGameObjectFromThisAttribute(String key) {
		return getGameObjectFromAttribute(THIS,key);
	}
	
	public GameObject getGameObjectFromAttribute(String blockName,String key) {
		return getGameObjectFromId(getAttribute(blockName,key));
	}
	
	public GameObject getGameObjectFromId(String id) {
		return id==null?null:getGameData().getGameObject(Long.valueOf(id));
	}
	
	/**
	 * Removes the object from whichever object is currently holding it (if any).  If none, then this function does nothing.
	 */
	public void detach() {
		GameObject heldBy = getHeldBy();
		if (heldBy!=null) {
			heldBy.remove(this);
		}
	}
	
	/**
	 * This deletes the game object completely from the GameData structure.
	 */
	public void delete() {
		detach();
		getGameData().removeObject(this);
	}

	private void startUncommitted() {
		// Builds the uncommitted object to look just like this object in every respect
		uncommitted = new GameObject();
		uncommitted.id = this.id;
		uncommitted.setName(name); // so that keyval searches with name actually work!!
		uncommitted._setVersion(version);
		for (Iterator i = attributeBlocks.keySet().iterator(); i.hasNext();) {
			String blockName = (String) i.next();
			OrderedHashtable attributes = (OrderedHashtable) attributeBlocks.get(blockName);
			for (Iterator j = attributes.keySet().iterator(); j.hasNext();) {
				String attributeKey = (String) j.next();
				Object value = attributes.get(attributeKey);
				if (value instanceof String) {
					uncommitted.getAttributeBlock(blockName).put(attributeKey, value);
				}
				else {
					uncommitted.getAttributeBlock(blockName).put(attributeKey, new ArrayList((Collection) value));
				}
			}
		}
		uncommitted.heldBy = heldBy;
		uncommitted.hold.addAll(hold);
		
		uncommitted.copyChangeListeners(this);
	}

	public void rollback() {
		stopUncommitted();
	}

	protected void stopUncommitted() {
		if (uncommitted!=null) {
			changeListeners.clear();
		}
		uncommitted = null;
	}

	/**
	 * Add a GameObject (doesn't protect from circular references!)
	 */
	public void add(GameObject obj) {
		if (needHoldResolved) {
			throw new IllegalArgumentException("Cannot add object:  needHoldResolved is true");
		}
		if (parent!=null && parent.getDataId() != obj.parent.getDataId()) {
			throw new IllegalArgumentException("Cannot add object:  non-matching parent data!");
		}
		if (obj==this) {
			throw new IllegalArgumentException("GameObjects cannot hold themselves!  Circular containment!");
		}
		if (parent != null && parent.isTracksChanges()) {
			if (uncommitted == null) {
				startUncommitted();
			}
			if (obj.getHeldBy() != null) {
				obj.getHeldBy().remove(obj);
			}
			obj.setHeldBy(this);
			uncommitted.hold.add(obj);
			uncommitted.setModified(true);
			GameHoldAddChange change = new GameHoldAddChange(this);
			change.setHoldId(obj.getId());
			parent.addChange(change);
		}
		else {
			_add(obj);
		}
	}

	/**
	 * Add a GameObject (doesn't protect from circular references!)
	 */
	public void _add(GameObject obj) {
		stopUncommitted();
		if (needHoldResolved) {
			throw new IllegalArgumentException("Cannot add object:  needHoldResolved is true");
		}
		if (parent!=null && parent.getDataId() != obj.parent.getDataId()) {
			throw new IllegalArgumentException("Cannot add object:  non-matching parent data!");
		}
		if (obj.heldBy != null) {
			obj.heldBy._remove(obj);
		}
		obj.heldBy = this;
		hold.add(obj);
		setModified(true);
		version++;
	}

	/**
	 * Add all GameObjects in the collection
	 */
	public void addAll(Collection c) {
		if (needHoldResolved) {
			throw new IllegalArgumentException("Cannot add object:  needHoldResolved is true");
		}
		ArrayList list = new ArrayList(c);
		for (Iterator i = list.iterator(); i.hasNext();) {
			GameObject obj = (GameObject) i.next();
			add(obj);
		}
	}

	/**
	 * @return			A list of GameObjectChange objects required to build this object from scratch.  There are only
	 * 					add attributes, add attribute lists, and add
	 */
	public ArrayList<GameObjectChange> buildChanges() {
		if (uncommitted != null) {
			throw new IllegalStateException("Cannot buildChanges with uncommitted changes");
		}
		ArrayList<GameObjectChange> changes = new ArrayList<GameObjectChange>();
		for (Iterator i = getAttributeBlockNames().iterator(); i.hasNext();) {
			String blockName = (String) i.next();
			OrderedHashtable block = getAttributeBlock(blockName);
			for (Iterator k = block.keySet().iterator(); k.hasNext();) {
				String attributeName = (String) k.next();
				Object value = block.get(attributeName);
				if (value instanceof String) {
					GameAttributeChange change = new GameAttributeChange(this);
					change.setAttribute(blockName, attributeName, (String) value);
					changes.add(change);
				}
				else {
					GameAttributeListChange change = new GameAttributeListChange(this);
					change.setAttributeList(blockName, attributeName, new ArrayList(), (ArrayList) value);
					changes.add(change);
				}
			}
		}

		for (Iterator i = hold.iterator(); i.hasNext();) {
			GameObject obj = (GameObject) i.next();
			GameHoldAddChange change = new GameHoldAddChange(this);
			change.setHoldId(obj.getId());
			changes.add(change);
		}

		return changes;
	}

	/**
	 * @return			A list of GameObjectChange objects required to make this game object look exactly like the other
	 */
	public ArrayList buildChanges(GameObject other) {
		if (uncommitted != null || other.uncommitted != null) {
			throw new IllegalStateException("Cannot buildChanges with uncommitted changes");
		}
		/*
		 * Assumptions:
		 * 		1)  Each game object with the same id in each data object has the same blockNames
		 */
		ArrayList changes = new ArrayList();
		for (Iterator i = getAttributeBlockNames().iterator(); i.hasNext();) {
			String blockName = (String) i.next();
			OrderedHashtable block = getAttributeBlock(blockName);
			OrderedHashtable otherBlock = other.getAttributeBlock(blockName);
			if (otherBlock != null) {
				// check for changed and deleted attributes
				for (Iterator k = block.keySet().iterator(); k.hasNext();) {
					String attributeName = (String) k.next();
					Object value = block.get(attributeName);
					Object otherValue = otherBlock.get(attributeName);
					if (otherValue != null) {
						boolean valueIsString = value instanceof String;
						boolean otherIsString = otherValue instanceof String;
						if (valueIsString == otherIsString) {
							// both objects are the same type (either String or ArrayList);
							if (otherIsString) {
								if (!value.equals(otherValue)) {
									GameAttributeChange action = new GameAttributeChange(other);
									action.setAttribute(blockName, attributeName, (String) otherValue);
									changes.add(action);
								}
							}
							else {
								GameAttributeListChange action = new GameAttributeListChange(other);
								action.setAttributeList(blockName, attributeName, (ArrayList) value, (ArrayList) otherValue);
								if (action.hasChange()) {
									changes.add(action);
								}
							}
						}
						else {
							// both objects are different types.  Defer to other
							if (otherIsString) {
								GameAttributeChange action = new GameAttributeChange(other);
								action.setAttribute(blockName, attributeName, (String) otherValue);
								changes.add(action);
							}
							else {
								GameAttributeListChange action = new GameAttributeListChange(other);
								action.setAttributeList(blockName, attributeName, null, (ArrayList) otherValue);
								changes.add(action);
							}
						}
					}
					else {
						// doesn't matter what type the value is (String or ArrayList), its been deleted
						GameAttributeChange action = new GameAttributeChange(other);
						action.deleteAttribute(blockName, attributeName);
						changes.add(action);
					}
				}
				// check for added attributes
				for (Iterator k = otherBlock.keySet().iterator(); k.hasNext();) {
					String attributeName = (String) k.next();
					if (!block.containsKey(attributeName)) {
						Object val = otherBlock.get(attributeName);
						if (val instanceof String) {
							GameAttributeChange action = new GameAttributeChange(other);
							action.setAttribute(blockName, attributeName, (String) val);
							changes.add(action);
						}
						else {
							GameAttributeListChange action = new GameAttributeListChange(other);
							action.setAttributeList(blockName, attributeName, null, (ArrayList) val);
							changes.add(action);
						}
					}
				}
			}
		}

		// Check for new blocks
		for (Iterator i = other.getAttributeBlockNames().iterator(); i.hasNext();) {
			String blockName = (String) i.next();
			if (!hasAttributeBlock(blockName)) {
				OrderedHashtable otherBlock = other.getAttributeBlock(blockName);
				for (Iterator k = otherBlock.keySet().iterator(); k.hasNext();) {
					String attributeName = (String) k.next();
					Object value = otherBlock.get(attributeName);
					if (value instanceof String) {
						GameAttributeChange action = new GameAttributeChange(other);
						action.setAttribute(blockName, attributeName, (String) value);
						changes.add(action);
					}
					else if (value instanceof ArrayList) {
						GameAttributeListChange action = new GameAttributeListChange(other);
						action.setAttributeList(blockName, attributeName, new ArrayList(), (ArrayList) value);
						changes.add(action);
					}
				}
			}
		}

		// Search for hold additions
		for (Iterator i = other.hold.iterator(); i.hasNext();) {
			GameObject go = (GameObject) i.next();
			if (!hold.contains(go)) {
				GameHoldAddChange action = new GameHoldAddChange(other);
				action.setHoldId(go.getId());
				changes.add(action);
			}
		}

		// Search for hold removals
		for (Iterator i = hold.iterator(); i.hasNext();) {
			GameObject go = (GameObject) i.next();
			if (!other.hold.contains(go)) {
				GameHoldRemoveChange action = new GameHoldRemoveChange(other);
				action.setHoldId(go.getId());
				changes.add(action);
			}
		}

		return changes;
	}

	/**
	 * Clear out this objects hold
	 */
	public void clearHold() {
		if (needHoldResolved) {
			throw new IllegalArgumentException("Cannot remove object:  needHoldResolved is true");
		}
		GameObject[] toClear = (GameObject[]) hold.toArray(new GameObject[hold.size()]);
		for (int i = 0; i < toClear.length; i++) {
			remove(toClear[i]);
		}
	}
	
	public GameObject copy() {
		return getGameData().createNewObject(this);
	}

	/**
	 * Make a minimal copy of name and attributes only (not hold and id)
	 */
	public void copyAttributesFrom(GameObject obj) {
		reset();
		setName(new String(obj.getName()));
		// Need to do a deep copy here!
		for (Iterator i = obj.getAttributeBlocks().orderedKeys().iterator(); i.hasNext();) {
			String blockName = (String) i.next();
			OrderedHashtable block = (OrderedHashtable) obj.getAttributeBlocks().get(blockName);
			for (Iterator v = block.orderedKeys().iterator(); v.hasNext();) {
				String key = (String) v.next();
				Object val = block.get(key);
				if (val instanceof ArrayList) {
					setAttributeList(blockName, key, (ArrayList)val);
				}
				else {
					setAttribute(blockName, key, (String)val);
				}
			}
		}
	}

	/**
	 * Makes a FULL copy (Except for ID)
	 */
	public void copyFrom(GameObject obj) {
		copyAttributesFrom(obj);
		needHoldResolved = true;
		for (Iterator i = obj.getHold().iterator(); i.hasNext();) {
			GameObject held = (GameObject) i.next();
			hold.add(new Long(held.getId()));
		}
	}

	/**
	 * Create a new attribute block (ie., side_1, side_2, this)
	 */
	private OrderedHashtable createAttributeBlock(String blockName) {
		if (!hasAttributeBlock(blockName)) {
			attributeBlocks.put(blockName, new OrderedHashtable());
		}
		return (OrderedHashtable) attributeBlocks.get(blockName);
	}

	/**
	 * Two game objects are equal if they share the same id and the same parent
	 */
	public boolean equals(Object o1) {
		if (o1 instanceof GameObject) {
			GameObject go = (GameObject) o1;
			return (id == go.getId());
		}
		return false;
	}

	public boolean equalsId(long num) {
		return (num == id);
	}

	public boolean equalsId(Long num) {
		return (num.longValue() == id);
	}
	
	public boolean allAttributesMatch(GameObject go) {
		return _allAttributesMatch(go) && go._allAttributesMatch(this);
	}
	private boolean _allAttributesMatch(GameObject go) {
		for (Iterator i=getAttributeBlockNames().iterator();i.hasNext();) {
			String attributeBlock = (String)i.next();
			if (!go.hasAttributeBlock(attributeBlock)) {
				return false;
			}
			OrderedHashtable block = getAttributeBlock(attributeBlock);
			for (Iterator n=block.keySet().iterator();n.hasNext();) {
				String key = (String)n.next();
				String val = (String)block.get(key);
				if (!go.hasAttribute(attributeBlock,key)) {
					return false;
				}
				if (!val.equals(go.getAttribute(attributeBlock,key))) {
					return false;
				}
			}
		}
		return true;
	}

	public int getAttributeInt(String blockName, String key) {
		// don't catch the exception here: I want it!
		return Integer.valueOf(getAttribute(blockName, key)).intValue();
	}

	/**
	 * Get an attribute
	 */
	public String getAttribute(String blockName, String key) {
		if (uncommitted != null) {
			return uncommitted.getAttribute(blockName, key);
		}
		if (attributeBlocks.containsKey(blockName)) {
			OrderedHashtable attributeBlock = (OrderedHashtable) attributeBlocks.get(blockName);
			return (String) attributeBlock.get(key.toLowerCase());
		}
		return null;
	}
	
	public Object getObject(String blockName,String key) {
		if (uncommitted != null) {
			return uncommitted.getObject(blockName, key);
		}
		if (attributeBlocks.containsKey(blockName)) {
			OrderedHashtable attributeBlock = (OrderedHashtable) attributeBlocks.get(blockName);
			return attributeBlock.get(key.toLowerCase());
		}
		return null;
	}

	public OrderedHashtable getAttributeBlock(String blockName) {
		if (uncommitted != null) {
			return uncommitted.getAttributeBlock(blockName);
		}
		return createAttributeBlock(blockName);
	}

	public int getAttributeBlockCount() {
		if (uncommitted != null) {
			return uncommitted.getAttributeBlockCount();
		}
		return attributeBlocks.size();
	}

	public Collection getAttributeBlockNames() {
		if (uncommitted != null) {
			return uncommitted.getAttributeBlockNames();
		}
		return attributeBlocks.keySet();
	}

	public OrderedHashtable getAttributeBlocks() {
		if (uncommitted != null) {
			return uncommitted.getAttributeBlocks();
		}
		return attributeBlocks;
	}

	protected Element getAttributeBlockXML(String blockName) {
		if (uncommitted != null) {
			throw new IllegalStateException("Cannot generate XML for uncommitted object");
		}
		Element element = null;
		OrderedHashtable attributeBlock = (OrderedHashtable) attributeBlocks.get(blockName);
		if (attributeBlock != null) {
			element = new Element("AttributeBlock");
			element.setAttribute(new Attribute("blockName", blockName));
			// Read attributes (in order)
			for (int i = 0; i < attributeBlock.size(); i++) {
				String key = (String) attributeBlock.getKey(i);
				Object val = attributeBlock.getValue(i);

				if (val instanceof String) { // attribute
					Element attributeElement = new Element("attribute");
					//try {
						attributeElement.setAttribute(key, val.toString());
					//}
					//catch(IllegalNameException ex) {
					//	System.out.println(fineDetail());
					//	throw ex;
					//}
					element.addContent(attributeElement);
				}
				else if (val instanceof Collection) { // attributeList
					Collection c = (Collection) val;
					Element attributeListElement = new Element("attributeList");
					attributeListElement.setAttribute("keyName", key);
					int n = 0;
					for (Iterator v = c.iterator(); v.hasNext();) {
						String listVal = v.next().toString();
						Element attributeElement = new Element("attributeVal");
						attributeElement.setAttribute("N" + String.valueOf(n++), listVal);
						attributeListElement.addContent(attributeElement);
					}
					element.addContent(attributeListElement);
				}
			}
		}
		return element;
	}

	public ArrayList getAttributeList(String blockName, String key) {
		if (uncommitted != null) {
			return uncommitted.getAttributeList(blockName, key);
		}
		if (attributeBlocks.containsKey(blockName)) {
			OrderedHashtable attributeBlock = (OrderedHashtable) attributeBlocks.get(blockName);
			Object obj = attributeBlock.get(key.toLowerCase());
			if (obj==null || obj instanceof ArrayList) {
				return (ArrayList)obj;
			}
			else{
				throw new IllegalArgumentException("Found string instead of list for '"+blockName+"' and '"+key+"' in GameObject "+getName());
			}
		}
		return null;
	}

	public Element getContainsXML() {
		Element element = new Element("contains");
		element.setAttribute(new Attribute("id", "" + id));
		return element;
	}

	public String getFullTitle() {
		return (parent == null ? "dummy" : parent.getGameName()) + ":  " + name;
	}

	public GameData getGameData() {
		return parent;
	}

	public GameObject getHeldBy() {
		if (uncommitted != null) {
			return uncommitted.heldBy;
		}
		return heldBy;
	}

	public ArrayList getHold() {
		if (uncommitted != null) {
			return uncommitted.hold;
		}
		return hold;
	}
	
	public ArrayList<GameObject> getHoldAsGameObjects(){
		ArrayList<GameObject> result = new ArrayList<GameObject>();
		ArrayList toCopy = new ArrayList();
		
		if (uncommitted != null) {
			toCopy = uncommitted.hold;
		} else {
			toCopy = hold;
		}

		for(Iterator i=toCopy.iterator();i.hasNext();){
			result.add((GameObject)i.next());
		}
		
		return result;
	}

	public int getHoldCount() {
		return getHold().size();
	}

	public long getId() {
		return id;
	}

	/**
	 * Convenience method for reading int attributes
	 */
	public int getInt(String blockName, String key) throws NumberFormatException {
		String val = getAttribute(blockName, key);
		if (val != null && val.trim().length()>0) {
			return Integer.valueOf(val).intValue();
		}
		return 0;
	}
	/**
	 * Convenience method for reading integer attributes
	 */
	public Integer getInteger(String blockName, String key) throws NumberFormatException {
		String val = getAttribute(blockName, key);
		if (val != null && val.trim().length()>0) {
			return Integer.valueOf(val);
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public String getStringId() {
		return String.valueOf(id);
	}

	public String getThisAttribute(String key) {
		return getAttribute(THIS, key);
	}

	public OrderedHashtable getThisAttributeBlock() {
		return getAttributeBlock(THIS);
	}

	public ArrayList getThisAttributeList(String key) {
		return getAttributeList(THIS, key);
	}

	public int getThisInt(String key) throws NumberFormatException {
		return getInt(THIS, key);
	}

	public String fineDetail() {
		StringBuffer sb = new StringBuffer();
		sb.append(name+"["+id+"]\n");
		sb.append("\theldBy="+heldBy+"\n");
		sb.append("\tholds="+hold+"\n");
		for (Iterator i=getAttributeBlockNames().iterator();i.hasNext();) {
			String block = (String)i.next();
			sb.append("\t"+block+"\n");
			Hashtable hash = getAttributeBlock(block);
			for (Iterator n=hash.keySet().iterator();n.hasNext();) {
				String key = (String)n.next();
				Object val = hash.get(key);
				sb.append("\t\t"+key+"="+val+"\n");
			}
		}
		return sb.toString();
	}
	public void _outputDetail() {
		System.out.println("------CURRENT----------\n"+fineDetail());
		if (uncommitted!=null) {
			System.out.println("-----UNCOMMITTED-------\n"+uncommitted.fineDetail());
		}
		if (parent!=null) {
			System.out.println("----CHANGES-----\n"+parent.getChangeCount()+": "+parent.getObjectChanges());
		}
	}
	public Element getXML() {
		if (uncommitted != null) {
//			System.out.println("------CURRENT----------\n"+fineDetail());
//			System.out.println("-----UNCOMMITTED-------\n"+uncommitted.fineDetail());
//			System.out.println("----CHANGES-----\n"+parent.getObjectChanges());
//			throw new IllegalStateException("Cannot generate XML for uncommitted object: "+toString());
			return uncommitted.getXML();
		}
		Element element = new Element("GameObject");
		element.setAttribute(new Attribute("id", "" + id));
		element.setAttribute(new Attribute("name", name));
		// Gather attribute block info (in order!)
		for (int i = 0; i < attributeBlocks.size(); i++) {
			String blockName = (String) attributeBlocks.getKey(i);
			element.addContent(getAttributeBlockXML(blockName));
		}
		// Gather contains info
		for (Iterator i = hold.iterator(); i.hasNext();) {
			GameObject go = (GameObject) i.next();
			element.addContent(go.getContainsXML());
		}

		// return
		return element;
	}

	public String getXMLString() {
		return outputter.outputString(getXML());
	}

	//	/**
	//	 * Searches ALL attribute blocks for all keys in the collection.
	//	 */
	/*	public boolean hasAllKeys(Collection keys) {
	 ArrayList lowercasedKeys = new ArrayList();
	 for (Iterator i=keys.iterator();i.hasNext();) {
	 String key = (String)i.next();
	 lowercasedKeys.add(key.toLowerCase());
	 }
	 HashSet set = new HashSet();
	 for (Enumeration e=attributeBlocks.keys();e.hasMoreElements();) {
	 String blockName = (String)e.nextElement();
	 OrderedHashtable attributeBlock = (OrderedHashtable)attributeBlocks.get(blockName);
	 set.addAll(attributeBlock.keySet());
	 }
	 return set.containsAll(lowercasedKeys);
	 }*/

	public boolean hasAllKeyVals(String query) {
		return hasAllKeyVals(GamePool.makeKeyVals(query));
	}
	/**
	 * Searches ALL attribute blocks for all keys (and optionally values) in the collection.
	 * The format for a keyVal is "key=val", and a key alone is simply "key".  Will also exclude
	 * items from the list based on negative keys like "!key=val" and "!key".
	 */
	public boolean hasAllKeyVals(Collection keyVals) {
		if (uncommitted != null) {
			return uncommitted.hasAllKeyVals(keyVals);
		}
		// First, cleanup the keys and keyVals
		ArrayList fixedKeyVals = new ArrayList();
		ArrayList fixedNegativeKeyVals = new ArrayList();
		for (Iterator i = keyVals.iterator(); i.hasNext();) {
			String string = (String) i.next();
			StringTokenizer tokens = new StringTokenizer(string, "=");
			if (tokens.countTokens() == 1) {
				String key = tokens.nextToken().trim().toLowerCase();
				if (key.startsWith("!")) {
					fixedNegativeKeyVals.add(key);
				}
				else {
					fixedKeyVals.add(key);
				}
			}
			else if (tokens.countTokens() == 2) {
				String key = tokens.nextToken().trim().toLowerCase();
				String val = tokens.nextToken().trim();
				if (key.startsWith("!")) {
					fixedNegativeKeyVals.add(key + "=" + val);
				}
				else {
					fixedKeyVals.add(key + "=" + val);
				}
			}
		}

		// Now collect prepped keyVals from the attributeBlocks
		HashSet attributes = new HashSet();
		attributes.add("name=" + name); // name is always one of the choices
		ArrayList absOrderedKeys = attributeBlocks.orderedKeys();
		for (int i=0;i<absOrderedKeys.size();i++) {
			String blockName = (String)absOrderedKeys.get(i);
			OrderedHashtable attributeBlock = (OrderedHashtable) attributeBlocks.get(blockName);
			ArrayList abOrderedKeys = attributeBlock.orderedKeys();
			for (int k=0;k<abOrderedKeys.size();k++) {
				String key = (String) abOrderedKeys.get(k);
				Object val = attributeBlock.get(key);

				if (val instanceof String) {
					// Add both types, in case user is querying on key alone
					attributes.add(key);
					if (val.toString().trim().length() > 0) {
						attributes.add(key + "=" + val);
					}
				}
				else if (val instanceof ArrayList) {
					// Add both types, in case user is querying on key alone
					attributes.add(key);
					ArrayList c = (ArrayList) val;
					for (int n=0;n<c.size();n++) {
						String stringVal = (String)c.get(n);
						if (stringVal.trim().length() > 0) {
							attributes.add(key + "=" + stringVal);
						}
					}
				}
			}
		}
		boolean hasAllPos = attributes.containsAll(fixedKeyVals);
		boolean hasNoNeg = true;
		for (Iterator i = fixedNegativeKeyVals.iterator(); i.hasNext();) {
			String keyVal = (String) i.next();
			if (attributes.contains(keyVal.substring(1))) {
				hasNoNeg = false;
				break;
			}
		}

		return (hasAllPos && hasNoNeg);
	}

	public boolean hasAttribute(String blockName, String key) {
		if (uncommitted != null) {
			return uncommitted.hasAttribute(blockName, key);
		}
		if (key!=null && hasAttributeBlock(blockName)) {
			return getAttributeBlock(blockName).containsKey(key.toLowerCase());
		}
		return false;
	}

	public boolean hasAttributeBlock(String blockName) {
		if (uncommitted != null) {
			return uncommitted.hasAttributeBlock(blockName);
		}
		return attributeBlocks.containsKey(blockName);
	}

	public int hashCode() {
		return name.length();
	}

	/**
	 * Searches ALL attribute blocks for key
	 */
	public boolean hasKey(String key) {
		if (uncommitted != null) {
			return uncommitted.hasKey(key);
		}
		key = key.toLowerCase();
		for (Enumeration e = attributeBlocks.keys(); e.hasMoreElements();) {
			String blockName = (String) e.nextElement();
			OrderedHashtable attributeBlock = (OrderedHashtable) attributeBlocks.get(blockName);
			if (attributeBlock.containsKey(key)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Search an attribute block for key
	 */
	public boolean hasKey(String blockName, String key) {
		if (uncommitted != null) {
			return uncommitted.hasKey(blockName, key);
		}
		key = key.toLowerCase();
		OrderedHashtable attributeBlock = (OrderedHashtable) attributeBlocks.get(blockName);
		return (attributeBlock != null && attributeBlock.containsKey(key));
	}

	public boolean hasThisAttribute(String key) {
		return hasAttribute(THIS, key);
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}

	/**
	 * Remove a GameObject (returns true on success)
	 */
	public boolean remove(GameObject obj) {
		if (needHoldResolved) {
			throw new IllegalArgumentException("Cannot remove object:  needHoldResolved is true");
		}
		if (parent.getDataId() != obj.parent.getDataId()) {
			throw new IllegalArgumentException("Cannot remove object:  non-matching parent data!");
		}
		if (parent != null && parent.isTracksChanges()) {
			if (obj.getHeldBy() == this) {
				if (uncommitted == null) {
					startUncommitted();
				}
				obj.setHeldBy(null);
				uncommitted.hold.remove(obj);
				uncommitted.setModified(true);
				GameHoldRemoveChange change = new GameHoldRemoveChange(this);
				change.setHoldId(obj.getId());
				parent.addChange(change);
				return true;
			}
		}
		else {
			return _remove(obj);
		}
		return false;
	}

	/**
	 * Remove a GameObject (returns true on success)
	 */
	public boolean _remove(GameObject obj) {
		stopUncommitted();
		if (needHoldResolved) {
			throw new IllegalArgumentException("Cannot remove object:  needHoldResolved is true");
		}
		if (parent.getDataId() != obj.parent.getDataId()) {
			throw new IllegalArgumentException("Cannot remove object:  non-matching parent data!");
		}
		if (obj.heldBy == this) {
			obj.heldBy = null;
			hold.remove(obj);
			setModified(true);
			version++;
			return true;
		}
		return false;
	}

	/**
	 * Delete's an entire attribute block
	 */
	public boolean removeAttributeBlock(String blockName) {
		if (parent != null && parent.isTracksChanges()) {
			GameObject test = uncommitted==null?this:uncommitted;
			if (test.hasAttributeBlock(blockName)) {
				if (uncommitted == null) {
					startUncommitted();
				}
				GameAttributeChange change = new GameAttributeChange(this);
				change.deleteAttributeBlock(blockName);
				parent.addChange(change);
				uncommitted._removeAttributeBlock(blockName);
				return true;
			}
		}
		else {
			return _removeAttributeBlock(blockName);
		}
		return false;
	}

	public boolean _removeAttributeBlock(String blockName) {
		stopUncommitted();
		if (attributeBlocks.containsKey(blockName)) {
			boolean ret = (attributeBlocks.remove(blockName) != null);
			if (ret) {
				setModified(true);
				version++;
			}
			return ret;
		}
		return false;
	}
	public void _renameAttributeBlock(String from,String to) {
		stopUncommitted();
		if (attributeBlocks.containsKey(from)) {
			if (!attributeBlocks.containsKey(to)) {
				OrderedHashtable block = (OrderedHashtable)attributeBlocks.remove(from);
				attributeBlocks.put(to,block);
				version++;
			}
			else {
				throw new IllegalArgumentException("There is already an attribute block named: "+to);
			}
		}
		else {
			throw new IllegalArgumentException("There is no attribute block named: "+from);
		}
	}
	public void renameAttributeBlock(String from,String to) {
		if (parent != null && parent.isTracksChanges()) {
			if (uncommitted == null) {
				startUncommitted();
			}
			GameAttributeBlockChange change = new GameAttributeBlockChange(this);
			change.rename(from,to);
			parent.addChange(change);
			uncommitted._renameAttributeBlock(from,to);
		}
		else {
			_renameAttributeBlock(from,to);
		}
	}
	public void _copyAttributeBlockFrom(GameObject source,String blockName) {
		stopUncommitted();
		if (source.hasAttributeBlock(blockName)) {
			OrderedHashtable block = source.getAttributeBlock(blockName);
			for (Iterator i=block.keySet().iterator();i.hasNext();) {
				String key = (String)i.next();
				Object val = block.get(key);
				if (val instanceof ArrayList) {
					ArrayList copy = new ArrayList((ArrayList)val);
					_setAttributeList(blockName,key,copy);
				}
				else {
					_setAttribute(blockName,key,(String)val);
				}
			}
		}
		else {
			throw new IllegalArgumentException(source+" does not have a block named "+blockName);
		}
	}
	
	public void copyAttributeBlock(String fromBlockName,String toBlockName) {
		// This doesn't use committed block or GameObjectChange - it just uses the base setAttribute and setAttributeList methods, so it will still get tracked
		OrderedHashtable block = getAttributeBlock(fromBlockName);
		for (Object ok:block.keySet()) {
			String key = (String)ok;
			Object val = block.get(key);
			if (val instanceof ArrayList) {
				ArrayList copy = new ArrayList((ArrayList)val);
				setAttributeList(toBlockName,key,copy);
			}
			else {
				setAttribute(toBlockName,key,(String)val);
			}
		}		
	}
	
	public void copyAttributeBlockFrom(GameObject source,String blockName) {
		if (parent != null && parent.isTracksChanges()) {
			if (uncommitted == null) {
				startUncommitted();
			}
			GameAttributeBlockChange change = new GameAttributeBlockChange(this);
			change.copyFrom(source,blockName);
			parent.addChange(change);
			uncommitted._copyAttributeBlockFrom(source,blockName);
		}
		else {
			_copyAttributeBlockFrom(source,blockName);
		}
	}

	/**
	 * Delete an attribute (returns true on success) - if parent is tracking changes, then the change is only
	 * final when the data is committed
	 */
	public boolean removeAttribute(String blockName, String key) {
		if (parent != null && parent.isTracksChanges()) {
			GameObject test = uncommitted==null?this:uncommitted;
			if (test.hasAttribute(blockName, key)) {
				if (uncommitted == null) {
					startUncommitted();
				}
				GameAttributeChange change = new GameAttributeChange(this);
				change.deleteAttribute(blockName, key);
				parent.addChange(change);
				uncommitted._removeAttribute(blockName, key);
				return true;
			}
		}
		else {
			return _removeAttribute(blockName, key);
		}
		return false;
	}

	public boolean _removeAttribute(String blockName, String key) {
		stopUncommitted();
		if (attributeBlocks.containsKey(blockName)) {
			OrderedHashtable attributeBlock = (OrderedHashtable) attributeBlocks.get(blockName);
			boolean ret = (attributeBlock.remove(key.toLowerCase()) != null);
			if (ret) {
				setModified(true);
				version++;
			}
			return ret;
		}
		return false;
	}

	public boolean removeThisAttribute(String key) {
		return removeAttribute(THIS, key);
	}

	public void reset() {
		attributeBlocks.clear();
		hold.clear();
		needHoldResolved = false;
	}

//	/**
//	 * Provide this method with a collection of all objects to resolve the hold
//	 */
//	public void resolveHold(Collection allObjects) {
//		if (needHoldResolved) {
//			needHoldResolved = false;
//			// Fix hold
//			ArrayList numbers = hold;
//			hold = new ArrayList();
//			for (Iterator n = numbers.iterator(); n.hasNext();) {
//				Long number = (Long) n.next();
//				for (Iterator i = allObjects.iterator(); i.hasNext();) {
//					GameObject obj = (GameObject) i.next();
//					if (obj.equalsId(number)) {
//						add(obj);
//					}
//				}
//			}
//		}
//	}
	/**
	 * This should be faster than the way I was doing it (iterate through a collection)
	 */
	public void resolveHold(HashMap objectHash) {
		if (needHoldResolved) {
			needHoldResolved = false;
			// Fix hold
			ArrayList numbers = hold;
			hold = new ArrayList();
			for (Iterator n = numbers.iterator(); n.hasNext();) {
				Long number = (Long) n.next();
				GameObject obj = (GameObject)objectHash.get(number);
if (obj==null) {
	System.out.println("Error during resolveHold:  Cannot find: "+number);
}
				add(obj);
			}
		}
	}

	public void revertNameToDefault() {
		setName("GameObject"); // default name
	}

	public void setAttribute(String blockName, String key) {
		setAttribute(blockName, key, "");
	}

	public void setAttribute(String blockName, String key, int val) {
		setAttribute(blockName, key, String.valueOf(val));
	}

	/**
	 * Add a new attribute - if the parent is tracking changes, then the attribute isn't physically modified
	 * until the parent is committed
	 */
	public void setAttribute(String blockName, String key, String val) {
		if (parent != null && parent.isTracksChanges()) {
			GameAttributeChange change = new GameAttributeChange(this);
			change.setAttribute(blockName, key, val);
			parent.addChange(change);
			if (uncommitted == null) {
				startUncommitted();
			}
			uncommitted._setAttribute(blockName, key, val);
		}
		else {
			_setAttribute(blockName, key, val);
		}
	}

	/**
	 * Add a new attribute
	 */
	public void _setAttribute(String blockName, String key, String val) {
		stopUncommitted();
		OrderedHashtable attributes = createAttributeBlock(blockName);
		attributes.put(key.toLowerCase(), val);
		setModified(true);
		version++;
	}

	protected void setAttributeBlockXML(Element element) {
		String blockName = element.getAttribute("blockName").getValue();
		createAttributeBlock(blockName);
		// Do all normal attributes first
		Collection attributes = element.getChildren("attribute");
		for (Iterator i = attributes.iterator(); i.hasNext();) {
			Element attributeElement = (Element) i.next();
			Attribute attribute = (Attribute) attributeElement.getAttributes().iterator().next();
			String key = attribute.getName();
			String val = attribute.getValue();
			setAttribute(blockName, key, val);
		}
		// Then do the attribute lists
		Collection attributeLists = element.getChildren("attributeList");
		for (Iterator i = attributeLists.iterator(); i.hasNext();) {
			Element attributeListElement = (Element) i.next();
			String keyName = attributeListElement.getAttributeValue("keyName");
			Hashtable attributeHash = new Hashtable();
			Collection attributeVals = attributeListElement.getChildren("attributeVal");
			for (Iterator v = attributeVals.iterator(); v.hasNext();) {
				Element attributeElement = (Element) v.next();
				Attribute attribute = (Attribute) attributeElement.getAttributes().iterator().next();
				Integer num = Integer.valueOf(attribute.getName().substring(1));
				String val = attribute.getValue();
				attributeHash.put(num, val);
			}
			ArrayList nums = new ArrayList(attributeHash.keySet());
			Collections.sort(nums);
			ArrayList attributeList = new ArrayList();
			for (Iterator n = nums.iterator(); n.hasNext();) {
				Integer num = (Integer) n.next();
				attributeList.add(attributeHash.get(num));
			}
			setAttributeList(blockName, keyName, attributeList);
		}
	}

	public void setAttributeBoolean(String blockName, String key, boolean val) {
		if (val) {
			setAttribute(blockName, key);
		}
		else {
			removeAttribute(blockName, key);
		}
	}

	/**
	 * Add a new attribute list
	 */
	public void setAttributeList(String blockName, String key, ArrayList val) {
		if (parent != null && parent.isTracksChanges()) {
			GameAttributeListChange change = new GameAttributeListChange(this);
			change.setAttributeList(blockName, key, (ArrayList) getAttributeList(blockName, key), val);
			parent.addChange(change);
			if (uncommitted == null) {
				startUncommitted();
			}
			uncommitted.setAttributeList(blockName, key, val);
		}
		else {
			_setAttributeList(blockName, key, val);
		}
	}
	
	public boolean hasThisAttributeListItem(String key,String val) {
		return hasAttributeListItem(THIS,key,val);
	}

	public boolean hasAttributeListItem(String blockName, String key, String val) {
		if (uncommitted != null) {
			return uncommitted.hasAttributeListItem(blockName, key, val);
		}
		Collection c = getAttributeList(blockName, key);
		return (c != null && c.contains(val));
	}
	
	public void removeThisAttributeListItem(String key, String val) {
		removeAttributeListItem(THIS,key,val);
	}
	
	public void removeAttributeListItem(String blockName, String key, String val) {
		if (parent != null && parent.isTracksChanges()) {
			GameAttributeListChange change = new GameAttributeListChange(this);
			change.removeAttributeListItem(blockName, key, val);
			parent.addChange(change);
			if (uncommitted == null) {
				startUncommitted();
			}
			uncommitted._removeAttributeListItem(blockName, key, val);
		}
		else {
			_removeAttributeListItem(blockName, key, val);
		}
	}

	public void _removeAttributeListItem(String blockName, String key, String item) {
		stopUncommitted();
		ArrayList c = getAttributeList(blockName, key);
		if (c == null) {
			return;
		}
		if (item==null) {
			throw new IllegalArgumentException("list item cannot be null for blockName "+blockName+" and key "+key);
		}
		c.remove(item);

		setModified(true);
		version++;
	}
	
	public void addThisAttributeListItem(String key, String val) {
		addAttributeListItem(THIS, key, val);
	}

	public void addAttributeListItem(String blockName, String key, String val) {
		if (parent != null && parent.isTracksChanges()) {
			GameAttributeListChange change = new GameAttributeListChange(this);
			change.addAttributeListItem(blockName, key, val);
			parent.addChange(change);
			if (uncommitted == null) {
				startUncommitted();
			}
			uncommitted._addAttributeListItem(blockName, key, val);
		}
		else {
			_addAttributeListItem(blockName, key, val);
		}
	}

	public void _addAttributeListItem(String blockName, String key, String item) {
		stopUncommitted();
		ArrayList c = getAttributeList(blockName, key);
		if (c == null) {
			c = new ArrayList();
			_setAttributeList(blockName, key, c);
		}
		if (item==null) {
			throw new IllegalArgumentException("list item cannot be null for blockName "+blockName+" and key "+key);
		}
		c.add(item);

		setModified(true);
		version++;
	}

	public void _setAttributeList(String blockName, String key, ArrayList val) {
		stopUncommitted();
		OrderedHashtable attributes = createAttributeBlock(blockName);
		for (Iterator i=val.iterator();i.hasNext();) {
			if (i.next()==null) {
				throw new IllegalArgumentException("list items cannot be null for blockName "+blockName+" and key "+key);
			}
		}
		attributes.put(key.toLowerCase(), val);
		setModified(true);
		version++;
	}

	private void setHeldBy(GameObject obj) {
		if (parent != null && parent.isTracksChanges()) {
			if (uncommitted == null) {
				startUncommitted();
			}
			uncommitted.heldBy = obj;
			// 11/8/05 - The next two lines are new
			GameBumpVersionChange change = new GameBumpVersionChange(this); // just guarantees this object is updated
			parent.addChange(change);
		}
		else {
			heldBy = obj;
		}
	}

	/**
	 * This method should NOT be called by outside apps.  This method is here to provide
	 * GameData a way to renumber its objects.  If this method is used incorrectly, it could
	 * corrupt the data file!
	 */
	protected void setId(long newid) {
		this.id = newid;
	}

	public void setName(String val) {
		if (val != null && !val.equals(name)) {
			// Since name is changing, so is the way it should be hashed by its parent.  Fix that!
			if (parent!=null) {
				parent.changingName(name,val,this);
			}
			
			name = val;
			setModified(true);
		}
	}

	public void setNeedHoldResolved(boolean needHoldResolved) {
		this.needHoldResolved = needHoldResolved;
	}

	public void setThisAttribute(String key) {
		setAttribute(THIS, key);
	}

	public void setThisAttribute(String key, int val) {
		setAttribute(THIS, key, val);
	}

	public void setThisAttribute(String key, String val) {
		setAttribute(THIS, key, val);
	}

	public void setThisAttributeBoolean(String key, boolean val) {
		setAttributeBoolean(THIS, key, val);
	}

	public void setThisAttributeList(String key, ArrayList val) {
		setAttributeList(THIS, key, val);
	}

	public void setXML(Element element) {
		String sid = (String) element.getAttribute("id").getValue();
		try {
			Integer n = Integer.valueOf(sid);
			id = n.intValue();
			reset();
			revertNameToDefault();
			Attribute nameAtt = element.getAttribute("name");
			if (nameAtt != null) {
				setName((String) nameAtt.getValue());
			}

			// Retrieve attribute block info
			Collection blocks = element.getChildren("AttributeBlock");
			for (Iterator i = blocks.iterator(); i.hasNext();) {
				setAttributeBlockXML((Element) i.next());
			}

			// Retrieve contains info
			Collection contains = element.getChildren("contains");

			// for now, add ids to hold and set the flag to resolve
			for (Iterator i = contains.iterator(); i.hasNext();) {
				Element contain = (Element) i.next();
				String csid = (String) contain.getAttribute("id").getValue();
				hold.add(Long.valueOf(csid));
			}
			needHoldResolved = true;
			setModified(true);
		}
		catch (NumberFormatException ex) {
			reset();
			ex.printStackTrace();
		}
	}

	public String toString() {
		String string = (parent==null?"":(parent.getDataId()+":"))+"[" + id + "] " + name;
		if (parent != null && parent.isTracksChanges() && parent.getObjectChanges().size() > 0) {
			return string + " {Parent has " + parent.getObjectChanges().size() + " uncommitted changes}";
		}
		return string;
	}

	public long getVersion() {
		if (uncommitted != null) {
			return uncommitted.getVersion();
		}
		return version;
	}

	public void _setVersion(long ver) {
		stopUncommitted();
		this.version = ver;
	}

	public void bumpVersion() {
		if (parent != null && parent.isTracksChanges()) {
			GameBumpVersionChange change = new GameBumpVersionChange(this);
			parent.addChange(change);
			if (uncommitted == null) {
				startUncommitted();
			}
			uncommitted._bumpVersion();
		}
		else {
			_bumpVersion();
		}
	}

	public void _bumpVersion() {
		stopUncommitted();
		setModified(true);
		version++;
	}
	public void setThisKeyVals(String keyValString) {
		setKeyVals(THIS,keyValString);
	}
	public void setKeyVals(String blockName,String keyValString) {
		ArrayList<String> keyVals = GamePool.makeKeyVals(keyValString);
		for (String keyVal:keyVals) {
			StringTokenizer tokens = new StringTokenizer(keyVal, "=");
			if (tokens.countTokens() == 1) {
				String key = tokens.nextToken().trim().toLowerCase();
				if (!key.startsWith("!")) {
					setAttribute(blockName,key);
				}
			}
			else if (tokens.countTokens() == 2) {
				String key = tokens.nextToken().trim().toLowerCase();
				String val = tokens.nextToken().trim();
				setAttribute(blockName,key,val);
			}
		}
	}
	public void stripThisKeyVals(String keyValString) {
		stripKeyVals(THIS,keyValString);
	}
	public void stripKeyVals(String blockName,String keyValString) {
		ArrayList<String> keyVals = GamePool.makeKeyVals(keyValString);
		for (String keyVal:keyVals) {
			StringTokenizer tokens = new StringTokenizer(keyVal, "=");
			String key = tokens.nextToken().trim().toLowerCase();
			if (!key.startsWith("!")) {
//System.out.println("stripping "+blockName+","+key+" from "+name);
				removeAttribute(blockName,key);
			}
		}
	}

	// Serializable interface
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}
	
	public int getRelativeSize() {
		int count = 0;
		for (Iterator b=attributeBlocks.values().iterator();b.hasNext();) {
			OrderedHashtable oh = (OrderedHashtable)b.next();
			count+=oh.values().size()+1;
		}
		if (uncommitted!=null) {
			count += uncommitted.getRelativeSize();
		}
		return count;
	}
	
	/**
	 * This method is here to allow you to make a GameObject instance without having a data parent.  This is useful for testing,
	 * or in cases where a game object is expected, and you don't have one.
	 */
	public static GameObject createEmptyGameObject() {
		return new GameObject();
	}
	
	public static void setListKeyVals(String blockName,String keyVals,ArrayList<GameObject> list) {
		for (GameObject go:list) {
			go.setKeyVals(blockName,keyVals);
		}
	}
	public static void stripListKeyVals(String blockName,String keyVals,ArrayList<GameObject> list) {
		for (GameObject go:list) {
			go.stripKeyVals(blockName,keyVals);
		}
	}
	public boolean isModified() {
		if (uncommitted!=null) {
			return uncommitted.isModified();
		}
		return super.isModified();
	}
	/**
	 * Iterates through the hold, and the holds of each item in the hold, and so on, until every GameObject contained is returned.
	 */
	public ArrayList<GameObject> getAllContainedGameObjects() {
		ArrayList<GameObject> allQuestObjects = new ArrayList<GameObject>();
		ArrayList<GameObject> layer = new ArrayList<GameObject>();
		layer.add(this);
		while(layer.size()>0) {
			ArrayList<GameObject> nextLayer = new ArrayList<GameObject>();
			for(GameObject go:layer) {
				allQuestObjects.add(go);
				for(Iterator i=go.getHold().iterator();i.hasNext();) {
					GameObject held = (GameObject)i.next();
					if (!allQuestObjects.contains(held)) {
						nextLayer.add(held);
					}
				}
			}
			layer = nextLayer;
		}
		return allQuestObjects;
	}
	public void clearAllAttributes() {
		for(Object name:getAttributeBlockNames()) {
			removeAttributeBlock(name.toString());
		}
		setName(getName()+" - Erased");
	}
}

/*
 <GameObject id="628" name="Berserker Move H5*">
 <AttributeBlock blockName="this">
 <attribute character_chit="berserker" />
 <attribute action="move" />
 <attribute strength="H" />
 <attribute speed="5" />
 <attribute effort="1" />
 <attributeList keyName="actions">
 <attributeVal N1="move 123" />
 <attributeVal N2="hide" />
 <attributeVal N3="hide" />
 <attributeVal N4="hide" />
 </attributeList>
 <attribute character_level="1" />
 <attribute original_game="" />
 </AttributeBlock>
 </GameObject>

 */