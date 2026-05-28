/**
 * 课程管理模块
 * 功能：课程信息的添加、删除、修改、查询，课程分类管理，学分统计
 * 开发者：张涵宇
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 课程实体类
 */
class Course {
    private String courseId;
    private String courseName;
    private String category;
    private double credit;
    private int classHours;
    private String teacher;
    private String semester;
    private int maxStudents;
    private String description;

    public Course() {}

    public Course(String courseId, String courseName, String category, double credit,
                  int classHours, String teacher, String semester, int maxStudents,
                  String description) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.category = category;
        this.credit = credit;
        this.classHours = classHours;
        this.teacher = teacher;
        this.semester = semester;
        this.maxStudents = maxStudents;
        this.description = description;
    }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getCredit() { return credit; }
    public void setCredit(double credit) { this.credit = credit; }

    public int getClassHours() { return classHours; }
    public void setClassHours(int classHours) { this.classHours = classHours; }

    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public int getMaxStudents() { return maxStudents; }
    public void setMaxStudents(int maxStudents) { this.maxStudents = maxStudents; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return String.format("课程编号：%s | 课程名：%s | 类别：%s | 学分：%.1f | 学时：%d | 教师：%s | 学期：%s",
                courseId, courseName, category, credit, classHours, teacher, semester);
    }
}

/**
 * 课程管理器
 * 负责课程信息的增删改查、分类查询、学分统计
 */
public class course_CourseManager {

    private List<Course> courseList;

    public course_CourseManager() {
        this.courseList = new ArrayList<>();
    }

    /**
     * 添加新课程
     */
    public boolean addCourse(Course course) {
        if (!validateCourse(course)) {
            System.out.println("[错误] 课程信息校验不通过。");
            return false;
        }
        if (findByCourseId(course.getCourseId()).isPresent()) {
            System.out.println("[错误] 课程编号 " + course.getCourseId() + " 已存在。");
            return false;
        }
        courseList.add(course);
        System.out.println("[成功] 课程 " + course.getCourseName() + " 添加成功。");
        return true;
    }

    /**
     * 更新课程信息
     */
    public boolean updateCourse(Course updatedCourse) {
        Optional<Course> existing = findByCourseId(updatedCourse.getCourseId());
        if (!existing.isPresent()) {
            System.out.println("[错误] 未找到课程编号为 " + updatedCourse.getCourseId() + " 的课程。");
            return false;
        }
        if (!validateCourse(updatedCourse)) {
            System.out.println("[错误] 课程信息校验不通过。");
            return false;
        }
        int index = courseList.indexOf(existing.get());
        courseList.set(index, updatedCourse);
        System.out.println("[成功] 课程信息更新成功。");
        return true;
    }

    /**
     * 按课程编号删除
     */
    public boolean deleteCourse(String courseId) {
        Optional<Course> course = findByCourseId(courseId);
        if (!course.isPresent()) {
            System.out.println("[错误] 未找到指定课程。");
            return false;
        }
        courseList.remove(course.get());
        System.out.println("[成功] 课程删除成功。");
        return true;
    }

    /**
     * 按编号查找
     */
    public Optional<Course> findByCourseId(String courseId) {
        return courseList.stream()
                .filter(c -> c.getCourseId().equals(courseId))
                .findFirst();
    }

    /**
     * 按课程名模糊查找
     */
    public List<Course> findByName(String keyword) {
        return courseList.stream()
                .filter(c -> c.getCourseName().contains(keyword))
                .collect(Collectors.toList());
    }

    /**
     * 按课程类别查找
     */
    public List<Course> findByCategory(String category) {
        return courseList.stream()
                .filter(c -> c.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    /**
     * 按任课教师查找
     */
    public List<Course> findByTeacher(String teacher) {
        return courseList.stream()
                .filter(c -> c.getTeacher().equals(teacher))
                .collect(Collectors.toList());
    }

    /**
     * 按学期查找
     */
    public List<Course> findBySemester(String semester) {
        return courseList.stream()
                .filter(c -> c.getSemester().equals(semester))
                .collect(Collectors.toList());
    }

    /**
     * 获取所有课程
     */
    public List<Course> getAllCourses() {
        return new ArrayList<>(courseList);
    }

    /**
     * 按学分降序排列课程
     */
    public List<Course> sortByCreditDesc() {
        return courseList.stream()
                .sorted(Comparator.comparingDouble(Course::getCredit).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 统计某类别课程的总学分
     */
    public double sumCreditsByCategory(String category) {
        return courseList.stream()
                .filter(c -> c.getCategory().equals(category))
                .mapToDouble(Course::getCredit)
                .sum();
    }

    /**
     * 统计所有课程的总学分
     */
    public double sumAllCredits() {
        return courseList.stream()
                .mapToDouble(Course::getCredit)
                .sum();
    }

    /**
     * 获取所有课程类别
     */
    public List<String> getAllCategories() {
        return courseList.stream()
                .map(Course::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 课程信息校验
     */
    private boolean validateCourse(Course course) {
        if (course.getCourseId() == null || course.getCourseId().trim().isEmpty()) {
            System.out.println("[校验失败] 课程编号不能为空。");
            return false;
        }
        if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) {
            System.out.println("[校验失败] 课程名称不能为空。");
            return false;
        }
        if (course.getCredit() <= 0 || course.getCredit() > 10) {
            System.out.println("[校验失败] 学分须在0.5-10之间。");
            return false;
        }
        if (course.getClassHours() <= 0 || course.getClassHours() > 200) {
            System.out.println("[校验失败] 学时须在1-200之间。");
            return false;
        }
        if (course.getMaxStudents() <= 0) {
            System.out.println("[校验失败] 最大选课人数须大于0。");
            return false;
        }
        return true;
    }

    /**
     * 测试主方法
     */
    public static void main(String[] args) {
        course_CourseManager manager = new course_CourseManager();

        Course c1 = new Course("C001", "Java程序设计", "专业必修", 4.0, 64,
                "王教授", "2024-2025-1", 60, "面向对象编程基础课程");
        Course c2 = new Course("C002", "数据结构", "专业必修", 3.5, 56,
                "李教授", "2024-2025-1", 60, "常用数据结构与算法");
        Course c3 = new Course("C003", "软件工程", "专业必修", 3.0, 48,
                "赵教授", "2024-2025-2", 60, "软件开发过程与方法论");
        Course c4 = new Course("C004", "大学英语", "公共必修", 2.0, 32,
                "钱老师", "2024-2025-1", 80, "大学英语综合课程");
        Course c5 = new Course("C005", "羽毛球", "公共选修", 1.0, 16,
                "孙老师", "2024-2025-1", 30, "体育选修课");

        manager.addCourse(c1);
        manager.addCourse(c2);
        manager.addCourse(c3);
        manager.addCourse(c4);
        manager.addCourse(c5);

        System.out.println("\n===== 所有课程（按学分降序）=====");
        manager.sortByCreditDesc().forEach(System.out::println);

        System.out.println("\n===== 专业必修课列表 =====");
        manager.findByCategory("专业必修").forEach(System.out::println);

        System.out.println("\n===== 课程类别汇总 =====");
        for (String cat : manager.getAllCategories()) {
            System.out.printf("%s → 总学分：%.1f%n", cat, manager.sumCreditsByCategory(cat));
        }

        System.out.println("\n所有课程总学分：" + manager.sumAllCredits());
    }
}
