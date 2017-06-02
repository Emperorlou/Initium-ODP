package com.universeprojects.miniup.server.services;

import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.OperationBase;

/**
 * This is a helper class to easily call javascript on the client from the server through long operations or commands.
 * 
 * @author Owner
 *
 */
public class OperationJSService
{
	final private OperationBase operation;
	
	public OperationJSService(OperationBase operation)
	{
		this.operation = operation;
	}
	
	public void playSoundEffect(String soundId)
	{
		playSoundEffect(soundId, null, null);
	}
	
	public void playSoundEffect(String soundId, Double volume)
	{
		playSoundEffect(soundId, volume, null);
	}

	/**
	 * Plays a sound effect on the client.
	 * 
	 * @param soundId The "name" of the sound effect. You can find the various names of sounds in soundeffects.js. You can only play sound effects that are already listed in the code.
	 * @param volume A value between 0 and 1 for the volume.
	 * @param delayMs How long to delay this sound effect in milliseconds (useful for things like thunder).
	 */
	public void playSoundEffect(String soundId, Double volume, Integer delayMs)
	{
		String js = "playAudio('"+soundId+"', "+volume+", "+delayMs+")";
		operation.addJavascriptToResponse(js);
	}

	public void playBannerFx(String animationUrl)
	{
		playBannerFx(animationUrl, false, false);
	}

	/**
	 * Plays an animated gif over the the banner area. Usually used for things like combat effects. 
	 * 
	 * @param animationUrl This can be a relative url or the full url. If it is a relative url, the resource server will be added to complete the url.
	 * @param flipX Flip the image on the horizontal axis or no.
	 * @param flipY Flip the image on the vertical axis or no.
	 */
	public void playBannerFx(String animationUrl, boolean flipX, boolean flipY)
	{
		String js = "playBannerFx('"+GameUtils.getResourceUrl(animationUrl)+"', "+flipX+", "+flipY+")";
		operation.addJavascriptToResponse(js);
	}
	
	/**
	 * This issues a desktop notification that when clicked will bring the player back to the initium game tab. 
	 * 
	 * The category to use is important because if a notification is issued that is in the SAME category as 
	 * a notification that has is already on the screen, this new notification will replace the old one instead 
	 * of appearing in addition to it. This allows us to display only the LATEST message in a particular category
	 * of messages.
	 * 
	 * @param text The text that will be in the desktop notification.
	 * @param category See the category explanation above.
	 */
	public void simpleDesktopNotification(String text, String category)
	{
		String js = "doSimpleDesktopNotification('"+text+"', '"+category+"')";
		operation.addJavascriptToResponse(js);
	}
}
