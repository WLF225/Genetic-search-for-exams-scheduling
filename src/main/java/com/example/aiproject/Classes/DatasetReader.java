package com.example.aiproject.Classes;

import java.io.*;
import java.util.*;
import java.util.stream.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.example.aiproject.*;

// To parse the Excel dataset and produce the Course and Student arrays the GA needs
public class DatasetReader {

    // To bundle courses and students together so they can be passed around as one object
    public static class Data {

        Course[] courses;
        Student[] students;

        public Data(Course[] courses, Student[] students) {
            this.courses  = courses;
            this.students = students;
        }

        public Course[]  getCourses()  { return courses;  }
        public Student[] getStudents() { return students; }
    }

    public static Data readDataset(String filePath) throws Exception {

        Workbook workbook = new XSSFWorkbook(new FileInputStream(filePath));
        DataFormatter formatter = new DataFormatter();

        // To collect unique course codes in sorted order before building the array
        Sheet courseSheet = workbook.getSheet("Course_Catalog");
        Set<String> courseSet = new TreeSet<>();

        boolean firstRow = true;
        for (Row row : courseSheet) {
            // To skip the header row on the first iteration
            if (firstRow) { firstRow = false; continue; }

            // To read only column 0 (Course_Code) and ignore Credits and Enrollment
            Cell cell = row.getCell(0);
            if (cell != null) {
                String value = formatter.formatCellValue(cell).trim();
                if (!value.isEmpty()) courseSet.add(value);
            }
        }

        // To build the course array (TreeSet ordering gives us free alphabetical sorting)
        Course[] courses = new Course[courseSet.size()];
        int idx = 0;
        for (String c : courseSet) courses[idx++] = new Course(c);

        // To enable binary search when mapping a course name to its array index
        String[] courseNames = Arrays.stream(courses)
                .map(c -> c.name)
                .toArray(String[]::new);

        // To read each student row and convert the course names to index references
        Sheet studentSheet = workbook.getSheet("Student_Courses");
        List<Student> studentsList = new ArrayList<>();

        firstRow = true;
        for (Row row : studentSheet) {
            // To skip the header row on the first iteration
            if (firstRow) { firstRow = false; continue; }

            Cell studentCell = row.getCell(0);
            if (studentCell == null) continue;

            String studentName = formatter.formatCellValue(studentCell).trim();
            if (studentName.isEmpty()) continue;

            // To guard against POI returning -1 for completely empty rows
            if (row.getLastCellNum() < 1) continue;

            List<Integer> indexes = new ArrayList<>();

            // To skip columns 1-2 (Courses_Taken comma-list and Number_of_Courses)
            // and read individual course codes starting at column 3
            for (int i = 3; i < row.getLastCellNum(); i++) {
                Cell courseCell = row.getCell(i);
                if (courseCell != null) {
                    String courseName = formatter.formatCellValue(courseCell).trim();
                    if (!courseName.isEmpty()) {
                        int courseIndex = Arrays.binarySearch(courseNames, courseName);
                        if (courseIndex >= 0) indexes.add(courseIndex);
                    }
                }
            }

            int[] arr = indexes.stream().mapToInt(Integer::intValue).toArray();
            studentsList.add(new Student(studentName, arr));
        }

        Student[] students = studentsList.toArray(new Student[0]);
        workbook.close();

        return new Data(courses, students);
    }
}
