package de.piraten.superherbert;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

public class HerbertAccelListener extends Activity implements SensorEventListener {

	private float mLastX, mLastY, mLastZ;
	private boolean mInitialized;

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;

	private final float NOISE = (float) 2.0;
	
	JumpGameLayer2 parentLayer=null;
	
	public HerbertAccelListener(JumpGameLayer2 parentLayer){
	/*
	 *  da die CClayers ja selber keine Activities sein können
	 *  macht HerbertAccelListener das für uns und sagt dann dem JumpGameLayer
	 *  wie es Herbert verschieben muss. uasy... NICHT.
	 */
		this.parentLayer=parentLayer;
		
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mInitialized = false;
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO (maybe. maybe not.) Auto-generated method stub
		
	}

	public void onSensorChanged(SensorEvent event) {
		//TextView tvX= (TextView)findViewById(R.id.x_axis);
		//TextView tvY= (TextView)findViewById(R.id.y_axis);
		//TextView tvZ= (TextView)findViewById(R.id.z_axis);
		//ImageView iv = (ImageView)findViewById(R.id.image);
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		if (!mInitialized) {
			mLastX = x;
			mLastY = y;
			mLastZ = z;
			//tvX.setText("0.0");
			//tvY.setText("0.0");
			//tvZ.setText("0.0");
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
			System.out.println("deltaX"+Float.toString(deltaX));
			System.out.println("deltaY"+Float.toString(deltaY));
			System.out.println("deltaZ"+Float.toString(deltaZ));
			
			//parentLayer.positionHerbert(deltaX);
			
			//iv.setVisibility(View.VISIBLE);
			/*if (deltaX > deltaY) {
				//iv.setImageResource(R.drawable.horizontal);
				System.out.println("horizontal");
			} else if (deltaY > deltaX) {
				//iv.setImageResource(R.drawable.vertical);
				System.out.println("vertical");
			} else {
				//iv.setVisibility(View.INVISIBLE);
			}*/
		}
		
	}
	
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}
	


}
