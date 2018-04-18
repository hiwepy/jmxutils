package com.github.vindell.jmxutils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MonitorUtils {
	public final static long SECOND = 1000;
	public final static long MINUTE = 60 * SECOND;
	public final static long HOUR = 60 * MINUTE;
	public final static long DAY = 24 * HOUR;
	public final static String cr = System.getProperty("line.separator");
	public final static DateFormat timeDF = new SimpleDateFormat("HH:mm");
	private final static DateFormat timeWithSecondsDF = new SimpleDateFormat("HH:mm:ss");
	private final static DateFormat dateDF = new SimpleDateFormat("yyyy-MM-dd");
	private final static String decimalZero = new DecimalFormatSymbols().getDecimalSeparator() + "0";

	public static final String getHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (final UnknownHostException ex) {
			return "unknown";
		}
	}

	public static final String getHostAddress() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (final UnknownHostException ex) {
			return "unknown";
		}
	}

	public static String formatTime(long t) {
		String str;
		if (t < 1 * MINUTE) {
			String seconds = String.format("%.3f", t / (double) SECOND);
			str = getText("DurationSeconds", seconds);
		} else {
			long remaining = t;
			long days = remaining / DAY;
			remaining %= 1 * DAY;
			long hours = remaining / HOUR;
			remaining %= 1 * HOUR;
			long minutes = remaining / MINUTE;

			if (t >= 1 * DAY) {
				str = getText("DurationDaysHoursMinutes", days, hours, minutes);
			} else if (t >= 1 * HOUR) {
				str = getText("DurationHoursMinutes", hours, minutes);
			} else {
				str = getText("DurationMinutes", minutes);
			}
		}
		return str;
	}

	public static String formatNanoTime(long t) {
		long ms = t / 1000000;
		return formatTime(ms);
	}

	public static String formatClockTime(long time) {
		return timeDF.format(time);
	}

	public static String formatDate(long time) {
		return dateDF.format(time);
	}

	public static String formatDateTime(long time) {
		return dateDF.format(time) + " " + timeWithSecondsDF.format(time);
	}

	public static DateFormat getDateTimeFormat(String key) {
		String dtfStr = getText(key);
		int dateStyle = -1;
		int timeStyle = -1;

		if (dtfStr.startsWith("SHORT")) {
			dateStyle = DateFormat.SHORT;
		} else if (dtfStr.startsWith("MEDIUM")) {
			dateStyle = DateFormat.MEDIUM;
		} else if (dtfStr.startsWith("LONG")) {
			dateStyle = DateFormat.LONG;
		} else if (dtfStr.startsWith("FULL")) {
			dateStyle = DateFormat.FULL;
		}

		if (dtfStr.endsWith("SHORT")) {
			timeStyle = DateFormat.SHORT;
		} else if (dtfStr.endsWith("MEDIUM")) {
			timeStyle = DateFormat.MEDIUM;
		} else if (dtfStr.endsWith("LONG")) {
			timeStyle = DateFormat.LONG;
		} else if (dtfStr.endsWith("FULL")) {
			timeStyle = DateFormat.FULL;
		}

		if (dateStyle != -1 && timeStyle != -1) {
			return DateFormat.getDateTimeInstance(dateStyle, timeStyle);
		} else if (dtfStr.length() > 0) {
			return new SimpleDateFormat(dtfStr);
		} else {
			return DateFormat.getDateTimeInstance();
		}
	}

	public static double toExcelTime(long time) {
		// Excel is bug compatible with Lotus 1-2-3 and pretends
		// that 1900 was a leap year, so count from 1899-12-30.
		// Note that the month index is zero-based in Calendar.
		Calendar cal = new GregorianCalendar(1899, 11, 30);

		// Adjust for the fact that now may be DST but then wasn't
		Calendar tmpCal = new GregorianCalendar();
		tmpCal.setTimeInMillis(time);
		int dst = tmpCal.get(Calendar.DST_OFFSET);
		if (dst > 0) {
			cal.set(Calendar.DST_OFFSET, dst);
		}

		long millisSince1900 = time - cal.getTimeInMillis();
		double value = (double) millisSince1900 / (24 * 60 * 60 * 1000);

		return value;
	}

	public static String[] formatKByteStrings(long... bytes) {
		int n = bytes.length;
		for (int i = 0; i < n; i++) {
			if (bytes[i] > 0) {
				bytes[i] /= 1024;
			}
		}
		String[] strings = formatLongs(bytes);
		for (int i = 0; i < n; i++) {
			strings[i] = getText("kbytes", strings[i]);
		}
		return strings;
	}

	public static String formatKBytes(long bytes) {
		if (bytes == -1) {
			return getText("kbytes", "-1");
		}

		long kb = bytes / 1024;
		return getText("kbytes", justify(kb, 10));
	}

	public static String formatBytes(long v, boolean html) {
		return formatBytes(v, v, html);
	}

	public static String formatBytes(long v, long vMax) {
		return formatBytes(v, vMax, false);
	}

	public static String formatBytes(long v, long vMax, boolean html) {
		String s;

		int exp = (int) Math.log10((double) vMax);

		if (exp < 3) {
			s = getText("Size Bytes", v);
		} else if (exp < 6) {
			s = getText("Size Kb", trimDouble(v / Math.pow(10.0, 3)));
		} else if (exp < 9) {
			s = getText("Size Mb", trimDouble(v / Math.pow(10.0, 6)));
		} else {
			s = getText("Size Gb", trimDouble(v / Math.pow(10.0, 9)));
		}
		if (html) {
			s = s.replace(" ", "&nbsp;");
		}
		return s;
	}

	/*
	 * Return the input value rounded to one decimal place. If after rounding the
	 * string ends in the (locale-specific) decimal point followed by a zero then
	 * trim that off as well.
	 */
	private static String trimDouble(double d) {
		String s = String.format("%.1f", d);
		if (s.length() > 3 && s.endsWith(decimalZero)) {
			s = s.substring(0, s.length() - 2);
		}
		return s;
	}

	public static String formatLong(long value) {
		return String.format("%,d", value);
	}

	public static String[] formatLongs(long... longs) {
		int n = longs.length;
		int size = 0;
		String[] strings = new String[n];
		for (int i = 0; i < n; i++) {
			strings[i] = formatLong(longs[i]);
			size = Math.max(size, strings[i].length());
		}
		for (int i = 0; i < n; i++) {
			strings[i] = justify(strings[i], size);
		}
		return strings;
	}

	// A poor attempt at right-justifying for numerical data
	public static String justify(long value, int size) {
		return justify(formatLong(value), size);
	}

	public static String justify(String str, int size) {
		StringBuffer buf = new StringBuffer();
		int n = size - str.length();
		for (int i = 0; i < n; i++) {
			buf.append(" ");
		}
		buf.append(str);
		return buf.toString();
	}

	public static String getText(String key, Object... args) {
		String format = "";
		for (Object o : args) {
			format += o;
		}
		format += " " + key;
		return format;
	}

	public static String getThreadPriorityCN(int priority) {
		/*
		 * Map<Integer,String> pi = new HashMap<Integer,String>();
		 * pi.put(Thread.MAX_PRIORITY, "��"); pi.put(9, "���ڱ�׼"); pi.put(8, "���ڱ�׼");
		 * pi.put(7, "���ڱ�׼"); pi.put(6, "���ڱ�׼"); pi.put(Thread.NORM_PRIORITY,
		 * "��׼"); pi.put(4, "���ڱ�׼"); pi.put(3, "���ڱ�׼"); pi.put(2, "���ڱ�׼");
		 * pi.put(Thread.MIN_PRIORITY,"��");
		 */
		if (priority == Thread.MAX_PRIORITY) {
			return "��";
		} else if (priority == Thread.NORM_PRIORITY) {
			return "��׼";
		} else if (priority == Thread.MIN_PRIORITY) {
			return "��";
		} else if (priority > Thread.NORM_PRIORITY && priority < Thread.MAX_PRIORITY) {
			return "���ڱ�׼";
		} else if (priority > Thread.MIN_PRIORITY && priority < Thread.NORM_PRIORITY) {
			return "���ڱ�׼";
		} else if (priority > Thread.MAX_PRIORITY) {
			return "����";
		} else if (priority < Thread.MIN_PRIORITY) {
			return "����";
		} else {
			return "δ֪";
		}
	}
}
