package bestan.common.tab;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import bestan.common.exception.TableLoadException;


public class Table<T extends TableInterface> {
	private T[] m_Rows;
	private int m_Count;

	private final int TitleRowCount = 2;

	private Map<String, Integer> m_ColumnNameMap;

	private int m_LastIndex;

	private String m_FileName;

	private Class<T> tableType;

	public Table(Class<T> tableType) {
		this.m_ColumnNameMap = new HashMap<String, Integer>();
		this.tableType = tableType;

	}

	private void SetColumnNames(String[] columnNames, int columnCount) {
		if (null == columnNames) {
			return;
		}

		if (columnCount <= 0) {
			return;
		}

		for (int i = 0; i < columnCount; ++i) {
			if (columnNames[i] == null) {
				break;
			}

			m_ColumnNameMap.put(columnNames[i], i);
		}
	}

	private int BinarySearch(int startIndex, int endIndex, int key) {
		if (m_Rows == null)
			return -1;

		int start = startIndex;
		int end = endIndex;
		int middle = (end + start) / 2;
		while (start < end && !(m_Rows[middle].getIndex() == key)) {
			if (m_Rows[middle].getIndex() < key) {
				start = middle + 1;
			} else {
				end = middle - 1;
			}

			middle = (end + start) / 2;
		}
		return m_Rows[middle].getIndex() == key ? middle : -1;
	}

	private T Row(int ID) {
		if (m_LastIndex >= 0 && m_LastIndex < m_Count && m_Rows[m_LastIndex].getIndex() == ID) {
			return m_Rows[m_LastIndex];
		}
		int foundIndex = BinarySearch(0, m_Count - 1, ID);
		if (foundIndex < 0) {
			TableSerializer.ShowDebugLog(m_FileName, "can't find row! rowID = " + ID);
			return null;
		}

		m_LastIndex = foundIndex;
		return m_Rows[foundIndex];
	}

	/**
	 * java 模板实例化 通过反射
	 * @return
	 */
	public T createT() {
		T empty = null;
		try {
			empty = this.tableType.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return empty;
	}

	/**
	 * 在java 中如果使用反射来创建 数组 则需要(T[]) Array.newInstance(this.tableType, size);
	 * 如果使用new T[size]; 会报出 error: Cannot create a generic array of T
	 * @param size
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T[] createTArray(int size) {
		return (T[]) Array.newInstance(this.tableType, size);
	}

	public boolean load(String fileName) throws TableLoadException {
		return load(fileName, CommonTableDB.RootPath);
	}

	public boolean load(String fileName, String rootPath) throws TableLoadException {
		TableSerializer s = new TableSerializer();
		m_FileName = rootPath + fileName;
		s.SetCheckColumn(true);
		if (!s.OpenRead(m_FileName)) {
			TableSerializer.ShowErrorLog(m_FileName, "Open Read Table Failed.");
			throw new TableLoadException(fileName);
		}
		
		// Skip the Title
		s.SkipHeader();
		
		int maxRowCount = s.LineCount() - TitleRowCount;
		m_Rows = createTArray(maxRowCount);
		for (int i = 0; i < maxRowCount; ++i) {
			m_Rows[i] = createT();
		}
		m_Count = 0;
		while (s.NextLine()) {
			if (m_Count >= maxRowCount) {
				TableSerializer.ShowErrorLog(m_FileName, "table rows error." + m_Count);
				break;
			}

			m_Rows[m_Count].mapData(s);

			if (m_Count > 0 && m_Rows[m_Count].getIndex() <= m_Rows[m_Count - 1].getIndex()) {
				TableSerializer.ShowErrorLog(m_FileName, "table id isn't in order:" + m_Count);
				throw new TableLoadException(fileName);
			}
			if (m_Count == 0 && !s.CheckColumnCount()) {
				throw new TableLoadException(fileName);
			}

			++m_Count;
		}
		SetColumnNames(s.GetColumnNames(), s.ColumnCount());
		s.Close();
		return true;
	}

	public Boolean ContainsRow(int ID) {
		int foundIndex = BinarySearch(0, m_Count - 1, ID);
		if (foundIndex >= 0) {
			m_LastIndex = foundIndex;
		}
		return foundIndex >= 0;
	}

	public T get(int ID) {
		return Row(ID);
	}

	/**
	 * 通过index 查找
	 * @param index
	 * @return
	 */
	public T getRowByIndex(int index) {
		if (index < 0 || index >= m_Count)
			return null;

		return m_Rows[index];
	}
	/**
	 * 通过表ID获取index
	 * @param ID
	 * @return
	 */
	public int getIndexByID(int ID){
		if (m_LastIndex >= 0 && m_LastIndex < m_Count && m_Rows[m_LastIndex].getIndex() == ID) {
			return m_LastIndex;
		}
		return BinarySearch(0, m_Count - 1, ID);
	}

	public int rowCount() {
		return m_Count;
	}

	public int GetColumnIndexByName(String name) {
		if (!m_ColumnNameMap.containsKey(name)) {
			return -1;
		}
		return m_ColumnNameMap.get(name).intValue();
	}
}
