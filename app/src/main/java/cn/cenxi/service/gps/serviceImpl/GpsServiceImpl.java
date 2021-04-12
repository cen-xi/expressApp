package cn.cenxi.service.gps.serviceImpl;

import android.annotation.SuppressLint;
import android.icu.text.SimpleDateFormat;
import android.util.Log;
import android.webkit.WebView;

import com.baidu.location.BDLocation;

import java.text.ParseException;
import java.util.Date;

import cn.cenxi.MainActivity;
import cn.cenxi.service.entity.JsData;
import cn.cenxi.service.gps.GpsService;

public class GpsServiceImpl implements GpsService {


    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    //app 调用 页面的 js 方法
    private void jsCallbackMethod(final String method, final String params) {

        //拼接js
        final String js = "javascript:" + method + "('" + params + "')";
        //获取容器对象
        final WebView w = MainActivity.w;
        //调用js方法
        w.post(new Runnable() {
            @Override
            public void run() {
                //执行
                w.loadUrl(js);
            }
        });

    }

    //如果使用监听则需要加入MissingPermission
    @SuppressLint("MissingPermission")
    //获取gps定位数据
    @Override
    public String getGPSData(final JsData d) {
        //开始时间
        final Date yd = new Date();
        //开启线程,异步操作
        new Thread() {
            @Override
            public void run() {
                super.run();
                BDLocation location = null;
                //线程循环,200毫秒休眠一次,最多等待10秒
                for (int i = 0; i < 50; i++) {
                    if (!MainActivity.mLocationClient.isStarted()) {
                        //如果未开启,则开启
                        MainActivity.mLocationClient.start();
                    }
                    if (MainActivity.locationData != null
                            && MainActivity.locationData.getTime() != null
                    ) {
                        //有时间数据
                        try {
                            //字符串转日期
                            // 禁止宽松格式
                            format.setLenient(false);
                            Date mc = format.parse(MainActivity.locationData.getTime());
                            if (!mc.before(yd)) {
                                //定位更新时间大于或等于刚进来的时间
                                location = MainActivity.locationData;
                                break;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        //休眠200毫秒
                        Thread.sleep(200);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (MainActivity.mLocationClient.isStarted()) {
                    //如果开启,则关闭
                    MainActivity.mLocationClient.stop();
                }
                //封装结果，执行回调操作
                if (d.getCallbackMethodForApp() != null) {
                    jsCallbackMethod(d.getCallbackMethodForApp(), setLocationResult(location));
                }
            }
        }.start();
        return null;
    }

    //封装返回结果
    private String setLocationResult(BDLocation location) {
        if (location == null) {
            return "";
        }
        //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
        //以下只列举部分获取经纬度相关（常用）的结果信息
        //更多结果信息获取说明，请参照类参考中BDLocation类中的说明
        StringBuilder sb = new StringBuilder(1000);
        sb.append("time : ");
        /**
         * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
         * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
         */
        sb.append(location.getTime());
        sb.append("\nlocType : ");// 定位类型
        sb.append(location.getLocType());
        sb.append("\nCoorType : ");// 经纬度坐标类型
        sb.append(location.getCoorType());
        sb.append("\nlocType description : ");// *****对应的定位类型说明*****
        sb.append(location.getLocTypeDescription());
        sb.append("\nlatitude : ");// 纬度
        sb.append(location.getLatitude());
        sb.append("\nlontitude : ");// 经度
        sb.append(location.getLongitude());
        sb.append("\nradius : ");// 半径，定位精度，默认值为0.0f
        sb.append(location.getRadius());
        sb.append("\nCountryCode : ");// 国家码
        sb.append(location.getCountryCode());
        sb.append("\nCountry : ");// 国家名称
        sb.append(location.getCountry());
        sb.append("\ncitycode : ");// 城市编码
        sb.append(location.getCityCode());
        sb.append("\ncity : ");// 城市
        sb.append(location.getCity());
        sb.append("\nDistrict : ");// 区
        sb.append(location.getDistrict());
        sb.append("\nStreet : ");// 街道
        sb.append(location.getStreet());
        sb.append("\naddr : ");// 地址信息
        sb.append(location.getAddrStr());
        sb.append("\nUserIndoorState: ");// *****返回用户室内外判断结果*****
        sb.append(location.getUserIndoorState());
        sb.append("\nDirection(not all devices have value): ");
        sb.append(location.getDirection());// 方向
        sb.append("\nlocationdescribe: ");
        sb.append(location.getLocationDescribe());// 位置语义化信息
        sb.append("\nPoi: ");// POI信息
        Log.d("",sb.toString());
        Log.d("",sb.toString().length()+"");
        return sb.toString();
    }

}
