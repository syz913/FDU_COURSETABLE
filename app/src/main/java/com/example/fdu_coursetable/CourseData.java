package com.example.fdu_coursetable;

import android.app.Application;
import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseData extends Application {
    public static List<Course> courseList;
    public static Map<String, Integer> courseName;
    public static StringBuilder cookieStr;
    public static String ids;

    public static class Course {
        String teacherId;
        String teacherName;
        String courseId;
        String courseName;
        String RoomId;
        String RoomName;
        String vaildWeek;
        int startRow;
        int endRow;
        int col;
    }

    static {
        courseList = new ArrayList<Course>();
        courseName = new HashMap<String, Integer>();
        cookieStr = new StringBuilder();
        ids = "";
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
