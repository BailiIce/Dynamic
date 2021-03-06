package com.example.dynamic.Controller;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.dynamic.Database.Ip;
import com.example.dynamic.R;
import com.example.dynamic.Database.ButtonsInfo;
import com.example.dynamic.Database.EditTextsInfo;
import com.example.dynamic.Utils.Config;
import com.github.ybq.android.spinkit.style.Circle;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.util.List;

import es.dmoral.toasty.Toasty;


public class MainActivity extends AppCompatActivity {

    public static int count,i,k = 0;
    public static String okc;
    private float dx, dy,x,y,w,h,r,b,Y,y1,y2;
    private long lastDownTime,eventTime;
    private boolean isLongPressed=false;
    private Button[] btn = new Button[100];
    private final EditText[] editTexts = new EditText[100];
    private Button addB,clear,addEdit,changeHttp;
    private RelativeLayout relativeLayout;
    private long mLastClickTime, mThisClickTime = 0;
    private int chosedEditId;
    private List<Ip> ipList;
    private int flag = 1;
    private ProgressDialog progressDialog;
    private ProgressBar progressBar;
    private Circle circle = new Circle();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent=getIntent();
        okc=intent.getStringExtra("okc");
        addB=(Button) findViewById(R.id.button1);
        clear=(Button) findViewById(R.id.button2);
        addEdit=(Button) findViewById(R.id.button4);
        changeHttp = (Button) findViewById(R.id.button3);
        progressBar = (ProgressBar)findViewById(R.id.spin_kit);
        progressBar.setIndeterminateDrawable(circle);
        showProgressDialog();
        init();
    }

    private void init(){
        LitePal.getDatabase();
        relativeLayout = (RelativeLayout) findViewById(R.id.Rela);
        final LayoutInflater inflater = getLayoutInflater();
        final View dialogIP = inflater.inflate(R.layout.layout_ip,(ViewGroup) findViewById(R.id.dialog));
        final EditText editIP = (EditText) dialogIP.findViewById(R.id.ipText);
        final EditText editPort = (EditText) dialogIP.findViewById(R.id.portText);
        ipList = DataSupport.findAll(Ip.class);
        if (ipList.size() > 0){
            for (Ip ips : ipList){
                Config.ADDRESS = ips.getIp();
                Config.PORT = ips.getPort();
            }
        }
        editIP.setText(Config.ADDRESS);
        editPort.setText(String.valueOf(Config.PORT == 0 ? "" : Config.PORT));
        changeHttp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("aaa" ,"aaa " + Config.ADDRESS);
                Log.d("aaa" ,"bbb " + Config.PORT);
                editIP.setText(Config.ADDRESS);
                editPort.setText(String.valueOf(Config.PORT == 0 ? "" : Config.PORT));

                if (dialogIP.getParent() != null){
                    ((ViewGroup) dialogIP.getParent()).removeView(dialogIP);
                }
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setView(dialogIP);
                dialog.setTitle("请输入服务器IP地址和端口号");
                dialog.setIcon(R.drawable.b_http);
                dialog.setCancelable(true);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (flag == 1){
                            if (editIP.getText().toString().equals("")
                                    || editPort.getText().toString().equals("")){
                                Toasty.warning(MainActivity.this, "IP地址或端口号不能为空", Toast.LENGTH_SHORT, true).show();
                            }else if (!(editPort.getText().toString().matches("[0-9]+"))){
                                Toasty.warning(MainActivity.this, "端口号格式有误", Toast.LENGTH_SHORT, true).show();
                            }
                        }
                    }
                });
                dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editIP.getText().toString().equals("")
                                || editPort.getText().toString().equals("")){
                            Toasty.warning(MainActivity.this, "IP地址或端口号不能为空", Toast.LENGTH_SHORT, true).show();
                            //这里flag不设置为0为了提醒用户设置失败
                            return;
                        }else if (!(editPort.getText().toString().matches("[0-9]+"))){
                            Toasty.warning(MainActivity.this, "端口号格式有误", Toast.LENGTH_SHORT, true).show();
                            return;
                        }
                        //设置成功
                        flag = 0;
                        DataSupport.deleteAll(Ip.class);
                        Config.ADDRESS = editIP.getText().toString().replaceAll(" ","");
                        Config.PORT = Integer.parseInt(editPort.getText().toString());
                        Toasty.success(MainActivity.this, "设置成功", Toast.LENGTH_SHORT, true).show();
                        Ip ip = new Ip();
                        ip.setIp(Config.ADDRESS);
                        ip.setPort(Config.PORT);
                        ip.save();
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                dialog.show();
            }
        });
        addB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i++;
                count=relativeLayout.getChildCount();
                relativeLayout.addView(createButton());
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count = relativeLayout.getChildCount();
                //研究整个LAYOUT布局，第0位的是add
                //因此，在remove的时候，只能操作的是0<location<count-3这个范围的
                //在执行每次remove时，我们从count-2的位置即textview上面的那个控件开始删除~
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("");
                dialog.setMessage("确认初始化界面和设置？一旦确认无法撤回");
                dialog.setCancelable(false);
                dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        relativeLayout.removeViews(4,count-4);
                        DataSupport.deleteAll(ButtonsInfo.class);
                        DataSupport.deleteAll(EditTextsInfo.class);
                        DataSupport.deleteAll(Ip.class);
                        i = k = 0;
                        Config.ADDRESS = "";
                        Config.PORT = 0;
                        flag = 1;
                        Toasty.success(MainActivity.this,"初始化成功",Toast.LENGTH_SHORT,true).show();
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                dialog.show();

            }

        });

        addEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                k++;
                count=relativeLayout.getChildCount();
                relativeLayout.addView(createEditText());
            }
        });


        //初始化界面
        List<ButtonsInfo> buttonInfos = DataSupport.findAll(ButtonsInfo.class);
        for (ButtonsInfo buttonInfo : buttonInfos){
            relativeLayout.addView(initButton(buttonInfo.getbId(),buttonInfo.getbText(),
                    buttonInfo.getbWidth(),buttonInfo.getbHeight(),buttonInfo.getB_X(),
                    buttonInfo.getB_Y(),buttonInfo.getbColor()));
        }
        Log.d("da","max button id = " + DataSupport.max(ButtonsInfo.class,"bId",int.class));
        i = DataSupport.max(ButtonsInfo.class,"bId",int.class);

        List<EditTextsInfo> editTextsInfos = DataSupport.findAll(EditTextsInfo.class);
        for (EditTextsInfo editTextsInfo : editTextsInfos){
            relativeLayout.addView(initEditText(editTextsInfo.geteId(),editTextsInfo.geteText(),
                    editTextsInfo.getE_X(),editTextsInfo.getE_Y()));
        }
        Log.d("da","max edittext id = " + DataSupport.max(EditTextsInfo.class,"eId",int.class));
        k = DataSupport.max(EditTextsInfo.class,"eId",int.class);
    }

    private View initButton(final int id, String text, int width, int height,
                            float b_x, float b_y,int color){

        Log.d("da","??????? " + text);
        btn[id] = new Button(this);
        btn[id].setId(id);
        Log.d("da","Button id is " + btn[id].getId());
        if (text != null){
            btn[id].setText(text);
        } else btn[id].setText("点击修改属性");
        if (width * height == 0){
            Log.d("da","PPPPPP");
            btn[id].setWidth(250);
            btn[id].setHeight(100);
            //将长宽信息存储到数据库
            ButtonsInfo buttonInfo = new ButtonsInfo();
            buttonInfo.setbWidth(250);
            buttonInfo.setbHeight(100);
            buttonInfo.updateAll("bId = ?",Integer.toString(id));
        }else {
            btn[id].setWidth(width);
            btn[id].setHeight(height);
        }
        btn[id].setX(b_x);
        btn[id].setY(b_y);
        if (color == 0){
            btn[id].setBackgroundColor(Color.parseColor("#CCCCCC"));
            ButtonsInfo buttonInfo = new ButtonsInfo();
            buttonInfo.setbColor(Color.parseColor("#CCCCCC"));
            buttonInfo.updateAll("bId = ?",Integer.toString(id));
        }else btn[id].setBackgroundColor(color);


        btn[id].setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                Log.d("da","被点击的是 " + id);

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    dx = motionEvent.getX();
                    dy = motionEvent.getY();
                    w=view.getWidth();
                    h=view.getHeight();
                    Y=view.getY();
                    y1=motionEvent.getRawY();
                    lastDownTime=motionEvent.getDownTime();

                }
                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    x=motionEvent.getX();
                    y=motionEvent.getY();
                    r=view.getRight();
                    b=view.getBottom();
                    eventTime=motionEvent.getEventTime();
                    //检测是否长按,在非长按时检测
                    if (!isLongPressed) {
                        isLongPressed = isLongPressed(dx, dy, x, y, lastDownTime, eventTime, 1000);
                    }
                    if (isLongPressed) {
                        //长按模式所做的事
                        y2=motionEvent.getRawY();
                        view.setX(motionEvent.getRawX() - dx);
                        view.setY(Y+y2-y1);
                        //存储到数据库
                        ButtonsInfo buttonInfo = new ButtonsInfo();
                        buttonInfo.setB_X(motionEvent.getRawX() - dx);
                        buttonInfo.setB_Y(Y+y2-y1);
                        buttonInfo.updateAll("bId = ?",Integer.toString(id));

                    }
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    isLongPressed = false;
                }
                return false;
            }
        });



        btn[id].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ButtonActivity.class);
                int id = v.getId();
                intent.putExtra("id",id);
                startActivityForResult(intent,1);
                Toast.makeText(MainActivity.this,"ok",Toast.LENGTH_SHORT).show();
            }
        });
        return btn[id];
    }

    private View initEditText(final int id, String text, float e_x, float e_y){

        editTexts[id] = new EditText(this);
        editTexts[id].setId(id);
        Log.d("da","EditText id is " + editTexts[id].getId());
        editTexts[id].setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        if (text != null){
            if (text.equals("")){
                editTexts[id].setHint("请输入文本");
                editTexts[id].setHintTextColor(Color.LTGRAY);
            }else editTexts[id].setText(text);
        }else {
            Log.d("da",".....");
            editTexts[id].setHint("请输入文本");
            editTexts[id].setHintTextColor(Color.LTGRAY);
        }
        editTexts[id].setX(e_x);
        editTexts[id].setY(e_y);



        editTexts[id].addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                EditTextsInfo editTextsInfo = new EditTextsInfo();
                editTextsInfo.seteText(s.toString());
                editTextsInfo.updateAll("eId = ?",Integer.toString(chosedEditId));
            }
        });


        //获取被选中的id
        editTexts[id].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d("da","被选中的是 " + v.getId());
                chosedEditId = v.getId();
            }
        });


        editTexts[id].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d("da","被点击的是 " + v.getId());
                mThisClickTime = mLastClickTime;
                mLastClickTime = System.currentTimeMillis();
                if (mLastClickTime - mThisClickTime < 500){
                    mThisClickTime = mLastClickTime = 0;
                    //双击事件
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("");
                    dialog.setMessage("确认删除该控件?");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            relativeLayout.removeView(v);
                            DataSupport.deleteAll(EditTextsInfo.class,"eId = ?",Integer.toString(v.getId()));
                        }
                    });
                    dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    dialog.show();
                }
            }
        });


        editTexts[id].setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    dx = motionEvent.getX();
                    dy = motionEvent.getY();
                    w = view.getWidth();
                    h = view.getHeight();
                    Y = view.getY();
                    y1 = motionEvent.getRawY();
                    lastDownTime = motionEvent.getDownTime();

                }
                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    x=motionEvent.getX();
                    y=motionEvent.getY();
                    r=view.getRight();
                    b=view.getBottom();
                    eventTime=motionEvent.getEventTime();
                    //检测是否长按,在非长按时检测
                    if (!isLongPressed) {
                        isLongPressed = isLongPressed(dx, dy, x, y, lastDownTime, eventTime, 1000);
                    }
                    if (isLongPressed) {
                        //长按模式所做的事
                        y2=motionEvent.getRawY();
                        view.setX(motionEvent.getRawX() - dx);
                        view.setY(Y+y2-y1);
                        //存储到数据库
                        EditTextsInfo editTextsInfo = new EditTextsInfo();
                        editTextsInfo.setE_X(motionEvent.getRawX() - dx);
                        editTextsInfo.setE_Y(Y+y2-y1);
                        editTextsInfo.updateAll("eId = ?",Integer.toString(id));
                    }
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    isLongPressed=false;
                }

                return false;


            }
        });

        return editTexts[id];

    }





    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        if (data == null){
            return;
        }
        else {
            switch (requestCode) {
                case 1:
                    if (resultCode == RESULT_OK) {
                        okc = data.getStringExtra("okc");
                        int w = data.getIntExtra("w",11);
                        int h = data.getIntExtra("h",12);
                        int id = data.getIntExtra("id",3);
                        int color = data.getIntExtra("chosedColor",9);
                        Log.d("da","return id is " + id);
                        Log.d("da","Button name is " + okc);
                        if (!okc.equals("")){
                            btn[id].setText(okc);
                            ButtonsInfo buttonInfo = new ButtonsInfo();
                            buttonInfo.setbText(okc);
                            buttonInfo.updateAll("bId = ?",Integer.toString(id));
                        }
                        if (w * h != 0){
                            btn[id].setWidth(w);
                            btn[id].setHeight(h);
                            //将长宽信息存储到数据库
                            ButtonsInfo buttonInfo = new ButtonsInfo();
                            buttonInfo.setbWidth(w);
                            buttonInfo.setbHeight(h);
                            buttonInfo.updateAll("bId = ?",Integer.toString(id));
                        }
                        if (color!= -131332){
                            btn[id].setBackgroundColor(color);
                            ButtonsInfo buttonInfo = new ButtonsInfo();
                            buttonInfo.setbColor(color);
                            buttonInfo.updateAll("bId = ?",Integer.toString(id));
                        }

                        break;
                    }
                    if (resultCode == RESULT_CANCELED){
                        int id=data.getIntExtra("id",4);
                        relativeLayout.removeView(btn[id]);
                        DataSupport.deleteAll(ButtonsInfo.class,"bId = ?",Integer.toString(id));
//                        btn[id].setVisibility(View.INVISIBLE);
//                        i--;
                    }
                    break;
                default:
                    break;
            }
        }
    }
    private View createButton(){

        btn[i] = new Button(this);
        btn[i].setId(i);
        Log.d("da","Button id is " + btn[i].getId());
        btn[i].setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        btn[i].setText("点击修改属性");
        Log.d("da","color is ");

        //存储到数据库
        ButtonsInfo buttonInfo = new ButtonsInfo();
        buttonInfo.setbId(i);
        buttonInfo.save();


        btn[i].setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                Log.d("da","被点击的是 " + view.getId());

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    dx = motionEvent.getX();
                    dy = motionEvent.getY();
                    w=view.getWidth();
                    h=view.getHeight();
                    Y=view.getY();
                    y1=motionEvent.getRawY();
                    lastDownTime=motionEvent.getDownTime();

                }
                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    x=motionEvent.getX();
                    y=motionEvent.getY();
                    r=view.getRight();
                    b=view.getBottom();
                    eventTime=motionEvent.getEventTime();
                    //检测是否长按,在非长按时检测
                    if (!isLongPressed) {
                        isLongPressed = isLongPressed(dx, dy, x, y, lastDownTime, eventTime, 1000);
                    }
                    if (isLongPressed) {
                        //长按模式所做的事
                        y2=motionEvent.getRawY();
                        view.setX(motionEvent.getRawX() - dx);
                        view.setY(Y+y2-y1);
                        //存储到数据库
                        ButtonsInfo buttonInfo = new ButtonsInfo();
                        buttonInfo.setB_X(motionEvent.getRawX() - dx);
                        buttonInfo.setB_Y(Y+y2-y1);
                        buttonInfo.updateAll("bId = ?",Integer.toString(view.getId()));
                    }
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    isLongPressed=false;
                }
                return false;
            }
        });
        btn[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,ButtonActivity.class);
                int id=v.getId();
                intent.putExtra("id",id);
                startActivityForResult(intent,1);
            }
        });
        return btn[i];

    }
    protected View createEditText(){

        editTexts[k] = new EditText(this);
        editTexts[k].setId(k);
        Log.d("da","EditText id is " + editTexts[k].getId());
        editTexts[k].setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        /*btn.setText(okc);*/
        editTexts[k].setHint("请输入文本");
        editTexts[k].setHintTextColor(Color.LTGRAY);

        //存储到数据库
        EditTextsInfo editTextsInfo = new EditTextsInfo();
        editTextsInfo.seteId(k);
        editTextsInfo.save();


        editTexts[k].addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                EditTextsInfo editTextsInfo = new EditTextsInfo();
                editTextsInfo.seteText(s.toString());
                editTextsInfo.updateAll("eId = ?",Integer.toString(chosedEditId));
            }
        });


        //获取被选中的id
        editTexts[k].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d("da","被选中的是 " + v.getId());
                chosedEditId = v.getId();
            }
        });


        editTexts[k].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d("da","被点击的是 " + v.getId());
                mThisClickTime = mLastClickTime;
                mLastClickTime = System.currentTimeMillis();
                if (mLastClickTime - mThisClickTime < 500){
                    mThisClickTime = mLastClickTime = 0;
                    //双击事件
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("");
                    dialog.setMessage("确认删除该控件?");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            relativeLayout.removeView(v);
                            DataSupport.deleteAll(EditTextsInfo.class,"eId = ?",Integer.toString(v.getId()));
                        }
                    });
                    dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    dialog.show();
                }
            }
        });


        editTexts[k].setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    dx = motionEvent.getX();
                    dy = motionEvent.getY();
                    w = view.getWidth();
                    h = view.getHeight();
                    Y = view.getY();
                    y1 = motionEvent.getRawY();
                    lastDownTime = motionEvent.getDownTime();

                }
                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    x=motionEvent.getX();
                    y=motionEvent.getY();
                    r=view.getRight();
                    b=view.getBottom();
                    eventTime=motionEvent.getEventTime();
                    //检测是否长按,在非长按时检测
                    if (!isLongPressed) {
                        isLongPressed = isLongPressed(dx, dy, x, y, lastDownTime, eventTime, 1000);
                    }
                    if (isLongPressed) {
                        //长按模式所做的事
                        y2=motionEvent.getRawY();
                        view.setX(motionEvent.getRawX() - dx);
                        view.setY(Y+y2-y1);
                        //存储到数据库
                        EditTextsInfo editTextsInfo = new EditTextsInfo();
                        editTextsInfo.setE_X(motionEvent.getRawX() - dx);
                        editTextsInfo.setE_Y(Y+y2-y1);
                        editTextsInfo.updateAll("eId = ?",Integer.toString(view.getId()));
                    }
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    isLongPressed=false;
                }

                return false;


            }
        });

        return editTexts[k];

    }
/*    public int getStatusBarHeight(){
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    public int getTitleBarHeight(){
        Window window = getWindow();
        int contentViewTop = getWindow()
                .findViewById(window.ID_ANDROID_CONTENT).getTop();
        // statusBarHeight是上面所求的状态栏的高度
        int titleBarHeight = contentViewTop - getStatusBarHeight();
        return titleBarHeight;
    }*/

    static boolean isLongPressed(float lastX, float lastY, float thisX,
                                 float thisY, long lastDownTime, long thisEventTime,
                                 long longPressTime) {
        float offsetX = Math.abs(thisX - lastX);
        float offsetY = Math.abs(thisY - lastY);
        long intervalTime = thisEventTime - lastDownTime;
        return (offsetX <= 10 && offsetY <= 10 && intervalTime >= longPressTime);
    }


    @Override
    public void onResume(){
        super.onResume();
        closeProgressDialog();
    }

    public void showProgressDialog(){
        if (progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setIndeterminateDrawable(circle);
            progressDialog.setMessage("正在加载数据...");
            progressDialog.setCanceledOnTouchOutside(false);

        }
        progressDialog.show();
    }

    public void closeProgressDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }









}

