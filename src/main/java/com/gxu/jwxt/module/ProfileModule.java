package com.gxu.jwxt.module;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gxu.jwxt.JwxtSession;
import com.gxu.jwxt.model.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/** 学生个人信息查询模块 */
public class ProfileModule {

    private static final String GNMKDM = "N100801";
    private static final Gson gson = new Gson();
    private static final Type LIST_EXAM = new TypeToken<List<ExamEntry>>() {}.getType();
    private static final Type LIST_COURSE_SEL = new TypeToken<List<CourseSelectionEntry>>() {}.getType();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

    private final JwxtSession session;
    private String xhId; // 学号，首次查询时自动获取

    public ProfileModule(JwxtSession session) {
        this.session = session;
    }

    /**
     * 个人信息页面 HTML
     */
    public String page() throws IOException {
        session.ensureLogin();
        long ts = System.currentTimeMillis();
        return session.get(
            "/jwglxt/xsxxxggl/xsgrxxwh_cxXsgrxx.html?gnmkdm=" + GNMKDM + "&layout=default&time=" + ts
        );
    }

    // ========== 学生基本信息 ==========

    /**
     * 获取当前学生基本信息
     */
    public StudentProfile profile() throws IOException {
        session.ensureLogin();
        String body = session.post(
            "/jwglxt/xsxxxggl/xsxxwh_cxCkDgxsxx.html?gnmkdm=" + GNMKDM,
            Map.of()
        );
        StudentProfile p = gson.fromJson(body, StudentProfile.class);
        if (p.getStudentId() != null) {
            this.xhId = p.getStudentId();
        }
        return p;
    }

    // ========== 班级信息 ==========

    /**
     * 获取班级列表
     */
    public List<Map<String, String>> classList() throws IOException {
        session.ensureLogin();
        String xh = ensureXhId();
        String body = session.post(
            "/jwglxt/xsxxxggl/xsxxwh_cxBjxxList.html?gnmkdm=" + GNMKDM,
            Map.of("xh_id", xh)
        );
        return gson.fromJson(body, new TypeToken<List<Map<String, String>>>() {}.getType());
    }

    // ========== 等级考试 ==========

    /**
     * 等级考试成绩（CET-4/6 等）
     */
    public List<ExamEntry> exams() throws IOException {
        session.ensureLogin();
        String xh = ensureXhId();
        PageQuery q = new PageQuery();
        Map<String, String> params = q.toMap(Map.of("xh_id", xh));
        String body = session.post(
            "/jwglxt/xsxxxggl/xsxxwh_cxDjksxx.html?gnmkdm=" + GNMKDM,
            params
        );
        Map<String, Object> raw = gson.fromJson(body, MAP_TYPE);
        Object items = raw.get("items");
        if (items == null) return List.of();
        return gson.fromJson(gson.toJson(items), LIST_EXAM);
    }

    // ========== 选课信息 ==========

    /**
     * 选课信息列表
     */
    public List<CourseSelectionEntry> courseSelections() throws IOException {
        session.ensureLogin();
        String xh = ensureXhId();
        PageQuery q = new PageQuery();
        Map<String, String> params = q.toMap(Map.of("xh_id", xh));
        String body = session.post(
            "/jwglxt/xsxxxggl/xsxxwh_cxXsxkxx.html?gnmkdm=" + GNMKDM,
            params
        );
        Map<String, Object> raw = gson.fromJson(body, MAP_TYPE);
        Object items = raw.get("items");
        if (items == null) return List.of();
        return gson.fromJson(gson.toJson(items), LIST_COURSE_SEL);
    }

    // ========== 家庭成员 ==========

    /**
     * 家庭成员列表
     */
    public List<Map<String, Object>> familyMembers() throws IOException {
        session.ensureLogin();
        String xh = ensureXhId();
        PageQuery q = new PageQuery();
        Map<String, String> params = q.toMap(Map.of("xh_id", xh));
        String body = session.post(
            "/jwglxt/xsxxxggl/xsxxwh_cxXsjtcy.html?gnmkdm=" + GNMKDM,
            params
        );
        return extractItems(body);
    }

    // ========== 简历/鉴定等（可能为空） ==========

    /**
     * 简历信息
     */
    public List<Map<String, Object>> educationRecords() throws IOException {
        session.ensureLogin();
        String xh = ensureXhId();
        PageQuery q = new PageQuery();
        Map<String, String> params = q.toMap(Map.of("xh_id", xh));
        String body = session.post(
            "/jwglxt/xsxxxggl/xsxxwh_cxXsxxjl.html?gnmkdm=" + GNMKDM,
            params
        );
        return extractItems(body);
    }

    /**
     * 学年鉴定
     */
    public List<Map<String, Object>> yearEvaluations() throws IOException {
        session.ensureLogin();
        String xh = ensureXhId();
        PageQuery q = new PageQuery();
        Map<String, String> params = q.toMap(Map.of("xh_id", xh));
        String body = session.post(
            "/jwglxt/xsxxxggl/xsxxwh_cxXsxnjd.html?gnmkdm=" + GNMKDM,
            params
        );
        return extractItems(body);
    }

    // ========== 内部 ==========

    private String ensureXhId() throws IOException {
        if (xhId == null) {
            StudentProfile p = profile();
            xhId = p.getStudentId();
        }
        return xhId;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractItems(String json) {
        Map<String, Object> raw = gson.fromJson(json, MAP_TYPE);
        Object items = raw.get("items");
        if (items == null) return List.of();
        return (List<Map<String, Object>>) items;
    }
}
