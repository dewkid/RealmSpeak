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

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.robin.general.io.*;
import com.robin.general.util.*;

public class GameData extends ModifyableObject implements Serializable {
	
	private static long c_dataid = 0;
	private long dataid = c_dataid++;
	public long getDataId() {
		return dataid;
	}
	
	public boolean reportFormatErrors = true;
	public boolean ignoreRandomSeed = false;
	
	private String dataName = "defaultDataName"; // can use this variable (which is never saved in xml) to identify local instances of GameData
	
	private static final String ZIP_INTERNAL_FILENAME = "GameData_CHEATER_.xml";

	protected long cumulative_id = 0;
	
	protected long dataVersion = 0; // updated each time there is a real change?
	
	protected boolean filter = false;

	protected String gameName;
	protected String gameDesc;
	protected String filterString;
	protected ArrayList<GameObject> excludeList;
	protected ArrayList<GameObject> gameObjects;
	protected HashMap gameObjectIDHash;
	protected HashLists<String,GameObject> gameObjectNameHash;
	protected ArrayList<GameObject> filteredGameObjects;
	
	protected ArrayList gameSetups;
	
	protected boolean tracksChanges = false;
	private ArrayList<GameObjectChange> objectChanges;
	
	private ChangeListener modifyListener = new ChangeListener() {
		public void stateChanged(ChangeEvent ev) {
			setModified(true);
		}
	};
	
	public GameData() {
		this("SHOULDNT_SEE_THIS");
	}
	public GameData(String name) {
		filterString = null;
		excludeList = null;
		gameName = name;
		gameObjects = new ArrayList<GameObject>();
		gameObjectIDHash = new HashMap();
		gameObjectNameHash = new HashLists<String,GameObject>();
		filteredGameObjects = new ArrayList<GameObject>();
		gameSetups = new ArrayList();
		setModified(true);
	}
	public String getCheckSum() {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			for (Iterator i=gameObjects.iterator();i.hasNext();) {
				GameObject go = (GameObject)i.next();
				md.update(go.getName().getBytes());
				OrderedHashtable hash = go.getAttributeBlocks();
				ArrayList blocks = new ArrayList(hash.keySet());
				Collections.sort(blocks);
				for (Iterator b=blocks.iterator();b.hasNext();) {
					String blockName = (String)b.next();
					OrderedHashtable block = (OrderedHashtable)hash.get(blockName);
					ArrayList keys = new ArrayList(block.keySet());
					Collections.sort(keys);
					for (Iterator k=keys.iterator();k.hasNext();) {
						String key = (String)k.next();
						Object val = block.get(key);
						md.update(key.getBytes());
						md.update(val.toString().getBytes());
					}
				}
			}
			byte[] bytes = md.digest();
			StringBuffer sb=new StringBuffer();
			for (int i = 0; i < bytes.length; i++) {
				String hex=Integer.toHexString(0xff & bytes[i]);
				if(hex.length()==1) sb.append('0');
				sb.append(hex);
			}
			return sb.toString();
		}
		catch(NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	public void setFilter(boolean val) {
		filter = val;
	}
	public ChangeListener getModifyListener() {
		return modifyListener;
	}
	public GameData copy() {
		GameData data = new GameData(gameName);
		for (Iterator i=getGameObjects().iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			GameObject goCopy = data.createNewObject(go);
			goCopy.copyFrom(go);
		}
		for (Iterator i=data.getGameObjects().iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			go.resolveHold(data.getGameObjectIDHash());
		}
		return data;
	}
	public boolean hasChanges() {
		return tracksChanges && objectChanges!=null && !objectChanges.isEmpty();
	}
	public void setGameName(String val) {
		gameName = val;
	}
	public String getGameName() {
		return gameName;
	}
	public void setGameDescription(String val) {
		gameDesc = val;
		setModified(true);
	}
	public String getGameDescription() {
		return gameDesc;
	}
	public GameObject getGameObject(long id) {
		return getGameObject(new Long(id));
	}
	public GameObject getGameObject(Long id) {
		return (GameObject)gameObjectIDHash.get(id);
	}
	public ArrayList<GameObject> getGameObjects() {
		return gameObjects;
	}
	public HashMap getGameObjectIDHash() {
		return gameObjectIDHash;
	}
	public Set<String> getAllGameObjectNames() {
		return gameObjectNameHash.keySet();
	}
	public GameObject getGameObjectByNameIgnoreCase(String name) {
		for(String test:gameObjectNameHash.keySet()) {
			if (test.equalsIgnoreCase(name)) {
				return gameObjectNameHash.getList(test).get(0);
			}
		}
		return null;
	}
	public ArrayList<GameObject> getGameObjectsByNameIgnoreCase(String name) {
		ArrayList<GameObject> ret = new ArrayList<GameObject>();
		for(String test:gameObjectNameHash.keySet()) {
			if (test.equalsIgnoreCase(name)) {
				ret.addAll( gameObjectNameHash.getList(test));
			}
		}
		return ret;
	}
	public GameObject getGameObjectByName(String name) {
		ArrayList list = getGameObjectsByName(name);
		if (list.isEmpty()) {
			return null;
		}
		return (GameObject)list.get(0);
	}
	public ArrayList<GameObject> getGameObjectsByName(String name) {
		ArrayList<GameObject> ret = new ArrayList<GameObject>();
		ArrayList<GameObject> val = gameObjectNameHash.getList(name);
		if (val!=null) {
			ret.addAll(val);
		}
		return ret;
	}
	public void renumberObjectsByName() {
		Collections.sort(gameObjects,new Comparator() {
			public int compare(Object o1,Object o2) {
				GameObject g1 = (GameObject)o1;
				GameObject g2 = (GameObject)o2;
				return g1.getName().compareTo(g2.getName());
			}
		});
		renumberObjects();
	}
	public void moveObjectsBefore(ArrayList objects,GameObject indexObject) {
		moveObjects(objects,indexObject,true);
	}
	public void moveObjectsAfter(ArrayList objects,GameObject indexObject) {
		moveObjects(objects,indexObject,false);
	}
	/**
	 * Moves the objects to the position BEFORE the GameObject with an id==idPosition
	 */
	private void moveObjects(ArrayList objects,GameObject indexObject,boolean before) {
		// First, verify ALL objects are in the list, and that the list is uniqued
		ArrayList<GameObject> validObjects = new ArrayList<GameObject>();
		for (Iterator i=objects.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			if (go.parent==this && gameObjects.contains(go) && !validObjects.contains(go)) {
				validObjects.add(go);
			}
		}
		if (validObjects.size()!=objects.size()) {
			throw new IllegalStateException("Invalid object set to move!");
		}
		
		// Find the index of the specified id
		if (indexObject==null) {
			throw new IllegalStateException("Invalid indexObject!");
		}
		
		// Remove all valid objects
		gameObjects.removeAll(validObjects);
		
		int index = gameObjects.indexOf(indexObject);
		if (!before) index++;
		
		// Reinsert into specified position
		gameObjects.addAll(index,validObjects);
		
		// Renumber!
		renumberObjects();
	}
	/**
	 * Renumbers objects by their list order in gameObjects
	 */
	private void renumberObjects() {
		gameObjectIDHash.clear();
		cumulative_id = 0;
		for (Iterator i=gameObjects.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			go.setId(cumulative_id++);
			gameObjectIDHash.put(new Long(go.getId()),go);
		}
		
		rebuildFilteredGameObjects();
		setModified(true);
	}
	public long getCumulativeId() {
		return cumulative_id;
	}
	public long getMaxId() {
		long max = 0;
		for (Iterator i=gameObjectIDHash.keySet().iterator();i.hasNext();) {
			Long key = (Long)i.next();
			max = Math.max(key.longValue(),max);
		}
		return max;
	}
	public void renumberObjectsStartingWith(long startId) {
		gameObjectIDHash.clear();
		cumulative_id = startId;
		for (Iterator i=gameObjects.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			go.setId(cumulative_id++);
			gameObjectIDHash.put(new Long(go.getId()),go);
		}
		
		rebuildFilteredGameObjects();
		setModified(true);
	}
	public void clearFilterAndExcludeList() {
		filterString = null;
		excludeList = null;
		rebuildFilteredGameObjects();
		setModified(true);
	}
	public void setFilterString(String filter) {
		filterString = filter;
		rebuildFilteredGameObjects();
		setModified(true);
	}
	public String getFilterString() {
		return filterString;
	}
	public void clearExcludeList() {
		excludeList = null;
		rebuildFilteredGameObjects();
	}
	public void setExcludeList(GameObject object) {
		ArrayList<GameObject> exclude = new ArrayList<GameObject>();
		exclude.add(object);
		setExcludeList(exclude);
	}
	public void setExcludeList(ArrayList<GameObject> exclude) {
		excludeList = exclude;
		rebuildFilteredGameObjects();
	}
	public ArrayList<GameObject> getExcludeList() {
		return excludeList;
	}
	public void rebuildFilteredGameObjects() {
		if (filter) {
			// Rebuild collection
			filteredGameObjects.clear();
			
			if (filterString==null) {
				filteredGameObjects.addAll(gameObjects);
			}
			else {
				// Filter gameObjects
				ArrayList filterTerms = new ArrayList();
				StringTokenizer tokens = new StringTokenizer(filterString,",");
				while(tokens.hasMoreTokens()) {
					filterTerms.add(tokens.nextToken());
				}
				
				for (Iterator i=gameObjects.iterator();i.hasNext();) {
					GameObject obj = (GameObject)i.next();
					if (obj.hasAllKeyVals(filterTerms)) {
						// Conditions met - add it.
						filteredGameObjects.add(obj);
					}
				}
			}
			if (excludeList!=null) {
				// Remove exclude list
				filteredGameObjects.removeAll(excludeList);
			}
		}
	}
	public ArrayList<GameObject> getFilteredGameObjects() {
		return filter?filteredGameObjects:gameObjects;
	}
	public ArrayList getGameSetups() {
		return gameSetups;
	}
	public void resetIdToMax(Collection c) {
		cumulative_id = 0;
		for (Iterator i=c.iterator();i.hasNext();) {
			GameObject obj = (GameObject)i.next();
			if (obj.getId()>cumulative_id) {
				cumulative_id = obj.getId();
			}
		}
		cumulative_id++;
	}
	public boolean zipFromFile(File zipFile) {
		ZipUtilities.unzip(zipFile);
		String path = FileUtilities.getFilePathString(zipFile,false,false);
		File tempFile = new File(path+ZIP_INTERNAL_FILENAME);
		if (loadFromFile(tempFile)) {
			tempFile.delete();
			return true;
		}
		return false;
	}
	/**
	 * This allows you to load from a file compressed in a jar archive
	 */
	public boolean loadFromPath(String path) {
		path = fixFilePath(path);
		InputStream stream = ResourceFinder.getInputStream(path);
		if (stream!=null) {
			return loadFromStream(stream);
		}
		else {
			System.err.println("GameData unable to loadFromPath.  "+path+" not found?");
		}
		return false;
	}
	public boolean loadFromFile(File file) {
		file = fixFileExtension(file);
		try {
			InputStream stream = new FileInputStream(file);
			return loadFromStream(stream);
		}
		catch(FileNotFoundException ex) {
			System.out.println("Problem loading file: "+ex);
		}
		return false;
	}
	public boolean loadFromStream(InputStream stream) {
		try {
			// Load file
			Document doc = new SAXBuilder().build(stream);
			
			// Read game
			Element game = doc.getRootElement();
			setXML(game);
			return true;
		}
		catch(Exception ex) {
			if (reportFormatErrors) {
				JOptionPane.showMessageDialog(null,"Invalid file/format:\n\n"+ex,"Error",JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			}
		}
		return false;
	}
	public void setXML(Element game) {
		gameName = game.getAttribute("name").getValue();
		gameDesc = game.getAttribute("description").getValue();
		String seedString = game.getAttributeValue("_rseed");
		if (!ignoreRandomSeed && seedString!=null) {
			String rt = game.getAttributeValue("_rgtype");
			RandomNumber.setRandomNumberGenerator(rt==null ? RandomNumberType.System : RandomNumberType.valueOf(rt));
			String countString = game.getAttributeValue("_rcount");
			RandomNumber.init(Long.valueOf(seedString),Long.valueOf(countString));
		}
		
		// Read objects
		Collection objects = game.getChild("objects").getChildren();
		gameObjects.clear();
		gameObjectIDHash.clear();
		gameObjectNameHash.clear();
		for (Iterator i=objects.iterator();i.hasNext();) {
			Element obj = (Element)i.next();
			GameObject newObj = new GameObject(this);
			newObj.setXML(obj);
			gameObjects.add(newObj);
			gameObjectIDHash.put(new Long(newObj.getId()),newObj);
			gameObjectNameHash.put(newObj.getName(),newObj);
		}
		
		// Resolve objects (holds can't be calculated until all are loaded!)
		for (Iterator i=gameObjects.iterator();i.hasNext();) {
			GameObject obj = (GameObject)i.next();
			obj.resolveHold(gameObjectIDHash);
		}
		
		rebuildFilteredGameObjects();
		
		// Set cumulative_id to something real
		resetIdToMax(gameObjects);
		
		// Read setups
		Collection setups = game.getChild("setups").getChildren();
		gameSetups.clear();
		for (Iterator i=setups.iterator();i.hasNext();) {
			Element setup = (Element)i.next();
			GameSetup newSetup = new GameSetup(this);
			newSetup.setXML(setup);
			gameSetups.add(newSetup);
		}
		
		// Done.
		setModified(false);
	}
	public boolean zipToFile(File zipFile) {
		String path = FileUtilities.getFilePathString(zipFile,false,false);
		File tempFile = new File(path+ZIP_INTERNAL_FILENAME);
		
		if (saveToFile(tempFile)) {
			ArrayList<File> files = new ArrayList<File>();
			files.add(tempFile);
			ZipUtilities.zip(zipFile,files.toArray(new File[files.size()]));
			tempFile.delete();
			return true;
		}
		
		return false;
	}
	public boolean saveToFile(File file) {
		file = fixFileExtension(file);
		Element game = getXML();
		
		// Save file
		try {
			FileOutputStream stream = new FileOutputStream(file);
			XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
			outputter.output(game,stream);
			stream.close();
			setModified(false);
			return true;
		}
		catch(FileNotFoundException ex) {
			ex.printStackTrace();
		}
		catch(IOException ex) {
			if (reportFormatErrors) {
				ex.printStackTrace();
			}
		}
		return false;
	}
	public Element getXML() {
		// Build game
		Element game = new Element("game");
		game.setAttribute(new Attribute("file_version","1.0"));
		game.setAttribute(new Attribute("name",gameName));
		game.setAttribute(new Attribute("description",gameDesc==null?"":gameDesc));
		if (!ignoreRandomSeed && RandomNumber.hasBeenInitialized()) {
			game.setAttribute(new Attribute("_rseed",String.valueOf(RandomNumber.getSeed())));
			game.setAttribute(new Attribute("_rcount",String.valueOf(RandomNumber.getCount())));
			game.setAttribute(new Attribute("_rgtype",RandomNumber.getRandomNumberGenerator().toString()));
		}
		
		// Build objects
		Element objects = new Element("objects");
		for (Iterator i=gameObjects.iterator();i.hasNext();) {
			GameObject obj = (GameObject)i.next();
			objects.addContent(obj.getXML());
		}
		game.addContent(objects);
		
		// Build setup
		Element setups = new Element("setups");
		for (Iterator i=gameSetups.iterator();i.hasNext();) {
			GameSetup setup = (GameSetup)i.next();
			setups.addContent(setup.getXML());
		}
		game.addContent(setups);
		
		return game;
	}
	private File fixFileExtension(File file) {
		return new File(file.getPath());
	}
	private String fixFilePath(String path) {
		while(path.endsWith(File.separator) || path.endsWith(".")) {
			path = path.substring(0,path.length()-1);
		}
		if (!path.toLowerCase().endsWith(".xml")) {
			path = path + ".xml";
		}
		return path;
	}
	public void removeObject(GameObject obj) {
		int index = gameObjects.indexOf(obj);
		if (index>=0) {
//			if (tracksChanges) {
//				addChange(new GameObjectDeletionChange(obj));
//			}
			
			// Make sure all links are broken (these four lines were added 8/3/2007)
			// This is bad!  This breaks RealmSpeak, so I need a different solution!!!!
//			obj.clearHold();
//			if (obj.getHeldBy()!=null) {
//				obj.getHeldBy().remove(obj);
//			}
			
			gameObjects.remove(index);
			gameObjectIDHash.remove(new Long(obj.getId()));
			gameObjectNameHash.removeKeyValue(obj.getName(),obj);
			rebuildFilteredGameObjects();
			setModified(true);
		}
	}
	public void removeSetup(GameSetup setup) {
		int index = gameSetups.indexOf(setup);
		if (index>=0) {
			gameSetups.remove(index);
			setModified(true);
		}
	}
	public GameObject createNewObject() {
		return createNewObject(cumulative_id);
	}
	public GameObject createNewObject(long anId) {
		if (!gameObjectIDHash.containsKey(anId)) {
			if (anId>=cumulative_id) {
				cumulative_id = anId + 1;
			}
			GameObject obj = new GameObject(this,anId);
			gameObjects.add(obj);
			gameObjectIDHash.put(new Long(obj.getId()),obj);
			gameObjectNameHash.put(obj.getName(),obj);
			rebuildFilteredGameObjects();
			setModified(true);
			return obj;
		}
		throw new IllegalArgumentException("Cannot create an object with id "+anId+", because one already exists!!");
	}
	/**
	 * Creates a new game object that is a clone of the provided game object (same name and attributes)
	 */
	public GameObject createNewObject(GameObject go) {
		GameObject clone = createNewObject();
		clone.copyAttributesFrom(go);
		return clone;
	}
	public GameSetup createNewSetup() {
		GameSetup setup = new GameSetup(this);
		gameSetups.add(setup);
		setModified(true);
		return setup;
	}
	/**
	 * Replaces the game object with the same id.  Returns true on success.
	 */
	public boolean replaceObject(GameObject obj) {
		GameObject old = getGameObject(obj.getId());
		if (old!=null) {
			int oldIndex = gameObjects.indexOf(old);
			gameObjects.set(oldIndex,obj);
			gameObjectIDHash.put(new Long(obj.getId()),obj);
			gameObjectNameHash.removeKeyValue(old.getName(),old);
			gameObjectNameHash.put(obj.getName(),obj);
			return true;
		}
		return false;
	}
	protected void changingName(String oldName,String newName,GameObject obj) {
		gameObjectNameHash.removeKeyValue(oldName,obj);
		gameObjectNameHash.put(newName,obj);
	}
	/**
	 * Returns true if the provided object is the same one as found in GameData
	 */
	public boolean validate(GameObject obj) {
		GameObject real = getGameObject(obj.getId());
		return real==obj;
	}
	/**
	 * Provides an independant (deep) copy of the gameObjects collection
	 */
	private ArrayList getGameObjectsCopy() {
		HashMap map = new HashMap();
		ArrayList goCopy = new ArrayList();
		for (Iterator i=gameObjects.iterator();i.hasNext();) {
			GameObject obj = (GameObject)i.next();
			GameObject theCopy = new GameObject(this);
			theCopy.copyFrom(obj);
			goCopy.add(theCopy);
			map.put(obj.getId(),theCopy);
		}
		for (Iterator i=goCopy.iterator();i.hasNext();) {
			GameObject obj = (GameObject)i.next();
			obj.resolveHold(map);
		}
		return goCopy;
	}
	public GameSetup findSetup(String setupName) {
		// Find setup
		for (Iterator i=gameSetups.iterator();i.hasNext();) {
			GameSetup setup = (GameSetup)i.next();
			if (setup.getName().equals(setupName)) {
				return setup;
			}
		}
		return null;
	}
	public String[] getGameSetupNames() {
		String[] names = new String[gameSetups.size()];
		int n=0;
		for (Iterator i=gameSetups.iterator();i.hasNext();) {
			GameSetup setup = (GameSetup)i.next();
			names[n++] = setup.getName();
		}
		return names;
	}
	
	/**
	 * Test setup - GameData itself is not modified - a copy of the objects is made
	 * prior to setup
	 */
	public ArrayList doTestSetup(String setupName) {
		return doTestSetup(new StringBuffer(),findSetup(setupName));
	}
	public ArrayList doTestSetup(StringBuffer result,String setupName) {
		return doTestSetup(result,findSetup(setupName));
	}
	public ArrayList doTestSetup(StringBuffer result,GameSetup setup) {
		if (setup!=null) {
			ArrayList aCopy = setup.processSetup(result,getGameObjectsCopy());
			return aCopy;
		}
		return null;
	}
	/**
	 * Process the game objects with a setup type
	 */
	public ArrayList doSetup(String setupName,ArrayList<String> keyVals) {
		return doSetup(new StringBuffer(),findSetup(setupName),keyVals);
	}
	public ArrayList doSetup(StringBuffer result,String setupName,ArrayList<String> keyVals) {
		return doSetup(result,findSetup(setupName),keyVals);
	}
	public ArrayList doSetup(StringBuffer result,GameSetup setup,ArrayList<String> keyVals) {
		if (setup!=null) {
			GamePool pool = new GamePool(gameObjects);
			ArrayList aCopy = setup.processSetup(result,pool.find(keyVals));
			return aCopy;
		}
		return null;
	}
	public String toString() {
		return gameName;
	}
	public void setModified(boolean val) {
		super.setModified(val);
		rebuildFilteredGameObjects();
	}
	// Serializable interface
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}
	public boolean isTracksChanges() {
		return tracksChanges;
	}
	/**
	 * Sets whether or not this data object will track changes.  If true, then all changes require a call to commit()
	 * to be set permanently.  To subvert this strategy, you can instead use the _set methods, but please don't.
	 */
	public void setTracksChanges(boolean tracksChanges) {
		this.tracksChanges = tracksChanges;
		if (tracksChanges) {
			objectChanges = new ArrayList<GameObjectChange>();
		}
		else {
			objectChanges.clear();
			objectChanges = null;
		}
	}
	public long getDataVersion() {
		return dataVersion;
	}
//	public void bumpVersion() {
//		dataVersion++;
//	}
	public synchronized void addChange(GameObjectChange change) {
		dataVersion++;
		if (tracksChanges) {
			objectChanges.add(change);
//if (change instanceof GameAttributeBlockChange) {
//	System.out.println("-------------------");
//	(new Exception()).printStackTrace(System.out);
//	System.out.println(dataid+": GameAttributeBlockChange = "+change);
//}
		}
		else throw new IllegalStateException("Cannot add a change to data when not tracking changes.");
	}
	public ArrayList<GameObjectChange> getObjectChanges() {
		return objectChanges;
	}
	public int getChangeCount() {
		if (objectChanges!=null) {
			return objectChanges.size();
		}
		return 0;
	}
	/**
	 * It's possible for the GameObjects to lose their uncommitted object, but still be changes remembered by this
	 * GameData object.  In this scenario, we need to restore synchronicity by rebuilding the changes.
	 */
	public void rebuildChanges() {
		if (objectChanges!=null && !objectChanges.isEmpty()) {
			ArrayList<GameObjectChange> rebuildObjectChanges = new ArrayList<GameObjectChange>(objectChanges);
			
			// First, go through all affected objects, and make sure that their uncommitted object is gone
			for (GameObjectChange change:rebuildObjectChanges) {
				change.getGameObject(this).stopUncommitted();
			}
			
			objectChanges.clear(); // Since we are rebuilding, clear out the changes so they can be added back
			for (GameObjectChange change:rebuildObjectChanges) {
				change.rebuildChange(this);
				// Note:  rebuildChange WILL cause feedback to GameData, and add a change to objectChanges
				//		This is why I'm using a new ArrayList, so there won't be any concurrent mods.
			}
		}
	}
	/**
	 * This pops changes off the objectChanges stack, and commits them immediately
	 */
	public synchronized ArrayList<GameObjectChange> popAndCommit() {
		ArrayList<GameObjectChange> list = new ArrayList<GameObjectChange>();
		int size = objectChanges.size();
		if (size>0) {
			for (int i=0;i<size;i++) {
				GameObjectChange change = objectChanges.remove(0);
				change.applyChange(this);
				list.add(change);
			}
		}
		return list;
	}
	public synchronized void commit() {
//(new Exception()).printStackTrace(System.out);
		if (objectChanges!=null && !objectChanges.isEmpty()) {
//System.out.println(dataid+":  **** COMMIT "+objectChanges.size()+" OBJECTS ****");
			for (GameObjectChange change:objectChanges) {
				change.applyChange(this);
			}
			objectChanges.clear();
//System.out.println(dataid+":  **** COMMIT FINISHED ****");
		}
	}
	public void rollback() {
//System.out.println("rollback()");
		if (objectChanges!=null && !objectChanges.isEmpty()) {
			ArrayList objectsThatHaveChanged = new ArrayList();
			for (GameObjectChange change:objectChanges) {
//System.out.println("Rolling back "+change);
				GameObject go = getGameObject(change.getId());
				if (!objectsThatHaveChanged.contains(go)) {
					objectsThatHaveChanged.add(go);
					go.rollback();
				}
			}
			objectChanges.clear();
		}
	}
	/**
	 * @return			A list of GameObjectChange objects required to make this game data object look exactly like the other
	 */
	public ArrayList buildChanges(GameData other) {
		ArrayList changes = new ArrayList();
		
//		long maxid = getMaxId();
		
		// Add new objects first (do separately from attribute builds in case they reference each other!)
		ArrayList newObjects = new ArrayList();
		for (Iterator i=other.gameObjects.iterator();i.hasNext();) {
			GameObject otherGo = (GameObject)i.next();
			if (!gameObjectIDHash.containsKey(new Long(otherGo.getId()))) {
//				if (otherGo.getId()<maxid) {
//					throw new IllegalStateException("This is not good");
//				}
				newObjects.add(otherGo);
				changes.add(new GameObjectCreationChange(otherGo));
			}
		}
		// Now we can get the builds
		for (Iterator i=newObjects.iterator();i.hasNext();) {
			GameObject otherGo = (GameObject)i.next();
			changes.addAll(otherGo.buildChanges());
		}
		
		// Finally
		for (Iterator i=gameObjects.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			GameObject otherGo = (GameObject)other.getGameObject(go.getId());
			if (otherGo!=null) {
				changes.addAll(go.buildChanges(otherGo));
			}
			else {
				changes.add(new GameObjectDeletionChange(go));
//				throw new IllegalStateException("null object in other for id="+go.getId());
			}
		}
		
		return changes;
	}
	public String getDataName() {
		return dataName;
	}
	public void setDataName(String dataName) {
		this.dataName = dataName;
	}
	public String toIdentifier() {
		return dataName+"["+dataid+"]";
	}
	public String getChangeInformation() {
		StringBuffer sb = new StringBuffer();
		sb.append(dataName);
		sb.append("<");
		sb.append(getRelativeSize());
		sb.append(">");
		sb.append("  ");
		sb.append("TrackChanges=");
		sb.append(tracksChanges?"T":"F");
		sb.append("  ");
		sb.append("Changes=");
		sb.append(getChangeCount());
		return sb.toString();
	}
	public int getRelativeSize() {
		int count = 0;
		ArrayList list = new ArrayList(gameObjects);
		for (Iterator i=list.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			count+=go.getRelativeSize();
		}
		return count;
	}
}

/*

SAMPLE LAYOUT

<?xml version="1.0" encoding="UTF-8"?>
<game name="Magic Realm" description="Avalon Hill's Trade name for...">
	<objects>
		<GameObject id="0" name="Company 3>
			<AttributeBlock blockName="this">
				<keyword name="native" />     <--- This hasn't been implemented....
				<keyword name="s_campfire" />
				<keyword name="l_campfire" />
				<keyword name="company" />
				<attribute rank="3" />
				<attribute die_roll="4" />
			</AttributeBlock>
			<AttributeBlock blockName="side_1">
				<keyword name="light" />
				<attribute attack="4" />
				<attribute move="5" />
			</AttributeBlock>
			<AttributeBlock blockName="side_2">
				<keyword name="dark" />
				<attribute attack="5" />
				<attribute move="4" />
			</AttributeBlock>
			<contains id="1" />
			<contains id="2" />
			<contains id="3" />
			<contains id="4" />
		</GameObject>
		<GameObject id="1" name="Company 4>
			<AttributeBlock blockName="this">
				<attribute company="4" />
			</AttributeBlock>
			<AttributeBlock blockName="side_1">
				<attribute attack="2" />
				<attribute move="6" />
			</AttributeBlock>
			<AttributeBlock blockName="side_2">
				<attribute attack="6" />
				<attribute move="2" />
			</AttributeBlock>
			<contains id="5" />
			<contains id="6" />
		</GameObject>
	</objects>
	<setup>
		<GameSetup id="7" name="Standard Setup">
			<Extract from="ALL" to="ORIGINAL_SET" keywords="original_set" />
			<Extract from="ORIGINAL_SET" to="TREASURE_POOL" keywords="sound" />
			<Extract from="ORIGINAL_SET" to="TREASURE_POOL" keywords="treasure_location" />
			<Extract from="ORIGINAL_SET" to="CITY" keywords="lost_city" />
			<Extract from="ORIGINAL_SET" to="CASTLE" keywords="lost_castle" />
			<Distribute from="TREASURE_POOL" to="CITY" count="5" method="random" />
			<Distribute from="TREASURE_POOL" to="CASTLE" count="5" method="random" />
			<Move from="TREASURE_POOL" to="CITY" count="4" method="random" />
			<Move from="TREASURE_POOL" to="CASTLE" count="4" method="random" />
			<Extract from="ORIGINAL_SET" to="TILES_C" keywords="tile,c" />
			<Extract from="ORIGINAL_SET" to="TILES_M" keywords="tile,m" />
			<Distribute from="CITY" to="TILES_C" count="5" method="random" />
			<Distribute from="CASTLE" to="TILES_M" count="5" method="random" />
		</GameSetup>
	</setup>
	<notes>
		<note id="0">This is a note</note>
	</notes>
</game>

*/