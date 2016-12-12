package com.ibm.research.cogassist.json;

import java.lang.reflect.Array;
import java.util.Collection;

import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

public class JSONArray {
	
	protected final org.json.simple.JSONArray a;

    public JSONArray() {
    	this.a = new org.json.simple.JSONArray();
    }

    protected JSONArray(org.json.simple.JSONArray a) {
    	this.a = a;
    }
    
    public JSONArray(String encodedJsonString) throws JSONException {
    	try {
    		this.a = (org.json.simple.JSONArray) JSONValue.parseWithException(encodedJsonString);
    	} catch (ParseException e) {
    		throw new JSONException("Input string is not well-formed JSON.", e);
    	} catch (ClassCastException e) {
    		throw new JSONException("Input string does not represent a JSON Array.", e);
    	}
    }

	public JSONArray(Collection<?> collection) {
    	this();
    	this.addAll(collection);
    }

    public JSONArray(Object array) throws JSONException {
        this();
        if (array.getClass().isArray()) {
            int length = Array.getLength(array);
            for (int i = 0; i < length; i += 1) {
                this.add(Array.get(array, i));
            }
        } else {
            throw new JSONException("JSONArray initial value should be a string or collection or array.");
        }
    }

    
    public Object get(int index) {
   		Object r = a.get(index);
   		if (r instanceof org.json.simple.JSONObject) {
   			return this.getJSONObject(index);
   		} else if (r instanceof org.json.simple.JSONArray) {
   			return this.getJSONArray(index);
   		} else {
   			return r;
   		}
    }

    public Boolean getBoolean(int index) throws JSONException {
    	try {
    		return (Boolean) a.get(index);
    	} catch (ClassCastException e) {
    		throw new JSONException("Object at index '" + index + "' is not a boolean.", e);
    	}
    }


    public Integer getInt(int index) throws JSONException {
    	try {
    		Number val = (Number) a.get(index);
    		return (val == null) ? null : val.intValue();
    	} catch (ClassCastException e) {
    		throw new JSONException("Object at index '" + index + "' is not an integer.", e);
    	}
    }


    public Long getLong(int index) throws JSONException {
    	try {
    		Number val = (Number) a.get(index);
    		return (val == null) ? null : val.longValue();
    	} catch (ClassCastException e) {
    		throw new JSONException("Object at index '" + index + "' is not a long.", e);
    	}
    }


    public Double getDouble(int index) throws JSONException {
    	try {
    		Number val = (Number) a.get(index);
    		return (val == null) ? null : val.doubleValue();
    	} catch (ClassCastException e) {
    		throw new JSONException("Object at index '" + index + "' is not a double.", e);
    	}
    }

    
    public String getString(int index) throws JSONException {
    	try {
    		return (String) a.get(index);
    	} catch (ClassCastException e) {
    		throw new JSONException("Object at index '" + index + "' is not a string.", e);
    	}
    }

    

    public JSONObject getJSONObject(int index) throws JSONException {
    	try {
    		JSONObject o = (JSONObject) this.a.get(index);
    		return  o;
    	} catch (ClassCastException e) {
    		throw new JSONException("Object at index '" + index + "' is not an object.", e);
    	}
    }

    
    public JSONArray getJSONArray(int index) throws JSONException {
    	try {
    		JSONArray a = (JSONArray) this.a.get(index);
    		return  a;
    	} catch (ClassCastException e) {
    		throw new JSONException("Object at index '" + index + "' is not an array.", e);
    	}
    }


    
    public int length() {
        return a.size();
    }
    
    @SuppressWarnings("unchecked")
	public JSONArray add(Object value) {
    	this.a.add(value);
    	return this;
    }

    @SuppressWarnings("unchecked")
	public JSONArray add(int index, Object value) {
    	this.a.add(index, value);
    	return this;
    }
    
    @SuppressWarnings("unchecked")
	public JSONArray addAll(Collection<?> collection) {
    	this.a.addAll(collection);
    	return this;
    }
    
    @SuppressWarnings("unchecked")
	public JSONArray addAll(JSONArray array) {
    	for (int i=0; i< array.length(); i++)
    		this.a.add(array.getJSONObject(i));
    	return this;
    }

    @SuppressWarnings("unchecked")
	public JSONArray addAll(int index, Collection<?> collection) {
    	this.a.addAll(index, collection);
    	return this;
    }
    
    @SuppressWarnings("unchecked")
	public JSONArray addAll(int index, JSONArray array) {
    	int ind = array.length();
    	if (index < ind)
    		ind = index;
    	for (int i=0; i< ind; i++)
    		this.a.add(array.getJSONObject(i));
    	return this;
    }
    
    public String toString() {
    	return this.a.toJSONString();
    }

}