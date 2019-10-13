package edu.oswego.props;

import com.google.gson.JsonElement;

public enum MessageType {
	INITIALIZE, // Means they login, we pull, we send back user favs/folders
	CALCULATE, // Means they apply filters
	REFRESH; // Means they want to reintiailize. We do not need refresh though. Can use init.
	
//	public static MessageType parse(String jsonMessage) {
//		if (jsonMessage.equals("REFRESH"))
//			return REFRESH;
//		
//		return null;
//	}
	
	public static boolean checkType(MessageType type, JsonElement jsonMessage) {
		if (jsonMessage.toString().toUpperCase().equals("REFRESH"))
			return true;
		
		return false;
	}

}
