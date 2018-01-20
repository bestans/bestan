package bestan.common.config;

/**
 * ConfigurationParams encapsulates parameters to be
 * used to initialize the Configuration class.
 */
public class ConfigurationParams {

	private boolean relativeToHome = false;

	private String basedir;

	/** Default name of configuration file */
	private String configurationFile;

	private boolean persistence = true;

	public ConfigurationParams() {
		// default constructor
	}

	public ConfigurationParams(boolean relativeToHome, String basedir, String configurationFile) {
		this.relativeToHome = relativeToHome;
		this.basedir = basedir;
		this.configurationFile = configurationFile;
	}

	public void setRelativeToHome(boolean relativeToHome) {
		this.relativeToHome = relativeToHome;
	}

	public boolean isRelativeToHome() {
		return relativeToHome;
	}

	public void setBasedir(String basedir) {
		this.basedir = basedir;
	}

	public String getBasedir() {
		return basedir;
	}

	public void setConfigurationFile(String configurationFile) {
		this.configurationFile = configurationFile;
	}

	public String getConfigurationFile() {
		return configurationFile;
	}

	public void setPersistence(boolean persistence) {
		this.persistence = persistence;
	}

	public boolean isPersistence() {
		return persistence;
	}

}
