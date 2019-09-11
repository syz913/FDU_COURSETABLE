package com.example.fdu_coursetable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.fdu_coursetable.ScheduleView.OnItemClassClickListener;
import com.example.fdu_coursetable.Utils.DialogUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.fdu_coursetable.CourseData.cookieStr;

public class CourseTable extends AppCompatActivity {

    private ScheduleView scheduleView;
    private ArrayList<ClassInfo> classList;
    private List<CourseData.Course> courseList;

    private Dialog dialog;
    private List<String> name = new ArrayList<String>();
    private OkHttpClient okHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_table);
        scheduleView = (ScheduleView) this.findViewById(R.id.scheduleView);
        okHttpClient = new OkHttpClient()
                .newBuilder()
                .followRedirects(false)//禁止OkHttp的重定向操作，我们自己处理重定向
                .followSslRedirects(false)
                .build();
        getClassData();
        scheduleView.setClassList(classList);// 将课程信息导入到课表中
        scheduleView
                .setOnItemClassClickListener(new OnItemClassClickListener() {

                    @Override
                    public void onClick(ClassInfo classInfo) {
                        String[] teachers = classInfo.getTeacherName().split("/");
                        String[] classRooms = classInfo.getClassRoom().split("/");
                        String[] times = classInfo.getTime().split("/");
                        Toast toast;
                        String mContext = null;
                        if (teachers.length == 1) {
                            mContext = "您点击的课程是：\n" + classInfo.getClassname() + "\n\n任课教师:" + classInfo.getTeacherName()
                                    + "\n教学时间:" + handleWeek(classInfo.getTime()) + "\n教室:" + classInfo.getClassRoom();
                            toast = Toast.makeText(CourseTable.this, mContext, Toast.LENGTH_SHORT);
                        } else {
                            mContext = "您点击的课程是:\n" + classInfo.getClassname();
                            for (int i = 0; i < teachers.length; i++) {
                                mContext += "\n\n任课教师" + ":" + teachers[i]
                                        + "\n教学时间:" + handleWeek(times[i]) + "\n教室:" + classRooms[i];
                            }
                            toast = Toast.makeText(CourseTable.this, mContext, Toast.LENGTH_SHORT);
                        }
                        toast.setGravity(Gravity.CENTER, 0, 0);

//                        toast.show();
                        final AlertDialog.Builder normalDialog =
                                new AlertDialog.Builder(CourseTable.this);
                        normalDialog.setTitle("课程详细信息");
                        normalDialog.setMessage(mContext);

                        normalDialog.setNegativeButton("关闭",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //...To-do
                                    }
                                });
                        // 显示
                        normalDialog.show();
                    }
                });
    }

    //处理单双周的情况

    /**
     * 1、纯单周
     * 2、纯双周
     * 3、连续周
     * 3、混合课程
     */
    private String handleWeek(String weeks) {
        int start = 0, end = 0, start2 = 0, end2 = 0;
        //先判断属于哪种情况
        if (!weeks.contains("11") && weeks.indexOf("1") % 2 == 0) {
            //00101010101
            start = weeks.indexOf("1");
            end = weeks.lastIndexOf("1");
            return "(双)第" + start + "周-" + "第" + end + "周";
        } else if (!weeks.contains("11") && weeks.indexOf("1") % 2 == 1) {
            //010101010
            start = weeks.indexOf("1");
            end = weeks.lastIndexOf("1");
            return "(单)第" + start + "周-" + "第" + end + "周";
        } else if (!weeks.contains("101")) {
            start = weeks.indexOf("1");
            end = weeks.lastIndexOf("1");
            return "第" + start + "周-" + "第" + end + "周";
        } else {
            //混合，只考虑单双不同时出现的情况
            if (weeks.indexOf("101") < weeks.indexOf("11")) {
                start = weeks.indexOf("1");
                end = weeks.lastIndexOf("01") + 1;
                start2 = end + 1;
                end2 = weeks.lastIndexOf("1");
                if (start % 2 == 0)
                    return "(双)第" + start + "周-" + "第" + end + "周"
                            + "\n(全)第" + start2 + "周-" + "第" + end2 + "周";
                else
                    return "(单)第" + start + "周-" + "第" + end + "周"
                            + "\n(全)第" + start2 + "周-" + "第" + end2 + "周";
            } else {
                start = weeks.indexOf("1");
                end = weeks.lastIndexOf("11") + 1;
                start2 = end + 1;
                end2 = weeks.lastIndexOf("1");
                if (start2 % 2 == 0)
                    return "(全)第" + start + "周-" + "第" + end + "周"
                            + "\n(双)第" + start2 + "周-" + "第" + end2 + "周";
                else
                    return "(全)第" + start + "周-" + "第" + end + "周"
                            + "\n(单)第" + start2 + "周-" + "第" + end2 + "周";
            }

        }

    }

    private void getClassData() {
        System.out.println("课表如下:\n");
        courseList = new ArrayList<CourseData.Course>();
        courseList = CourseData.courseList;
        classList = new ArrayList<ClassInfo>();
        //排序
        Collections.sort(courseList, new Comparator<CourseData.Course>() {
            @Override
            public int compare(CourseData.Course o1, CourseData.Course o2) {
                return o2.vaildWeek.compareTo(o1.vaildWeek);  //升序
            }
        });
        for (int i = 0; i < courseList.size(); i++)
            System.out.println(courseList.get(i).courseName + "\t" + courseList.get(i).teacherName + courseList.get(i).col + "\n" +
                    courseList.get(i).vaildWeek + "\t" + "第" + courseList.get(i).startRow + "节-第" + courseList.get(i).endRow + "节\n" + "------------\n");
        for (int i = 0; i < courseList.size(); i++) {
            CourseData.Course course = courseList.get(i);
            if (!mergeCourse(course)) {
                ClassInfo classInfo = new ClassInfo();
                classInfo.setClassname(course.courseName);
                classInfo.setFromClassNum(course.startRow + 1);
                classInfo.setClassNumLen(course.endRow - course.startRow + 1);
                classInfo.setClassRoom(course.RoomName);
                classInfo.setWeekday(course.col + 1);
                classInfo.setTeacherName(course.teacherName);
                classInfo.setTime(course.vaildWeek);
                classInfo.setSingle(true);
                classInfo.setConfilct(false);
                classList.add(classInfo);
            }
        }
    }

    private Boolean mergeCourse(CourseData.Course course) {
        for (int i = 0; i < classList.size(); i++)
            if (classList.get(i).getWeekday() == course.col + 1 &&
                    classList.get(i).getFromClassNum() == course.startRow + 1) {
                ClassInfo course1 = classList.get(i);
                classList.get(i).setSingle(false);
                classList.get(i).setTeacherName(course1.getTeacherName() + "/" + course.teacherName);
                classList.get(i).setClassRoom(course1.getClassRoom() + "/" + course.RoomName);
                classList.get(i).setTime(course1.getTime() + "/" + course.vaildWeek);

                System.out.println(classList.get(i).getTeacherName());

                return true;
            }
        return false;
    }

    private void handleConflict() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId())//得到被点击的item的itemId 
        {
            case R.id.logout:
                dialog = DialogUtils.createLoadingDialog(CourseTable.this, "登出中...");
                LogOut();
                break;
            case R.id.refresh:
                dialog = DialogUtils.createLoadingDialog(CourseTable.this, "刷新中...");
                reFresh();
                Toast.makeText(CourseTable.this, "刷新成功!", Toast.LENGTH_SHORT).show();
            default:
                break;
        }
        return true;
    }

    private void LogOut() {
        final Request request = new Request.Builder()
                .url("http://jwfw.fudan.edu.cn/eams/logout.action")
                .get()
                .addHeader("Cookie", cookieStr.toString())
                .build();

        final Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                final String errdata = e.toString();
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //放在UI线程弹Toast
                        System.out.println(errdata);
                        Toast.makeText(CourseTable.this, "登出失败，网络问题" + errdata, Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.code() == 200) { // 登录失败，提示重新输入账号密码
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //放在UI线程弹Toast
                            Toast.makeText(CourseTable.this, "账号或密码错误", Toast.LENGTH_SHORT).show();
                            DialogUtils.closeDialog(dialog);
                        }
                    });
                } else if (response.code() == 302) { //成功后会重定向三次

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(CourseTable.this, MainActivity.class));
                            Toast.makeText(CourseTable.this, "登出成功！", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //放在UI线程弹Toast
                            Toast.makeText(CourseTable.this, String.format("未处理情况 response code: %d", response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void reFresh() {
        RequestBody req_uis_body = new FormBody.Builder()
                .add("ignoreHead", "1")
                .add("setting.kind", "std")
                .add("startWeek", "1")
                .add("semester.id", "324")
                .add("ids", CourseData.ids)
                .build();
        final Request request = new Request.Builder()
                .url("http://jwfw.fudan.edu.cn/eams/courseTableForStd!courseTable.action")
                .post(req_uis_body)
                .addHeader("Cookie", "semester.id=284;" + cookieStr.toString())
                .build();

        final Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                final String errdata = e.toString();
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //放在UI线程弹Toast
                        System.out.println(errdata);
                        Toast.makeText(CourseTable.this, "获取课表失败，网络问题" + errdata, Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.code() == 200) {
                    Document doc = Jsoup.parse(response.body().string());
                    String regex = "new TaskActivity(.*?);";//这样不能匹配换行符
                    String regex_2 = "new TaskActivity([\\s\\S]*?)(activity =|table0.marshalTable)";
                    Pattern pattern = Pattern.compile(regex);
                    Pattern pattern1 = Pattern.compile(regex_2);
                    Matcher matcher = pattern.matcher(doc.getElementsByTag("script").toString());
                    Matcher matcher1 = pattern1.matcher(doc.getElementsByTag("script").toString());
                    while (matcher.find()) {
                        int i = 1;
                        String cour = matcher.group(i);
                        cour = cour.substring(1, cour.length() - 2);
//                    cour = cour.replaceAll("\"", "");
                        String[] list = cour.split("\",\"");
                        CourseData.Course course = new CourseData.Course();
                        course.teacherId = list[0];
                        course.teacherName = list[1];
                        course.courseId = list[2];
                        course.courseName = list[3];
                        course.RoomId = list[4];
                        course.RoomName = list[5];
//                    course.vaildWeek = handleWeek(list[6]);
                        //handleWeek暂时有Bug
                        course.vaildWeek = list[6];
                        courseList.add(course);

                        name.add(list[3]);

                        i++;
                    }
                    int k = 0;
                    while (matcher1.find()) {
                        int i = 1;
                        String cour = matcher1.group(i);
                        String regex_3 = "index =([\\s\\S]*?)\\*unitCount";
                        String regex_4 = "\\+([\\s\\S]*?);";

                        Pattern pattern2 = Pattern.compile(regex_3);
                        Pattern pattern3 = Pattern.compile(regex_4);

                        Matcher matcher2 = pattern2.matcher(cour);
                        Matcher matcher3 = pattern3.matcher(cour);
                        while (matcher2.find()) {
                            int j = 1;
                            String course = matcher2.group(j);
                            courseList.get(k).col = Integer.parseInt(course);
                            j++;
                        }
                        List<String> list = new ArrayList<String>();
                        while (matcher3.find()) {
                            int j = 1;
                            String course = matcher3.group(j);
                            list.add(course);
                            j++;
                        }
                        courseList.get(k).startRow = Integer.parseInt(list.get(0));
                        courseList.get(k).endRow = Integer.parseInt(list.get(list.size() - 1));
                        i++;
                        k++;
                    }
                    name = new ArrayList<>(new HashSet<>(name));
                    for (int i = 0; i < name.size(); i++)
                        CourseData.courseName.put(name.get(i), i);
                    CourseData.courseList = courseList;
                    DialogUtils.closeDialog(dialog);
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //放在UI线程弹Toast
                            Toast.makeText(CourseTable.this, "刷新成功!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //放在UI线程弹Toast
                            Toast.makeText(CourseTable.this, "刷新失败!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }
}
