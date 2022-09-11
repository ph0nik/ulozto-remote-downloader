package service;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MyCookieJar implements CookieJar {

    private List<Cookie> cookieList;
    private HttpUrl url;

    public MyCookieJar() {
    }

    @NotNull
    @Override
    public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
        return (cookieList == null) ? new ArrayList<>() : cookieList;
    }

    @Override
    public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
        this.url = httpUrl;
        this.cookieList = list;
    }

    public HttpUrl getUrl() {
        return url;
    }

    public List<Cookie> getCookieList() {
        return cookieList;
    }
}
