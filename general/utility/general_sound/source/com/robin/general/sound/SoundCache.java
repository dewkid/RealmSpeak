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

import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;

import javax.sound.sampled.*;

import com.robin.general.io.ResourceFinder;

public class SoundCache {
	private static Hashtable cache = new Hashtable();
	
	private static double currentGain = 0.5;
	
	private static boolean useSound = false;
	
	public static void setSoundEnabled(boolean val) {
		useSound = val;
	}
	public static boolean isSoundEnabled() {
		return useSound;
	}
	
	public static Clip getClip(String name) {
		String soundPath = "sounds/"+name;
		Clip clip = (Clip)cache.get(soundPath);
		if (clip==null) {
			clip = loadClip(soundPath);
			cache.put(soundPath,clip);
		}
		
		return clip;
	}
	
	protected static Clip loadClip(String soundPath) {
		Clip clip = null;
		try {
			InputStream stream = ResourceFinder.getInputStream(soundPath);
			if (stream!=null) {
				AudioInputStream ais = AudioSystem.getAudioInputStream(stream);
				clip = loadClip(ais);
				adjustVolume(clip,currentGain);
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			useSound = false; // issue with sound here, then just turn it off
		}
		if (clip==null) {
			System.out.println("failed to read sound at "+soundPath);
//			(new Exception()).printStackTrace(); // so I can see where this happens!
		}
		return clip;
	}
	private static Clip loadClip(AudioInputStream ais) {
		AudioFormat format = ais.getFormat();

		DataLine.Info info = new DataLine.Info(
		                  Clip.class, 
		                  ais.getFormat(), 
		                  ((int) ais.getFrameLength() *
		                      format.getFrameSize()));

		try {
			Clip clip = (Clip) AudioSystem.getLine(info);
	        clip.addLineListener(new ClipListener(clip));
			clip.open(ais);
			return clip;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			useSound = false; // issue with sound here, then just turn it off
		}
		return null;
	}
	public static void playClip(Clip clip) {
		if (clip!=null && useSound) {
			clip.stop();
			clip.flush();
			clip.setFramePosition(0);
			clip.start();
		}
	}
	public static void playSound(String name) {
		if (useSound && name!=null) {
			String soundPath = "sounds/"+name;
			Clip clip = (Clip)cache.get(name);
			if (clip==null) {
				try {
					InputStream stream = ResourceFinder.getInputStream(soundPath);
					AudioInputStream ais = AudioSystem.getAudioInputStream(stream);
					AudioFormat format = ais.getFormat();

					DataLine.Info info = new DataLine.Info(
					                  Clip.class, 
					                  ais.getFormat(), 
					                  ((int) ais.getFrameLength() *
					                      format.getFrameSize()));

					clip = (Clip) AudioSystem.getLine(info);
	                clip.addLineListener(new ClipListener(clip));
					clip.open(ais);
				}
				catch(Exception ex) {
					ex.printStackTrace();
				}
				if (clip!=null) {
					cache.put(name,clip);
				}
				else {
					System.out.println("failed to read "+soundPath);
					(new Exception()).printStackTrace(); // so I can see where this happens!
				}
			}
			playClip(clip);
		}
	}
	public static double getVolume() {
		return currentGain;
	}
	public static void setVolume(double gain) {
		if (gain!=currentGain) {
			if (gain<0.0 || gain>1.0) {
				throw new IllegalArgumentException("gain must be from 0 to 1");
			}
			currentGain = gain;
			for (Iterator i=cache.values().iterator();i.hasNext();) {
				Clip clip = (Clip)i.next();
				adjustVolume(clip,currentGain);
			}
		}
	}
	/**
	 * @param clip		The clip to be adjusted
	 * @param gain		A number between 0 (quiet) and 1 (loud)
	 */
	private static void adjustVolume(Clip clip,double gain) {
		// Set Volume
		FloatControl gainControl = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
//		double gain = .5D;    // number between 0 and 1 (loudest)
		float dB = (float)(Math.log(gain)/Math.log(10.0)*20.0);
		gainControl.setValue(dB);
		    
//		// Mute On
//		BooleanControl muteControl = (BooleanControl)clip.getControl(BooleanControl.Type.MUTE);
//		muteControl.setValue(true);
//		    
//		// Mute Off
//		muteControl.setValue(false);
	}
	
	public static void main(String[] args) {
		SoundCache.setSoundEnabled(true);
		SoundCache.playSound("test.wav");
		SoundCache.playSound("boing.wav");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}