/****************************************************************************
 *
 * Copyright (C) Agile Data, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Stephen Noel <noel@hub4edi.com>, December 2018
 *
 ****************************************************************************/
package com.mftlabs.sfgservices.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class IoUtil {
	
	public static void close(Closeable c) {
		try {
			c.close();
		} catch (Exception ignore) {
		}
	}
	public static void close(AutoCloseable c) {
		try {
			c.close();
		} catch (Exception ignore) {
		}
	}
	public static void close(HttpURLConnection c) {
		try {
			c.disconnect();
		} catch (Exception ignore) {
		}
	}
	public static void close(Connection c) {
		try {
			c.close();
		} catch (Exception ignore) {
		}
	}
	public static void close(PreparedStatement c) {
		try {
			c.close();
		} catch (Exception ignore) {
		}
	}
	public static void close(ResultSet c) {
		try {
			c.close();
		} catch (Exception ignore) {
		}
	}
	
	public static void copy(InputStream is, OutputStream os) throws IOException {
		try {
			int len = 0;
			byte[] buf = new byte[4096];
			while ((len=is.read(buf, 0, 4096)) != -1) {
				os.write(buf, 0, len);
			}
		} finally {
			close(is);
			close(os);
		}
		
	}
}
