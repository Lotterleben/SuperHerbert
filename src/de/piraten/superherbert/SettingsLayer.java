package de.piraten.superherbert;

import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.menus.CCMenu;
import org.cocos2d.menus.CCMenuItem;
import org.cocos2d.menus.CCMenuItemImage;
import org.cocos2d.menus.CCMenuItemToggle;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGSize;

public class SettingsLayer extends CCLayer {
	StartActivity parent=null;
    // TODO: Bildchen je nach status on/off anzeigen
    CCMenuItem music=null;
    CCMenuItem effects=null;
    CCMenuItemToggle musicSwitch=null;
    CCMenuItemToggle effectSwitch=null;

	
	protected SettingsLayer(StartActivity parent){
		this.parent=parent;
		
	    CGSize winSize = CCDirector.sharedDirector().displaySize();
	    CCSprite background = CCSprite.sprite("background-800x480.png");

	    background.setPosition(CGPoint.ccp(background.getContentSize().width / 2.0f, winSize.height / 2.0f));
	    addChild(background);
	    
	    // Determine how to display Settingtoggledingenskirchen
	    CCMenuItem	onMusic = CCMenuItemImage.item("opt_music_on@2x.png", "opt_music_on@2x.png", this, "toggleMusic");
	    CCMenuItem	offMusic = CCMenuItemImage.item("opt_music_off@2x.png", "opt_music_off@2x.png", this, "toggleMusic");
		musicSwitch = CCMenuItemToggle.item(this, "toggleMusic", onMusic, offMusic);

		CCMenuItem onEffects = CCMenuItemImage.item("opt_effects_on@2x.png", "opt_effects_on@2x.png", this, "toggleEffects");
		CCMenuItem offEffects = CCMenuItemImage.item("opt_effects_off@2x.png", "opt_effects_off@2x.png", this, "toggleEffects");
		effectSwitch = CCMenuItemToggle.item(this, "toggleEffects", onEffects, offEffects);

		
	    CCMenuItem back = CCMenuItemImage.item("btn_done@2x.png", "btn_done_over@2x.png", this, "done");
	    
	    CCMenu menu = CCMenu.menu(musicSwitch, effectSwitch, back);
	    menu.setPosition(CGPoint.ccp(background.getContentSize().width / 2.0f, winSize.height / 3.0f));
	    menu.alignItemsVertically((float)2.5);
	    addChild(menu);
	    
	    updateSwitches();
		
	}
	
/*	private void updateSwitches() {
		// TODO Auto-generated method stub
		boolean musicStatus= parent.getSetting("musicOn");
		//System.out.println(musicSwitch.selectedIndex());
		// zum testen, wenns klappt TODO getSetting umschreiben
		System.out.println("musicStatus: "+musicStatus);
	}*/

	public void toggleMusic(Object sender){
		parent.bgMusicClicked();		
	}
	
	public void toggleEffects(Object sender){
		// Turn effects on/off FIXME
		parent.effectsClicked();

	}
	
	public void done(Object sender){
		// back to teh menuez
		CCDirector.sharedDirector().replaceScene(MenuLayer.scene(parent));

	}
	
    public static CCScene scene(StartActivity parent)
    {
        CCScene scene = CCScene.node();
    	// Callback durchreichen, brauchen wir für das Zurückkehren zum MenuLayer
        SettingsLayer layer = new SettingsLayer(parent);
        System.out.println("new SettingsLayer");
        scene.addChild(layer);
 
        return scene;
    }
    
    public void updateSwitches(){
    	// FIXME works, but sure ain't pretty.
    	if (parent.getSetting("musicOn"))
    		musicSwitch.setSelectedIndex(0);
    	else
    		musicSwitch.setSelectedIndex(1);
    	
    	if (parent.getSetting("effectsOn"))
    		effectSwitch.setSelectedIndex(0);
    	else
    		effectSwitch.setSelectedIndex(1);
    }
}
