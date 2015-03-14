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
package com.robin.magic_realm.components.swing;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.*;

import com.robin.game.objects.GameObject;
import com.robin.general.swing.*;
import com.robin.general.util.DateUtility;
import com.robin.general.util.OrderedHashtable;
import com.robin.magic_realm.components.MagicRealmColor;
import com.robin.magic_realm.components.utility.HallOfFame;

public class HallOfFameView extends JPanel {
	
//	private static final Font TITLE_FONT = new Font("Script MT Bold",Font.BOLD,48);
	private static final Font TITLE_FONT = new Font("Dialog",Font.BOLD,48);
	
	private JEditorPane roll;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
	
	private JButton resetHallOfFameButton;
	
	public HallOfFameView() {
		setLayout(new BorderLayout());
		ComponentTools.lockComponentSize(this,800,600);
		initComponents();
	}
	public void initComponents() {
		ImageIcon icon = IconFactory.findIcon("images/logo/rs_logo.jpg");
		JLabel title = new JLabel("Hall of Fame",icon,JLabel.CENTER);
		title.setFont(TITLE_FONT);
		title.setForeground(Color.blue);
		title.setVerticalTextPosition(JLabel.BOTTOM);
		title.setHorizontalTextPosition(JLabel.CENTER);
		add(title,"North");
		
		roll = new JEditorPane();
		roll.setBackground(MagicRealmColor.PALEYELLOW);
		roll.setContentType("text/html");
		JScrollPane sp = new JScrollPane(roll);
		add(sp,"Center");
		
		resetHallOfFameButton = new JButton("Clear Hall of Fame");
		resetHallOfFameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				resetHallOfFame();
			}
		});
		Box box = Box.createHorizontalBox();
//		box.add(Box.createHorizontalGlue());
		box.add(resetHallOfFameButton);
		box.add(Box.createHorizontalGlue());
		add(box,"South");
	}
	
	private void resetHallOfFame() {
		int ret = JOptionPane.showConfirmDialog(
				this,
				"This action is not reversible!\n\nAre you sure you want to clear the Hall of Fame?",
				"Clear Hall of Fame",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if (ret==JOptionPane.YES_OPTION) {
			HallOfFame.reset();
			HallOfFame hof = HallOfFame.getSingleton();
			init(hof);
			repaint();
		}
	}
	
	private String getTable(String title,GameObject goList) {
		StringBuffer sb = new StringBuffer();
		sb.append("<table align=\"center\" width=390 cellspacing=\"2\"><tr><td bgcolor=\"66ff99\" align=\"center\" colspan=\"6\"><h2>");
		sb.append(title);
		sb.append("</td></tr>");
		String header = "<th bgcolor=\"eeeeee\">";
		sb.append(header);
		sb.append("#</th>");
		sb.append(header);
		sb.append("Date</th>");
		sb.append(header);
		sb.append("Player</th>");
		sb.append(header);
		sb.append("Character</th>");
		sb.append(header);
		sb.append("VPs</th>");
		sb.append(header);
		sb.append("Score</th>\n<br>");
		int n=1;
		for (Iterator i=goList.getHold().iterator();i.hasNext();) { // should already be in order by score
			GameObject go = (GameObject)i.next();
			sb.append("<tr>");
			sb.append("<td align=\"center\"><b>");
			sb.append(n++);
			sb.append("</b></td>");
			sb.append("<td align=\"center\">");
			String dateVal = go.getThisAttribute(HallOfFame.GAME_DATE);
			if (dateVal!=null) {
				Date date = DateUtility.convertString2Date(dateVal);
				sb.append(dateFormat.format(date));
			}
			else {
				sb.append("pre-2007");
			}
			sb.append("</td><td>");
			sb.append(go.getThisAttribute(HallOfFame.PLAYER_NAME));
			sb.append("</td><td>");
			sb.append(go.getName());
			sb.append("</td><td align=\"center\">");
			sb.append(go.getThisAttribute(HallOfFame.TOTAL_VPS));
			sb.append("</td><td align=\"center\">");
			sb.append(go.getThisAttribute(HallOfFame.TOTAL_SCORE));
			sb.append("</td>");
			sb.append("</tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}
	private String getNextRow(HallOfFame hof,OrderedHashtable hash) {
		StringBuffer sb = new StringBuffer();
		sb.append("<tr>");
		
		ArrayList list = new ArrayList(hash.orderedKeys());
		int n = list.size();
		
		for (int i=0;i<Math.min(n,2);i++) {
			if (n==1) {
				sb.append("<td valign=\"top\" colspan=\"2\">");
			}
			else {
				sb.append("<td valign=\"top\">");
			}
//			if ((n>=3)
//					|| (n==2 && i!=1)
//					|| (n==1 && i==1)) {
//			if (n>=2) {
				String listName = (String)list.get(0);
				String title = (String)hash.remove(listName);
				sb.append(getTable(title,hof.getHolderFor(listName)));
				list.remove(listName);
//			}
			sb.append("</td>");
		}
		
		sb.append("</tr>");
		return sb.toString();
	}
	
	public void init(HallOfFame hof) {
		StringBuffer sb = new StringBuffer();
		sb.append("<html><body>");
		
		sb.append("<table cellpadding=\"0\">");
		
		OrderedHashtable hash = new OrderedHashtable();
		
		// Do this one separately, so it gets centered on top
		hash.put(HallOfFame.CAT_OVERALL,"Overall");
		sb.append(getNextRow(hof, hash));
		
		// The rest will work out
		hash.put(HallOfFame.CAT_FIGHTERS,"Fighters");
		hash.put(HallOfFame.CAT_MAGIC_USERS,"Magic Users");
		ArrayList names = hof.getAllCharacterNames();
		Collections.sort(names);
		for (Iterator i=names.iterator();i.hasNext();) {
			String name = (String)i.next();
			hash.put(name,name+" Top Ten");
		}
		while(!hash.isEmpty()) {
			sb.append(getNextRow(hof, hash));
		}
		
		sb.append("</table>");
		sb.append("</html></body>");
		roll.setText(sb.toString());
	}
	
	public static void showHallOfFame(JFrame parent) {
		HallOfFameView view = new HallOfFameView();
		view.init(HallOfFame.getSingleton());
		FrameManager.showDefaultManagedFrame(parent,view,"RealmSpeak Hall of Fame",null,false);
	}
	
	public static void main(String[] args) {
		showHallOfFame(new JFrame());
	}
}