package com.gxu.jwxt;

import com.gxu.jwxt.exceptions.LoginException;
import com.gxu.jwxt.exceptions.NotLoggedInException;
import com.gxu.jwxt.model.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JwxtSessionTest {

    private static final String USERNAME = requireEnv("JWXT_USERNAME");
    private static final String PASSWORD = requireEnv("JWXT_PASSWORD");
    private static final String BASE_URL = System.getenv().getOrDefault("JWXT_BASE_URL", "https://jwxt2018.gxu.edu.cn");

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("运行集成测试需要设置环境变量 " + name);
        }
        return value;
    }

    private static JwxtClient client;

    @Test
    @Order(1)
    @DisplayName("登录 - 应成功")
    void testLogin() {
        client = new JwxtClient(USERNAME, PASSWORD, BASE_URL);
        assertDoesNotThrow(() -> client.login(), "登录不应抛出异常");
        assertTrue(client.isLoggedIn(), "登录后 isLoggedIn 应为 true");
    }

    @Test
    @Order(2)
    @DisplayName("登录 - 错误密码应抛出 LoginException")
    void testLoginFailure() {
        JwxtClient badClient = new JwxtClient("wrong_user_99999", "wrong_pass_99999", BASE_URL);
        assertThrows(LoginException.class, badClient::login,
            "错误账号应抛出 LoginException");
    }

    @Test
    @Order(3)
    @DisplayName("课表 - page() 返回 HTML")
    void testSchedulePage() {
        assertDoesNotThrow(() -> {
            String html = client.schedule().page();
            assertNotNull(html, "课表页面不应为 null");
            assertFalse(html.isEmpty(), "课表页面不应为空");
        });
    }

    @Test
    @Order(4)
    @DisplayName("课表 - personal() 返回结构化数据")
    void testSchedulePersonal() {
        assertDoesNotThrow(() -> {
            ScheduleResponse data = client.schedule().personal("2024", "12");
            assertNotNull(data, "个人课表数据不应为 null");
            assertNotNull(data.getStudentInfo(), "学生信息不应为 null");
            assertNotNull(data.getStudentInfo().getName(), "学生姓名不应为 null");
            assertNotNull(data.getCourses(), "课程列表不应为 null");
            assertFalse(data.getCourses().isEmpty(), "课程列表不应为空");

            CourseEntry first = data.getCourses().get(0);
            assertNotNull(first.getCourseName(), "课程名不应为 null");
            assertNotNull(first.getTeacherName(), "教师名不应为 null");
        });
    }

    @Test
    @Order(5)
    @DisplayName("课表 - teacher() 返回结构化数据")
    void testScheduleTeacher() {
        assertDoesNotThrow(() -> {
            TeacherScheduleResponse data = client.schedule().teacher("2024", "12", "");
            assertNotNull(data, "教师课表数据不应为 null");
            assertNotNull(data.getScheduleTypes(), "学时类型列表不应为 null");
            assertFalse(data.getScheduleTypes().isEmpty(), "学时类型不应为空");

            ScheduleType st = data.getScheduleTypes().get(0);
            assertNotNull(st.getCode());
            assertNotNull(st.getName());
        });
    }

    @Test
    @Order(6)
    @DisplayName("课表 - classSchedulePage() 返回 HTML")
    void testClassSchedulePage() {
        assertDoesNotThrow(() -> {
            String html = client.schedule().classSchedulePage();
            assertNotNull(html);
            assertFalse(html.isEmpty());
        });
    }

    @Test
    @Order(7)
    @DisplayName("课表 - creditConfirm() 返回 HTML")
    void testCreditConfirm() {
        assertDoesNotThrow(() -> {
            String html = client.schedule().creditConfirm();
            assertNotNull(html);
            assertFalse(html.isEmpty());
        });
    }

    @Test
    @Order(8)
    @DisplayName("泛型 query() 返回指定类型")
    void testGenericQuery() {
        assertDoesNotThrow(() -> {
            PageQuery q = new PageQuery();
            ScheduleResponse data = client.query(
                "/jwglxt/kbcx/xskbcx_cxXsgrkb.html?gnmkdm=N2151",
                q.toMap(java.util.Map.of("xnm", "2024", "xqm", "12")),
                ScheduleResponse.class
            );
            assertNotNull(data);
            assertNotNull(data.getStudentInfo());
        });
    }

    @Test
    @Order(9)
    @DisplayName("currentSemester() 返回当前学期")
    void testCurrentSemester() {
        var sem = JwxtClient.currentSemester();
        assertNotNull(sem);
        assertNotNull(sem.getYear());
        assertNotNull(sem.getTerm());
        assertTrue(sem.getTerm() == com.gxu.jwxt.model.Term.AUTUMN
                || sem.getTerm() == com.gxu.jwxt.model.Term.SPRING,
            "学期应为 AUTUMN 或 SPRING，实际: " + sem.getTerm());
    }

    @Test
    @Order(10)
    @DisplayName("getUsername() 返回正确的用户名")
    void testGetUsername() {
        assertEquals(USERNAME, client.getUsername());
    }

    @Test
    @Order(11)
    @DisplayName("toString() 包含用户名")
    void testToString() {
        String s = client.toString();
        assertTrue(s.contains(USERNAME), "toString 应包含用户名");
    }

    @Test
    @Order(12)
    @DisplayName("未登录时调用 schedule 应抛出 NotLoggedInException")
    void testNotLoggedInThrows() {
        JwxtClient c = new JwxtClient("user", "pass");
        assertThrows(NotLoggedInException.class,
            () -> c.schedule().page(),
            "未登录访问课表应抛出 NotLoggedInException");
    }

    @Test
    @Order(13)
    @DisplayName("退出登录")
    void testLogout() {
        assertDoesNotThrow(() -> client.logout());
        assertFalse(client.isLoggedIn(), "退出后 isLoggedIn 应为 false");
    }
}
