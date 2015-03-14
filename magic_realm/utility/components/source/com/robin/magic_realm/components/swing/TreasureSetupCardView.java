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
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

import com.robin.game.objects.*;
import com.robin.general.graphics.GraphicsUtil;
import com.robin.general.graphics.TextType;
import com.robin.general.graphics.TextType.Alignment;
import com.robin.general.swing.*;
import com.robin.general.util.HashLists;
import com.robin.magic_realm.components.*;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.GameWrapper;
import com.robin.magic_realm.components.wrapper.HostPrefWrapper;

public class TreasureSetupCardView extends JComponent {
	
	private static final Color HIGHLIGHT_COLOR = Color.white;
	private static final int HIGHLIGHT_INTENSITY = 60; // percent
	
	private static final int SPACING = 5;
	private static final int TEXT_SPACING = 10;
	private static final int LEFT_BORDER = 60;
	private static final int RIGHT_BORDER = 20;
	
	private static final int NON_MD_LIST_WIDTH = 140;
	private static final int NON_MD_LIST_HEIGHT = 21;
	private static final int NON_MD_LIST_COLUMN_LENGTH = 27;
	
	private static final Color PANEL_COLOR = UIManager.getColor("Panel.background");
	private static final Color PANEL_SHADOW = new Color(0,0,0,80);
	
	private static final Font SUMMON_FONT = new Font("Dialog",Font.BOLD,10);
	private static final Font LABEL_FONT = new Font("Dialog",Font.BOLD,12);
	private static final Font COUNT_FONT = new Font("Dialog",Font.BOLD,24);
	
	private static final Stroke THICK_STROKE = new BasicStroke(3);
	
	private GameData data;
	private GameWrapper game;
	private ArrayList sections;
	private Dimension cardSize;
	private Hashtable sectionRowHash;
	private ArrayList nonMdList;
	
	private Image image;
	
	private ArrayList<Rectangle> drawRectList;
	private ArrayList<GameObject> drawContainerList;
	
	private HostPrefWrapper hostPrefs;
	
	private int mouseHoverIndex = -1;
	private boolean rightClick;
	private GameObject clickViewObject = null;
	
	private String title = "Treasure Setup Card";
	private String boardKey;
	private String playerName;
	
	public TreasureSetupCardView(GameData data,String playerName) {
		this(data,playerName,null);
	}
	public TreasureSetupCardView(GameData data,String playerName,String boardKey) {
		this.data = data;
		this.boardKey = boardKey;
		this.playerName = playerName;
		game = GameWrapper.findGame(data);
		hostPrefs = HostPrefWrapper.findHostPrefs(data);
		if (boardKey!=null && !boardKey.endsWith(Constants.BOARD_NUMBER)) {
			title = title + " - " + boardKey.substring(boardKey.length()-1);
		}
		initView();
	}
	public ArrayList<Rectangle> getDrawRectList() {
		return drawRectList;
	}
	public ArrayList<GameObject> getDrawContainerList() {
		return drawContainerList;
	}
	public String toString() {
		return title;
	}
	private void initView() {
		// Get all the section objects
		GamePool pool = new GamePool(data.getGameObjects());
		ArrayList query = new ArrayList();
		query.add("ts_section");
		query.add(hostPrefs.getGameKeyVals());
		if (boardKey!=null) {
			query.add(boardKey);
		}
		ArrayList<GameObject> list = pool.find(query);
		
		// Sort by section, monster_die, summon, and finally box_num
		Collections.sort(list,new Comparator<GameObject>() {
			public int compare(GameObject go1,GameObject go2) {
				int ret = 0;
				String s1 = go1.getThisAttribute("ts_section");
				String s2 = go2.getThisAttribute("ts_section");
				ret = s1.compareTo(s2);
				if (ret==0) {
					int md1 = go1.getThisInt("monster_die");
					int md2 = go2.getThisInt("monster_die");
					ret = md1-md2;
					if (ret==0) {
						String sm1 = go1.getThisAttribute("summon");
						if (sm1==null) sm1 = go1.getName();
						String sm2 = go2.getThisAttribute("summon");
						if (sm2==null) sm2 = go2.getName();
						ret = sm2.compareTo(sm1);
						if (ret==0) {
							int bn1 = go1.getThisInt("box_num");
							int bn2 = go2.getThisInt("box_num");
							ret = bn1-bn2;
						}
					}
				}
				return ret;
			}
		});
		
		// Hash by section and monster die
		HashLists hash = new HashLists();
		sections = new ArrayList();
		for (Iterator i=list.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			String section = go.getThisAttribute("ts_section");
			if (!sections.contains(section)) {
				sections.add(section);
			}
			String key;
			if (go.hasThisAttribute("monster_die") && !go.hasThisAttribute("ts_sidebar")) {
				key = section+go.getThisAttribute("monster_die");
			}
			else {
				// Non-monster die entries (like TWT and Valley Dwellings)
				key = section;
			}
			
			hash.put(key,go);
		}
		
		// Take each subsection (section+monster_die) and divide into groups based on summon string
		sectionRowHash = new Hashtable();
		for (int n=1;n<=6;n++) {
			for (Iterator i=sections.iterator();i.hasNext();) {
				String section = (String)i.next();
				String key = section+n;
				ArrayList row = hash.getList(key);
				if (row!=null) {
					HashLists groups = new HashLists();
					for (Iterator t=row.iterator();t.hasNext();) {
						GameObject go = (GameObject)t.next();
						String summon = go.getThisAttribute("summon");
						if (summon==null) {
							summon = go.getThisAttribute("dwelling");
							if (summon==null) {
								summon = go.getName();
							}
						}
//System.out.println("summon = "+summon+" ... "+go.getName());
						groups.put(summon,go);
					}
//					System.out.println(groups.keySet());
					sectionRowHash.put(key,groups);
				}
			}
		}
		
		nonMdList = new ArrayList();
		for (Iterator i=sections.iterator();i.hasNext();) {
			String section = (String)i.next();
			String key = section;
			ArrayList l = hash.getList(key);
			if (l!=null) {
				Collections.sort(l,new Comparator() {
					public int compare(Object o1,Object o2) {
						GameObject go1 = (GameObject)o1;
						GameObject go2 = (GameObject)o2;
						return go1.getName().compareTo(go2.getName());
					}
				});
				for(Iterator n=l.iterator();n.hasNext();) {
					GameObject go = (GameObject)n.next();
					if (!go.hasThisAttribute(CacheChitComponent.DEPLETED_CACHE) && go.hasThisAttribute("ts_color")) {
						nonMdList.add(go);
					}
				}
			}
		}
		
		// Now we have the sectionRowHash keyed on subsection, and hashing HashLists objects which divide summon groups
		int height = SPACING+((ChitComponent.T_CHIT_SIZE+SPACING+TEXT_SPACING+SPACING)*6);
		
		int maxwidth = 0;
		for (int n=1;n<=6;n++) {
//System.out.print("Row "+n+": ");
			int width = 0;
			for (Iterator i=sections.iterator();i.hasNext();) {
				String section = (String)i.next();
				String key = section+n;
				HashLists groups = (HashLists)sectionRowHash.get(key);
				if (groups!=null) {
					for (Iterator t=groups.values().iterator();t.hasNext();) {
						ArrayList group = (ArrayList)t.next();
						width += (SPACING<<1);
//System.out.print(" ["+section+"]-");
						for (Iterator g=group.iterator();g.hasNext();) {
							GameObject go = (GameObject)g.next();
							String size = getChitSizeAttribute(go);
							width += ChitComponent.getDimensionForSize(size).width;
//System.out.print(size);
						}
					}
				}
			}
			maxwidth = Math.max(width,maxwidth);
//System.out.println();
		}
		
		int nonMdListColumns = ((nonMdList.size()-1)/NON_MD_LIST_COLUMN_LENGTH)+1;
		
		maxwidth += ((NON_MD_LIST_WIDTH+RIGHT_BORDER) * nonMdListColumns); // room for nonMdList
		
		cardSize = new Dimension(maxwidth+LEFT_BORDER+RIGHT_BORDER,height);
		setPreferredSize(cardSize);
		setSize(cardSize);
	}
	public void reset() {
		mouseHoverIndex = -1;
		clickViewObject = null;
		initView();
		image = getImage();
	}
	private String getChitSizeAttribute(GameObject go) {
		String size = go.getThisAttribute("ts_size");
		if (!"S+".equals(size) && !"HCARD".equals(size) && RealmComponent.isDisplayStyleFrenzel()) {
			return "H";
		}
		return size;
	}
	public void paint(Graphics g1) {
		Graphics2D g = (Graphics2D)g1;
		g.drawImage(image,0,0,null);
		if (clickViewObject!=null) {
			drawClickViewObject(g);
		}
		else if (mouseHoverIndex>=0) {
			Rectangle r = drawRectList.get(mouseHoverIndex);
			g.setStroke(THICK_STROKE);
			g.setColor(Color.yellow);
			g.draw(r);
			g.setFont(COUNT_FONT);
			GraphicsUtil.drawCenteredString(g,r,"VIEW");
		}
	}
	private void drawClickViewObject(Graphics2D g) {
		int border = 50;
		int subBorder = 10;
		int spacing = 5;
		int title = 40;
		int shadow = 10;

		JViewport viewport = (JViewport)getParent();
		Rectangle vr = viewport.getViewRect();
		Rectangle displayRect = new Rectangle(vr.x+border,vr.y+border,vr.width-(border<<1),vr.height-(border<<1));
		
		// Draw panel base with shadow
		g.setColor(PANEL_SHADOW);
		g.fillRect(displayRect.x+shadow,displayRect.y+displayRect.height,displayRect.width,shadow);
		g.fillRect(displayRect.x+displayRect.width,displayRect.y+shadow,shadow,displayRect.height-shadow);
		g.setColor(PANEL_COLOR);
		g.fill(displayRect);
		g.setColor(Color.black);
		Stroke old = g.getStroke();
		g.setStroke(THICK_STROKE);
		g.draw(displayRect);
		g.setStroke(old);
		
		// Draw panel title bar
		g.setColor(Color.blue);
		g.fillRect(displayRect.x,displayRect.y,displayRect.width,title);
		g.setColor(Color.white);
		g.setFont(COUNT_FONT);
		String titleString = clickViewObject.getName() + (rightClick?" (Flipsides)":"");
		GraphicsUtil.drawCenteredString(g,displayRect.x,displayRect.y,displayRect.width,title,titleString);
		
		if (!rightClick) {
			g.setFont(LABEL_FONT);
			g.setColor(Color.blue);
			GraphicsUtil.drawCenteredString(g,displayRect.x,displayRect.y+displayRect.height-title,displayRect.width,title,"Right-click (or CTRL-click) to view chit flipsides.");
		}
		
		// Populate panel
		RealmComponent clickViewRc = RealmComponent.getRealmComponent(clickViewObject);
		int x = vr.x + border + subBorder;
		int y = vr.y + border + subBorder + title;
		int maxH = 0;
		ArrayList<RealmComponent> contents = new ArrayList<RealmComponent>();
		for (Iterator i=clickViewObject.getHold().iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			RealmComponent rc = RealmComponent.getRealmComponent(go);
			contents.add(rc);
			if (rc.isNative()) {
				RealmComponent horse = (RealmComponent)rc.getHorse();
				if (horse!=null) {
					contents.add(horse);
				}
			}
			else if (rc.isMonster()) {
				MonsterChitComponent monster = (MonsterChitComponent)rc;
				RealmComponent part = monster.getWeapon();
				if (part!=null) {
					contents.add(part);
				}
			}
		}
		boolean showAlways = game.hasBeenRevealed() || playerName.equals(clickViewObject.getThisAttribute("ts_cansee"));
		if (showAlways && clickViewRc.isCacheChit()) {
			CacheChitComponent cache = (CacheChitComponent)clickViewRc;
			if (cache.getGold()>0) {
				y += 20;
				g.setFont(Constants.FORTRESS_FONT);
				g.setColor(Color.black);
				g.drawString(((int)cache.getGold())+" Gold",x,y);
				y += (maxH+spacing);
			}
		}
		for (RealmComponent rc:contents) {
			Dimension d = rc.getSize();
			if ((x+d.width)>(vr.x + vr.width-border-subBorder)) {
				y += (maxH+spacing);
				x = vr.x + border + subBorder;
			}
			Image rcImage = null;
			if (rc.isCard()) {
				CardComponent card = (CardComponent)rc;
				boolean flip = card.isFaceUp();
				// Guarantee all cards are face down until reveal!!
				if (showAlways) {
					flip = !flip;
				}
				
				if (!showAlways
						&& card.getGameObject().hasThisAttribute("nosecret")
						&& card.getGameObject().hasThisAttribute(Constants.TREASURE_SEEN)) {
					// Enchanted cards that have been seen are always face up!
					flip = !flip;
				}
				
				card.setNextAddInfo(true);
				if (flip) {
					rcImage = card.getFlipSideImage();
				}
				else {
					rcImage = card.getImage();
				}
			}
			else {
				if (rc.isChit() && rightClick) {
					ChitComponent chit = (ChitComponent)rc;
					if (chit instanceof GoldSpecialChitComponent) {
						GameObject chitGo = chit.getGameObject();
						GameObject other = data.getGameObject(Long.valueOf(chitGo.getThisAttribute("pairid")));
						ChitComponent otherRc = (ChitComponent)RealmComponent.getRealmComponent(other);
						rcImage = otherRc.getFlipSideImage();
					}
					else {
						rcImage = chit.getFlipSideImage();
					}
				}
				else {
					rcImage = rc.getImage();
				}
			}
			g.drawImage(rcImage,x,y,null);
			maxH = Math.max(maxH,d.height);
			x += (d.width+spacing);
		}
	}
	public void setMouseHover(MouseEvent ev) {
		int newMouseHoverIndex = -1;
		if (ev!=null) {
			Point p = ev.getPoint();
			int i = 0;
			for (Rectangle rect:drawRectList) {
				if (rect.contains(p)) {
					newMouseHoverIndex = i;
					break;
				}
				i++;
			}
		}
		
		if (newMouseHoverIndex!=mouseHoverIndex) {
			mouseHoverIndex = newMouseHoverIndex;
			repaint();
		}
	}
	public void showClickViewObject() {
		if (mouseHoverIndex>=0) {
			clickViewObject = drawContainerList.get(mouseHoverIndex);
			repaint();
		}
	}
	public void clearClickViewObject() {
			clickViewObject = null;
			repaint();
	}
	private static final AlphaComposite TRANSPARENT = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.4f);
	private void draw(Graphics g1) {
		Graphics2D g = (Graphics2D)g1;
		Composite defaultComposite = g.getComposite();
		
		ArrayList<Integer> monsterDice = new ArrayList<Integer>();
		DieRoller roller = game.getMonsterDie();
		if (roller!=null) {
			int n = roller.getNumberOfDice();
			for (int i=0;i<n;i++) {
				monsterDice.add(roller.getValue(i));
			}
		}
		
		drawRectList = new ArrayList<Rectangle>();
		drawContainerList = new ArrayList<GameObject>();
	
		Die die = new Die(40,9,Color.red,Color.white);
		
		g.setColor(MagicRealmColor.GRAY);
		g.fillRect(0,0,cardSize.width,cardSize.height);
		int h = (ChitComponent.T_CHIT_SIZE+SPACING+TEXT_SPACING+SPACING);
		ArrayList allDrawables = new ArrayList();
		ArrayList allDrawableRects = new ArrayList();
		for (int n=1;n<=6;n++) {
			int x = LEFT_BORDER;
			int y = ((n-1)*h)+SPACING+TEXT_SPACING+5;
			
			boolean prowling = monsterDice.contains(n);
			
			if (prowling) {
				Dimension s = getSize();
				g.setColor(GraphicsUtil.convertColor(UIManager.getColor("Panel.background"),HIGHLIGHT_COLOR,HIGHLIGHT_INTENSITY));
				g.fillRect(5,y-TEXT_SPACING-SPACING,s.width,h);
				die.setColor(Color.red,Color.white);
			}
			else {
				die.setColor(Color.white,Color.black);
			}
			die.setFace(n);
			die.paintIcon(this,g,10,y-TEXT_SPACING-SPACING+((h-die.getIconHeight())>>1));
			
			for (Iterator i=sections.iterator();i.hasNext();) {
				String section = (String)i.next();
				String key = section+n;
				HashLists groups = (HashLists)sectionRowHash.get(key);
				if (groups!=null) {
					x += SPACING;
					ArrayList summons = new ArrayList();
					for (Iterator t=groups.keySet().iterator();t.hasNext();) {
						String summon = (String)t.next();
						ArrayList group = (ArrayList)groups.get(summon);
						summons.add(new SummonGroup(summon,group));
					}
					
					Collections.sort(summons);
					for (Iterator t=summons.iterator();t.hasNext();) {
						SummonGroup sg = (SummonGroup)t.next();
						String summon = sg.summon;
						ArrayList group = sg.group;
						ArrayList rects = new ArrayList();
						ArrayList gos = new ArrayList();
						int startX = x-SPACING;
						int yoffset = 0;
						for (Iterator gr=group.iterator();gr.hasNext();) {
							GameObject go = (GameObject)gr.next();
							String size = getChitSizeAttribute(go);
							Dimension d = ChitComponent.getDimensionForSize(size);
							String col = go.getThisAttribute("ts_color");
							Color c = MagicRealmColor.getColor(col);
							if (prowling) {
								c = GraphicsUtil.convertColor(c,HIGHLIGHT_COLOR,HIGHLIGHT_INTENSITY);
							}
							g.setColor(c);
							boolean bottom = true;//go.hasThisAttribute("ts_draw_below");
							yoffset = bottom?(ChitComponent.T_CHIT_SIZE-d.height):0;
							if (yoffset>4) yoffset-=4;
							rects.add(new Rectangle(x,y+yoffset,d.width-1,d.height));
							gos.add(go);
							x += d.width;
						}
						x += SPACING;
						g.fillRect(startX,y-SPACING-TEXT_SPACING,x-startX,h);
						g.setColor(Color.black);
						g.setFont(SUMMON_FONT);
						String drawSummon = summon.toUpperCase().replace('_',' ');
						if (yoffset>0) {
							TextType tt = new TextType(drawSummon,x-startX-20,"NORMAL");
							tt.setDelims(",");
							tt.setSpace(", ");
							tt.draw(g,startX+1,y-SPACING+yoffset-tt.getHeight(g),Alignment.Left);
						}
						else {
							g.drawString(drawSummon,startX+1,y-SPACING+yoffset);
						}
						g.setColor(Color.black);
						
						g.setFont(COUNT_FONT);
						Iterator gg = gos.iterator();
						for (Iterator r=rects.iterator();r.hasNext();) {
							
							// Start by drawing the box
							Rectangle rect = (Rectangle)r.next();
							GameObject go = (GameObject)gg.next();
							
							// Save these attributes for later mouse point interpretations
							drawRectList.add(rect);
							drawContainerList.add(go);
//System.out.println(rect+" contains "+go.getName());

							g.setComposite(TRANSPARENT);
							g.draw(rect);
							String iconType = go.getThisAttribute(Constants.ICON_TYPE);
							
							// Draw the icon (if any)
							if (iconType != null) {
								String folder = go.getThisAttribute("icon_folder");
								String filename = folder+"/"+iconType;
								ImageIcon icon = null;
								String size = getChitSizeAttribute(go);
								if (size.equals("T") || size.equals("HCARD")) {
									icon = ImageCache.getIcon(filename,90);
								}
								else {
									icon = ImageCache.getIcon(filename,70);
								}
								int dx = ((rect.width-icon.getIconWidth())>>1);
								int dy = ((rect.height-icon.getIconHeight())>>1);
								g.drawImage(icon.getImage(),rect.x+dx,rect.y+dy,null);
							}
							
							// Draw the count (if any)
							String count = go.getThisAttribute("ts_count");
							if (count!=null) {
								g.drawString(count,rect.x+5,rect.y+20);
							}
							g.setComposite(defaultComposite);
							
							// Draw contents
							ArrayList hold = go.getHold();
							ArrayList drawable = new ArrayList();
							ArrayList horses = new ArrayList();
							for (Iterator q=hold.iterator();q.hasNext();) {
								GameObject held = (GameObject)q.next();
								RealmComponent rc = RealmComponent.getRealmComponent(held);
								if (rc.isMonster() || rc.isNative() || rc.isGoldSpecial()) {
									drawable.add(0,rc);
									RealmComponent horse = (RealmComponent)rc.getHorse(false);
									if (horse!=null) {
										horses.add(0,horse);
									}
								}
							}
							drawable.addAll(0,horses);
							
							if (!drawable.isEmpty()) {
								int offset = 0;
								
								// what is the difference in size of the box and the topmost chit?
								RealmComponent topChit = (RealmComponent)drawable.get(drawable.size()-1);
								int diff = rect.height - topChit.getHeight();
								
								int yoff = ((drawable.size()-1)*3)-(diff>>1);
								
								if (drawable.size()>5) yoff -= 4;
								for (Iterator q=drawable.iterator();q.hasNext();) {
									RealmComponent rc = (RealmComponent)q.next();
									int dx = rect.x+((rect.width-rc.getWidth())>>1)-offset;
									int dy = rect.y+((rect.height-rc.getHeight())>>1)-offset+yoff;
									allDrawables.add(rc);
									allDrawableRects.add(new Rectangle(dx,dy,rect.width+10,rect.height+10));
									offset+=3;
								}
							}
						}
					}
				}
			}
		}
		
		// Draw all the sidebar boxes (the ones along the right-hand side)
		int nonMdListColumns = ((nonMdList.size()-1)/NON_MD_LIST_COLUMN_LENGTH)+1;
		int totalNonMdWidth = ((NON_MD_LIST_WIDTH+RIGHT_BORDER) * nonMdListColumns);
		int x = cardSize.width - totalNonMdWidth;
		int y = SPACING+TEXT_SPACING+5;
		g.setFont(LABEL_FONT);
		int count = 0;
		for (Iterator i=nonMdList.iterator();i.hasNext();) {
			GameObject go = (GameObject)i.next();
			Rectangle r = new Rectangle(x,y,NON_MD_LIST_WIDTH,NON_MD_LIST_HEIGHT);
			
			// Draw boxes
			g.setColor(MagicRealmColor.getColor(go.getThisAttribute("ts_color")));
			g.fillRect(r.x-4,r.y-4,r.width+8,r.height+8);
			g.setColor(Color.black);
			g.setComposite(TRANSPARENT);
			g.draw(r);
			g.drawString(go.getName(),x+5,y+NON_MD_LIST_HEIGHT-5);
			g.setComposite(defaultComposite);
			
			// Capture info
			drawRectList.add(r);
			drawContainerList.add(go);
			y += NON_MD_LIST_HEIGHT+SPACING;
			count++;
			if (count>=NON_MD_LIST_COLUMN_LENGTH) {
				count = 0;
				x += NON_MD_LIST_WIDTH + RIGHT_BORDER;
				y = SPACING+TEXT_SPACING+5;
			}
		}
		
		Iterator r=allDrawableRects.iterator();
		for (Iterator i=allDrawables.iterator();i.hasNext();) {
			RealmComponent rc = (RealmComponent)i.next();
			Rectangle rect = (Rectangle)r.next();
			rc.paint(g.create(rect.x,rect.y,rect.width,rect.height));
		}
		g.setColor(Color.black);
		g.drawRect(0,0,cardSize.width-1,cardSize.height-1);
	}
	private class SummonGroup implements Comparable {
		public String summon;
		public ArrayList group;
		private int sortOrder = 0;
		public SummonGroup(String summon,ArrayList group) {
			this.summon = summon;
			this.group = group;
			for (Iterator i=group.iterator();i.hasNext();) {
				GameObject go = (GameObject)i.next();
				sortOrder = go.getInt("this","ts_sort");
			}
		}
		public int compareTo(Object o1) {
			int ret = 0;
			if (o1 instanceof SummonGroup) {
				SummonGroup s = (SummonGroup)o1;
				ret = sortOrder - s.sortOrder;
				if (ret==0) {
					ret = summon.compareTo(s.summon);
				}
			}
			return ret;
		}
	}
	public ImageIcon getIcon() {
		return new ImageIcon(getImage());
	}
	public Image getImage() {
		Dimension size = cardSize;
		BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_3BYTE_BGR);
		draw(image.getGraphics());
		return image;
	}
	public static final String TS_VIEW = "ts_view";
	private static class DisplayFrame extends JFrame implements ManagedFrame {
		private TreasureSetupCardView view;
		MouseInputListener mil = new MouseInputAdapter() {
			public void mousePressed(MouseEvent ev) {
				view.rightClick = MouseUtility.isRightOrControlClick(ev);
				view.showClickViewObject();
			}
			public void mouseReleased(MouseEvent ev) {
				view.clearClickViewObject();
			}
			public void mouseMoved(MouseEvent ev) {
				view.setMouseHover(ev);
				view.repaint();
			}
			public void mouseExited(MouseEvent ev) {
				view.clearClickViewObject();
				view.setMouseHover(null);
				view.repaint();
			}
		};
		public DisplayFrame(JFrame parent,TreasureSetupCardView in) {
			super(in.title);
			view = in;
			view.reset();
			initComponents();
		}
		public String getKey() {
			return TS_VIEW;
		}
		private void initComponents() {
			// I'd like it to be this big
			Dimension best = new Dimension(view.cardSize.width+25,view.cardSize.height+75);
			
			// But NOT bigger than the users screen!
			Rectangle r = ComponentTools.findPreferredRectangle();
			best.width = Math.min(r.width,best.width);
			best.height = Math.min(r.height,best.height);
			
			// Ah, just right
			setSize(best);
			getContentPane().setLayout(new BorderLayout());
			JScrollPane pane = new JScrollPane(view);
			ComponentTools.lockComponentSize(pane,900,750);
			getContentPane().add(pane,"Center");
			Box box = Box.createHorizontalBox();
			JButton closeButton = new JButton("Close");
			closeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					FrameManager.getFrameManager().disposeFrame(getKey());
				}
			});
			box.add(Box.createHorizontalGlue());
			box.add(closeButton);
			box.add(Box.createHorizontalGlue());
			getContentPane().add(box,"South");
			
			view.addMouseListener(mil);
			view.addMouseMotionListener(mil);
		}
		public void cleanup() {
			view.removeMouseListener(mil);
			view.removeMouseMotionListener(mil);
		}
	}
	public static void displayView(JFrame parent,TreasureSetupCardView view) {
		DisplayFrame frame = new DisplayFrame(parent,view);
		frame.setLocationRelativeTo(parent);
		FrameManager.getFrameManager().addFrame(frame);
	}
	public static void main(String[]args) {
		RealmUtility.setupTextType();
		//RealmComponent.displayStyle = RealmComponent.DISPLAY_STYLE_FRENZEL;
		RealmComponent.displayStyle = RealmComponent.DISPLAY_STYLE_COLOR;
		RealmLoader loader = new RealmLoader();
		HostPrefWrapper hostPrefs = HostPrefWrapper.createDefaultHostPrefs(loader.getData());
//		hostPrefs.setGameKeyVals("original_game");
//		hostPrefs.setGameKeyVals("alt_monsters1_game");
		hostPrefs.setGameKeyVals("rw_expansion_1");
//		loader.getData().doSetup("standard_game");
		SetupCardUtility.resetAllTreasureLocationDenizens(loader.getData());
		GameWrapper game = GameWrapper.findGame(loader.getData());
		DieRoller roller = new DieRoller();
		roller.addRedDie();
		roller.addRedDie();
		roller.rollDice();
		game.setMonsterDie(roller);
		ArrayList<String> query = new ArrayList<String>();
		query.add("original_game");
		loader.getData().doSetup("standard_game",query);
		TreasureSetupCardView view = new TreasureSetupCardView(loader.getData(),"Bob");
		displayView(new JFrame(),view);
//		System.exit(0);
	}
}