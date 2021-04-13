package cn.cenxi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import cn.cenxi.service.InterfaceForJS;

public class MainActivity extends AppCompatActivity {

    //快捷键输入 logt 然后回车马，用于日志
    private static final String TAG = "Main4Activity";

    //浏览器组件对象
    @SuppressLint("StaticFieldLeak")
    public static WebView w;
    //定位管理器
    public static LocationManager lm;
    //
    //定位权限认证标识
    public static final int TAKE_LOCATION = 1;
    //百度sdk客户端
    public static LocationClient mLocationClient = null;
    //百度定位数据
    public static BDLocation locationData = null;
    //开启间隔 n毫秒 刷新gps数据,如果是0则仅刷新一次就关闭,默认是0 【如果不是0必须大于1000才生效】
    public static int scanSpanNum = 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //仅设置任务栏字体为黑色
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        //将任务栏设置为沉浸式
        //setBarIntoWindow();
        //绑定页面
        setContentView(R.layout.activity_main);
        //初始化页面
        initlay();
    }

    //将任务栏设置为沉浸式
    private void setBarIntoWindow(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0及以上
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN  //设置为全屏
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    |View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//状态栏字体颜色设置为黑色这个是Android 6.0才出现的属性   默认是白色
            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);//设置为透明色
            window.setNavigationBarColor(Color.TRANSPARENT);
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4到5.0
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
    }

    //初始化页面
    private void initlay() {
        //隐藏系统自带的顶部标题栏，导入新的标题栏，详细看书本110页
        ActionBar act = getSupportActionBar();
        if (act != null) {
            act.hide();
        }
        w = (WebView) findViewById(R.id.web);
        //获取定位管理器
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        //定位权限验证
        if (checkLocationPower()) {
            //初始化webview容器
            setWebViewInfo();
        }
    }

    //封装定位权限验证
    private boolean checkLocationPower() {
        // 判断GPS是否正常启动
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "请开启GPS导航...", Toast.LENGTH_SHORT).show();
            // 返回开启GPS导航设置界面
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
            return false;
        }


        //动态权限请求
        int ACCESS_COARSE_LOCATION = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int ACCESS_FINE_LOCATION = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int ACCESS_WIFI_STATE = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_WIFI_STATE);
        int ACCESS_NETWORK_STATE = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_NETWORK_STATE);
        int CHANGE_WIFI_STATE = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CHANGE_WIFI_STATE);
        if (ACCESS_COARSE_LOCATION != PackageManager.PERMISSION_GRANTED ||
                ACCESS_FINE_LOCATION != PackageManager.PERMISSION_GRANTED ||
                ACCESS_WIFI_STATE != PackageManager.PERMISSION_GRANTED ||
                ACCESS_NETWORK_STATE != PackageManager.PERMISSION_GRANTED ||
                CHANGE_WIFI_STATE != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.CHANGE_WIFI_STATE,}, TAKE_LOCATION);
            return false;
        }
        //初始化定位配置
        setBaiduGPSLocationInfo();
        return true;
    }


    //权限询问反馈，可判断多个权限是否通过
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //requestCode为权限认证标识
        switch (requestCode) {
            case TAKE_LOCATION:
                for (int r : grantResults) {
                    if (r != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "没有定位权限，即将退出", Toast.LENGTH_SHORT).show();
                        //倒计时2秒后关闭app
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //注销当前页面
                                finish();
                                //彻底清除app
                                System.exit(0);
                            }
                        }, 2000);
                        return;
                    }
                }
                //初始化定位配置
                setBaiduGPSLocationInfo();
                //初始化webview容器
                setWebViewInfo();
                break;
            default:
        }
    }

    private void setBaiduGPSLocationInfo() {

        //百度sdk客户端，//声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());

        //配置定位信息
        LocationClientOption option = new LocationClientOption();
        //可选，设置定位模式，默认高精度
        //LocationMode.Hight_Accuracy：高精度；
        //LocationMode. Battery_Saving：低功耗；
        //LocationMode. Device_Sensors：仅使用设备；
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，设置返回经纬度坐标类型，默认GCJ02
        //GCJ02：国测局坐标；
        //BD09ll：百度经纬度坐标；
        //BD09：百度墨卡托坐标；
        //海外地区定位，无需设置坐标类型，统一返回WGS84类型坐标
        option.setCoorType("bd09ll");

        //可选，设置发起定位请求的间隔，int类型，单位ms
        //如果设置为0，则代表单次定位，即仅定位一次，默认为0
        //如果设置非0，需设置1000ms以上才有效
        option.setScanSpan(scanSpanNum);
        //可选，设置是否使用gps，默认false
        //使用高精度和仅用设备两种定位模式的，参数必须设置为true
        option.setOpenGps(true);
        //可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false
        //可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false
//            option.setLocationNotify(true);

        //可选，定位SDK内部是一个service，并放到了独立进程。
        //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)
        option.setIgnoreKillProcess(false);

        //可选，设置是否收集Crash信息，默认收集，即参数为false
        option.SetIgnoreCacheException(false);
        //可选，V7.2版本新增能力
        //如果设置了该接口，首次启动定位时，会先判断当前Wi-Fi是否超出有效期，
        // 若超出有效期，会先重新扫描Wi-Fi，然后定位
        option.setWifiCacheTimeOut(5 * 60 * 1000);
        //可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false
        option.setEnableSimulateGps(false);
        //可选，设置是否需要最新版本的地址信息。默认需要，即参数为true
        option.setNeedNewVersionRgc(true);
        //是否需要地址信息,默认不需要
        option.setIsNeedAddress(true);
        //可选，设置是否需要地址描述
        option.setIsNeedLocationDescribe(true);
        //可选，设置是否需要设备方向结果
        option.setNeedDeviceDirect(false);
        //可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setLocationNotify(false);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
        option.setIsNeedAltitude(false);

        //mLocationClient为第二步初始化过的LocationClient对象
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        //更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明
        mLocationClient.setLocOption(option);
        //
        //配置监听
        BDAbstractLocationListener myListener = new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
//                Log.d(TAG, "纬度=" + location.getLatitude() + "");
                //4.9E-324 表示计算错误
                if (location.getTime() == null || location.getLatitude() == 4.9E-324) {
                    //失败
                    return;
                }
                MainActivity.locationData = location;
            }
        };
        //客户端注册监听函数
        mLocationClient.registerLocationListener(myListener);
    }


    //封装webview 配置
    @SuppressLint("SetJavaScriptEnabled")
    private void setWebViewInfo() {
        //启用js
        w.getSettings().setJavaScriptEnabled(true);
        //启用dom
        w.getSettings().setDomStorageEnabled(true);
        //允许弹窗
        w.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        //加载前端的网址
//        w.loadUrl("http://192.168.0.103:55/html/app/login.html");
//        w.loadUrl("https://www.baidu.com");
        w.loadUrl("http://192.168.0.103:55/html/app/taskRoom/taskList.html");
        //使用chrome 浏览器客户端
        w.setWebChromeClient(new WebChromeClient());
        w.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //禁用非http 和 https 的超链接
                if (url.contains("://") && !url.contains("ftp://") && !url.contains("http://")
                        && !url.contains("https://")) {
                    //不存在时,禁用跳转操作
                    return true;
                }
                w.loadUrl(url);
                return true;
            }

        });
        //绑定暴露给js的接口
        w.addJavascriptInterface(new InterfaceForJS(), "InterfaceForJS");
    }

}
