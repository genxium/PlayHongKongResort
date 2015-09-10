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

    protected String table = null;
    protected List<String> selectCols = null;

    protected List<String> insertCols = null;
    protected List<List<Object>> insertVals = null;

    protected List<String> replaceCols = null;
    protected List<List<Object>> replaceVals = null;

    protected List<String> updateCols = null;
    protected List<Object> updateVals = null;

    protected List<String> increaseCols = null;
    protected List<Object> increaseVals = null;
    protected List<String> decreaseCols = null;
    protected List<Object> decreaseVals = null;

    protected Map<String, List<OnCondition>> join = null;

    protected List<String> whereCols = null;
    protected List<String> whereOps = null;
    protected List<Object> whereVals = null;
    protected String whereLink = null;

    protected List<String> orderBy = null;
    protected List<String> orientations = null;

    protected List<Integer> limits = null;
    protected boolean ignore = false;

    public SQLBuilder() {

    }

    public SQLBuilder from(final String data) {
        table = data;
        return this;
    }

    public SQLBuilder into(final String data) {
        table = data;
        return this;
    }

    public SQLBuilder update(final String data) {
        table = data;
        return this;
    }

    public SQLBuilder select(final String col) {
        if (selectCols == null) selectCols = new LinkedList<>();
        selectCols.add(col);
        return this;
    }

    public SQLBuilder select(final List<String> cols) {
        for (String col : cols) select(col);
        return this;
    }

    public SQLBuilder select(final String[] cols) {
        for (String col : cols) select(col);
        return this;
    }

    public SQLBuilder insert(final String[] cols, final Object[] vals) {
        if (cols == null || vals == null || cols.length != vals.length) return this;
        if (insertCols == null) {
            insertCols = new ArrayList<>(); // lazy init
            Collections.addAll(insertCols, cols);
        }
        if (insertVals == null) insertVals = new ArrayList<>(); // lazy init
        List<Object> tmp = new ArrayList<>();
        Collections.addAll(tmp, vals);
        insertVals.add(tmp);
        return this;
    }

    public SQLBuilder replace(final String[] cols, final Object[] vals) {
        if (cols == null || vals == null || cols.length != vals.length) return this;
        if (replaceCols == null) {
            replaceCols = new ArrayList<>(); // lazy init
            Collections.addAll(replaceCols, cols);
        }
        if (replaceVals == null) replaceVals = new ArrayList<>(); // lazy init
        List<Object> tmp = new ArrayList<>();
        Collections.addAll(tmp, vals);
        replaceVals.add(tmp);
        return this;
    }

    public SQLBuilder ignore(final boolean val) {
        ignore = val;
        return this;
    }

    public SQLBuilder set(final String col, final Object val) {
        if (updateCols == null) updateCols = new LinkedList<>();
        if (updateVals == null) updateVals = new LinkedList<>();
        updateCols.add(col);
        updateVals.add(val);
        return this;
    }

    public SQLBuilder set(final List<String> cols, final List<Object> vals) {
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

    public SQLBuilder set(final String[] cols, final Object[] vals) {
        if (cols == null || vals == null) return this;
        if (cols.length != vals.length) return this;
        int n = cols.length;
        for (int i = 0; i < n; i++) {
            set(cols[i], vals[i]);
        }
        return this;
    }

    public SQLBuilder increase(final String col, final Object val) {
        if (increaseCols == null) increaseCols = new LinkedList<>();
        if (increaseVals == null) increaseVals = new LinkedList<>();
        increaseCols.add(col);
        increaseVals.add(val);
        return this;
    }

    public SQLBuilder increase(final String[] cols, final Object[] vals) {
        if (cols == null || vals == null) return this;
        if (cols.length != vals.length) return this;
        int n = cols.length;
        for (int i = 0; i < n; i++) {
            increase(cols[i], vals[i]);
        }
        return this;
    }

    public SQLBuilder decrease(final String col, final Object val) {
        if (decreaseCols == null) decreaseCols = new LinkedList<>();
        if (decreaseVals == null) decreaseVals = new LinkedList<>();
        decreaseCols.add(col);
        decreaseVals.add(val);
        return this;
    }

    public SQLBuilder decrease(final String[] cols, final Object[] vals) {
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
     * be a primary table (i.e. table) field.
     */
    public SQLBuilder join(final String table, final String[] keys, final String[] ops, final Object[] vals) {
        if (table == null || keys == null || ops == null || vals == null) return this;
        if (keys.length != vals.length || ops.length != vals.length) return this;
        int length = keys.length;
        if (join == null) join = new HashMap<>();
        List<OnCondition> conditionList = new ArrayList<>();
        for (int i = 0; i < length; ++i) conditionList.add(new OnCondition(keys[i], ops[i], vals[i]));
        join.put(table, conditionList);
        return this;
    }

    public SQLBuilder where(final String col, final String op, final Object val) {
        if (whereCols == null) whereCols = new LinkedList<>();
        if (whereOps == null) whereOps = new LinkedList<>();
        if (whereVals == null) whereVals = new LinkedList<>();
        whereCols.add(col);
        whereOps.add(op);
        whereVals.add(val);
        return this;
    }

    public SQLBuilder where(final String[] cols, final String[] ops, final Object[] vals) {
        if (cols == null || ops == null || vals == null) return this;
        if (cols.length != ops.length || cols.length != vals.length) return this;
        int n = cols.length;
        for (int i = 0; i < n; i++) {
            where(cols[i], ops[i], vals[i]);
        }
        return this;
    }

    public SQLBuilder order(final String col, final String orientation) {
        if (orderBy == null) orderBy = new LinkedList<>();
        orderBy.add(col);

        if (orientation == null) return this;
        if (orientations == null) orientations = new LinkedList<>();
        orientations.add(orientation);
        return this;
    }

    public SQLBuilder order(final String col) {
        order(col, null);
        return this;
    }

    public SQLBuilder order(final List<String> cols, final List<String> orientations) {
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

    public SQLBuilder order(final String[] cols, final String[] orientations) {
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

    public SQLBuilder order(final List<String> cols) {
        return order(cols, null);
    }

    public SQLBuilder order(final String[] cols) {
        return order(cols, null);
    }

    public SQLBuilder limit(final int st, final int count) {
        if (limits == null) limits = new LinkedList<>();
        limits.clear();
        limits.add(st);
        limits.add(count);
        return this;
    }

    public SQLBuilder limit(final int num) {
        if (limits == null) limits = new LinkedList<>();
        limits.clear();
        limits.add(num);
        return this;
    }

    protected String appendFromTable(String query) {
        return query + " FROM `" + table + "`";
    }

    protected String appendIntoTable(String query) {
        return query + " INTO `" + table + "`";
    }

    protected String appendJoin(String query) {
        if (join == null) return query;
        int index = 0;
        for (Map.Entry<String, List<OnCondition>> entry : join.entrySet()) {
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
                    query += ("`" + table + "`.`" + ((PrimaryTableField) condition.val).name + "`"); // special case for joining
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
        if (whereCols == null) return query;
        query += " WHERE ";
        for (int i = 0; i < whereCols.size(); i++) {
            String col = whereCols.get(i);
            String op = whereOps.get(i);
            Object val = whereVals.get(i);
            query += ("`" + col + "` " + op + " ");
            if (val instanceof List) {
                query += "(";
                List<?> castedVals = (List<?>) (whereVals.get(i));
                for (int j = 0; j < castedVals.size(); ++j) {
                    query += "?";
                    if (j < castedVals.size() - 1) query += ", ";
                }
                query += ")";
            } else query += "?";
            if (i < whereCols.size() - 1) {
                if (whereLink == null) query += " AND ";
                else query += (" " + whereLink + " ");
            }
        }
        return query;
    }

    protected String appendOrder(String query) {
        if (orderBy == null) return query;
        query += " ORDER BY ";
        for (int i = 0; i < orderBy.size(); i++) {
            String col = orderBy.get(i);
            query += ("`" + col + "`");
            if (orientations.size() > i) {
                String orientation = orientations.get(i);
                query += " " + orientation;
            }
            if (i < orderBy.size() - 1) query += ", ";
        }
        return query;
    }

    protected String appendLimit(String query) {
        if (limits == null) return query;
        query += " LIMIT ";
        for (int i = 0; i < limits.size(); i++) {
            query += "?";
            if (i < limits.size() - 1) query += ", ";
        }
        return query;
    }

    public PreparedStatement toSelect(final Connection connection) {

        if (connection == null) return null;
        PreparedStatement statement = null;
        try {
            String query = "SELECT ";

            for (int i = 0; i < selectCols.size(); i++) {
                query += ("`" + selectCols.get(i) + "`");
                if (i < selectCols.size() - 1) query += ", ";
            }

            query = appendFromTable(query);
            query = appendJoin(query);
            query = appendWhere(query);
            query = appendOrder(query);
            query = appendLimit(query);

            int index = 1;
            statement = connection.prepareStatement(query);
            if (join != null) {
                for (Map.Entry<String, List<OnCondition>> entry : join.entrySet()) {
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
            if (whereVals != null) {
                for (Object val : whereVals) {
                    if (val instanceof List) {
                        List<?> castedVals = (List<?>) val;
                        for (Object castedVal : castedVals) {
                            statement.setObject(index++, castedVal);
                        }
                    } else statement.setObject(index++, val);
                }
            }
            if (limits != null) {
                for (Integer limit : limits) {
                    statement.setInt(index++, limit);
                }
            }

        } catch (Exception e) {

        }
        return statement;
    }

    public PreparedStatement toInsert(final Connection connection) {
        if (connection == null) return null;
        PreparedStatement statement = null;
        try {
            String query = "INSERT";
            if (ignore) query += " IGNORE";
            query = appendIntoTable(query);

            query += "(";
            for (int i = 0; i < insertCols.size(); i++) {
                query += ("`" + insertCols.get(i) + "`");
                if (i < insertCols.size() - 1) query += ", ";
            }
            query += ") ";

            query += " VALUES ";
            for (int i = 0; i < insertVals.size(); i++) {
                query += "(";
                for (int j = 0; j < insertVals.get(i).size(); ++j) {
                    query += "?";
                    if (j < insertVals.get(i).size() - 1) query += ", ";
                }
                query += ")";
                if (i < insertVals.size() - 1) query += ", ";
            }

            int index = 1;

            statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            if (insertVals != null) {
                for (List<Object> list : insertVals) {
                    for (Object val : list) {
                        statement.setObject(index++, val);
                    }
                }
            }

        } catch (Exception e) {

        }
        return statement;
    }

    public PreparedStatement toReplace(final Connection connection) {
        if (connection == null) return null;
        PreparedStatement statement = null;
        try {
            String query = "REPLACE";
            query = appendIntoTable(query);

            query += "(";
            for (int i = 0; i < replaceCols.size(); i++) {
                query += ("`" + replaceCols.get(i) + "`");
                if (i < replaceCols.size() - 1) query += ", ";
            }
            query += ") ";

            query += " VALUES ";
            for (int i = 0; i < replaceVals.size(); i++) {
                query += "(";
                for (int j = 0; j < replaceVals.get(i).size(); ++j) {
                    query += "?";
                    if (j < replaceVals.get(i).size() - 1) query += ", ";
                }
                query += ")";
                if (i < replaceVals.size() - 1) query += ", ";
            }

            int index = 1;

            statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            if (replaceVals != null) {
                for (List<Object> list : replaceVals) {
                    for (Object val : list) {
                        statement.setObject(index++, val);
                    }
                }
            }

        } catch (Exception e) {

        }
        return statement;
    }

    public PreparedStatement toDelete(final Connection connection) {
        if (connection == null) return null;
        PreparedStatement statement = null;
        try {
            String query = "DELETE";
            query = appendFromTable(query);
            query = appendWhere(query);
            query = appendLimit(query);

            int index = 1;
            statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            if (whereVals != null) {
                for (Object val : whereVals) {
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

    public PreparedStatement toUpdate(final Connection connection) {
        if (connection == null) return null;
        PreparedStatement statement = null;
        try {
            String query = "UPDATE " + table;
            query += " SET ";

            boolean hasPrevFields = false;

            if (updateCols != null) {
                    for (String col : updateCols) {
                            if (hasPrevFields) query += ", ";
                            hasPrevFields = true;
                            query += ("`" + col + "`" + "=?");
                    }
            }

            if (increaseCols != null) {
                    for (String col : increaseCols) {
                            if (hasPrevFields) query += ", ";
                            hasPrevFields = true;
                            query += ("`" + col + "`" + "=" + "`" + col + "`" + "+?");
                    }
            }

            if (decreaseCols != null) {
                    for (String col : decreaseCols) {
                            if (hasPrevFields) query += ", ";
                            hasPrevFields = true;
                            query += ("`" + col + "`" + "=" + "`" + col + "`" + "-?");
                    }
            }

            query = appendWhere(query);
            query = appendLimit(query);

            int index = 1;
            statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            if (updateVals != null) {
                for (Object val : updateVals) {
                    statement.setObject(index++, val);
                }
            }

            if (increaseVals != null) {
                for (Object val : increaseVals) {
                    statement.setObject(index++, val);
                }
            }

            if (decreaseVals != null) {
                for (Object val : decreaseVals) {
                    statement.setObject(index++, val);
                }
            }

            if (whereVals != null) {
                for (Object val : whereVals) {
                    if (val instanceof List) {
                        List<?> castedVals = (List<?>) val;
                        for (Object castedVal : castedVals) {
                            statement.setObject(index++, castedVal);
                        }
                    } else statement.setObject(index++, val);
                }
            }

            if (limits != null) {
                for (Integer val : limits) {
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
