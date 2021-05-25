package yc.ac.smartattendance;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import javax.security.auth.login.LoginException;

import static java.lang.Thread.sleep;

public class Login extends AppCompatActivity {
    EditText idText,passwordText;
    AlertDialog dialog;
    boolean start = true;
    ImageView imageView;
    // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;
    // 첫 번째 뒤로가기 버튼을 누를때 표시
    private Toast toast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( Login.this,  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                Log.e("newToken", newToken);

            }
        });


        idText = (EditText) findViewById(R.id.idText);
        passwordText = (EditText)findViewById(R.id.passwordText);
        imageView = (ImageView) findViewById(R.id.mainimage) ;


        //로그인
        Button loginButton = (Button)findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //버튼 동기화
                if(start) {
                    start = false;
                    final String id = idText.getText().toString();
                    final String password = passwordText.getText().toString();

                    //아이디 또는 비밀번호를 입력하지 않았을때
                    if (id.equals("") || password.equals("")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                        dialog = builder.setMessage("아이디와 비밀번호를 입력해주세요").setNegativeButton("확인", null).create();
                        dialog.show();
                        start = true;
                        return;
                    }


                    //스레드사용
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                               if(id.equals("admin") && password.equals("admin")) {
                                   start = true;
                                   Intent intent = new Intent(getApplicationContext(), MenuChoice.class);
                                   startActivity(intent);
                               }else{
                                    //로그인 실패시
                                        start = true;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                start = true;
                            }
                        }
                    }).start();

                }
            }
        });


        //회원가입
        TextView registerText = (TextView)findViewById(R.id.registerButton);
        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 회원가입 클릭시
                Toast.makeText(getApplicationContext(), "회원가입 문의 : smartsw97@gmail.com", Toast.LENGTH_LONG).show();
                //Intent intent = new Intent(getApplicationContext(), joinAsMember.class);
                //startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // 기존 뒤로가기 버튼의 기능을 막기위해 주석처리 또는 삭제
        // super.onBackPressed();

        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지났으면 Toast Show
        // 2000 milliseconds = 2 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
        // 현재 표시된 Toast 취소
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
            finishAffinity();
            toast.cancel();
        }
    }

}