package yc.ac.smartattendance;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static yc.ac.smartattendance.SpinnerChoice.whichLecture;
import static yc.ac.smartattendance.SpinnerChoice.whichPeriod;

public class OpenAttendance extends AppCompatActivity {
    TextView textView;
    Spinner spinnerLecture;
    Spinner spinnerPeriod;
    String selectedLecture="";
    String selectedPeriod="";
    String tempLecture;
    String tempPeriod;
    int attendanceOpenCheck = 0;


    String TAG = "spinner choice";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_attendance);

        textView = findViewById(R.id.textView);
        textView.setText(Today.getYear() + "년 " + Today.getMonth() + "월 " + Today.getDay() + "일");

        spinnerLecture = findViewById(R.id.spinnerLecture);
        spinnerPeriod = findViewById(R.id.spinnerPeriod);

        //lecture스피너
        spinnerLecture.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView)parent.getChildAt(0)).setTextColor(Color.BLACK);
                selectedLecture = whichLecture(parent.getItemAtPosition(position).toString());
                tempLecture = parent.getItemAtPosition(position).toString();
                if("".equals("selectedLecture")){ selectedPeriod = whichLecture("스프링"); };
                if("".equals("tempLecture")){ selectedPeriod = "스프링"; };
//                Log.d(TAG, "과목 onItemSelected: " + parent.getItemAtPosition(position) + " 이 선택됨");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedLecture = whichLecture("스프링");
                tempLecture = "스프링";
//                Log.d(TAG, "과목 onNothingSelected: " + "선택X -> 스프링 자동선택");
            }
        });

        //period스피너
        spinnerPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView)parent.getChildAt(0)).setTextColor(Color.BLACK);
                selectedPeriod = whichPeriod(parent.getItemAtPosition(position).toString());
                tempPeriod = parent.getItemAtPosition(position).toString();
                if("".equals("selectedPeriod")){ selectedPeriod = "1"; };
                if("".equals("tempPeriod")){ selectedPeriod = "1교시"; };
//                Log.d(TAG, "교시 onItemSelected: " + parent.getItemAtPosition(position) + " 이 선택됨");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedPeriod = "1";
                tempPeriod = "1교시";
//                Log.d(TAG, "교시 onNothingSelected: " + "선택x -> 1교시 자동 선택");
            }
        });

    }


    //출석 열기 버튼 클릭
    public void openClick(View view) {
        if( attendanceOpenCheck == 0) {
//            new Thread() {
//                @Override
//                public void run() {
//                    //테이블명
//                    String result = Today.getDate() + "_" + selectedPeriod + "_" + selectedLecture;
//                    //서버로 테이블명 전송
//                    SocketProcess.sendMsg(result);
//                    //소켓통신으로 데이터 전송 후 초기화
//                    selectedLecture = "";
//                    selectedPeriod = "";
//                    attendanceOpenCheck = 1;
//                }
//            }.start();

            //출석이 열려있는 시간 설정을 위해 Alert창 띄우기
            AlertDialog.Builder builder = new AlertDialog.Builder(OpenAttendance.this);
            builder.setTitle("\'" + Today.getDate() + "_" + tempPeriod + "_" + tempLecture + "\'" + " 출석 열림");                                                                //타이틀을 지정합니다.
            builder.setMessage("10분 후 출석이 자동으로 닫힙니다");

            //10분 후 출석이 닫히게 설정
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new Thread() {
                        @Override
                        public void run() {
                            Timer timer = new Timer();
                            TimerTask task = new TimerTask() {
                                @Override
                                public void run() {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            String result = "end";
                                            //서버로 전송
                                            SocketProcess.sendMsg(result);
                                            attendanceOpenCheck = 0;
                                        }
                                    }.start();
                                }
                            };
                            timer.schedule(task, 60 * 60 * 1000);
                        }
                    }.start();
                    
                    //데이터전송
                    new Thread() {
                        @Override
                        public void run() {
                            //테이블명
                            String result = Today.getDate() + "_" + selectedPeriod + "_" + selectedLecture;
                            //서버로 테이블명 전송
                            SocketProcess.sendMsg(result);
                            //소켓통신으로 데이터 전송 후 초기화
                            attendanceOpenCheck = 1;
                        }
                    }.start();
                    
                }
            });

            //30분 후에 출석이 닫히게 설정
            builder.setNegativeButton("30분으로 설정", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new Thread() {
                        @Override
                        public void run() {
                            Timer timer = new Timer();
                            TimerTask task = new TimerTask() {
                                @Override
                                public void run() {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            String result = "end";
                                            //서버로 전송
                                            SocketProcess.sendMsg(result);
                                            attendanceOpenCheck = 0;
                                            //clearVariable();
                                        }
                                    }.start();
                                }
                            };
                            timer.schedule(task, 60 * 60 * 1000 * 3);
                        }
                    }.start();
                    
                    //데이터 전송
                    new Thread() {
                        @Override
                        public void run() {
                            //테이블명
                            String result = Today.getDate() + "_" + selectedPeriod + "_" + selectedLecture;
                            //서버로 테이블명 전송
                            SocketProcess.sendMsg(result);
                            //소켓통신으로 데이터 전송 후 초기화
                            attendanceOpenCheck = 1;

                        }
                    }.start();
                    
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(OpenAttendance.this);
            builder.setTitle("현재 출석이 열려 있습니다.");
            builder.setMessage("현재 출석을 종료하시겠습니까?");

            //새로운 출석을 열기 위해 기존 출석 종료
            builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
                @Override

                public void onClick(DialogInterface dialog, int which) {
                    //열려있는 기존 출석 종료
                    new Thread() {
                        @Override
                        public void run() {
                            String result = "end";
                            //서버로 전송
                            SocketProcess.sendMsg(result);
                            //clearVariable();
                            attendanceOpenCheck = 0;
                        }
                    }.start();
                }
            });

            //새로운 출석을 열지 않음
            builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //아무것도 하지 않음
                }
            });
            AlertDialog alert = builder.create();
            alert.show(); //
        }
    }


    public void closeClick(View view) {
        new Thread() {
            @Override
            public void run() {
                String result = "end";
                //서버로 전송
                SocketProcess.sendMsg(result);
                //clearVariable();
                attendanceOpenCheck = 0;
                Log.d("cliseClick 발생", "closeClick: " + result);
            }
        }.start();
    }

    public void clearVariable(){
        selectedLecture = "";
        selectedPeriod = "";
        tempLecture = "";
        tempPeriod = "";
    }

}