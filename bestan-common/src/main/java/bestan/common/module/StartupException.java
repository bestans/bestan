package bestan.common.module;

public class StartupException extends RuntimeException {
	private static final long serialVersionUID = -7739765801156813426L;

	public StartupException(IModule startup) {
		super(startup.getClass().getSimpleName() + " failed.");
	}
}
