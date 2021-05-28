package yc.ac.smartattendance;

import android.util.Log;

public class StudentInformation {
    String id;
    String inform;

    public void setId(String id) {
        this.id = id;
    }

    public void setInform(String inform) {
        Log.d("StudentInformation클래스", "setInform: " + inform);
        this.inform = inform;
    }

    public String getId() {
        return id;
    }

    public String getInform() {
        return inform;
    }
}
