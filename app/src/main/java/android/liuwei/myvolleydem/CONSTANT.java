package android.liuwei.myvolleydem;

public class CONSTANT
{
    private static final String URL_BAIDU
            = "http://www.baidu.com";
    private static final String URL_BAIDU_SEC
            = "https://www.baidu.com";
    private static final String URL_BAIDU_M_SEC
            = "https://m.baidu.com/";

    private static final String URL_NL_CONFIG
            = "http://mobile.neulion.net.cn/svn/projects/espn/2019/appconfig_android_dev.json";
    private static final String URL_NL_CONFIG_CDN
            = "https://neulion-a.akamaihd.net/nlmobile/espn/config/2019/appconfig_android_r1.nmc";
    private static final String URL_NL_CONFIG_LOCAL =
            "http://mobile.neulion.net.cn/svn/projects/espn/2019/appconfig_android_dev.json";
    private static final String URL_IMAGE_CACHE_MAX_AGE_600 =
            "https://neulionsmbnyc-a.akamaihd.net/u/qa88/espnv6/thumbs/channels/2_es.jpg";

    private static final String URL_VIDEO_PATH =
            "http://vip.sports.cctv.com/servlets/encryptvideopath.do";

    public static final String URL_ACCESS_TOKEN = "https://espnplayerqa.neulion.com/secure/accesstoken";

    public static final String URL_CHANNEL = "https://qaapi.neulion.com/api_espn/v1/channels";

    // ---- Impl ----
    public static final String URL_GET = URL_NL_CONFIG_CDN;

    public static final String URL_POST = URL_VIDEO_PATH;
}
