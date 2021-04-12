package cn.cenxi.service;


import android.webkit.JavascriptInterface;

import com.alibaba.fastjson.JSON;

import cn.cenxi.service.entity.JsData;
import cn.cenxi.service.gps.GpsService;
import cn.cenxi.service.gps.serviceImpl.GpsServiceImpl;

/**
 * 用于被js调用的接口
 */
public class InterfaceForJS {

    private static GpsService gpsService = new GpsServiceImpl();

    //使用单独的回调js方法回调结果
    @JavascriptInterface
    public String jsCallbackMethod(String jsonstr) {
        //解析接口参数为对象
//        System.out.println(jsonstr);
        JsData d = JSON.parseObject(jsonstr, JsData.class);
//        System.out.println(d.getType());
//        System.out.println(d.getData());
//        System.out.println(d.getCallbackMethodForApp());
        String res = "";
        switch (d.getType()) {
            case "getGPSData":
                //获取gps定位数据
                res = gpsService.getGPSData(d);
                break;
            default:
                break;

        }
        return res;
    }
}
