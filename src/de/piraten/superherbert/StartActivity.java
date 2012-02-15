

package de.piraten.superherbert;

import java.util.concurrent.atomic.AtomicBoolean;

import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.opengl.CCGLSurfaceView;
import org.cocos2d.sound.SoundEngine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;

/*
 * Dear person who is about to read this code: I'm deeply sorry. Part of this mess is a result of 
 * blindly translating plaetzchen's iOS original copypasta (https://github.com/plaetzchen/Super-Herbert )-- untangling
 * that one would've taken way too long. The other part is a result of despair and lack of documentation.
 * I will fix and clean this (hopefully). Until then, I strongly recommend you to close this file this instant.
 */

// Probleme: 

// 1. das springen sieht kacke aus.

// OBACHT: org.cocos2d.types.ccVertex2F hat keinen getter/setter für die X / Y werte. Hab's jetzt manuell reingepopelt.

public class StartActivity extends Activity {
	/**
	 * Da das hier schienbar der einzge Ort ist, aus dem
	 * sich Actvities (URL aufrufen, settings speichern.. ) ausführen lassen,
	 * finden sich unten mehrere Methoden die den layers die ActivityFunktionen dieser
	 * Klasse zugängig machen. Nicht schön, but hey, at least it works.
	 */
	protected CCGLSurfaceView _glSurfaceView;
	SharedPreferences preferences;
	private  Context _cocos2dContext;
	
	private AtomicBoolean soundPlaying = new AtomicBoolean(false);
	private boolean soundPaused = false;
	private boolean resumeSound = false;
	private boolean effectsOn = false;


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
	    super.onCreate(savedInstanceState);
	 
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	 
	    _glSurfaceView = new CCGLSurfaceView(this);
	 	    
	    setContentView(_glSurfaceView);
	    
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    
	    _cocos2dContext = this;

	    // Preload background music
	    SoundEngine.sharedEngine().preloadSound(_cocos2dContext, R.raw.background);
	    SoundEngine.sharedEngine().preloadSound(_cocos2dContext, R.raw.menu);
	    soundPlaying.set(getSetting("musicOn"));
	    if (soundPlaying.get())
	        SoundEngine.sharedEngine().playSound(_cocos2dContext, R.raw.menu, true);
	    else
	    	// prevents hickups.
	    	soundPaused=true;

	  
	    // Preload sound effects
	    SoundEngine.sharedEngine().preloadEffect(_cocos2dContext, R.raw.coin);	    
	    SoundEngine.sharedEngine().preloadEffect(_cocos2dContext, R.raw.gameover);
	    SoundEngine.sharedEngine().preloadEffect(_cocos2dContext, R.raw.whoosh);
	    effectsOn=getSetting("effectsOn");
	    
		System.out.println("onCreate: soundplaying: "+soundPlaying+", SoundPaused: "+soundPaused);
	    
	}
	
	@Override
	public void onStart()
	{
	    super.onStart();
	    //Looper.prepare();
	    
	    CCDirector.sharedDirector().attachInView(_glSurfaceView);
	    CCDirector.sharedDirector().setDisplayFPS(true);
	    CCDirector.sharedDirector().setAnimationInterval(1.0f / 60.0f);
	    
	    // horrible hack to make opening a URL from the scene work. a bit. FIXME
	    try{
	    	System.out.println("creating MenuLayer...");
	    	CCScene scene = MenuLayer.scene(this);
		    CCDirector.sharedDirector().runWithScene(scene);
	    }catch (Exception e){
	    	System.out.println("Exception in startactivity.scenestuff");

	    	e.printStackTrace();
	    }

	}
	@Override
	public void onPause()
	{
	    super.onPause();
	 
	    CCDirector.sharedDirector().pause();
	    
        //mSensorManager.unregisterListener(this);
	    
		System.out.println("onPause(), soundplaying: "+soundPlaying+", SoundPaused: "+soundPaused);
	    // If the sound is loaded and not paused, pause it - but flag that we want it resumed
	    if (soundPlaying.get() && !soundPaused){
	        SoundEngine.sharedEngine().pauseSound();
	        soundPaused = true;
	        resumeSound = true;
	    }
	    // No sound playing, don't resume
	    else
	        resumeSound = false; 
	}
	 
	@Override
	public void onResume()
	{
	    super.onResume();
	    CCDirector.sharedDirector().resume();
	    
	    // Resume playing sound only if it's loaded, paused and we want to resume it
	    if (soundPlaying.get() && soundPaused && resumeSound)
	    {
	        SoundEngine.sharedEngine().resumeSound();
	        soundPaused = false;
	    }
	}
	 
	@Override
	public void onStop()
	{
	    super.onStop();
	 
	    CCDirector.sharedDirector().end();
	}
	
	public void unregisterSensorListener() {
		System.out.println("unregisterListener was called, but it is empty");
	}
	
/////// HELPERS ///////////////////////////////////////////////////////////////
	
	public void openURL(String url){
		final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
		startActivity(intent);
		
	}
	
	public void storeSetting(String key, boolean val){
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(key, val);
		editor.commit();
	}
	
	public void storeSetting(String key, int val){
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(key, val);
		editor.commit();
	}
	
	public boolean getSetting(String key){
		// obacht!! returns true as default value.
		return preferences.getBoolean(key, true);
	}
	
	public int getScore(String key){
		// obacht!! returns true as default value.
		return preferences.getInt(key, 0);
	}
		
	public void bgMusicClicked(){
		System.out.println("before: bgMusicClicked(), soundplaying: "+soundPlaying+", SoundPaused: "+soundPaused);
	    // If we haven't started playing the sound - play it!
	    if (!soundPlaying.get()){
	        SoundEngine.sharedEngine().playSound(_cocos2dContext, R.raw.menu, true);
	        soundPlaying.set(true);
	        soundPaused= false;
            System.out.println("sound was not playing");

	        // this brings up the little white box. why the fuck.
	        storeSetting("musicOn", soundPlaying.get());
	    }
	    else{
	        // We've loaded the sound, now it's just a case of pausing / resuming
	        if (!soundPaused){
	            SoundEngine.sharedEngine().pauseSound();
	            soundPaused = true;
	            System.out.println("sound was playing and is now paused");
	        }
	        else{
	        	//System.out.println("soundPlaying:"+soundPlaying);
	            SoundEngine.sharedEngine().resumeSound();
	            soundPaused = false;
	            System.out.println("sound was paused");
	            
	        }
	        soundPlaying.set(!soundPaused);
	        System.out.println("after: bgMusicClicked(), soundplaying: "+soundPlaying+", SoundPaused: "+soundPaused);
            storeSetting("musicOn", ! soundPaused);
	    }
	}
	
	public void effectsClicked(){
		effectsOn = !effectsOn;
		storeSetting("effectsOn", effectsOn);
	}
	
	public boolean isEffectsOn() {
		return effectsOn;
	} 
	
	public AtomicBoolean isMusicOn(){
		return soundPlaying;
	}
	
	protected Context getContext(){
		return _cocos2dContext;
	}
}