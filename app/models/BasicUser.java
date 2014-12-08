package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.SQLCommander;
import org.json.simple.JSONObject;
import utilities.Converter;
import utilities.Loggy;

public class BasicUser extends AbstractModel {

    public static String TAG = BasicUser.class.getName();

    public static String EMAIL = "email";
    public static String NAME = "name";
    public static String AVATAR = "avatar";

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
        super(userJson);
        if (userJson.containsKey(NAME)) m_name = (String) userJson.get(NAME);
        if (userJson.containsKey(EMAIL)) m_email = (String) userJson.get(EMAIL);
        if (userJson.containsKey(AVATAR)) m_avatar = Converter.toInteger(userJson.get(AVATAR));
    }

    public ObjectNode toObjectNode(Long viewerId) {
        ObjectNode ret = super.toObjectNode();
        try {
            if (viewerId != null && viewerId.equals(m_id)) ret.put(EMAIL, m_email);
            ret.put(NAME, m_name);
            Image image = SQLCommander.queryImage(m_avatar);
            if (image != null)  ret.put(AVATAR, image.getUrl());
        } catch (Exception e) {
            Loggy.e(TAG, "toObjectNode", e);
        }
        return ret;
    }
}
