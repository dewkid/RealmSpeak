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
package com.robin.general.swing;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import javax.swing.*;

import com.robin.general.graphics.GraphicsUtil;
import com.robin.general.util.RandomNumber;

public class DieRoller extends JComponent {
	
	public static String LOG_ANNOTATION = "_DR463LOG__";

	public static int DEFAULT_DIE_SIZE = 50;
	public static int DEFAULT_DOT_SIZE = 11;
	public static int DEFAULT_BORDER = 5;
	public static int DEFAULT_SPACER = 2;
	public static Color DEFAULT_MOD_COLOR = Color.black;
	public static String MODIFIER_FONT_NAME = "Monospaced";
	
	private int dieSize = DEFAULT_DIE_SIZE;
	private int dotSize = DEFAULT_DOT_SIZE;
	private int border = DEFAULT_BORDER;
	private int spacer = DEFAULT_SPACER;
	private Font modifierFont = null;
	
	private static final Font font = new Font(MODIFIER_FONT_NAME,Font.BOLD,12);
	
	protected ArrayList<Die> dice = new ArrayList<Die>();
	protected Collection actionListeners;
	protected boolean hasRolled = false;
	
	protected boolean showPane = false;
	
	protected int modifier = 0;
	protected String title = null;
	protected Point drawOffset = null;
	
	public DieRoller() {
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent ev) {
				rollDice();
			}
		});
	}
	public DieRoller(String stringResult) {
		this(stringResult,DEFAULT_DIE_SIZE,DEFAULT_DOT_SIZE);
	}
	public DieRoller(String stringResult,int dieSize,int dotSize) {
		this(stringResult,dieSize,dotSize,DEFAULT_BORDER);
	}
	public DieRoller(String stringResult,int dieSize,int dotSize,int borderSize) {
		super();
		this.border = borderSize;
		adjustDieSize(dieSize,dotSize);
		int dollar = stringResult.indexOf("$");
		if (dollar>=0) {
			title = stringResult.substring(dollar+1);
			stringResult = stringResult.substring(0,dollar);
		}
		int amp = stringResult.indexOf("&");
		if (amp>=0) {
			modifier = Integer.valueOf(stringResult.substring(amp+1)).intValue(); // NFE is correct behavior if not a number
			stringResult = stringResult.substring(0,amp);
		}
		StringTokenizer tokens = new StringTokenizer(stringResult,":");
		while(tokens.hasMoreTokens()) {
			String token = tokens.nextToken();
			addDie(token);
		}
		hasRolled = true;
		updateSize();
	}
	public boolean waitingRoll() {
		return !hasRolled;
	}
	public String getDescription() {
		return getDescription(true);
	}
	public String getDescription(boolean showDieName) {
		StringBuffer sb = new StringBuffer();
		for (Die die:dice) {
			if (sb.length()>0) {
				sb.append(" and ");
			}
			sb.append(die.getValue());
			if (showDieName) {
				String name = die.getName();
				if (name.length()>0) {
					sb.append(" (");
					sb.append(name);
					sb.append(")");
				}
			}
		}
		if (modifier!=0) {
			sb.append(", with a modifier of ");
			if (modifier>0) {
				sb.append("+");
			}
			sb.append(modifier);
		}
		return "Rolled "+sb.toString()+".";
	}
	public String getLogAnnotation() {
		if (dice.isEmpty()) {
			return "";
		}
		return LOG_ANNOTATION+getStringResult()+LOG_ANNOTATION;
	}
	public String getStringResult() {
		StringBuffer sb = new StringBuffer();
		for (Die die:dice) {
			if (sb.length()>0) {
				sb.append(":");
			}
			sb.append(die.getStringResult());
		}
		if (modifier!=0) {
			sb.append("&");
			sb.append(String.valueOf(modifier));
		}
		if (title!=null) {
			sb.append("$");
			sb.append(title);
		}
		return sb.toString();
	}
	public void adjustDieSize(int inDieSize,int inDotSize) {
		this.dieSize = inDieSize;
		this.dotSize = inDotSize;
		updateSize();
	}
	public void addActionListener(ActionListener listener) {
		if (actionListeners==null) {
			actionListeners = new ArrayList();
		}
		actionListeners.add(listener);
	}
	public void removeActionListener(ActionListener listener) {
		if (actionListeners!=null) {
			actionListeners.remove(listener);
			if (actionListeners.size()==0) {
				actionListeners = null;
			}
		}
	}
	private void fireActionPerformed() {
		if (actionListeners!=null) {
			ActionEvent ev = new ActionEvent(this,0,"Dice Rolled");
			for (Iterator i=actionListeners.iterator();i.hasNext();) {
				ActionListener listener = (ActionListener)i.next();
				listener.actionPerformed(ev);
			}
		}
	}
	public void reset() {
		hasRolled = false;
		repaint();
	}
	public void rollDice() {
		rollDice(null);
	}
	public void rollDice(String purpose) {
		if (!hasRolled) {
			for (Die die:dice) {
				die.setFace(RandomNumber.getDieRoll());
			}
			hasRolled = true;
			if (rollLogger!=null) {
				rollLogger.addDieRoll(this,purpose);
			}
			fireActionPerformed();
			repaint();
		}
	}
	public void addWhiteDie() {
		addDie(Color.white,Color.black,Die.WHITE);
	}
	public void addRedDie() {
		addDie(Color.red,Color.white,Die.RED);
	}
	private void addDie(Color dieColor,Color dotColor,String name) {
		Die die = new Die(dieSize,dotSize,dieColor,dotColor);
		die.setName(name);
		dice.add(die);
		updateSize();
	}
	public void addDie(String val) {
		Die die = new Die(dieSize,dotSize,val);
		dice.add(die);
		updateSize();
	}
	public int getNumberOfDice() {
		return dice.size();
	}
	/**
	 * If you want the first die, then use dienum=0.  No modifiers are applied here!
	 */
	public int getValue(int dienum) {
		int count = 0;
		for (Die die:dice) {
			if (count==dienum) {
				return die.getValue();
			}
			count++;
		}
		return -1;
	}
	/**
	 * @param dienum		A zero-based index of dice
	 * @param val			An actual pip-count on the die face
	 */
	public void setValue(int dienum,int val) {
		hasRolled = true;
		int count = 0;
		for (Die die:dice) {
			if (count==dienum) {
				die.setFace(val);
				return;
			}
			count++;
		}
		throw new IllegalArgumentException("Invalid dienum: "+dienum+"  Only "+dice.size()+" dice.");
	}
	public void setRed(int dienum) {
		Die die = dice.get(dienum);
		die.setColor(Color.red,Color.white);
		die.setName(Die.RED);
	}
	public void setWhite(int dienum) {
		Die die = dice.get(dienum);
		die.setColor(Color.white,Color.black);
		die.setName(Die.WHITE);
	}
	public void setAllRed() {
		for (Die die:dice) {
			die.setColor(Color.red,Color.white);
			die.setName("red");
		}
	}
	public int getTotal() {
		int total = 0;
		for (Die die:dice) {
			total += die.getValue();
		}
		return total+modifier;
	}
	public int getDieResultCount(int result) {
		int count = 0;
		for (Die die:dice) {
			if (die.getValue()==result) count++;
		}
		return count;
	}
	/**
	 * This includes the modifier
	 */
	public int getHighDieResult() {
		int max = 0;
		for (Die die:dice) {
			if (die.getValue()>max) {
				max = die.getValue();
			}
		}
		return max+modifier;
	}
	public void updateSize() {
		updateSize(2);
	}
	public void updateSize(int shrinkFactor) {
		modifierFont = new Font(MODIFIER_FONT_NAME,Font.BOLD,dieSize-shrinkFactor);
		setFont(modifierFont);
		int width = (dice.size()*dieSize)+((dice.size()-1)*spacer)+(border<<1);
		if (modifier!=0) {
			String modString = String.valueOf(modifier);
			width += (dieSize+spacer)*modString.length();
		}
		int height = dieSize+(border<<1);
		
		if (title!=null) {
			Dimension s = GraphicsUtil.getStringDimension(this,title);
			drawOffset = new Point((s.width-width)>>1,s.height);
			width = Math.max(width,s.width);
			height += s.height;
		}
		Dimension size = new Dimension(width,height);
		
		setPreferredSize(size);
		setMaximumSize(size);
		setMinimumSize(size);
	}
	public void paintComponent(Graphics g) {
		if (showPane) {
			Dimension size = getPreferredSize();
			g.setColor(Color.lightGray);
			g.fillRect(-5,0,size.width+10,size.height);
			g.setColor(DEFAULT_MOD_COLOR);
			g.drawRect(-5,0,size.width+10-1,size.height-1);
		}
		
		int x = border;
		int y = border;
		if (title!=null) {
			g.setColor(DEFAULT_MOD_COLOR);
			g.setFont(getFont());
			
			int sx = drawOffset.x<0?-drawOffset.x:0;
			g.drawString(title,sx,drawOffset.y);
			
			x += drawOffset.x>0?drawOffset.x:0;
			y += drawOffset.y;
		}
		
		for (Die die:dice) {
			die.paintIcon(this,g,x,y);
			x += (dieSize+spacer);
		}
		if (modifier!=0) {
			g.setFont(modifierFont);
			g.setColor(DEFAULT_MOD_COLOR);
			String sign = modifier>0?"+":""; // the minus is part of the number
			g.drawString(sign+modifier,x,y+dieSize-border);
		}
		
		if (!hasRolled) {
			g.setColor(Color.blue);
			g.setFont(font);
			g.drawString("Roll Dice...",border<<1,dieSize>>1);
		}
	}
	public int getModifier() {
		return modifier;
	}
	public void setModifier(int modifier) {
		this.modifier = modifier;
		updateSize();
	}
	public void addModifier(int mod) {
		this.modifier += mod;
		updateSize();
	}
	public ImageIcon getIcon() {
		return new ImageIcon(getImage());
	}
	public Image getImage() {
		Dimension size = getPreferredSize();
		BufferedImage image = new BufferedImage(size.width,size.height,BufferedImage.TYPE_4BYTE_ABGR);
		paintComponent(image.getGraphics());
		return image;
	}
	
	/**
	 * @return the showPane
	 */
	public boolean isShowPane() {
		return showPane;
	}
	/**
	 * @param showPane the showPane to set
	 */
	public void setShowPane(boolean showPane) {
		this.showPane = showPane;
	}
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
		drawOffset = null;
		updateSize();
	}
	public String toString() {
		return getDescription(false);
	}
	
	public static ArrayList breakOutRollers(String in,int dieSize,int dotSize) {
		ArrayList list = new ArrayList();
		int n;
		boolean addingString = true;
		if (in.startsWith(LOG_ANNOTATION)) {
			in = in.substring(LOG_ANNOTATION.length());
			addingString = false;
		}
		while((n=in.indexOf(LOG_ANNOTATION))>=0) {
			if (n>0) {
				if (addingString) {
					list.add(in.substring(0,n));
				}
				else {
					DieRoller roller = new DieRoller(in.substring(0,n),dieSize,dotSize,0);
					list.add(roller);
				}
				in = in.substring(n+LOG_ANNOTATION.length());
			}
			addingString = !addingString;
		}
		list.add(in);
		return list;
	}
	public int getDieBorder() {
		return border;
	}
	public void setDieBorder(int border) {
		this.border = border;
	}
	
	public static void __main(String[] args) {
		StringBuffer sb = new StringBuffer();
		sb.append("This is a die roller here: ");
		DieRoller roller = new DieRoller();
//		roller.addWhiteDie();
//		roller.addWhiteDie();
		roller.rollDice();
		sb.append(roller.getLogAnnotation());
		sb.append(", but does that work?");
		
		System.out.println(sb.toString());
		ArrayList list = DieRoller.breakOutRollers(sb.toString(),10,2);
		for (Iterator i=list.iterator();i.hasNext();) {
			System.out.println(i.next());
		}
	}
	/**
	 * For testing only
	 */
	public static void _main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
			final DieRoller roller = new DieRoller();
//roller.adjustDieSize(25,6);
			roller.addWhiteDie();
			roller.addWhiteDie();
			roller.addWhiteDie();
			roller.addRedDie();
			roller.addRedDie();
			roller.setModifier(1);
			roller.setBorder(BorderFactory.createLineBorder(Color.black));
			roller.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					System.out.println("Die Total = "+roller.getTotal());
				}
			});
			System.out.println(roller.getLogAnnotation());
		frame.getContentPane().add(roller,"North");
		frame.setSize(400,400);
		frame.setVisible(true);
	}
	public static void main(String[] args) {
		DieRoller roller = new DieRoller();
		roller.addWhiteDie();
		int total = 0;
		int[] bin = new int[6];
		NumberFormat format = DecimalFormat.getPercentInstance();
		while(true) {
			roller.reset();
			roller.rollDice();
			int val = roller.getTotal();
			bin[val-1]++;
			total++;

			System.out.println(total+":  ");
			for (int i=0;i<6;i++) {
				double n = (double)bin[i]/(double)total;
				System.out.print(format.format(n));
				System.out.print("% ");
			}
			System.out.println();
		}
	}
	
	private static DieRollerLoggable rollLogger = null;
	public static void setDieRollerLog(DieRollerLoggable logger) {
		rollLogger = logger;
	}
	public static DieRollerLoggable getDieRollerLog() {
		return rollLogger;
	}
	public static void stopDieRollerLog() {
		rollLogger = null;
	}
	public static boolean isLoggingDieRolls() {
		return rollLogger!=null;
	}
}