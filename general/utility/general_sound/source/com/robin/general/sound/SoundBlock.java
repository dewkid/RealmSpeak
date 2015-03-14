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
package com.robin.general.sound;

import javax.sound.sampled.Clip;

/**
 * This class will manage multiple copies of the sound, so that multiple sounds can be heard (if supported)
 */
public class SoundBlock {
	
	private int index;
	private Clip[] clip;
	
	public SoundBlock(String soundName,int copies) {
		// Load multiple copies of the sound
		clip = new Clip[copies];
		for (int i=0;i<copies;i++) {
			clip[i] = SoundCache.loadClip("sounds/"+soundName+".wav");
		}
		index = 0;
	}
	public Clip getNextClip() {
		Clip ret = clip[index];
		index++;
		index %= clip.length;
		return ret;
	}
}