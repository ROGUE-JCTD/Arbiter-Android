package com.lmn.Arbiter_Android.BaseClasses;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ColorMap {

	public static final Map<String, String> COLOR_MAP;
    static {
        Map<String, String> aMap = new HashMap<String,String>();
        aMap.put("teal","#008080");
		aMap.put("maroon","#800000");
		aMap.put("green","#008000");
		aMap.put("purple","#800080");
		aMap.put("fuchsia","#FF00FF");
		aMap.put("lime","#00FF00");
		aMap.put("red","#FF0000");
		aMap.put("black","#000000");
		aMap.put("navy","#000080");
		aMap.put("aqua","#00FFFF");
		aMap.put("grey","#808080");
		aMap.put("olive","#808000");
		aMap.put("yellow","#FFFF00");
		aMap.put("silver","#C0C0C0");
		aMap.put("white","#FFFFFF");
		COLOR_MAP = Collections.unmodifiableMap(aMap);
    }
}
