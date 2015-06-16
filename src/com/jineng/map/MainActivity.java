package com.jineng.map;

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapLongClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
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

		initListeners();
	}

	private LatLng current;

	private void initListeners() {
		/*
		 * 当长按时，弹出“添加供应点”对话框
		 */
		baiduMap.setOnMapLongClickListener(new OnMapLongClickListener() {
			public void onMapLongClick(LatLng point) {
				current = point;
				showCreatePoiDialog();
			}
		});

		baiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {

				Bundle bundle = marker.getExtraInfo();
				if (bundle != null) {
					Vendor v = (Vendor) bundle.getSerializable("vendor");

					showUpdatePoiDialog(v);
				}
				return false;
			}

		});
	}

	private void showCreatePoiDialog() {
		LayoutInflater factory = LayoutInflater.from(MainActivity.this);

		final View view = factory.inflate(R.layout.dialog_add_poi, null);
		Dialog dialog = new AlertDialog.Builder(MainActivity.this)
				.setTitle("添加")
				.setIcon(R.drawable.ic_launcher)
				.setPositiveButton("保存", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Vendor v = new Vendor();

						vendorFromDialog(view, v, true);
						beginCreatePoi(v);

					}

					private void beginCreatePoi(final Vendor v) {
						Thread thread = new Thread(new Runnable() {
							@Override
							public void run() {
								boolean success = LbsCloudService
										.CreateVendor(v);

								Message msg = new Message();
								msg.what = JinengMessage.CREATE_POI;
								if (success)
									msg.obj = v;
								else
									msg.obj = null;
								handler.dispatchMessage(msg);
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

		EditText edit = (EditText) view.findViewById(R.id.poiLatLong);
		edit.setText(String
				.format("%f,%f", current.longitude, current.latitude));
		dialog.show();
	}

	private void vendorFromDialog(View view, Vendor v, boolean createOrUpdate) {
		EditText name = (EditText) view.findViewById(R.id.poiName);
		v.setTitle(name.getText().toString());
		EditText address = (EditText) view.findViewById(R.id.poiAddress);
		v.setAddress(address.getText().toString());
		if (createOrUpdate) {
			v.setLatitude(current.latitude);
			v.setLongitude(current.longitude);
		}

		EditText contactor = (EditText) view.findViewById(R.id.poiContactor);
		v.setContactor(contactor.getText().toString());
		EditText mobile = (EditText) view.findViewById(R.id.poiMobile);
		v.setMobile(mobile.getText().toString());
		EditText price = (EditText) view.findViewById(R.id.poiPrice);
		v.setPrice(Double.parseDouble(price.getText().toString()));
		EditText quantity = (EditText) view.findViewById(R.id.poiQuantity);
		v.setQuantity(Double.parseDouble(quantity.getText().toString()));
		EditText memo = (EditText) view.findViewById(R.id.poiMemo);
		v.setMemo(memo.getText().toString());
		EditText startDate = (EditText) view.findViewById(R.id.poiStartDate);
		v.setStartDate(startDate.getText().toString());
	}

	private void showUpdatePoiDialog(final Vendor v) {
		LayoutInflater factory = LayoutInflater.from(MainActivity.this);

		final View view = factory.inflate(R.layout.dialog_add_poi, null);
		Dialog dialog = new AlertDialog.Builder(MainActivity.this)
				.setTitle("修改")
				.setIcon(R.drawable.ic_launcher)
				.setPositiveButton("保存", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						Vendor v2 = new Vendor();
						v2.setId(v.getId());
						vendorFromDialog(view, v2, false);

						beginUpdatePoi(v2);
					}

					private void beginUpdatePoi(final Vendor v) {
						class UpdateRunnable implements Runnable {
							Handler handler;

							public UpdateRunnable(Handler handler) {
								this.handler = handler;
							}

							@Override
							public void run() {
								Looper.prepare();
								boolean success = LbsCloudService
										.UpdateVendor(v);
								Message msg = new Message();
								msg.what = JinengMessage.UPDATE_POI;
								if (success)
									msg.obj = v;
								else
									msg.obj = null;
								handler.dispatchMessage(msg);
								Looper.loop();
							}
						}

						Thread thread = new Thread(new UpdateRunnable(handler));
						thread.start();

					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).setView(view).create();

		initUpdateDialog(v, view);

		dialog.show();
	}

	private void initUpdateDialog(final Vendor v, final View view) {
		EditText name = (EditText) view.findViewById(R.id.poiName);
		name.setText(v.getTitle());
		EditText address = (EditText) view.findViewById(R.id.poiAddress);
		address.setText(v.getAddress());

		EditText ll = (EditText) view.findViewById(R.id.poiLatLong);
		ll.setText(String.format("%f,%f", v.getLatitude(), v.getLongitude()));

		EditText contactor = (EditText) view.findViewById(R.id.poiContactor);
		contactor.setText(v.getContactor());

		EditText mobile = (EditText) view.findViewById(R.id.poiMobile);
		mobile.setText(v.getMobile());
		EditText price = (EditText) view.findViewById(R.id.poiPrice);

		price.setText(Double.valueOf(v.getPrice()).toString());

		EditText quantity = (EditText) view.findViewById(R.id.poiQuantity);

		quantity.setText(Double.valueOf(v.getQuantity()).toString());

		EditText memo = (EditText) view.findViewById(R.id.poiMemo);
		memo.setText(v.getMemo());
		EditText startDate = (EditText) view.findViewById(R.id.poiStartDate);
		startDate.setText(v.getStartDate());
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

	private static class MyHandler extends Handler{
		
		WeakReference<MainActivity> mActivity;

        MyHandler(MainActivity activity) {
                mActivity = new WeakReference<MainActivity>(activity);
        }

		
		@Override
		public void handleMessage(Message msg) {
			MainActivity activity = mActivity.get();
			
			super.handleMessage(msg);
			switch (msg.what) {
			case JinengMessage.CREATE_POI: {
				Vendor v = (Vendor) msg.obj;
				if (v != null)
					activity.createMarker(v);
				else
					Toast.makeText(activity, "添加供应点失败！",
							Toast.LENGTH_SHORT).show();
				break;
			}
			case JinengMessage.LOAD_POIS: {
				@SuppressWarnings("unchecked")
				List<Vendor> vendors = (List<Vendor>) msg.obj;
				activity.loadPois(vendors);
				break;
			}
			case JinengMessage.UPDATE_POI: {
				Vendor v = (Vendor) msg.obj;
				String text = (v == null) ? "修改供应点失败！" : "修改供应点成功！";
				Toast.makeText(activity, text, Toast.LENGTH_SHORT)
						.show();
				break;
			}
			}
		}
	}
	
	MyHandler handler = new MyHandler(this);

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
				Looper.prepare();

				List<Vendor> vendors = LbsCloudService.findAllVendors();
				Message m = new Message();
				m.obj = vendors;
				m.what = JinengMessage.LOAD_POIS;
				handler.sendMessage(m);
				
				Looper.loop();
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
		Marker marker = (Marker) (baiduMap.addOverlay(new MarkerOptions()
				.position(new LatLng(v.getLatitude(), v.getLongitude()))
				.icon(bd).zIndex(10)));

		Bundle bundle = new Bundle();
		bundle.putSerializable("vendor", v);
		marker.setExtraInfo(bundle);
		return marker;
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
