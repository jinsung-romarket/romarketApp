package kr.co.romarket.config;

public class Constant {

    public static String schemeName = "romarket";

    // public static String serverUrl = "https://app.ro-market.com";
    // public static String serverUrl = "http://192.168.45.100";
    public static String serverUrl = "http://192.168.45.128";
    public static String checkServerUrl = "/init/checkserver";
    public static String setInfoUrl = "/init/android/setinfo";
    public static String serverStatusSuccess = "0000";
    public static String imgServerUrl = "http://dnmart.co.kr";

    public static String mainViewUrl = "/display/main";
    public static String todayViewUrl = "/display/today";
    public static String evtViewUrl = "/display/evt";
    public static String cartViewUrl = "/cart/cart";

    public static final int REQUEST_CODE_CAMERA = 1000;
    public static final int REQUEST_CODE_LOCALTION = 1001;
    public static final int REQUEST_CODE_PHONE = 1002;

    public static final long FINISH_INTERVAL_TIME = 2000;

    public static String PAGE_CODE_MAIN = "MAIN";
    public static String PAGE_CODE_TODAY = "TODAY";
    public static String PAGE_CODE_EVENT = "EVENT";
    public static String PAGE_CODE_ORDER = "ORDER";
    public static String PAGE_CODE_CART = "CART";

    public static final int RESULT_REQUEST_SCAN = 1000;

}
