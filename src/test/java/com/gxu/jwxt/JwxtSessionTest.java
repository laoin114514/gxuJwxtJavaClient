package com.gxu.jwxt;

import com.gxu.jwxt.exceptions.NotLoggedInException;
import com.gxu.jwxt.model.Term;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** 不需要真实教务系统账号的客户端状态测试。 */
class JwxtSessionTest {

    @Test
    void currentSemesterReturnsSupportedTerm() {
        var semester = JwxtClient.currentSemester();
        assertNotNull(semester.getYear());
        assertTrue(semester.getTerm() == Term.AUTUMN || semester.getTerm() == Term.SPRING);
    }

    @Test
    void exposesConfiguredUsernameWithoutLoggingIn() {
        JwxtClient client = new JwxtClient("student", "password");
        assertEquals("student", client.getUsername());
        assertFalse(client.isLoggedIn());
        assertTrue(client.toString().contains("student"));
    }

    @Test
    void structuredModuleRequiresLogin() {
        JwxtClient client = new JwxtClient("student", "password");
        assertThrows(NotLoggedInException.class,
            () -> client.schedule().personal("2025", Term.SPRING));
    }
}
