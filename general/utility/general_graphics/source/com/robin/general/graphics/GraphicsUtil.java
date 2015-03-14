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
package com.robin.general.graphics;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

public class GraphicsUtil {

	/**
	 * This will return a Color object that is between a given source and target, based
	 * on percent.  A percent value of 100 will return the target, and a value of 0 will
	 * return the source.
	 *
	 * @param source		The source Color
	 * @param target		The target Color
	 * @param percent		A value from 0-100 that determines how like the target Color
	 *						the source Color should be
	 * @return				The resulting Color
	 */
	public static Color convertColor(Color source,Color target,int percent) {
		float r1,g1,b1;
		float r2,g2,b2;
		int r3,g3,b3;
		float p;
		
		percent = percent>100?100:percent;
		percent = percent<0?0:percent;
		
		if (percent==0) return source;
		else if (percent==100) return target;
		
		r1 = source.getRed();
		g1 = source.getGreen();
		b1 = source.getBlue();
		
		r2 = target.getRed();
		g2 = target.getGreen();
		b2 = target.getBlue();
		
		p = (float)percent/100;
		
		r3 = (int)(r1+((r2-r1)*p));
		g3 = (int)(g1+((g2-g1)*p));
		b3 = (int)(b1+((b2-b1)*p));
		
		r3 = r3>255?255:r3;
		g3 = g3>255?255:g3;
		b3 = b3>255?255:b3;
		r3 = r3<0?0:r3;
		g3 = g3<0?0:g3;
		b3 = b3<0?0:b3;
		
		return (new Color(r3,g3,b3));
	}
	
	/**
	 * Returns a compatible inverse Color for a given source
	 *
	 * @param source		Source Color to get the inverse of
	 *
	 * @return				A compatible inverse Color
	 */
	public static Color inverseColor(Color source) {
		int r = source.getRed();
		int g = source.getGreen();
		int b = source.getBlue();
		if (Math.abs(r-128)<20) r=255;
		if (Math.abs(g-128)<20) g=255;
		if (Math.abs(b-128)<20) b=255;
		return new Color(255-r,255-g,255-b);
	}
	
	/**
	 * A method to determine if two colors are precisely the same
	 *
	 * @return			true if both colors are precisely the same
	 */
	public static boolean equalColor(Color c1,Color c2) {
		if (c1!=null && c2!=null) {
			int r1 = c1.getRed();
			int g1 = c1.getGreen();
			int b1 = c1.getBlue();
			
			int r2 = c2.getRed();
			int g2 = c2.getGreen();
			int b2 = c2.getBlue();
			
			if (r1==r2 && g1==g2 && b1==b2)
				return true;
		}
		
		return false;
	}
	
	/**
	 * Draw a dashed line
	 *
	 * @param g				The Graphics object to draw with
	 * @param x1			The x coordinate of one end of the line
	 * @param y1			The y coordinate of one end of the line
	 * @param x2			The x coordinate of the other end of the line
	 * @param y2			The y coordinate of the other end of the line
	 * @param dashlength	The length of individual dashes
	 * @param spacelength	The length of the spacing between dashes
	 */
	public static void drawDashedLine(Graphics g,int x1,int y1,int x2,int y2,double dashlength, double spacelength) {
		if ((dashlength+spacelength)>0) {
			double linelength=Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
			double xincdashspace=(x2-x1)/(linelength/(dashlength+spacelength));
			double yincdashspace=(y2-y1)/(linelength/(dashlength+spacelength));
			double xincdash=(x2-x1)/(linelength/(dashlength));
			double yincdash=(y2-y1)/(linelength/(dashlength));
			int counter=0;
			for (double i=0;i<linelength-dashlength;i+=dashlength+spacelength) {
				g.drawLine((int)(x1+xincdashspace*counter),(int)(y1+yincdashspace*counter),
							(int)(x1+xincdashspace*counter+xincdash),(int)(y1+yincdashspace*counter+yincdash));
				counter++;
			}
			if ((dashlength+spacelength)*counter<=linelength)
				g.drawLine((int) (x1+xincdashspace*counter),(int)(y1+yincdashspace*counter),x2,y2);
		}
		else g.drawLine(x1,y1,x2,y2);
	}
	
	/**
	 * The accuracy of shortenedLineFar is crummy for some reason.  Lets try a different route.
	 */
	public static Point getPointOnLine(Point from,Point to,int pixelsOut) {
		double angle;
		int dx = to.x - from.x;
		int dy = to.y - from.y;
		if (dx==0 && dy==0) {
			angle = 0;
		}
		else {
			angle = Math.atan2((double)dy,(double)dx);
		}

		
		int x = (int)((pixelsOut * Math.cos(angle)) + from.x);
		int y = (int)((pixelsOut * Math.sin(angle)) + from.y);
		
		return new Point(x,y);
	}
	
	/**
	 * Determine the intersection point of two line segments, or return null if
	 * no intersection.
	 *
	 * @param line1P1		Coordinates of one end of line #1
	 * @param line1P2		Coordinates of the other end of line #1
	 * @param line2P1		Coordinates of one end of line #2
	 * @param line2P2		Coordinates of the other end of line #2
	 *
	 * @return				Coordinates of the intersection, or null if none exists
	 */
	public static Point lineSegmentIntersection(Point line1P1,Point line1P2,Point line2P1,Point line2P2) {
		double numA = ((line2P2.x - line2P1.x) * (line1P1.y - line2P1.y)) - 
				  ((line2P2.y - line2P1.y) * (line1P1.x - line2P1.x));
		double numB = ((line1P2.x - line1P1.x) * (line1P1.y - line2P1.y)) - 
				  ((line1P2.y - line1P1.y) * (line1P1.x - line2P1.x));
		double den = ((line2P2.y - line2P1.y) * (line1P2.x - line1P1.x)) -
				  ((line2P2.x - line2P1.x) * (line1P2.y - line1P1.y));
		if ((numA==0 || numB==0) && den==0) {
			// Coincident lines
			return null; // there is no precise intersection
		}
		else if (den==0) {
			// Parallel lines
			return null;
		}
		else {
			// The lines intersect...
			double ua = numA/den;
			double ub = numB/den;
			
			// ...but do the line SEGMENTS intersect?
			if (ua>=0.0 && ua<=1.0 && ub>=0.0 && ub<=1.0) {
				// Yes, the line segements intersect
				int x = line1P1.x + (int)(ua * (double)(line1P2.x - line1P1.x));
				int y = line1P1.y + (int)(ua * (double)(line1P2.y - line1P1.y));
				return new Point(x,y);
			}
		}
		return null; // no intersection
	}
	
	/**
	 * Determine the unique intersection point of a line segment and a rectangle.
	 *
	 * @param p1		Coordinates of one end of a line
	 * @param p2		Coordinates of the other end of a line
	 * @param r			Rectangle object
	 *
	 * @return			A Point representing the unique intersection, or null
	 *					if no unique intersection exists.
	 */
	public static Point uniqueLineSegmentRectangleIntersection(Point p1,Point p2,Rectangle r) {
		// Only four corners on a rectangle (well, duh!)
		Point[] corner = new Point[4];
		corner[0] = new Point( r.x         , r.y          );
		corner[1] = new Point( r.x+r.width , r.y          );
		corner[2] = new Point( r.x         , r.y+r.height );
		corner[3] = new Point( r.x+r.width , r.y+r.height );
		
		// Only four lines in the rectangle to intersect with
		Point[] intersect = new Point[4];
		intersect[0] = lineSegmentIntersection(corner[0],corner[1],p1,p2);
		intersect[1] = lineSegmentIntersection(corner[0],corner[2],p1,p2);
		intersect[2] = lineSegmentIntersection(corner[1],corner[3],p1,p2);
		intersect[3] = lineSegmentIntersection(corner[2],corner[3],p1,p2);
		
		int count = 0;
		Point found=null;
		for (int i=0;i<4;i++) {
			if (intersect[i]!=null) {
				found = intersect[i];
				count++;
			}
		}
		if (count>0) {
			// there is at least one intersection - doesn't matter if there are more - just return one!
			return found;
		}
		return null;
	}
	
	/**
	 * Draws a line with a set thickness
	 *
	 * @param g				The Graphics object to draw with
	 * @param x1			The x coordinate of one end of the line
	 * @param y1			The y coordinate of one end of the line
	 * @param x2			The x coordinate of the other end of the line
	 * @param y2			The y coordinate of the other end of the line
	 * @param thickness		The thickness of the line
	 */
	public static void drawThickLine(Graphics g,int x1,int y1,int x2,int y2,int thickness) {
		// End1
		Polar theLine = new Polar(new Point(x1,y1),new Point(x2,y2));
		Polar end1 = new Polar(theLine);
		end1.setLength(thickness>>1);
		end1.addAngle(90);
		int angle = end1.getAngle();
		end1 = new Polar(end1.getRect(),new Point(0,0));
		end1.setLength(thickness);
		end1.setAngle(angle+180);
		
		// End2
		theLine = new Polar(new Point(x2,y2),new Point(x1,y1));
		Polar end2 = new Polar(theLine);
		end2.setLength(thickness>>1);
		end2.addAngle(90);
		angle = end2.getAngle();
		end2 = new Polar(end2.getRect(),new Point(0,0));
		end2.setLength(thickness);
		end2.setAngle(angle+180);
		
		Polygon theThickLine = new Polygon();
		theThickLine.addPoint(end1.getOrigin().x,end1.getOrigin().y);
		theThickLine.addPoint(end1.getRect().x,end1.getRect().y);
		theThickLine.addPoint(end2.getOrigin().x,end2.getOrigin().y);
		theThickLine.addPoint(end2.getRect().x,end2.getRect().y);
		g.fillPolygon(theThickLine);
	}
	
	/**
	 * Find the shortest connecting line between two rectangles.
	 *
	 * @return			An array of two coordinates describing the line
	 */
	public static Point[] getConnectingLine(Rectangle r1,Rectangle r2) {
		// Find centers of rectangles
		int x1 = r1.x + (r1.width>>1);
		int y1 = r1.y + (r1.height>>1);
		int x2 = r2.x + (r2.width>>1);
		int y2 = r2.y + (r2.height>>1);
		
		// Find the intersection with each of the rectangles,
		// and the line between the two centers
		Point[] p = new Point[2];
		p[0] = uniqueLineSegmentRectangleIntersection(new Point(x1,y1),new Point(x2,y2),r1);
		p[1] = uniqueLineSegmentRectangleIntersection(new Point(x1,y1),new Point(x2,y2),r2);
		if (p[0]!=null && p[1]!=null) {
			return p;
		}
		return null;
	}
	
	/**
	 * Draw a line between two rectangles, with or without arrowheads on either end
	 *
	 * @param g				The Graphics object to draw with
	 * @param r1			Rectangle #1
	 * @param arrowHead1	If true, an arrowhead will be drawn pointing to Rectangle #1
	 * @param r2			Rectangle #2
	 * @param arrowHead2	If true, an arrowhead will be drawn pointing to Rectangle #2
	 */
	public static void drawArrowLine(Graphics g,Rectangle r1,boolean arrowHead1,Rectangle r2,boolean arrowHead2) {
		Point[] p = getConnectingLine(r1,r2);
		
		if (p!=null) {
			// Draw the line using the intersection points
			drawArrowLine(g,p[0],arrowHead1,p[1],arrowHead2);
		}
	}
	
	/**
	 * Draw a line between two points, with or without arrowheads on either end
	 *
	 * @param g				The Graphics object to draw with
	 * @param p1			Point #1
	 * @param arrowHead1	If true, an arrowhead will be drawn pointing to Point #1
	 * @param p1			Point #2
	 * @param arrowHead2	If true, an arrowhead will be drawn pointing to Point #2
	 */
	public static void drawArrowLine(Graphics g,Point p1,boolean arrowHead1,Point p2,boolean arrowHead2) {
		Polar end1 = new Polar(p2,p1);
		Polar end2 = new Polar(p1,p2);
		
		Point adjP1 = end1.getRect();
		Point adjP2 = end2.getRect();
		g.drawLine(adjP1.x,adjP1.y,adjP2.x,adjP2.y);
		
		// Draw arrowheads if desired
		int arrowBreadth = 60;
		int arrowLength = 10;
		Polygon head;
		if (arrowHead1) {
			head = new Polygon();
			head.addPoint(adjP1.x,adjP1.y);
			int angle = end1.getAngle();
			end1 = new Polar(end1.getRect(),new Point(0,0));
			end1.setAngle(angle+180);
			end1.setLength(arrowLength);
			end1.addAngle(arrowBreadth>>1);
			Point arrow = end1.getRect();
			head.addPoint(arrow.x,arrow.y);
			end1.addAngle(-arrowBreadth);
			arrow = end1.getRect();
			head.addPoint(arrow.x,arrow.y);
			g.fillPolygon(head);
		}
		if (arrowHead2) {
			head = new Polygon();
			head.addPoint(adjP2.x,adjP2.y);
			int angle = end2.getAngle();
			end2 = new Polar(end2.getRect(),new Point(0,0));
			end2.setAngle(angle+180);
			end2.setLength(arrowLength);
			end2.addAngle(arrowBreadth>>1);
			Point arrow = end2.getRect();
			head.addPoint(arrow.x,arrow.y);
			end2.addAngle(-arrowBreadth);
			arrow = end2.getRect();
			head.addPoint(arrow.x,arrow.y);
			g.fillPolygon(head);
		}
	}
	
	/**
	 * Get the shortest distance in pixels of a point (px,py) to a line
	 * segment (x1,y1) to (x2,y2)
	 *
	 * @param px		The x coordinate of the point
	 * @param py		The y coordinate of the point
	 * @param x1		The x coordinate of one end of the line
	 * @param y1		The y coordinate of one end of the line
	 * @param x2		The x coordinate of the other end of the line
	 * @param y2		The y coordinate of the other end of the line
	 *
	 * @return			The shortest distance in pixels
	 */
	public static int distancePoint2Line(int px,int py,int x1,int y1,int x2,int y2) {
		int dx = x2-x1;
		int dy = y2-y1;
		int intersectionX;
		int intersectionY;
		if (dx==0 && dy==0) {
			intersectionX = x1;
			intersectionY = y1;
		}
		else {
			double u = ((px-x1)*dx)+((py-y1)*dy);
			u = u/((dx*dx)+(dy*dy));
			if (u<0.0 || u>1.0)
				return 999999;
			intersectionX = (int)((double)x1 + (u*(double)dx));
			intersectionY = (int)((double)y1 + (u*(double)dy));
		}
		dx = intersectionX - px;
		dy = intersectionY - py;
		return (int)Math.sqrt((dx*dx)+(dy*dy));
	}
	
	/**
	 * "Capture" the graphics in a component to a file.
	 *
	 * @param c				The Component instance to capture
	 * @param filename		The path to use for saving the image
	 * @param overwrite		Use true if you want to overwrite the file, if exists
	 *
	 * @return				true on success
	 */
	 
/*

	This method has been commented out because it requires the Acme class library
	to function.  If you are interested in using this method, uncomment this and the
	import line above, and link in the Acme.jar file, available at:
	
			http://www.acme.com/java/software/
*/
	 
	public static boolean componentToGif(Component c,String filename,boolean overwrite) {
/*		Dimension d = c.getSize();
		Image image = c.createImage(d.width,d.height);
		Graphics ig = image.getGraphics();
		c.paint(ig);
		try {
			File file = new File("test.gif");
			if (overwrite || !file.exists()) {
				System.out.println("writing file...");
				FileOutputStream stream = new FileOutputStream(filename);
				GifEncoder ge = new GifEncoder(image,stream);
				ge.encode();
				stream.close();
				System.out.println("done writing file.");
				return true;
			}
		}
		catch(IOException err) {
		}
*/
		return false;
	}

	/**
	 * Utility method to draw and connect two rectangles with a directional line.
	 */
	public static void connectRect(Graphics g,Rectangle r1,boolean arrow1,Rectangle r2,boolean arrow2,int borderSize) {
		r1 = new Rectangle(r1);
		r2 = new Rectangle(r2);
		g.drawRect(r1.x,r1.y,r1.width,r1.height);
		g.drawRect(r2.x,r2.y,r2.width,r2.height);
		if (r2.x<r1.x) {
			// swap 'em.
			Rectangle r = new Rectangle(r1);
			r1 = new Rectangle(r2);
			r2 = new Rectangle(r);
		}
		
		Point p1 = new Point(r1.x+r1.width+borderSize,r1.y+(r1.height>>1));
		Point p2 = new Point(r2.x-borderSize,r2.y+(r2.height>>1));
		drawArrowLine(g,p1,arrow1,p2,arrow2);
	}
	/**
	 * Returns a Dimension object describing the precise size of a given string object, based on the given
	 * Graphics context.
	 */
	public static Dimension getStringDimension(Graphics g,String string) {
		FontMetrics fm = g.getFontMetrics();
		int w = fm.stringWidth(string);
		int h = fm.getAscent();
		return new Dimension(w,h);
	}
	public static Dimension getStringDimension(JComponent c,String string) {
		FontMetrics fm = c.getFontMetrics(c.getFont());
		int w = fm.stringWidth(string);
		int h = fm.getAscent();
		return new Dimension(w,h);
	}
	/**
	 * @param g			Graphics context.
	 * @param x			The right justified x coordinate.
	 * @param y			The baseline y coordinate.
	 * @param string	The string to draw.
	 */
	public static void drawRightJustifiedString(Graphics g,int x,int y,String string) {
		Dimension d = getStringDimension(g,string);
		g.drawString(string,x-d.width,y);
	}
	public static void drawCenteredString(Graphics g,Rectangle r,String string) {
		drawCenteredString(g,r.x,r.y,r.width,r.height,string);
	}
	public static void drawCenteredString(Graphics g,int x,int y,int w,int h,String string) {
		Dimension d = getStringDimension(g,string);
		int offx = x + (w>>1) - (d.width>>1);
		int offy = y + (h>>1) + (d.height>>1);
		g.drawString(string,offx,offy);
	}
	public static void drawCenteredStrings(Graphics g,int x,int y,int w,int h,Object[] string) {
		Dimension d = getStringDimension(g,string[0].toString());
		int totalHeight = (string.length-1)*d.height;
		int offy = y + (h>>1) - (totalHeight>>1);// + (d.height>>1);
		for (int i=0;i<string.length;i++) {
			d = getStringDimension(g,string[i].toString());
			int offx = x + (w>>1) - (d.width>>1);
			g.drawString(string[i].toString(),offx,offy);
			offy += d.height;
		}
	}
	public static void drawCenteredImageIcon(Graphics g,Rectangle r,ImageIcon image) {
		int offx = r.x + (r.width>>1) - (image.getIconWidth()>>1);
		int offy = r.y + (r.height>>1) - (image.getIconHeight()>>1);
		g.drawImage(image.getImage(),offx,offy,null);
	}
	public static void drawSoftenedShape(Graphics2D g,Shape shape,Color mainColor,boolean dark) {
		Color softColor;
		if (dark) {
			softColor = new Color(0,0,0,100);
		}
		else {
			softColor = new Color(255,255,255,100);
		}
		g.setColor(mainColor);
		g.fill(shape);
		g.setColor(softColor);
		g.draw(shape);
	}
	public static ImageIcon overlayImages(ImageIcon base,ImageIcon overlay) {
		BufferedImage bi = new BufferedImage(base.getIconWidth(),base.getIconHeight(),BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = (Graphics2D)bi.getGraphics();
		g.drawImage(base.getImage(),0,0,null);
		g.drawImage(overlay.getImage(),0,0,null);
		return new ImageIcon(bi);
	}
	public static Point2D rotate(Point2D point,Point2D center,double angleRadians) {
		AffineTransform transform = new AffineTransform();
		transform.rotate(angleRadians,center.getX(),center.getY());
		return transform.transform(point,new Point2D.Double());
	}
	public static Point midPoint(Point p1,Point p2) {
		return new Point((p1.x+p2.x)>>1,(p1.y+p2.y)>>1);
	}
	public static Point asPoint(String s) {
		StringTokenizer st = new StringTokenizer(s,",");
		int x = Integer.valueOf(st.nextToken()).intValue();
		int y = Integer.valueOf(st.nextToken()).intValue();
		return new Point(x,y);
	}
    public static Point shortenedLineFar(Point p1, Point p2, int pixelsShort) {
        Polar theLine = new Polar(p1, p2);
        int length = theLine.getLength() - pixelsShort;
        if(length < 0)
            length = 0;
        theLine.setLength(length);
        return theLine.getRect();
    }

	public static ImageIcon copyImageIcon(ImageIcon icon) {
		BufferedImage bi = new BufferedImage(icon.getIconWidth(),icon.getIconHeight(),BufferedImage.TYPE_4BYTE_ABGR);
		bi.getGraphics().drawImage(icon.getImage(),0,0,null);
		return new ImageIcon(bi);
	}
	public static ImageIcon componentToIcon(JComponent component) {
		Dimension s = component.getPreferredSize();
		BufferedImage bi = new BufferedImage(s.width,s.height,BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = bi.getGraphics();
		component.paint(g);
		return new ImageIcon(bi);
	}
	public static boolean saveImageToPNG(ImageIcon imageIcon,File file) {
		return saveImageToFile(imageIcon,file,"PNG");
	}
	public static boolean saveImageToJPG(ImageIcon imageIcon,File file) {
		return saveImageToFile(imageIcon,file,"JPG");
	}
	private static boolean saveImageToFile(ImageIcon imageIcon,File file,String type) {
		try {
//			System.out.println("writing image: "+imageIcon.getIconWidth()+" x "+imageIcon.getIconHeight());
			
			// Why this is necessary, I have no idea, but it solves my problem with blank images being written
			BufferedImage bi = new BufferedImage(imageIcon.getIconWidth(),imageIcon.getIconHeight(),BufferedImage.TYPE_4BYTE_ABGR);
			bi.getGraphics().drawImage(imageIcon.getImage(),0,0,null);
			
			FileOutputStream fileoutputstream = new FileOutputStream(file);
			ImageIO.write(bi,type,fileoutputstream);
			
			fileoutputstream.close();
			return true;
		}
		catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	protected static final double radians_degrees60 =  (Math.PI * 60.0)/180.0;
	public static void main(String[]args) {
		Point2D.Double start = new Point2D.Double(5,1);
		Point2D.Double center = new Point2D.Double(5,5);
		System.out.println(rotate(start,center,radians_degrees60*1));
	}
}