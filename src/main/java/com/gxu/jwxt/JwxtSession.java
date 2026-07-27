package com.gxu.jwxt;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gxu.jwxt.exceptions.LoginException;
import com.gxu.jwxt.exceptions.NotLoggedInException;

import okhttp3.*;

import javax.crypto.Cipher;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/** 教务系统核心客户端 */
public class JwxtSession {

    private static final String DEFAULT_BASE_URL = "https://jwxt2018.gxu.edu.cn";
    private static final String LOGIN_PATH = "/jwglxt/xtgl/login_slogin.html";
    private static final String PUBLIC_KEY_PATH = "/jwglxt/xtgl/login_getPublicKey.html";
    private static final String INIT_MENU_PATH = "/jwglxt/xtgl/index_initMenu.html";

    private final String baseUrl;
    private final String username;
    private final String password;
    private final OkHttpClient httpClient;
    private final AtomicBoolean loggedIn = new AtomicBoolean(false);
    private String csrfToken;

    public JwxtSession(String username, String password) {
        this(username, password, DEFAULT_BASE_URL);
    }

    public JwxtSession(String username, String password, String baseUrl) {
        this.username = username;
        this.password = password;
        this.baseUrl = baseUrl != null ? baseUrl : DEFAULT_BASE_URL;

        CookieJar cookieJar = new CookieJar() {
            private final java.util.concurrent.CopyOnWriteArrayList<Cookie> cookies = new java.util.concurrent.CopyOnWriteArrayList<>();

            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> list) {
                for (Cookie c : list) {
                    cookies.removeIf(existing ->
                        existing.name().equals(c.name()) &&
                        (existing.domain() == null || existing.domain().equals(c.domain())));
                    cookies.add(c);
                }
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                return new java.util.ArrayList<>(cookies);
            }
        };

        this.httpClient = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .followRedirects(true)
                .followSslRedirects(true)
                .build();
    }

    // ========== HTTP ==========

    private String fullUrl(String path) {
        if (path.startsWith("http")) return path;
        return baseUrl + path;
    }

    private String referer() {
        return baseUrl + "/jwglxt/xtgl/index_initMenu.html";
    }

    /**
     * GET 请求
     */
    public String get(String path) throws IOException {
        Request req = new Request.Builder()
                .url(fullUrl(path))
                .header("Referer", referer())
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .build();
        try (Response resp = httpClient.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                throw new IOException("HTTP " + resp.code() + " for " + path);
            }
            return resp.body() != null ? resp.body().string() : "";
        }
    }

    /**
     * POST 请求 (form-urlencoded)
     */
    public String post(String path, java.util.Map<String, String> data) throws IOException {
        FormBody.Builder fb = new FormBody.Builder();
        if (data != null) {
            data.forEach(fb::add);
        }
        Request req = new Request.Builder()
                .url(fullUrl(path))
                .header("Referer", referer())
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(fb.build())
                .build();
        try (Response resp = httpClient.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                throw new IOException("HTTP " + resp.code() + " for " + path);
            }
            return resp.body() != null ? resp.body().string() : "";
        }
    }

    /**
     * POST 请求 (form-urlencoded)，返回完整 Response 对象。
     * 用于需要检查最终重定向 URL 的场景（如登录成功检测）。
     */
    private Response postWithResponse(String path, java.util.Map<String, String> data) throws IOException {
        FormBody.Builder fb = new FormBody.Builder();
        if (data != null) {
            data.forEach(fb::add);
        }
        Request req = new Request.Builder()
                .url(fullUrl(path))
                .header("Referer", referer())
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(fb.build())
                .build();
        Response resp = httpClient.newCall(req).execute();
        if (!resp.isSuccessful()) {
            resp.close();
            throw new IOException("HTTP " + resp.code() + " for " + path);
        }
        return resp;
    }

    // ========== RSA ==========

    /**
     * RSA 公钥加密密码
     */
    public String encryptPassword(String password) throws Exception {
        long ts = System.currentTimeMillis();
        String body = get(PUBLIC_KEY_PATH + "?time=" + ts);
        JsonObject key = JsonParser.parseString(body).getAsJsonObject();

        byte[] modulusBytes = Base64.getDecoder().decode(key.get("modulus").getAsString());
        byte[] exponentBytes = Base64.getDecoder().decode(key.get("exponent").getAsString());
        BigInteger modulus = new BigInteger(1, modulusBytes);
        BigInteger exponent = new BigInteger(1, exponentBytes);

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(spec);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        byte[] encrypted = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // ========== 认证 ==========

    /**
     * 登录教务系统
     */
    public void login() throws LoginException {
        if (loggedIn.get()) return;

        try {
            // 1. 获取 csrftoken
            String loginHtml = get(LOGIN_PATH);
            Document doc = Jsoup.parse(loginHtml);
            var csrfInput = doc.selectFirst("input[name=csrftoken]");
            this.csrfToken = csrfInput != null ? csrfInput.attr("value") : "";

            // 2. 加密密码
            String encryptedPwd = encryptPassword(password);

            // 3. 提交登录
            long ts = System.currentTimeMillis();
            try (Response loginResp = postWithResponse(
                LOGIN_PATH + "?time=" + ts,
                new java.util.LinkedHashMap<>() {{
                    put("csrftoken", csrfToken);
                    put("language", "zh_CN");
                    put("yhm", username);
                    put("mm", encryptedPwd);
                    put("ydType", "");
                }}
            )) {
                if (!checkLoginSuccess(loginResp)) {
                    raiseLoginError(loginResp);
                }
            }

            // 4. 初始化会话
            loggedIn.set(true);
            ts = System.currentTimeMillis();
            get(INIT_MENU_PATH + "?jsdm=xs&_t=" + ts + "&echarts=1");

        } catch (LoginException e) {
            throw e;
        } catch (Exception e) {
            throw new LoginException("登录异常: " + e.getMessage(), e);
        }
    }

    public void ensureLogin() {
        if (!loggedIn.get()) {
            throw new NotLoggedInException();
        }
    }

    public void logout() throws IOException {
        get("/jwglxt/xtgl/login_logoutAccount.html");
        loggedIn.set(false);
    }

    // ========== 状态 ==========

    public boolean isLoggedIn() { return loggedIn.get(); }
    public String getUsername() { return username; }
    public OkHttpClient getHttpClient() { return httpClient; }
    public String getBaseUrl() { return baseUrl; }

    // ========== 内部 ==========

    private boolean checkLoginSuccess(Response resp) {
        String url = resp.request().url().toString().toLowerCase();
        return !url.contains("login") || url.contains("index");
    }

    private void raiseLoginError(Response resp) throws IOException {
        String html = resp.body() != null ? resp.body().string() : "";
        Document doc = Jsoup.parse(html);
        String[] selectors = {"#tips", "#errorMsg", ".error", ".alert-danger", ".form-msg"};
        for (String sel : selectors) {
            var el = doc.selectFirst(sel);
            if (el != null && !el.text().isBlank()) {
                throw new LoginException(el.text().trim());
            }
        }
        var title = doc.selectFirst("title");
        if (title != null && (title.text().contains("错误") || title.text().contains("失败"))) {
            throw new LoginException(title.text().trim());
        }
        throw new LoginException("用户名或密码错误，或账号已被锁定");
    }
}
