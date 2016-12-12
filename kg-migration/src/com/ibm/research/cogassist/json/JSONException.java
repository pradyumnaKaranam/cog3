package com.ibm.research.cogassist.json;

public class JSONException extends RuntimeException {
	private static final long serialVersionUID = 177377483637926898L;

    public JSONException(String message) {
        super(message);
    }

    public JSONException(Throwable t) {
        super(t);
    }

    public JSONException(String message, Throwable t) {
        super(message, t);
    }
    
}