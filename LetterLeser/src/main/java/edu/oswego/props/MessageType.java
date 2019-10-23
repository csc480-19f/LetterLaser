package edu.oswego.props;

import com.google.gson.JsonElement;

/**
 * Unified type for MessageType
 * 
 * @author Jimmy Nguyen
 * @since 10/23/2019
 *
 */

public enum MessageType {
	INITIALIZE, // Means they login, we pull, we send back user favs/folders
	CALCULATE, // Means they apply filters
	REFRESH; // Means they want to reintiailize. We do not need refresh though. Can use init.

	/**
	 * Checks the type of a message with a JSON message response
	 * 
	 * @param type
	 * @param jsonMessage
	 * @return if messages are the same
	 */
	public static boolean checkType(MessageType type, JsonElement jsonMessage) {
		if (jsonMessage.toString().toUpperCase().equals("REFRESH"))
			return true;

		return false;
	}

}
