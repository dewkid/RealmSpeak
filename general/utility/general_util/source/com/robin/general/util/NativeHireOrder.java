package com.robin.general.util;

import java.util.Comparator;

import com.robin.game.objects.GameObject;
import com.robin.magic_realm.components.RealmComponent;

public class NativeHireOrder implements Comparator<GameObject> {

	@Override
	public int compare(GameObject n1, GameObject n2) {
		String rs1 = n1.getThisAttribute("rank");
		if (rs1==null) rs1 = "0";
		Integer rank1 = "HQ".equals(rs1)?new Integer(0):Integer.valueOf(rs1);
		
		String rs2 = n2.getThisAttribute("rank");
		if (rs2==null) rs2 = "0";
		Integer rank2 = "HQ".equals(rs2)?new Integer(0):Integer.valueOf(rs2);
		
		return rank2.compareTo(rank1);
	}

}
