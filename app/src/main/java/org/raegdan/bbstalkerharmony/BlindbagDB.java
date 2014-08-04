package org.raegdan.bbstalkerharmony;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;

//////////////////////////////////////////////////
// BlindbagDB class - handling the main database
//////////////////////////////////////////////////
public class BlindbagDB implements Cloneable {

	/////////////////
	// P U B L I C //
	/////////////////
	public List<Wave> waves;
	public List<Blindbag> blindbags;

	public BlindbagDB() {
		super();
		waves = new ArrayList<Wave>();
		blindbags = new ArrayList<Blindbag>();
	}
	
	///////////////////////////////////////////////////////////////////
	// Loads blind bags database and collection from JSON into class.
	///////////////////////////////////////////////////////////////////
	public Boolean loadDB(Context context) 	{
		try {
			parseDB(getDB(context));
			parseCollection(_getCollection(context));
			parseWishlist(_getWishlist(context));
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	///////////////////////////////////////
	// Performs lookup through database
	// using Smart or Fast search methods.
	///////////////////////////////////////
	public BlindbagDB lookupDB(String query, Boolean smartSearch) {
		if (smartSearch) {
			performSmartSearch(query);
		} else {
			performFastSearch(query);
		}
		
		return this;
	}

	/////////////////////////////////////////////////
	// Performs reverse lookup by race and colors.
	/////////////////////////////////////////////////
	public BlindbagDB reverseLookup(String query) {
		try {
            JSONObject js = new JSONObject(query);

			for (int i = 0; i < blindbags.size(); i++) 	{
				Blindbag bb = blindbags.get(i);
				
				if ( !(js.getBoolean("alicorn") && bb.race == Blindbag.RACE_ALICORN) &&
						!(js.getBoolean("unicorn") && bb.race == Blindbag.RACE_UNICORN) &&
						!(js.getBoolean("pegasus") && bb.race == Blindbag.RACE_PEGASUS) &&
						!(js.getBoolean("earthen") && bb.race == Blindbag.RACE_EARTHEN) &&
						!(js.getBoolean("nonpony") && bb.race == Blindbag.RACE_NONPONY)) {
					bb.priority = 0;
				} else {
					if (js.getBoolean("mane") || js.getBoolean("body")) {
						int priority = 0;

						if (js.getBoolean("mane") && js.getBoolean("body")) {
							final double DEVQ = 0.3;
							Double md = colorDiff(bb.manecolor, js.getInt("manecolor"));
							Double bd = colorDiff(bb.bodycolor, js.getInt("bodycolor")) * 0.75;
							Double avg = (md + bd) / 2.0;
							Double dev = Math.abs(md - bd) * DEVQ;
							priority = 1000 - (int) (avg + dev);
						} else {
							if (js.getBoolean("mane")) {
								priority = 1000 - colorDiff(bb.manecolor, js.getInt("manecolor")).intValue();
							}

							if (js.getBoolean("body")) {
								priority = 1000 - colorDiff(bb.bodycolor, js.getInt("bodycolor")).intValue();
							}
						}

						bb.priority = priority;
					} else {
						bb.priority = 1;
					}
				}
				
				blindbags.set(i, bb);
			}
			
			prioritySort();
			
			return this;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	///////////////////////////////////////////////////////////////////////
	// Commits changes in collection and wishlist into SharedPreferences.
	///////////////////////////////////////////////////////////////////////
	public Boolean commitDB(Context context) {
		JSONArray coll = new JSONArray();
		JSONArray wl   = new JSONArray();	
		
		for (int i = 0; i < blindbags.size(); i++) 	{
			Integer count  = blindbags.get(i).count;
			Boolean wanted = blindbags.get(i).wanted;
			
			try {
				if (count > 0) {	
					coll.put(new JSONObject().put("uniqid", blindbags.get(i).uniqid).put("count", count));
				}
				
				if (wanted) {
					wl.put(blindbags.get(i).uniqid);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		Editor ed = sp.edit();
		ed.putString(COLLECTION_PREF_ID, coll.toString());
		ed.putString(WISHLIST_PREF_ID, wl.toString());
		ed.commit();

		return true;
	}
	
	//////////////////////////////////////////
	// Returns the blind bad data by its ID.
	//////////////////////////////////////////
	public Blindbag getBlindbagByUniqID(String uniqid) 	{
		for (int i = 0; i < blindbags.size(); i++) 	{
			if (blindbags.get(i).uniqid.equalsIgnoreCase(uniqid)) {
				return blindbags.get(i);
			}
		}
		
		return null;
	}
	
	/////////////////////////////////////
	// Returns the wave data by its ID.
	/////////////////////////////////////	
	public Wave getWaveByWaveID(String waveid) {
		Wave w = new Wave();
		
		for (int i = 0; i < waves.size(); i++) 	{
			if (waves.get(i).waveid.equalsIgnoreCase(waveid)) {
				w = waves.get(i);
			}
		}
		
		return w;
	}
	
	/////////////////////////////////////////////////
	// Returns all blind bags of a wave by wave id.
	/////////////////////////////////////////////////
	public BlindbagDB getWaveBBs(String waveid) {
		BlindbagDB outDB = this;
		
		for (int i = 0; i < outDB.blindbags.size(); i++) {
			if (!outDB.blindbags.get(i).waveid.equalsIgnoreCase(waveid)) {
				outDB.blindbags.get(i).priority = 0;
			}
		}
		
		return outDB;
	}
	
	///////////////////////////////////
	// Gets user collection database.
	///////////////////////////////////
	public BlindbagDB getCollection(Context context) {
		try {
			parseCollection(_getCollection(context));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		BlindbagDB outDB = this;
		
		for (int i = 0; i < outDB.blindbags.size(); i++) {
			if (outDB.blindbags.get(i).count < 1) {
				outDB.blindbags.get(i).priority = 0;
			}
		}
		
		return outDB;
	}
	
	/////////////////////////////////
	// Gets user wishlist database.
	/////////////////////////////////
	public BlindbagDB getWishlist(Context context) 	{
		try {
			parseWishlist(_getWishlist(context));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		BlindbagDB outDB = this;
		
		for (int i = 0; i < outDB.blindbags.size(); i++) {
			if (!outDB.blindbags.get(i).wanted) {
				outDB.blindbags.get(i).priority = 0;
			}
		}
		
		return outDB;
	}
	
	///////////////////////
	// Clones the object.
	///////////////////////
	public BlindbagDB clone(Context context) throws CloneNotSupportedException {
		BlindbagDB clone = (BlindbagDB) super.clone();
		clone.waves = new ArrayList<Wave>();
		clone.blindbags = new ArrayList<Blindbag>();
		
		for (int i = 0; i < waves.size(); i++) {
			clone.waves.add(waves.get(i).clone());
		}
		
		for (int i = 0; i < blindbags.size(); i++) {
			clone.blindbags.add(blindbags.get(i).clone());
		}
		
		try {
			clone.parseCollection(_getCollection(context));
			clone.parseWishlist(_getWishlist(context));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return clone;
	}
	
	/////////////////////////////
	// Dumps DB into JSON code.
	/////////////////////////////
	public String dumpDB(Context context)
	{
		JSONObject dump = new JSONObject();
		
		try {
			dump.put("collection", _getCollection(context));
			dump.put("wishlist", _getWishlist(context));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return dump.toString();
	}
	
	/////////////////////////////////////////////////////////
	// Restores DB from JSON dump.
	// Returns true on success;
	// false on error (database remains untouched on error)
	/////////////////////////////////////////////////////////
	public Boolean restoreDB(String dump, Context context) {
		JSONObject jsondump;
		
		try {
			jsondump = new JSONObject(dump);
		} catch (JSONException e) {
			return false;
		}
		
		try {
            parseCollection(jsondump.getJSONArray("collection"));
			parseWishlist(jsondump.getJSONArray("wishlist"));
		} catch (JSONException e) {
			try {
				parseCollection(_getCollection(context));
				parseWishlist(_getWishlist(context));
				return false;
			} catch (JSONException e1) {
				e1.printStackTrace();
			}		
		}
		
		commitDB(context);
		return true;
	}
	
	///////////////////
	// P R I V A T E //
	///////////////////
	
	protected final static String DB_ASSET = "database.json";
	protected final static String COLLECTION_PREF_ID = "bbcollection";
	protected final static String WISHLIST_PREF_ID = "bbwishlist";
	
	///////////////////////////////////////////////////////////////////////////////////
	// Smart Search: recognizes the query type by regexps from database
	// and sorts the results by priority.
	// About 10 times slower than Fast Search, but is smarter than Anatoly Wasserman.
	///////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	protected void performSmartSearch(String query) {
		String queryRegexp = queryToRegexp(query);
		BlindbagDB source = this;
		
		for (int i = 0; i < blindbags.size(); i++) 	{
			Blindbag bb = blindbags.get(i);
			
			Wave w = source.getWaveByWaveID(bb.waveid);
			
			Integer priority = 0;
			for (int j = 0; j < w.priorities.size(); j++) {				
				if (matchRegexp(w.priorities.get(j).regexp, query.toUpperCase(Locale.ENGLISH)) && (w.priorities.get(j).priority > priority)) {
					String priorityField = w.priorities.get(j).field;
					Object field = bb.getFieldByName(priorityField);
					
					// Exception in GFBN
					if (field == null) 	{
                        // do nothing
                    }
					
					// Field is string (e.g. name)
					else if (field instanceof String) {
						if (matchRegexp(queryRegexp.toUpperCase(Locale.ENGLISH), ((String) field).toUpperCase(Locale.ENGLISH))) {
							priority = w.priorities.get(j).priority;
						}							
					}
					
					// Field is list (e.g. bbids)
					else if (field instanceof List) {
						for (int k = 0; k < bb.bbids.size(); k++) {
							if (matchRegexp(queryRegexp.toUpperCase(Locale.ENGLISH), ((List<String>) field).get(k))) {
								priority = w.priorities.get(j).priority;
							}
						}							
					}
				}
			}
			
			bb.priority = priority;
			
			blindbags.set(i, bb);
		}
		
		prioritySort();
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	// Fast search: no query recognition, no sorting.
	// Just answers the question if the blind bag contains query in any of its fields.
	// About 10 times faster than Smart Search, but is as stupid as a soldier boot.
	////////////////////////////////////////////////////////////////////////////////////
	protected void performFastSearch(String query) {
		String queryRegexp = queryToRegexp(query);
		
		for (int i = 0; i < blindbags.size(); i++) 	{
			Blindbag bb = blindbags.get(i);
			
			bb.priority = 0;
			if (matchRegexp(queryRegexp.toUpperCase(Locale.ENGLISH), bb.name.toUpperCase(Locale.ENGLISH)) || bb.waveid.equals(query)) {
				bb.priority = 1;
				continue;
			}
			
			for (int j = 0; j < bb.bbids.size(); j++) {
				if (matchRegexp(queryRegexp.toUpperCase(Locale.ENGLISH), bb.bbids.get(j).toUpperCase(Locale.ENGLISH))) {
					bb.priority = 1;
					break;
				}
			}
			
			blindbags.set(i, bb);
		}	
	}
	
	///////////////////////////////
	// Sorts database by priority
	///////////////////////////////
	protected void prioritySort() {
		Boolean f = true;
        Blindbag buf;
		
		while (f) {
			f = false;
			for (int i = 0; i < blindbags.size() - 1; i++) 	{
				if (blindbags.get(i).priority < blindbags.get(i + 1).priority) {
					buf = blindbags.get(i);
					blindbags.set(i, blindbags.get(i + 1));
					blindbags.set(i+1, buf);
					f = true;
				}
			}
		}
	}
	
	/////////////////////
	// Storage fetching
	/////////////////////
	protected JSONObject getDB(Context context) throws JSONException, IOException {
		AssetManager am = context.getAssets();
		InputStream is = am.open(DB_ASSET);
		JSONObject db = new JSONObject(streamToString(is));
		is.close();
		return db;
	}
	
	protected JSONArray _getCollection(Context context) throws JSONException {
		SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		return new JSONArray(sp.getString(COLLECTION_PREF_ID, "[{\"uniqid\": \"\", \"count\": 0}]"));		
	}
	
	protected JSONArray _getWishlist(Context context) throws JSONException {
		SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		return new JSONArray(sp.getString(WISHLIST_PREF_ID, "[]"));		
	}
	
	////////////////////
	// Storage parsing
	////////////////////
	protected void parseDB(JSONObject db) throws JSONException {
		for (int i = 0; i < db.getJSONArray("waves").length(); i++) {
			Wave w = new Wave();
			w.waveid = db.getJSONArray("waves").getJSONObject(i).getString("waveid");
			w.year = db.getJSONArray("waves").getJSONObject(i).getString("year");
			w.format = db.getJSONArray("waves").getJSONObject(i).getString("format");
			w.name = db.getJSONArray("waves").getJSONObject(i).getString("name");

			for (int j = 0; j < db.getJSONArray("waves").getJSONObject(i).getJSONArray("priorities").length(); j++) {
				RegexpField rf = new RegexpField();
				rf.field =  db.getJSONArray("waves").getJSONObject(i).getJSONArray("priorities").getJSONObject(j).getString("field");
				rf.regexp =  db.getJSONArray("waves").getJSONObject(i).getJSONArray("priorities").getJSONObject(j).getString("regexp");
				rf.priority = db.getJSONArray("waves").getJSONObject(i).getJSONArray("priorities").getJSONObject(j).getInt("priority");
				w.priorities.add(rf);
			}
			
			waves.add(w);
		}
		
		for (int i = 0; i < db.getJSONArray("blindbags").length(); i++) {
			Blindbag bb = new Blindbag();
			bb.name = db.getJSONArray("blindbags").getJSONObject(i).getString("name");
			bb.uniqid = db.getJSONArray("blindbags").getJSONObject(i).getString("uniqid");
			bb.waveid = db.getJSONArray("blindbags").getJSONObject(i).getString("waveid");
			bb.wikiurl = db.getJSONArray("blindbags").getJSONObject(i).getString("wikiurl");
			
			String rawrace = db.getJSONArray("blindbags").getJSONObject(i).getString("race");
			bb.race = Blindbag.RACE_NONPONY;
			if (rawrace.equalsIgnoreCase("unicorn")) {
				bb.race = Blindbag.RACE_UNICORN;
			} else if (rawrace.equalsIgnoreCase("alicorn")) {
				bb.race = Blindbag.RACE_ALICORN;
			} else if (rawrace.equalsIgnoreCase("pegasus")) {
				bb.race = Blindbag.RACE_PEGASUS;
			} else if (rawrace.equalsIgnoreCase("earthen")) {
				bb.race = Blindbag.RACE_EARTHEN;
			}
			
			bb.manecolor = Integer.valueOf(db.getJSONArray("blindbags").getJSONObject(i).getString("mane"), 16);
			bb.bodycolor = Integer.valueOf(db.getJSONArray("blindbags").getJSONObject(i).getString("body"), 16);
			
			bb.priority = 1;
			
			for (int j = 0; j < db.getJSONArray("blindbags").getJSONObject(i).getJSONArray("bbids").length(); j++) 	{
				bb.bbids.add(db.getJSONArray("blindbags").getJSONObject(i).getJSONArray("bbids").getString(j));
			}
			
			blindbags.add(bb);
		}
	}
	
	protected void parseCollection(JSONArray collection) throws JSONException {
        Blindbag bb;

		for (int i = 0; i < blindbags.size(); i++) 	{
			bb = blindbags.get(i);
			Boolean found = false;
			
			for (int j = 0; j < collection.length(); j++) {
				JSONObject jo = collection.getJSONObject(j);
				
				if (jo.getString("uniqid").equalsIgnoreCase(bb.uniqid)) {
					bb.count = jo.getInt("count");
					found = true;
					break;
				}
			}
			
			if (!found) {
				bb.count = 0;
			}
			
			blindbags.set(i, bb);
		}
	}
	
	protected void parseWishlist(JSONArray wishlist) throws JSONException
	{
        Blindbag bb;
		for (int i = 0; i < blindbags.size(); i++) {
			bb = blindbags.get(i);
			bb.wanted = false;
			
			for (int j = 0; j < wishlist.length(); j++) {
				String wanted_bb = wishlist.getString(j);
				
				if ((wanted_bb.equalsIgnoreCase(bb.uniqid)) && (bb.count < 1)) {
					bb.wanted = true;
					break;
				}
			}
			
			blindbags.set(i, bb);
		}
	}
	
	//////////////////
	// Misc routines
	//////////////////
	protected String queryToRegexp(String query) {
		query = query.replaceAll("([^A-Za-z0-9\\.\\*])", "\\\\$1");
		query = query.replaceAll("\\*", ".{1,}?");
		query = ".*?" + query + ".*?";
		
		return query;
	}
	
	protected String streamToString(InputStream is) throws IOException {
		String s = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		for(String line = br.readLine(); line != null; line = br.readLine()) 
		{
			s += line;
		}
		br.close();
		return s;
	}
	
	protected Boolean matchRegexp(String regexp, String s) {
		return Pattern.compile(regexp).matcher(s).matches();
	}
	
	protected Double colorDiff(int color1, int color2) {
		int R1, G1, B1, R2, G2, B2;
		int[] lab1 = {0,0,0};
		int[] lab2 = {0,0,0};
		
		R1 = (color1 & 0xff0000) >> 16;
		G1 = (color1 & 0x00ff00) >> 8;
		B1 = (color1 & 0x0000ff);
		R2 = (color2 & 0xff0000) >> 16;
		G2 = (color2 & 0x00ff00) >> 8;
		B2 = (color2 & 0x0000ff);
		
		rgb2lab(R1, G1, B1, lab1);
		rgb2lab(R2, G2, B2, lab2);
		
		return Math.sqrt((lab1[0] - lab2[0]) * (lab1[0] - lab2[0]) + (lab1[1] - lab2[1]) * (lab1[1] - lab2[1]) + (lab1[2] - lab2[2]) * (lab1[2] - lab2[2]));
	}
	
	// Function taken at http://www.f4.fhtw-berlin.de/~barthel/ImageJ/ColorInspector/HTMLHelp/farbraumJava.htm
	public void rgb2lab(int R, int G, int B, int []lab) {
		//http://www.brucelindbloom.com
		  
		float r, g, b, X, Y, Z, fx, fy, fz, xr, yr, zr;
		float Ls, as, bs;
		float eps = 216.f/24389.f;
		float k = 24389.f/27.f;
		   
		float Xr = 0.964221f;  // reference white D50
		float Yr = 1.0f;
		float Zr = 0.825211f;
		
		// RGB to XYZ
		r = R/255.f; //R 0..1
		g = G/255.f; //G 0..1
		b = B/255.f; //B 0..1
		
		// assuming sRGB (D65)
		if (r <= 0.04045)
			r = r/12;
		else
			r = (float) Math.pow((r+0.055)/1.055,2.4);
		
		if (g <= 0.04045)
			g = g/12;
		else
			g = (float) Math.pow((g+0.055)/1.055,2.4);
		
		if (b <= 0.04045)
			b = b/12;
		else
			b = (float) Math.pow((b+0.055)/1.055,2.4);
		
		
		X =  0.436052025f*r     + 0.385081593f*g + 0.143087414f *b;
		Y =  0.222491598f*r     + 0.71688606f *g + 0.060621486f *b;
		Z =  0.013929122f*r     + 0.097097002f*g + 0.71418547f  *b;
		
		// XYZ to Lab
		xr = X/Xr;
		yr = Y/Yr;
		zr = Z/Zr;
				
		if ( xr > eps )
			fx =  (float) Math.pow(xr, 1/3.);
		else
			fx = (float) ((k * xr + 16.) / 116.);
		 
		if ( yr > eps )
			fy =  (float) Math.pow(yr, 1/3.);
		else
		fy = (float) ((k * yr + 16.) / 116.);
		
		if ( zr > eps )
			fz =  (float) Math.pow(zr, 1/3.);
		else
			fz = (float) ((k * zr + 16.) / 116);
		
		Ls = ( 116 * fy ) - 16;
		as = 500*(fx-fy);
		bs = 200*(fy-fz);
		
		lab[0] = (int) (2.55*Ls + .5);
		lab[1] = (int) (as + .5); 
		lab[2] = (int) (bs + .5);       
	} 
}
