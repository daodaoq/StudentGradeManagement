/**
 * 统计报表模块
 * 功能：学生成绩综合统计、班级排名、成绩趋势分析、数据导出格式化
 * 开发者：宋君奎（组长）
 */

import java.util.*;
import java.util.stream.Collectors;

/**
 * 综合报表数据类
 */
class StudentReport {
    private String studentId;
    private String studentName;
    private String className;
    private int courseCount;
    private double totalCredits;
    private double averageScore;
    private double gpa;
    private int rank;
    private long failCount;

    public StudentReport() {}

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public int getCourseCount() { return courseCount; }
    public void setCourseCount(int courseCount) { this.courseCount = courseCount; }

    public double getTotalCredits() { return totalCredits; }
    public void setTotalCredits(double totalCredits) { this.totalCredits = totalCredits; }

    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public double getGpa() { return gpa; }
    public void setGpa(double gpa) { this.gpa = gpa; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public long getFailCount() { return failCount; }
    public void setFailCount(long failCount) { this.failCount = failCount; }

    @Override
    public String toString() {
        return String.format("排名：%d | 学号：%s | 姓名：%s | 课程数：%d | 平均分：%.1f | GPA：%.2f | 不及格：%d门",
                rank, studentId, studentName, courseCount, averageScore, gpa, failCount);
    }
}

/**
 * 课程统计报表数据类
 */
class CourseStatistics {
    private String courseId;
    private String courseName;
    private int studentCount;
    private double average;
    private double maxScore;
    private double minScore;
    private double passRate;
    private Map<String, Long> distribution;

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public double getAverage() { return average; }
    public void setAverage(double average) { this.average = average; }

    public double getPassRate() { return passRate; }
    public void setPassRate(double passRate) { this.passRate = passRate; }

    public Map<String, Long> getDistribution() { return distribution; }
    public void setDistribution(Map<String, Long> distribution) { this.distribution = distribution; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("课程：%s | 人数：%d | 平均分：%.1f | 最高分：%.1f | 最低分：%.1f | 通过率：%.1f%%\n",
                courseName, studentCount, average, maxScore, minScore, passRate * 100));
        sb.append("  等级分布：");
        distribution.forEach((level, count) -> sb.append(level).append("=").append(count).append("人 "));
        return sb.toString();
    }

    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }
    public void setMinScore(double minScore) { this.minScore = minScore; }
}

/**
 * 统计报表生成器
 * 负责汇总成绩数据、生成排名、导出格式化报表
 */
public class statistics_ReportGenerator {

    private grade_GradeManager gradeManager;
    private student_StudentManager studentManager;
    private course_CourseManager courseManager;
    private Map<String, String> studentNames;
    private Map<String, String> studentClasses;
    private Map<String, Double> courseCredits;

    public statistics_ReportGenerator(grade_GradeManager gradeManager,
                                       student_StudentManager studentManager,
                                       course_CourseManager courseManager) {
        this.gradeManager = gradeManager;
        this.studentManager = studentManager;
        this.courseManager = courseManager;
        initMaps();
    }

    /**
     * 初始化辅助映射表
     */
    private void initMaps() {
        studentNames = new HashMap<>();
        studentClasses = new HashMap<>();
        for (Student s : studentManager.getAllStudents()) {
            studentNames.put(s.getStudentId(), s.getName());
            studentClasses.put(s.getStudentId(), s.getClassName());
        }

        courseCredits = new HashMap<>();
        for (Course c : courseManager.getAllCourses()) {
            courseCredits.put(c.getCourseId(), c.getCredit());
        }
    }

    /**
     * 生成学生综合排名报表（按GPA降序）
     */
    public List<StudentReport> generateStudentRanking() {
        List<StudentReport> reports = new ArrayList<>();

        Set<String> studentIds = gradeManager.getAllGrades().stream()
                .map(Grade::getStudentId)
                .collect(Collectors.toSet());

        for (String sid : studentIds) {
            List<Grade> grades = gradeManager.findByStudentId(sid);
            StudentReport report = new StudentReport();
            report.setStudentId(sid);
            report.setStudentName(studentNames.getOrDefault(sid, "未知"));
            report.setClassName(studentClasses.getOrDefault(sid, "未知"));
            report.setCourseCount(grades.size());

            double totalCredits = grades.stream()
                    .mapToDouble(g -> courseCredits.getOrDefault(g.getCourseId(), 0.0))
                    .sum();
            report.setTotalCredits(totalCredits);

            double avgScore = grades.stream()
                    .mapToDouble(Grade::getTotalScore).average().orElse(0.0);
            report.setAverageScore(avgScore);

            report.setGpa(gradeManager.calculateGPA(sid, courseCredits));

            long failCount = grades.stream()
                    .filter(g -> g.getTotalScore() < 60).count();
            report.setFailCount(failCount);

            reports.add(report);
        }

        reports.sort((a, b) -> Double.compare(b.getGpa(), a.getGpa()));
        for (int i = 0; i < reports.size(); i++) {
            reports.get(i).setRank(i + 1);
        }

        return reports;
    }

    /**
     * 生成课程成绩统计报表
     */
    public List<CourseStatistics> generateCourseStatistics() {
        List<CourseStatistics> statsList = new ArrayList<>();
        List<Course> courses = courseManager.getAllCourses();

        for (Course c : courses) {
            List<Grade> grades = gradeManager.findByCourseId(c.getCourseId());
            if (grades.isEmpty()) continue;

            CourseStatistics stats = new CourseStatistics();
            stats.setCourseId(c.getCourseId());
            stats.setCourseName(c.getCourseName());
            stats.setStudentCount(grades.size());

            double avg = grades.stream().mapToDouble(Grade::getTotalScore).average().orElse(0.0);
            stats.setAverage(avg);

            double max = grades.stream().mapToDouble(Grade::getTotalScore).max().orElse(0.0);
            double min = grades.stream().mapToDouble(Grade::getTotalScore).min().orElse(0.0);
            stats.setMaxScore(max);
            stats.setMinScore(min);

            long passed = grades.stream().filter(g -> g.getTotalScore() >= 60).count();
            stats.setPassRate((double) passed / grades.size());

            Map<String, Long> dist = grades.stream()
                    .collect(Collectors.groupingBy(Grade::getGradeLevel, Collectors.counting()));
            stats.setDistribution(dist);

            statsList.add(stats);
        }

        return statsList;
    }

    /**
     * 按班级分组统计平均成绩
     */
    public Map<String, Double> getClassAverageScores() {
        Map<String, List<Double>> classScores = new HashMap<>();

        for (Grade g : gradeManager.getAllGrades()) {
            String className = studentClasses.getOrDefault(g.getStudentId(), "未知班级");
            classScores.computeIfAbsent(className, k -> new ArrayList<>())
                    .add(g.getTotalScore());
        }

        Map<String, Double> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<Double>> entry : classScores.entrySet()) {
            double avg = entry.getValue().stream()
                    .mapToDouble(Double::doubleValue)
                    .average().orElse(0.0);
            result.put(entry.getKey(), avg);
        }
        return result;
    }

    /**
     * 打印分隔线
     */
    private void printSeparator(String title) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  " + title);
        System.out.println("=".repeat(60));
    }

    /**
     * 导出学生排名报表
     */
    public void exportStudentRankingReport() {
        List<StudentReport> reports = generateStudentRanking();
        printSeparator("学生综合排名报表");
        System.out.printf("%-4s %-10s %-8s %-8s %-6s %-6s %-6s %s%n",
                "排名", "学号", "姓名", "班级", "课程数", "平均分", "GPA", "不及格");
        System.out.println("-".repeat(60));
        for (StudentReport r : reports) {
            System.out.printf("%-4d %-10s %-8s %-8s %-6d %-6.1f %-6.2f %d门%n",
                    r.getRank(), r.getStudentId(), r.getStudentName(),
                    r.getClassName(), r.getCourseCount(),
                    r.getAverageScore(), r.getGpa(), r.getFailCount());
        }
    }

    /**
     * 导出课程统计报表
     */
    public void exportCourseStatisticsReport() {
        List<CourseStatistics> stats = generateCourseStatistics();
        printSeparator("课程成绩统计报表");
        for (CourseStatistics cs : stats) {
            System.out.println(cs.toString());
        }
    }

    /**
     * 导出班级对比报表
     */
    public void exportClassComparisonReport() {
        Map<String, Double> classAvg = getClassAverageScores();
        printSeparator("班级平均成绩对比");
        List<Map.Entry<String, Double>> sorted = new ArrayList<>(classAvg.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        for (int i = 0; i < sorted.size(); i++) {
            Map.Entry<String, Double> entry = sorted.get(i);
            System.out.printf("  %d. %s → 平均分：%.1f%n", i + 1, entry.getKey(), entry.getValue());
        }
    }

    /**
     * 导出完整综合报表
     */
    public void exportFullReport() {
        exportStudentRankingReport();
        exportCourseStatisticsReport();
        exportClassComparisonReport();
        System.out.println("\n报表导出完毕。");
    }

    /**
     * 测试主方法 - 综合演示
     */
    public static void main(String[] args) {
        student_StudentManager sm = new student_StudentManager();
        course_CourseManager cm = new course_CourseManager();
        grade_GradeManager gm = new grade_GradeManager();

        sm.addStudent(new Student("20240001", "张三", "男", 20, "软件2304", "13800138001", "zs@qq.com", "2024"));
        sm.addStudent(new Student("20240002", "李四", "女", 19, "软件2304", "13800138002", "ls@qq.com", "2024"));
        sm.addStudent(new Student("20240003", "王五", "男", 21, "软件2303", "13800138003", "ww@163.com", "2024"));
        sm.addStudent(new Student("20240004", "赵六", "女", 20, "软件2303", "13800138004", "zl@163.com", "2024"));

        cm.addCourse(new Course("C001", "Java程序设计", "专业必修", 4.0, 64, "王教授", "2024-2025-1", 60, ""));
        cm.addCourse(new Course("C002", "数据结构", "专业必修", 3.5, 56, "李教授", "2024-2025-1", 60, ""));
        cm.addCourse(new Course("C003", "软件工程", "专业必修", 3.0, 48, "赵教授", "2024-2025-2", 60, ""));

        gm.addGrade("20240001", "C001", 90, 85, "2024-2025-1");
        gm.addGrade("20240001", "C002", 78, 72, "2024-2025-1");
        gm.addGrade("20240001", "C003", 88, 92, "2024-2025-2");
        gm.addGrade("20240002", "C001", 60, 55, "2024-2025-1");
        gm.addGrade("20240002", "C002", 75, 80, "2024-2025-1");
        gm.addGrade("20240003", "C001", 45, 50, "2024-2025-1");
        gm.addGrade("20240003", "C002", 95, 93, "2024-2025-1");
        gm.addGrade("20240004", "C001", 70, 68, "2024-2025-1");
        gm.addGrade("20240004", "C003", 82, 88, "2024-2025-2");

        statistics_ReportGenerator reportGen = new statistics_ReportGenerator(gm, sm, cm);
        reportGen.exportFullReport();
    }
}
