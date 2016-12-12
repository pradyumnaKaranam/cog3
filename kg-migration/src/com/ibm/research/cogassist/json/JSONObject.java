package com.ibm.research.cogassist.json;

import java.util.Iterator;

import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

public class JSONObject implements Iterable<String> {
	
	protected final org.json.simple.JSONObject o;

    public JSONObject() {
    	this.o = new org.json.simple.JSONObject();
    }
    
    protected JSONObject(org.json.simple.JSONObject o) {
    	this.o = o;
    }

    public JSONObject(String encodedJsonString) throws JSONException {
    	try {
    		this.o = (org.json.simple.JSONObject) JSONValue.parseWithException(encodedJsonString);
    	} catch (ParseException e) {
    		throw new JSONException("Input string is not well-formed JSON.", e);
    	} catch (ClassCastException e) {
    		throw new JSONException("Input string does not represent a JSON Object.", e);
    	}
    }

    public Object get(String key) {
   		Object r = o.get(key);
   		if (r instanceof org.json.simple.JSONObject) {
   			return this.getJSONObject(key);
   		} else if (r instanceof org.json.simple.JSONArray) {
   			return this.getJSONArray(key);
   		} else {
   			return r;
   		}
    }

    public Boolean getBoolean(String key) throws JSONException {
    	try {
    		return (Boolean) o.get(key);
    	} catch (ClassCastException e) {
    		throw new JSONException("Object at key '" + key + "' is not a boolean.", e);
    	}
    }


    public Integer getInt(String key) throws JSONException {
    	try {
    		Number val = (Number) o.get(key);
    		return (val == null) ? null : val.intValue();
    	} catch (ClassCastException e) {
    		throw new JSONException("Object at key '" + key + "' is not an integer.", e);
    	}
    }


    public Long getLong(String key) throws JSONException {
    	try {
    		Number val = (Number) o.get(key);
    		return (val == null) ? null : val.longValue();
    	} catch (ClassCastException e) {
    		throw new JSONException("Object at key '" + key + "' is not a long.", e);
    	}
    }


    public Double getDouble(String key) throws JSONException {
    	try {
    		Number val = (Number) o.get(key);
    		return (val == null) ? null : val.doubleValue();
    	} catch (ClassCastException e) {
    		throw new JSONException("Object at key '" + key + "' is not a double.", e);
    	}
    }

    
    public String getString(String key) throws JSONException {
    	try {
    		return (String) o.get(key);
    	} catch (ClassCastException e) {
    		throw new JSONException("Object at key '" + key + "' is not a string.", e);
    	}
    }

    

    public JSONObject getJSONObject(String key) throws JSONException {
    	try {
    		JSONObject o = (JSONObject) this.o.get(key);
    		return o;
    	} catch (ClassCastException e) {
    		throw new JSONException("Object at key '" + key + "' is not an object.", e);
    	}
    }

    
    public JSONArray getJSONArray(String key) throws JSONException {
    	try {
    		JSONArray a = (JSONArray) this.o.get(key);
    		return a;
    	} catch (ClassCastException e) {
    		throw new JSONException("Object at key '" + key + "' is not an array.", e);
    	}
    }

    
    public boolean has(String key) {
        return o.containsKey(o);
    }

    @SuppressWarnings("unchecked")
	public Iterator<String> iterator() {
    	return o.keySet().iterator();
    }

    public Iterator<?> keys() {
    	return o.keySet().iterator();
    }
    
    public int length() {
        return o.size();
    }


    @SuppressWarnings("unchecked")
	public JSONObject put(String key, Object value) throws JSONException {
    	o.put(key, value);
    	return this;
    }
    
    public String toString() {
    	return o.toJSONString();
    }

	@SuppressWarnings("unchecked")
	public JSONObject putAll(JSONObject obj) {
		for (String k : obj)
			o.put(k, obj.get(k));
		return this;
	}
    
}
