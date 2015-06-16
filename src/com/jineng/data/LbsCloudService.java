package com.jineng.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class LbsCloudService {

	public static String AK = "6QQrfoSrIKAjL5TK3uTbmsb4";
	public static int GEOTABLE_ID = 107664;
	public static String CREATE_POI = "http://api.map.baidu.com/geodata/v3/poi/create";
	public static String QUERY_POI_LIST = "http://api.map.baidu.com/geodata/v3/poi/list";
	public static String QUERY_POI = "http://api.map.baidu.com/geodata/v3/poi/detail";
	public static String UPDATE_POI = "http://api.map.baidu.com/geodata/v3/poi/update";
	public static String DELETE_POI = "http://api.map.baidu.com/geodata/v3/poi/delete";

	public static Vendor findVendor(long id) {

		String s = HttpUtil.getRequest(QUERY_POI, "ak:" + AK, 
				"geotable_id:"+ GEOTABLE_ID,
				"id:" + id);

		if (s != null) {
			try {
				JSONObject obj = new JSONObject(s);

				int status = obj.getInt("status");
				if (status == 0) {
					JSONObject poi = obj.getJSONObject("poi");
					Vendor v = new Vendor();
					JSONArray location = poi.getJSONArray("location");
					v.setLongitude(location.getDouble(0));
					v.setLatitude(location.getDouble(1));
					v.setAddress(poi.getString("address"));
					return v;
				}

			} catch (Exception e) {
				Log.e("LbsCloudService", e.toString());
			}
		}
		return null;
	}

	public static List<Vendor> findAllVendors() {
		List<Vendor> vendors = new ArrayList<Vendor>();

		String s = HttpUtil.getRequest(QUERY_POI_LIST, "ak:" + AK,
				"geotable_id:" + GEOTABLE_ID);
		if (s != null) {
			try {
				JSONObject obj = new JSONObject(s);

				int status = obj.getInt("status");
				if (status == 0) {
					JSONArray pois = obj.getJSONArray("pois");

					for (int i = 0; i < pois.length(); i++) {
						JSONObject poi = pois.getJSONObject(i);
						Vendor v = new Vendor();
						v.setId(poi.getLong("id"));
						JSONArray location = poi.getJSONArray("location");
						v.setLongitude(location.getDouble(0));
						v.setLatitude(location.getDouble(1));
						v.setAddress(poi.getString("address"));
						vendors.add(v);
					}
				}

			} catch (Exception e) {
				Log.e("LbsCloudService", e.toString());
			}
		}
		return vendors;
	}

	public static boolean CreateVendor(Vendor v){
		
		String s = HttpUtil.postRequest(CREATE_POI, "ak:" + AK,
				"geotable_id:" + GEOTABLE_ID,
				"title:"+(v.getTitle()!=null?v.getTitle():" "),
				"address:"+(v.getAddress()!=null?v.getAddress():" "),
				"latitude:"+v.getLatitude(),
				"longitude:"+v.getLongitude(),
				"coord_type:3",
				"quantity:"+v.getQuantity(),
				"price:"+v.getPrice(),
				"contactor:" + (v.getContactor()!=null?v.getContactor():" "),
				"mobile:"+(v.getMobile()!=null?v.getMobile():" "),
				"memo:"+(v.getMemo()!=null?v.getMemo():" "),
				"startDate:"+(v.getStartDate()!=null?v.getStartDate():" ")
				);
		
		if (s != null) {
			try {
				JSONObject obj = new JSONObject(s);

				int status = obj.getInt("status");
				if (status == 0) {
					v.setId(obj.getLong("id"));
					return true;
				}
			} catch (Exception e) {
				Log.e("LbsCloudService", e.toString());
			}
		}
		
		return false;
	}

	public static boolean UpdateVendor(Vendor v) {
		
		String s = HttpUtil.postRequest(UPDATE_POI, "ak:" + AK,
				"geotable_id:" + GEOTABLE_ID,
				"id:"+v.getId(),
				"title:"+(v.getTitle()!=null?v.getTitle():" "),
				"address:"+(v.getAddress()!=null?v.getAddress():" "),
				"latitude:"+v.getLatitude(),
				"longitude:"+v.getLongitude(),
				"coord_type:3",
				"quantity:"+v.getQuantity(),
				"price:"+v.getPrice(),
				"contactor:" + (v.getContactor()!=null?v.getContactor():" "),
				"mobile:"+(v.getMobile()!=null?v.getMobile():" "),
				"memo:"+(v.getMemo()!=null?v.getMemo():" "),
				"startDate:"+(v.getStartDate()!=null?v.getStartDate():" ")
				);
		
		if (s != null) {
			try {
				Log.e("LbsCloudService", s);
				v.setTitle(s);
				JSONObject obj = new JSONObject(s);

				int status = obj.getInt("status");
				if (status == 0) {
					
					return true;
				}
			} catch (Exception e) {
				Log.e("LbsCloudService", e.toString());
			}
		}
		
		return false;
	}

	public static boolean DeleteVendor(Vendor v) {
		String s = HttpUtil.postRequest(DELETE_POI, "ak:" + AK,
				"geotable_id:" + GEOTABLE_ID,
				"id:"+v.getId()
				);
		
		if (s != null) {
			try {
				JSONObject obj = new JSONObject(s);

				int status = obj.getInt("status");
				if (status == 0) {
					return true;
				}
			} catch (Exception e) {
				Log.e("LbsCloudService", e.toString());
			}
		}
		
		return false;
	}
}
