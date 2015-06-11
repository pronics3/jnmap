package com.jineng.map;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;

public class MainActivity extends Activity {
	MapView mapView = null;  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//在使用SDK各组件之前初始化context信息，传入ApplicationContext  
        //注意该方法要再setContentView方法之前实现  
        SDKInitializer.initialize(getApplicationContext());  
        
		setContentView(R.layout.activity_main);
		
		mapView = (MapView)findViewById(R.id.bdmapView);
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
        mapView.onDestroy(); 
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
