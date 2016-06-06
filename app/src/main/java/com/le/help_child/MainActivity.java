package com.le.help_child;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.le.help_child.activity.MyRecordActivity;
import com.le.help_child.activity.TabActivity;
import com.le.help_child.activity.MyUploadActivity;
import com.le.help_child.activity.ToolsActivity;
import com.le.help_child.update.NetWork;
import com.le.help_child.update.VersionUpdate;
import com.le.help_child.view.SlidingMenu;
import com.squareup.leakcanary.LeakCanary;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import cn.jpush.android.api.JPushInterface;
public class MainActivity extends AppCompatActivity {
    private SlidingMenu mLeftMenu ;
    private Boolean isLogin = false;
    private String input;
    private String upload_params = null;
    private String record_params = null;
    private int money_para = 0;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LeakCanary.install(this.getApplication());
        boolean netState = NetWork.checkNetWorkStatus(MainActivity.this);
        //如果联网检测更新
        if (netState) {
            VersionUpdate manager = new VersionUpdate(MainActivity.this);// 检查软件更新
            manager.checkUpdate();
        }
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
        final TextView tv = (TextView)findViewById(R.id.textView);
        sp = getSharedPreferences("userInfo", 0);//构建
        final String name=sp.getString("USER_NAME", "");//获取用户名
        final Handler loadHandler = new Handler(){
            @Override
            public void handleMessage(Message msg)
            {
                if(msg.what==0)
                {
                   //Toast.makeText(getApplicationContext(), "未检测到该手机号的上传信息",Toast.LENGTH_SHORT).show();
                    Toast toast;
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom,(ViewGroup)findViewById(R.id.llToast));
                    TextView text = (TextView) layout.findViewById(R.id.tvTextToast);
                    text.setText("未检测到该手机号的上传信息");
                    toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.BOTTOM, 0, 20);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                }
                else if(msg.what==1)
                {
                    assert tv != null;
                    tv.setText(input);
                }
                else if(msg.what==2){
                    assert tv != null;
                    tv.setText(name);
                }
            }
        };
        final boolean choseRemember =sp.getBoolean("remember", false);//获取是否记住
        final boolean choseAutoLogin =sp.getBoolean("autologin", false);//获取是否自动登录

        //如果上次登录选了自动登录，那进入登录页面也自动勾选自动登录
        if(!name.equals("")&&choseAutoLogin){
            new Thread() {
                @Override
                public void run() {
                    //input = phone_et.getText().toString();//输入的手机号
                    //验证成功
                    String exam = check(name);
                    //这里是子线程不能控制UI！！！要向主线程发消息！！！
                    if (!exam.equals("")) {
                        loadHandler.sendEmptyMessage(2);
                        try {
                            JSONObject obj_exam = new JSONObject(exam);
                            JSONArray a = obj_exam.getJSONArray("upload");
                            upload_params = a.toString();
                            JSONArray b = obj_exam.getJSONArray("record");
                            record_params = b.toString();
                            money_para = obj_exam.getInt("money");
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        isLogin = true;
                    }
                    //验证失败
                    else {
                        loadHandler.sendEmptyMessage(0);
                    }
                }
            }.start();

        }
        ImageButton camera_imb = (ImageButton)findViewById(R.id.camera_button);
        ImageButton record_imb = (ImageButton)findViewById(R.id.record_button);
        ImageButton place_imb = (ImageButton)findViewById(R.id.place_button);
        ImageButton help_imb = (ImageButton)findViewById(R.id.help_button);
        if (camera_imb != null) {
            camera_imb.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0) {

                    Intent i = new Intent(MainActivity.this,TabActivity.class);
                    i.putExtra("para",0);
                    startActivity(i);
                }
            });
        }
        if (record_imb != null) {
            record_imb.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    Intent i = new Intent(MainActivity.this,TabActivity.class);
                    i.putExtra("para",1);
                    startActivity(i);
                }
            });
        }
        if(place_imb !=null){
            place_imb.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    Intent i = new Intent(MainActivity.this,TabActivity.class);
                    i.putExtra("para",2);
                    startActivity(i);
                }
            });
        }
        if(help_imb !=null){
            help_imb.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    Intent i = new Intent(MainActivity.this,TabActivity.class);
                    i.putExtra("para",3);

                    startActivity(i);
                }
            });
        }
        mLeftMenu = (SlidingMenu) findViewById(R.id.id_menu);

        LinearLayout mUpload = (LinearLayout) findViewById(R.id.Layout_upload);
        assert mUpload != null;
        mUpload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isLogin){
                    Intent i = new Intent(MainActivity.this,MyUploadActivity.class);
                    i.putExtra("paras",upload_params);
                    startActivity(i);
                }
                else{
                   //Toast.makeText(getApplicationContext(), "您尚未登录",Toast.LENGTH_SHORT).show();
                    Toast toast;
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom,(ViewGroup)findViewById(R.id.llToast));
                    TextView text = (TextView) layout.findViewById(R.id.tvTextToast);
                    text.setText("您尚未登录");
                    toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.BOTTOM, 0, 20);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                }
            }
        });
        LinearLayout mRecord = (LinearLayout) findViewById(R.id.Layout_record);
        assert mRecord != null;
        mRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isLogin){
                    Intent i = new Intent(MainActivity.this,MyRecordActivity.class);
                    i.putExtra("paras",record_params);
                    startActivity(i);
                }
                else{
                    //Toast.makeText(getApplicationContext(), "您尚未登录",Toast.LENGTH_SHORT).show();
                    Toast toast;
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom,(ViewGroup)findViewById(R.id.llToast));
                    TextView text = (TextView) layout.findViewById(R.id.tvTextToast);
                    text.setText("您尚未登录");
                    toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.BOTTOM, 0, 20);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                }
            }
        });
        LinearLayout muMessage = (LinearLayout) findViewById(R.id.Layout_message);
        assert muMessage != null;
        muMessage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isLogin){
//                    Intent i = new Intent(MainActivity.this,MyMessageActivity.class);
//                    // i.putExtra("paras",input);
//                    startActivity(i);
                    new  AlertDialog.Builder(MainActivity.this)
                            .setTitle("我的消息模块尚未开通！" )
                            .setMessage("为确保及时接受消息数据，请打开您的接收消息权限！")
                            .setPositiveButton("确定" , null )
                            .show();
                }
                else{
                    //Toast.makeText(getApplicationContext(), "您尚未登录",Toast.LENGTH_SHORT).show();
                    Toast toast;
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom,(ViewGroup)findViewById(R.id.llToast));
                    TextView text = (TextView) layout.findViewById(R.id.tvTextToast);
                    text.setText("您尚未登录");
                    toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.BOTTOM, 0, 20);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                }
            }
        });
        LinearLayout mMoney = (LinearLayout) findViewById(R.id.Layout_money);
        assert mMoney != null;
        mMoney.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isLogin){
//                    Intent i = new Intent(MainActivity.this, MyMoneyActivity.class);
//                    i.putExtra("paras",money_para);
//                    startActivity(i);
                    new  AlertDialog.Builder(MainActivity.this)
                            .setTitle("您的积分" )
                            .setMessage(money_para+"积分")
                            .setPositiveButton("确定" , null )
                            .show();
                }
                else{
                    //Toast.makeText(getApplicationContext(), "您尚未登录",Toast.LENGTH_SHORT).show();
                    Toast toast;
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom,(ViewGroup)findViewById(R.id.llToast));
                    TextView text = (TextView) layout.findViewById(R.id.tvTextToast);
                    text.setText("您尚未登录");
                    toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.BOTTOM, 0, 20);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                }
            }
        });
        ImageButton mytools = (ImageButton) findViewById(R.id.tools);
        assert mytools != null;
        mytools.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ToolsActivity.class);
                assert tv != null;
                i.putExtra("paras", tv.getText().toString());
                startActivity(i);
            }
        });


        //登录按钮的点击时间
        assert tv != null;
        tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String username  = tv.getText().toString();
                if(username.equals("登录")){
                    final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setView(new EditText(MainActivity.this));
                    alertDialog.show();
                    Window window = alertDialog.getWindow();
                    window.setContentView(R.layout.login_dialog);
                    final EditText phone_et =(EditText)window.findViewById(R.id.username);
                    phone_et.setFocusable(true);
                    phone_et.setFocusableInTouchMode(true);
                    // input = phone_et.getText().toString();//输入的手机号
                    final CheckBox isrem = (CheckBox)window.findViewById(R.id.is_rem);
                    final CheckBox autologin = (CheckBox)window.findViewById(R.id.is_auto);
//                    sp = getSharedPreferences("userInfo", 0);//构建
//                    final String name=sp.getString("USER_NAME", "");//获取用户名
//                    boolean choseRemember =sp.getBoolean("remember", false);//获取是否记住
//                    boolean choseAutoLogin =sp.getBoolean("autologin", false);//获取是否自动登录
                    //如果上次选了记住密码，那进入登录页面也自动勾选记住密码，并填上用户名和密码
                    if(choseRemember){
                        phone_et.setText(name);
                        assert isrem != null;
                        isrem.setChecked(true);
                    }
                    Button login = (Button)window.findViewById(R.id.login);
                    login.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new Thread() {
                                @Override
                                public void run() {
                                    input = phone_et.getText().toString();//输入的手机号
                                    //验证成功
                                    String exam = check(input);
                                    //这里是子线程不能控制UI！！！要向主线程发消息！！！
                                    if (!exam.equals("")) {
                                        JPushInterface.setAlias(MainActivity.this,input,null);//标记用户名为极光推送用户名
                                        //记录登录名
                                        SharedPreferences.Editor editor =sp.edit();
                                        editor.putString("USER_NAME",input);
                                        //是否记住密码
                                        assert isrem != null;
                                        if(isrem.isChecked()){
                                            editor.putBoolean("remember", true);
                                        }else{
                                            editor.putBoolean("remember", false);
                                        }
                                        //是否自动登录
                                        assert autologin != null;
                                        if(autologin.isChecked()){
                                            editor.putBoolean("autologin", true);
                                        }else{
                                            editor.putBoolean("autologin", false);
                                        }
                                        editor.commit();
                                        loadHandler.sendEmptyMessage(1);
                                        try {
                                            JSONObject obj_exam = new JSONObject(exam);
                                            JSONArray a = obj_exam.getJSONArray("upload");
                                            upload_params = a.toString();
                                            JSONArray b = obj_exam.getJSONArray("record");
                                            record_params = b.toString();
                                            money_para = obj_exam.getInt("money");
                                        } catch (Exception e)
                                        {
                                            e.printStackTrace();
                                        }
                                        isLogin = true;

                                    }
                                    //验证失败
                                    else {
                                        loadHandler.sendEmptyMessage(0);

                                    }
                                }
                            }.start();
                            alertDialog.dismiss();
                        }

                    });
                    Button exit = (Button)window.findViewById(R.id.exit);
                    exit.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });
                }
            }
        });
        //退出登录按钮事件
        Button out_of_login = (Button)findViewById(R.id.out_login_button);
        assert out_of_login != null;
        out_of_login.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                isLogin = false;
                tv.setText("登录");
                upload_params = "";
                record_params = "";
                money_para = 0;
            }
        });
    }

    private String check(String input) {
        String url = "http://123.57.249.60:8080/help_child_t1/MyInfo?phone="+input;
        String result="";
        try
        {
            URL getUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
            String lines;
            String line ="";
            while ((lines=reader.readLine()) != null) {
                line+=lines;
            }
            JSONObject obj = new JSONObject(line);
            String my_m = obj.getString("money");
            if(my_m.equals("")){
                result = "";
            }
            else{
                result= line;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }

    public void toggleMenu()
    {
        mLeftMenu.toggle();
    }
}
