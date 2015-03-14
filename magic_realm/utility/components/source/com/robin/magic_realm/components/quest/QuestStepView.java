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
package com.robin.magic_realm.components.quest;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.robin.game.objects.GameData;
import com.robin.general.graphics.GraphicsUtil;
import com.robin.general.swing.ComponentTools;
import com.robin.general.swing.ImageCache;
import com.robin.magic_realm.components.utility.Constants;

public class QuestStepView extends JComponent {
	
	private static int BORDER = 5;
	private static int SQUARE = 30;
	
	private static ImageIcon selection = ImageCache.getIcon("quests/tokenselect",QuestStepState.tokenSizePercent);
	
	private static Font idFont = new Font("Dialog",Font.BOLD,12);
	
	ArrayList<QuestStepToken> tokens;
	private int orientation = SwingConstants.HORIZONTAL;
	private int maxRank;
	private int maxDisplayOrder;
	
	private ArrayList<ChangeListener> changeListeners;
	
	public QuestStepView(){
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent ev) {
				QuestStep step = getStepAtPoint(ev.getPoint());
				if (step!=null) {
					setSelectedStep(step);
					fireStateChanged();
				}
			}
		});		
	}
	public void addChangeListener(ChangeListener listener) {
		if (changeListeners==null) changeListeners = new ArrayList<ChangeListener>();
		if (!changeListeners.contains(listener)) {
			changeListeners.add(listener);
		}
	}
	public void removeChangeListener(ChangeListener listener) {
		if (changeListeners==null) return;
		changeListeners.remove(listener);
		if (changeListeners.size()==0) changeListeners=null;
	}
	private void fireStateChanged() {
		if (changeListeners==null) return;
		ChangeEvent ev = new ChangeEvent(this);
		for (ChangeListener listener:changeListeners) {
			listener.stateChanged(ev);
		}
	}
	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}
	public void setSelectedStep(QuestStep step) {
		if (tokens==null) return;
		for(QuestStepToken token:tokens) {
			token.setSelected(token.getStep()==step);
		}
		repaint();
	}
	public void setFirstSelectedStep() {
		if (tokens==null || tokens.size()==0) return;
		setSelectedStep(tokens.get(0).getStep());
		fireStateChanged();
	}
	public QuestStep getSelectedStep() {
		for(QuestStepToken token:tokens) {
			if (token.isSelected()) return token.getStep();
		}
		return null;
	}
	public void updateSteps(ArrayList<QuestStep> steps) {
		rebuildTokens(steps);
		layoutTokens();
		repaint();
	}
	public QuestStep getStepAtPoint(Point p) {
		if (tokens!=null) {
			for(QuestStepToken token:tokens) {
				Rectangle r = new Rectangle(token.drawX,token.drawY,SQUARE,SQUARE);
				if (r.contains(p)) {
					return token.getStep();
				}
			}
		}
		return null;
	}
	public void paintComponent(Graphics g1) {
		super.paintComponent(g1);
		if (tokens==null) return;
		Graphics2D g = (Graphics2D)g1;
		
		Stroke old = g.getStroke();
		
		// Draw connectors
		g.setColor(Color.black);
		g.setStroke(Constants.THICK_STROKE);
		for(QuestStepToken token:tokens) {
			if (token.getStep().isOrigin()) continue;
			int x1 = token.getDrawX()+(SQUARE>>1); 
			int y1 = token.getDrawY()+(SQUARE>>1); 
			for(QuestStepToken otherToken:tokens) {
				if (token==otherToken) continue;
				if (token.getViewRank()!=otherToken.viewRank+1) continue;
				boolean required = token.getStep().requires(otherToken.getStep());
				boolean onfail = token.getStep().requiresFail(otherToken.getStep());
				boolean bothVirtual = token.isVirtual() && otherToken.isVirtual();
				if (((required || onfail) && !bothVirtual)
						|| token.getStep()==otherToken.getStep()) { // virtual match
					// draw a line...
					Color c = onfail ? Color.red : token.getStep().getLogicType().getColor();
					g.setColor(c);
					int x2 = otherToken.getDrawX()+(SQUARE>>1); 
					int y2 = otherToken.getDrawY()+(SQUARE>>1);
					g.drawLine(x1,y1,x2,y2);
				}
			}
		}
		g.setStroke(old);
		
		// Draw tokens
		g.setFont(idFont);
		for(QuestStepToken token:tokens) {
			if (token.isVirtual()) {
				if (SHOW_VIRTUAL) {
					g.setColor(Color.white);
					g.fillOval(token.getDrawX()+2,token.getDrawY()-1,SQUARE,SQUARE);
					GraphicsUtil.drawCenteredString(g1,token.getDrawX()+2,token.getDrawY()-1,SQUARE,SQUARE,""+token.getStep().getId());
					g.setColor(Color.blue);
					GraphicsUtil.drawCenteredString(g1,token.getDrawX()+1,token.getDrawY()-2,SQUARE,SQUARE,""+token.getStep().getId());
				}
				continue;
			}
			QuestStepState state = token.getStep().getState();
			g.drawImage(state.getIcon().getImage(),token.getDrawX(),token.getDrawY(),this);
			if (token.isSelected()) {
				g.drawImage(selection.getImage(),token.getDrawX(),token.getDrawY(),this);
			}
			if (state == QuestStepState.None) {
				g.setColor(Color.white);
				GraphicsUtil.drawCenteredString(g1,token.getDrawX()+2,token.getDrawY()-1,SQUARE,SQUARE,""+token.getStep().getId());
				g.setColor(Color.blue);
				GraphicsUtil.drawCenteredString(g1,token.getDrawX()+1,token.getDrawY()-2,SQUARE,SQUARE,""+token.getStep().getId());
			}
		}
	}
	private void layoutTokens() {
		int rankSize = (int)((maxRank+1)*SQUARE)+(BORDER<<1) - SQUARE;
		int displayOrderSize = ((maxDisplayOrder+1)*SQUARE)+(BORDER<<2);
		int width = orientation==SwingConstants.HORIZONTAL?rankSize:displayOrderSize;
		int height = orientation==SwingConstants.HORIZONTAL?displayOrderSize:rankSize;
		ComponentTools.lockComponentSize(this,width,height);
		setBorder(BorderFactory.createEtchedBorder());
		Hashtable<Integer,Integer> hash = new Hashtable<Integer,Integer>();
		for(QuestStepToken token:tokens) {
			int viewRank = token.getViewRank();
			if (hash.containsKey(viewRank)) {
				hash.put(viewRank,hash.get(viewRank)+1);
			}
			else {
				hash.put(viewRank,1);
			}
		}
		for(QuestStepToken token:tokens) {
			token.initOrientation(orientation,BORDER,SQUARE,displayOrderSize,hash.get(token.getViewRank()));
		}
	}
	private void rebuildTokens(ArrayList<QuestStep> steps) {
		maxRank = 0;
		maxDisplayOrder = 0;
		tokens = new ArrayList<QuestStepToken>();
		if (steps==null) return;
		steps = new ArrayList<QuestStep>(steps); // make a copy that we can modify
		ArrayList<QuestStepToken> origins = new ArrayList<QuestStepToken>();
		int displayOrder=0;
		ArrayList<QuestStep> toRemove = new ArrayList<QuestStep>();
		for(QuestStep step:steps) {
			if (step.isOrigin()) {
				QuestStepToken token = new QuestStepToken(step);
				token.setViewRank(0);
				maxDisplayOrder = Math.max(displayOrder,maxDisplayOrder);
				token.setDisplayOrder(displayOrder++);
				origins.add(token);
				toRemove.add(step);
			}
		}
		steps.removeAll(toRemove);
		int currentRank = 1;
		while(origins.size()>0) {
			tokens.addAll(origins);
			boolean allVirtual = true;
			for(QuestStepToken test:origins) {
				if (!test.isVirtual()) {
					allVirtual = false;
					break;
				}
			}
			if (allVirtual) break;
			displayOrder = 0;
			ArrayList<QuestStep> virtualCovered = new ArrayList<QuestStep>();
			ArrayList<QuestStepToken> newTokens = new ArrayList<QuestStepToken>();
			for(QuestStepToken token:origins) {
				//if (token.isVirtual()) continue;
				toRemove.clear();
				for(QuestStep step:steps) {
					boolean sameAsToken = step.getId()==token.getStep().getId();
					boolean tokenIsRequiredByStep = step.requires(token.getStep()) || step.requiresFail(token.getStep());
					if (!sameAsToken && !tokenIsRequiredByStep) continue;
					if (tokenIsRequiredByStep && token.isVirtual()) continue;
					if (virtualCovered.contains(step)) continue;
					maxDisplayOrder = Math.max(displayOrder,maxDisplayOrder);
					QuestStepToken newToken = new QuestStepToken(step);
					newToken.setViewRank(currentRank);
					newToken.setDisplayOrder(displayOrder++);
					newToken.setVirtual(!newToken.allRequiredPresent(tokens));
					newTokens.add(newToken);
					maxRank = currentRank;
					if (newToken.isVirtual()) {
						virtualCovered.add(step);
					}
					else {
						toRemove.add(step);
					}
				}
				steps.removeAll(toRemove);
			}
			origins = newTokens;
			currentRank++;
		}
//		System.out.println("------------------");
//		System.out.println(maxRank);
//		for(QuestStepToken token:tokens) {
//			System.out.println(token);
//		}
	}
	private static ArrayList<QuestStep> getTestSteps1() {
		GameData data = new GameData();
		ArrayList<QuestStep> steps = new ArrayList<QuestStep>();
		QuestStep step1 = new QuestStep(data.createNewObject());
		step1.setName("Step 1");
		step1.setId(1);
		QuestStep step2 = new QuestStep(data.createNewObject());
		step2.setName("Step 2");
		step2.setId(2);
		QuestStep step3 = new QuestStep(data.createNewObject());
		step3.setName("Step 3");
		step3.setId(3);
		QuestStep step4 = new QuestStep(data.createNewObject());
		step4.setName("Step 4");
		step4.setId(4);
		QuestStep step5 = new QuestStep(data.createNewObject());
		step5.setName("Step 5");
		step5.setId(5);
		QuestStep step6 = new QuestStep(data.createNewObject());
		step6.setName("Step 6");
		step6.setId(6);
		step2.addRequiredStep(step1);
		step3.addRequiredStep(step1);
		step4.addRequiredStep(step1);
		step5.addRequiredStep(step3);
		step6.addRequiredStep(step2);
		step6.addRequiredStep(step4);
		step6.addRequiredStep(step5);
		steps.add(step1);
		steps.add(step2);
		steps.add(step3);
		steps.add(step4);
		steps.add(step5);
		steps.add(step6);
		return steps;
	}
	private static ArrayList<QuestStep> getTestSteps2() {
		GameData data = new GameData();
		ArrayList<QuestStep> steps = new ArrayList<QuestStep>();
		QuestStep step1 = new QuestStep(data.createNewObject());
		step1.setName("Step 1");
		step1.setId(1);
		QuestStep step2 = new QuestStep(data.createNewObject());
		step2.setName("Step 2");
		step2.setId(2);
		QuestStep step3 = new QuestStep(data.createNewObject());
		step3.setName("Step 3");
		step3.setId(3);
		QuestStep step4 = new QuestStep(data.createNewObject());
		step4.setName("Step 4");
		step4.setId(4);
		QuestStep step5 = new QuestStep(data.createNewObject());
		step5.setName("Step 5");
		step5.setId(5);
		QuestStep step6 = new QuestStep(data.createNewObject());
		step6.setName("Step 6");
		step6.setId(6);
		step3.addRequiredStep(step1);
		step4.addRequiredStep(step2);
		step5.addRequiredStep(step3);
		step5.addRequiredStep(step4);
		step6.addRequiredStep(step5);
		steps.add(step1);
		steps.add(step2);
		steps.add(step3);
		steps.add(step4);
		steps.add(step5);
		steps.add(step6);
		return steps;
	}
	private static ArrayList<QuestStep> getTestSteps3() {
		GameData data = new GameData();
		ArrayList<QuestStep> steps = new ArrayList<QuestStep>();
		QuestStep step1 = new QuestStep(data.createNewObject());
		step1.setName("Step 1");
		step1.setId(1);
		QuestStep step2 = new QuestStep(data.createNewObject());
		step2.setName("Step 2");
		step2.setId(2);
		QuestStep step3 = new QuestStep(data.createNewObject());
		step3.setName("Step 3");
		step3.setId(3);
		QuestStep step4 = new QuestStep(data.createNewObject());
		step4.setName("Step 4");
		step4.setId(4);
		QuestStep step5 = new QuestStep(data.createNewObject());
		step5.setName("Step 5");
		step5.setId(5);
		QuestStep step6 = new QuestStep(data.createNewObject());
		step6.setName("Step 6");
		step6.setId(6);
		step3.addRequiredStep(step1);
		step4.addRequiredStep(step1);
		step4.addRequiredStep(step2);
		step5.addRequiredStep(step3);
		step5.addRequiredStep(step2);
		step6.addRequiredStep(step4);
		step6.addRequiredStep(step5);
		steps.add(step1);
		steps.add(step2);
		steps.add(step3);
		steps.add(step4);
		steps.add(step5);
		steps.add(step6);
		return steps;
	}
	private static ArrayList<QuestStep> getTestSteps4() {
		GameData data = new GameData();
		ArrayList<QuestStep> steps = new ArrayList<QuestStep>();
		QuestStep step1 = new QuestStep(data.createNewObject());
		step1.setName("Step 1");
		step1.setId(1);
		QuestStep step2 = new QuestStep(data.createNewObject());
		step2.setName("Step 2");
		step2.setId(2);
		QuestStep step3 = new QuestStep(data.createNewObject());
		step3.setName("Step 3");
		step3.setId(3);
		QuestStep step4 = new QuestStep(data.createNewObject());
		step4.setName("Step 4");
		step4.setId(4);
		QuestStep step5 = new QuestStep(data.createNewObject());
		step5.setName("Step 5");
		step5.setId(5);
		QuestStep step6 = new QuestStep(data.createNewObject());
		step6.setName("Step 6");
		step6.setId(6);
		step2.addRequiredStep(step1);
		step3.addRequiredStep(step1);
		step4.addRequiredStep(step2);
		step5.addRequiredStep(step4);
		step6.addRequiredStep(step1);
		step6.addRequiredStep(step3);
		step6.addRequiredStep(step5);
		steps.add(step1);
		steps.add(step2);
		steps.add(step3);
		steps.add(step4);
		steps.add(step5);
		steps.add(step6);
		return steps;
	}
	private static ArrayList<QuestStep> getTestSteps5() {
		GameData data = new GameData();
		ArrayList<QuestStep> steps = new ArrayList<QuestStep>();
		QuestStep step1 = new QuestStep(data.createNewObject());
		step1.setName("Step 1");
		step1.setId(1);
		QuestStep step2 = new QuestStep(data.createNewObject());
		step2.setName("Step 2");
		step2.setId(2);
		QuestStep step3 = new QuestStep(data.createNewObject());
		step3.setName("Step 3");
		step3.setId(3);
		QuestStep step4 = new QuestStep(data.createNewObject());
		step4.setName("Step 4");
		step4.setId(4);
		QuestStep step5 = new QuestStep(data.createNewObject());
		step5.setName("Step 5");
		step5.setId(5);
		QuestStep step6 = new QuestStep(data.createNewObject());
		step6.setName("Step 6");
		step6.setId(6);
		step2.addRequiredStep(step1);
		step3.addRequiredStep(step2);
		step4.addRequiredStep(step3);
		step5.addRequiredStep(step2);
		step5.addRequiredStep(step4);
		step6.addRequiredStep(step5);
		steps.add(step1);
		steps.add(step2);
		steps.add(step3);
		steps.add(step4);
		steps.add(step5);
		steps.add(step6);
		return steps;
	}
	private static ArrayList<QuestStep> getTestSteps6() {
		GameData data = new GameData();
		ArrayList<QuestStep> steps = new ArrayList<QuestStep>();
		QuestStep step1 = new QuestStep(data.createNewObject());
		step1.setName("Step 1");
		step1.setId(1);
		QuestStep step2 = new QuestStep(data.createNewObject());
		step2.setName("Step 2");
		step2.setId(2);
		QuestStep step3 = new QuestStep(data.createNewObject());
		step3.setName("Step 3");
		step3.setId(3);
		step2.addRequiredStep(step1);
		step2.addRequiredStep(step3);
		step3.addRequiredStep(step2);
		steps.add(step1);
		steps.add(step2);
		steps.add(step3);
		return steps;
	}
	private static boolean SHOW_VIRTUAL = false;
	public static void main(String[] args) {
		QuestStepView view = new QuestStepView();
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(view);
		box.add(Box.createHorizontalGlue());
		
		view.updateSteps(getTestSteps1());
		view.setPreferredSize(new Dimension(400,400));
		JOptionPane.showMessageDialog(null,box);
		
		view.updateSteps(getTestSteps2());
		view.setPreferredSize(new Dimension(400,400));
		JOptionPane.showMessageDialog(null,box);
		
		view.updateSteps(getTestSteps3());
		view.setPreferredSize(new Dimension(400,400));
		JOptionPane.showMessageDialog(null,box);
		
		view.updateSteps(getTestSteps4());
		view.setPreferredSize(new Dimension(400,400));
		JOptionPane.showMessageDialog(null,box);
		
		view.updateSteps(getTestSteps5());
		view.setPreferredSize(new Dimension(400,400));
		JOptionPane.showMessageDialog(null,box);
		
		view.updateSteps(getTestSteps6());
		view.setPreferredSize(new Dimension(400,400));
		JOptionPane.showMessageDialog(null,box);
		
		System.exit(0);
	}
}