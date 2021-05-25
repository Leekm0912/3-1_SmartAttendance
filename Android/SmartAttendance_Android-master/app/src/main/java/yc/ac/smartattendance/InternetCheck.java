package yc.ac.smartattendance;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

// 서버와 연결을 체크해주는 클래스
class InternetCheck extends AsyncTask<Void,Void,Boolean> {
    private Consumer mConsumer; // 서버 연결 여부에 따라 동작할 코드.
    public  interface Consumer { void accept(Boolean internet); }

    // 객체 생성과 동시에 execute()로 실행시켜줌.
    public  InternetCheck(Consumer consumer) { mConsumer = consumer; execute(); }

    @Override protected Boolean doInBackground(Void... voids) { try {
        Socket sock = new Socket();
        sock.connect(new InetSocketAddress(AppData.SERVER_IP, Integer.parseInt(AppData.SERVER_PORT)), 1500);
        sock.close();
        Log.i("서버연결 확인","성공");
        return true; // 성공시 true
    } catch (IOException e) {
        AppData.SERVER_IP = "";
        e.printStackTrace();
        return false;
        }
    } // 실패시 false 리턴

    @Override protected void onPostExecute(Boolean internet) { mConsumer.accept(internet); }
}