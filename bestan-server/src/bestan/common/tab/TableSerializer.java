package bestan.common.tab;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;

import org.slf4j.Logger;

import bestan.common.exception.TableLoadException;
import bestan.common.util.Global;
import bestan.log.GLog;

public class TableSerializer implements ISerializer {
	private static final Logger logger = GLog.log;

    public static enum EM_TYPE_COLUMN
    {
        EM_TYPE_COLUMN_INVALID,

        EM_TYPE_COLUMN_INT,
        EM_TYPE_COLUMN_UINT,
        EM_TYPE_COLUMN_LONG,
        EM_TYPE_COLUMN_ULONG,
        EM_TYPE_COLUMN_FLOAT,
        EM_TYPE_COLUMN_STRING,
        EM_TYPE_COLUMN_INT_ARRAY,
        EM_TYPE_COLUMN_FLOAT_ARRAY,

        EM_TYPE_COLUMN_COUNT,
    };

    public final int MaxColumnCount = 512;

    public final int MaxColumnNameLength = 32;

    public static final String[] g_ColumnTypeToString = new String[]
    {
        "INVALID",
        "INT",
        "UINT",
        "INT64",
        "UINT64",
        "FLOAT",
        "STRING",
        "STRING_INT",
        "STRING_FLOAT",
        "INT",
    };
    
    private RandomAccessFile m_File;
    private String[] m_ColumnNames;
    private EM_TYPE_COLUMN[] m_Columns;
    private int m_ColumnCount;
    private int m_ColumnOfCurrentLine;
    private int m_LineEndPos;
    private static String m_FileName;

    private byte[] m_ReadBuffer;
    private int m_CurrentPos;
    private int m_FileEndPos;
    private boolean m_HasReadBuffer;
    private boolean m_IsCheckColumn;
    
    private int m_CurrentID;

    public static int MaxFileLineSize = 16 * 1024;
    public static byte[] m_FileLineBuffer = new byte[MaxFileLineSize];

    public TableSerializer()
    {
    	m_CurrentID = Global.INVALID_VALUE;
    }

    public static int FileLineCount(RandomAccessFile file)
    {
    	try {
    		if (file == null)
                return 0;
            file.seek(0);

            int realSize = file.read(m_FileLineBuffer, 0, MaxFileLineSize);
            int fileLineCount = 0;
            while (realSize > 0)
            {
                for (int i = 0; i < realSize && i < MaxFileLineSize; ++i)
                {
                    if (m_FileLineBuffer[i] == '\n')
                        ++fileLineCount;
                }
                realSize = file.read(m_FileLineBuffer, 0, MaxFileLineSize);
            }

            // by lxw modifid old code : return fileLineCount + 1; ==> return fileLineCount;
            return fileLineCount + 1;
    	} catch(IOException ioe) {
    		ioe.printStackTrace();
    		return 0;
    	} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
    }

    public static int FileLineEnd(byte[] fileBuffer, int currentIndex, int fileEndIndex)
    {
        if (currentIndex < 0 || currentIndex >= fileBuffer.length ||
            fileEndIndex < 0 || fileEndIndex >= fileBuffer.length)
            return 0;
        while (currentIndex <= fileEndIndex)
        {
            if ((fileBuffer[currentIndex] == '\r' && fileBuffer[currentIndex + 1] == '\n' && currentIndex < fileEndIndex) ||
                (fileBuffer[currentIndex] == '\n' && currentIndex < fileEndIndex))
            {
                return currentIndex - 1;
            }

            ++currentIndex;
        }
        return fileEndIndex;
    }

    public boolean PrepareRead()
    {
        if (m_HasReadBuffer)
            return true;

        if (m_File == null)
        {
            ShowErrorLog(m_FileName, "File Load Failed.");
            return false;
        }
        try {
	        if (m_File.length() > 2147483647)
	        {
	            ShowErrorLog(m_FileName, "File Size Too Big:FileSize = " + m_File.length());
	            return false;
	        }
        
	        int fileSize = (int)m_File.length();
	        m_ReadBuffer = new byte[fileSize + 4];
	        int readSize = m_File.read(m_ReadBuffer, 0, fileSize);
	
	        m_CurrentPos = 0;
	        m_FileEndPos = readSize - 1;
	        m_HasReadBuffer = true;
	        return true;
        } catch(IOException e) {
        	e.printStackTrace();
        	return false;
        }
    }

    private boolean SaveColumnNames()
    {
        int curlineLength = m_LineEndPos - m_CurrentPos + 1;
        if (curlineLength <= 0 || curlineLength > (MaxColumnCount * (MaxColumnNameLength + 1))) //����+1����Ϊ\t�ָ���
            return false;

        try {
        	byte[] nameList = new byte[curlineLength];
        	System.arraycopy(m_ReadBuffer, m_CurrentPos, nameList, 0, curlineLength);
        	String seperator = "\t";
        	String lineStr = new String(nameList, "utf-8");
       
        	String[] nameStrList = lineStr.split(seperator);
	        if (nameStrList.length > MaxColumnCount)
	            return false;

	        m_ColumnNames = nameStrList;
	        return true;
        } catch(UnsupportedEncodingException e) {
        	e.printStackTrace();
        	return false;
        }
    }

    private EM_TYPE_COLUMN ParseColumnType(String columnType)
    {
        if (columnType == null)
            return EM_TYPE_COLUMN.EM_TYPE_COLUMN_INVALID;
        else if (columnType.equalsIgnoreCase("INT"))
            return EM_TYPE_COLUMN.EM_TYPE_COLUMN_INT;
        else if (columnType.equalsIgnoreCase("UINT"))
            return EM_TYPE_COLUMN.EM_TYPE_COLUMN_UINT;
        else if (columnType.equalsIgnoreCase("INT64"))
            return EM_TYPE_COLUMN.EM_TYPE_COLUMN_LONG;
        else if (columnType.equalsIgnoreCase("UINT64"))
            return EM_TYPE_COLUMN.EM_TYPE_COLUMN_ULONG;
        else if (columnType.equalsIgnoreCase("FLOAT"))
            return EM_TYPE_COLUMN.EM_TYPE_COLUMN_FLOAT;
        else if (columnType.equalsIgnoreCase("STRING"))
            return EM_TYPE_COLUMN.EM_TYPE_COLUMN_STRING;
        else if (columnType.equalsIgnoreCase("STRING_INT"))
            return EM_TYPE_COLUMN.EM_TYPE_COLUMN_INT_ARRAY;
        else if (columnType.equalsIgnoreCase("STRING_FLOAT"))
            return EM_TYPE_COLUMN.EM_TYPE_COLUMN_FLOAT_ARRAY;
        else
        {
            ShowErrorLog(m_FileName, "invalid column type =" + columnType);
            return EM_TYPE_COLUMN.EM_TYPE_COLUMN_INVALID;
        }
    }

    private boolean SaveColumnTypes()
    {
        int curlineLength = m_LineEndPos - m_CurrentPos + 1;
        if (curlineLength <= 0 || curlineLength > (MaxColumnCount * (MaxColumnNameLength + 1))) //����+1����Ϊ\t�ָ���
            return false;

        try {
	        byte[] nameList = new byte[curlineLength];
	        System.arraycopy(m_ReadBuffer, m_CurrentPos, nameList, 0, curlineLength);
	        String seperator = "\t";
	        String lineStr = new String(nameList, "utf-8");
	        String[] nameStrList = lineStr.split(seperator);
	        if (nameStrList.length > MaxColumnCount)
	            return false;
	        m_Columns = new EM_TYPE_COLUMN[nameStrList.length];
	        for (int count = 0; count < nameStrList.length; ++count)
	        {
	            m_Columns[count] = ParseColumnType(nameStrList[count]);
	        }
	        m_ColumnCount = nameStrList.length;
	
	        return true;
        } catch(UnsupportedEncodingException e) {
        	e.printStackTrace();
        	return false;
        }
    }

    public void SkipHeader()
    {
        if (!PrepareRead())
            return;

        if (!NextLine())
        {
            ShowErrorLog(m_FileName, "first line error.");
            return;
        }

        SaveColumnNames();

        if (!NextLine())
        {
            ShowErrorLog(m_FileName, "second line error.");
            return;
        }

        SaveColumnTypes();
    }

    public Boolean NextLine()
    {
        if (!PrepareRead())
            return false;

        m_ColumnOfCurrentLine = 0;

        if (m_LineEndPos == 0)
        {
            m_LineEndPos = FileLineEnd(m_ReadBuffer, m_CurrentPos, m_FileEndPos);
        }
        else
        {
            if (m_LineEndPos < m_FileEndPos)
            {
                if (m_ReadBuffer[m_LineEndPos + 1] == '\r')
                    m_CurrentPos = m_LineEndPos + 2 + 1;
                else
                    m_CurrentPos = m_LineEndPos + 1 + 1;

                m_LineEndPos = FileLineEnd(m_ReadBuffer, m_CurrentPos, m_FileEndPos);
            }
        }
        if (m_ReadBuffer[m_CurrentPos] == '#' ||
            m_ReadBuffer[m_CurrentPos] == '\r' ||
            m_ReadBuffer[m_CurrentPos] == '\n')
        {
            NextLine();
        }

        if (m_CurrentPos >= m_LineEndPos || m_CurrentPos >= m_FileEndPos)
            return false;

        if (m_ReadBuffer[m_CurrentPos] == '\t' || m_ReadBuffer[m_CurrentPos] == 0)
        {
            return false;
        }

        return true;
    }

    public static void ShowErrorLog(String filename, String errMsg)
    {
    	logger.error(filename + ":" + errMsg);
    }
    
    public static void ShowDebugLog(String filename, String errMsg)
    {
    	logger.debug(filename + ":" + errMsg);
    }

    public boolean OpenRead(String fileName)
    {
        m_FileName = fileName;
        try
        {
        	if (m_File != null)
            {
            	m_File.seek(0);
                return true;
            }
        	
            m_File = new RandomAccessFile(fileName, "rw");
            m_File.seek(0);
            return true;
        }
        catch (FileNotFoundException fnfe) {
        	fnfe.printStackTrace();
        	return false;
		}
        catch (IOException ioe)
        {
            ioe.printStackTrace();
            return false;
        }
    }

    public int LineCount()
    {
        return FileLineCount(m_File);
    }

    public boolean CheckColumnCount()
    {
        if (m_IsCheckColumn && m_ColumnOfCurrentLine != m_ColumnCount)
        {
            ShowErrorLog(m_FileName, "column count isn't match! ColumnOfCurrentLine = " + m_ColumnOfCurrentLine + ", ColumnCount = " + m_ColumnCount);
            return false;
        }
        return true;
    }

    private boolean CheckColumnType(EM_TYPE_COLUMN columnType)
    {
        if (m_IsCheckColumn)
        {
            if (m_Columns[m_ColumnOfCurrentLine] == columnType)
                return true;

            ShowErrorLog(m_FileName, "column isn't match.line:" + m_ColumnOfCurrentLine + ":struct columnType = " + g_ColumnTypeToString[columnType.ordinal()] +
                            ", table columnType:" + g_ColumnTypeToString[m_Columns[m_ColumnOfCurrentLine].ordinal()]);
            return false;
        }
        return true;
    }

    public void Close()
    {
    	try {
	        if (m_File != null)
	        {
	            m_File.close();
	            m_File = null;
	        }
    	} catch(IOException ioe) {
    		ioe.printStackTrace();
    	}
    }
    
	public void SetCheckColumn(boolean isCheck)
    {
        m_IsCheckColumn = isCheck;
    }

    public int ColumnCount()
    {
        return m_ColumnCount;
    }

    private void Set(TableInterface t, String func, EM_TYPE_COLUMN eType) throws TableLoadException
    {
        if (!CheckColumnType(eType))
            return;
        
        String parseStr = GetParseStr();
        if (parseStr == null)
            return;
        
        Class<? extends TableInterface> cls = t.getClass();
        if(null == cls) {
        	logger.error("get TableInterface class is nil");
        	return;
        }
        
        Method implMethod = null;
        try {
	        switch (eType) {
				case EM_TYPE_COLUMN_INT:
				{
					if (parseStr.isEmpty()) {
						logger.error("table[" + m_FileName + "], id[" + m_CurrentID + "]," + "parse uint error");
						throw new TableLoadException(m_FileName);
					}
					implMethod = cls.getMethod(func, int.class);
					implMethod.invoke(t, Integer.valueOf(parseStr).intValue());
				}
				break;
				case EM_TYPE_COLUMN_LONG:
				{
					if (parseStr.isEmpty()) {
						logger.error("table[" + m_FileName + "], id[" + m_CurrentID + "]," + "parse long error");
						throw new TableLoadException(m_FileName);
					}
					implMethod = cls.getMethod(func, long.class);
					implMethod.invoke(t, Long.valueOf(parseStr).longValue());
				}
				break;
				case EM_TYPE_COLUMN_FLOAT:
				{
					if (parseStr.isEmpty()) {
						logger.error("table[" + m_FileName + "], id[" + m_CurrentID + "]," + "parse float error");
						throw new TableLoadException(m_FileName);
					}
					implMethod = cls.getMethod(func, float.class);
					implMethod.invoke(t, Float.valueOf(parseStr).floatValue());
				}
				break;
				case EM_TYPE_COLUMN_STRING:
				{
					implMethod = cls.getMethod(func, String.class);
					implMethod.invoke(t, parseStr);
				}
				break;
				case EM_TYPE_COLUMN_INT_ARRAY:
				{
					String[] parseStrList = parseStr.split("\\|");
					if (parseStrList == null) {
						logger.error("table[" + m_FileName + "], id[" + m_CurrentID + "]," + "parse intarray error");
						throw new TableLoadException(m_FileName);
					}
			        int[] tempArr = new int[parseStrList.length];
			        for (int i = 0; i < parseStrList.length; ++i)
			        {
						if (parseStrList[i].isEmpty()) {
							logger.error("table[" + m_FileName + "], id[" + m_CurrentID + "]," + "parse intarray error");
							throw new TableLoadException(m_FileName);
						}
			        	tempArr[i] = Integer.valueOf(parseStrList[i]).intValue();
			        }
			        
			        implMethod = cls.getMethod(func, int[].class);
					implMethod.invoke(t, tempArr);
				}
				break;
				case EM_TYPE_COLUMN_FLOAT_ARRAY:
				{
					String[] parseStrList = parseStr.split("\\|");
					if (parseStrList == null) {
						logger.error("table[" + m_FileName + "], id[" + m_CurrentID + "]," + "parse floatarray error");
						throw new TableLoadException(m_FileName);
					}
					float[] tempArr = new float[parseStrList.length];
			        for (int i = 0; i < parseStrList.length; ++i)
			        {
						if (parseStrList[i].isEmpty()) {
							logger.error("table[" + m_FileName + "], id[" + m_CurrentID + "]," + "parse floatarray error");
							throw new TableLoadException(m_FileName);
						}
			        	tempArr[i] = Float.valueOf(parseStrList[i]).floatValue();
			        }
			        
			        implMethod = cls.getMethod(func, float[].class);
					implMethod.invoke(t, tempArr);
				}
				break;
				default:
				{
					logger.error("Type is invalid");
					return;
				}
			}// end of switch
		} catch (NoSuchMethodException nse) {
			nse.printStackTrace();
			throw new TableLoadException(m_FileName + " ", nse.getMessage() + " Current id: " + m_CurrentID + 
					" ColumnOfCurrentLine:" + m_ColumnOfCurrentLine + " ColumnName:" + m_ColumnNames[m_ColumnOfCurrentLine - 1]);
		} catch (SecurityException se) {
			se.printStackTrace();
			throw new TableLoadException(m_FileName + " ", se.getMessage() + " Current id: " + m_CurrentID + 
					" ColumnOfCurrentLine:" + m_ColumnOfCurrentLine + " ColumnName:" + m_ColumnNames[m_ColumnOfCurrentLine - 1]);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TableLoadException(m_FileName + " ", e.getMessage() + " Current id: " + m_CurrentID + 
					" ColumnOfCurrentLine:" + m_ColumnOfCurrentLine + " ColumnName:" + m_ColumnNames[m_ColumnOfCurrentLine - 1]);
		}
    }

	public ISerializer Parse(TableInterface t, String func, EM_TYPE_COLUMN eType) throws TableLoadException { Set(t, func, eType); return this; }

    private boolean IsSeparator(byte c)
    {
        return (c == '\t') || (c == '\r') || (c == '\n') || c == 0;
    }

    private String GetParseStr()
    {

        m_ColumnOfCurrentLine++;

        if (m_CurrentPos > m_LineEndPos)
        {
            return null;
        }
        int parseBeginPos = m_CurrentPos;
        while (!IsSeparator(m_ReadBuffer[m_CurrentPos]) && m_CurrentPos < m_FileEndPos)
        {
            m_CurrentPos++;
        }

        int curlineLength = m_CurrentPos - parseBeginPos;
        if (curlineLength < 0)
            return null;

        String parseStr;
        if (curlineLength == 0)
        {
            parseStr = "";
        }
        else
        {
            byte[] parseByteList = new byte[curlineLength];
            System.arraycopy(m_ReadBuffer, parseBeginPos, parseByteList, 0, curlineLength);
            try {
            	parseStr = new String(parseByteList, "utf-8");
            } catch(UnsupportedEncodingException uee) {
            	uee.printStackTrace();
            	return null;
            }
        }

        ++m_CurrentPos;
        return parseStr;
    }

    public String[] GetColumnNames() { return m_ColumnNames; }

	public void SkipField()
    {
        GetParseStr();
    }
    
    public void SetCurrentID(int id){
    	m_CurrentID = id;
    }
}
