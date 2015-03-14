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
package com.robin.magic_realm.components.quest.requirement;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import com.robin.game.objects.GameObject;
import com.robin.general.util.StringUtilities;
import com.robin.magic_realm.components.quest.*;
import com.robin.magic_realm.components.wrapper.CharacterWrapper;

public class QuestRequirementSearchResult extends QuestRequirement {
	private static Logger logger = Logger.getLogger(QuestRequirementSearchResult.class.getName());
	
	public static final String REQ_TABLENAME = "_tn";
	public static final String RESULT1 = "_r1";
	public static final String RESULT2 = "_r2";
	public static final String RESULT3 = "_r3";
	public static final String REQUIRES_GAIN = "_rqg";
	public static final String TARGET_REGEX = "_rx";
	public static final String LOCATION = "_lcn";
	public static final String TARGET_LOC = "_tlcn";
	private static final String[] ALL_RESULTS = new String[]{RESULT1,RESULT2,RESULT3};
	
	public QuestRequirementSearchResult(GameObject go) {
		super(go);
	}

	protected boolean testFulfillsRequirement(JFrame frame, CharacterWrapper character, QuestRequirementParams reqParams) {
		SearchTableType reqTable = getRequiredSearchTable();
		if (reqParams!=null && reqTable!=SearchTableType.Any && !reqTable.toString().equals(reqParams.actionName)) {
			logger.fine("Search table name "+reqParams.actionName+" does not match "+reqTable);
			return false;
		}
		ArrayList<SearchResultType> acceptibleSearchResults = getAcceptableSearchResults();
		if (reqParams!=null && reqParams.searchType!=null && acceptibleSearchResults.contains(reqParams.searchType)) {
			if (!requiresGain() || reqParams.searchHadAnEffect) {
				// So far so good.  Now make sure the search target is accurate if a regex was specified
				String regex = getTargetRegEx();
				Pattern pattern = regex!=null && regex.trim().length()>0?Pattern.compile(regex):null;
				boolean regexGood = pattern==null || (reqParams.targetOfSearch!=null && pattern.matcher(reqParams.targetOfSearch.getName()).find());
				QuestLocation ql = getQuestLocation();
				boolean qlGood = ql==null || ql.locationMatchAddress(frame,character);
				QuestLocation tl = getTargetLocation();
				boolean tlGood = tl==null || (reqParams.targetOfSearch!=null && tl.locationMatchAddress(frame,character,reqParams.targetOfSearch));
				
				if (!regexGood) {
					logger.fine("The target of search ("+reqParams.targetOfSearch+") didn't match expected pattern: "+regex);
				}
				if (!qlGood) {
					logger.fine("The character's location doesn't match the QuestLocation: "+ql.getName());
				}
				if (!tlGood) {
					logger.fine("The target of the character's search doesn't match the TargetLocation: "+tl.getName());
				}
				
				return regexGood && qlGood && tlGood;
			}
			else {
				logger.fine("Requires some type of search gain, and there wasn't any.");
			}
		}
		else {
			if (reqParams==null) {
				logger.fine("No search was done.");
			}
			else {
				logger.fine("Search type "+reqParams.searchType+" wasn't among the acceptable search results: "+StringUtilities.collectionToString(acceptibleSearchResults,","));
			}
		}
		return false;
	}
	protected String buildDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Must get a search result of ");
		sb.append(getSearchResult1());
		SearchResultType r2 = getSearchResult2();
		if (r2!=null) sb.append(" or "+r2);
		SearchResultType r3 = getSearchResult3();
		if (r3!=null) sb.append(" or "+r3);
		SearchTableType table = getRequiredSearchTable();
		if (table!=SearchTableType.Any) {
			sb.append(" from ");
			sb.append(table);
		}
		String rx = getTargetRegEx();
		boolean hasRx = rx!=null && rx.trim().length()>0;
		QuestLocation tl = getTargetLocation();
		if (hasRx || tl!=null) {
			sb.append(" while searching ");
			if (hasRx) {
				sb.append("/");
				sb.append(rx);
				sb.append("/");
			}
			if (hasRx && tl!=null) {
				sb.append(" or ");
			}
			if (tl!=null) {
				sb.append("<");
				sb.append(tl.getName());
				sb.append(">");
			}
		}
		QuestLocation ql = getQuestLocation();
		if (ql!=null) {
			sb.append(" at ");
				sb.append("<");
				sb.append(ql.getName());
				sb.append(">");
		}
		if (requiresGain()) {
			sb.append(", with an effect");
		}
		sb.append(".");
		return sb.toString();
	}
	public RequirementType getRequirementType() {
		return RequirementType.SearchResult;
	}
	public SearchTableType getRequiredSearchTable() {
		return SearchTableType.valueOf(getString(REQ_TABLENAME));
	}
	public ArrayList<SearchResultType> getAcceptableSearchResults() {
		ArrayList<SearchResultType> list = new ArrayList<SearchResultType>();
		
		for(String key:ALL_RESULTS) {
			SearchResultType type = null;
			try {
				type = SearchResultType.valueOf(getString(key));
			}
			catch(IllegalArgumentException ex) {
				type = null;
			}
			if (type!=null) {
				if (type==SearchResultType.Any) {
					for(SearchResultType s:SearchResultType.values()) {
						if (s!=SearchResultType.Any && !list.contains(s)) {
							list.add(s);
						}
					}
				}
				else if (!list.contains(type)) {
					list.add(type);
				}
			}
		}

		return list;
	}
	public SearchResultType getSearchResult1() {
		return SearchResultType.valueOf(getString(RESULT1));
	}
	public SearchResultType getSearchResult2() {
		try {
			return SearchResultType.valueOf(getString(RESULT2));
		}
		catch(IllegalArgumentException ex) {
			return null;
		}
	}
	public SearchResultType getSearchResult3() {
		try {
			return SearchResultType.valueOf(getString(RESULT3));
		}
		catch(IllegalArgumentException ex) {
			return null;
		}
	}
	public boolean requiresGain() {
		return getBoolean(REQUIRES_GAIN);
	}
	public String getTargetRegEx() {
		return getString(TARGET_REGEX);
	}
	
	public QuestLocation getQuestLocation() {
		String id = getString(LOCATION);
		if (id!=null) {
			GameObject go = getGameData().getGameObject(Long.valueOf(id));
			if (go!=null) {
				return new QuestLocation(go);
			}
		}
		return null;
	}
	
	public QuestLocation getTargetLocation() {
		String id = getString(TARGET_LOC);
		if (id!=null) {
			GameObject go = getGameData().getGameObject(Long.valueOf(id));
			if (go!=null) {
				return new QuestLocation(go);
			}
		}
		return null;
	}
	
	public void updateIds(Hashtable<Long, GameObject> lookup) {
		updateIdsForKey(lookup,LOCATION);
		updateIdsForKey(lookup,TARGET_LOC);
	}
}