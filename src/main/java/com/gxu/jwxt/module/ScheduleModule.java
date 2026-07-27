package com.gxu.jwxt.module;

import com.google.gson.Gson;
import com.gxu.jwxt.JwxtSession;
import com.gxu.jwxt.model.ClassScheduleResponse;
import com.gxu.jwxt.model.PageQuery;
import com.gxu.jwxt.model.ScheduleResponse;
import com.gxu.jwxt.model.TeacherScheduleResponse;
import com.gxu.jwxt.model.Term;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** 课表查询模块 */
public class ScheduleModule {

    private static final String GNMKDM = "N2151";
    private static final Gson gson = new Gson();

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
    public ScheduleResponse personal(String year, Term term) throws IOException {
        return personal(year, term.code());
    }

    /** @deprecated 使用 {@link #personal(String, Term)} */
    @Deprecated
    public ScheduleResponse personal(String year, String term) throws IOException {
        session.ensureLogin();
        PageQuery q = new PageQuery();
        Map<String, String> data = q.toMap(Map.of("xnm", year, "xqm", term));
        String body = session.post(
            "/jwglxt/kbcx/xskbcx_cxXsgrkb.html?gnmkdm=" + GNMKDM,
            data
        );
        return gson.fromJson(body, ScheduleResponse.class);
    }

    /**
     * 教师课表
     */
    public TeacherScheduleResponse teacher(String year, Term term, String name) throws IOException {
        return teacher(year, term.code(), name);
    }

    /** @deprecated 使用 {@link #teacher(String, Term, String)} */
    @Deprecated
    public TeacherScheduleResponse teacher(String year, String term, String name) throws IOException {
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
        return gson.fromJson(body, TeacherScheduleResponse.class);
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

    // ========== 班级课表 ==========

    private static final String CLASS_SCHEDULE_GNMKDM = "N214505";

    /**
     * 班级课表详情（结构化数据）。
     *
     * @param year      学年，如 {@code "2025"}
     * @param term      学期，{@link Term#AUTUMN} 或 {@link Term#SPRING}
     * @param classId   班级 ID，如 {@code "24071101"}
     * @param gradeCode 年级代码，如 {@code "2024"}
     * @param majorCode 专业号 ID，如 {@code "0711"}
     * @return 班级课表数据
     */
    public ClassScheduleResponse classDetail(String year, Term term, String classId,
                                              String gradeCode, String majorCode) throws IOException {
        return classDetail(year, term.code(), classId, gradeCode, majorCode);
    }

    /**
     * 班级课表详情（结构化数据，使用学期编码）。
     *
     * @deprecated 使用 {@link #classDetail(String, Term, String, String, String)}
     */
    @Deprecated
    public ClassScheduleResponse classDetail(String year, String termCode, String classId,
                                              String gradeCode, String majorCode) throws IOException {
        session.ensureLogin();
        PageQuery q = new PageQuery();
        Map<String, String> data = q.toMap(new LinkedHashMap<>() {{
            put("xnm", year);
            put("xqm", termCode);
            put("bh_id", classId);
            put("njdm_id", gradeCode);
            put("zyh_id", majorCode);
            put("xqh_id", "1");
            put("tjkbzdm", "1");
            put("tjkbzxsdm", "0");
            put("kzlx", "ck");
            put("sfcxxqh", "1");
        }});
        String body = session.post(
            "/jwglxt/kbdy/bjkbdy_cxBjKb.html?gnmkdm=" + CLASS_SCHEDULE_GNMKDM,
            data
        );
        return gson.fromJson(body, ClassScheduleResponse.class);
    }
}
