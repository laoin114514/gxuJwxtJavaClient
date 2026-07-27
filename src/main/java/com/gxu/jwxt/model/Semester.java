package com.gxu.jwxt.model;

import java.time.LocalDate;

/** 学年学期 */
public class Semester {
    private final String year;
    private final String term;

    public Semester(String year, String term) {
        this.year = year;
        this.term = term;
    }

    public String getYear() { return year; }
    public String getTerm() { return term; }

    /** 根据当前日期推断学期 */
    public static Semester current() {
        LocalDate now = LocalDate.now();
        int y = now.getYear();
        int m = now.getMonthValue();
        if (m >= 9) {
            return new Semester(String.valueOf(y), "3");
        } else if (m >= 2) {
            return new Semester(String.valueOf(y - 1), "12");
        } else {
            return new Semester(String.valueOf(y - 1), "3");
        }
    }

    @Override
    public String toString() {
        return year + "-" + (Integer.parseInt(year) + 1) + " 第" + (term.equals("3") ? "一" : "二") + "学期";
    }
}
