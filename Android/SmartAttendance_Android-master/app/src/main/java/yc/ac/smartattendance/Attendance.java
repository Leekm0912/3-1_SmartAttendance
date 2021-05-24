package yc.ac.smartattendance;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Iterator;

import static yc.ac.smartattendance.SpinnerChoice.codeToLecture;

public class Attendance extends AppCompatActivity {
    static TextView textView1;
    TextView textView2;
    TextView textAttendance;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    LinearLayout linearLayout;
    int color=0;
    int count=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);


        textView1 = findViewById(R.id.textView2);
        textView2 = findViewById(R.id.textView1);
        linearLayout = findViewById(R.id.linear);
        textAttendance = findViewById(R.id.textAttendance);

        Intent getIntent = getIntent();
        String table = getIntent.getStringExtra("table");
        Log.d("넘어온 인텐트 값", table + "");

        if (table != null) {
            //Log.d("전송된 인텐트 값", value);
            String[] tableName = table.split("_");
            textView2.setText("20" + tableName[0] + "_" + tableName[1] + "교시" + "_" + codeToLecture(tableName[2]));
            Log.d("테이블 검색", table + "검색");

            //테이블명으로 데이터 검색
            myRef = database.getReference().child(table);
            Log.d("테이블 요소 검색", myRef + "검색");
            if (myRef != null) {
                    // 실시간으로 데이터 베이스에 변화가 생겼을때 실행되는 콜벡메서드
                    // Read from the database
                    myRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();

                            if( map != null) {
                                Log.d("value가 null이 아닐때 데이터 바뀜", "Value is: " + map);
                                Iterator<String> keys = map.keySet().iterator();
                                String cleanCode="";
                                textView1.setText("");
                                while (keys.hasNext()){

                                    //출석한 인원수 세기
                                    count++;

                                    String key = keys.next();
                                    Log.d("해쉬맵 key", key);
                                    String id = dataSnapshot.child(key).child("id").getValue(Object.class).toString();
                                    String temp = dataSnapshot.child(key).child("temp").getValue(Object.class).toString();
                                    String result = dataSnapshot.child(key).child("result").getValue(Object.class).toString();
                                    String time = dataSnapshot.child(key).child("time").getValue(Object.class).toString();

                                    switch (result){
                                        case "0":
                                            result = "오류";
                                            color = 0xFF000000;
                                            break;
                                        case "1":
                                            result = "정상";
                                            color = 0xFF00FF00;
                                            break;
                                        case "2":
                                            result = "미열";
                                            color = 0xFFFF00FF;
                                            break;
                                        case "3":
                                            result = "고온";
                                            color = 0xFFFF0000;
                                            break;
                                    }


                                    cleanCode = "학번 : " + id + " / " + "온도 : " + temp + "°C / " + "결과 : " + result + " / " + "시간 : " + time.substring(11,16);

                                    TextView textView = new TextView(Attendance.this);
                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    textView.setLayoutParams(layoutParams);
                                    textView.setText(cleanCode);
                                    textView.setTextColor(color);
                                    linearLayout.addView(textView);
                                    Log.d("파이어베이스 데이터", cleanCode);
                                }
                                textAttendance.setText("출석한 인원 : " + count + "명");

                            }else{
                                setNullText();
                                Log.d("value가 null 일때 데이터 바뀜", "Value is: " + map);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            setNullText();
                            //Log.w("데이터 바뀜 에러", "Failed to read value.", error.toException());
                        }
                    });
                }else{
                    Log.d("NULL 체크", "myRef 가 null 값임");
                    setNullText();
                }
            } else {
                Log.d("NULL 체크", "table이 null 값임");
                setNullText();

            }


    }//onCreate closed


    public static void setNullText(){
        textView1.setText("결과 없음");
        textView1.setTextColor(Color.parseColor("#FF0000"));

    }
}