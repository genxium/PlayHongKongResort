package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import utilities.DataUtils;
import org.json.simple.JSONObject;

public class EasyPreparedStatementBuilder {

    public static final String TAG = EasyPreparedStatementBuilder.class.getName();

    protected String m_table = null;
    protected List<String> m_selectCols = null;
    protected List<String> m_insertCols = null;
    protected List<Object> m_insertVals = null;
    protected List<String> m_updateCols = null;
    protected List<Object> m_updateVals = null;
    protected List<String> m_increaseCols = null;
    protected List<Object> m_increaseVals = null;
    protected List<String> m_decreaseCols = null;
    protected List<Object> m_decreaseVals = null;
    protected List<String> m_whereCols = null;
    protected List<String> m_whereOps = null;
    protected List<Object> m_whereVals = null;
    protected String m_whereLink = null;
    protected List<String> m_orderBy = null;
    protected List<String> m_orientations = null;
    protected List<Integer> m_limits = null;

    public EasyPreparedStatementBuilder() {

    }

    public EasyPreparedStatementBuilder from(String table) {
        m_table = table;
        return this;
    }

    public EasyPreparedStatementBuilder into(String table) {
        m_table = table;
        return this;
    }

    public EasyPreparedStatementBuilder update(String table) {
        m_table = table;
        return this;
    }

    public EasyPreparedStatementBuilder select(String col) {
        if (m_selectCols == null) m_selectCols = new LinkedList<String>();
        m_selectCols.add(col);
        return this;
    }

    public EasyPreparedStatementBuilder select(List<String> cols) {
        for (String col : cols) {
            select(col);
        }
        return this;
    }

    public EasyPreparedStatementBuilder select(String[] cols) {
        for (String col : cols) {
            select(col);
        }
        return this;
    }

    public EasyPreparedStatementBuilder insert(String col, Object val) {
        if (m_insertCols == null) m_insertCols = new LinkedList<String>();
        if (m_insertVals == null) m_insertVals = new LinkedList<Object>();
        m_insertCols.add(col);
        m_insertVals.add(val);
        return this;
    }

    public EasyPreparedStatementBuilder insert(List<String> cols, List<Object> vals) {
        if (cols == null || vals == null) return this;
        if (cols.size() != vals.size()) return this;
        int n = cols.size();
        for (int i = 0; i < n; i++) {
            String col = cols.get(i);
            Object val = vals.get(i);
            insert(col, val);
        }
        return this;
    }

    public EasyPreparedStatementBuilder insert(String[] cols, Object[] vals) {
        if (cols == null || vals == null) return this;
        if (cols.length != vals.length) return this;
        int n = cols.length;
        for (int i = 0; i < n; i++) {
            insert(cols[i], vals[i]);
        }
        return this;
    }

    public EasyPreparedStatementBuilder set(String col, Object val) {
        if (m_updateCols == null) m_updateCols = new LinkedList<String>();
        if (m_updateVals == null) m_updateVals = new LinkedList<Object>();
        m_updateCols.add(col);
        m_updateVals.add(val);
        return this;
    }

    public EasyPreparedStatementBuilder set(List<String> cols, List<Object> vals) {
        if (cols == null || vals == null) return this;
        if (cols.size() != vals.size()) return this;
        int n = cols.size();
        for (int i = 0; i < n; i++) {
            String col = cols.get(i);
            Object val = vals.get(i);
            set(col, val);
        }
        return this;
    }

    public EasyPreparedStatementBuilder set(String[] cols, Object[] vals) {
        if (cols == null || vals == null) return this;
        if (cols.length != vals.length) return this;
        int n = cols.length;
        for (int i = 0; i < n; i++) {
            set(cols[i], vals[i]);
        }
        return this;
    }

    public EasyPreparedStatementBuilder increase(String col, Object val) {
        if (m_increaseCols == null) m_increaseCols = new LinkedList<String>();
        if (m_increaseVals == null) m_increaseVals = new LinkedList<Object>();
        m_increaseCols.add(col);
        m_increaseVals.add(val);
        return this;
    }

    public EasyPreparedStatementBuilder increase(List<String> cols, List<Object> vals) {
        if (cols == null || vals == null) return this;
        if (cols.size() != vals.size()) return this;
        int n = cols.size();
        for (int i = 0; i < n; i++) {
            String col = cols.get(i);
            Object val = vals.get(i);
            increase(col, val);
        }
        return this;
    }

    public EasyPreparedStatementBuilder increase(String[] cols, Object[] vals) {
        if (cols == null || vals == null) return this;
        if (cols.length != vals.length) return this;
        int n = cols.length;
        for (int i = 0; i < n; i++) {
            increase(cols[i], vals[i]);
        }
        return this;
    }

    public EasyPreparedStatementBuilder decrease(String col, Object val) {
        if (m_decreaseCols == null) m_decreaseCols = new LinkedList<String>();
        if (m_decreaseVals == null) m_decreaseVals = new LinkedList<Object>();
        m_decreaseCols.add(col);
        m_decreaseVals.add(val);
        return this;
    }

    public EasyPreparedStatementBuilder decrease(List<String> cols, List<Object> vals) {
        if (cols == null || vals == null) return this;
        if (cols.size() != vals.size()) return this;
        int n = cols.size();
        for (int i = 0; i < n; i++) {
            String col = cols.get(i);
            Object val = vals.get(i);
            decrease(col, val);
        }
        return this;
    }

    public EasyPreparedStatementBuilder decrease(String[] cols, Object[] vals) {
        if (cols == null || vals == null) return this;
        if (cols.length != vals.length) return this;
        int n = cols.length;
        for (int i = 0; i < n; i++) {
            decrease(cols[i], vals[i]);
        }
        return this;
    }

    public EasyPreparedStatementBuilder where(String col, String op, Object val) {
        if (m_whereCols == null) m_whereCols = new LinkedList<String>();
        if (m_whereOps == null) m_whereOps = new LinkedList<String>();
        if (m_whereVals == null) m_whereVals = new LinkedList<Object>();
        m_whereCols.add(col);
        m_whereOps.add(op);
        m_whereVals.add(val);
        return this;
    }

    public EasyPreparedStatementBuilder where(List<String> cols, List<String> ops, List<Object> vals) {
        if (cols == null || ops == null || vals == null) return this;
        if (cols.size() != ops.size() || cols.size() != vals.size()) return this;
        int n = cols.size();
        for (int i = 0; i < n; i++) {
            String col = cols.get(i);
            String op = ops.get(i);
            Object val = vals.get(i);
            where(col, op, val);
        }
        return this;
    }

    public EasyPreparedStatementBuilder where(String[] cols, String[] ops, Object[] vals) {
        if (cols == null || ops == null || vals == null) return this;
        if (cols.length != ops.length || cols.length != vals.length) return this;
        int n = cols.length;
        for (int i = 0; i < n; i++) {
            where(cols[i], ops[i], vals[i]);
        }
        return this;
    }

    public EasyPreparedStatementBuilder where(String whereLink) {
        m_whereLink = whereLink;
        return this;
    }

    public EasyPreparedStatementBuilder order(String col, String orientation) {
        if (m_orderBy == null) m_orderBy = new LinkedList<String>();
        m_orderBy.add(col);

        if (orientation == null) return this;
        if (m_orientations == null) m_orientations = new LinkedList<String>();
        m_orientations.add(orientation);
        return this;
    }

    public EasyPreparedStatementBuilder order(String col) {
        order(col, null);
        return this;
    }

    public EasyPreparedStatementBuilder order(List<String> cols, List<String> orientations) {
        if (cols == null) return this;
        int n = cols.size();
        for (int i = 0; i < n; i++) {
            String col = cols.get(i);
            String orientation = null;
            if (orientations != null && orientations.size() > i) {
                orientation = orientations.get(i);
            }
            order(col, orientation);
        }
        return this;
    }

    public EasyPreparedStatementBuilder order(String[] cols, String[] orientations) {
        if (cols == null) return this;
        int n = cols.length;
        for (int i = 0; i < n; i++) {
            String col = cols[i];
            String orientation = null;
            if (orientations != null && orientations.length > i) {
                orientation = orientations[i];
            }
            order(col, orientation);
        }
        return this;
    }

    public EasyPreparedStatementBuilder order(List<String> cols) {
        return order(cols, null);
    }

    public EasyPreparedStatementBuilder order(String[] cols) {
        return order(cols, null);
    }

    public EasyPreparedStatementBuilder limit(int st, int ed) {
        if (m_limits == null) m_limits = new LinkedList<Integer>();
        m_limits.clear();
        m_limits.add(st);
        m_limits.add(ed);
        return this;
    }

    public EasyPreparedStatementBuilder limit(int num) {
        if (m_limits == null) m_limits = new LinkedList<Integer>();
        m_limits.clear();
        m_limits.add(num);
        return this;
    }

    protected String appendFromTable(String query) {
        String ret = query + " FROM " + m_table;
        return ret;
    }

    protected String appendIntoTable(String query) {
        String ret = query + " INTO " + m_table;
        return ret;
    }

    protected String appendWhere(String query) {
	if (m_whereCols == null) return query; 
        String ret = query;
	ret += " WHERE ";
	for (int i = 0; i < m_whereCols.size(); i++) {
		String col = m_whereCols.get(i);
		String op = m_whereOps.get(i);
		ret += ("`" + col + "`" + op + "?");
		if (i < m_whereCols.size() - 1) {
			if (m_whereLink == null) ret += " AND ";
			else ret += (" " + m_whereLink + " ");
		}
	}
        return ret;
    }

    protected String appendOrder(String query) {
        if (m_orderBy == null) return query;
        String ret = query;
	ret += " ORDER BY ";
	for (int i = 0; i < m_orderBy.size(); i++) {
		String col = m_orderBy.get(i);
		ret += ("`" + col + "`");
		if (m_orientations.size() > i) {
			String orientation = m_orientations.get(i);
			ret += " " + orientation;
		}
		if (i < m_orderBy.size() - 1) ret += ", ";
	}
        return ret;
    }

    protected String appendLimit(String query) {
        String ret = query;
        if (m_limits != null) {
            ret += " LIMIT ";
            for (int i = 0; i < m_limits.size(); i++) {
                ret += "?";
                if (i < m_limits.size() - 1) ret += ", ";
            }
        }
        return ret;
    }

    public PreparedStatement toSelect(Connection connection) {

        if (connection == null) return null;
        PreparedStatement statement = null;
        try {
            String query = "SELECT ";

            for (int i = 0; i < m_selectCols.size(); i++) {
                query += ("`" + m_selectCols.get(i) + "`");
                if (i < m_selectCols.size() - 1) query += ", ";
            }

            query = appendFromTable(query);
            query = appendWhere(query);
            query = appendOrder(query);
            query = appendLimit(query);

            int index = 1;

            statement = connection.prepareStatement(query);
            if (m_whereVals != null) {
                for (Object val : m_whereVals) {
                    statement.setObject(index++, val);
                }
            }
            if (m_limits != null) {
                for (Integer limit : m_limits) {
                    statement.setInt(index++, limit);
                }
            }

        } catch (Exception e) {

        }
        return statement;
    }

    public PreparedStatement toInsert(Connection connection) {
        if (connection == null) return null;
        PreparedStatement statement = null;
        try {
            String query = "INSERT";
            query = appendIntoTable(query);

            query += "(";
            for (int i = 0; i < m_insertCols.size(); i++) {
                query += ("`" + m_insertCols.get(i) + "`");
                if (i < m_insertCols.size() - 1) query += ", ";
            }
            query += ") ";

            query += " VALUES(";
            for (int i = 0; i < m_insertVals.size(); i++) {
                query += "?";
                if (i < m_insertVals.size() - 1) query += ", ";
            }
            query += ") ";

            int index = 1;

            statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            if (m_insertVals != null) {
                for (Object val : m_insertVals) {
                    statement.setObject(index++, val);
                }
            }

        } catch (Exception e) {

        }
        return statement;
    }

    public PreparedStatement toDelete(Connection connection) {
        if (connection == null) return null;
        PreparedStatement statement = null;
        try {
            String query = "DELETE";
            query = appendFromTable(query);
            query = appendWhere(query);
            query = appendLimit(query);

            int index = 1;
            statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            if (m_whereVals != null) {
                for (Object val : m_whereVals) {
                    statement.setObject(index++, val);
                }
            }

        } catch (Exception e) {

        }
        return statement;
    }

    public PreparedStatement toUpdate(Connection connection) {
        if (connection == null) return null;
        PreparedStatement statement = null;
        try {
            String query = "UPDATE " + m_table;
            query += " SET ";

            boolean hasPrevFields = false;

            if (m_updateCols != null) {
                for (int i = 0; i < m_updateCols.size(); i++) {
                    if (hasPrevFields) query += ", ";
                    hasPrevFields = true;
                    query += ("`" + m_updateCols.get(i) + "`" + "=?");
                }
            }

            if (m_increaseCols != null) {
                for (int i = 0; i < m_increaseCols.size(); i++) {
                    if (hasPrevFields) query += ", ";
                    hasPrevFields = true;
                    query += ("`" + m_increaseCols.get(i) + "`" + "=" + "`" + m_increaseCols.get(i) + "`" + "+?");
                }
            }

            if (m_decreaseCols != null) {
                for (int i = 0; i < m_decreaseCols.size(); i++) {
                    if (hasPrevFields) query += ", ";
                    hasPrevFields = true;
                    query += ("`" + m_decreaseCols.get(i) + "`" + "=" + "`" + m_decreaseCols.get(i) + "`" + "-?");
                }
            }

            query = appendWhere(query);
            query = appendLimit(query);

            int index = 1;
            statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            if (m_updateVals != null) {
                for (Object val : m_updateVals) {
                    statement.setObject(index++, val);
                }
            }

            if (m_increaseVals != null) {
                for (Object val : m_increaseVals) {
                    statement.setObject(index++, val);
                }
            }

            if (m_decreaseVals != null) {
                for (Object val : m_decreaseVals) {
                    statement.setObject(index++, val);
                }
            }

            if (m_whereVals != null) {
                for (Object val : m_whereVals) {
                    statement.setObject(index++, val);
                }
            }

            if (m_limits != null) {
                for (Integer val : m_limits) {
                    statement.setInt(index++, val);
                }
            }
        } catch (Exception e) {

        }
        return statement;
    }

    public List<JSONObject> execSelect() {
        List<JSONObject> ret = null;
        try {
            Connection connection = SQLHelper.getConnection();
            PreparedStatement statement = this.toSelect(connection);
            ResultSet rs = statement.executeQuery();
            if (rs != null) {
                ret = ResultSetUtil.convertToJSON(rs);
                rs.close();
            }
            statement.close();
            SQLHelper.closeConnection(connection);
        } catch (Exception e) {
            DataUtils.log(TAG, "select", e);
        }
        return ret;
    }

    public Integer execInsert() {
        Integer lastId = SQLHelper.INVALID;
        try {
            Connection connection = SQLHelper.getConnection();
            PreparedStatement statement = this.toInsert(connection);
            // the following command returns the last inserted row id for the auto incremented key
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            if (rs != null && rs.next()) {
                lastId = (int) rs.getLong(1);
                rs.close();
            }
            statement.close();
            SQLHelper.closeConnection(connection);
        } catch (Exception e) {
            // return the invalid value for exceptions
            DataUtils.log(TAG, "insert", e);
        }
        return lastId;
    }

    public boolean execUpdate() {
        boolean bRet = false;
        try {
            Connection connection = SQLHelper.getConnection();
            PreparedStatement statement = this.toUpdate(connection);
            // the following command returns the last inserted row id for the auto incremented key
            statement.executeUpdate();
            statement.close();
            SQLHelper.closeConnection(connection);
            bRet = true;
        } catch (Exception e) {
            DataUtils.log(TAG, "update", e);
        }
        return bRet;
    }

    public boolean execDelete() {
        boolean bRet = false;
        try {
            Connection connection = SQLHelper.getConnection();
            PreparedStatement statement = this.toDelete(connection);
            // the following command returns the last inserted row id for the auto incremented key
            statement.executeUpdate();
            statement.close();
            SQLHelper.closeConnection(connection);
            bRet = true;
        } catch (Exception e) {
            DataUtils.log(TAG, "delete", e);
        }
        return bRet;
    }
}
