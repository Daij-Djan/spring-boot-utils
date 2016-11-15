package info.pich.chatbottest.luis;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * A utility class that contains functions needed by LUIS SDK
 */
@Slf4j
public class LUISUtility {

    /**
     * Converts a JSONObject to a Map<String, Object>
     *
     * @param JSONobject A JSONObject that needs to be converted to a Map<String, Object>
     * @return A Map<String, Object> that contains the data of the JSONObject
     */
    public static Map<String, Object> JSONObjectToMap(JSONObject JSONobject) {
        Map<String, Object> map = new HashMap<>();

        @SuppressWarnings("unchecked")
        Iterator<String> keysItr = JSONobject.keys();
        while (keysItr.hasNext()) {
            try {
                String key = keysItr.next();
                Object value = JSONobject.get(key);
                if (value instanceof JSONObject) {
                    value = JSONObjectToMap((JSONObject) value);
                } else if (value instanceof JSONArray) {
                    value = JSONArrayToList((JSONArray) value);
                }
                map.put(key, value);
            } catch (JSONException e) {
                log.error("LUIS Exception: " + e, e);
                break;
            }
        }

        return map;
    }

    /**
     * Converts a JSONArray to a List<Object>
     *
     * @param JSONarray A JSONArray that needs to be converted to a List<Object>
     * @return A List<Object> that contains the data of the JSONArray
     */
    public static List<Object> JSONArrayToList(JSONArray JSONarray) {
        List<Object> list = new ArrayList<>();

        for (int i = 0; i < JSONarray.length(); i++) {
            try {
                Object value = JSONarray.get(i);
                if (value instanceof JSONObject) {
                    value = JSONObjectToMap((JSONObject) value);
                } else if (value instanceof JSONArray) {
                    value = JSONArrayToList((JSONArray) value);
                }
                list.add(value);
            } catch (JSONException e) {
                log.error("LUIS Exception: " + e, e);
                break;
            }
        }

        return list;
    }
}
