/**
 * 成绩管理模块
 * 功能：成绩录入、修改、查询、绩点计算、成绩统计分析
 * 开发者：刘超
 */

import java.util.*;
import java.util.stream.Collectors;

/**
 * 成绩实体类
 */
class Grade {
    private String gradeId;
    private String studentId;
    private String courseId;
    private double regularScore;
    private double examScore;
    private double totalScore;
    private String gradeLevel;
    private double gradePoint;
    private String semester;

    public Grade() {}

    public Grade(String gradeId, String studentId, String courseId,
                 double regularScore, double examScore, String semester) {
        this.gradeId = gradeId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.regularScore = regularScore;
        this.examScore = examScore;
        this.semester = semester;
        calculateTotalAndLevel();
    }

    public void calculateTotalAndLevel() {
        this.totalScore = regularScore * 0.3 + examScore * 0.7;
        this.gradeLevel = determineLevel(this.totalScore);
        this.gradePoint = determineGradePoint(this.totalScore);
    }

    private String determineLevel(double score) {
        if (score >= 90) return "优秀";
        else if (score >= 80) return "良好";
        else if (score >= 70) return "中等";
        else if (score >= 60) return "及格";
        else return "不及格";
    }

    private double determineGradePoint(double score) {
        if (score >= 90) return 4.0;
        else if (score >= 85) return 3.7;
        else if (score >= 80) return 3.3;
        else if (score >= 75) return 3.0;
        else if (score >= 70) return 2.7;
        else if (score >= 65) return 2.3;
        else if (score >= 60) return 2.0;
        else return 0.0;
    }

    public String getGradeId() { return gradeId; }
    public void setGradeId(String gradeId) { this.gradeId = gradeId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public double getRegularScore() { return regularScore; }
    public void setRegularScore(double regularScore) {
        this.regularScore = regularScore;
        calculateTotalAndLevel();
    }

    public double getExamScore() { return examScore; }
    public void setExamScore(double examScore) {
        this.examScore = examScore;
        calculateTotalAndLevel();
    }

    public double getTotalScore() { return totalScore; }
    public String getGradeLevel() { return gradeLevel; }
    public double getGradePoint() { return gradePoint; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    @Override
    public String toString() {
        return String.format("成绩ID：%s | 学号：%s | 课程：%s | 平时：%.1f | 考试：%.1f | 总评：%.1f | 等级：%s | 绩点：%.1f",
                gradeId, studentId, courseId, regularScore, examScore, totalScore, gradeLevel, gradePoint);
    }
}

/**
 * 成绩管理器
 * 负责成绩录入、查询、统计分析与绩点计算
 */
public class grade_GradeManager {

    private List<Grade> gradeList;
    private int nextGradeId = 1;

    public grade_GradeManager() {
        this.gradeList = new ArrayList<>();
    }

    /**
     * 录入成绩
     * @return 成绩对象，校验失败返回null
     */
    public Grade addGrade(String studentId, String courseId,
                          double regularScore, double examScore, String semester) {
        if (!validateScores(regularScore, examScore)) {
            System.out.println("[错误] 成绩数据校验不通过（0-100分）。");
            return null;
        }
        String gradeId = "G" + String.format("%05d", nextGradeId++);
        Grade grade = new Grade(gradeId, studentId, courseId, regularScore, examScore, semester);
        gradeList.add(grade);
        System.out.println("[成功] 成绩录入成功。总分：" + String.format("%.1f", grade.getTotalScore())
                + "，等级：" + grade.getGradeLevel());
        return grade;
    }

    /**
     * 修改成绩
     */
    public boolean updateGrade(String gradeId, double regularScore, double examScore) {
        Optional<Grade> existing = findByGradeId(gradeId);
        if (!existing.isPresent()) {
            System.out.println("[错误] 未找到该成绩记录。");
            return false;
        }
        if (!validateScores(regularScore, examScore)) {
            System.out.println("[错误] 成绩数据校验不通过。");
            return false;
        }
        Grade grade = existing.get();
        grade.setRegularScore(regularScore);
        grade.setExamScore(examScore);
        System.out.println("[成功] 成绩修改成功。新课成绩：" + String.format("%.1f", grade.getTotalScore()));
        return true;
    }

    /**
     * 删除成绩
     */
    public boolean deleteGrade(String gradeId) {
        Optional<Grade> grade = findByGradeId(gradeId);
        if (!grade.isPresent()) {
            System.out.println("[错误] 未找到该成绩记录。");
            return false;
        }
        gradeList.remove(grade.get());
        System.out.println("[成功] 成绩记录删除成功。");
        return true;
    }

    /**
     * 按成绩ID查找
     */
    public Optional<Grade> findByGradeId(String gradeId) {
        return gradeList.stream()
                .filter(g -> g.getGradeId().equals(gradeId))
                .findFirst();
    }

    /**
     * 按学号查找该学生所有成绩
     */
    public List<Grade> findByStudentId(String studentId) {
        return gradeList.stream()
                .filter(g -> g.getStudentId().equals(studentId))
                .collect(Collectors.toList());
    }

    /**
     * 按课程查找该课程所有成绩
     */
    public List<Grade> findByCourseId(String courseId) {
        return gradeList.stream()
                .filter(g -> g.getCourseId().equals(courseId))
                .collect(Collectors.toList());
    }

    /**
     * 计算学生的加权平均绩点(GPA)
     * @param courseCreditMap 课程ID到学分的映射
     */
    public double calculateGPA(String studentId, Map<String, Double> courseCreditMap) {
        List<Grade> studentGrades = findByStudentId(studentId);
        if (studentGrades.isEmpty()) return 0.0;

        double totalPoints = 0;
        double totalCredits = 0;
        for (Grade g : studentGrades) {
            Double credit = courseCreditMap.getOrDefault(g.getCourseId(), 0.0);
            totalPoints += g.getGradePoint() * credit;
            totalCredits += credit;
        }
        return totalCredits == 0 ? 0.0 : totalPoints / totalCredits;
    }

    /**
     * 统计各等级人数分布
     */
    public Map<String, Long> getGradeDistribution() {
        return gradeList.stream()
                .collect(Collectors.groupingBy(Grade::getGradeLevel, Collectors.counting()));
    }

    /**
     * 计算某课程的平均分
     */
    public double getAverageScoreByCourse(String courseId) {
        return gradeList.stream()
                .filter(g -> g.getCourseId().equals(courseId))
                .mapToDouble(Grade::getTotalScore)
                .average()
                .orElse(0.0);
    }

    /**
     * 计算某课程的最高分和最低分
     */
    public double[] getMinMaxByCourse(String courseId) {
        List<Grade> courseGrades = findByCourseId(courseId);
        if (courseGrades.isEmpty()) return new double[]{0, 0};
        double min = courseGrades.stream().mapToDouble(Grade::getTotalScore).min().getAsDouble();
        double max = courseGrades.stream().mapToDouble(Grade::getTotalScore).max().getAsDouble();
        return new double[]{min, max};
    }

    /**
     * 获取不及格学生列表
     */
    public List<Grade> getFailedGrades() {
        return gradeList.stream()
                .filter(g -> g.getTotalScore() < 60)
                .collect(Collectors.toList());
    }

    /**
     * 成绩校验：分数在0-100之间
     */
    private boolean validateScores(double regularScore, double examScore) {
        if (regularScore < 0 || regularScore > 100) {
            System.out.println("[校验失败] 平时成绩须在0-100之间。");
            return false;
        }
        if (examScore < 0 || examScore > 100) {
            System.out.println("[校验失败] 考试成绩须在0-100之间。");
            return false;
        }
        return true;
    }

    /**
     * 获取所有成绩
     */
    public List<Grade> getAllGrades() {
        return new ArrayList<>(gradeList);
    }

    /**
     * 测试主方法
     */
    public static void main(String[] args) {
        grade_GradeManager manager = new grade_GradeManager();

        manager.addGrade("20240001", "C001", 90, 85, "2024-2025-1");
        manager.addGrade("20240001", "C002", 78, 72, "2024-2025-1");
        manager.addGrade("20240002", "C001", 60, 55, "2024-2025-1");
        manager.addGrade("20240002", "C003", 88, 92, "2024-2025-1");
        manager.addGrade("20240003", "C001", 45, 50, "2024-2025-1");
        manager.addGrade("20240003", "C002", 95, 93, "2024-2025-1");

        System.out.println("\n===== 所有成绩记录 =====");
        manager.getAllGrades().forEach(System.out::println);

        System.out.println("\n===== 成绩等级分布 =====");
        manager.getGradeDistribution()
                .forEach((level, count) -> System.out.println("  " + level + "：" + count + "人"));

        System.out.println("\n===== C001课程统计 =====");
        System.out.printf("平均分：%.1f%n", manager.getAverageScoreByCourse("C001"));
        double[] minMax = manager.getMinMaxByCourse("C001");
        System.out.printf("最高分：%.1f，最低分：%.1f%n", minMax[1], minMax[0]);

        System.out.println("\n===== 不及格学生 =====");
        List<Grade> failed = manager.getFailedGrades();
        if (failed.isEmpty()) {
            System.out.println("无不及时成绩记录。");
        } else {
            failed.forEach(System.out::println);
        }

        System.out.println("\n===== 学生20240001的成绩 =====");
        Map<String, Double> credits = new HashMap<>();
        credits.put("C001", 4.0);
        credits.put("C002", 3.5);
        credits.put("C003", 3.0);
        manager.findByStudentId("20240001").forEach(System.out::println);
        System.out.printf("GPA：%.2f%n", manager.calculateGPA("20240001", credits));
    }
}
