/*
 * This file is part of Wireless Crafting Terminal. Copyright (c) 2017, p455w0rd
 * (aka TheRealp455w0rd), All rights reserved unless otherwise stated.
 *
 * Wireless Crafting Terminal is free software: you can redistribute it and/or
 * modify it under the terms of the MIT License.
 *
 * Wireless Crafting Terminal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the MIT License for
 * more details.
 *
 * You should have received a copy of the MIT License along with Wireless
 * Crafting Terminal. If not, see <https://opensource.org/licenses/MIT>.
 */
package p455w0rd.wct.init;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author p455w0rd
 *
 */
public class ModLogger {

	private static Logger LOGGER = LogManager.getLogger(ModGlobals.NAME);
	public static String LOG_PREFIX = "==========[Start " + ModGlobals.NAME + " %s]========";
	public static String LOG_SUFFIX = "==========[End " + ModGlobals.NAME + " %s]==========";

	public static void warn(String msg) {
		LOGGER.warn(msg);
	}

	public static void error(String msg) {
		LOGGER.error(msg);
	}

	public static void infoBegin(String headerInfo) {
		String header = String.format(LOG_PREFIX, headerInfo);
		LOGGER.info(header);
	}

	public static void infoBegin(String headerInfo, String msg) {
		String header = String.format(LOG_PREFIX, headerInfo);
		LOGGER.info(header);
		LOGGER.info(msg);
	}

	public static void infoEnd(String footerInfo) {
		String footer = String.format(LOG_SUFFIX, footerInfo);
		LOGGER.info(footer);
	}

	public static void info(String msg) {
		LOGGER.info(msg);
	}

	public static void debug(String msg) {
		LOGGER.debug(msg);
	}

}