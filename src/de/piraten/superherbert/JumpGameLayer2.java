package de.piraten.superherbert;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.cocos2d.actions.instant.CCCallFuncN;
import org.cocos2d.actions.interval.CCMoveTo;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;

import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.sound.SoundEngine;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccVertex2F;

import android.hardware.SensorEvent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

// TODO: was ist mit onpause(), onResume(), onCreate()? (->sensorinfo-code übertragen!)
// TODO: bei quit durch exception senosreventlistener unsubscriben
// CCLayer implementiert SensorEventListener schon!!

public class JumpGameLayer2 extends CCLayer{
	final int kPlatformStartTag = 200;
	final int kBonusStartTag = 300;

	final int kPlatformNumber = 25;
	final int kMinPlatformStep = 50;
	final int kMaxPlatformStep = 320;
	final int kPlatformTopPadding = 5;

	final int kMinBonusStep = 30;
	final int kMaxBonusStep = 50;
	final int kHerbertTag = 4;

	/*protected enum bonuses{
		kBonus5 , kBonus10, kBonus100k,kNumBonuses;
	}*/

	StartActivity parent=null;
	CCSprite herbert=null;
	CCSprite platform=null;
	CCSprite highscoreLabel=null;
	private boolean effectsOn=false;
	FlyThread flyThread;
	Handler handler;
	// timer tick rate FIXME: finetuning 
	//float dt = 1.0f;

	private int currentPlatformTag = 0;


	private CGPoint herbertPos;
	private ccVertex2F herbertVel;
	private ccVertex2F herbertAcc;

	private float currentPlatformY;
	private float currentMaxPlatformStep;
	private int currentBonusPlatformIndex;
	private int currentBonusType;
	private int platformCount;
	private boolean herbertLookingRight;

	int score=0;
	int highScore=0;
	int height=0;
	int bestHeight=0;

	//CCTimer my ass
	Timer flyTimer = new Timer();
	private boolean mInitialized= false;
	private final float NOISE = (float) 2.0;

	private Random random;

	protected JumpGameLayer2(StartActivity parent){

		this.parent=parent;

		random = new Random();

		// rate ist recht randomly gewählt. FIXME
		enableAccelerometerWithRate(1);

		CGSize winSize = CCDirector.sharedDirector().displaySize();
		CCSprite background = CCSprite.sprite("background-800x480.png");

		effectsOn=parent.getSetting("effectsOn");
		if (effectsOn){
			SoundEngine.sharedEngine().preloadEffect(parent.getContext(), R.raw.coin);	    
			SoundEngine.sharedEngine().preloadEffect(parent.getContext(), R.raw.gameover);
			SoundEngine.sharedEngine().preloadEffect(parent.getContext(), R.raw.whoosh);
		}

		// Graphics
		background.setPosition(CGPoint.ccp(background.getContentSize().width / 2.0f, winSize.height / 2.0f));
		addChild(background);

		if (parent.isMusicOn().get()){
			System.out.println("parent.isMusicOn().get():"+parent.isMusicOn().get());
			SoundEngine.sharedEngine().playSound(parent.getContext(), R.raw.background, true);
		}

		initPlatforms();
		herbertPos= new CGPoint();
		herbertVel = new ccVertex2F();
		herbertAcc= new ccVertex2F();

		// this exists for debug reasons
		/*CCMenuItem tmp = CCMenuItemImage.item("highscore-hd.png", "highscore-hd.png", this, "gameOver");

	    CCMenu menu = CCMenu.menu(tmp);
	    menu.setPosition(CGPoint.ccp(background.getContentSize().width / 2.0f, winSize.height / 3.0f));
	    menu.alignItemsVertically((float)0.8);
	    addChild(menu); */
		// /debug

		herbert = CCSprite.sprite("dude_left-1-hd.png");
		herbert.setPosition(CGPoint.ccp(herbert.getContentSize().width / 0.52f, 0f));
		herbert.setTag(kHerbertTag);
		addChild(herbert, 3);

		//bonus = CCSprite.sprite("coin-hd.png");
		//bonus.setTag(kBonusStartTag);

		// TODO: scorelabel  
		for( int i = 0; i<4; i++ ){
			CCSprite bonus = CCSprite.sprite("coin.png");
			bonus.setTag(kBonusStartTag+i);
			addChild(bonus, 2);

			System.out.println("new Bonus: "+bonus);

			bonus.setVisible(false);
		}

		highscoreLabel = CCSprite.sprite("highscore-hd.png");
		highscoreLabel.setPosition(250,470);
		highscoreLabel.setVisible(false);
		highscoreLabel.setTag(999);
		addChild(highscoreLabel, 2);

		System.out.println("children:"+getChildren());

		resetPlatforms();
		resetHerbert();
		resetBonus();

		// TODO best height auslesen (und speichern. duh :D )

		//flyingHerbert();
		//flyThread= new FlyThread(/*this*/);
		//flyThread.start();

		this.schedule("stepTask", 1.0f);

	}

	public void gameOver(Object sender){
		// switch to game over scene
		// TODO gamemusik ausschalten
		if (effectsOn)
			SoundEngine.sharedEngine().playEffect(parent.getContext(), R.raw.gameover);

		//Message quit = new Message();		
		//beginningquit.what=0;
		//handler.sendMessageAtFrontOfQueue(quit);
		
		//CCDirector.sharedDirector().replaceScene(GameOverLayer.scene(parent));

	}

	public static CCScene scene(StartActivity parent)
	{

		CCScene scene = CCScene.node();

		// Callback durchreichen, brauchen wir für GameOverLayer
		JumpGameLayer2 layer = new JumpGameLayer2(parent);

		scene.addChild(layer);
		return scene;
	}

	// this method exists for testing reasons.
	public void flyingHerbert(){

		// Parameter: ( duration, new position)
		CCMoveTo actionMove = CCMoveTo.action(3, CGPoint.ccp(-herbert.getContentSize().width / 2.0f, 100));

		// Herbert ist abgestürzt-> callbackfunktion ruft gameOver() auf
		CCCallFuncN actionMoveDone = CCCallFuncN.action(this, "gameOver");
		CCSequence actions = CCSequence.actions(actionMove, actionMoveDone);

		herbert.runAction(actions);	
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float accelFilter = 0.1f;
		float accX = event.values[0];

		herbertVel.setX(herbertVel.getX() * accelFilter + accX * (1.0f - accelFilter) * 500.0f);

		/*if (!mInitialized) {
			synchronized(herbertVel){
				//herbertVel.x = x;
				herbertVel.setX(accX);
			}
			mInitialized = true;
		} else {
			float deltaX = Math.abs(herbertVel.getX() - accX);
			if (deltaX < NOISE) deltaX = (float)0.0;
			synchronized(herbertVel){
				herbertVel.setX(accX);
			}
		}	*/

		/*Message msg = new Message();
		msg.obj=(Float)mLastX;

		// spezifzieren dass es sich um Koordinate, nicht um quit-msg handelt
		msg.what=1;
		handler.sendMessage(msg);	*/	
	}

	///////// HERBERT BEWEGUNG HILFSMETHODEN /////////////////////////////////

	protected void initPlatforms(){
		currentPlatformTag = kPlatformStartTag;
		// disclaimer: der Autor, von dem plaetzchen den Code gemopst hat hat das verbrochen.
		while (currentPlatformTag < kPlatformStartTag + kPlatformNumber){
			initPlatform();
			currentPlatformTag++;
		}
	}

	protected void initPlatform() {
		CCSprite platform=null;
		switch (random.nextInt(3)){
		case 0:
			platform = CCSprite.sprite("cloud-small-hd.png");
			platform.setTag(currentPlatformTag);
			break;
		case 1:
			platform = CCSprite.sprite("cloud-medium-hd.png");
			platform.setTag(currentPlatformTag);
			break;
		case 2:
			platform = CCSprite.sprite("cloud-big-hd.png");
			platform.setTag(currentPlatformTag);
			break;
		}
		platform.setTag(currentPlatformTag);

		// speichere woelkchen in der CCLayer-eigenen Liste & male es
		addChild(platform, 1);
	}

	private void resetPlatforms(){
		currentPlatformY = -1;
		currentPlatformTag = kPlatformStartTag;
		currentMaxPlatformStep = 60.0f;
		currentBonusPlatformIndex = 0;
		currentBonusType = 0;
		platformCount = 0;

		while(currentPlatformTag < kPlatformStartTag + kPlatformNumber) {
			resetPlatform();
			currentPlatformTag++;
		}

	}

	private void resetPlatform(){
		if(currentPlatformY < 0) {
			currentPlatformY = 30.0f;
		}else{
			currentPlatformY += random.nextInt((int)(currentMaxPlatformStep - kMinPlatformStep) + kMinPlatformStep) ;
			if(currentMaxPlatformStep < kMaxPlatformStep) {
				currentMaxPlatformStep += 0.5f;
			}
		}

		CCSprite platform = (CCSprite) getChildByTag((int)currentPlatformTag);
		//System.out.println(platform);

		if(random.nextInt(2)==1) 
			platform.setScaleX(-1.0f);

		float x;
		CGSize size = platform.getContentSize();
		if(currentPlatformY == 30.0f) {
			x = 160.0f;
		} else {
			x = random.nextInt(320-(int)size.width) + size.width/2;
		}

		platform.setPosition(CGPoint.ccp(x,currentPlatformY));
		platformCount++;

		if(platformCount == currentBonusPlatformIndex) {

			//CCSprite bonus = (CCSprite) getChildByTag(kBonusStartTag+currentBonusType);
			CCSprite bonus = (CCSprite) getChildByTag(300);
			System.out.println(bonus);
			bonus.setPosition(CGPoint.ccp(x,currentPlatformY+30));
			bonus.setVisible(true);
		}
	}

	private void resetHerbert(){
		// what. thefuck.
		//CCSprite spriteHerbert = (CCSprite) getChildByTag(kHerbertTag);
		//System.out.println("herbert:" +spriteHerbert);

		//System.out.println(herbertPos);

		herbertPos.setX(160);
		herbertPos.setY(160);
		//System.out.println(herbertPos);


		// not sure if this is correct (TODO)
		herbert.setPosition(herbertPos);

		herbertVel.setY(0f);
		herbertVel.setX(0f);

		herbertAcc.setX(0f);
		herbertAcc.setY(-550.0f);

		// FYI: left the herbert_acc.x/y out

		herbert.setScaleX(1.0f);
	}

	private void resetBonus(){
		System.out.println("kBonusStartTag: "+ kBonusStartTag);
		System.out.println("currentBonusType: "+currentBonusType);
		CCSprite bonus = (CCSprite) getChildByTag((int)(kBonusStartTag+currentBonusType));

		bonus.setVisible(false);
		currentBonusPlatformIndex += random.nextFloat() % (kMaxBonusStep - kMinBonusStep) + kMinBonusStep;


		if(score < 1000) {
			currentBonusType = 0;
		} else if(score < 5000) {
			currentBonusType = random.nextInt() % 2;
		} else if(score < 10000) {
			currentBonusType = random.nextInt() % 3;
		} else {
			currentBonusType = random.nextInt() % 2 + 2;
		}
	}

	private void jump(){
		System.out.println("jump! jump!");
		SoundEngine.sharedEngine().playEffect(parent.getContext(), R.raw.whoosh);
		//herbertVel.y = 350.0f + fabsf(herbertVel.x);
		herbertVel.setY(350.0f + Math.abs(herbertVel.getX()));

	}

	public void stepTask(float dt)
	{
		System.out.println("pos: "+herbertPos +"+x: "+herbertPos.x);
		System.out.println("acc: "+herbertAcc.getX()+" "+herbertAcc.getY());
		System.out.println("vel: "+herbertVel.getX()+" "+herbertVel.getY());


		herbertPos.setX(herbertPos.x+ herbertVel.getX() * dt);
		if(herbertVel.getX() < -30.0f && herbertLookingRight) {
			herbertLookingRight = false;
			herbert.setScaleX(-1.0f);
		} else if (herbertVel.getX() > 30.0f && !herbertLookingRight) {
			System.out.println("herbertlookingright");
			herbertLookingRight = true;
			herbert.setScaleX(1.0f);
		}
		CGSize herbertSize = herbert.getContentSize();
		float max_x = 320-herbertSize.width/2;
		float min_x = 0+herbertSize.width/2;        

		if(herbertPos.x>max_x) herbertPos.setX(max_x);
		if(herbertPos.x<min_x) herbertPos.setX(min_x);

		System.out.println("herbertPos.y vor:"+herbertPos);
		System.out.println("dt: "+dt);

		System.out.println("herbertVel.getY() "+herbertVel.getY());
		System.out.println("herbertAcc.getY() "+herbertAcc.getY());
		
		// ich glaub hier liegt der wurm. FIXME
		herbertVel.setY(herbertVel.getY() + herbertAcc.getY() * dt);
		System.out.println("herbertVel.getY() "+herbertVel.getY());
		herbertPos.setY(herbertPos.y + herbertVel.getY() * dt);
		System.out.println("herbertPos.y: "+ herbertPos.y);
		// TODO: boni setzen
		CCSprite bonus = (CCSprite) getChildByTag(kBonusStartTag+currentBonusType);
		if(bonus.getVisible()){
			CGPoint bonusPos = bonus.getPosition();
			float range = 30.0f;

			if( 
					herbertPos.x > bonusPos.x - range &&
					herbertPos.x < bonusPos.y + range &&
					herbertPos.y > bonusPos.y - range &&
					herbertPos.y < bonusPos.y + range ) {
				switch((int)currentBonusType) {
				case 0:   score += 5000;   break;
				case 1:  score += 10000;  break;
				case 2:  score += 50000;  break;
				case 3: score += 100000; break;
				}

				/* TODO (highscorestring)
			NSString *scoreStr = [NSString stringWithFormat:@"%d",score];
			[scoreLabel setString:scoreStr];
            [self playEffect:@"coin.mp3"];
			[self resetBonus];*/
			}

		}

		int t;

		if( herbertVel.getY() < 0) {

			// FYI: not sure if correct
			for(t=kPlatformStartTag; t < (kPlatformStartTag + kPlatformNumber); t++) {
				// erstelle neue Plattform& ziehe Plattform aus wölkchenliste
				System.out.println("beginning advanced woelkchen detection..");
				CCSprite platform = (CCSprite) getChildByTag(t);

				CGSize platformSize = platform.getContentSize();
				CGPoint platformPos = platform.getPosition();

				//max_x = platformPos.x - platformSize.width/2;
				max_x = platformPos.x - platformSize.getWidth()/2;
				
//				min_x = platformPos.x + platformSize.width/2;
				min_x = platformPos.x + platformSize.getWidth()/2;
				
				//float min_y = platformPos.y + (platformSize.height+herbertSize.height)/2 - kPlatformTopPadding;
				float min_y = platformPos.y + (platformSize.getHeight()+herbertSize.getHeight())/2 - kPlatformTopPadding;

				System.out.println("herbertpos.x= "+herbertPos.x+" herbertpos.y= "+herbertPos.y);
				System.out.println("max_x= "+max_x+" min_x= "+min_x);
				System.out.println("platformPos.y= "+platformPos.y);
				if(herbertPos.x > max_x &&
						herbertPos.x < min_x &&
						herbertPos.y > platformPos.y &&
						herbertPos.y < min_y) {
					jump();
				}
			}

			if(herbertPos.y < -herbertSize.height/2) {
				gameOver(this);
			}

		} else if(herbertPos.y > 240) {

			float delta = herbertPos.y - 240;
			herbertPos.setY(240);

			currentPlatformY -= delta;

			if (height > bestHeight){
				highscoreLabel.setVisible(true);
				// FIXME (maybe. not sure if this is right.)
				highscoreLabel.setPosition(CGPoint.ccp(highscoreLabel.getPosition().x,highscoreLabel.getPosition().y-delta));
			}


			if (highscoreLabel.getPosition().y < 0)
				removeChild(highscoreLabel, true);

			for(t=kPlatformStartTag; t < kPlatformStartTag + kPlatformNumber; t++) {
				// I'm 21 and what is this. 
				CCSprite platform = (CCSprite) getChildByTag(t);

				CGPoint pos = platform.getPosition();
				pos = CGPoint.ccp(pos.x,pos.y-delta);
				if(pos.y < -platform.getContentSize().height/2) {
					currentPlatformTag = t;
					resetPlatform();
				} else {
					platform.setPosition( pos );
				}
			}

			if(bonus.getVisible()) {
				CGPoint pos = bonus.getPosition();
				pos.y -= delta;
				if(pos.y < -bonus.getContentSize().height/2) {
					resetBonus();
				} else {
					bonus.setPosition(pos);
				}
			}

			//score += (int)delta;
			height += (int)delta;
			//NSString *scoreStr = [NSString stringWithFormat:@"%d",score];
			//[scoreLabel setString:scoreStr];
		}

		if (height == bestHeight){
			highscoreLabel.setVisible(true);
		}
		herbert.setPosition(herbertPos);
	}
	

	// DEPRECATED (methinks. weep.)
	// not sure f I really *need* a thread , but I thought it was prettier.
	public class FlyThread extends Thread{
		public void run(){
			Looper.prepare();

			//flyTimer.scheduleAtFixedRate(step,0,dt);

			//this.schedule("stepTask", 1.0f);

			handler = new Handler(){
				public void handleMessage(Message msg){
					// switch kann weg. nur noch auf quit überprüfen.TODO
					switch(msg.what){
					case 0:
						parent.unregisterSensorListener();
						break;
					case 1:
						System.out.println("handler sez "+msg.obj);
						//step((Float)msg.obj);
						break;		
					}
				}
			};

			Looper.loop();

		}
	}

}



//}

