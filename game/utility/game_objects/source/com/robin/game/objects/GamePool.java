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
package com.robin.game.objects;

import java.util.*;

public class GamePool extends ArrayList {

	public static final int RANDOM = 0;
	public static final int FROM_BEGINNING = 1;
	public static final int FROM_END = 2;
	
	public static final String RANDOM_NAME = "Random";
	public static final String FROM_BEGINNING_NAME = "From beginning";
	public static final String FROM_END_NAME = "From end";
	
	private Random random;
	
	public GamePool() {
		super();
		random = new Random();
	}
	
	public GamePool(Collection c) {
		super();
		addAll(c);
		random = new Random();
	}
	
	public GameObject getGameObject(int index) {
		return (GameObject)get(index);
	}

	/**
	 * Overridden method guarantees that the added object is a GameObject
	 */
	public boolean add(Object o) {
		if (o instanceof GameObject) {
			return super.add(o);
		}
		return false;
	}
	/**
	 * Overridden method guarantees that the added collection contains only GameObjects
	 */
	public boolean addAll(Collection c) {
		for (Iterator i=c.iterator();i.hasNext();) {
			if (!(i.next() instanceof GameObject)) {
				return false;
			}
		}
		return super.addAll(c);
	}
	public ArrayList pick(int number,int type) {
		GamePool temp = new GamePool();
		move(temp,number,type);
		return temp;
	}
	
	public GameObject findFirst(String keyVals) {
		return findFirst(makeKeyVals(keyVals));
	}
	public GameObject findFirst(Collection keyVals) {
		ArrayList<GameObject> list = find(keyVals);
		if (!list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}
	
	/**
	 * Locates all GameObjects that have all members of "keyVals" in their attributes
	 */
	public ArrayList<GameObject> find(String keyVals) {
		return find(makeKeyVals(keyVals));
	}
	/**
	 * Locates all GameObjects that have all members of "keyVals" in their attributes
	 */
	public ArrayList<GameObject> find(Collection keyVals) {
		ArrayList<GameObject> foundObjects = new ArrayList<GameObject>();
		for (int i=0;i<size();i++) {
			GameObject go = (GameObject)get(i);
			if (go.hasAllKeyVals(keyVals)) {
				foundObjects.add(go);
			}
		}
		return foundObjects;
	}
	/**
	 * This is useful for translating the hold into a generic typed array
	 */
	public ArrayList<GameObject> findAll() {
		ArrayList<GameObject> foundObjects = new ArrayList<GameObject>();
		for (int i=0;i<size();i++) {
			GameObject go = (GameObject)get(i);
			foundObjects.add(go);
		}
		return foundObjects;
	}
	/**
	 * Locates and extracts (removes) all GameObjects that have all members of "keyVals" in their attributes
	 */
	public ArrayList<GameObject> extract(String keyVals) {
		return extract(makeKeyVals(keyVals),0);
	}
	public ArrayList<GameObject> extract(String keyVals,int limit) {
		return extract(makeKeyVals(keyVals),limit);
	}
	/**
	 * Locates and extracts (removes) all GameObjects that have all members of "keyVals" in their attributes
	 */
	public ArrayList<GameObject> extract(Collection keyVals) {
		return extract(keyVals,0);
	}
	/**
	 * Locates and extracts (removes) all GameObjects that have all members of "keyVals" in their attributes
	 * 
	 * @param keyVals		The keyvals
	 * @param limit		The maximum number of objects to extract, or if less than 1, all of them.
	 */
	public ArrayList<GameObject> extract(Collection keyVals,int limit) {
		ArrayList<GameObject> extractedObjects = find(keyVals);
		for (GameObject extracted:extractedObjects) {
			remove(extracted);
			if (limit>0) {
				limit--;
				if (limit==0) {
					break;
				}
			}
		}
		return extractedObjects;
	}
	/**
	 * Transfers GameObjects from one pool to another.  Returns the number of objects moved.
	 *
	 * @param to		GamePool that will receive all the objects from this pool
	 * @return			Count of objects moved
	 */
	public int move(GamePool to) {
		return move(to,size());
	}
	/**
	 * Transfers GameObjects from one pool to another.  Returns the number of objects moved.
	 *
	 * @param to		GamePool that will receive the objects
	 * @param number	Count of objects to move randomly
	 * @return			Count of objects moved
	 */
	public int move(GamePool to,int number) {
		return move(to,number,RANDOM);
	}
	/**
	 * Transfers GameObjects from one pool to another.  Returns the number of objects moved.
	 *
	 * @param to		GamePool that will receive the objects
	 * @param number	Count of objects to move
	 * @param type		Method of move: RANDOM, FROM_BEGINNING, FROM_END
	 * @return			Count of objects moved
	 */
 	public int move(GamePool to,int number,int type) {
 		int count = 0;
		while(size()>0 && number>0) {
			int n;
			switch(type) {
				case RANDOM:
					n = random.nextInt(size());
					break;
				case FROM_BEGINNING:
					n = 0;
					break;
				case FROM_END:
					n = size()-1;
					break;
				default:
					throw new IllegalArgumentException("Invalid type given to GamePool.move(...): "+type);
			}
			GameObject go = getGameObject(n);
			remove(go);
			to.add(go);			
			count++;
			number--;
		}
		return count;
	}
	/**
	 * Distributes GameObjects from one pool to another.  Distribution is not the same as moving:  when
	 * an object is distributed to a pool, it is added to the contents of one of the objects in the pool.
	 * 
	 * @param dist		The pool receiving a the distribution of all the objects in this pool
	 * @return			Returns the number of objects distributed.
	 */
	public int distribute(GamePool dist) {
		return distribute(dist,size());
	}
	/**
	 * Distributes GameObjects from one pool to another.  Distribution is not the same as moving:  when
	 * an object is distributed to a pool, it is added to the contents of one of the objects in the pool.
	 * 
	 * @param dist		The pool receiving the distribution
	 * @param number	The count of objects to distribute randomly to dist
	 * @return			Returns the number of objects distributed.
	 */
	public int distribute(GamePool dist,int number) {
		return distribute(dist,number,RANDOM);
	}
	/**
	 * Distributes GameObjects from one pool to another.  Distribution is not the same as moving:  when
	 * an object is distributed to a pool, it is added to the contents of one of the objects in the pool.
	 * 
	 * @param dist		The pool receiving the distribution
	 * @param number	The count of objects to distribute to dist
	 * @param type		Method of selection of objects from this pool:  RANDOM, FROM_BEGINNING, FROM_END.  Irregardless
	 *					of this method, objects are ALWAYS added to the distributed pool starting with the first,
	 *					and cycling through the list until number has been reached.  If you want the distributed
	 *					pool to be equally random, simply shuffle it before doing the distribution.
	 * @return			Returns the number of objects distributed.
	 */
	public int distribute(GamePool dist,int number,int type) {
 		int count = 0;
//System.out.println("size = "+size());
//System.out.println("number = "+number);
//System.out.println("dist.size = "+dist.size());
		while(size()>0 && number>0 && dist.size()>0) {
			int n;
			switch(type) {
				case RANDOM:
					n = random.nextInt(size());
					break;
				case FROM_BEGINNING:
					n = 0;
					break;
				case FROM_END:
					n = size()-1;
					break;
				default:
					throw new IllegalArgumentException("Invalid type given to GamePool.move(...): "+type);
			}
			GameObject go = getGameObject(n);
			remove(go);
			
			int distIndex = count % dist.size();
			GameObject goAcceptor = dist.getGameObject(distIndex);
			goAcceptor.add(go);
//System.out.println("distribute "+go+" to "+goAcceptor);
			count++;
			number--;
		}
		return count;
	}
	/**
	 * Mixes up the pool.  Call more than once for iterative shuffles.
	 */
	public void shuffle() {
		if (size()>1) {
			// There needs to be at least 2 objects for this to even make sense!
			int iterations = size() + random.nextInt(size());
			for (int i=0;i<iterations;i++) {
				int n1 = random.nextInt(size());
				int n2;
				while((n2=random.nextInt(size()))==n1); // find a DIFFERENT index
				
				// Swap
				GameObject o1 = getGameObject(n1);
				GameObject o2 = getGameObject(n2);
				set(n1,o2);
				set(n2,o1);
			}
		}
	}
	
	public static int getTransferType(String transferTypeName) {
		if (transferTypeName.equals(RANDOM_NAME)) {
			return RANDOM;
		}
		else if (transferTypeName.equals(FROM_BEGINNING_NAME)) {
			return FROM_BEGINNING;
		}
		else if (transferTypeName.equals(FROM_END_NAME)) {
			return FROM_END;
		}
		return RANDOM;
	}
	public static String getTransferName(int transferType) {
		switch(transferType) {
			case RANDOM:			return RANDOM_NAME;
			case FROM_BEGINNING:	return FROM_BEGINNING_NAME;
			case FROM_END:			return FROM_END_NAME;
		}
		return null;
	}
	/**
	 * Convenience method for making a keyVal collection from a String
	 */
	public static ArrayList<String> makeKeyVals(String string) {
		StringTokenizer tokens = new StringTokenizer(string,",");
		ArrayList<String> keyVals = new ArrayList<String>();
		while(tokens.hasMoreTokens()) {
			keyVals.add(tokens.nextToken());
		}
		return keyVals;
	}
}