package info.kgeorgiy.ja.kadyrov.student;

import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class StudentDB implements StudentQuery {
    public static final Comparator<Student> STUDENT_COMPARATOR = Comparator.comparing(Student::getLastName).
            thenComparing(Student::getFirstName).reversed().
            thenComparing(Student::getId);

    private <T> List<T> getStudentField(
            List<Student> students,
            Function<Student, T> getField) {
        return getStudentFieldWithCollector(students, getField, Collectors.toList());
    }

    private <T, C extends Collection<T>> C getStudentFieldWithCollector(
            List<Student> students,
            Function<Student, T> getField,
            Collector<T, ?, C> collector) {
        return students.stream()
                .map(getField)
                .collect(collector);
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getStudentField(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getStudentField(students, Student::getLastName);
    }


    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return getStudentField(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getStudentField(students, student -> student.getFirstName() + " " + student.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return getStudentFieldWithCollector(students, Student::getFirstName, Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream()
                .max(Student::compareTo)
                .map(Student::getFirstName)
                .orElse("");
    }

    private List<Student> sortStudentBy(Collection<Student> students, Comparator<Student> comparator) {
        return students.stream()
                .sorted(comparator)
                .toList();
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudentBy(students, Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudentBy(students, STUDENT_COMPARATOR);
    }

    private <T> List<Student> findStudentBy(
            Collection<Student> students,
            Function<Student, T> parameter,
            T value
    ) {
        return sortStudentsByName(students).stream()
                .filter(student -> parameter.apply(student).equals(value))
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudentBy(students, Student::getFirstName, name);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudentBy(students, Student::getLastName, name);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return findStudentBy(students, Student::getGroup, group);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return students.stream()
                .filter(student -> student.getGroup().equals(group))
                .collect(Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(Comparator.naturalOrder())));
    }
}
