package com.example.fdu_coursetable;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.FloatingActionButton;

import com.example.fdu_coursetable.Utils.DialogUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.CookieStore;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;

import static android.text.TextUtils.split;
import static com.example.fdu_coursetable.CourseData.cookieStr;

public class MainActivity extends AppCompatActivity {
    private EditText username;
    private EditText password;
    private Button login;
    private String str_username;
    private String str_password;
    private SharedHelper sh;
    private Context mContext;
    private OkHttpClient okHttpClient;

    private String lt, dllt, execution, _eventId, rmShown, ids;
    private HttpUrl url_1, url_2;
    private List<CourseData.Course> courseList = new ArrayList<CourseData.Course>();
    private List<String> name = new ArrayList<String>();

    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        sh = new SharedHelper(mContext);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC); // set logger

        okHttpClient = new OkHttpClient()
                .newBuilder()
                .followRedirects(false)//禁止OkHttp的重定向操作，我们自己处理重定向
                .followSslRedirects(false)
                .cookieJar(new CookieJar() {
                    //自定义
                    private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<HttpUrl, List<Cookie>>();

                    //复写
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        cookieStore.put(url, cookies);
                    }

                    //复写
                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(url);
                        return cookies != null ? cookies : new ArrayList<Cookie>();
                    }
                })//为OkHttp设置自动携带Cookie的功能
                .build();
        getLt();
        bindViews();
    }

    private void bindViews() {
        username = (EditText) findViewById(R.id.account);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);

        username.setText(sh.read().get("username"));
        password.setText(sh.read().get("password"));
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str_username = username.getText().toString();
                str_password = password.getText().toString();
                sh.save(str_username, str_password);
                Login();
                dialog = DialogUtils.createLoadingDialog(MainActivity.this, "获取课表中...");
            }
        });
    }

    private void getLt() {
        final Request request = new Request.Builder()
                .url("http://uis.fudan.edu.cn/authserver/login?service=http%3A%2F%2Fjwfw.fudan.edu.cn%2Feams%2Flogin.action")
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
                        Toast.makeText(MainActivity.this, "获取Lt失败，网络问题" + errdata, Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                Document doc = Jsoup.parse(response.body().string());
                lt = doc.select("input[name=lt]").val();
                dllt = doc.select("input[name=dllt]").val();
                execution = doc.select("input[name=execution]").val();
                _eventId = doc.select("input[name=_eventId]").val();
                rmShown = doc.select("input[name=rmShown]").val();
                System.out.println("lt:" + lt);
                List<Cookie> cookies = Cookie.parseAll(request.url(), response.headers());
                if (cookies != null) {
                    //存储到Cookie管理器中
                    okHttpClient.cookieJar().saveFromResponse(request.url(), cookies);
                }
                System.out.println(cookies);
            }
        });
    }

    private void Login() {
        RequestBody req_uis_body = new FormBody.Builder()
                .add("username", str_username)
                .add("password", str_password)
                .add("lt", lt)
                .add("dllt", dllt)
                .add("execution", execution)
                .add("_eventId", _eventId)
                .add("rmShown", rmShown)
                .build();
        try {
            Log.e("哈哈哈哈", getParamContent(req_uis_body));
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Request request = new Request.Builder()
                .url("http://uis.fudan.edu.cn/authserver/login?service=http%3A%2F%2Fjwfw.fudan.edu.cn%2Feams%2Flogin.action")
                .post(req_uis_body)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36")
                .addHeader("Connection", "keep-alive")
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
                        Toast.makeText(MainActivity.this, "登录失败，网络问题" + errdata, Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                System.out.println(str_password);
                if (response.code() == 200) { // 登录失败，提示重新输入账号密码
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //放在UI线程弹Toast
                            Toast.makeText(MainActivity.this, "账号或密码错误", Toast.LENGTH_SHORT).show();
                            DialogUtils.closeDialog(dialog);
                        }
                    });
                } else if (response.code() == 302) { //登录成功后会重定向三次
                    List<Cookie> cookie = Cookie.parseAll(request.url(), response.headers());
                    if (cookie != null) {
                        //存储到Cookie管理器中
                        url_1 = request.url();
                        okHttpClient.cookieJar().saveFromResponse(request.url(), cookie);
                    }

                    Request redirect_1 = new Request.Builder().url(response.header("Location"))
                            .build();
                    Response response_1 = okHttpClient.newCall(redirect_1).execute();
                    List<Cookie> cookies = Cookie.parseAll(redirect_1.url(), response_1.headers());
                    if (cookies != null) {
                        //存储到Cookie管理器中
                        url_2 = redirect_1.url();
                        okHttpClient.cookieJar().saveFromResponse(redirect_1.url(), cookies);
                    }

                    Request redirect_2 = new Request.Builder().url(response_1.header("Location"))
                            .build();
                    okHttpClient.newCall(redirect_2).execute();
                    Response response_2 = okHttpClient.newCall(redirect_2).execute();

                    Request redirect_3 = new Request.Builder().url(response_1.header("Location"))
                            .build();
                    okHttpClient.newCall(redirect_3).execute();
                    Response response_3 = okHttpClient.newCall(redirect_3).execute();

                    Document doc = Jsoup.parse(response_3.body().string());

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //放在UI线程弹Toast
                            List<Cookie> cookies = okHttpClient.cookieJar().loadForRequest(url_1);
                            for (Cookie cookie : cookies) {
                                cookieStr.append(cookie.name()).append("=").append(cookie.value()).append(";");
                            }
                            List<Cookie> cookies_2 = okHttpClient.cookieJar().loadForRequest(url_2);
                            for (Cookie cookie : cookies_2) {
                                cookieStr.append(cookie.name()).append("=").append(cookie.value()).append(";");
                            }
                            Log.e("cookies:", cookieStr.toString());
                            getIds();
//                            getCourseTable();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //放在UI线程弹Toast
                            Toast.makeText(MainActivity.this, String.format("未处理情况 response code: %d", response.code()), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void getIds() {
        final Request request = new Request.Builder()
                .url("http://jwfw.fudan.edu.cn/eams/courseTableForStd.action?_=1567869271962")
                .get()
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
                        Toast.makeText(MainActivity.this, "获取Ids失败，网络问题" + errdata, Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                Document doc = Jsoup.parse(response.body().string());
                String script = doc.getElementsByTag("script").last().toString();
                String[] strings = split(script, "bg.form.addInput");
                Pattern pattern = Pattern.compile("[^0-9]");//获取script中的数字
                Matcher matcher = pattern.matcher(strings[1]);
                String result = matcher.replaceAll("").trim();
                ids = result;
                CourseData.ids = ids;
                System.out.println("ids:" + result);
                getCourseTable();
            }
        });
    }

    private void getCourseTable() {
        RequestBody req_uis_body = new FormBody.Builder()
                .add("ignoreHead", "1")
                .add("setting.kind", "std")
                .add("startWeek", "1")
                .add("semester.id", "324")
                .add("ids", ids)
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
                        Toast.makeText(MainActivity.this, "获取课表失败，网络问题" + errdata, Toast.LENGTH_SHORT).show();
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
                    startActivity(new Intent(MainActivity.this, CourseTable.class));
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //放在UI线程弹Toast
                            Toast.makeText(MainActivity.this, "获取课表失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private String getParamContent(RequestBody body) throws IOException {
        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        return buffer.readUtf8();
    }
}