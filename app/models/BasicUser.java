package models;

import controllers.SQLCommander;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.simple.JSONObject;
import play.libs.Json;
import utilities.DataUtils;
import utilities.Logger;

public class BasicUser {

    public static String TAG = BasicUser.class.getName();

    public static String ID = "id";
    public static String EMAIL = "email";
    public static String NAME = "name";
    public static String AVATAR = "avatar";

    protected int m_id = 0;

    public int getId() {
        return m_id;
    }

    public void setId(int id) {
        m_id = id;
    }

    protected String m_email = null;

    public String getEmail() {
        return m_email;
    }

    public void setEmail(String email) {
        m_email = email;
    }

    protected String m_name = null;

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    protected int m_avatar = 0;

    public int getAvatar() {
        return m_avatar;
    }

    public void setAvatar(int avatar) {
        m_avatar = avatar;
    }

    public BasicUser(String email, String name) {
        m_email = email;
        m_name = name;
    }

    public BasicUser(JSONObject userJson) {
        if (userJson.containsKey(ID)) m_id = (Integer) userJson.get(ID);
        if (userJson.containsKey(NAME)) m_name = (String) userJson.get(NAME);
        if (userJson.containsKey(EMAIL)) m_email = (String) userJson.get(EMAIL);
        if (userJson.containsKey(AVATAR)) m_avatar = (Integer) userJson.get(AVATAR);
    }

    public ObjectNode toObjectNode(Integer viewerId) {
        ObjectNode ret = Json.newObject();
        try {
            ret.put(ID, String.valueOf(m_id));
            ret.put(EMAIL, m_email);
            ret.put(NAME, m_name);
            Image image = SQLCommander.queryImage(m_avatar);
            if (image != null) {
                ret.put(AVATAR, image.getImageURL());
            }
        } catch (Exception e) {
            Logger.e(TAG, "toObjectNode", e);
        }
        return ret;
    }
}
