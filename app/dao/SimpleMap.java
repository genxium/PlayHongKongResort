package dao;

import com.fasterxml.jackson.databind.JsonNode;
import utilities.Converter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SimpleMap {
        Map<String, Object> container;

        public SimpleMap() {
                container = new HashMap<>();
        }
        public void put(final String key, final Object val) {
                container.put(key, val);
        }
        public String getStr(final String key) {
                if (!container.containsKey(key)) return null;
                return (String) container.get(key);
        }
        public Integer getInt(final String key) {
                if (!container.containsKey(key)) return null;
                return Converter.toInteger(container.get(key));
        }
        public Long getLong(final String key) {
                if (!container.containsKey(key)) return null;
                return Converter.toLong(container.get(key));
        }

        public static SimpleMap fromJsonNodeTopLevel(final JsonNode node) {
                SimpleMap ret = new SimpleMap();
                Iterator<String> itName = node.fieldNames();
                while (itName.hasNext()) {
                        String name = itName.next();
                        JsonNode element = node.get(name);
                        if (element.isLong()) ret.put(name, element.asLong());
                        if (element.isInt()) ret.put(name, element.asInt());
                        if (element.isTextual()) ret.put(name, element.asText());
                }
                return ret;
        }
}
