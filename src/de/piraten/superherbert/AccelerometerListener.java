package de.piraten.superherbert;

import de.piraten.superherbert.JumpGameLayer2.FlyThread;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;


public class AccelerometerListener extends Activity implements SensorEventListener {
		// TODO: mLastY, mLastZ-Overhead entfernen
	
		private float mLastX;

		private boolean mInitialized;
		private SensorManager mSensorManager;
	    private Sensor mAccelerometer;
	    private final float NOISE = (float) 2.0;
		 
	    //JumpGameLayer parentLayer = null;
	    Handler handler = null;
	    
	    /*public AccelerometerListener(JumpGameLayer parentLayer){
	    	this.parentLayer = parentLayer;
	    }*/
	    
	    public AccelerometerListener(Handler handler){
	    	this.handler = handler;
	    }
	    
	    /** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	    	Looper.prepare();
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
		
		public void unregisterSensorListener() {
		mSensorManager.unregisterListener(this);
		}

		public void onSensorChanged(SensorEvent event) {
			System.out.println("sensorChanged");
			float x = event.values[0];

			if (!mInitialized) {
				mLastX = x;
				mInitialized = true;
			} else {
				float deltaX = Math.abs(mLastX - x);
				if (deltaX < NOISE) deltaX = (float)0.0;
				mLastX = x;
			}	
			//System.out.println("sensorChanged: "+mLastX+" "+mLastY+" "+mLastZ);
			Message msg = new Message();
			//eww.
			msg.obj=(Float)mLastX;
			// spezifzieren dass es sich um Koordinate, nicht um quit-msg handelt
			msg.what=1;
			System.out.println(msg.toString());
			
			handler.sendMessage(msg);
			
		}
		
	}