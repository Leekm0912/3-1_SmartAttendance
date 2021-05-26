package yc.ac.smartattendance;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MenuChoice extends AppCompatActivity {
    private Button open; // 출석 열기 버튼
    private Button search; //  출석 조회 버튼

    // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;
    // 첫 번째 뒤로가기 버튼을 누를때 표시
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_choice);

        open = findViewById(R.id.button2);
        search = findViewById(R.id.button3);


    }


    public void searchClick(View view) {
        Intent intent = new Intent(this, SearchAttendance.class);
        startActivity(intent);
    }

    public void openClick(View view) {
        if ("".equals(AppData.SERVER_IP)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("서버 주소 설정");
            alert.setMessage("서버의 IP 주소를 입력하세요");
            final EditText ip = new EditText(this);
            InputFilter[] FilterArray = new InputFilter[1];
            FilterArray[0] = new InputFilter.LengthFilter(15); //글자수 제한
            ip.setFilters(FilterArray);
            alert.setView(ip);

            alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) { //확인 버튼을 클릭했을때
                    AppData.SERVER_IP = String.valueOf(ip.getText());
                    Log.d("서버IP설정", AppData.SERVER_IP);

                    new InternetCheck(internet -> { // 서버 연결 체크
                        if (internet) {
                            Intent intent = new Intent(MenuChoice.this, OpenAttendance.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(MenuChoice.this, "서버 주소가 옳바르지 않습니다", Toast.LENGTH_LONG).show();
                        }
                    });


                }
            });
            alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) { //취소 버튼을 클릭
                }
            });
            alert.show();


        } else {
            new InternetCheck(internet -> { // 서버 연결 체크
                if (internet) {
                    Intent intent = new Intent(MenuChoice.this, OpenAttendance.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(MenuChoice.this, "서버 주소가 옳바르지 않습니다", Toast.LENGTH_LONG).show();
                }
            });
        }
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