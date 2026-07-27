package com.gxu.jwxt.module;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gxu.jwxt.JwxtSession;
import com.gxu.jwxt.model.PageQuery;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

/** 课表查询模块 */
public class ScheduleModule {

    private static final String GNMKDM = "N2151";
    private static final Gson gson = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

    private final JwxtSession session;

    public ScheduleModule(JwxtSession session) {
        this.session = session;
    }

    /**
     * 课表页面 HTML（含本学期课表数据）
     */
    public String page() throws IOException {
        session.ensureLogin();
        long ts = System.currentTimeMillis();
        return session.get(
            "/jwglxt/kbcx/xskbcx_cxXskbcxIndex.html?gnmkdm=" + GNMKDM + "&layout=default&time=" + ts
        );
    }

    /**
     * 个人课表数据
     */
    public Map<String, Object> personal(String year, String term) throws IOException {
        session.ensureLogin();
        PageQuery q = new PageQuery();
        Map<String, String> data = q.toMap(Map.of("xnm", year, "xqm", term));
        String body = session.post(
            "/jwglxt/kbcx/xskbcx_cxXsgrkb.html?gnmkdm=" + GNMKDM,
            data
        );
        return gson.fromJson(body, MAP_TYPE);
    }

    /**
     * 教师课表
     */
    public Map<String, Object> teacher(String year, String term, String name) throws IOException {
        session.ensureLogin();
        PageQuery q = new PageQuery();
        Map<String, String> data = q.toMap(Map.of(
            "xnm", year,
            "xqm", term,
            "jsmc", name != null ? name : ""
        ));
        String body = session.post(
            "/jwglxt/kbcx/jskbcx_cxJsKb.html?gnmkdm=" + GNMKDM,
            data
        );
        return gson.fromJson(body, MAP_TYPE);
    }

    /**
     * 班级课表查询/打印页面
     */
    public String classSchedulePage() throws IOException {
        session.ensureLogin();
        return session.get("/jwglxt/kbdy/bjkbdy_cxBjkbdyIndex.html?gnmkdm=N214505");
    }

    /**
     * 课表/学分确认页面
     */
    public String creditConfirm() throws IOException {
        session.ensureLogin();
        long ts = System.currentTimeMillis();
        return session.get("/jwglxt/kbcx/xskbqr_cxXskbqrIndex.html?gnmkdm=N2158&time=" + ts);
    }
}
