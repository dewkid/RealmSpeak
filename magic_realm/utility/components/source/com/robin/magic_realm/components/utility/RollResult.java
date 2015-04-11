package com.robin.magic_realm.components.utility;

import com.robin.general.swing.DieRoller;

public class RollResult {
	public DieRoller roller;
	public String message;
	public int roll;
	
	public RollResult(DieRoller roller, String message, int roll){
		this.roller = roller;
		this.message=message;
		this.roll = roll;
	}	
}
