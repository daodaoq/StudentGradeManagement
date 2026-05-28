/**
 * 学生信息管理模块
 * 功能：学生基本信息的增删改查、数据校验、信息展示
 * 开发者：马新皓
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 学生实体类
 */
class Student {
    private String studentId;
    private String name;
    private String gender;
    private int age;
    private String className;
    private String phone;
    private String email;
    private String enrollmentYear;

    public Student() {}

    public Student(String studentId, String name, String gender, int age,
                   String className, String phone, String email, String enrollmentYear) {
        this.studentId = studentId;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.className = className;
        this.phone = phone;
        this.email = email;
        this.enrollmentYear = enrollmentYear;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEnrollmentYear() { return enrollmentYear; }
    public void setEnrollmentYear(String enrollmentYear) { this.enrollmentYear = enrollmentYear; }

    @Override
    public String toString() {
        return String.format("学号：%s | 姓名：%s | 性别：%s | 年龄：%d | 班级：%s | 手机：%s",
                studentId, name, gender, age, className, phone);
    }
}

/**
 * 学生信息管理器
 * 负责学生数据的增删改查及数据校验
 */
public class student_StudentManager {

    private List<Student> studentList;

    public student_StudentManager() {
        this.studentList = new ArrayList<>();
    }

    /**
     * 添加学生信息
     * @return true-添加成功, false-信息校验不通过
     */
    public boolean addStudent(Student student) {
        if (!validateStudent(student)) {
            System.out.println("[错误] 学生信息校验不通过，请检查输入数据。");
            return false;
        }
        if (findByStudentId(student.getStudentId()).isPresent()) {
            System.out.println("[错误] 学号 " + student.getStudentId() + " 已存在。");
            return false;
        }
        studentList.add(student);
        System.out.println("[成功] 学生 " + student.getName() + " 信息添加成功。");
        return true;
    }

    /**
     * 更新学生信息
     */
    public boolean updateStudent(Student updatedStudent) {
        Optional<Student> existing = findByStudentId(updatedStudent.getStudentId());
        if (!existing.isPresent()) {
            System.out.println("[错误] 未找到学号为 " + updatedStudent.getStudentId() + " 的学生。");
            return false;
        }
        if (!validateStudent(updatedStudent)) {
            System.out.println("[错误] 更新的学生信息校验不通过。");
            return false;
        }
        int index = studentList.indexOf(existing.get());
        studentList.set(index, updatedStudent);
        System.out.println("[成功] 学生 " + updatedStudent.getName() + " 信息更新成功。");
        return true;
    }

    /**
     * 根据学号删除学生
     */
    public boolean deleteStudent(String studentId) {
        Optional<Student> student = findByStudentId(studentId);
        if (!student.isPresent()) {
            System.out.println("[错误] 未找到学号为 " + studentId + " 的学生。");
            return false;
        }
        studentList.remove(student.get());
        System.out.println("[成功] 学生信息删除成功。");
        return true;
    }

    /**
     * 按学号查找学生
     */
    public Optional<Student> findByStudentId(String studentId) {
        return studentList.stream()
                .filter(s -> s.getStudentId().equals(studentId))
                .findFirst();
    }

    /**
     * 按姓名模糊查找
     */
    public List<Student> findByName(String name) {
        return studentList.stream()
                .filter(s -> s.getName().contains(name))
                .collect(Collectors.toList());
    }

    /**
     * 按班级查找学生列表
     */
    public List<Student> findByClassName(String className) {
        return studentList.stream()
                .filter(s -> s.getClassName().equals(className))
                .collect(Collectors.toList());
    }

    /**
     * 获取所有学生列表
     */
    public List<Student> getAllStudents() {
        return new ArrayList<>(studentList);
    }

    /**
     * 获取学生总数
     */
    public int getStudentCount() {
        return studentList.size();
    }

    /**
     * 按入学年份统计学生人数
     */
    public long countByEnrollmentYear(String year) {
        return studentList.stream()
                .filter(s -> s.getEnrollmentYear().equals(year))
                .count();
    }

    /**
     * 学生信息校验
     * 规则：学号8位数字、姓名不为空、年龄10-60、手机号11位、邮箱格式正确
     */
    private boolean validateStudent(Student student) {
        if (student.getStudentId() == null || !Pattern.matches("\\d{8}", student.getStudentId())) {
            System.out.println("[校验失败] 学号须为8位数字。");
            return false;
        }
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            System.out.println("[校验失败] 姓名不能为空。");
            return false;
        }
        if (student.getAge() < 10 || student.getAge() > 60) {
            System.out.println("[校验失败] 年龄范围须在10-60之间。");
            return false;
        }
        if (student.getPhone() != null && !student.getPhone().isEmpty()) {
            if (!Pattern.matches("\\d{11}", student.getPhone())) {
                System.out.println("[校验失败] 手机号须为11位数字。");
                return false;
            }
        }
        if (student.getEmail() != null && !student.getEmail().isEmpty()) {
            if (!Pattern.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$", student.getEmail())) {
                System.out.println("[校验失败] 邮箱格式不正确。");
                return false;
            }
        }
        return true;
    }

    /**
     * 测试主方法
     */
    public static void main(String[] args) {
        student_StudentManager manager = new student_StudentManager();

        Student s1 = new Student("20240001", "张三", "男", 20, "软件2304", "13800138001", "zhangsan@qq.com", "2024");
        Student s2 = new Student("20240002", "李四", "女", 19, "软件2304", "13800138002", "lisi@qq.com", "2024");
        Student s3 = new Student("20240003", "王五", "男", 21, "软件2303", "13800138003", "wangwu@163.com", "2024");

        manager.addStudent(s1);
        manager.addStudent(s2);
        manager.addStudent(s3);

        System.out.println("\n===== 所有学生列表 =====");
        manager.getAllStudents().forEach(System.out::println);

        System.out.println("\n===== 按班级查找（软件2304）=====");
        manager.findByClassName("软件2304").forEach(System.out::println);

        System.out.println("\n===== 按姓名模糊查找（'张'）=====");
        manager.findByName("张").forEach(System.out::println);

        System.out.println("\n学生总数：" + manager.getStudentCount());

        s1.setPhone("13800138099");
        manager.updateStudent(s1);

        manager.deleteStudent("20240003");
        System.out.println("删除后学生总数：" + manager.getStudentCount());
    }
}
