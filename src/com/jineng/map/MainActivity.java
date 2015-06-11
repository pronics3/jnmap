package com.jineng.map;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.jineng.data.LbsCloudService;
import com.jineng.data.Vendor;

public class MainActivity extends Activity {
	MapView mapView = null;  
	ToggleButton btnAddPin;
	BaiduMap baiduMap;
	boolean addPinMode = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
		setContentView(R.layout.activity_main);
		
		mapView = (MapView)findViewById(R.id.bmapView);
		
		baiduMap = mapView.getMap();
		
		initOverlay();
		
		btnAddPin = (ToggleButton)findViewById(R.id.add_pin);
		btnAddPin.setOnCheckedChangeListener( new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton button, boolean checked) {
				addPinMode = checked;
			}
			
		});
	}
	
	private Handler messageHandler = new Handler(){
		 public void handleMessage(Message msg)  
         {  
             super.handleMessage(msg);  
             Vendor v = (Vendor)msg.obj;
             Toast.makeText(MainActivity.this, v.getAddress(), 1000).show();
         }  
	};
	
	private Marker markerA;
	BitmapDescriptor bdA = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_marka);
	private void initOverlay(){
		
		Thread thread = new Thread(new Runnable(){

			@Override
			public void run() {
				
				Vendor v = LbsCloudService.findVendor(933424604);
				
				Message message = new Message();
				message.obj = v;
				messageHandler.sendMessage( message);
			}
			
		});
		thread.start();
		
			
			
			LatLng llA = new LatLng(39.963175, 116.400244);
			
		
			OverlayOptions ooA = new MarkerOptions().position(llA).icon(bdA)
					.zIndex(9);
			markerA = (Marker) (baiduMap.addOverlay(ooA));
		
		
	}

	@Override
	public void onDestroy(){
		super.onDestroy();  
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理  
        mapView.onDestroy(); 
	}
	
	@Override
	public void onResume(){
		super.onResume();
        mapView.onResume(); 
	}
	
	@Override
	public void onPause(){
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
