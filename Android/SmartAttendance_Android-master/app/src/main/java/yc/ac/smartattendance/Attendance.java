package yc.ac.smartattendance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
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
    //public static ArrayList<StudentInformation> arrayList1 = new ArrayList<StudentInformation>();;
    public static String sex;
    String stuid;
    ListView listView;
    MyAdapter myAdapter; // 어댑터
    sex sex2 = new sex();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

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

                    myRef.addValueEventListener(sex2);
            }
        }

        
    }//onCreate closed




    class sex implements ValueEventListener{
        private ArrayList<StudentInformation> arrayList1;
        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
            arrayList1 = new ArrayList<StudentInformation>();
            int count=0;
            if( map != null) {
                Log.d("value가 null이 아닐때 데이터 바뀜", "Value is: " + map);
                Iterator<String> keys = map.keySet().iterator();
                String cleanCode="";
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
                    cleanCode =  "온도 : " + temp + "°C / " + "결과 : " + result + " / " + "시간 : " + time.substring(11,16);
                    stuid = "학번 : " + id;
                    StudentInformation studentInformation = new StudentInformation();
                    studentInformation.setId(stuid);
                    studentInformation.setInform(cleanCode);
                    arrayList1.add(studentInformation);

                }
                listView = findViewById(R.id.listview);
                Log.d(sex2.arrayList1.toString(),"asdf");
                myAdapter = new MyAdapter(Attendance.this,  R.layout.list1, sex2.arrayList1);
                listView.setAdapter(myAdapter);
                textAttendance.setText("출석한 인원 : " + count + "명");

            }else{
                Log.d("value가 null 일때 데이터 바뀜", "Value is: " + map);
            }
        }
    }


    // 리스트뷰 동작할 어댑터
    class MyAdapter extends BaseAdapter {
        Context context;
        LayoutInflater inflater;
        ArrayList<StudentInformation> list;
        int layout;


        @SuppressLint("ServiceCast")
        public MyAdapter(Context context, int layout, ArrayList<StudentInformation> item){
            this.context = context;
            this.layout = layout;
            this.list = item;
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) { return position; }

        /*
         * position : 생성할 항목의 순서값
         * parent : 생성되는 뷰의 부모(지금은 리스트뷰)
         * convertView : 이전에 생성되었던 차일드 뷰(지금은 Layout.xml) 첫 호출시에는 null
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = inflater.inflate(layout,parent,false);
            }
            TextView textId = convertView.findViewById(R.id.stuid);
            TextView textInform = convertView.findViewById(R.id.stuinform);


            textId.setText(list.get(position).getId());
            textInform.setText(list.get(position).getInform());
            Log.d("어뎁터 클래스의 getView메서드", "getView: " + list.get(position).getInform());

            return convertView;
        }
    }




}