package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.ExtraCommander;
import org.json.simple.JSONObject;
import utilities.Converter;
import utilities.Loggy;

import java.util.regex.Pattern;

public class BasicPlayer extends AbstractModel {

    public static String TAG = BasicPlayer.class.getName();

    public static String EMAIL = "email";
    public static String NAME = "name";
    public static String AVATAR = "avatar";

    public static final Pattern EMAIL_PATTERN = Pattern.compile("^((([a-z]|\\d|[!#\\$%&'\\*\\+\\-\\/=\\?\\^_`{\\|}~]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])+(\\.([a-z]|\\d|[!#\\$%&'\\*\\+\\-\\/=\\?\\^_`{\\|}~]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])+)*)|((\\x22)((((\\x20|\\x09)*(\\x0d\\x0a))?(\\x20|\\x09)+)?(([\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x7f]|\\x21|[\\x23-\\x5b]|[\\x5d-\\x7e]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])|(\\\\([\\x01-\\x09\\x0b\\x0c\\x0d-\\x7f]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]))))*(((\\x20|\\x09)*(\\x0d\\x0a))?(\\x20|\\x09)+)?(\\x22)))@((([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])|(([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])([a-z]|\\d|-|\\.|_|~|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])*([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])))\\.)+(([a-z]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])|(([a-z]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])([a-z]|\\d|-|\\.|_|~|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])*([a-z]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])))\\.?$", Pattern.UNICODE_CHARACTER_CLASS); // referred to https://jqueryui.com/resources/demos/dialog/modal-form.html;
    public static final Pattern NAME_PATTERN = Pattern.compile("^[0-9a-zA-Z_]{6,32}$", Pattern.UNICODE_CHARACTER_CLASS);

    public static final String[] QUERY_FILEDS = {ID, EMAIL, NAME, AVATAR};

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

    protected Long m_avatar = 0L;

    public long getAvatar() {
        return m_avatar;
    }

    public void setAvatar(long avatar) {
        m_avatar = avatar;
    }

    public BasicPlayer(final String email, final String name) {
        m_email = email;
        m_name = name;
    }

    public BasicPlayer(final JSONObject json) {
        super(json);
        if (json.containsKey(NAME)) m_name = (String) json.get(NAME);
        if (json.containsKey(EMAIL)) m_email = (String) json.get(EMAIL);
        if (json.containsKey(AVATAR)) m_avatar = Converter.toLong(json.get(AVATAR));
    }

    public ObjectNode toObjectNode(Long viewerId) {
        ObjectNode ret = super.toObjectNode();
        try {
            ret.put(ID, m_id);
            ret.put(NAME, m_name);
            if (viewerId != null && viewerId.equals(m_id)) ret.put(EMAIL, m_email);
            Image image = ExtraCommander.queryImage(m_avatar);
            if (image != null) ret.put(AVATAR, image.getUrl());
        } catch (Exception e) {
            Loggy.e(TAG, "toObjectNode", e);
        }
        return ret;
    }
}