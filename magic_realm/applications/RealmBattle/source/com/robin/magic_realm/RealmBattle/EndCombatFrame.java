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
package com.robin.magic_realm.RealmBattle;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.*;

import com.robin.game.server.GameClient;
import com.robin.magic_realm.components.utility.RealmDirectInfoHolder;

public class EndCombatFrame extends JFrame {
	
	public static long cum_id = 1L;
	
	private long id;
	
	private CombatFrame parent;
	private ArrayList playersToRespond;
	
	private JTextArea responseArea;
	private Hashtable responseHash;
	
	private JButton closeButton;
	private JButton cancelEndButton;
	
	public EndCombatFrame(CombatFrame parent,ArrayList playersToRespond) {
		this.id = cum_id++;
		this.parent = parent;
		this.playersToRespond = playersToRespond;
		responseHash = new Hashtable();
		initComponents();
	}
	public long getId() {
		return id;
	}
	private void initComponents() {
		setSize(400,400);
		setTitle("End Combat");
		setLocationRelativeTo(parent);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(new JLabel("Waiting for responses..."),"North");
		
		responseArea = new JTextArea(10,40);
		responseArea.setEditable(false);
		responseArea.setLineWrap(false);
		getContentPane().add(new JScrollPane(responseArea),"Center");
		
		Box box = Box.createHorizontalBox();
		cancelEndButton = new JButton("Cancel");
		cancelEndButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				forceCancel();
				doClose();
			}
		});
		box.add(cancelEndButton);
		closeButton = new JButton("Done");
		closeButton.setEnabled(false);
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				doClose();
			}
		});
		box.add(Box.createHorizontalGlue());
		box.add(closeButton);
		getContentPane().add(box,"South");
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}
	private void forceCancel() {
		responseHash.clear();
	}
	private void doClose() {
		setVisible(false);
		dispose();
		String unanimous = getUnanimousResponse();
		if (unanimous!=null && RealmDirectInfoHolder.QUERY_RESPONSE_YES.equals(unanimous)) {
			parent.endCombatNow();
		}
		else {
			parent.cancelEndCombat();
		}
	}
	public void updateResponse(String respondingPlayer,String response) {
		int colon = response.indexOf(":");
		if (colon>=0) {
			long responseid = Long.valueOf(response.substring(0,colon)).longValue();
			if (responseid==id) { // This prevents earlier END requests from getting out of sync
				response = response.substring(colon+1);
				responseHash.put(respondingPlayer,response);
				
				boolean yes = response.equals(RealmDirectInfoHolder.QUERY_RESPONSE_YES);
				responseArea.append(respondingPlayer+" says "+(yes?"yes":"no")+".\n");
				GameClient.broadcastClient(respondingPlayer,yes?"Approves END Combat":"Cancels END Combat");
				
				if (responseHash.size()==playersToRespond.size()) {
					closeButton.setEnabled(true);
				}
			}
		}
	}
	private String getUnanimousResponse() {
		ArrayList all = new ArrayList();
		for (Iterator i=responseHash.values().iterator();i.hasNext();) {
			String response = (String)i.next();
			if (!all.contains(response)) {
				all.add(response);
			}
		}
		if (all.size()==1) { // unanimous response
			return (String)all.get(0);
		}
		return null;
	}
}