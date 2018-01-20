package bestan.common.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;


/**
 * Allows transparent access to files. Subclasses implement Persistence for normal
 */
public abstract class Persistence {
	private static Persistence instance = null;

	/**
	 * Returns the Persistence manager for this environment
	 * 
	 * @return Persistence
	 */
	public static Persistence get() {
		if (instance == null) {
			try {
				System.getProperty("user.home");
			} catch (AccessControlException e) {
				e.printStackTrace();
				return null;
			}
			
			instance = new FileSystemPersistence();
		}
		return instance;
	}

	/**
	 * Gets an input stream to this "virtual" file
	 * 
	 * @param relativeToHome
	 *            should this file be placed below the users home directory?
	 * @param basedir
	 *            directory prefix which is ignore in webstart environment
	 * @param filename
	 *            filename (without path)
	 * @return InputStream
	 * @throws IOException
	 *             on IO error
	 */
	public abstract InputStream getInputStream(boolean relativeToHome, String basedir,
	        String filename) throws IOException;

	/**
	 * Gets an output stream to this "virtual" file
	 * 
	 * @param relativeToHome
	 *            should this file be placed below the users home directory?
	 * @param basedir
	 *            directory prefix which is ignore in webstart environment
	 * @param filename
	 *            filename (without path)
	 * @return OutputStream
	 * @throws IOException
	 *             on IO error
	 */
	public abstract OutputStream getOutputStream(boolean relativeToHome, String basedir,
	        String filename) throws IOException;

}
