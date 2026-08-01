package com.gxu.jwxt;

import com.gxu.jwxt.model.Semester;
import com.gxu.jwxt.exceptions.LoginException;
import com.gxu.jwxt.module.ProfileModule;
import com.gxu.jwxt.module.ScheduleModule;
import com.gxu.jwxt.module.GradeModule;
import com.gxu.jwxt.module.ExamModule;
import com.gxu.jwxt.module.ClassroomModule;
import com.gxu.jwxt.module.SelectionModule;
import com.gxu.jwxt.module.TeachingPlanModule;
import com.gxu.jwxt.module.OnlineLearningModule;
import com.gxu.jwxt.module.DictionaryModule;
import com.gxu.jwxt.module.MenuModule;

import java.io.IOException;

/** 教务系统客户端门面 */
public class JwxtClient {

    private final JwxtSession session;
    private final ScheduleModule schedule;
    private final ProfileModule profile;
    private final GradeModule grades;
    private final ExamModule exams;
    private final ClassroomModule classrooms;
    private final SelectionModule selections;
    private final TeachingPlanModule teachingPlans;
    private final OnlineLearningModule onlineLearning;
    private final DictionaryModule dictionary;
    private final MenuModule menu;

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
        this.grades = new GradeModule(this.session);
        this.exams = new ExamModule(this.session);
        this.classrooms = new ClassroomModule(this.session);
        this.selections = new SelectionModule(this.session);
        this.teachingPlans = new TeachingPlanModule(this.session);
        this.onlineLearning = new OnlineLearningModule(this.session);
        this.dictionary = new DictionaryModule(this.session);
        this.menu = new MenuModule(this.session);
    }

    // ========== 认证 ==========

    public void login() throws LoginException {
        session.login();
    }

    /**
     * 强制重新登录（session 过期后调用）。
     *
     * @throws LoginException 登录失败
     */
    public void relogin() throws LoginException {
        session.relogin();
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

    public GradeModule grades() {
        return grades;
    }

    public ExamModule exams() {
        return exams;
    }

    public ClassroomModule classrooms() {
        return classrooms;
    }

    /** 选课确认与选课名单等只读查询。 */
    public SelectionModule selections() {
        return selections;
    }

    public TeachingPlanModule teachingPlans() {
        return teachingPlans;
    }

    public OnlineLearningModule onlineLearning() {
        return onlineLearning;
    }

    public DictionaryModule dictionary() {
        return dictionary;
    }

    public MenuModule menu() {
        return menu;
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
