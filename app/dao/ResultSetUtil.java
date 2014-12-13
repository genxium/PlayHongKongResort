package dao;

import org.json.simple.JSONObject;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ResultSetUtil {

    /**
     * Convert a result set into a JSON Array
     *
     * @param resultSet
     * @return a JSONArray
     * @throws Exception
     */
    public static List<JSONObject> convertToJSON(ResultSet resultSet) throws Exception {

        List<JSONObject> jsonList = new ArrayList<JSONObject>();
        while (resultSet.next()) {
            int nCols = resultSet.getMetaData().getColumnCount();
            JSONObject obj = new JSONObject();
            for (int i = 0; i < nCols; i++) {
                obj.put(resultSet.getMetaData().getColumnLabel(i + 1),
                        resultSet.getObject(i + 1));
            }
            jsonList.add(obj);
        }
        return jsonList;
    }
};