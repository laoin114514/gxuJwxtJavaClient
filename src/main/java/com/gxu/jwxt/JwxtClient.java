package com.gxu.jwxt;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gxu.jwxt.exceptions.LoginException;
import com.gxu.jwxt.model.Semester;
import com.gxu.jwxt.module.ScheduleModule;
import com.gxu.jwxt.module.ProfileModule;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.JsonSyntaxException;

/** 教务系统客户端门面 */
public class JwxtClient {

    private static final Gson gson = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

    private final JwxtSession session;
    private final ScheduleModule schedule;
    private final ProfileModule profile;

    public JwxtClient(String username, String password) {
        this(username, password, null, RetryConfig.DEFAULT);
    }

    public JwxtClient(String username, String password, String baseUrl) {
        this(username, password, baseUrl, RetryConfig.DEFAULT);
    }

    public JwxtClient(String username, String password, String baseUrl, RetryConfig retryConfig) {
        this.session = new JwxtSession(username, password, baseUrl, retryConfig);
        this.schedule = new ScheduleModule(this.session);
        this.profile = new ProfileModule(this.session);
    }

    // ========== 认证 ==========

    public void login() throws LoginException {
        session.login();
    }

    public void logout() throws IOException {
        session.logout();
    }

    public boolean isLoggedIn() {
        return session.isLoggedIn();
    }

    // ========== 业务模块 ==========

    public ScheduleModule schedule() {
        return schedule;
    }

    public ProfileModule profile() {
        return profile;
    }

    // ========== 通用查询 ==========

    /**
     * 通用 POST 查询，返回 Map
     */
    public Map<String, Object> query(String path, Map<String, String> data) throws IOException {
        session.ensureLogin();
        String body = session.post(path, data);
        try {
            return gson.fromJson(body, MAP_TYPE);
        } catch (JsonSyntaxException e) {
            return Map.of("_html", body);
        }
    }

    /**
     * 通用 POST 查询，返回指定类型
     */
    public <T> T query(String path, Map<String, String> data, Class<T> type) throws IOException {
        session.ensureLogin();
        String body = session.post(path, data);
        return gson.fromJson(body, type);
    }

    /**
     * 通用 GET 页面
     */
    public String queryPage(String path) throws IOException {
        session.ensureLogin();
        return session.get(path);
    }

    // ========== 辅助 ==========

    public static Semester currentSemester() {
        return Semester.current();
    }

    public RetryConfig getRetryConfig() {
        return session.getRetryConfig();
    }

    public String getUsername() {
        return session.getUsername();
    }

    @Override
    public String toString() {
        return "<JwxtClient username=" + getUsername() + " logged_in=" + isLoggedIn() + ">";
    }
}
