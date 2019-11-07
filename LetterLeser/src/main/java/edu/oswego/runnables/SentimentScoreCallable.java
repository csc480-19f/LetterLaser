package edu.oswego.runnables;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.oswego.model.Email;
import edu.oswego.sentiment.AnalyzeThis;

import java.util.List;
import java.util.concurrent.Callable;

public class SentimentScoreCallable implements Callable {
	private List<Email> emails;

	public SentimentScoreCallable(List<Email> emails) {
		this.emails = emails;
	}

	/**
	 * This method determines the percentage of positive emails, disregarding that
	 * neutral scores exist and are meaningful.
	 */
	@Override
	public Object call() throws Exception {
		int positive = 0;
		for (Email e : emails) {
			int score = AnalyzeThis.evaluateSentiment(e.getSentimentScore());
			if (score == 2) {
				positive += 1;
			}
		}

		int num = positive / emails.size() * 100;
		return num;
	}
}
