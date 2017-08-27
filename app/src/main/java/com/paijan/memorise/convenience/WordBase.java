package com.paijan.memorise.convenience;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public abstract class WordBase implements Serializable {
	public abstract void fromJson(JSONObject json) throws JSONException;
	public abstract JSONObject toJson() throws JSONException;
}
