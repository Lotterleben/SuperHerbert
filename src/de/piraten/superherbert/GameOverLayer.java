package de.piraten.superherbert;

import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.menus.CCMenu;
import org.cocos2d.menus.CCMenuItem;
import org.cocos2d.menus.CCMenuItemImage;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCLabel;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCLabel.TextAlignment;
import org.cocos2d.sound.SoundEngine;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGSize;

public class GameOverLayer extends CCLayer {
	StartActivity parent=null;
	int score;
	boolean isHighScore;
	
	
	protected GameOverLayer(StartActivity parent,int score, boolean isHighScore){
		this.parent=parent;
		this.score=score;
		this.isHighScore=isHighScore;
		
	    CGSize winSize = CCDirector.sharedDirector().displaySize();
	    CCSprite background = CCSprite.sprite("background_gameover-800x480.png");	   

	    background.setPosition(CGPoint.ccp(background.getContentSize().width / 2.0f, winSize.height / 2.0f));
	    addChild(background);
	    
	    if ( parent.isMusicOn().get() ){
	    	SoundEngine.sharedEngine().playSound(parent.getContext(), R.raw.menu, true);
	    	// release Game sound to save Memory
	    	SoundEngine.sharedEngine().realesSound(R.raw.background);
	    }

	    String scoreString = "Du hast "+score+" Punkte erreicht.";
        if (isHighScore)
            scoreString += " Das ist deine neue Bestmarke!";
        
        CCLabel scoreView = CCLabel.makeLabel(scoreString, CGSize.make(300, 200), TextAlignment.LEFT, 
        		"Arial", 20.0f);
        scoreView.setPosition(CGPoint.ccp(230,80));
        addChild(scoreView);
	    
	    CCMenuItem repeat = CCMenuItemImage.item("btn_repeat@2x.png", "btn_repeat_over@2x.png", this, "repeatGame");
	    CCMenuItem done = CCMenuItemImage.item("btn_done@2x.png", "btn_done_over@2x.png", this, "done");
	    
	    CCMenu menu = CCMenu.menu(repeat,done);
	    menu.setPosition(CGPoint.ccp(background.getContentSize().width / 2.0f, winSize.height / 3.0f));
	    menu.alignItemsVertically((float)0.8);
	    addChild(menu);
	    
	}
	
	public void done(Object sender){
		// back to teh menuez
		CCDirector.sharedDirector().replaceScene(MenuLayer.scene(parent));

	}
	
	public void repeatGame(Object sender){
		CCDirector.sharedDirector().replaceScene(JumpGameLayer.scene(parent));	
	}
	
	public void onPause(){
		System.out.println("onPause");
	}
	
	public void onDestroy(){
		System.out.println("onDestroy");
	}
	
    public static CCScene scene(StartActivity parent, int score, boolean isHighScore){
        CCScene scene = CCScene.node();
    	// Callback durchreichen, brauchen wir für das Zurückkehren zum MenuLayer
        GameOverLayer layer = new GameOverLayer(parent, score, isHighScore);
  
        scene.addChild(layer);
 
        return scene;
    }

}
