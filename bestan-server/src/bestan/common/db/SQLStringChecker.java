package bestan.common.db;

import java.sql.SQLException;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Helper class to validate strings and escape SQL strings.
 */
public class SQLStringChecker {
	private static final String RE_INT = "^-?[0-9 ]*$";
    private static final String RE_INT_LIST = "^-?[0-9, ]*$";
    
	/**
	 * This method returns true if a string is valid because it lacks of any kind of
	 * control or escape character.
	 *
	 * @param string
	 *            The string to check
	 * @return true if the string is valid for storing it at database or as XML.
	 */
	public static boolean validString(String string) {
		if (string.indexOf('\\') != -1) {
			return false;
		}
		if (string.indexOf('\'') != -1) {
			return false;
		}
		if (string.indexOf('"') != -1) {
			return false;
		}
		if (string.indexOf('%') != -1) {
			return false;
		}
		if (string.indexOf(';') != -1) {
			return false;
		}
		if (string.indexOf(':') != -1) {
			return false;
		}
		if (string.indexOf('#') != -1) {
			return false;
		}
		if (string.indexOf('<') != -1) {
			return false;
		}
		if (string.indexOf('>') != -1) {
			return false;
		}
		return true;
	}

	/**
	 * Escapes ' and \ in a string so that the result can be passed into an SQL
	 * command. The parameter has be quoted using ' in the sql. Most database
	 * engines accept single quotes around numbers as well.
	 * <p>
	 * Please note that special characters for LIKE and other matching commands
	 * are not quotes. The result of this method is suitable for INSERT, UPDATE
	 * and an "=" operator in the WHERE part.
	 *
	 * @param param
	 *            string to quote
	 * @return quoted string
	 */
	public static String escapeSQLString(String param) {
		if (param == null) {
			return param;
		}
		return param.replace("'", "''").replace("\\", "\\\\");
	}

	/**
	 * Trims the string to the specified size without error in case it is already shorter. 
	 * Escapes ' and \ in a string so that the result can be passed into an SQL
	 * command. The parameter has be quoted using ' in the sql. Most database
	 * engines accept single quotes around numbers as well.
	 * <p>
	 * Please note that special characters for LIKE and other matching commands
	 * are not quotes. The result of this method is suitable for INSERT, UPDATE
	 * and an "=" operator in the WHERE part.
	 *
	 * @param param
	 *            string to quote
	 * @param size maximal length of this string before encoding
	 * @return quoted string
	 */
	public static String trimAndEscapeSQLString(String param, int size) {
		if (param == null) {
			return param;
		}
		String res = param;
		if (res.length() > size) {
			res = res.substring(0, size);
		}
		return escapeSQLString(res);
	}
	
	/**
     * Replaces variables SQL-Statements and prevents SQL injection attacks
     * @warn 检查sql是否带有攻击参数 因为游戏中并没有像其他业务一样会直接从读取数据进行数据库操作都是有程序员自己写的变量也都是正确的
     *
     * @param sql SQL-String
     * @param params replacement parameters
     * @return SQL-String with substituted parameters
     * @throws SQLException in case of an sql injection attack
     */
    public String subst(String sql, Map<String, ?> params) throws SQLException {
    	if (params == null) {
    		return sql;
    	}
        StringBuffer res = new StringBuffer();
        StringTokenizer st = new StringTokenizer(sql, "([]'", true);
        String lastToken = "";
        String secondLastToken = "";
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (lastToken.equals("[")) {

                // replace variables
                Object temp = params.get(token);
                if (temp != null) {
                    token = temp.toString();
                } else {
                    token = "";
                }

                // intercept SQL-Injection
                if (secondLastToken.equals("(")) {
                    if (!token.matches(RE_INT_LIST)) {
                        throw new SQLException("Illegal argument: \"" + token + "\" is not an integer list"); 
                    }
                } else if (secondLastToken.equals("'")) {
                    if (token.length() > 0) {
                        token = SQLStringChecker.escapeSQLString(token);
                    }
                } else {
                    if (!token.matches(RE_INT)) {
                        throw new SQLException("Illegal argument: \"" + token + "\" is not an integer."); 
                    }
                }
            }
            secondLastToken = lastToken;
            lastToken = token.trim();
            if (token.equals("[") || token.equals("]")) {
                token = "";
            }
            res.append(token);
        }
        return res.toString();
    }
}
