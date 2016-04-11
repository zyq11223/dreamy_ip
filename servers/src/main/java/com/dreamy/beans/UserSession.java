package com.dreamy.beans;

import com.dreamy.utils.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by wangyongxing on 16/4/1.
 */
public class UserSession  implements CanonicalSession {

    private static final long serialVersionUID = -6308267983694496853L;

    public static final String ATTRIBUTES_CAPTCHA = "captcha";

    public static final UserSession GUEST = new UserSession();

    private int userId;

    private String userKey;

    private String username;




    private Map<String, Serializable> attributes = new HashMap<>();


    public void addAttribute(String key, Serializable value) {
        if (StringUtils.isNotEmpty(key) && value != null) {
            attributes.put(key, value);
        }
    }

    public Serializable getAttribute(String key) {
        if (StringUtils.isNotEmpty(key)) {
            return attributes.get(key);
        }
        return null;
    }



    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }



    public void remoteAttribute(String key) {
        attributes.remove(key);
    }

    public boolean captchaed(String captchaCheck) {
        String captchaStore = (String) getAttribute(ATTRIBUTES_CAPTCHA);
        if (StringUtils.isNotEmpty(captchaCheck, captchaStore)) {
            captchaCheck = captchaCheck.toLowerCase();
            if(captchaStore.equals(captchaCheck)) {
                remoteAttribute(ATTRIBUTES_CAPTCHA);
                return true;
            }
        }
        return false;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    @Override
    public boolean isLogin() {
        return userId > 0;
    }
}
