package net.p455w0rd.wirelesscraftingterminal.common.utils;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WCTLog {
	public static final Logger log = LogManager.getLogger("Wireless Crafting Terminal");

	/**
	 * Displays the section header.
	 */
	public static long beginSection(final String section) {
		log.info(String.format("-=-=-=-=-=-=-=[START] %s=-=-=-=-=-=-=-", section));
		return System.currentTimeMillis();
	}

	/**
	 * Logs a debug statement.
	 * 
	 * @param format
	 * @param data
	 */
	public static void debug(final String format, final Object... data) {
		log.debug(String.format(format, data));
	}

	/**
	 * Displays the section footer.
	 */
	public static void endSection(final String section, final long sectionStartTime) {
		log.info(String.format("-=-=-=-=-=-=-=[FINISH] %s in %dms=-=-=-=-=-=-=-", section, (System.currentTimeMillis() - sectionStartTime)));
	}

	/**
	 * Logs basic info.
	 * 
	 * @param format
	 * @param data
	 */
	public static void info(final String format, final Object... data) {
		log.info(String.format(format, data));
	}

	/**
	 * Logs an error.
	 * 
	 * @param format
	 * @param data
	 */
	public static void severe(final String format, final Object... data) {
		log.error(String.format(format, data));
	}

	/**
	 * Logs a warning.
	 * 
	 * @param format
	 * @param data
	 */
	public static void warning(final String format, final Object... data) {
		log.warn(String.format(format, data));
	}
	
	public static void integration( @Nonnull final Throwable exception )
	{
		log.debug( exception );
	}
}
