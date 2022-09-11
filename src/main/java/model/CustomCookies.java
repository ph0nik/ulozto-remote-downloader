package model;

import okhttp3.Cookie;

import java.util.List;
import java.util.Map;

public class CustomCookies {

    private Map<String, String> cookies;
    private Map<String, List<Cookie>> cookieStore;

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public Map<String, List<Cookie>> getCookieStore() {
        return cookieStore;
    }

    public void setCookieStore(Map<String, List<Cookie>> cookieStore) {
        this.cookieStore = cookieStore;
    }
}
