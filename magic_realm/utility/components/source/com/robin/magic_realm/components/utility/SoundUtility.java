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
package com.robin.magic_realm.components.utility;

import com.robin.general.sound.SoundBlock;
import com.robin.general.sound.SoundCache;

public class SoundUtility {
	
	private static int BUFFER = 3;
	
	private static SoundBlock AttentionSound = new SoundBlock("attention_quick",BUFFER);
	private static SoundBlock ClickSound = new SoundBlock("click3",BUFFER);
	
	public static void setSoundEnabled(boolean val) {
		SoundCache.setSoundEnabled(val);
	}
	public static void playAttention() {
		SoundCache.playClip(AttentionSound.getNextClip());
	}
	public static void playClick() {
		SoundCache.playClip(ClickSound.getNextClip());
	}
}