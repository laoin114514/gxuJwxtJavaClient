package com.gxu.jwxt.module;

import com.gxu.jwxt.JwxtSession;
import com.gxu.jwxt.model.CreditStatistic;
import com.gxu.jwxt.model.GradeCount;
import com.gxu.jwxt.model.GradeEntry;
import com.gxu.jwxt.model.PageQuery;
import com.gxu.jwxt.model.PagedResult;
import com.gxu.jwxt.model.Term;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/** 成绩查询模块。 */
public class GradeModule {
    private static final String GNMKDM = "N305005";
    private static final String REFERER = "/jwglxt/cjcx/cjcx_cxDgXscj.html?gnmkdm=" + GNMKDM;
    private final JwxtSession session;

    public GradeModule(JwxtSession session) {
        this.session = session;
    }

    /** 查询一个学期的课程成绩。 */
    public PagedResult<GradeEntry> term(String year, Term term) throws IOException {
        return term(year, term, new PageQuery());
    }

    public PagedResult<GradeEntry> term(String year, Term term, PageQuery page) throws IOException {
        return query(year, term.code(), page);
    }

    /** 查询全部学期的课程成绩。 */
    public PagedResult<GradeEntry> all() throws IOException {
        return all(new PageQuery());
    }

    public PagedResult<GradeEntry> all(PageQuery page) throws IOException {
        return query("", "", page);
    }

    /** 指定学期的课程数与学生数。 */
    public GradeCount count(String year, Term term) throws IOException {
        session.ensureLogin();
        PageQuery page = new PageQuery();
        String body = session.post("/jwglxt/cjcx/cjcx_cxXxCount.html?gnmkdm=" + GNMKDM,
            page.toMap(Map.of("xnm", year, "xqm", term.code())), REFERER);
        return JsonSupport.GSON.fromJson(body, GradeCount.class);
    }

    /** 按课程性质汇总已修学分。 */
    public List<CreditStatistic> creditStatistics() throws IOException {
        session.ensureLogin();
        String body = session.get("/jwglxt/cjcx/cjcx_cxXsxftj.html", REFERER);
        return JsonSupport.list(body, CreditStatistic.class);
    }

    /** 成绩页项目类别下拉选项。 */
    public List<String> projectCategories(String year, Term term) throws IOException {
        session.ensureLogin();
        String body = session.get("/jwglxt/cjcx/cjcx_cxXmblbzlist.html?xnm=" + year
            + "&xqm=" + term.code(), REFERER);
        return JsonSupport.GSON.fromJson(body,
            new com.google.gson.reflect.TypeToken<List<String>>() {}.getType());
    }

    private PagedResult<GradeEntry> query(String year, String term, PageQuery page) throws IOException {
        session.ensureLogin();
        String body = session.post("/jwglxt/cjcx/cjcx_cxXsgrcj.html?doType=query&gnmkdm=" + GNMKDM,
            page.toMap(Map.of("xnm", year, "xqm", term)), REFERER);
        return JsonSupport.page(body, GradeEntry.class);
    }
}
