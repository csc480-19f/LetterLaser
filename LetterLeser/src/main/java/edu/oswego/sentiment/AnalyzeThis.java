/**
 * @author Phoenix Boisnier
 */
package edu.oswego.sentiment;

import java.io.*;
import java.util.Scanner;

/**
 * This class holds methods that execute python code.
 */
public class AnalyzeThis {
	private static final String pathToSentimentOnPi = "~/javaServer/apache-tomcat-9.0.26/webapps/LetterLeser/WEB-INF/classes/edu/oswego/sentiment/";
	// if we run into issues with other dir then we use the hard coded one above
	private final static String pathToSentiment = System.getProperty("user.dir") + File.separator + "LetterLeser"
			+ File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + "edu"
			+ File.separator + "oswego" + File.separator + "sentiment" + File.separator;
	private final static File sentimentByEmail = new File(pathToSentiment + "SentimentByEmail.py");
	private final static File output = new File(pathToSentiment + "output");

	// private final static File output = new File(pathToSentiment+"output");

	/**
	 * This returns the results from the sentiment analysis of the list of emails.
	 * 
	 * @param emails
	 *            The list of emails to be analyzed.
	 * @return An array of emails' corresponding sentiment score objects.
	 */
	public static SentimentScore[] process(String[] emails) {
		SentimentScore[] scores = new SentimentScore[emails.length];

		// This section feeds the python code the enciphered .txt file and demands the
		// sentiment results.
		String filePath = AnalyzeThis.encipher(emails);

		try {
			Process p = Runtime.getRuntime().exec("python " + sentimentByEmail.getPath() + " " + filePath);
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()), 8);
			String vals = in.readLine();
			File input = new File(vals);
			Scanner scone = new Scanner(input);
			for (int q = 0; q < emails.length; q++) {
				String line = scone.nextLine();
				Scanner scune = new Scanner(line);
				SentimentScore s = new SentimentScore(Double.parseDouble(scune.next()),
						Double.parseDouble(scune.next()), Double.parseDouble(scune.next()),
						Double.parseDouble(scune.next()));
				scores[q] = s;
			}
			p.destroy();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Oh no.");
		}
		// Get your fresh, hot results here.
		return scores;

	}

	/**
	 * This enciphers and writes the input string array to a text file.
	 * 
	 * @param emails
	 *            The strings to be enciphered.
	 * @return The path name of the file.
	 */
	private static String encipher(String[] emails) {
		// This section creates the file and enciphers it.

		File pyIn = new File(pathToSentiment + "pythonInput.txt");
		try {
			pyIn.createNewFile();
			FileWriter write = new FileWriter(pyIn);
			for (String email : emails) {
				// What we really do first is fix the string.
				String fixedEmail = fix(email);
				// First we apply a caesar shift of 10 to our email.
				String[] param1 = { fixedEmail, "10" };
				String ciph1 = Cipherer.encipher(0, param1);
				// Then we apply a vignere cipher with keyword systemic to the caesar shifted
				// text.
				String[] param2 = { ciph1, "systemic" };
				String ciph2 = Cipherer.encipher(2, param2);
				write.write(ciph2 + "\n");
				write.flush();
			}
			write.close();
		} catch (IOException e) {
			System.out.println("Oh no!");
		}

		return pyIn.getPath();
	}

	/**
	 * This method restricts strings to char values 0 to 255.
	 * 
	 * @param s
	 *            The string that needs to be fixed.
	 * @return The fixed string.
	 */
	private static String fix(String s) {
		String retVal = "";
		char[] letters = s.toCharArray();
		for (int i = 0; i < letters.length; i++) {
			if (letters[i] <= 255) {
				if (!(i == 129 || i == 141 || i == 143 || i == 144 || i == 157 || i == 193 || i == 205 || i == 207
						|| i == 208 || i == 221)) {
					retVal += letters[i];
				}
			}
		}
		return retVal;
	}

	/**
	 * This method evaluates the sentiment score into three categories based on it's
	 * compound score. The ranges for what is considered positive, negative, and
	 * neutral is based on evaluation testing.
	 * 
	 * @param s
	 *            The sentiment score to be evaluated.
	 * @return 0, 1, or 2 for either negative, neutral, or positive, respectively.
	 */
	public static int evaluateSentiment(SentimentScore s) {
		if (s.getCompound() <= -0.05)
			return 0;
		if (s.getCompound() > -0.05)
			return 1;
		if (s.getCompound() >= 5.0 / 12.0)
			return 2;
		return -1;
	}

}
