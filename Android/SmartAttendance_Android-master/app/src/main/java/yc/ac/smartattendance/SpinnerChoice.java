package yc.ac.smartattendance;

public class SpinnerChoice {
    public static String whichLecture(String lecture){
        String result;
        switch (lecture){
            case "스프링":
                result = "K0124723";
                break;
            case "자바스크립트 심화":
                result = "K0124786";
                break;
            case "캡스톤디자인2":
                result = "122108";
                break;
            case "클라우드":
                result = "K0124627";
                break;
            default:
                result = "K0124723";
        }
        return result;
    }

    public static String codeToLecture(String code){
        String result;
        switch (code){
            case "K0124723":
                result = "스프링";
                break;
            case "K0124786":
                result = "자바스크립트 심화";
                break;
            case "122108":
                result = "캡스톤디자인2";
                break;
            case "K0124627":
                result = "클라우드";
                break;
            default:
                result = "스프링";
        }
        return result;
    }
    
    public static String whichPeriod(String period){
        String result;
        switch (period){
            case "1교시":
                result = "1";
                break;
            case "2교시":
                result = "2";
                break;
            case "3교시":
                result = "3";
                break;
            case "4교시":
                result = "4";
                break;
            case "5교시":
                result = "5";
                break;
            case "6교시":
                result = "6";
                break;
            case "7교시":
                result = "7";
                break;
            case "8교시":
                result = "8";
                break;
            case "9교시":
                result = "9";
                break;
            default:
                result = "1교시";

        }
        return result;
    }
}
