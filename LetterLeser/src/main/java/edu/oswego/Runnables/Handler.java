package edu.oswego.Runnables;


import com.google.gson.JsonObject;
import edu.oswego.database.Database;
import edu.oswego.model.Email;
import edu.oswego.model.UserFavourites;

import java.util.List;


public class Handler implements Runnable {
	private Database database;
	private JsonObject jsonObject;
	private UserFavourites userFavourites;

	public Handler(Database database, JsonObject jsonObject){
		this.database = database;
		this.jsonObject = jsonObject;
	}
	public Handler(Database database, UserFavourites userFavourites){
		this.database = database;
		this.userFavourites = userFavourites;
	}


	@Override
	public void run() {

		if(jsonObject!=null){

		}else if(userFavourites!=null){
			List<Email> emails = database.getEmailByFilter();
		}else{

		}
	}

	private void performCalculations(List<Email> emailList){

	}



}
