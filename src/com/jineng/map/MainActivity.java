package com.jineng.map;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapLongClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapTouchListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.jineng.data.LbsCloudService;
import com.jineng.data.Vendor;

public class MainActivity extends Activity {
	MapView mapView = null;
	ToggleButton btnAddPin;
	BaiduMap baiduMap;
	boolean addPinMode = false;

	LocationClient locClient;
	private MyLocationListener locationListen = new MyLocationListener();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		mapView = (MapView) findViewById(R.id.bmapView);

		baiduMap = mapView.getMap();
		baiduMap.setMyLocationEnabled(true);

		beginLoadPois();

		initLocationSetting();

		btnAddPin = (ToggleButton) findViewById(R.id.add_pin);
		btnAddPin.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button, boolean checked) {
				addPinMode = checked;
			}

		});

		initListeners();
	}

	private LatLng current;

	private void initListeners() {
		baiduMap.setOnMapTouchListener(new OnMapTouchListener() {

			@Override
			public void onTouch(MotionEvent event) {
			}
		});
		baiduMap.setOnMapClickListener(new OnMapClickListener() {

			public void onMapClick(LatLng point) {
				current = point;
			}

			public boolean onMapPoiClick(MapPoi poi) {
				return false;
			}
		});

		baiduMap.setOnMapLongClickListener(new OnMapLongClickListener() {
			public void onMapLongClick(LatLng point) {
				current = point;
				showAddPoiDialog(point);
			}
		});
	}

	private void showAddPoiDialog(final LatLng point) {
		LayoutInflater factory = LayoutInflater.from(MainActivity.this);

		final View view = factory.inflate(R.layout.dialog_add_poi, null);
		Dialog dialog = new AlertDialog.Builder(MainActivity.this)
				.setTitle("添加供应点").setIcon(R.drawable.ic_launcher)
				.setPositiveButton("保存", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Vendor v = new Vendor();
						
						EditText name = (EditText)view.findViewById(R.id.poiName);
						v.setTitle(name.getText().toString());
						EditText address = (EditText)view.findViewById(R.id.poiAddress);
						v.setAddress(address.getText().toString());
						v.setLatitude(point.latitude);
						v.setLongitude(point.longitude);
						EditText contactor = (EditText)view.findViewById(R.id.poiContactor);
						v.setContactor(contactor.getText().toString());
						EditText mobile = (EditText)view.findViewById(R.id.poiMobile);
						v.setMobile(mobile.getText().toString());
						EditText price = (EditText)view.findViewById(R.id.poiPrice);
						v.setPrice( Double.parseDouble(price.getText().toString()));
						EditText quantity = (EditText)view.findViewById(R.id.poiQuantity);
						v.setQuantity(Double.parseDouble(quantity.getText().toString()));
						EditText memo = (EditText)view.findViewById(R.id.poiMemo);
						v.setMemo( memo.getText().toString());
						EditText startDate = (EditText)view.findViewById(R.id.poiStartDate);
						v.setStartDate( startDate.getText().toString());
						
						
						beginSavePoi(v);
						
						
					}

					private void beginSavePoi(final Vendor v) {
						Thread thread = new Thread(new Runnable() {
							@Override
							public void run() {
								
								boolean success = LbsCloudService.CreateVendor(v);
									
								Log.e("dialog", Boolean.valueOf(success).toString());
								
							}
						});
						thread.start();
						
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).setView(view).create();
		
		EditText edit = (EditText)view.findViewById(R.id.poiLatLong);
		edit.setText(String.format("%f,%f", current.longitude, current.latitude));
		dialog.show();
	}

	private void initLocationSetting() {
		locClient = new LocationClient(this);
		locClient.registerLocationListener(locationListen);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		locClient.setLocOption(option);
		locClient.start();
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			/*
			 * Vendor v = (Vendor)msg.obj; Toast.makeText(MainActivity.this,
			 * v.getAddress(), 1000).show();
			 */
			switch (msg.what) {
			case JinengMessage.LOAD_POIS: {
				@SuppressWarnings("unchecked")
				List<Vendor> vendors = (List<Vendor>) msg.obj;
				loadPois(vendors);
			}
				break;
			case JinengMessage.UPDATE_POI: {
				Vendor v = (Vendor) msg.obj;
				Toast.makeText(MainActivity.this, v.getTitle(), 1000).show();
			}
				break;

			}

		}
	};

	/*
	 * Message what definition
	 */

	// private Marker markerA;
	BitmapDescriptor bd = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_marka);

	private void beginLoadPois() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				List<Vendor> vendors = LbsCloudService.findAllVendors();

				Message m = new Message();
				m.obj = vendors;
				m.what = JinengMessage.LOAD_POIS;
				handler.sendMessage(m);

				/*
				 * Vendor v = vendors.get(0); v.setTitle("niubility");
				 * LbsCloudService.UpdateVendor(v);
				 * 
				 * Message m2 = new Message(); m2.obj = v; m2.what =
				 * JinengMessage.UPDATE_POI; handler.sendMessage(m2);
				 */
			}
		});
		thread.start();
	}

	private void loadPois(List<Vendor> vendors) {
		for (Vendor v : vendors) {
			createMarker(v);
		}
	}

	private Marker createMarker(Vendor v) {

		LatLng ll = new LatLng(v.getLatitude(), v.getLongitude());

		OverlayOptions o = new MarkerOptions().position(ll).icon(bd).zIndex(9);

		return (Marker) (baiduMap.addOverlay(o));
	}

	boolean firstLoc = true;

	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mapView == null)
				return;
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(location.getDirection())
					.latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			baiduMap.setMyLocationData(locData);
			if (firstLoc) {
				firstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				baiduMap.animateMapStatus(u);
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	@Override
	public void onDestroy() {
		// 退出时销毁定位
		locClient.stop();
		// 关闭定位图层
		baiduMap.setMyLocationEnabled(false);
		mapView.onDestroy();
		mapView = null;
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		mapView.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mapView.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
