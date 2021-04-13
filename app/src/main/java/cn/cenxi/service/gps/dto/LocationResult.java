package cn.cenxi.service.gps.dto;


import lombok.Data;

@Data
public class LocationResult {

    //定位时间
    private String time;
    //定位类型
    private String locType;
    //定位类型结果描述
    private String describe;
//    //对应的定位类型说明
//    private String locTypeDescription;
//    //经纬度坐标类型
//    private String coorType;
    //纬度
    private String latitude;
    //经度
    private String longitude;
    //半径，定位精度，默认值为0.0f
    private String radius;
//    //国家编码
//    private String countryCode;
//    //国家名称
//    private String country;
//     //省份编码
//    private String provinceCode;
//    //省份名称
//    private String province;
//    //城市编码
//    private String citycode;
//    //城市名称
//    private String city;
//    //区
//    private String district;
//    //街道
//    private String street;
//    //乡镇信息
//    private String town;
    //地址信息 【合并了国家、省份、市、区、街道】
    private String addrStr;
//    //返回用户室内外判断结果
//    private String UserIndoorState;
//    //方向
//    private String direction;
    //位置语义化信息【即在xxx附近】
    private String locationdescribe;
//    //POI信息【兴趣点信息】
//    private String poiList;
//    //速度，单位：km/h
//    private String speed;
//    //卫星数目
//    private String satelliteNumber;
//    //海拔高度 单位：米
//    private String Altitude;
//    //gps质量判断
//    private String GpsAccuracyStatus;
    //运营商信息
    private String operators;


}
