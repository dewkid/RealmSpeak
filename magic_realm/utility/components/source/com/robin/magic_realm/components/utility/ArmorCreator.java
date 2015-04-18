package com.robin.magic_realm.components.utility;

import java.util.ArrayList;

import com.robin.game.objects.GameData;
import com.robin.game.objects.GameObject;
import com.robin.game.objects.GamePool;
import com.robin.general.util.Extensions;

public class ArmorCreator {
	private String armorKey;
	private ArrayList<GameObject> armorCreated;
	
	public static final int THRUST = 1;
	public static final int SWING = 2;
	public static final int SMASH = 4;
	public static final int CHOICE = 8;
	
	public ArmorCreator(String key){
		armorKey = key;
		armorCreated = new ArrayList<GameObject>();
	}
	
	public GameObject createOrReuseArmor(GameData data){
		GamePool pool = new GamePool(data.getGameObjects());
			
		ArrayList query = new ArrayList();
		query.add(armorKey);
		query.add(Constants.DESTROYED);
		GameObject go = pool.findFirst(query);
		if (go==null) {
			go = data.createNewObject();
		}
		
		armorCreated.add(go);
		go.removeThisAttribute(Constants.DESTROYED);
		
		return go;	
	}
	
	public void setupSide(GameObject go, String side, int price, String chitColor ){
		go.removeAttribute(side, "base_price");
		go.removeAttribute(side, "chit_color");
		
		go.setAttribute(side, "base_price", price);
		go.setAttribute(side, "chit_color", chitColor);
	}
	
	public void setupGameObject(GameObject go, String name, String iconType, String vuln, String weight, int armorRow, int armorLocations ){
		go.setName(name);
		go.setThisAttribute("armor");
		go.setThisAttribute(armorKey);
		go.setThisAttribute("vulnerability", vuln);
		go.setThisAttribute("weight", weight);
		go.setThisAttribute("icon_type", iconType);
		go.setThisAttribute("facing", "dark");
		go.setThisAttribute("armor_row", armorRow);
		go.setThisAttribute("item");
		
		setupArmorLocations(go, armorLocations);
	}
	
	private void setupArmorLocations(GameObject go, int locations){
		if(Extensions.hasFlag(locations, THRUST)) go.setThisAttribute("armor_thrust");
		if(Extensions.hasFlag(locations, SWING)) go.setThisAttribute("armor_swing");
		if(Extensions.hasFlag(locations, SMASH)) go.setThisAttribute("armor_smash");
		if(Extensions.hasFlag(locations, CHOICE)) go.setThisAttribute("armor_choice");
	}
}
