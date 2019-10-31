package edu.oswego.props;

/**
 * Unified type for interval
 * 
 * @author Jimmy Nguyen
 * @since 10/23/2019
 *
 */

public enum Interval {

	WEEK, MONTH, YEAR;

	/**
	 * Parses a String interval into an Interval object
	 * 
	 * @param interval
	 * @return Interval object
	 */
	public static Interval parse(String interval) {//KEEP EVERYTHING LOWERCASE
		if (interval.equals("week"))
			return WEEK;
		else if (interval.equals("month"))
			return MONTH;
		else if (interval.equals("year"))
			return YEAR;

		return null;
	}

}
