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
package com.robin.magic_realm.RealmCharacterBuilder;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import com.robin.game.objects.*;
import com.robin.general.io.FileUtilities;
import com.robin.general.io.ZipUtilities;
import com.robin.general.swing.*;
import com.robin.general.util.OrderedHashtable;
import com.robin.magic_realm.components.CharacterChitComponent;
import com.robin.magic_realm.components.RealmComponent;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class RealmCharacterBuilderModel {
	
	/*
	 * As new versions are released (hopefully few and far between), I will document each, and support
	 * a conversion process.
	 */
	public static final String IMAGE_EXTENSION = ".png";
	public static final String CHARACTER_XML_FILENAME = "_RCB2212_character.xml";
	public static final String CHARACTER_PICTURE_GIF_FILENAME = "_RCB2212_picture";
	public static final String CHARACTER_TOKEN_GIF_FILENAME = "_RCB2212_symbol";
	public static final String CHARACTER_WEAPON_GIF_PREFIX = "_RCB2212_weapon_";
	public static final String CHARACTER_BADGE_GIF_PREFIX = "_RCB2212_badge_";
	
	private CharacterWrapper character;
	private GameObject[] chit;
	
	private CharacterChitComponent characterToken;
	
	private ImageIcon characterSymbol = null; // initially, it is defaulted to question.gif
	private ImageIcon pictureIcon = null;
	
	private Hashtable<String,GameObject> weaponHash;
	
	public RealmCharacterBuilderModel(GameData data) {
		chit = new GameObject[12];
		int n=0;
		ArrayList list = new ArrayList(data.getGameObjects());
		for (Iterator i=list.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			String opt = go.getThisAttribute("optional");
			if ("add".equals(opt)) {
				continue;
			}
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			if (rc!=null) {
				if (rc.isActionChit()) {
					chit[n++] = go;
				}
				else if (rc.isCharacter()) {
					character = new CharacterWrapper(go);
					characterToken = (CharacterChitComponent)rc;
				}
			}
		}
		weaponHash = new Hashtable<String,GameObject>();
		GamePool pool = new GamePool(character.getGameObject().getGameData().getGameObjects());
		ArrayList<GameObject> weapons = pool.find(TemplateLibrary.WEAPON_QUERY);
		for (GameObject go:weapons) {
			weaponHash.put(go.getName(),go);
		}
	}
	public CharacterInfoCard getCard() {
		CharacterInfoCard card = new CharacterInfoCard(character);
		card.setPicture(pictureIcon);
		return card;
	}
	public void setRelationship(String denizen,int rel) {
		if (rel==0) {
			character.getGameObject().removeAttribute(Constants.BASE_RELATIONSHIP,denizen);
			character.getGameObject().removeAttribute(Constants.GAME_RELATIONSHIP,denizen); // Handle both, so the table works
		}
		else {
			character.getGameObject().setAttribute(Constants.BASE_RELATIONSHIP,denizen,rel);
			character.getGameObject().setAttribute(Constants.GAME_RELATIONSHIP,denizen,rel);
		}
	}
	public CharacterWrapper getCharacter() {
		return character;
	}
	public GameData getData() {
		return character.getGameObject().getGameData();
	}
	public CharacterChitComponent getCharacterToken() {
		return characterToken;
	}
	public ArrayList<GameObject> getAllChits() {
		ArrayList<GameObject> list = new ArrayList<GameObject>();
		for (int i=1;i<=4;i++) {
			list.addAll(getChits(i));
		}
		return list;
	}
	public ArrayList<GameObject> getChits(int level) {
		ArrayList<GameObject> list = new ArrayList<GameObject>();
		int start = ((level-1)*3);
		for (int i=start;i<start+3;i++) {
			list.add(chit[i]);
		}
		return list;
	}
	public void updateSymbolIcon(ImageIcon icon) {
		String iconName = character.getGameObject().getName().toLowerCase(); // this may change when the file is saved!
		updateSymbolIcon(icon,iconName);
	}
	public void updateSymbolIcon(ImageIcon icon,String iconName) {
		String iconFolder = RealmCharacterConstants.CUSTOM_ICON_BASE_PATH+"characters";
		String iconPath = iconFolder+"/"+iconName;
		character.getGameObject().setThisAttribute(Constants.ICON_TYPE,iconName);
		characterSymbol = icon; // for saving purposes only
		ImageCache._placeImage(iconPath,icon); // Trick the cache to use this icon
		character.getIcon(); // force the loading of the character icon while the file is open!
		CharacterChitComponent rc = new CharacterChitComponent(character.getGameObject());
		rc.getSmallSymbol(); // another forced loading (sigh)
		
		// update character
		character.getGameObject().setThisAttribute(Constants.ICON_TYPE,iconName);
		character.getGameObject().setThisAttribute(Constants.ICON_FOLDER,iconFolder);
		
		// update chits
		for (int i=0;i<chit.length;i++) {
			chit[i].setThisAttribute(Constants.ICON_TYPE,iconName);
			chit[i].setThisAttribute(Constants.ICON_FOLDER,iconFolder);
		}
	}
	public void updateWeaponIcon(GameObject weapon,ImageIcon icon) {
		String iconFolder = RealmCharacterConstants.CUSTOM_ICON_BASE_PATH+"weapons";
		String iconName = weapon.getName().toLowerCase(); // this may change when the file is saved!
		String iconPath = iconFolder+"/"+iconName;
		ImageCache._placeImage(iconPath,icon); // Trick the cache to use this icon
		weapon.setThisAttribute(Constants.ICON_FOLDER,iconFolder);
		weapon.setThisAttribute(Constants.ICON_TYPE,iconName);
	}
	public ImageIcon getCharacterSymbol() {
		return characterSymbol;
	}
	public void addWeapon(String name,GameObject weapon) {
		weaponHash.put(name,weapon);
	}
	public GameObject getWeapon(String name) {
		return weaponHash.get(name);
	}
	public boolean hasWeapon(String name) {
		return weaponHash.containsKey(name);
	}
	/**
	 * This will grab the weapon names from all four levels, and remove any GameObjects that are NOT used
	 */
	public void updateWeaponUsage() {
		ArrayList<String> weapons = new ArrayList<String>();
		for (int i=1;i<=4;i++) {
			String levelKey = "level_"+i;
			String weapon = character.getGameObject().getAttribute(levelKey,"weapon");
			if (weapon!=null && !weapons.contains(weapon)) {
				weapons.add(weapon);
			}
		}
		GamePool pool = new GamePool(character.getGameObject().getGameData().getGameObjects());
		ArrayList<GameObject> toRemove = new ArrayList<GameObject>();
		ArrayList<GameObject> currentWeapons = pool.find(TemplateLibrary.WEAPON_QUERY);
		for (GameObject go:currentWeapons) {
			if (!weapons.contains(go.getName())) {
				toRemove.add(go);
			}
		}
		for (GameObject go:toRemove) {
			weaponHash.remove(go.getName());
			character.getGameObject().getGameData().removeObject(go);
		}
	}
	public ArrayList<String> getAllWeaponNames() {
		return new ArrayList<String>(weaponHash.keySet());
	}
	public boolean saveToFile(File file,boolean graphicsOnly) {
		String name = character.getGameObject().getName().replace(' ','_');
		
		// before doing anything, rename all badge icons using character name
		for (int i=1;i<=4;i++) {
			String levelKey = "level_"+i;
			String badgeName = character.getGameObject().getAttribute(levelKey,"badge_icon");
			if (badgeName!=null) {
				ImageIcon icon = ImageCache.getIcon(badgeName);
				badgeName = name+"badge"+i;
				ImageCache._placeImage(badgeName,icon);
				character.getGameObject().setAttribute(levelKey,"badge_icon",badgeName);
			}
		}
		
		getData().setGameDescription(VersionManager.CURRENT_VERSION);
		
		// save it
		String dir = graphicsOnly?(file.getAbsolutePath()+File.separator):FileUtilities.getFilePathString(file,false,false);
		File xmlFile = new File(dir+CHARACTER_XML_FILENAME);
		xmlFile.deleteOnExit();
		if (getData().saveToFile(xmlFile)) {
			if (graphicsOnly) {
				xmlFile.delete();
			}
			
			ArrayList<File> fileList = new ArrayList<File>();
			fileList.add(xmlFile);
			
			// Save the images
			if (pictureIcon!=null) {
				File pictureFile = new File(dir+name+CHARACTER_PICTURE_GIF_FILENAME+IMAGE_EXTENSION);
				if (!graphicsOnly) pictureFile.deleteOnExit();
				exportImage(pictureFile,pictureIcon);
				fileList.add(pictureFile);
			}
			if (getCharacterSymbol()!=null) {
				File symbolFile = new File(dir+name+CHARACTER_TOKEN_GIF_FILENAME+IMAGE_EXTENSION);
				if (!graphicsOnly) symbolFile.deleteOnExit();
				exportImage(symbolFile,getCharacterSymbol());
				fileList.add(symbolFile);
			}
			ArrayList<String> weaponNames = getAllWeaponNames();
			for (String weaponName:weaponNames) {
				GameObject go = getWeapon(weaponName);
				String iconFolder = go.getThisAttribute(Constants.ICON_FOLDER);
				if (iconFolder.startsWith(RealmCharacterConstants.CUSTOM_ICON_BASE_PATH)) {
					// All custom icons require saving
					String iconName = go.getThisAttribute(Constants.ICON_TYPE);
					String filename = iconFolder+"/"+iconName;
					ImageIcon icon = ImageCache.getIcon(filename);
					File weaponFile = new File(dir+name+CHARACTER_WEAPON_GIF_PREFIX+weaponName.replace(' ','_')+IMAGE_EXTENSION);
					if (!graphicsOnly) weaponFile.deleteOnExit();
					exportImage(weaponFile,icon);
					fileList.add(weaponFile);
				}
			}
			// Badge icons
			for (int i=1;i<=4;i++) {
				String levelKey = "level_"+i;
				String badgeName = character.getGameObject().getAttribute(levelKey,"badge_icon");
				if (badgeName!=null) {
					ImageIcon icon = ImageCache.getIcon(badgeName);
					File badgeFile = new File(dir+name+CHARACTER_BADGE_GIF_PREFIX+badgeName+IMAGE_EXTENSION);
					if (!graphicsOnly) badgeFile.deleteOnExit();
					exportImage(badgeFile,icon);
					fileList.add(badgeFile);
				}
			}
			
			if (!graphicsOnly) {
				// Now zip it all up
				ZipUtilities.zip(file,fileList.toArray(new File[fileList.size()]));
				for (File del:fileList) {
					del.delete();
				}
			}
		}
		return true;
	}
	public static boolean exportImage(File file, ImageIcon imageIcon) {
		return exportImage(file,imageIcon,"PNG");
	}
	public static boolean exportImage(File file, ImageIcon imageIcon,String format) {
		try {
//			System.out.println("writing image: "+imageIcon.getIconWidth()+" x "+imageIcon.getIconHeight());
			
			// Why this is necessary, I have no idea, but it solves my problem with blank images being written
			BufferedImage bi = new BufferedImage(imageIcon.getIconWidth(),imageIcon.getIconHeight(),BufferedImage.TYPE_4BYTE_ABGR);
			bi.getGraphics().drawImage(imageIcon.getImage(),0,0,null);
			
			FileOutputStream fileoutputstream = new FileOutputStream(file);
			ImageIO.write(bi,"PNG",fileoutputstream);
			
			fileoutputstream.close();
			return true;
		}
		catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
	}
	public ImageIcon getPictureIcon() {
		return pictureIcon;
	}
	public void setPictureIcon(ImageIcon pictureIcon) {
		this.pictureIcon = pictureIcon;
	}
	public ArrayList<GameObject> getAllUniqueArmor(GameData magicRealmData) {
		ArrayList<String> armorNames = new ArrayList<String>();
		for (int i=1;i<=4;i++) {
			String armor = character.getGameObject().getAttribute("level_"+i,"armor");
			if (armor!=null) {
				StringTokenizer st = new StringTokenizer(armor,",");
				while(st.hasMoreTokens()) {
					String token = st.nextToken();
					if (!armorNames.contains(token)) {
						armorNames.add(token);
					}
				}
			}
		}
		ArrayList<GameObject> list = new ArrayList<GameObject>();
		GamePool pool = new GamePool(magicRealmData.getGameObjects());
		for (String armorName:armorNames) {
			GameObject template = pool.findFirst("!magic,Name="+armorName);
			GameObject go = GameObject.createEmptyGameObject();
			go.copyAttributesFrom(template);
			list.add(go);
		}
		return list;
	}
	public ArrayList<GameObject> getAllUniqueWeapons() {
		ArrayList<String> weaponNames = new ArrayList<String>();
		for (int i=1;i<=4;i++) {
			String name = character.getGameObject().getAttribute("level_"+i,"weapon");
			if (name!=null && !weaponNames.contains(name)) {
				weaponNames.add(name);
			}
		}
		ArrayList<GameObject> list = new ArrayList<GameObject>();
		for (String weaponName:weaponNames) {
			GameObject go = getWeapon(weaponName);
			list.add(go);
		}
		return list;
	}
	public ArrayList<GameObject> getAllCompanions() {
		ArrayList<String> companionNames = new ArrayList<String>();
		for (int i=1;i<=4;i++) {
			ArrayList list = character.getGameObject().getAttributeList("level_"+i,Constants.COMPANION_NAME);
			if (list!=null) {
				companionNames.addAll(list);
			}
		}
		ArrayList<GameObject> ret = new ArrayList<GameObject>();
		for (String name:companionNames) {
			for (Iterator i=character.getGameObject().getGameData().getGameObjects().iterator();i.hasNext();) {
				GameObject go = (GameObject)i.next();
				if (name.equals(go.getName())) {
					ret.add(go);
					if (go.getHoldCount()>0) {
						ret.addAll(go.getHold());
					}
					break;
				}
			}
		}
		return ret;
	}
	
	public ArrayList<GameObject> getAllExtraChits() {
		ArrayList<GameObject> ret = new ArrayList<GameObject>();
		for (int n=1;n<=4;n++) {
			String levelKey = "level_"+n;
			String blockName = Constants.BONUS_CHIT+levelKey;
			if (character.getGameObject().hasAttributeBlock(blockName)) {
				GameObjectBlockManager man = new GameObjectBlockManager(character.getGameObject());
				GameObject go = man.extractGameObjectFromBlocks(blockName,true);
				ret.add(go);
			}
		}
		return ret;
	}
	
	public ArrayList<GameObject> getAllExtraInventory() {
		ArrayList<GameObject> ret = new ArrayList<GameObject>();
		for (int n=1;n<=4;n++) {
			String levelKey = "level_"+n;
			String blockName = Constants.BONUS_INVENTORY+levelKey;
			if (character.getGameObject().hasAttributeBlock(blockName)) {
				GameObjectBlockManager man = new GameObjectBlockManager(character.getGameObject());
				GameObject go = man.extractGameObjectFromBlocks(blockName,true);
				ret.add(go);
			}
		}
		return ret;
	}
	
	public static GameData createEmptyCharacter() {
		GameData gameData = new GameData("Created by Realm Character Builder");
		GameObject character = gameData.createNewObject();
		character.setName("Untitled");
		character.setThisAttribute(Constants.CUSTOM_CHARACTER);
		character.setThisAttribute("character");
		character.setThisAttribute("fighter"); // for now
		character.setThisAttribute("facing","dark");
		character.setThisAttribute("start","Inn");
		character.setThisAttribute("pronoun","he");
		character.setThisAttribute("creator","Unknown");
		character.setThisAttribute("artcredit","Unknown");
		character.setThisAttribute("meaning","Unknown");
		character.setThisAttribute("vulnerability","L");
		character.setThisAttribute("icon_type","question");
		character.setThisAttribute("icon_folder",RealmCharacterConstants.CUSTOM_ICON_BASE_PATH+"characters");
// test
//character.addAttributeListItem("level_2","advantages","TEST: Did this work?  What if this is a really really long explanation?");
//character.setAttribute("level_2","badge_icon",RealmCharacterConstants.CUSTOM_ICON_BASE_PATH+"badges/two");
//character.setAttribute("level_2","no_magic_fatigue","");
		for (int i=1;i<=4;i++) {
			String name = i==4?"Untitled":("Level "+i);
			character.setAttribute("level_"+i,"name",name);
		}
		GameObject chit;
		for (int i=0;i<12;i++) {
			chit = gameData.createNewObject();
			if (i%3==0) {
				chit.setName("FIGHT L4");
				chit.setThisAttribute("action","FIGHT");
			}
			else {
				chit.setName("MOVE L4");
				chit.setThisAttribute("action","MOVE");
			}
			chit.setThisAttribute("strength","L");
			chit.setThisAttribute("speed",4);
			chit.setThisAttribute("character_chit","");
			chit.setThisAttribute("icon_type","question");
			chit.setThisAttribute("icon_folder",RealmCharacterConstants.CUSTOM_ICON_BASE_PATH+"characters");
			chit.setThisAttribute("stage",i+1);
			chit.setThisAttribute("level",(i/3)+1);
			chit.setThisAttribute("effort",0);
			character.add(chit);
		}
		return gameData;
	}
	public static GameData createCharacterFromTemplate(GameObject template) {
		GameData gameData = new GameData("Created by Realm Character Builder");
		GameObject character = gameData.createNewObject();
		character.copyAttributesFrom(template);
		character.setThisAttribute(Constants.CUSTOM_CHARACTER);
		
		// Create all the chits
		for (Iterator i=template.getHold().iterator();i.hasNext();) {
			GameObject chitTemplate = (GameObject)i.next();
			GameObject chit = gameData.createNewObject();
			chit.copyAttributesFrom(chitTemplate);
			character.add(chit);
		}
		
		// Create all the weapons
		ArrayList<String> weaponNames = new ArrayList<String>();
		for (int i=1;i<=4;i++) {
			String name = template.getAttribute("level_"+i,"weapon");
			if (!weaponNames.contains(name)) {
				weaponNames.add(name);
			}
		}
		if (!weaponNames.isEmpty()) {
			GamePool pool = new GamePool(template.getGameData().getGameObjects());
			ArrayList<GameObject> allWeapons = pool.find(TemplateLibrary.WEAPON_QUERY);
			for (GameObject go:allWeapons) {
				if (weaponNames.contains(go.getName())) {
					weaponNames.remove(go.getName());
				}
				GameObject weapon = gameData.createNewObject();
				weapon.copyAttributesFrom(go);
			}
		}
		
		// Copy the relationships so they show
		OrderedHashtable hash = character.getAttributeBlock(Constants.BASE_RELATIONSHIP);
		for (Iterator i=hash.keySet().iterator();i.hasNext();) {
			String key = (String)i.next();
			String val = (String)hash.get(key);
			character.setAttribute(Constants.GAME_RELATIONSHIP,key,val);
		}
		
		return gameData;
	}
	public static RealmCharacterBuilderModel createFromFile(File file) {
		// load it
		File[] files = ZipUtilities.unzip(file);
		if (files==null) {
			throw new RealmCharacterException("Unable to open "+file.getAbsolutePath()+":  "+ZipUtilities.lastError);
		}
		
		// Make sure all the files will be deleted no matter what happens
		for (int i=0;i<files.length;i++) {
			files[i].deleteOnExit();
		}
		
		RealmCharacterBuilderModel model = null;
		// First, get the XML file, so the model can be created
		String error = "Invalid character file"; // default error
		for (int i=0;i<files.length;i++) {
			String filename = FileUtilities.getFilename(files[i],true);
			if (CHARACTER_XML_FILENAME.equals(filename)) {
				GameData data = new GameData();
				data.ignoreRandomSeed = true;
				data.loadFromFile(files[i]);
				model = new RealmCharacterBuilderModel(data);
				error = VersionManager.convert(model);
				if (error!=null) {
					System.err.println(error);
					model = null;
				}
				break; // once you find the xml file, stop looking
			}
		}
		
		if (model!=null) {
			// Grab all the images
			for (int i=0;i<files.length;i++) {
				String filename = FileUtilities.getFilename(files[i],true);
				if (filename.indexOf(CHARACTER_TOKEN_GIF_FILENAME)>0) {
					ImageIcon icon = IconFactory.findIcon(files[i].getAbsolutePath());
					model.updateSymbolIcon(icon);
				}
				else if (filename.indexOf(CHARACTER_PICTURE_GIF_FILENAME)>0) {
					ImageIcon icon = IconFactory.findIcon(files[i].getAbsolutePath());
					model.setPictureIcon(icon);
				}
				else if (filename.indexOf(CHARACTER_WEAPON_GIF_PREFIX)>0) {
					ImageIcon icon = IconFactory.findIcon(files[i].getAbsolutePath());
					String weaponName = cropName(CHARACTER_WEAPON_GIF_PREFIX,model.getCharacter().getGameObject().getName(),filename);
					weaponName = weaponName.replace('_',' ');
					GameObject weapon = model.getWeapon(weaponName);
					model.updateWeaponIcon(weapon,icon);
				}
				else if (filename.indexOf(CHARACTER_BADGE_GIF_PREFIX)>0) {
					ImageIcon icon = IconFactory.findIcon(files[i].getAbsolutePath());
					String badgeName = cropName(CHARACTER_BADGE_GIF_PREFIX,model.getCharacter().getGameObject().getName(),filename);
					ImageCache._placeImage(badgeName,icon);
				}
				
				// Delete the file when done
				files[i].delete();
			}
			
			if (VersionManager.getWasConverted()) {
				// Resave the file so no further conversion is needed
				model.saveToFile(file,false);
			}
		}
		else {
			throw new RealmCharacterException(error);
		}
		return model;
	}
	private static String cropName(String prefix,String charName,String filename) {
		String fullPrefix = prefix+charName;
		return filename.substring(fullPrefix.length(),filename.length()-4);
	}
	public static void loadAllCustomCharacters() {
		String customFolderPath = "./characters/"; // default
		if (System.getProperty("customFolder")!=null) {
			customFolderPath = System.getProperty("customFolder")+File.separator;
		}
		ArrayList<String> unknownChars = new ArrayList<String>();
		File customFolder = new File(customFolderPath);
//System.out.println(customFolder.getAbsolutePath());
		if (customFolder.isDirectory() && customFolder.exists()) {
			File[] charFile = customFolder.listFiles();
			for (int i=0;i<charFile.length;i++) {
				if (charFile[i].getAbsolutePath().endsWith(".rschar")) {
//System.out.println(charFile[i].getAbsolutePath());
					try {
						RealmCharacterBuilderModel model = RealmCharacterBuilderModel.createFromFile(charFile[i]);
						if (model!=null) {
							CharacterInfoCard card = model.getCard();
							CustomCharacterLibrary.getSingleton().addCustomCharacterTemplate(
									model.getCharacter().getGameObject(),card.getImageIcon(true));
						}
					}
					catch(ImageCacheException ex) {
						System.err.println("Problem initializing graphics for character file "+charFile[i].getName()+".  Try redownloading the character.");
						unknownChars.add(charFile[i].getName()+" - "+ex.getMessage());
					}
					catch(RealmCharacterException ex) {
						System.err.println("Character file "+charFile[i].getName()+" is not recognized, and will be ignored.");
						unknownChars.add(charFile[i].getName()+" - "+ex.getMessage());
					}
					catch(Exception ex) {
						System.err.println("Character file "+charFile[i].getName()+" is broken, and will be ignored.");
						String at = ex.getStackTrace().length>0?("\n            at "+ex.getStackTrace()[0].toString()):"";
						unknownChars.add(charFile[i].getName()+" - "+ex.toString()+at);
					}
//System.out.println(CustomCharacterLibrary.getSingleton().getCharacterUniqueKey(model.getCharacter().getGameObject().getName()));
				}
			}
		}
		if (unknownChars.size()>0) {
			StringBuffer sb = new StringBuffer();
			sb.append("Some custom character files were not loaded, because they were invalid:\n\n");
			for (String file:unknownChars) {
				sb.append("     ");
				sb.append(file);
				sb.append("\n");
			}
			JOptionPane.showMessageDialog(null,sb.toString(),"Invalid Custom Characters",JOptionPane.WARNING_MESSAGE);
		}
	}
	public static void addCustomCharacters(HostPrefWrapper hostPrefs,GameData dataSource) {
		// Add all custom characters
		hostPrefs.clearCharacterKeys();
		CustomCharacterLibrary library = CustomCharacterLibrary.getSingleton();
		for (String name:library.getCharacterTemplateNameList()) {
			hostPrefs.addCharacterKey(library.getCharacterUniqueKey(name));
			
			GameObject template = library.getCharacterTemplate(name);
			GameObject newChar = dataSource.createNewObject();
			newChar.copyAttributesFrom(template);
			newChar.setThisKeyVals(hostPrefs.getGameKeyVals());
			RealmComponent.clearOwner(newChar);
			for (Iterator i=template.getHold().iterator();i.hasNext();) {
				GameObject go = (GameObject)i.next();
				if (go.hasThisAttribute("character_chit")) {
					GameObject newChit = dataSource.createNewObject();
					newChit.copyAttributesFrom(go);
					newChit.setThisKeyVals(hostPrefs.getGameKeyVals());
					newChar.add(newChit);
				}
			}
		}
	}
}