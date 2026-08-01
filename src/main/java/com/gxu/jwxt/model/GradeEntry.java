package com.gxu.jwxt.model;

import com.google.gson.annotations.SerializedName;

/** 课程成绩条目。 */
public class GradeEntry {
    @SerializedName("kcmc") private String courseName;
    @SerializedName("kch") private String courseCode;
    @SerializedName("kcywmc") private String englishCourseName;
    @SerializedName("cj") private String score;
    @SerializedName("bfzcj") private String percentageScore;
    @SerializedName("jd") private String gradePoint;
    @SerializedName("xf") private String credits;
    @SerializedName("zxs") private String totalHours;
    @SerializedName("khfsmc") private String assessmentMethod;
    @SerializedName("ksxz") private String examNature;
    @SerializedName("jsxm") private String teacherName;
    @SerializedName("xnm") private String schoolYear;
    @SerializedName("xqm") private String term;
    @SerializedName("xnmmc") private String schoolYearName;
    @SerializedName("xqmmc") private String termName;
    @SerializedName("kclbmc") private String courseCategory;

    public String getCourseName() { return courseName; }
    public String getCourseCode() { return courseCode; }
    public String getEnglishCourseName() { return englishCourseName; }
    public String getScore() { return score; }
    public String getPercentageScore() { return percentageScore; }
    public String getGradePoint() { return gradePoint; }
    public String getCredits() { return credits; }
    public String getTotalHours() { return totalHours; }
    public String getAssessmentMethod() { return assessmentMethod; }
    public String getExamNature() { return examNature; }
    public String getTeacherName() { return teacherName; }
    public String getSchoolYear() { return schoolYear; }
    public String getTerm() { return term; }
    public String getSchoolYearName() { return schoolYearName; }
    public String getTermName() { return termName; }
    public String getCourseCategory() { return courseCategory; }
}
