/***************************************************************************
 *                   (C) Copyright 2010-2010 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package bestan.common.util;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;

import bestan.log.GLog;

/**
 * validates parameters with respect to their type
 *
 * @author hendrik
 */
class Validator {
	private static Logger logger = GLog.log;

	public void validateVeryLongString(String value) {
		// okay
	}

	public void validate65536LongString(String value) {
		try {
			if (value.getBytes("UTF-8").length >= 65535) {
				throw new IllegalArgumentException("LongString too long");
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void validate255LongString(String value) {
		try {
			if (value.getBytes("UTF-8").length >= 255) {
				throw new IllegalArgumentException("String too long");
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void validateFloat(String value) {
		Float.parseFloat(value);
	}

	public void validateInteger(String value) {
		Integer.parseInt(value);
	}

	public void validateShort(String value) {
		Short.parseShort(value);
	}

	public void validateByte(String value) {
		Byte.parseByte(value);
	}

	public void validateMap(String value) {
		throw new IllegalArgumentException("Trying to set a value into a map type argument");
	}

}
