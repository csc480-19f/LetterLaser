package edu.oswego.Runnables;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.oswego.model.Email;

import javax.mail.Folder;
import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author PhoenixBoisnier
 * This runnable calculates the folder populations. It assumes that the list of emails provided all exist
 * within a folder within a folder, and assumes that all the emails in the relevant folders is contained
 * within the list given.
 */
public class FolderCallable implements Callable {
	private List<Email> emails;


	public FolderCallable(List<Email> emails) {
		this.emails = emails;
	}

	/**
	 * This method creates a folderMapper class, which calculates the data for us in the constructor.
	 */
	@Override
	public Object call() throws Exception {
		FolderMapper fm = new FolderMapper(emails);
		HashMap<String, Integer> counts = fm.outerCount;
		HashMap<String, String> relationships = fm.folderToParent;
		ArrayList<String> keys = fm.keyRing;

		JsonObject finalRet = new JsonObject();
		JsonArray emailsByFolder = new JsonArray();
		for(String s : keys){
			JsonObject folderObj = new JsonObject();
			JsonArray folderName = new JsonArray();
			folderName.add(s);
			folderObj.add("FolderName", folderName);
			JsonArray folderParent = new JsonArray();
			folderParent.add(relationships.get(s));
			folderObj.add("FolderParent", folderParent);
			JsonArray contributions = new JsonArray();
			contributions.add(counts.get(s));
			folderObj.add("Contributions", contributions);
			emailsByFolder.add(folderObj);
		}
		finalRet.add("EmailsByFolder", emailsByFolder);

		return finalRet;
	}

}

/**
 * This is the folderMapper class. It happened to be the way that made the most sense to myself to organize the data.
 * This class has protected results as: outerCount; mapping the relevant folder names to their email counts,
 * folderToParent; mapping the relevant folders to their parent folder, and keyRing; the complete list of keys used for
 * outerCount and folderToParent.
 */
class FolderMapper{
	HashMap<String, Integer> outerCount = new HashMap<>();
	ArrayList<String> keyRing = new ArrayList<>();
	HashMap<String, String> folderToParent = new HashMap<>();

	//The constructor also does the calculations.
	FolderMapper(List<Email> emails){
		//For each email in emails,
		for(Email e : emails){
			//Make a parent variable.
			Folder parent = null;
			try {
				//The parent exists.
				parent = e.getFolder().getFolder().getParent();
			} catch (MessagingException ex) {
				//Or the parent does not exist.
				//This would disqualify an email based on the class assumptions.
				System.out.println("Failed to locate parents.");
				ex.printStackTrace();
			}
			//Since the parent exists,
			if(parent!=null){
				//We create a grandparent variable.
				Folder grandParent = null;
				//And do the same as above.
				try {
					grandParent = parent.getParent();
				} catch (MessagingException ex) {
					//Like above, an email that does not gave a grandparent does not meet our assumptions.
					System.out.println("Failed to locate grandparents.");
					ex.printStackTrace();
				}
				//Since the email matches our assumptions,
				if(grandParent!=null){
					//If the folder that our email is in has already been counted,
					if(outerCount.containsKey(e.getFolder().getFolder().getName())){
						//Then we increment the value found.
						outerCount.put(e.getFolder().getFolder().getName(),
								outerCount.get(e.getFolder().getFolder().getName())+1);
					}
					//Otherwise, we make a new entry in our map.
					else{
						outerCount.put(e.getFolder().getFolder().getName(), 1);
						//And since we only care about emails that get this far, we only store the folder info now.
						keyRing.add(e.getFolder().getFolder().getName());
					}
					//In either case, since the parent exists, we'll add it to the relationship map.
					folderToParent.put(e.getFolder().getFolder().getName(), parent.getName());
				}
			}
		}
	}
}
