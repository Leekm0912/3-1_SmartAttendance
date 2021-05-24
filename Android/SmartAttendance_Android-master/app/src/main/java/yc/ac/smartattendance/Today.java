package yc.ac.smartattendance;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Today {
    public static String getDate(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        String today = sdf.format(date);

        return today;
    }

    public static String getYear(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String today = sdf.format(date);

        return today;
    }

    public static String getMonth(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("MM");
        String today = sdf.format(date);

        return today;
    }

    public static String getDay(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        String today = sdf.format(date);

        return today;
    }
}
