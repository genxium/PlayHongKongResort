package dao;

import utilities.Loggy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class SQLBuilder {

    public static final String TAG = SQLBuilder.class.getName();
    public static final String ALIAS_PREFIX = "bsajkfhoi";

    public static class PrimaryTableField {
        public String name;

        public PrimaryTableField(final String aName) {
            name = aName;
        }
    }

    public static class OnCondition {
        public String key; // the key bound to the joining table
        public String op; // the operator
        public Object val;

        public OnCondition(final String aKey, final String aOp, final Object aVal) {
            key = aKey;
            op = aOp;
            val = aVal;
        }
    }

    protected String m_table = null;
    protected List<String> m_selectCols = null;

    protected List<String> m_insertCols = null;
    protected List<List<Object>> m_insertVals = null;

    protected List<String> m_replaceCols = null;
    protected List<List<Object>> m_replaceVals = null;

    protected List<String> m_updateCols = null;
    protected List<Object> m_updateVals = null;

    protected List<String> m_increaseCols = null;
    protected List<Object> m_increaseVals = null;
    protected List<String> m_decreaseCols = null;
    protected List<Object> m_decreaseVals = null;

    protected Map<String, List<OnCondition>> m_join = null;

    protected List<String> m_whereCols = null;
    protected List<String> m_whereOps = null;
    protected List<Object> m_whereVals = null;
    protected String m_whereLink = null;

    protected List<String> m_orderBy = null;
    protected List<String> m_orientations = null;

    protected List<Integer> m_limits = null;
    protected boolean m_ignore = false;

    public SQLBuilder() {

    }

    public SQLBuilder from(String table) {
        m_table = table;
        return this;
    }

    public SQLBuilder into(String table) {
        m_table = table;
        return this;
    }

    public SQLBuilder update(String table) {
        m_table = table;
        return this;
    }

    public SQLBuilder select(String col) {
        if (m_selectCols == null) m_selectCols = new LinkedList<String>();
        m_selectCols.add(col);
        return this;
    }

    public SQLBuilder select(List<String> cols) {
        for (String col : cols) select(col);
        return this;
    }

    public SQLBuilder select(String[] cols) {
        for (String col : cols) select(col);
        return this;
    }

    public SQLBuilder insert(String[] cols, Object[] vals) {
        if (cols == null || vals == null || cols.length != vals.length) return this;
        if (m_insertCols == null) {
            m_insertCols = new ArrayList<>(); // lazy init
            Collections.addAll(m_insertCols, cols);
        }
        if (m_insertVals == null) m_insertVals = new ArrayList<List<Object>>(); // lazy init
        List<Object> tmp = new ArrayList<Object>();
        Collections.addAll(tmp, vals);
        m_insertVals.add(tmp);
        return this;
    }

    public SQLBuilder replace(String[] cols, Object[] vals) {
        if (cols == null || vals == null || cols.length != vals.length) return this;
        if (m_replaceCols == null) {
            m_replaceCols = new ArrayList<>(); // lazy init
            Collections.addAll(m_replaceCols, cols);
        }
        if (m_replaceVals == null) m_replaceVals = new ArrayList<List<Object>>(); // lazy init
        List<Object> tmp = new ArrayList<Object>();
        Collections.addAll(tmp, vals);
        m_replaceVals.add(tmp);
        return this;
    }

    public SQLBuilder ignore(boolean val) {
        m_ignore = val;
        return this;
    }

    public SQLBuilder set(String col, Object val) {
        if (m_updateCols == null) m_updateCols = new LinkedList<String>();
        if (m_updateVals == null) m_updateVals = new LinkedList<Object>();
        m_updateCols.add(col);
        m_updateVals.add(val);
        return this;
    }

    public SQLBuilder set(List<String> cols, List<Object> vals) {
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

    public SQLBuilder set(String[] cols, Object[] vals) {
        if (cols == null || vals == null) return this;
        if (cols.length != vals.length) return this;
        int n = cols.length;
        for (int i = 0; i < n; i++) {
            set(cols[i], vals[i]);
        }
        return this;
    }

    public SQLBuilder increase(String col, Object val) {
        if (m_increaseCols == null) m_increaseCols = new LinkedList<String>();
        if (m_increaseVals == null) m_increaseVals = new LinkedList<Object>();
        m_increaseCols.add(col);
        m_increaseVals.add(val);
        return this;
    }

    public SQLBuilder increase(String[] cols, Object[] vals) {
        if (cols == null || vals == null) return this;
        if (cols.length != vals.length) return this;
        int n = cols.length;
        for (int i = 0; i < n; i++) {
            increase(cols[i], vals[i]);
        }
        return this;
    }

    public SQLBuilder decrease(String col, Object val) {
        if (m_decreaseCols == null) m_decreaseCols = new LinkedList<String>();
        if (m_decreaseVals == null) m_decreaseVals = new LinkedList<Object>();
        m_decreaseCols.add(col);
        m_decreaseVals.add(val);
        return this;
    }

    public SQLBuilder decrease(String[] cols, Object[] vals) {
        if (cols == null || vals == null) return this;
        if (cols.length != vals.length) return this;
        int n = cols.length;
        for (int i = 0; i < n; i++) {
            decrease(cols[i], vals[i]);
        }
        return this;
    }

    /**
     * Note that `appendJoin` always translates the `ON` conditions as
     * <`secondary_table_alias`.`secondary_table_field`> <operator> <value>.
     * If <value> is to be filled in with associative field name, it could ONLY
     * be a primary table (i.e. m_table) field.
     */
    public SQLBuilder join(String table, String[] keys, String[] ops, Object[] vals) {
        if (table == null || keys == null || ops == null || vals == null) return this;
        if (keys.length != vals.length || ops.length != vals.length) return this;
        int length = keys.length;
        if (m_join == null) m_join = new HashMap<>();
        List<OnCondition> conditionList = new ArrayList<>();
        for (int i = 0; i < length; ++i) conditionList.add(new OnCondition(keys[i], ops[i], vals[i]));
        m_join.put(table, conditionList);
        return this;
    }

    public SQLBuilder where(String col, String op, Object val) {
        if (m_whereCols == null) m_whereCols = new LinkedList<>();
        if (m_whereOps == null) m_whereOps = new LinkedList<>();
        if (m_whereVals == null) m_whereVals = new LinkedList<>();
        m_whereCols.add(col);
        m_whereOps.add(op);
        m_whereVals.add(val);
        return this;
    }

    public SQLBuilder where(String[] cols, String[] ops, Object[] vals) {
        if (cols == null || ops == null || vals == null) return this;
        if (cols.length != ops.length || cols.length != vals.length) return this;
        int n = cols.length;
        for (int i = 0; i < n; i++) {
            where(cols[i], ops[i], vals[i]);
        }
        return this;
    }

    public SQLBuilder order(String col, String orientation) {
        if (m_orderBy == null) m_orderBy = new LinkedList<String>();
        m_orderBy.add(col);

        if (orientation == null) return this;
        if (m_orientations == null) m_orientations = new LinkedList<String>();
        m_orientations.add(orientation);
        return this;
    }

    public SQLBuilder order(String col) {
        order(col, null);
        return this;
    }

    public SQLBuilder order(List<String> cols, List<String> orientations) {
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

    public SQLBuilder order(String[] cols, String[] orientations) {
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

    public SQLBuilder order(List<String> cols) {
        return order(cols, null);
    }

    public SQLBuilder order(String[] cols) {
        return order(cols, null);
    }

    public SQLBuilder limit(int st, int ed) {
        if (m_limits == null) m_limits = new LinkedList<Integer>();
        m_limits.clear();
        m_limits.add(st);
        m_limits.add(ed);
        return this;
    }

    public SQLBuilder limit(int num) {
        if (m_limits == null) m_limits = new LinkedList<Integer>();
        m_limits.clear();
        m_limits.add(num);
        return this;
    }

    protected String appendFromTable(String query) {
        return query + " FROM `" + m_table + "`";
    }

    protected String appendIntoTable(String query) {
        return query + " INTO `" + m_table + "`";
    }

    protected String appendJoin(String query) {
        if (m_join == null) return query;
        int index = 0;
        for (Map.Entry<String, List<OnCondition>> entry : m_join.entrySet()) {
            String table = entry.getKey();
            List<OnCondition> conditionList = entry.getValue();
            int length = conditionList.size();
            String tableAlias = ALIAS_PREFIX + String.valueOf(index);
            query += (" JOIN `" + table + "` AS `" + tableAlias + "` ON ");
            for (int i = 0; i < length; ++i) {
                OnCondition condition = conditionList.get(i);
                query += ("`" + tableAlias + "`.`" + condition.key + "` " + condition.op + " ");
                if (condition.val instanceof List) {
                    List<?> castedVals = (List<?>) condition.val;
                    query += "(";
                    for (int j = 0; j < castedVals.size(); ++j) {
                        query += "?";
                        if (j < castedVals.size() - 1) query += ", ";
                    }
                    query += ")";
                } else if (condition.val instanceof PrimaryTableField) {
                    query += ("`" + m_table + "`.`" + ((PrimaryTableField) condition.val).name + "`"); // special case for joining
                } else {
                    query += "?";
                }
                if (i < length - 1) query += " AND ";
            }
            ++index;
        }
        return query;
    }

    protected String appendWhere(String query) {
        if (m_whereCols == null) return query;
        query += " WHERE ";
        for (int i = 0; i < m_whereCols.size(); i++) {
            String col = m_whereCols.get(i);
            String op = m_whereOps.get(i);
            Object val = m_whereVals.get(i);
            query += ("`" + col + "` " + op + " ");
            if (val instanceof List) {
                query += "(";
                List<?> castedVals = (List<?>) (m_whereVals.get(i));
                for (int j = 0; j < castedVals.size(); ++j) {
                    query += "?";
                    if (j < castedVals.size() - 1) query += ", ";
                }
                query += ")";
            } else query += "?";
            if (i < m_whereCols.size() - 1) {
                if (m_whereLink == null) query += " AND ";
                else query += (" " + m_whereLink + " ");
            }
        }
        return query;
    }

    protected String appendOrder(String query) {
        if (m_orderBy == null) return query;
        query += " ORDER BY ";
        for (int i = 0; i < m_orderBy.size(); i++) {
            String col = m_orderBy.get(i);
            query += ("`" + col + "`");
            if (m_orientations.size() > i) {
                String orientation = m_orientations.get(i);
                query += " " + orientation;
            }
            if (i < m_orderBy.size() - 1) query += ", ";
        }
        return query;
    }

    protected String appendLimit(String query) {
        if (m_limits == null) return query;
        query += " LIMIT ";
        for (int i = 0; i < m_limits.size(); i++) {
            query += "?";
            if (i < m_limits.size() - 1) query += ", ";
        }
        return query;
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
            query = appendJoin(query);
            query = appendWhere(query);
            query = appendOrder(query);
            query = appendLimit(query);

            int index = 1;
            statement = connection.prepareStatement(query);
            if (m_join != null) {
                for (Map.Entry<String, List<OnCondition>> entry : m_join.entrySet()) {
                    List<OnCondition> conditionList = entry.getValue();
                    for (OnCondition condition : conditionList) {
                        if (condition.val instanceof List) {
                            List<?> castedVals = (List<?>) condition.val;
                            for (Object castedVal : castedVals) statement.setObject(index++, castedVal);
                        } else if (condition.val instanceof PrimaryTableField) {
                            continue; // special case for joining
                        } else {
                            statement.setObject(index++, condition.val);
                        }
                    }
                }
            }
            if (m_whereVals != null) {
                for (Object val : m_whereVals) {
                    if (val instanceof List) {
                        List<?> castedVals = (List<?>) val;
                        for (Object castedVal : castedVals) {
                            statement.setObject(index++, castedVal);
                        }
                    } else statement.setObject(index++, val);
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
            if (m_ignore) query += " IGNORE";
            query = appendIntoTable(query);

            query += "(";
            for (int i = 0; i < m_insertCols.size(); i++) {
                query += ("`" + m_insertCols.get(i) + "`");
                if (i < m_insertCols.size() - 1) query += ", ";
            }
            query += ") ";

            query += " VALUES ";
            for (int i = 0; i < m_insertVals.size(); i++) {
                query += "(";
                for (int j = 0; j < m_insertVals.get(i).size(); ++j) {
                    query += "?";
                    if (j < m_insertVals.get(i).size() - 1) query += ", ";
                }
                query += ")";
                if (i < m_insertVals.size() - 1) query += ", ";
            }

            int index = 1;

            statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            if (m_insertVals != null) {
                for (List<Object> list : m_insertVals) {
                    for (Object val : list) {
                        statement.setObject(index++, val);
                    }
                }
            }

        } catch (Exception e) {

        }
        return statement;
    }

    public PreparedStatement toReplace(Connection connection) {
        if (connection == null) return null;
        PreparedStatement statement = null;
        try {
            String query = "REPLACE";
            query = appendIntoTable(query);

            query += "(";
            for (int i = 0; i < m_replaceCols.size(); i++) {
                query += ("`" + m_replaceCols.get(i) + "`");
                if (i < m_replaceCols.size() - 1) query += ", ";
            }
            query += ") ";

            query += " VALUES ";
            for (int i = 0; i < m_replaceVals.size(); i++) {
                query += "(";
                for (int j = 0; j < m_replaceVals.get(i).size(); ++j) {
                    query += "?";
                    if (j < m_replaceVals.get(i).size() - 1) query += ", ";
                }
                query += ")";
                if (i < m_replaceVals.size() - 1) query += ", ";
            }

            int index = 1;

            statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            if (m_replaceVals != null) {
                for (List<Object> list : m_replaceVals) {
                    for (Object val : list) {
                        statement.setObject(index++, val);
                    }
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
                    if (val instanceof List) {
                        List<?> castedVals = (List<?>) val;
                        for (Object castedVal : castedVals) {
                            statement.setObject(index++, castedVal);
                        }
                    } else statement.setObject(index++, val);
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
                    for (String col : m_updateCols) {
                            if (hasPrevFields) query += ", ";
                            hasPrevFields = true;
                            query += ("`" + col + "`" + "=?");
                    }
            }

            if (m_increaseCols != null) {
                    for (String col : m_increaseCols) {
                            if (hasPrevFields) query += ", ";
                            hasPrevFields = true;
                            query += ("`" + col + "`" + "=" + "`" + col + "`" + "+?");
                    }
            }

            if (m_decreaseCols != null) {
                    for (String col : m_decreaseCols) {
                            if (hasPrevFields) query += ", ";
                            hasPrevFields = true;
                            query += ("`" + col + "`" + "=" + "`" + col + "`" + "-?");
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
                    if (val instanceof List) {
                        List<?> castedVals = (List<?>) val;
                        for (Object castedVal : castedVals) {
                            statement.setObject(index++, castedVal);
                        }
                    } else statement.setObject(index++, val);
                }
            }

            if (m_limits != null) {
                for (Integer val : m_limits) {
                    statement.setInt(index++, val);
                }
            }
        } catch (Exception e) {
            Loggy.e(TAG, "select", e);
        }
        return statement;
    }

    public List<SimpleMap> execSelect() {
        List<SimpleMap> ret = new ArrayList<>();
        try {
            Connection connection = SQLHelper.getConnection();
            PreparedStatement statement = this.toSelect(connection);
            ResultSet rs = statement.executeQuery();
            if (rs != null) {
                ret = ResultSetUtil.convertToSimpleMap(rs);
                rs.close();
            }
            statement.close();
            SQLHelper.closeConnection(connection);
        } catch (Exception e) {
            Loggy.e(TAG, "select", e);
        }
        return ret;
    }

    public Long execInsert() {
        Long lastId = SQLHelper.INVALID;
        try {
            Connection connection = SQLHelper.getConnection();
            PreparedStatement statement = this.toInsert(connection);
            // the following command returns the last inserted row id for the auto incremented key
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            if (rs != null && rs.next()) {
                lastId = rs.getLong(1);
                rs.close();
            }
            statement.close();
            SQLHelper.closeConnection(connection);
        } catch (Exception e) {
            Loggy.e(TAG, "insert", e);
        }
        return lastId;
    }

    public Long execReplace() {
        Long lastId = SQLHelper.INVALID;
        try {
            Connection connection = SQLHelper.getConnection();
            PreparedStatement statement = this.toReplace(connection);
            // the following command returns the last inserted row id for the auto incremented key
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            if (rs != null && rs.next()) {
                lastId = rs.getLong(1);
                rs.close();
            }
            statement.close();
            SQLHelper.closeConnection(connection);
        } catch (Exception e) {
            Loggy.e(TAG, "insert", e);
        }
        return lastId;
    }

    public boolean execUpdate() {
        boolean bRet = false;
        try {
            Connection connection = SQLHelper.getConnection();
            PreparedStatement statement = this.toUpdate(connection);
            statement.executeUpdate();
            statement.close();
            SQLHelper.closeConnection(connection);
            bRet = true;
        } catch (Exception e) {
            Loggy.e(TAG, "update", e);
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
            Loggy.e(TAG, "delete", e);
        }
        return bRet;
    }
}