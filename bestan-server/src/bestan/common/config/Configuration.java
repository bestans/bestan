package bestan.common.config;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.slf4j.Logger;

import bestan.common.util.Global;
import bestan.log.GLog;

/** This class is a basic configuration file manager */
public class Configuration {

	/** the logger instance. */
	private static final Logger logger = GLog.log;

	/*TODO get rid of this static configuration parameters by removing the static setConfigurationFile()
	 * functions and the 'configuration' singleton variable */
//	private static ConfigurationParams staticParams = new ConfigurationParams();
//	private static Configuration configuration = null;

	private final ConfigurationParams params;
	private final Properties properties;

	/**
	 * This method defines the default configuration file for all the instances
	 * of Configuration
	 *
	 * @param conf
	 *            the location of the file
	 */
	public void setConfigurationFile(String conf) {
		params.setRelativeToHome(false);
		params.setBasedir("");
		params.setConfigurationFile(conf);
	}

	/**
	 * This method defines the default configuration file for all the instances
	 * of Configuration
	 *
	 * @param relativeToHome
	 *            should this file be placed below the users home directory?
	 * @param basedir
	 *            directory prefix which is ignore in webstart environment
	 * @param conf
	 *            the location of the file
	 */
	public void setConfigurationFile(boolean relativeToHome, String basedir, String conf) {
		params.setRelativeToHome(relativeToHome);
		params.setBasedir(basedir);
		params.setConfigurationFile(conf);
	}

	/**
	 * Should the configuration be read from and write to a file?
	 *
	 * @param persistence
	 *            true to use files, false otherwise
	 */
	public void setConfigurationPersitance(boolean persistence) {
		params.setPersistence(persistence);
	}

	/**
	 * Returns the name of the configuration file
	 *
	 * @return the name of the configuration file
	 */
	public String getConfigurationFile() {
		return params.getConfigurationFile();
	}

	/**
	 * Create a new COnfiguration instance using filename etc from the
	 * given params parameter.
	 *
	 * @param params
	 * @throws IOException
	 */
	public Configuration(ConfigurationParams params) throws IOException {
		this.params = params;

		BufferedReader in = null;
		try {
			properties = new Properties();

			if (params.isPersistence()) {
				InputStream is = Persistence.get().getInputStream(
						params.isRelativeToHome(), params.getBasedir(), params.getConfigurationFile());
				in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				properties.load(in);
				is.close();
			}
		} catch (FileNotFoundException e) {
			logger.warn("Configuration file not found: " + params.getConfigurationFile(), e);
			throw e;
		} catch (IOException e) {
			logger.warn("Error loading Configuration file", e);
			throw e;
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

	/**
	 * This method returns an instance of Configuration
	 *
	 * @return a shared instance of Configuration
	 * @throws IOException
	 */
//	public static Configuration getConfiguration() throws IOException {
//		if (configuration == null) {
//			configuration = new Configuration(staticParams);
//		}
//		return configuration;
//	}

	/**
	 * This method returns a String with the value of the property.
	 *
	 * @param property
	 *            the property we want the value
	 * @return a string containing the value of the property
	 */
	public String getString(String property) {
		if (!has(property)) {
			throw new java.lang.IllegalArgumentException(params.getConfigurationFile() + " does not has the property '" + property + "'");
		}
		String value = properties.getProperty(property);
		if (value != null) {
			value = value.trim();
		}
		return value;
	}
	
	/**
	 * This method returns an integer with the value of the property.
	 *
	 * @param property the property we want the value
	 * @return an integer of the property
	 */
	public int getInt(String property) {
		if (!has(property)) {
			throw new java.lang.IllegalArgumentException(params.getConfigurationFile() + " does not has the property\"" + property + "\"");
		}
		int res = Global.INVALID_VALUE;
		String value = properties.getProperty(property);
		if (value != null) {
			try {
				res = Integer.parseInt(value.trim());
			} catch (NumberFormatException e) {
				logger.error("Configuration parameter " + property + " is \"" + value + "\" but an int was expected.");
			}
		}
		return res;
	}
	
	/**
	 * This method returns an integer with the value of the property.
	 *
	 * @param property the property we want the value
	 * @return an Long of the property
	 */
	public long getLong(String property) {
		if (!has(property)) {
			throw new java.lang.IllegalArgumentException(params.getConfigurationFile() + " does not has the property\"" + property + "\"");
		}
		long res = Global.INVALID_VALUE;
		String value = properties.getProperty(property);
		if (value != null) {
			try {
				res = Long.parseLong(value.trim());
			} catch (NumberFormatException e) {
				logger.error("Configuration parameter " + property + " is \"" + value + "\" but an long was expected.");
			}
		}
		return res;
	}
	
	public float getFloat(String property){
		if (!has(property)) {
			throw new java.lang.IllegalArgumentException(params.getConfigurationFile() + " does not has the property\"" + property + "\"");
		}
		float res = 0.0f;
		String value = properties.getProperty(property);
		if (value != null) {
			try {
				res = Float.parseFloat(value.trim());
			} catch (NumberFormatException e) {
				logger.error("Configuration parameter " + property + " is \"" + value + "\" but an int was expected.");
			}
		}
		return res;
	}
	
	/**
	 * This method returns true if the property exists.
	 *
	 * @param property
	 * @return true if the property exists
	 */
	public boolean has(String property) {
		return properties.containsKey(property);
	}

	/**
	 * This method sets a property with the value we pass as parameter
	 *
	 * @param property
	 *            the property we want to set the value
	 * @param value
	 *            the value to set
	 */
	public void set(String property, String value) {
		try {
			properties.put(property, value);

			if (params.isPersistence()) {
				OutputStream os = Persistence.get().getOutputStream(
						params.isRelativeToHome(), params.getBasedir(), params.getConfigurationFile());
				properties.store(os, null);
				os.close();
			}
		} catch (FileNotFoundException e) {
			logger.error("Configuration file not found: " + params.isRelativeToHome() + " " + params.getBasedir() + " "
			        + params.getConfigurationFile(), e);
		} catch (IOException e) {
			logger.error("Error storing Configuration file", e);
		}
	}

	/**
	 * This method returns an enumeration of the properties that the file
	 * contains
	 *
	 * @return enumeration of keys
	 */
	public Enumeration<?> propertyNames() {
		return properties.propertyNames();
	}

	/**
	 * Clears the configuration.
	 */
	public void clear() {
	    properties.clear();
	}

	/**
	 * gets a copy of the configuration as Properties object
	 *
	 * @return Properties
	 */
	public Properties getAsProperties() {
		return (Properties) properties.clone();
	}
}
