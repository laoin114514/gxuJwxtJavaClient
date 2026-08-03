package com.gxu.jwxt.model;

import com.google.gson.annotations.SerializedName;

/**
 * 课程安排条目（课表 kbList / 实践课 sjkList 共用）。
 * 字段名映射教务系统拼音缩写，忽略 queryModel/userModel 等框架噪音。
 */
public class CourseEntry {

    // ---- 课程基本信息 ----
    @SerializedName("kcmc")
    private String courseName;          // 课程名

    @SerializedName("kch")
    private String courseCode;          // 课程号

    @SerializedName("kclb")
    private String courseCategory;      // 课程类别（通识选修课/学科核心课 等）

    @SerializedName("kcxz")
    private String courseNature;        // 课程性质（通识/学科/必修）

    @SerializedName("xf")
    private String credits;             // 学分

    @SerializedName("qqqh")
    private String qqGroup;             // QQ群号（个人课表接口直接返回）

    @SerializedName("zhxs")
    private String totalHours;          // 总学时

    @SerializedName("kcxszc")
    private String hourComposition;     // 学时组成（如 "讲课:32"）

    @SerializedName("kczxs")
    private String weeklyHours;         // 周学时

    // ---- 教师信息 ----
    @SerializedName("xm")
    private String teacherName;         // 教师名（kbList 用）

    @SerializedName("jsxm")
    private String teacherName2;        // 教师名（sjkList 用）

    @SerializedName("zzmm")
    private String teacherTitle;        // 职称/政治面貌

    // ---- 教学班 ----
    @SerializedName("jxbmc")
    private String className;           // 教学班名称

    @SerializedName("jxbzc")
    private String classComposition;    // 教学班组成

    @SerializedName("jxb_id")
    private String classId;             // 教学班 ID

    // ---- 教室 ----
    @SerializedName("cdmc")
    private String classroom;           // 教室名

    @SerializedName("cd_id")
    private String classroomId;         // 教室 ID

    @SerializedName("cdbh")
    private String classroomCode;       // 教室编号

    @SerializedName("cdlbmc")
    private String classroomType;       // 教室类别（多媒体/智慧教室）

    // ---- 时间安排 ----
    @SerializedName("jc")
    private String period;              // 节次（如 "1-2节"）

    @SerializedName("jcs")
    private String periodNum;           // 节次数（如 "1-2"）

    @SerializedName("jcor")
    private String periodOrder;         // 节次排序

    @SerializedName("zcd")
    private String weeks;               // 周次（如 "1-5周,7-14周,16周"）

    @SerializedName("xqj")
    private String weekday;             // 星期几（数字 1-7）

    @SerializedName("xqjmc")
    private String weekdayName;         // 星期几（中文）

    // ---- 考核 ----
    @SerializedName("khfsmc")
    private String examType;            // 考核方式（考试/考查）

    @SerializedName("ksfsmc")
    private String examForm;            // 考试形式

    @SerializedName("skfsmc")
    private String teachForm;           // 授课方式

    // ---- 开课 ----
    @SerializedName("kkzt")
    private String courseStatus;        // 开课状态

    @SerializedName("sxbj")
    private String selected;            // 是否已选

    @SerializedName("xkrs")
    private String maxStudents;         // 选课人数上限

    @SerializedName("zzrl")
    private String enrolled;            // 已选人数

    @SerializedName("sfjf")
    private String isCharge;            // 是否计费

    // ---- 实践课专属 ----
    @SerializedName("sfsjk")
    private String isPractice;          // 是否实践课 "1"

    @SerializedName("qsjsz")
    private String practicePeriodRange; // 起止教学周

    @SerializedName("sjkcgs")
    private String practiceDetail;      // 实践课程描述

    @SerializedName("qtkcgs")
    private String practiceFullDesc;    // 实践课程完整描述

    @SerializedName("xksj")
    private String selectTime;          // 选课时间

    @SerializedName("kklxdm")
    private String courseTypeCode;      // 开课类型代码

    @SerializedName("kcbj")
    private String courseMark;          // 课程标记（主修/辅修）

    // ---- getters ----

    public String getCourseName() { return courseName; }
    public String getCourseCode() { return courseCode; }
    public String getCourseCategory() { return courseCategory; }
    public String getCourseNature() { return courseNature; }
    public String getCredits() { return credits; }
    public String getQqGroup() { return qqGroup; }
    public String getTotalHours() { return totalHours; }
    public String getHourComposition() { return hourComposition; }
    public String getWeeklyHours() { return weeklyHours; }
    public String getTeacherName() { return teacherName != null ? teacherName : teacherName2; }
    public String getTeacherTitle() { return teacherTitle; }
    public String getClassName() { return className; }
    public String getClassComposition() { return classComposition; }
    public String getClassId() { return classId; }
    public String getClassroom() { return classroom; }
    public String getClassroomId() { return classroomId; }
    public String getClassroomCode() { return classroomCode; }
    public String getClassroomType() { return classroomType; }
    public String getPeriod() { return period; }
    public String getPeriodNum() { return periodNum; }
    public String getWeeks() { return weeks; }
    public String getWeekday() { return weekday; }
    public String getWeekdayName() { return weekdayName; }
    public String getExamType() { return examType; }
    public String getExamForm() { return examForm; }
    public String getTeachForm() { return teachForm; }
    public String getCourseStatus() { return courseStatus; }
    public String getSelected() { return selected; }
    public String getMaxStudents() { return maxStudents; }
    public String getEnrolled() { return enrolled; }
    public String getIsCharge() { return isCharge; }
    public String getIsPractice() { return isPractice; }
    public String getPracticePeriodRange() { return practicePeriodRange; }
    public String getPracticeDetail() { return practiceDetail; }
    public String getPracticeFullDesc() { return practiceFullDesc; }
    public String getSelectTime() { return selectTime; }
    public String getCourseTypeCode() { return courseTypeCode; }
    public String getCourseMark() { return courseMark; }

    @Override
    public String toString() {
        return getTeacherName() + " / " + courseName + " / " + period + " / " + classroom;
    }
}
