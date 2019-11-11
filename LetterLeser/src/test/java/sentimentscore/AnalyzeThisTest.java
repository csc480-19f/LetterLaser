package sentimentscore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.junit.Test;

import edu.oswego.model.SentimentScore;
import edu.oswego.sentiment.Cipherer;

public class AnalyzeThisTest {

	String user = "userTest@gmail.com";
	String[] emails = new String[100];

	@Test
	public void testEvaluateSentiment() {
		for (int q = 0; q < 100; q++) {
			emails[q] = "letters";
		}
		AnalyzeThisPublicTest analyze = new AnalyzeThisPublicTest(emails, user);
		assertEquals(analyze.getScores().length, emails.length);

		// This is the first string analyze checked.
		String emails0 = emails[0];
		String[] emails0Arr = { emails0 };
		// This is the score of the first string analyze checked.
		SentimentScore score0 = analyze.getScores()[0];
		// Analyze gets reassigned to check just a single string, emails0
		analyze = new AnalyzeThisPublicTest(emails0Arr, user);
		// emails0 should have the same score as the first score, score0.
		// This will show that the sentiment analyzer's evaluateSentiment method is
		// deterministic.
		assertEquals(analyze.evaluateSentiment(score0), analyze.evaluateSentiment(analyze.getScores()[0]));

		// This is a series of more trivial test.
		SentimentScore s1 = new SentimentScore(0, 0, 0, 0);
		SentimentScore s2 = new SentimentScore(0, 0, 0, 0);
		assertEquals(analyze.evaluateSentiment(s1), analyze.evaluateSentiment(s2));

		s1 = new SentimentScore(1, 1, 1, 1);
		s2 = new SentimentScore(1, 1, 1, 1);
		assertEquals(analyze.evaluateSentiment(s1), analyze.evaluateSentiment(s2));
	}

	@Test
	public void testProcess() {
		// Similarly to above, this proves that process is deterministic.
		for (int q = 0; q < 100; q++) {
			emails[q] = "facts yo";
		}
		AnalyzeThisPublicTest analyze = new AnalyzeThisPublicTest(emails, user);
		SentimentScore[] scores1 = analyze.getScores();
		SentimentScore[] scores2 = analyze.process(emails, user);
		for (int q = 0; q < scores1.length; q++) {
			assertTrue(Math.abs(scores1[q].getCompound() - scores2[q].getCompound()) < 0.000001);
			assertTrue(Math.abs(scores1[q].getPositive() - scores2[q].getPositive()) < 0.000001);
			assertTrue(Math.abs(scores1[q].getNegative() - scores2[q].getNegative()) < 0.000001);
			assertTrue(Math.abs(scores1[q].getNeutral() - scores2[q].getNeutral()) < 0.000001);
		}
	}

	@Test
	public void testFix() {
		// Passes
		// We make two strings; one fixed, and one pre-fix.
		String unfixed = "";
		String fixed = "";
		for (int q = 0; q < 256; q++) {
			unfixed = unfixed + (char) (q);
			// These are the characters that fix is supposed to filter out.
			if (!(q == 129 || q == 141 || q == 143 || q == 144 || q == 157 || q == 193 || q == 205 || q == 207
					|| q == 208 || q == 221)) {
				fixed = fixed + (char) (q);
			}
		}
		assertEquals(AnalyzeThisPublicTest.fix(unfixed), fixed);
	}

	@Test
	public void testEncipher() {
		// Passes
		String preCipher = "hello";
		String postCipher = "jmnoc";
		String result = AnalyzeThisPublicTest.encipher(new String[] { preCipher }, user);
		File testFile = new File(result);
		String line0 = "";
		try {
			line0 = new Scanner(testFile).nextLine();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		assertEquals(postCipher, line0);
	}
}

/**
 * This class holds methods that execute python code.
 */
class AnalyzeThisPublicTest {
	private final static String pathToSentiment = System.getProperty("user.dir") + File.separator + "src"
			+ File.separator + "test" + File.separator + "java" + File.separator + "sentiment" + File.separator;
	private final static File sentimentByEmail = new File(pathToSentiment + "SentimentByEmail.py");
	private SentimentScore[] scores;

	/**
	 * This class is used to evaluate sentiment of a list of strings.
	 *
	 * @param emails
	 *            The list of emails to be analyzed.
	 * @param userEmail
	 *            The user's email address; used to make filenames unique.
	 */
	public AnalyzeThisPublicTest(String[] emails, String userEmail) {
		scores = process(emails, userEmail);
	}

	/**
	 * This method will return the list of sentiment scores associated with the
	 * emails used to construct the class.
	 *
	 * @return
	 */
	public SentimentScore[] getScores() {
		return scores;
	}

	// private final static File output = new File(pathToSentiment+"output");

	/**
	 * This returns the results from the sentiment analysis of the list of emails.
	 *
	 * @param emails
	 *            The list of emails to be analyzed.
	 * @return An array of emails' corresponding sentiment score objects.
	 */
	public SentimentScore[] process(String[] emails, String userEmail) {
		SentimentScore[] scores = new SentimentScore[emails.length];

		// This section feeds the python code the enciphered .txt file and demands the
		// sentiment results.
		String filePath = AnalyzeThisPublicTest.encipher(emails, userEmail);

		try {
			Process p = Runtime.getRuntime().exec("python " + sentimentByEmail + " " + filePath + " " + userEmail);
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
	public static String encipher(String[] emails, String userEmail) {
		// This section creates the file and enciphers it.

		File pyIn = new File(pathToSentiment + "pythonInput" + userEmail + ".txt");

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
			e.printStackTrace();
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
	public static String fix(String s) {
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
		if (s.getCompound() > -0.05 && s.getCompound() < 5.0 / 12.0)
			return 1;
		if (s.getCompound() >= 5.0 / 12.0)
			return 2;
		return -1;
	}

}
