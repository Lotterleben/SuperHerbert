package de.piraten.superherbert;

import java.util.ArrayList;
import java.util.Random;

import org.cocos2d.actions.base.CCAction;
import org.cocos2d.actions.base.CCRepeatForever;
import org.cocos2d.actions.interval.CCAnimate;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCAnimation;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCSpriteFrame;
import org.cocos2d.nodes.CCSpriteFrameCache;
import org.cocos2d.sound.SoundEngine;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccVertex2F;
import org.cocos2d.nodes.CCLabel;
import org.cocos2d.nodes.CCLabel.TextAlignment;

import android.hardware.SensorEvent;
import android.os.Build;


public class JumpGameLayer extends CCLayer{
	
	final int kPlatformStartTag = 200;
	final int kBonusStartTag = 300;

	final int kPlatformNumber = 25;
	final int kMinPlatformStep = 80;
	final int kMaxPlatformStep = 320;
	final int kPlatformTopPadding = 5;

	final int kMinBonusStep = 30;
	final int kMaxBonusStep = 50;
	final int kHerbertTag = 4;
	
	final int kBonus5 = 0;
	final int kBonus10=1;
	final int kBonus50=2;
	final int kBonus100=3;
	final int kNumBonuses=4;
	
	CGPoint herbert_pos;
	ccVertex2F herbert_vel;
	ccVertex2F herbert_acc;
    
    float currentPlatformY;
	int currentPlatformTag;
	float currentMaxPlatformStep;
	int currentBonusPlatformIndex;
	int currentBonusType;
	int platformCount;
    
    boolean herbertLookingRight;
    boolean gameSuspended;
    
    CCSprite herbert;
    CCAction herbertFlying;
    CCSprite highScoreLabel;
    CCLabel scoreLabel;
    
    int orientation = -1;
    
    int score;
    int highScore;
    int height;
    int bestHeight;
    
	boolean effectsOn=false;
    
	StartActivity parent;
	
	Random random;
	float displayWidth=480;

	public JumpGameLayer(StartActivity parent){
		this.parent=parent;
		System.out.println("buildVersion"+Build.VERSION.RELEASE);
	    // since the Accelerometer orientation has changed with the API level 11, we'll
	    // have to adjust it acccording to the device.
		if (Build.VERSION.RELEASE.startsWith("3")){
			orientation=1;
		}
		random= new Random();

		CCSprite background= CCSprite.sprite("background-800x480.png");
		CGSize winSize = CCDirector.sharedDirector().displaySize();
		displayWidth= winSize.getWidth();
		background.setPosition(CGPoint.ccp(background.getContentSize().width / 2.0f, winSize.height / 2.0f));
		addChild(background, 0);
		
		initPlatforms();
		
        scoreLabel = CCLabel.makeLabel(String.valueOf(score), CGSize.make(300, 50), TextAlignment.RIGHT, 
        		"Arial", 22.0f);
		
		scoreLabel.setPosition(CGPoint.ccp(150,440));
        addChild(scoreLabel, 4);
				
		for( int i=0; i<kNumBonuses; i++){
			CCSprite bonus = new CCSprite("coin.png");
			bonus.setTag(kBonusStartTag+i);
			addChild(bonus,2);
			bonus.setVisible(false);
		}
		
		herbert= CCSprite.sprite("dude_left-1-hd.png");
		
		CCSpriteFrameCache sharedSpriteFrameCache= CCSpriteFrameCache.sharedSpriteFrameCache();
		
		// Since .plist and .png have the same name cocs2d finds the .png automatically
		sharedSpriteFrameCache.addSpriteFrames("dude-anim-hd_default.plist");
		
        ArrayList<CCSpriteFrame> flyAnimFrames = new ArrayList<CCSpriteFrame>();
        
        for (int i = 1; i <= 4; i++) {
            flyAnimFrames.add(sharedSpriteFrameCache.spriteFrameByName("herbert"+i+".png"));
        }
        CCAnimation flyAnim = CCAnimation.animation("flyAnim", 0.1f, flyAnimFrames);
        herbertFlying = CCRepeatForever.action(CCAnimate.action(flyAnim, false));
        herbert.runAction(herbertFlying);
                		
		herbert.setTag(kHerbertTag);
		addChild(herbert, 3);
		
		highScoreLabel = CCSprite.sprite("highscore.png");
		highScoreLabel.setPosition(CGPoint.ccp(250,470));
		highScoreLabel.setVisible(false);
		addChild(highScoreLabel, 2);
		
		resetPlatforms();
		resetHerbert();
		resetBonus();
		
		// since CCLayer extends AccelerometerListener, we can do this. \o/
		enableAccelerometerWithRate(1);
		
		effectsOn=parent.getSetting("effectsOn");
		if (effectsOn){
			SoundEngine.sharedEngine().preloadEffect(parent.getContext(), R.raw.coin);	    
			SoundEngine.sharedEngine().preloadEffect(parent.getContext(), R.raw.gameover);
			SoundEngine.sharedEngine().preloadEffect(parent.getContext(), R.raw.whoosh);
		}
		
		if (parent.isMusicOn().get()){
			SoundEngine.sharedEngine().playSound(parent.getContext(), R.raw.background, true);
		}
		
		bestHeight= parent.getScore("bestHeight");
		highScore= parent.getScore("highScore");
        
		// starts the actual game-loop.
		this.schedule("step", 0.1f);
	}
	
/////// ANDROID STUFF ///////////////////////////////////////////
	
	public static CCScene scene(StartActivity parent){

		CCScene scene = CCScene.node();

		// Callback durchreichen, brauchen wir fŸr GameOverLayer
		JumpGameLayer layer = new JumpGameLayer(parent);

		scene.addChild(layer);
		return scene;
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		float accelFilter = 0.8f;
		float accX = event.values[0];

		//herbert_vel.setX(herbert_vel.getX() * accelFilter + accX * (1.0f - accelFilter) * 500.0f * -1);
		herbert_vel.setX(herbert_vel.getX() * accelFilter + accX * (1.0f - accelFilter) * 100.0f * orientation);
	}
	
/////// GAME METHODS ///////////////////////////////////////////

	/*
	 * Creates the first 25 platforms that we'll scroll through.
	 */
	protected void initPlatforms(){
		
		currentPlatformTag = kPlatformStartTag;
		while(currentPlatformTag < kPlatformStartTag + kPlatformNumber){
			initPlatform();
			currentPlatformTag++;
		}
	}
	
	/*
	 * Creates randomly sized Platform (s, m or l)
	 */
	protected void initPlatform(){
		
		CCSprite platform;
		switch(random.nextInt(3)){
		case 1:
			platform= CCSprite.sprite("cloud-small-hd.png");
			platform.setTag(currentPlatformTag);
			break;
		case 2: 
			platform= CCSprite.sprite("cloud-medium-hd.png");
			platform.setTag(currentPlatformTag);
			break;	
		default:
			platform= CCSprite.sprite("cloud-big-hd.png");
			platform.setTag(currentPlatformTag);
			break;
		}
		addChild(platform, 1);	
	}
	
	
	protected void resetPlatforms(){
		
		currentPlatformY= -1;
		currentPlatformTag = kPlatformStartTag;
		//FYI currentMaxPlatformStep = 60.0f;
		currentMaxPlatformStep = 160.0f;
		currentBonusPlatformIndex = 0;
		currentBonusType = 0;
		platformCount = 0;
		
		while(currentPlatformTag < kPlatformStartTag + kPlatformNumber) {
			resetPlatform();
			currentPlatformTag++;
		}
	}
	
	protected void resetPlatform(){
		if(currentPlatformY < 0) {
			currentPlatformY = 30.0f;
		} else {
			currentPlatformY += random.nextInt((int) currentMaxPlatformStep - kMinPlatformStep) + kMinPlatformStep;
			if(currentMaxPlatformStep < kMaxPlatformStep) {
				currentMaxPlatformStep += 0.5f;
			}
		}
		
		CCSprite platform = (CCSprite) getChildByTag(currentPlatformTag);
				
		//if(random.nextInt(2)==1)
			//platform.setScaleX(-1.0f);
		
		float x;
		CGSize size = platform.getContentSize();
		if(currentPlatformY == 30.0f) {
			x = 160.0f;
		} else {
			x = random.nextInt((int)(displayWidth-size.width)) + size.width/2;
		}
		
		platform.setPosition(CGPoint.ccp(x,currentPlatformY));
		platformCount++;
		
		if(platformCount == currentBonusPlatformIndex) {
			CCSprite bonus = (CCSprite) getChildByTag(kBonusStartTag+currentBonusType);
			bonus.setPosition(CGPoint.ccp(x,currentPlatformY+30));
			bonus.setVisible(true);
		}
	}
	
	protected void resetHerbert(){

	    herbert_pos = new CGPoint();

	    herbert_pos.set(160, 160); 
		herbert.setPosition(herbert_pos);

		herbert_vel = new ccVertex2F();
		herbert_vel.setCGPoint(CGPoint.ccp(0,0));
		
		herbert_acc = new ccVertex2F();
		// FYI: edited this. herbert_acc.setCGPoint(CGPoint.ccp(0,-550.0f));
		herbert_acc.setCGPoint(CGPoint.ccp(0,-300.0f));
	    
		herbert.setScaleX(1.0f);
	}
	
	protected void resetBonus(){
		CCSprite bonus = (CCSprite) getChildByTag(kBonusStartTag+currentBonusType);
	    
		bonus.setVisible(false);
		currentBonusPlatformIndex += random.nextInt(kMaxBonusStep - kMinBonusStep) + kMinBonusStep;

	    
		if(score < 1000) {
			currentBonusType = 0;
		} else if(score < 5000) {
			currentBonusType = random.nextInt(2);
		} else if(score < 10000) {
			currentBonusType = random.nextInt(3);
		} else {
			currentBonusType = random.nextInt(2) + 2;
		}
	}
	
	public void step(float dt){
		if(gameSuspended) return;
	    		
		herbert_pos.set(herbert_pos.x += herbert_vel.getX() * dt, herbert_pos.y);
		
		if(herbert_vel.getX() < -30.0f && herbertLookingRight) {
			herbertLookingRight = false;
			herbert.setScaleX(-1.0f);
		} else if (herbert_vel.getX() > 30.0f && !herbertLookingRight) {
			herbertLookingRight = true;
			herbert.setScaleX(1.0f);
		}
	    
		CGSize herbert_size = herbert.getContentSize();
		float max_x = displayWidth-herbert_size.width/2;
		float min_x = 0+herbert_size.width/2;
		
		if(herbert_pos.x>max_x) herbert_pos.setX(max_x);
		if(herbert_pos.x<min_x) herbert_pos.setX(min_x);
	
		herbert_vel.setY(herbert_vel.getY() + herbert_acc.getY() * dt);
		herbert_pos.set(herbert_pos.x, herbert_pos.y + herbert_vel.getY() * dt);
		
		CCSprite bonus = (CCSprite) getChildByTag(kBonusStartTag+currentBonusType);
		if(bonus.getVisible()) {
			CGPoint bonus_pos = bonus.getPosition();
			// FY: edited this. float range = 30.0f;
			float range = 60.0f;
			if(herbert_pos.x > bonus_pos.x - range &&
			   herbert_pos.x < bonus_pos.x + range &&
			   herbert_pos.y > bonus_pos.y - range &&
			   herbert_pos.y < bonus_pos.y + range ) {
				switch(currentBonusType) {
					case kBonus5:   score += 5000;   break;
					case kBonus10:  score += 10000;  break;
					case kBonus50:  score += 50000;  break;
					case kBonus100: score += 100000; break;
				}
				
				scoreLabel.setString(String.valueOf(score));
	            playEffect("coin");
				resetBonus();
			}
		}
		
		int t;
		
		if(herbert_vel.getY() < 0) {
			
			for(t= kPlatformStartTag; t < kPlatformStartTag + kPlatformNumber; t++) {
				CCSprite platform = (CCSprite) getChildByTag(t);
	            
				CGSize platform_size = platform.getContentSize();
				CGPoint platform_pos = platform.getPosition();
				
				// evtl max & min eingrenzen fŸr mehr jump-accuracy? TODO
				max_x = platform_pos.x - platform_size.width/2;
				min_x = platform_pos.x + platform_size.width/2;
				float min_y = platform_pos.y + (platform_size.height+herbert_size.height)/2 - kPlatformTopPadding;
				
				
				if(herbert_pos.x > max_x &&
				   herbert_pos.x < min_x &&
				   herbert_pos.y > platform_pos.y &&
				   herbert_pos.y < min_y&&
				   herbert_pos.y != platform_pos.y &&
				   herbert_pos.x != platform_pos.y) {
					System.out.println("jump");
					jump();
				}
			}
			
			if(herbert_pos.y < -herbert_size.height/2) {
				gameOver();
			}
		} else if(herbert_pos.y > 240){
			
			float delta = herbert_pos.y - 240;
			herbert_pos.setY(240);
	        
			currentPlatformY -= delta;
	        	        
	        if (height > bestHeight){
	            highScoreLabel.setVisible(true);
	            highScoreLabel.setPosition(CGPoint.ccp(highScoreLabel.getPosition().x,highScoreLabel.getPosition().y-delta));
	        }
	        
	        if (highScoreLabel.getPosition().y < 0)
	            removeChild(highScoreLabel, true);
	        
			for(t = kPlatformStartTag; t < kPlatformStartTag + kPlatformNumber; t++) {
				CCSprite platform = (CCSprite) getChildByTag(t);
				CGPoint pos = platform.getPosition();
				pos = CGPoint.ccp(pos.x,pos.y-delta);
				if(pos.y < -platform.getContentSize().height/2) {
					currentPlatformTag = t;
					resetPlatform();
				} else {
					platform.setPosition (pos);
				}
			}
			 
			if(bonus.getVisible()) {
				CGPoint pos = bonus.getPosition();

				pos.set(pos.x, pos.y-delta );
				if(pos.y < -bonus.getContentSize().height/2) {
					resetBonus();
				} else {
					bonus.setPosition( pos);
				}
			}
			
			score += (int)delta;
	        height += (int)delta;

			scoreLabel.setString(String.valueOf(score));
			
		}

	    if (height == bestHeight){
	        highScoreLabel.setVisible(true);
		}

	    System.out.println("herbert_vel.y: "+herbert_vel.getY());
		herbert.setPosition(herbert_pos);
	}

	protected void playEffect(String sound){
		if (effectsOn){
			if (sound.equals("coin"))
			SoundEngine.sharedEngine().playEffect(parent.getContext(), R.raw.coin);
			else if (sound.equals("whoosh"))
			SoundEngine.sharedEngine().playEffect(parent.getContext(), R.raw.whoosh);
			else if (sound.equals("gameover"))
			SoundEngine.sharedEngine().playEffect(parent.getContext(), R.raw.gameover);
		}
	}
	
	protected void jump(){
	    playEffect("whoosh");
		//FYI: edited this. herbert_vel.setY(350.0f + Math.abs(herbert_vel.getX()));
	    herbert_vel.setY(400.0f + Math.abs(herbert_vel.getX()));
	}
	protected void gameOver(){
		playEffect("gameover");
		SoundEngine.sharedEngine().pauseSound();
		// why do I have to call parent here? o0 FIXME
		parent.unregisterSensorListener();
		
		boolean isHighScore=false;
		if (score > highScore){
			parent.storeSetting("highScore", score);
			isHighScore=true;
		}
		if (height > bestHeight)
			parent.storeSetting("bestHeight", height);
		
		CCDirector.sharedDirector().replaceScene(GameOverLayer.scene(parent, score,isHighScore ));
	}
	
}
