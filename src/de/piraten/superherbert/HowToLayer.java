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
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGSize;

public class HowToLayer extends CCLayer{
	StartActivity parent=null;

	protected HowToLayer(StartActivity parent){
		this.parent=parent;
	    CGSize winSize = CCDirector.sharedDirector().displaySize();

	    String bg = "background-800x480.png";
	    System.out.println(parent);
		if (parent.displayWidth() > 480f)
			bg = "background_720.png";
		/* else if (parent.displayWidth() < 480)
			bg = "background.png";*/
	    CCSprite background = CCSprite.sprite(bg);
	    
	    background.setPosition(CGPoint.ccp(background.getContentSize().width / 2.0f, winSize.height / 2.0f));
	    addChild(background);

	    String btn ="@2x.png";
	    if (parent.displayWidth()>480)  
	    	btn="-huge.png";
	    //else if(parent.displayWidth() < 480) 
	    //	btn=".png";
		
	    String helpString = "GerŠt nach links/rechts neigen, um herbert zu steuern.";
        
        CCLabel helpText = CCLabel.makeLabel(helpString, CGSize.make(300, 200), TextAlignment.LEFT, 
        		"Arial", 27.0f);
        helpText.setPosition(CGPoint.ccp(parent.displayWidth()/2 ,parent.displayHeight() / 2));
        addChild(helpText);
	    
	    
	    CCMenuItem ok = CCMenuItemImage.item("btn_done"+btn, "btn_done_over"+btn, this, "ok");
	    
	    CCMenu menu = CCMenu.menu(ok);
	    menu.setPosition(CGPoint.ccp(background.getContentSize().width / 2.0f, parent.displayHeight() / 3.0f));
	    menu.alignItemsVertically((float)0.8);
	    addChild(menu);
	}
	
	public void ok(Object sender){
		// back to teh menuez
		CCDirector.sharedDirector().replaceScene(JumpGameLayer.scene(parent));	
	}
	
    public static CCScene scene(StartActivity parent){
        CCScene scene = CCScene.node();
    	// Callback durchreichen, brauchen wir fŸr das ZurŸckkehren zum MenuLayer
        HowToLayer layer = new HowToLayer(parent);
  
        scene.addChild(layer);
 
        return scene;
    }
}
