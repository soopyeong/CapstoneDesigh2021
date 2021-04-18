package robot.jangts.capstonedesigh2021;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    ImageButton btn__connect; // 연결 버튼
    TextView textView_ip, textView_gas;

    ImageButton btn_up, btn_down, btn_right, btn_left; // 로봇 조종 버튼
    ImageButton btn_cam_up, btn_cam_down;

    private Handler mHandler;

    private Socket socket;

    private DataOutputStream dos;
    private DataInputStream dis;

    private String ip;
    private int port = 9999; // 소켓통신 포트

    WebView webView;
    //String url = "http://192.168.0.225:8091/?action=stream";
    String url = "http://www.google.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        btn__connect = findViewById(R.id.btn__connect);
        textView_ip = findViewById(R.id.textView_ip);
        textView_gas = findViewById(R.id.textView_gas);

        btn_up = findViewById(R.id.btn__up);
        btn_down = findViewById(R.id.btn__down);
        btn_right = findViewById(R.id.btn__right);
        btn_left = findViewById(R.id.btn__left);
        btn_cam_up = findViewById(R.id.btn__camup);
        btn_cam_down = findViewById(R.id.btn__camdown);

        btn_up.setOnTouchListener(RobotButtonListener);
        btn_down.setOnTouchListener(RobotButtonListener);
        btn_right.setOnTouchListener(RobotButtonListener);
        btn_left.setOnTouchListener(RobotButtonListener);

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClientClass());

    }

    void btn_up() {  // 전진버튼 스레드
        Thread Btn_up = new Thread() {
            public void run() {
                try {
                    dos.writeUTF("front");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Btn_up.start();
    }

    void btn_down() {  // 후진버튼 스레드
        Thread Btn_down = new Thread() {
            public void run() {
                try {
                    dos.writeUTF("back");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Btn_down.start();
    }

    void btn_right() {  // 우회전버튼 스레드
        Thread Btn_right = new Thread() {
            public void run() {
                try {
                    dos.writeUTF("right");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Btn_right.start();
    }

    void btn_left() {  // 좌회전버튼 스레드
        Thread Btn_left = new Thread() {
            public void run() {
                try {
                    dos.writeUTF("left");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Btn_left.start();
    }

    void btn_stop() {  // 정지버튼 스레드
        Thread Btn_stop = new Thread() {
            public void run() {
                try {
                    dos.writeUTF("stop");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Btn_stop.start();
    }

    private View.OnTouchListener RobotButtonListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                switch (v.getId()) {
                    case R.id.btn__up:
                        btn_up();
                        break;
                    case R.id.btn__down:
                        btn_down();
                        break;
                    case R.id.btn__left:
                        btn_left();
                        break;
                    case R.id.btn__right:
                        btn_right();
                        break;
                }
            }
            else if(event.getAction() == MotionEvent.ACTION_UP) {
                btn_stop();
            }
            return false;
        }
    };

    private class WebViewClientClass extends WebViewClient { // 웹뷰 관련 클래스
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { // 웹뷰 관련(뒤로가기 버튼 관련)
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void Connecting(View v) { // 팝업 창(소켓통신 연결)
       AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("소켓통신 연결");
        alert.setMessage("IP주소를 입력해주세요.");

        final EditText name = new EditText(this);
        alert.setView(name);

        alert.setPositiveButton("connect", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ip = name.getText().toString();
                textView_ip.setText(ip);
                connect();
            }
        });

        alert.setNegativeButton("cancel",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.show();
    }

    void connect(){
        mHandler = new Handler();
        Log.w("connect","연결 하는중");
        // 받아오는거
        Thread checkUpdate = new Thread() {
            public void run() {
                // ip받기
                String newip = ip;

                // 서버 접속
                try {
                    socket = new Socket(newip, port);
                    Log.w("서버 접속됨", "서버 접속됨");
                } catch (IOException e1) {
                    Log.w("서버접속못함", "서버접속못함");
                    e1.printStackTrace();
                }

                Log.w("edit 넘어가야 할 값 : ","안드로이드에서 서버로 연결요청");

                try {
                    dos = new DataOutputStream(socket.getOutputStream());   // output에 보낼꺼 넣음
                    dis = new DataInputStream(socket.getInputStream());     // input에 받을꺼 넣어짐
                    dos.writeUTF("안드로이드에서 서버로 연결요청");

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.w("버퍼", "버퍼생성 잘못됨");
                }
                Log.w("버퍼","버퍼생성 잘됨");

                while(true) {
                    try {
                        String sensor = "";
                        while (true) {
                            sensor = dis.readUTF();

                            Log.w("------서버에서 받아온 값 ", "" + sensor);

                            /*if (line2 == 99) {
                                Log.w("------서버에서 받아온 값 ", "" + line2);
                                socket.close();
                                break;
                            }*/
                        }
                    } catch (Exception e) {

                    }
                }
            }
        };
        // 소켓 접속 시도, 버퍼생성
        checkUpdate.start();
    }
}