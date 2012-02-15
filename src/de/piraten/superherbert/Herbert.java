package de.piraten.superherbert;


import org.cocos2d.layers.CCLayer;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGSize;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class Herbert extends CCLayer{
	private float mLastX, mLastY, mLastZ;
	
	protected void moveHerbert(){
		System.out.println("mLastX: "+mLastX+" mLastY"+mLastY+" mLastZ: "+mLastZ);
	}
	
	public class accelerometer extends Activity implements SensorEventListener {
		private boolean mInitialized;
		private SensorManager mSensorManager;
	    private Sensor mAccelerometer;
	    private final float NOISE = (float) 2.0;
		 
	    /** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	    	System.out.println("Hi herbert!");
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.main);
	        mInitialized = false;
	        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	        mSensorManager.registerListener(this, mAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
	    }
	    
	    protected void onResume() {
	        super.onResume();
	        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	    }

	    protected void onPause() {
	        super.onPause();
	        mSensorManager.unregisterListener(this);
	    }

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}

		public void onSensorChanged(SensorEvent event) {
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			if (!mInitialized) {
				mLastX = x;
				mLastY = y;
				mLastZ = z;
				mInitialized = true;
			} else {
				float deltaX = Math.abs(mLastX - x);
				float deltaY = Math.abs(mLastY - y);
				float deltaZ = Math.abs(mLastZ - z);
				if (deltaX < NOISE) deltaX = (float)0.0;
				if (deltaY < NOISE) deltaY = (float)0.0;
				if (deltaZ < NOISE) deltaZ = (float)0.0;
				mLastX = x;
				mLastY = y;
				mLastZ = z;
			}	
		}
		
	}
	
	
}
