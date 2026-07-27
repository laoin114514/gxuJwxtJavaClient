package com.gxu.jwxt;

import com.gxu.jwxt.exceptions.LoginException;
import com.gxu.jwxt.exceptions.NotLoggedInException;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JwxtSessionTest {

    // Use env vars or these hardcoded test credentials
    private static final String USERNAME = System.getenv().getOrDefault("JWXT_USERNAME", "REDACTED_STUDENT_ID");
    private static final String PASSWORD = System.getenv().getOrDefault("JWXT_PASSWORD", "REDACTED_PASSWORD");
    private static final String BASE_URL = System.getenv().getOrDefault("JWXT_BASE_URL", "https://jwxt2018.gxu.edu.cn");

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
    @DisplayName("课表 - personal() 返回非空 Map")
    void testSchedulePersonal() {
        assertDoesNotThrow(() -> {
            Map<String, Object> data = client.schedule().personal("2025", "12");
            assertNotNull(data, "个人课表数据不应为 null");
        });
    }

    @Test
    @Order(5)
    @DisplayName("课表 - teacher() 返回非空 Map")
    void testScheduleTeacher() {
        assertDoesNotThrow(() -> {
            Map<String, Object> data = client.schedule().teacher("2025", "12", "");
            assertNotNull(data, "教师课表数据不应为 null");
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
    @DisplayName("currentSemester() 返回当前学期")
    void testCurrentSemester() {
        var sem = JwxtClient.currentSemester();
        assertNotNull(sem);
        assertNotNull(sem.getYear());
        assertNotNull(sem.getTerm());
        // term must be either "3" or "12"
        assertTrue(sem.getTerm().equals("3") || sem.getTerm().equals("12"),
            "学期编码应为 3 或 12，实际: " + sem.getTerm());
    }

    @Test
    @Order(9)
    @DisplayName("getUsername() 返回正确的用户名")
    void testGetUsername() {
        assertEquals(USERNAME, client.getUsername());
    }

    @Test
    @Order(10)
    @DisplayName("toString() 包含用户名")
    void testToString() {
        String s = client.toString();
        assertTrue(s.contains(USERNAME), "toString 应包含用户名");
    }

    @Test
    @Order(11)
    @DisplayName("未登录时调用 schedule 应抛出 NotLoggedInException")
    void testNotLoggedInThrows() {
        JwxtClient c = new JwxtClient("user", "pass");
        assertThrows(NotLoggedInException.class,
            () -> c.schedule().page(),
            "未登录访问课表应抛出 NotLoggedInException");
    }

    @Test
    @Order(12)
    @DisplayName("退出登录")
    void testLogout() {
        assertDoesNotThrow(() -> client.logout());
        assertFalse(client.isLoggedIn(), "退出后 isLoggedIn 应为 false");
    }
}
