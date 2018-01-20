package bestan.common.db;

import java.io.IOException;
import java.sql.SQLException;

public interface IDatabaseInitialSchema {
	public void onServerStartInitialSchema(final DBTransaction transaction) throws SQLException, IOException;
}
