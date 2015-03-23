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
package com.robin.magic_realm.components;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import com.robin.game.objects.GameObject;
import com.robin.general.graphics.GraphicsUtil;
import com.robin.general.graphics.TextType;
import com.robin.general.graphics.TextType.Alignment;
import com.robin.general.io.ResourceFinder;
import com.robin.general.swing.IconFactory;
import com.robin.magic_realm.components.attribute.ColorMagic;
import com.robin.magic_realm.components.attribute.TileLocation;
import com.robin.magic_realm.components.utility.*;
import com.robin.magic_realm.components.wrapper.*;
import com.robin.magic_realm.map.Tile;

public class TileComponent extends ChitComponent {
	private static String DEFAULT_IMAGE_PATH = "default";
	private static boolean emergencyUpdateCalled = false; // only attempt this once!!
	private static boolean showBorderlandConnectedClearings = false; // for debugging!

	private static Logger logger = Logger.getLogger(TileComponent.class.getName());
	
	private static final int CHIT_PLACEMENT_OFFSET = 3;
	// public static final int TILE_EDGE_LENGTH = 187; // This is based on my image set of images 374x324 (http://www.thewinternet.com/magicrealm/)
	public static final double TILE_EDGE_LENGTH = 248; // This is based on my image set of images 497x431 (enlarged by 133%)

	protected static final double root3 = Math.sqrt(3);
	public static final int TILE_WIDTH = (int) (TILE_EDGE_LENGTH * 2.0);
	public static final int TILE_HEIGHT = (int) (TILE_EDGE_LENGTH * root3);
	public static final int CLEARING_RADIUS = (int) (TILE_EDGE_LENGTH / 6.0);
	public static final int EDGE_RADIUS = (int) (TILE_EDGE_LENGTH / 8.0);

	public static Dimension iconDimensions = null;

	protected static final int DRAW_BORDER = 5;

	public static final String NORMAL = LIGHT_SIDE_UP;
	public static final String ENCHANTED = DARK_SIDE_UP;

	public static final int NORMAL_INDEX = 0;
	public static final int ENCHANTED_INDEX = 1;

	protected static final double radians_degrees60 = (Math.PI * 60.0) / 180.0;
	public static final float[] dash_style = { 7.0f, 3.0f };

	private static Color DEFAULT_MARK_COLOR = Color.green;

	protected boolean marked = false;
	protected Color markColor = DEFAULT_MARK_COLOR;
	
	protected GameWrapper game;
	protected RealmCalendar calendar;

	// protected double TILE_EDGE_LENGTH;
	// protected int TILE_WIDTH;
	// protected int TILE_HEIGHT;
	// protected int CLEARING_RADIUS;

	protected HashMap<String,String>[] tileImagePath;
//	protected String[] tileFolderName;
//	protected String[] tileImageName;
	protected ArrayList[] clearings;
	protected ArrayList[] paths;
	protected Point[] offroadPos;

	// if 0, then image is solid and drawing is not seen
	// if 100, then image is not seen, and drawing is solid
	protected int imageTransparency = 0; // default is solid image only

	protected int rotation;

	private long lastPaintedVersion = 0;
	private boolean alwaysPaint = false;
	private boolean needsRepaint = true;

	private Hashtable<String,TileComponent> edgeTiles = new Hashtable<String,TileComponent>();

	private Rectangle lastPaintLocation = null;
	private Point[] lastOffroadPaintLocation = new Point[2];

	private boolean showFlipside = false;

	// Local variables (no need to store these in GameObject - or at least not yet)

	protected TileComponent() {
		super(null);
		// Empty constructor for EmptyTileComponent
	}

	public TileComponent(GameObject obj) {
		super(obj);
		game = GameWrapper.findGame(obj.getGameData());
		calendar = RealmCalendar.getCalendar(obj.getGameData());
		initSize();
		lightColor = Color.darkGray;
		darkColor = Color.darkGray;
	}

	public String getLightSideStat() {
		return "normal";
	}

	public String getDarkSideStat() {
		return "enchanted";
	}

	public void setShowFlipside(boolean val) {
		showFlipside = val;
		needsRepaint = true;
	}

	/**
	 * Returns a collection of strings representing precise connected edges AFTER rotation (ie., N,S,NE)
	 */
	public ArrayList getConnectedEdges() {
		ArrayList list = new ArrayList();
		for (Iterator i = paths[getFacingIndex()].iterator(); i.hasNext();) {
			PathDetail path = (PathDetail) i.next();
			String edge = path.getEdge();
			if (edge != null) {
				String rotEdge = Tile.convertEdge(edge, rotation);
				list.add(rotEdge);
			}
		}

		return list;
	}

	public PathDetail getEdgePath(String edgeName) {
		for (Iterator i = paths[getFacingIndex()].iterator(); i.hasNext();) {
			PathDetail path = (PathDetail) i.next();
			String edge = path.getEdge();
			if (edge != null) {
				String rotEdge = Tile.convertEdge(edge, rotation);
				if (rotEdge.equals(edgeName)) {
					return path;
				}
			}
		}
		return null;
	}

	/**
	 * Returns a collection of PathDetail objects that connect this clearing to another (or null if none)
	 */
	public ArrayList<PathDetail> findConnections(ClearingDetail clearing) {
		ArrayList<PathDetail> connections = new ArrayList<PathDetail>();
		for (Iterator i = paths[getFacingIndex()].iterator(); i.hasNext();) {
			PathDetail path = (PathDetail) i.next();
			if (path.findConnection(clearing) != null) {
				connections.add(path);
			}
		}
		return connections.isEmpty() ? null : connections;
	}
	
	public ArrayList<PathDetail> findConnectedMapEdges(ClearingDetail clearing) {
		ArrayList<PathDetail> connections = new ArrayList<PathDetail>();
		for (Iterator i = paths[getFacingIndex()].iterator(); i.hasNext();) {
			PathDetail path = (PathDetail) i.next();
			if (path.connectsToMapEdge() && path.hasClearing(clearing)) {
				connections.add(path);
			}
		}
		return connections.isEmpty() ? null : connections;
	}
	
	/**
	 * Returns all clearings on this side
	 */
	public ArrayList getShowingClearings() {
		return new ArrayList(clearings[getFacingIndex()]);
	}

	public boolean isEnchanted() {
		return !isLightSideUp();
	}

	public int getFacingIndex() {
		int facingIndex = isLightSideUp() ? NORMAL_INDEX : ENCHANTED_INDEX;
		if (showFlipside) {
			facingIndex = facingIndex == NORMAL_INDEX ? ENCHANTED_INDEX : NORMAL_INDEX;
		}
		return facingIndex;
	}

	public String getFacingName() {
		return getFacingIndex() == NORMAL_INDEX ? "green" : "enchanted";
	}

	public String getName() {
		return TILE;
	}

	public void setRotation(int val) {
		rotation = val;
	}

	public void initSize() {
		Dimension size = new Dimension(TILE_WIDTH + 4, TILE_HEIGHT + 4);
		setMinimumSize(size);
		setMaximumSize(size);
		setPreferredSize(size);
		initDetail();
	}

	public int getChitSize() {
		return TILE_WIDTH;
	}

	protected void initDetail() {
//		tileFolderName = new String[2];
//		tileImageName = new String[2];
		tileImagePath = new HashMap[2];
		clearings = new ArrayList[2];
		paths = new ArrayList[2];
		offroadPos = new Point[2];
		initSide(NORMAL_INDEX, gameObject.getAttributeBlock("normal"));
		initSide(ENCHANTED_INDEX, gameObject.getAttributeBlock("enchanted"));
	}

	public String getTileNameNoBoard() {
		String name = gameObject.getName();
		String bn = getGameObject().getThisAttribute(Constants.BOARD_NUMBER);
		if (bn!=null) {
			name = name.substring(0,name.length()-bn.length()-1);
		}
		return name;
	}
	public String getTileName() {
		return gameObject.getName();
	}

	public String getTileCode() {
		String code = gameObject.getThisAttribute("code");
		if (gameObject.hasThisAttribute(Constants.BOARD_NUMBER)) {
			return code + "[" + gameObject.getThisAttribute(Constants.BOARD_NUMBER) + "]";
		}
		return code;
	}

	public String getTileType() {
		return gameObject.getThisAttribute("tile_type");
	}

	public Hashtable getEdgePositionHash() {
		Hashtable edgePositionHash = new Hashtable();
		edgePositionHash.put("S", new Point(TILE_WIDTH >> 1, TILE_HEIGHT));
		edgePositionHash.put("SW", new Point(TILE_WIDTH >> 3, TILE_HEIGHT - (TILE_HEIGHT >> 2)));
		edgePositionHash.put("NW", new Point(TILE_WIDTH >> 3, TILE_HEIGHT >> 2));
		edgePositionHash.put("N", new Point(TILE_WIDTH >> 1, 0));
		edgePositionHash.put("NE", new Point(TILE_WIDTH - (TILE_WIDTH >> 3), TILE_HEIGHT >> 2));
		edgePositionHash.put("SE", new Point(TILE_WIDTH - (TILE_WIDTH >> 3), TILE_HEIGHT - (TILE_HEIGHT >> 2)));
		return edgePositionHash;
	}
	private static String[] EDGE_ORDER = {"S","SW","NW","N","NE","SE"};
	public String translateEdgeBasedOnRotation(String edge) {
		int rot = getRotation();
		for (int i=0;i<EDGE_ORDER.length;i++) {
			if (EDGE_ORDER[i].equals(edge)) {
				return EDGE_ORDER[(i+rot)%6];
			}
		}
		return edge;
	}

	protected void initSide(int side, Hashtable hash) {

		String folder = gameObject.getThisAttribute("folder");
		String imageName = gameObject.getThisAttribute("image");

		// if (imageName.equals("borderland")) {
		// imageName = "ambush"; // XXX Just to see it ...
		// }
		String tileSideName = side == NORMAL_INDEX ? "green" : "enchanted";

		String imageEnd = side == NORMAL_INDEX ? "1" : "-e1";
		String ext = ".gif";
		String folderPath = "images/"+folder+"/";
		String fullImage = imageName + imageEnd + ext;
		tileImagePath[side] = new HashMap<String,String>();
		if (calendar!=null) {
			for (GameObject season:calendar.getAllSeasons()) {
				String seasonName = season.getName();
				String seasonTilePath = folderPath+seasonName.toLowerCase()+"/"+fullImage;
				if (ResourceFinder.exists(seasonTilePath)) {
					tileImagePath[side].put(seasonName,seasonTilePath);
				}
			}
		}
		tileImagePath[side].put(DEFAULT_IMAGE_PATH,folderPath+fullImage);

		Hashtable edgePositionHash = getEdgePositionHash();

		// Setup clearings
		Hashtable clearingPositionHash = new Hashtable();
		clearings[side] = new ArrayList();
		for (int i = 1; i <= 6; i++) {
			String typeKey = "clearing_" + i + "_type";
			String xyKey = "clearing_" + i + "_xy";
			String magicKey = "clearing_" + i + "_magic";
			String extraKey = "clearing_" + i + "_extra";
			String type = (String) hash.get(typeKey);
			if (type != null) {
				String pos = (String) hash.get(xyKey);
				ClearingDetail detail = new ClearingDetail(this, i, type, readPoint(pos), side);
				
				// Read magic
				String magic = (String) hash.get(magicKey);
				if (magic != null && magic.length() > 0) {
					for (int m = ClearingDetail.MAGIC_WHITE; m <= ClearingDetail.MAGIC_BLACK; m++) {
						if (magic.indexOf(ClearingDetail.MAGIC_CHAR[m]) >= 0) {
							detail.setMagic(m, true);
							darkColor = detail.getColor();
						}
					}
				}
				
				// Read extras (if any)
				String extras = (String) hash.get(extraKey);
				if (extras!=null && extras.length()>0) {
					StringTokenizer tokens = new StringTokenizer(extras,",");
					while(tokens.hasMoreTokens()) {
						detail.addExtra(tokens.nextToken());
					}
				}
				
				// Add it
				clearings[side].add(detail);
				clearingPositionHash.put(detail.getName(), detail);
			}
		}
		// Setup paths
		paths[side] = new ArrayList();
		int i = 1;
		String from;
		while ((from = (String) hash.get("path_" + i + "_from")) != null) {
			String to = (String) hash.get("path_" + i + "_to");
			String type = (String) hash.get("path_" + i + "_type");
			String arcPoint = (String) hash.get("path_" + i + "_arc"); // optional

			ClearingDetail fromC = (ClearingDetail) clearingPositionHash.get(from);
			ClearingDetail toC = (ClearingDetail) clearingPositionHash.get(to);
			Point arc = readPoint(arcPoint);
			PathDetail pathDetail;
			if (fromC != null && toC != null) {
				pathDetail = new PathDetail(this, i, from, to, fromC, toC, arc, type, tileSideName);
			}
			else {
				// connects to edge
				ClearingDetail c1 = fromC == null ? new ClearingDetail(this, from, (Point) edgePositionHash.get(from), side) : fromC;
				ClearingDetail c2 = toC == null ? new ClearingDetail(this, to, (Point) edgePositionHash.get(to), side) : toC;
				pathDetail = new PathDetail(this, i, from, to, c1, c2, arc, type, tileSideName);
			}
			paths[side].add(pathDetail);
			i++;
		}

		// Setup offroad
		String offroad = (String) hash.get("offroad_xy");
		if (offroad != null) {
			offroadPos[side] = readPoint(offroad);
		}
		else {
			offroadPos[side] = new Point(0, 0);
		}
	}

	/**
	 * Reads the x,y version of the point from the String, and returns a Point that has been adjusted for tile size
	 */
	protected Point readPoint(String pos) {
		if (pos != null) {
			StringTokenizer st = new StringTokenizer(pos, ",");
			float px = Float.valueOf(st.nextToken()).floatValue();
			float py = Float.valueOf(st.nextToken()).floatValue();
			int x = (int) ((px * (float) TILE_WIDTH) / 100.0);
			int y = (int) ((py * (float) TILE_HEIGHT) / 100.0);
			return new Point(x, y);
		}
		return null;
	}

	// public Dimension getTileSize() {
	// return new Dimension(TILE_WIDTH,TILE_HEIGHT);
	// }
	public Shape getShape(int x, int y, int size) {
		return getHexShape(x, y, TILE_WIDTH, TILE_HEIGHT, size);
	}
	protected String getExtraBoardShadingType() {
		return null; // Never use an extra shading type for tiles - they already have a letter
	}

	public static Shape getHexShape(int x, int y, int tw, int th, int size) {
		int border = (tw - size) >> 1;
		Polygon hexBorder = new Polygon();
		int w = tw - (border << 1);
		int h = th - (border << 1);

		int xmod = w >> 2;
		int ymod = h >> 1;
		hexBorder.addPoint(x, y + ymod);
		hexBorder.addPoint(x + xmod, y);
		hexBorder.addPoint(x + w - xmod, y);
		hexBorder.addPoint(x + w, y + ymod);
		hexBorder.addPoint(x + w - xmod, y + h);
		hexBorder.addPoint(x + xmod, y + h);
		return hexBorder;
	}

	public static Shape getHexShape(int x, int y, int sx, int sy, int insetBorder, double scale) {
		double dx = ((double) x * scale) + sx;
		double dy = ((double) y * scale) + sy;
		double val = TILE_EDGE_LENGTH * scale;
		double dib = (double) insetBorder * scale;
		int tw = (int) (val * 2.0);
		int th = (int) (val * root3);
		return getHexShape((int) dx, (int) dy, tw, th, tw - (int) (dib * 2));
	}

	/**
	 * A method to do the rotate calculations
	 */
	public Point convertPoint(Point unrotatedPoint) {
		Point2D.Double tileCenter = new Point2D.Double(TILE_WIDTH >> 1, TILE_HEIGHT >> 1);
		Point2D rotatedPoint = GraphicsUtil.rotate(unrotatedPoint, tileCenter, rotation * radians_degrees60);
		return new Point((int) rotatedPoint.getX(), (int) rotatedPoint.getY());
	}

	public ClearingDetail findClearing(Point relativePoint) {
		Point2D.Double tileCenter = new Point2D.Double(TILE_WIDTH >> 1, TILE_HEIGHT >> 1);
		Point2D rotatedPoint = GraphicsUtil.rotate(relativePoint, tileCenter, -rotation * radians_degrees60);
		int tx = (int) rotatedPoint.getX();
		int ty = (int) rotatedPoint.getY();
		for (Iterator i = clearings[getFacingIndex()].iterator(); i.hasNext();) {
			ClearingDetail clearing = (ClearingDetail) i.next();
			Point p = clearing.getPosition();
			Shape s = getClearingShape(p, CLEARING_RADIUS);
			if (s.contains(tx, ty)) {
				return clearing;
			}
		}
		// Check map edges (if any)
		for (Iterator i=getMapEdges().iterator();i.hasNext();) {
			ClearingDetail clearing = (ClearingDetail) i.next();
			Point p = clearing.getPosition();
			Shape s = getClearingShape(p, CLEARING_RADIUS);
			if (s.contains(tx, ty)) {
				return clearing;
			}
		}
		return null;
	}

	public ArrayList<ClearingDetail> getClearings() {
		return getClearings(null);
	}

	public int getClearingCount() {
		return getClearings().size();
	}

	/**
	 * @param clearingType If clearingType is null, all clearings are returned
	 */
	public ArrayList<ClearingDetail> getClearings(String clearingType) {
		ArrayList<ClearingDetail> list = new ArrayList<ClearingDetail>();
		for (Iterator i = clearings[getFacingIndex()].iterator(); i.hasNext();) {
			ClearingDetail clearing = (ClearingDetail) i.next();
			if (clearingType == null || clearing.getType().equals(clearingType)) {
				list.add(clearing);
			}
		}
		return list;
	}
	public ArrayList<ClearingDetail> getEnchantedClearings() {
		ArrayList<ClearingDetail> list = new ArrayList<ClearingDetail>();
		for (Iterator i = clearings[ENCHANTED_INDEX].iterator(); i.hasNext();) {
			ClearingDetail clearing = (ClearingDetail) i.next();
			list.add(clearing);
		}
		return list;
	}
	
	public ClearingDetail getClearing(String numString) {
		try {
			int clearingNum = Integer.valueOf(numString);
			return getClearing(clearingNum);
		}
		catch(NumberFormatException ex) {
			// Not a number, try edge
			String edge = numString.substring(1); // skip dot
			for (ClearingDetail mapEdge:getMapEdges()) {
				if (mapEdge.getType().equals(edge)) {
					return mapEdge;
				}
			}
		}
		return null;
	}

	public ClearingDetail getClearing(int clearingNum) {
		for (Iterator i = clearings[getFacingIndex()].iterator(); i.hasNext();) {
			ClearingDetail clearing = (ClearingDetail) i.next();
			if (clearing.getNum() == clearingNum) {
				return clearing;
			}
		}
		return null;
	}
	
	public ArrayList<ClearingDetail> getMapEdges() {
		ArrayList<ClearingDetail> mapEdges = new ArrayList<ClearingDetail>();
		for (Iterator i = paths[getFacingIndex()].iterator(); i.hasNext();) {
			PathDetail path = (PathDetail) i.next();
			if (path.connectsToMapEdge()) {
				mapEdges.add(path.getEdgeAsClearing());
			}
		}
		return mapEdges; // Could be empty if the tile is only connected to other tiles
	}
	
	/**
	 * Call when the overall map has changed, like when building the map.
	 */
	public void resetClearingPositions() {
		int facingIndex = getFacingIndex();
		ArrayList allClearings = new ArrayList();
		
		// Regular clearings
		allClearings.addAll(clearings[facingIndex]);
		
		// Edge of the map clearings
		allClearings.addAll(getMapEdges()); // Might not be any
		
		for (Iterator i = allClearings.iterator(); i.hasNext();) {
			ClearingDetail detail = (ClearingDetail) i.next();
			detail.setAbsolutePosition(null);
		}
	}

	private void forceClearingPositionUpdate(boolean debug) {
		// Update absolute clearing locations (used when determining halfway points)
		int facingIndex = getFacingIndex();
		
		ArrayList allClearings = new ArrayList();
		// Regular clearings
		allClearings.addAll(clearings[facingIndex]);
		
		// Edge of the map clearings
		allClearings.addAll(getMapEdges()); // Might not be any
		
		for (Iterator i = allClearings.iterator(); i.hasNext();) {
			ClearingDetail detail = (ClearingDetail) i.next();
			if (detail.getAbsolutePosition() == null) {
				Point rotatedPosition = convertPoint(detail.getPosition());
				Point absPos = new Point(lastPaintLocation.x + rotatedPosition.x, lastPaintLocation.y + rotatedPosition.y);
				detail.setAbsolutePosition(absPos);
//				if (!lastPaintLocation.contains(absPos)) { // This can happen with edge clearings now, so remove this error
//					System.err.println("Problem setting absolute position in TileComponent:");
//					System.err.println(" lastPaintLocation = " + lastPaintLocation);
//					System.err.println("   rotatedPosition = " + rotatedPosition);
//					System.err.println("            absPos = " + absPos);
//				}
			}
		}
	}

	public void paintTo(Graphics g, int x, int y, int w, int h) {
		lastPaintLocation = new Rectangle(x, y, w, h);
		forceClearingPositionUpdate(false);
		paint(g.create(x, y, w, h));
	}

	public void paintComponent(Graphics g1) {
		logger.finer(getTileName() + "  lastPaintedVersion=" + lastPaintedVersion + "   thisVersion=" + gameObject.getVersion());
		if (alwaysPaint || needsRepaint || lastPaintedVersion != gameObject.getVersion()) {
			logger.finer(getTileName() + "  Painting!!");
			needsRepaint = false;
			lastPaintedVersion = gameObject.getVersion();
			super.paintComponent(g1);
			Graphics2D g = (Graphics2D) g1;

			// Apply shape clip
			g.clip(getShape(0, 0, getChitSize()));

			// Apply transform
			AffineTransform normal = g.getTransform();
			AffineTransform transform = new AffineTransform(normal);
			transform.rotate(rotation * radians_degrees60, TILE_WIDTH >> 1, TILE_HEIGHT >> 1);
			g.setTransform(transform);

			int facingIndex = getFacingIndex();
			if (imageTransparency > 0) {
				int magicRad = CLEARING_RADIUS << 2;
				if (fullDetail) {
					// Draw magic
					Composite old = g.getComposite();
					AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
					g.setComposite(composite);
					for (Iterator i = clearings[facingIndex].iterator(); i.hasNext();) {
						ClearingDetail detail = (ClearingDetail) i.next();
						Color c = detail.getColor();
						if (c != null) {
							g.setColor(c);
							Point p = detail.getPosition();
							g.fillOval(p.x - magicRad, p.y - magicRad, magicRad << 1, magicRad << 1);
						}
					}
					g.setComposite(old);
				}

				// Draw mountains
				int mntRad = CLEARING_RADIUS << 1;
				for (Iterator i = clearings[facingIndex].iterator(); i.hasNext();) {
					ClearingDetail detail = (ClearingDetail) i.next();
					if (detail.getType().equals("mountain")) {
						g.setColor(Color.gray);
						Point p = detail.getPosition();
						g.fillOval(p.x - mntRad, p.y - mntRad, mntRad << 1, mntRad << 1);
					}
				}

				// Enhance magic
				magicRad = CLEARING_RADIUS + 8;
				for (Iterator i = clearings[facingIndex].iterator(); i.hasNext();) {
					ClearingDetail detail = (ClearingDetail) i.next();
					Color c = detail.getColor();
					if (c != null) {
						g.setColor(c);
						Point p = detail.getPosition();
						g.fillOval(p.x - magicRad, p.y - magicRad, magicRad << 1, magicRad << 1);
					}
				}

				if (fullDetail) {
					// Draw black version
					for (Iterator i = clearings[facingIndex].iterator(); i.hasNext();) {
						ClearingDetail detail = (ClearingDetail) i.next();
						g.setColor(Color.black);
						drawClearing(g, detail, CLEARING_RADIUS, false);
						if (detail.isCave()) {
							g.setColor(Color.white);
							drawClearing(g, detail, CLEARING_RADIUS, true);
						}
					}
				}
				for (Iterator i = paths[facingIndex].iterator(); i.hasNext();) {
					PathDetail detail = (PathDetail) i.next();

					if (fullDetail) {
						g.setColor(Color.black);
						drawPath(g, detail, CLEARING_RADIUS, false);
					}
					if (detail.isDotted()) {
						if (fullDetail) {
							// Draw dotted lines INSTEAD of color paths for CAVES
							g.setColor(Color.white);
							drawPath(g, detail, CLEARING_RADIUS, true);
						}
						else {
							g.setColor(Color.black);
							drawPath(g, detail, CLEARING_RADIUS - (DRAW_BORDER << 1), false);
						}
					}
					else {
						g.setColor(detail.getColor());
						drawPath(g, detail, CLEARING_RADIUS - (DRAW_BORDER << 1), false);
					}
				}

				// Add Color
				for (Iterator i = clearings[facingIndex].iterator(); i.hasNext();) {
					ClearingDetail detail = (ClearingDetail) i.next();
					if (detail.isCave()) {
						g.setColor(Color.black);
					}
					else {
						g.setColor(MagicRealmColor.TAN);
					}
					drawClearing(g, detail, CLEARING_RADIUS - DRAW_BORDER, false);
					// tt = new TextType(detail.getNumString(),(CLEARING_RADIUS-2)<<1,"BIG_BOLD");
					// tt.draw(g,detail.getPosition().x-CLEARING_RADIUS,detail.getPosition().y-tt.getHeight(g),true,Color.white);
				}
			}

			// Overlay image
			if (imageTransparency < 100) {
				String imagePath = null;
				if (calendar!=null) {
					String seasonName = calendar.getSeasonName(game.getMonth());
					imagePath = tileImagePath[facingIndex].get(seasonName);
				}
				if (imagePath==null) {
					imagePath = tileImagePath[facingIndex].get(DEFAULT_IMAGE_PATH);
				}
				ImageIcon imageIcon = IconFactory.findIcon(imagePath); // don't cache the image - no need
				if (iconDimensions == null) {
					iconDimensions = new Dimension(imageIcon.getIconWidth(), imageIcon.getIconHeight());
				}
				if (imageTransparency > 0) {
					Composite normalComposite = g.getComposite();
					g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (1f - (imageTransparency / 100f))));
					g.drawImage(imageIcon.getImage(), 0, 0, null);
					g.setComposite(normalComposite);
				}
				else {
					g.drawImage(imageIcon.getImage(), 0, 0, null);
				}
			}

			String boardNumber = gameObject.getThisAttribute(Constants.BOARD_NUMBER);
			if (boardNumber != null) {
				int quarter = TILE_WIDTH >> 2;
				int x = TILE_WIDTH - quarter - 20;
				int y = TILE_HEIGHT - 50;
				g.setColor(Color.red);
				g.fillOval(x, y, 30, 30);
				TextType tt = new TextType(boardNumber, quarter, "BIG_BOLD");
				tt.draw(g, x + 7, y + 1, Alignment.Left, Color.black);
			}
			// UNCOMMENT THIS SECTION TO DRAW TEXT ON TILES
			// TextType tt = new TextType(gameObject.getName(),TILE_WIDTH,"BIG_BOLD");
			// tt.draw(g,0,TILE_HEIGHT-40,true,MagicRealmColor.YELLOW);
			//			
			// for (Iterator i=clearings[facingIndex].iterator();i.hasNext();) {
			// ClearingDetail detail = (ClearingDetail)i.next();
			// tt = new TextType(detail.getNumString(),(CLEARING_RADIUS-2)<<1,"CLEARING_FONT");
			// tt.draw(g,detail.getPosition().x-CLEARING_RADIUS,detail.getPosition().y-tt.getHeight(g),true);
			// }

			g.setTransform(normal);
			g.setClip(null);
		}
	}

	public void drawEmbellishments(Graphics g1, ChitDisplayOption displayOption) {
		int facingIndex = getFacingIndex();

		Graphics2D g = (Graphics2D) g1;
		// Add contained chits
		int offset = 0;
		HashMap clearingCount = new HashMap();
		drawHold(g, gameObject.getHold(), offset, clearingCount, displayOption);

		// Highlight Tile (if marked)
		if (marked) {
			g.setColor(markColor);
			g.setStroke(new BasicStroke(12));
			Shape shape = getShape(lastPaintLocation.x + 12, lastPaintLocation.y + 12, getChitSize() - 24);
			g.draw(shape);
		}

		// Highlight Clearings (if any are marked)
		for (Iterator i = clearings[facingIndex].iterator(); i.hasNext();) {
			ClearingDetail detail = (ClearingDetail) i.next();
			if (detail.isMarked()) {
				g.setColor(detail.getMarkColor());
				Point p = detail.getAbsolutePosition();
				Shape cShape = getClearingShape(p, CLEARING_RADIUS);
				g.setStroke(new BasicStroke(3));
				g.draw(cShape);
			}
		}
		
		// Highlight Map Edges (if any are marked)
		boolean didClip = false;
		for (Iterator i = paths[facingIndex].iterator(); i.hasNext();) {
			PathDetail path = (PathDetail) i.next();
			if (path.connectsToMapEdge()) {
				ClearingDetail detail = path.getEdgeAsClearing();
				if (detail.isMarked()) {
					if (!didClip) {
						Shape shape = getShape(lastPaintLocation.x, lastPaintLocation.y, getChitSize());
						g.setClip(shape);
						didClip = true;
					}
					g.setColor(detail.getMarkColor());
					Point p = detail.getAbsolutePosition();
					Shape cShape = getClearingShape(p, CLEARING_RADIUS-15);
					g.setStroke(new BasicStroke(3));
					g.draw(cShape);
				}
			}
		}
		if (didClip) {
			g.setClip(null);
		}
	}

	public Point getOffroadPoint() {
		int facingIndex = getFacingIndex();
		if (lastOffroadPaintLocation[facingIndex] == null) {
			lastOffroadPaintLocation[facingIndex] = convertPoint(new Point(offroadPos[facingIndex].x, offroadPos[facingIndex].y));
		}
		return lastOffroadPaintLocation[facingIndex];
	}

	protected void drawHold(Graphics2D g, ArrayList hold, int offset, HashMap clearingCount, ChitDisplayOption displayOption) {
		Point offroad = new Point(getOffroadPoint());
		offroad.x += lastPaintLocation.x;
		offroad.y += lastPaintLocation.y;
		
		ArrayList componentsToDraw = new ArrayList();
		
		if (displayOption.tileBewitchingSpells) {
			SpellMasterWrapper spellMaster = SpellMasterWrapper.getSpellMaster(getGameObject().getGameData());
			ArrayList<SpellWrapper> spells = spellMaster.getAffectingSpells(getGameObject());
			for (SpellWrapper spell:spells) {
				RealmComponent rc = RealmComponent.getRealmComponent(spell.getGameObject());
				componentsToDraw.add(rc);
			}
		}
		
		if (showBorderlandConnectedClearings) {
			for (ClearingDetail c:getClearings()) {
				if (!c.isConnectsToBorderland()) {
					g.setColor(Color.red);
					Point p = c.getAbsolutePosition();
					g.fillOval(p.x-50,p.y-50,100,100);
				}
				else {
					g.setColor(Color.green);
					Point p = c.getAbsolutePosition();
					g.fillOval(p.x-50,p.y-50,100,100);
				}
			}
		}
				
		// Now do the hold
		for (Iterator i = hold.iterator(); i.hasNext();) {
			GameObject obj = (GameObject) i.next();
			RealmComponent rc = RealmComponent.getRealmComponent(obj);
			if (rc == null) {
				System.err.println("No RealmComponent found for " + obj.getName() + " during TileComponent.drawHold?!?");
				continue;
			}
			if (displayOption.okayToDraw(rc)) {
				componentsToDraw.add(rc);
			}
			if (rc.isTreasureLocation() && !rc.isCacheChit() && displayOption.siteCards) {
				// Check TLs for face up SITE CARDS, cuz those should be painted too
				for (Iterator n = rc.getGameObject().getHold().iterator(); n.hasNext();) {
					GameObject thing = (GameObject) n.next();
					RealmComponent trc = RealmComponent.getRealmComponent(thing);
					if (trc.isTreasure()) {
						TreasureCardComponent treasure = (TreasureCardComponent) trc;
						if (treasure.isFaceUp()) {
							componentsToDraw.add(treasure);
						}
					}
				}
			}
			else if (rc.isPlayerControlledLeader()) {
				CharacterWrapper leader = new CharacterWrapper(rc.getGameObject());
				componentsToDraw.addAll(leader.getFollowingHirelings());
			}
		}

		// Biggest ones on the bottom
		Collections.sort(componentsToDraw);

		for (Iterator i = componentsToDraw.iterator(); i.hasNext();) {
			RealmComponent rc = (RealmComponent) i.next();
			int w = rc.getSize().width;
			int h = rc.getSize().height;

			TileLocation loc;
			if (rc.isSpell()) {
				loc = new TileLocation(this);
			}
			else {
				loc = ClearingUtility.getTileLocation(rc.getGameObject());
			}
			if (loc != null) { // Why would this ever be null?
				if (rc instanceof StateChitComponent) {
					StateChitComponent state = (StateChitComponent) rc;
					if (!state.hasBeenSeen()) {
						loc.clearing = null;
					}
				}
				if (!loc.hasClearing()) { // flying
					if (loc.isBetweenTiles()) {
						Point c1 = new Point(lastPaintLocation.x + (iconDimensions.width >> 1), lastPaintLocation.y + (iconDimensions.height >> 1));
						TileComponent otherTile = loc.getOther().tile;
						Point c2 = new Point(otherTile.lastPaintLocation.x + (iconDimensions.width >> 1), otherTile.lastPaintLocation.y + (iconDimensions.height >> 1));
						Point p = GraphicsUtil.midPoint(c1, c2);
						rc.paint(g.create(p.x - (w >> 1) - offset, p.y - (h >> 1) - offset, rc.getSize().width, rc.getSize().height));
					}
					else {
						rc.paint(g.create(offroad.x - (w >> 1) - offset, offroad.y - (h >> 1) - offset, rc.getSize().width, rc.getSize().height));
						offset += CHIT_PLACEMENT_OFFSET;
					}
				}
				else {
					String numString = loc.clearing.getNumString();
					Integer n = (Integer)clearingCount.get(numString);
					int count = n==null?0:n.intValue();
					int shift = count * CHIT_PLACEMENT_OFFSET;
//					int index = loc.clearing.getNum() - 1; // 0 through 5
//					int shift = clearingCount[index] * CHIT_PLACEMENT_OFFSET;
					Point p = loc.clearing.getAbsolutePosition();
					if (p == null) {
						// 1/3/2007 - This is fixed, so this code is probably unnecessary now. It was happening because
						// the "End Game" option was calling RealmUtility.resetGame(), which in turn was calling
						// RealmComponent.reset(). If you then pressed No or Cancel, the game would continue, but it would
						// have to refresh all the components, and that's disaster!! I'll leave this here for awhile, and see
						// if it ever happens again.
						if (!emergencyUpdateCalled) {
							System.err.println("The problem clearing here is " + loc.clearing.fullString() + " @ " + loc.clearing.parentToString());
							System.err.println("The location " + loc + " was derived for game object " + rc.getGameObject() + " which itself was derived from " + rc.getGameObject().getGameData().getDataName());
							System.err.println("This tile is derived from " + getGameObject().getGameData().getDataName());
							emergencyUpdateCalled = true;
							forceClearingPositionUpdate(true);
							JOptionPane.showMessageDialog(null, "forceClearingPositionUpdate was called.  Please check and see if dwellings appear on the map.  If\n" + "not, this solution didn't work.  If the dwellings are there, then it DID work, and I can finally\n" + "close BUG 372.  Please let Robin (robin@dewkid.com) know that you saw this message, and whether or\n" + "not you can still see dwellings and character tokens on the map.  Also, if you could send the error\n"
									+ "log (if any) to me, that would be awesome!  Thanks.", "Graphics Glitch Fixed?", JOptionPane.WARNING_MESSAGE);
							System.err.println("forceClearingPositionUpdate was called after p was null!");
							p = loc.clearing.getAbsolutePosition();
						}
					}
					if (loc.isBetweenClearings()) {
						p = GraphicsUtil.midPoint(p, loc.getOther().clearing.getAbsolutePosition());
					}
					if (p != null) {
						// if (rc==null) throw new IllegalStateException("rc is null!?!");
						// if (p==null) throw new IllegalStateException("p is null!?!");
						rc.paint(g.create(p.x - (w >> 1) - shift, p.y - (h >> 1) - shift, w, h));
						clearingCount.put(numString,count+1);
//						clearingCount[index]++;
					}
					else {
						System.err.println("p is null for " + rc.getGameObject().getName());
					}
				}
			}
			else {
				System.err.println("TileComponent:  TileLocation for " + rc.getGameObject().getName() + " is null?!?!");
			}
		}
	}

	public ArrayList<RealmComponent> getOffroadRealmComponents() {
		ArrayList<RealmComponent> found = new ArrayList<RealmComponent>();
		for (Iterator i = gameObject.getHold().iterator(); i.hasNext();) {
			GameObject obj = (GameObject) i.next();
			RealmComponent goc = RealmComponent.getRealmComponent(obj);
			String clearingNum = obj.getThisAttribute("clearing");
			if (goc instanceof StateChitComponent) {
				StateChitComponent state = (StateChitComponent) goc;
				if (!state.isFaceUp()) {
					continue; // as much as I hate this flow statement, I'm using it here to exclude face down chits from the view
				}
			}
			if (clearingNum == null) {
				found.add(goc);
			}
		}
		return found;
	}

	/**
	 * Returns a collection of all RealmComponents in this clearing. It does not directly return objects contained by other objects, except for RedSpecialChitComponent and TWT Sites
	 */
	public ArrayList<RealmComponent> getRealmComponentsAt(int clearing) {
		ArrayList<RealmComponent> found = new ArrayList<RealmComponent>();
		ArrayList hold = new ArrayList(gameObject.getHold());
		for (Iterator i = hold.iterator(); i.hasNext();) {
			GameObject obj = (GameObject) i.next();
			if (!obj.hasThisAttribute("otherClearing")) { // ignore components that are partway
				RealmComponent goc = RealmComponent.getRealmComponent(obj);
				String clearingNum = obj.getThisAttribute("clearing");
				if (goc instanceof RedSpecialChitComponent) {
					ArrayList innerHold = new ArrayList(obj.getHold());
					for (Iterator n = innerHold.iterator(); n.hasNext();) {
						GameObject chit = (GameObject) n.next();
						String innerClearingNum = chit.getThisAttribute("clearing");
						if (innerClearingNum != null && innerClearingNum.equals(String.valueOf(clearing))) {
							StateChitComponent innerState = (StateChitComponent) RealmComponent.getRealmComponent(chit);
							found.add(innerState);
						}
					}
				}
				if (clearingNum != null && clearingNum.equals(String.valueOf(clearing))) {
					found.add(goc);
				}
			}
		}
		return found;
	}

	/**
	 * Returns a collection of chits that can be shown to the player when they get the "Clues" result on a search table.
	 */
	public Collection getClues() {
		ArrayList clues = new ArrayList();
		for (Iterator i = gameObject.getHold().iterator(); i.hasNext();) {
			GameObject obj = (GameObject) i.next();
			RealmComponent goc = RealmComponent.getRealmComponent(obj);
			if (goc instanceof StateChitComponent) {
				StateChitComponent state = (StateChitComponent) goc;
				if (state instanceof RedSpecialChitComponent) { // resolve red specials...
					for (Iterator n = obj.getHold().iterator(); n.hasNext();) {
						GameObject chit = (GameObject) n.next();
						StateChitComponent innerState = (StateChitComponent) RealmComponent.getRealmComponent(chit);
						if (innerState.isFaceDown()) {
							clues.add(innerState);
						}
					}
				}
				if (state.isFaceDown()) { // only return face down chits! (who wants a clues result for face up chits!!)
					clues.add(state);
				}
			}
		}
		return clues;
	}

	private Ellipse2D.Float getClearingShape(Point p, int radius) {
		int diam = radius << 1;
		return new Ellipse2D.Float(p.x - radius, p.y - radius, diam, diam);
	}

	private void drawClearing(Graphics2D g, ClearingDetail detail, int radius, boolean dottedBorder) {
		Point p = detail.getPosition();
		Shape shape = getClearingShape(p, radius);
		if (dottedBorder) {
			g.setStroke(new BasicStroke(2, 0, 0, 1, dash_style, 0));
			g.draw(shape);
		}
		else {
			g.setStroke(new BasicStroke(1));
			g.fill(shape);
		}
	}

	private void drawPath(Graphics2D g, PathDetail detail, int size, boolean dottedBorder) {
		Shape shape = detail.getShape();
		Stroke stroke = new BasicStroke(detail.isNarrow() ? (size >> 1) : size);
		if (dottedBorder) {
			shape = stroke.createStrokedShape(shape);
			g.setStroke(new BasicStroke(2, 0, 0, 1, dash_style, 0));
			g.draw(shape);
		}
		else {
			g.setStroke(stroke);
			g.draw(shape);
		}
	}

	public ArrayList<StateChitComponent> setChitsFaceUp() {
		return setChitsFacing(true);
	}

	public void setChitsFaceDown() {
		setChitsFacing(false);
	}

	/**
	 * @return true if any Character is in this tile
	 */
	public boolean holdsCharacter() {
		for (Iterator i = gameObject.getHold().iterator(); i.hasNext();) {
			GameObject go = (GameObject) i.next();
			RealmComponent roc = RealmComponent.getRealmComponent(go);
			if (roc.isPlayerControlledLeader()) {
				return true;
			}
		}
		return false;
	}

	public void resetChitsSummoned() {
		ArrayList hold = new ArrayList(gameObject.getHold()); // to prevent concurrent modification when red specials are revealed
		for (Iterator i = hold.iterator(); i.hasNext();) {
			GameObject go = (GameObject) i.next();
			RealmComponent roc = RealmComponent.getRealmComponent(go);
			if (roc instanceof StateChitComponent) {
				StateChitComponent state = (StateChitComponent) roc;
				state.clearSummonedToday();
			}
		}
	}

	private ArrayList<StateChitComponent> setChitsFacing(boolean up) {
		ArrayList<StateChitComponent> flipped = new ArrayList<StateChitComponent>();
		ArrayList hold = new ArrayList(gameObject.getHold()); // to prevent concurrent modification when red specials are revealed
		for (Iterator i = hold.iterator(); i.hasNext();) {
			GameObject go = (GameObject) i.next();
			RealmComponent roc = RealmComponent.getRealmComponent(go);
			if (roc instanceof StateChitComponent) {
				StateChitComponent state = (StateChitComponent) roc;
				if (up!=state.isFaceUp()) {
					flipped.add(state);
					if (up) {
						state.setFaceUp();
					}
					else {
						state.setFaceDown();
					}
				}
			}
		}
		needsRepaint = true;
		return flipped;
	}

	public void setAlwaysPaint(boolean alwaysPaint) {
		this.alwaysPaint = alwaysPaint;
	}

	/**
	 * @return Returns the rotation.
	 */
	public int getRotation() {
		return rotation;
	}

	public void clearAdjacentTiles() {
		edgeTiles.clear();
	}
	public void putAdjacentTile(String rotatedEdge, TileComponent c) {
		edgeTiles.put(rotatedEdge, c);
	}

	public TileComponent getAdjacentTile(String rotatedEdge) {
		return edgeTiles.get(rotatedEdge);
	}

	public Collection<TileComponent> getAllAdjacentTiles() {
		return edgeTiles.values();
	}

	public int getEdgeTileCount() {
		return edgeTiles.size();
	}

	public void flip() {
		super.flip();
		gameObject.bumpVersion(); // Still having issues where other players aren't seeing a tile flip. This should solve that.
		needsRepaint = true;
	}

	/**
	 * Used when building a map
	 */
	public void doRepaint() {
		needsRepaint = true;
	}

	public boolean areMarkedClearings() {
		for (Iterator i = clearings[getFacingIndex()].iterator(); i.hasNext();) {
			ClearingDetail detail = (ClearingDetail) i.next();
			if (detail.isMarked()) {
				return true;
			}
		}
		return false;
	}

	public ArrayList<PathDetail> getHiddenPaths() {
		return getHiddenPaths(false);
	}
	public ArrayList<PathDetail> getHiddenPaths(boolean currentSideOnly) {
		ArrayList<PathDetail> list = new ArrayList<PathDetail>();
		for (int side = 0; side < 2; side++) {
			if (currentSideOnly && side!=getFacingIndex()) continue;
			for (Iterator i = paths[side].iterator(); i.hasNext();) {
				PathDetail path = (PathDetail) i.next();
				if (path.isHidden()) {
					list.add(path);
				}
			}
		}
		return list;
	}

	public ArrayList<PathDetail> getSecretPassages() {
		return getSecretPassages(false);
	}
	public ArrayList<PathDetail> getSecretPassages(boolean currentSideOnly) {
		ArrayList<PathDetail> list = new ArrayList<PathDetail>();
		for (int side = 0; side < 2; side++) {
			if (currentSideOnly && side!=getFacingIndex()) continue;
			for (Iterator i = paths[side].iterator(); i.hasNext();) {
				PathDetail path = (PathDetail) i.next();
				if (path.isSecret()) {
					list.add(path);
				}
			}
		}
		return list;
	}
	
	public ArrayList<String> getChitDescriptionList() {
		// The following will be a concatenated string that contains tilename and all state chit names:
		// For example:
		// high pass:ruins:lost city:hoard:lair:patter:flutter:ruins c
		//
		// If playing multiple boards, those designations will appear too
		// high pass:ruins:lost city:hoard b:lair:patter b:flutter:ruins c b
		ArrayList<String> list = new ArrayList<String>();
		
		list.add(getTileNameNoBoard().toLowerCase());
		ArrayList hold = new ArrayList(gameObject.getHold());
		for (Iterator i = hold.iterator(); i.hasNext();) {
			GameObject go = (GameObject) i.next();
			RealmComponent roc = RealmComponent.getRealmComponent(go);
			if (roc instanceof StateChitComponent) {
				StateChitComponent state = (StateChitComponent) roc;
				if (state.hasBeenSeen()) {
					list.addAll(getChitDescriptionList(state));
				}
			}
		}

		return list;
	}

	/**
	 * Recursive method for retrieving all held state chit names
	 */
	private ArrayList<String> getChitDescriptionList(StateChitComponent state) {
		ArrayList<String> list = new ArrayList<String>();
		String name = state.getGameObject().getName().toLowerCase();
		list.add(name);
		if (state instanceof WarningChitComponent) {
			// truncate letter on warning chits, so that (for example) "ruins m" and "ruins" are both returned
			list.add(name.substring(0,name.length()-2));
		}
		
		for (Iterator i = state.getGameObject().getHold().iterator(); i.hasNext();) {
			GameObject go = (GameObject) i.next();
			RealmComponent roc = RealmComponent.getRealmComponent(go);
			if (roc instanceof StateChitComponent) {
				StateChitComponent st = (StateChitComponent) roc;
				list.addAll(getChitDescriptionList(st));
			}
		}
		return list;
	}

	/**
	 * @return Returns the marked.
	 */
	public boolean isMarked() {
		return marked;
	}

	/**
	 * @param marked The marked to set.
	 */
	public void setMarked(boolean val) {
		marked = val;
		setMarkColor(DEFAULT_MARK_COLOR);
	}

	public Color getMarkColor() {
		return markColor;
	}

	public void setMarkColor(Color markColor) {
		this.markColor = markColor;
	}

	/**
	 * All sources of color in the tile
	 */
	public ArrayList<ColorMagic> getAllSourcesOfColor() {
		ArrayList<ColorMagic> list = new ArrayList<ColorMagic>();
		for (ClearingDetail clearing:getClearings()) {
			list.addAll(clearing.getAllSourcesOfColor(true));
		}
		for (RealmComponent rc:getOffroadRealmComponents()) {
			list.addAll(SpellUtility.getSourcesOfColor(rc));
		}
		return list;
	}
	public ArrayList<RealmComponent> getAllClearingComponents() {
		ArrayList<RealmComponent> ret = new ArrayList<RealmComponent>();
		for (ClearingDetail clearing:getClearings()) {
			ret.addAll(clearing.getClearingComponents());
		}
		return ret;
	}
	public ImageIcon getFaceUpIcon() {
		return getRepresentativeIconBigger();
	}

	public ImageIcon getMediumIcon() {
		boolean ap = alwaysPaint;
		alwaysPaint = true;
		ImageIcon icon = new ImageIcon(getImage().getScaledInstance(57,50, Image.SCALE_SMOOTH));
		alwaysPaint = ap;
		return icon;
	}
	public ImageIcon getRepresentativeIcon() {
		boolean ap = alwaysPaint;
		alwaysPaint = true;
		ImageIcon icon = new ImageIcon(getImage().getScaledInstance(86, 75, Image.SCALE_DEFAULT));
		alwaysPaint = ap;
		return icon;
	}
	public ImageIcon getRepresentativeIconBigger() {
		boolean ap = alwaysPaint;
		alwaysPaint = true;
		ImageIcon icon = new ImageIcon(getImage().getScaledInstance(86<<1, 75<<1, Image.SCALE_DEFAULT));
		alwaysPaint = ap;
		return icon;
	}
	public ImageIcon getTilePickIcon() {
		boolean ap = alwaysPaint;
		alwaysPaint = true;
		ImageIcon icon = new ImageIcon(getImage().getScaledInstance(86<<1, 75<<1, Image.SCALE_DEFAULT));
		alwaysPaint = ap;
		return icon;
	}
	public ImageIcon getTilePickFlipIcon() {
		boolean ap = alwaysPaint;
		alwaysPaint = true;
		ImageIcon icon = new ImageIcon(getFlipSideImage().getScaledInstance(86<<1, 75<<1, Image.SCALE_DEFAULT));
		alwaysPaint = ap;
		return icon;
	}

	public ImageIcon getRepresentativeFullSizeIcon() {
		boolean ap = alwaysPaint;
		alwaysPaint = true;
		ImageIcon icon = new ImageIcon(getImage());
		alwaysPaint = ap;
		return icon;
	}

	public ImageIcon getRepresentativeFullSizeFlipIcon() {
		boolean ap = alwaysPaint;
		alwaysPaint = true;
		ImageIcon icon = new ImageIcon(getFlipSideImage());
		alwaysPaint = ap;
		return icon;
	}

	public Rectangle getLastPaintLocation() {
		return lastPaintLocation;
	}

	public boolean isValley() {
		if( this.getGameObject().getThisAttribute("tile_type").equals("V")){return true;}
		return false;
	}
}