package yc.ac.smartattendance;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static java.lang.Integer.parseInt;
import static yc.ac.smartattendance.R.color.blue;
import static yc.ac.smartattendance.SpinnerChoice.whichLecture;
import static yc.ac.smartattendance.SpinnerChoice.whichPeriod;
import static yc.ac.smartattendance.Today.getDay;
import static yc.ac.smartattendance.Today.getMonth;
import static yc.ac.smartattendance.Today.getYear;

public class SearchAttendance extends AppCompatActivity {
    int choiceComplete = 0;
    Spinner spinnerLecture;
    Spinner spinnerPeriod;
    String selectedLecture="";
    String selectedPeriod="";
    private TextView textView_Date;
    private DatePickerDialog.OnDateSetListener callbackMethod;
    String tableName;
    TextView textToday;
    Button button;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_attendance);

        this.InitializeView();
        this.InitializeListener();
        spinnerLecture = findViewById(R.id.spinnerLecture2);
        spinnerPeriod = findViewById(R.id.spinnerPeriod2);
        textToday = findViewById(R.id.textToday);
        textToday.setText(Today.getYear() + "년 " + Today.getMonth() + "월 " + Today.getDay() + "일");
        button = findViewById(R.id.button6);

        //lecture스피너
        spinnerLecture.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView)parent.getChildAt(0)).setTextColor(Color.BLACK);
                selectedLecture = whichLecture(parent.getItemAtPosition(position).toString());
//                Log.d(TAG, "과목 onItemSelected: " + parent.getItemAtPosition(position) + " 이 선택됨");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedLecture = "K0124723";
//                Log.d(TAG, "과목 onNothingSelected: " + "선택X -> 스프링 자동선택");
            }
        });

        //period스피너
        spinnerPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView)parent.getChildAt(0)).setTextColor(Color.BLACK);
                selectedPeriod = whichPeriod(parent.getItemAtPosition(position).toString());
//                Log.d(TAG, "교시 onItemSelected: " + parent.getItemAtPosition(position) + " 이 선택됨");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedPeriod = "1";
//                Log.d(TAG, "교시 onNothingSelected: " + "선택x -> 1교시 자동 선택");
            }
        });


    }
        public void InitializeView()
        {
            textView_Date = (TextView)findViewById(R.id.textView_date);
        }



        public void OnClickHandler(View view)
        {
            DatePickerDialog dialog = new DatePickerDialog(this, callbackMethod, parseInt(getYear()),
                    parseInt(getMonth())-1, parseInt(getDay()));
            dialog.show();
        }


    public void searchClick(View view) {
        if(choiceComplete == 1){
            button.setText("날짜 선택");
            tableName += "_" + selectedPeriod + "_" + selectedLecture;
            myRef = database.getReference().child(tableName);
            if( myRef != null ) {
                Intent intent = new Intent(SearchAttendance.this, Attendance.class);
                intent.putExtra("table", tableName + "");
                startActivity(intent);
            }else{
                Toast.makeText(this.getApplicationContext(), "결과 값 없음", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this.getApplicationContext(), "날짜를 선택해 주십시요", Toast.LENGTH_SHORT).show();
        }

    }

    public void InitializeListener()
    {
        callbackMethod = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
            {
                String Month;
                String Day;
                if(monthOfYear <= 9){ Month = "0" + (monthOfYear + 1); }else{ Month = monthOfYear + ""; }
                if(dayOfMonth <= 9){ Day = "0" + dayOfMonth; }else{ Day = dayOfMonth + ""; }
                textView_Date.setText( year + "년 " + (monthOfYear + 1) + "월 " + Day + "일");
                textView_Date.setTextColor(Color.BLACK);
                choiceComplete = 1;
                //테이블명에 날짜 먼저 입력
                tableName = (year + "").substring(2) + Month + Day;
                button.setText("선택 완료");

            }
        };
    }


}


