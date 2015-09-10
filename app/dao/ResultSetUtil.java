package dao;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ResultSetUtil {
    public static List<SimpleMap> convertToSimpleMap(ResultSet resultSet) throws Exception {

        List<SimpleMap> list = new ArrayList<>();
        while (resultSet.next()) {
            int nCols = resultSet.getMetaData().getColumnCount();
            SimpleMap obj = new SimpleMap();
            for (int i = 0; i < nCols; i++) {
                obj.put(resultSet.getMetaData().getColumnLabel(i + 1),
                        resultSet.getObject(i + 1));
            }
            list.add(obj);
        }
        return list;
    }
};
