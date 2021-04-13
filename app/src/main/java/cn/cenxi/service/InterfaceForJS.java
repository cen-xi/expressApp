package cn.cenxi.service;


import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.alibaba.fastjson.JSON;

import cn.cenxi.MainActivity;
import cn.cenxi.service.entity.JsData;
import cn.cenxi.service.gps.GpsService;
import cn.cenxi.service.gps.serviceImpl.GpsServiceImpl;

/**
 * 用于被js调用的接口
 */
public class InterfaceForJS {

    private static GpsService gpsService = new GpsServiceImpl();


    //app 调用 页面的 js 方法
    public static void jsCallbackMethod(final String method, final String params) {

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


    //使用js提供的回调方法回调结果
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
