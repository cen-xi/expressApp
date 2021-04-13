package cn.cenxi.service.gps.serviceImpl;

import android.annotation.SuppressLint;
import android.icu.text.SimpleDateFormat;
import android.util.Log;
import android.webkit.WebView;

import com.alibaba.fastjson.JSON;
import com.baidu.location.BDLocation;
import com.baidu.location.Poi;

import java.text.ParseException;
import java.util.Date;

import cn.cenxi.MainActivity;
import cn.cenxi.service.InterfaceForJS;
import cn.cenxi.service.entity.JsData;
import cn.cenxi.service.gps.GpsService;
import cn.cenxi.service.gps.dto.LocationResult;

public class GpsServiceImpl implements GpsService {


    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


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
                    InterfaceForJS.jsCallbackMethod(d.getCallbackMethodForApp(), setLocationResult(location));
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
        LocationResult res = new LocationResult();
        res.setTime(location.getTime());
        res.setLocType(location.getLocType() + "");
        res.setLatitude(location.getLatitude() + "");
        res.setLongitude(location.getLongitude() + "");
        res.setRadius(location.getRadius() + "");
        res.setAddrStr(location.getAddrStr());
        res.setLocationdescribe(location.getLocationDescribe());
        res.setOperators(location.getOperators() + "");
        if (location.getLocType() == BDLocation.TypeGpsLocation) {
            res.setDescribe("gps定位成功");
        } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
            res.setDescribe("网络定位成功");
        } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
            res.setDescribe("离线定位成功，离线定位结果也是有效的");
        } else {
            res.setDescribe("定位失败");
        }
//        } else if (location.getLocType() == BDLocation.TypeServerError) {
//            res.setDescribe("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
//        } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
//            res.setDescribe("网络不同导致定位失败，请检查网络是否通畅");
//        } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
//            res.setDescribe("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
//        }

        //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
        //以下只列举部分获取经纬度相关（常用）的结果信息
        //更多结果信息获取说明，请参照类参考中BDLocation类中的说明
//        StringBuilder sb = new StringBuilder(1000);
//        sb.append("time : ");
//        /**
//         * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
//         * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
//         */
//        sb.append(location.getTime());
//        sb.append("\nlocType : ");// 定位类型
//        sb.append(location.getLocType());
//        sb.append("\nCoorType : ");// 经纬度坐标类型
//        sb.append(location.getCoorType());
//        sb.append("\nlocType description : ");// *****对应的定位类型说明*****
//        sb.append(location.getLocTypeDescription());
//        sb.append("\nlatitude : ");// 纬度
//        sb.append(location.getLatitude());
//        sb.append("\nlontitude : ");// 经度
//        sb.append(location.getLongitude());
//        sb.append("\nradius : ");// 半径，定位精度，默认值为0.0f
//        sb.append(location.getRadius());
//        sb.append("\nCountryCode : ");// 国家码
//        sb.append(location.getCountryCode());
//        sb.append("\nCountry : ");// 国家名称
//        sb.append(location.getCountry());
//        sb.append("\ncitycode : ");// 城市编码
//        sb.append(location.getCityCode());
//        sb.append("\ncity : ");// 城市
//        sb.append(location.getCity());
//        sb.append("\nDistrict : ");// 区
//        sb.append(location.getDistrict());
//        sb.append("\nStreet : ");// 街道
//        sb.append(location.getStreet());
//        sb.append("\naddr : ");// 地址信息
//        sb.append(location.getAddrStr());
//        sb.append("\nUserIndoorState: ");// *****返回用户室内外判断结果*****
//        sb.append(location.getUserIndoorState());
//        sb.append("\nDirection(not all devices have value): ");
//        sb.append(location.getDirection());// 方向
//        sb.append("\nlocationdescribe: ");
//        sb.append(location.getLocationDescribe());// 位置语义化信息
//        sb.append("\nPoi: ");// POI信息
//        if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
//            for (int i = 0; i < location.getPoiList().size(); i++) {
//                Poi poi = (Poi) location.getPoiList().get(i);
//                sb.append(poi.getName() + ";");
//            }
//        }
//        if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
//            sb.append("\nspeed : ");
//            sb.append(location.getSpeed());// 速度 单位：km/h
//            sb.append("\nsatellite : ");
//            sb.append(location.getSatelliteNumber());// 卫星数目
//            sb.append("\nheight : ");
//            sb.append(location.getAltitude());// 海拔高度 单位：米
//            sb.append("\ngps status : ");
//            sb.append(location.getGpsAccuracyStatus());// *****gps质量判断*****
//            sb.append("\ndescribe : ");
//            sb.append("gps定位成功");
//        } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
//            // 运营商信息
//            if (location.hasAltitude()) {// *****如果有海拔高度*****
//                sb.append("\nheight : ");
//                sb.append(location.getAltitude());// 单位：米
//            }
//            sb.append("\noperationers : ");// 运营商信息
//            sb.append(location.getOperators());
//            sb.append("\ndescribe : ");
//            sb.append("网络定位成功");
//        } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
//            sb.append("\ndescribe : ");
//            sb.append("离线定位成功，离线定位结果也是有效的");
//        } else if (location.getLocType() == BDLocation.TypeServerError) {
//            sb.append("\ndescribe : ");
//            sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
//        } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
//            sb.append("\ndescribe : ");
//            sb.append("网络不同导致定位失败，请检查网络是否通畅");
//        } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
//            sb.append("\ndescribe : ");
//            sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
//        }
//        Log.d("", sb.toString());
//        Log.d("", sb.toString().length() + "");
        return JSON.toJSONString(res);
    }

}
