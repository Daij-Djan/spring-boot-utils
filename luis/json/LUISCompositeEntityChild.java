package info.pich.chatbottest.luis;

import org.json.JSONObject;

public class LUISCompositeEntityChild {
    private String type;
    private String value;

    /**
     * Constructs a LUIS Composite Entity Child from a JSON object
     *
     * @param JSONcompositeEntityChild a JSONObject containing the composite entity child data
     */
    public LUISCompositeEntityChild(JSONObject JSONcompositeEntityChild) {
        type = JSONcompositeEntityChild.optString("type");
        value = JSONcompositeEntityChild.optString("value");
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
