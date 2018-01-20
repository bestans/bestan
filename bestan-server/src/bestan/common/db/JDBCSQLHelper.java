package bestan.common.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;

import bestan.common.config.ConfigurationParams;
import bestan.common.config.Persistence;
import bestan.common.log.LogManager;
import bestan.common.util.Global;

/**
 * 数据库升级功能
 */
public class JDBCSQLHelper {

	/** the logger instance. */
	private static final bestan.common.log.Logger logger = LogManager.getLogger(JDBCSQLHelper.class);
	
	/** excute db connection handler */
	private final DBTransaction transaction;
	
	/** excute db command or file */
	private String command;

	/**
	 * creates a new JDBCSQLHelper
	 *
	 * @param transaction DBTransaction
	 */
	public JDBCSQLHelper(DBTransaction transaction) {
		this.transaction = transaction;
	}

	/**
	 * This method runs a SQL file using the given transaction. You are
	 * responsible of beginning the transaction and commiting the changes or
	 * rollback on error.
	 *
	 * @param file
	 *            The file name that contains the SQL commands.
	 * @return true if the whole file was executed or false in any other error.
	 */
	public boolean runDBScriptFile(final String file) {
		boolean ret = true;
		BufferedReader in = null;

		try {
			ConfigurationParams param = new ConfigurationParams(false, Global.CONFIG_PATH, file);
			InputStream is = Persistence.get().getInputStream(
					param.isRelativeToHome(), param.getBasedir(), param.getConfigurationFile());
			in = new BufferedReader(new InputStreamReader(is, "UTF-8"));

			StringBuffer sb = new StringBuffer();

			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
				if (line.indexOf(';') != -1) {
					command = sb.toString().trim();
					transaction.execute(command);
					sb = new StringBuffer();
				}
			}

			return ret;
		} catch (SQLException e) {
			logger.error("error running SQL Script (file: " + file + "): " + command, e);
			return false;
		} catch (IOException e) {
			logger.error("error reading SQL Script (file: " + file + "): " + command, e);
			return false;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}
