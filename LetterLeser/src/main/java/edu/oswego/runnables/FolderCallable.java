package edu.oswego.runnables;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.oswego.model.Email;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author PhoenixBoisnier / MikeDoran
 * 			This runnable calculates the folder populations. It
 *         assumes that all the emails in the relevant
 *         folders is contained within the list given.
 */
public class FolderCallable implements Callable {
	private List<Email> emails;

	public FolderCallable(List<Email> emails) {
		this.emails = emails;
	}

	/**
	 * 	Constructs and returns a JSON object containing a list of all folders, their parents,
	 * 	and their content's contribution by number to the total number of given emails.
	 */
	@Override
	public Object call() {
		FolderMapper fm = new FolderMapper(emails);

		JsonObject emailsByFolder = new JsonObject();
		JsonArray folderObjs = new JsonArray();
		for (String folder : fm.allFolders()) {
			JsonObject folderObj = new JsonObject();
			JsonObject innerData = new JsonObject();

			String folderParent = (fm.hasParent(folder))? fm.getParent(folder) : "0";

			innerData.addProperty("domainname", folder);
			innerData.addProperty("domainparent", folderParent);
			innerData.addProperty("contribution", fm.getContribution(folder));
			folderObj.add("domainobj", innerData);
			folderObjs.add(folderObj);
		}

		emailsByFolder.add("emailbyfolder", folderObjs);
		return folderObjs;
	}


	/**
	 * FolderMapper: a book keeping class that keeps track of folder parents and
	 * contributions.
	 */
	private class FolderMapper {

		/**
		 *	The constructor constructs a mapping of all folders to their parents,
		 *	as well as a mapping of all folders to their contribution by number
		 */
		private HashMap<String,String> parents = new HashMap<>();
		private HashMap<String, Integer> contribution = new HashMap<>();
		public FolderMapper(List<Email> emails){
			for (Email e : emails) {
				String[] folders = e.getFolder().split("/");
				for (int i = folders.length - 1; i >= 0; i--) {
					parents.put(folders[i],((i-1) < 0)? "0" : folders[i-1]);
					incrementKey(contribution, folders[i]);
				}
			}
		}

		/**
		 *	getParent: Gets The parent of a given folder (By name in String)
		 */
		public String getParent(String child){ return parents.get(child); }

		/**
		 *	getContribution: Gets The count of a given folder (By name in String)
		 */
		public int getContribution(String folder){ return contribution.get(folder); }

		/**
		 *	allFolders: Gets a set of all folders (By name in String)
		 */
		public Set<String> allFolders(){ return contribution.keySet(); }

		/**
		 *	hasParent: Returns true if a given folder (By name in String) has a parent
		 */
		public boolean hasParent(String folder){ return parents.containsKey(folder); }

		/**
		 *	incrementKey: Increments the value at a given key in a given HashMap,
		 *	creating a new entry if one does not yet exist (Just to keep the
		 *	constructor less cluttered)
		 */
		private HashMap<String, Integer> incrementKey(HashMap<String,Integer> map, String key) {
			if(map.containsKey(key)){
				int x = map.get(key);
				map.put(key, ++x);
			}else{
				map.put(key, 1);
			}
			return map;
		}
	}
}