package com.lmn.Arbiter_Android.Map.Helpers.Parsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import org.json.*;
import android.util.Log;

import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.BaseClasses.Tileset;
import com.lmn.Arbiter_Android.BaseClasses.Server;

public class ParseGetCapabilities {
	private static final String LAYER_TAG = "Layer";
	private static final String FEATURE_TYPE = "Name";
	private static final String LAYER_TITLE = "Title";
	private static final String LAYER_BOUNDING_BOX = "BoundingBox";
	private static final String LAYER_SRS = "SRS";
	
	private ParseGetCapabilities(){}
	
	private static ParseGetCapabilities parser = null;
	
	public static ParseGetCapabilities getParser(){
		if(parser == null){
			parser = new ParseGetCapabilities();
		}
		
		return parser;
	}
	
	/**
	 * Parse the getCapabilities request
	 * @param server Object with the current servers info
	 * @param reader The reader for reading in the request response
	 * @return An ArrayList of parsed Layer objects containing the layers info
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public ArrayList<Layer> parseGetCapabilities(Server server, BufferedReader reader) throws XmlPullParserException, IOException{
		XmlPullParserFactory factory;
		factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(false);
		
		XmlPullParser pullParser = factory.newPullParser();
		pullParser.setInput(reader);
		
		String featureType = null;
		String title = null;
		String srs = null;
		
		String boundingBox = null;
		String minx = null; 
		String miny = null;
		String maxx = null;
		String maxy = null;
		
		String eventName;
		
		// For checking to see if the parser is in a layer element because
		// if not, we don't care about any of the data.
		int inLayerTag = 0;
		
		ArrayList<Layer> layers = new ArrayList<Layer>();
		// TODO There was a bug here where the style 
		int eventType = pullParser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT){
			eventName = pullParser.getName();
			
			if(inLayerTag == 2){
				if(eventType == XmlPullParser.START_TAG){
					
					if(eventName.equalsIgnoreCase(FEATURE_TYPE)){
						if(pullParser.next() == XmlPullParser.TEXT){
							if(featureType == null){
								featureType = pullParser.getText();
							}
						}
					} else if(eventName.equalsIgnoreCase(LAYER_TITLE)){
						if(pullParser.next() == XmlPullParser.TEXT){
							if(title == null){
								title = pullParser.getText();
							}
						}
					}  else if(eventName.equalsIgnoreCase(LAYER_SRS)){
                        if(pullParser.next() == XmlPullParser.TEXT){
                            if(srs == null){
                                    srs = pullParser.getText();
                            }
                        }
					} else if(eventName.equalsIgnoreCase(LAYER_BOUNDING_BOX)){
						minx = pullParser.getAttributeValue(null, "minx");
						miny = pullParser.getAttributeValue(null, "miny");
						maxx = pullParser.getAttributeValue(null, "maxx");
						maxy = pullParser.getAttributeValue(null, "maxy");
						
						if(boundingBox == null){
							boundingBox = minx + ", " + miny + ", " + maxx + ", " + maxy;
						}
					}
				}else if(eventType == XmlPullParser.END_TAG){
					if(eventName.equalsIgnoreCase(LAYER_TAG)){
						// Create the new layer object, and since this was the end of 
						// the layer element, specify that we're out.
						layers.add(new Layer(-1, featureType, null, server.getId(), server.getName(), 
								server.getUrl(), title, srs, boundingBox, "white", -1, false, "false"));
						inLayerTag -= 1;
						
						// Reset the fields to prepare for the next layer
						// Needed for the null checks
						featureType = null;
						title = null;
						srs = null;
						boundingBox = null;
					}
				}
			}else{
				if(eventType == XmlPullParser.START_TAG){
					eventName = pullParser.getName();
					
					if(eventName.equalsIgnoreCase(LAYER_TAG)){
						inLayerTag += 1;
					}
				}else if(eventType == XmlPullParser.END_TAG){
					if(eventName.equalsIgnoreCase(LAYER_TAG)){
						inLayerTag -= 1;
					}
				}
			}
			
			eventType = pullParser.next();
		}
		
		return layers;
	}

	public ArrayList<Tileset> parseGetCapabilitiesTileset(Server server, BufferedReader reader) throws JSONException, IOException{

		String finalJSON = "";

		try {
			// TAKE OVER READER FOR NOW
			reader = new BufferedReader(new FileReader("sdcard/Arbiter/jsonTest.json"));

			StringBuilder sb = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {
				sb.append(line);
				line = reader.readLine();
			}
			finalJSON = sb.toString();
		} catch(Exception e) {
			e.printStackTrace();
		}

		ArrayList<Tileset> tilesets = new ArrayList<Tileset>();

		// Tilesets from JSON file
		JSONObject jObj = new JSONObject(finalJSON.substring(finalJSON.indexOf("{"), finalJSON.lastIndexOf("}") + 1));
		JSONArray jArr = jObj.getJSONArray("tilesets");
		for (int i = 0; i < jArr.length(); ++i){
			JSONObject obj = jArr.getJSONObject(i);
			Tileset tileset = new Tileset(obj.getString("name"), obj.getLong("created_at"),
					obj.getString("created_by"), obj.getInt("filesize"),
					obj.getString("source"), obj.getString("bounds"),
					0, 0);
			// is_downloading(0 - false), downloadProgress(0%)

			tilesets.add(tileset);
		}

		// Test Tileset from new
		Tileset newTileset = new Tileset("Brand_New_Test", 10000050, "Sam", 6000.0, "Server_Name", "1000", 0, 0);
		tilesets.add(newTileset);

		// NOTICE: If the JSON has multiple of the same Tilesets (Same name/server), they will show up.

		return tilesets;
	}
}
