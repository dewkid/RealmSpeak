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

import java.util.ArrayList;

import javax.mail.MessagingException;

import com.robin.general.io.SendMail;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class RealmMail {
	public static String sendMail(HostPrefWrapper hostPrefs,ArrayList<String> recipients,String subtitle,String message) {
		return sendMail(hostPrefs.getSmtpHost(),hostPrefs.getHostEmail(),recipients,hostPrefs.getGameTitle(),subtitle,message);
	}
	public static String sendMail(String smtpHost,String hostEmail,ArrayList<String> recipients,String gameTitle,String subtitle,String message) {
		SendMail sm = new SendMail(smtpHost,hostEmail,recipients,null,null);
		sm.setSubject("["+gameTitle+"] - "+subtitle);
		sm.setMessage(message);
		String error = null;
		try {
			if (!sm.postMail()) {
				error = sm.getError();
			}
		}
		catch(MessagingException ex) {
			error = ex.toString();
		}
		return error;
	}
}