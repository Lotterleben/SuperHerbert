package de.piraten.superherbert;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.menus.CCMenu;
import org.cocos2d.menus.CCMenuItem;
import org.cocos2d.menus.CCMenuItemImage;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.sound.SoundEngine;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGSize;

// heißt im tutorial GameLayer

// TODO: MoveClouds
public class MenuLayer extends CCLayer /*implements Serializable*/{

	private StartActivity parent =null;
	private ArrayList<CCSprite> clouds;
	Random random;
	
	protected MenuLayer(StartActivity parent){
		//Looper.prepare();
		random = new Random();
		
		this.parent=parent;
		
		// FIXME der handler hiervon scheint Probleme zu bereiten.
	    CGSize winSize = CCDirector.sharedDirector().displaySize();
	    CCSprite background = CCSprite.sprite("background-menu-800x480.png");
	 
	    // _player durch player (/ background) ersetzt. let's hope this doesn't implode on me.
	    background.setPosition(CGPoint.ccp(background.getContentSize().width / 2.0f, winSize.height / 2.0f));
	    addChild(background);
	    
	    // Wolken schweben lassen
	    addClouds();
	    Timer timer = new Timer();
	    timer.schedule(new moveClouds(), 20, 20);
	    
	    // Menu Items hinzufügen.
	    // Syntax: CCMenuItemImage.item("button.png", "button-onclick.png", this, "nameDerOnclickMethode");
	    CCMenuItem spielen = CCMenuItemImage.item("btn_play@2x.png", "btn_play_over@2x.png", this, "spieleTouched");
	    CCMenuItem blog = CCMenuItemImage.item("btn_blog@2x.png", "btn_blog_over@2x.png", this, "blogTouched");
	    CCMenuItem pp = CCMenuItemImage.item("btn_pp@2x.png", "btn_pp_over@2x.png", this, "ppTouched");
	    CCMenuItem wp = CCMenuItemImage.item("btn_wp@2x.png", "btn_wp_over@2x.png", this, "wpTouched");
	    CCMenuItem settings = CCMenuItemImage.item("btn_settings@2x.png", "btn_settings_over@2x.png", this, "settingsTouched");

	    if (parent !=null)System.out.println("OHMYGLOB parent");
	    
	    if (parent.isMusicOn().get())
	        SoundEngine.sharedEngine().playSound(parent.getContext(), R.raw.menu, true);

	    // erstelle Menu mit übergebenen MenuItems?
	    CCMenu menu = CCMenu.menu(spielen,blog,pp,wp,settings);
	    menu.setPosition(CGPoint.ccp(background.getContentSize().width / 2.0f, winSize.height / 3.4f));
	    menu.alignItemsVertically((float)0.8);
	    addChild(menu);
	    
	}
	
	public void spieleTouched(Object sender){
		CCDirector.sharedDirector().replaceScene(JumpGameLayer.scene(parent));

	}
	
	public void blogTouched(Object sender){
		parent.openURL("http://www.herbert-foerster.de");
	}

	public void ppTouched(Object sender){
		parent.openURL("http://www.piratenpartei-frankfurt.de");
	}
	
	public void wpTouched(Object sender){
		parent.openURL("http://www.piratenpartei-frankfurt.de/content/wahlprogramm");
	}
	
	public void settingsTouched(Object sender){
		System.out.println("settingsTouched");
		CCDirector.sharedDirector().replaceScene(SettingsLayer.scene(parent));
	}
	
	public void addClouds(){
		clouds=new ArrayList<CCSprite>();
		CCSprite cloud;
		for (int i = 0; i < 5; i++){
			switch (random.nextInt(3)){
				case 0:
					cloud=CCSprite.sprite("menu-cloud-1.png");
					cloud.setTag(1);
					break;
				case 1:
					cloud=CCSprite.sprite("menu-cloud-2.png");
					cloud.setTag(2);
					break;
				case 2:
					cloud=CCSprite.sprite("menu-cloud-3.png");
					cloud.setTag(3);
					break;
				default:
					cloud=CCSprite.sprite("menu-cloud-1.png");
					cloud.setTag(3);
					break;
			}
			cloud.setPosition(CGPoint.ccp(random.nextInt(380) +100 , random.nextInt(380) +20));
		
			addChild(cloud);
		
			// Sprites im array speichern, um sie bewegen zu können
			clouds.add(cloud);
		}
	}
	
	class moveClouds extends TimerTask {
		public void run(){
		    for (int i = 0; i < clouds.size(); i++){
		    	CCSprite cloud = clouds.get(i);
		        
		        // Move according to cloud size
		        switch (cloud.getTag()){
		        case 1:
		        	cloud.setPosition(CGPoint.ccp(cloud.getPosition().x+(float)0.5, cloud.getPosition().y));
		            break;
		        case 2:
		        	cloud.setPosition(CGPoint.ccp(cloud.getPosition().x+(float)0.3, cloud.getPosition().y));
		        	break;
		        case 3:
		        	cloud.setPosition(CGPoint.ccp(cloud.getPosition().x+(float)0.1, cloud.getPosition().y));
		            break;
		        }
		        
		        // falls das Ende des Bildschirms erreicht ist:
		        if (cloud.getPosition().x >730){
		        	// % 220 um den float < 220 zu haben
		        	cloud.setPosition(CGPoint.ccp(-480, random.nextInt(220)+20));
		        }
		    }
		}
	}
	
	
	public static CCScene scene(StartActivity parent){
	    CCScene scene = CCScene.node();
	    // übergeben die aufrufende StartActivity dem neuen MenuLayer-Objekt.
	    // callback-hack, damit das mit dem URLs öffnen funktioniert.
	    CCLayer layer = new MenuLayer(parent);
	    scene.addChild(layer);
	 
	    return scene;

	}

}
