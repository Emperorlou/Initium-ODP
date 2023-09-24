// Uses visibility API, which should work on most browsers/versions from 2011 to today.
this.TabActive = function()
{
	// Set the name of the hidden property and the change event for visibility
	var hidden; 
	if (typeof document.hidden !== "undefined") // Opera 12.10 and Firefox 18 and later support 
	  hidden = "hidden";
	else if (typeof document.mozHidden !== "undefined") 
	  hidden = "mozHidden";
	else if (typeof document.msHidden !== "undefined")
	  hidden = "msHidden";
	else if (typeof document.webkitHidden !== "undefined") 
	  hidden = "webkitHidden";
	else
	{
		// Not sure which browser/version we're using... We'll just assume it's active.
		return true;
	}
	
	return document[hidden] == false;
}

// Represents the Notification info, to be used by whatever impl, whether JS, Cortava, etc.
function NotificationEvent(iconUrl, title, body, category, options)
{
	this.icon = iconUrl;
	this.title = title;
	this.body = body;
	this.category = category;
	this.options = options;
	
	// If no handlers specified for this specific request, or no global handlers present,
	// then we won't bother attaching any handlers.
	this.onclick = null;
	this.onerror = null;
}

function PopupNotifications()
{
	var _self = this;
	
	// Default icon to show in the notification
	this.icon = "favicon.ico";
	
	/**
	 * Request permission from the implementing Notification API.
     * Args: NotificationEvent object
     */
	this.requestPermission = null;
	
	/**
	 * Fires when Notification permissions are denied and a notification is still sent.
     * Args: NotificationEvent object
     */
	this.onDenied = null
	
	/**
	 * Notify implementation. Should handle onDenied if permissions were denied.
	 * Does not fire if user disabled notifications in settings, or tab is active.
     * Args: NotificationEvent object
     */
	this.onNotification = null;
	
	/**
	 * Default click handler. Can attach custom click handler on individual notifications.
     * Args: Notification object
     */
	this.defaultClick = null;
	
	/**
	 * Default error handler. Can attach custom error handler on individual notifications.
     * Args: Notification object
     */
	this.defaultError = null;
	
	this.popupNotify = function(iconUrl, title, body, category, options, onclick, onerror)
	{
		if(iconUrl == null || iconUrl == "") iconUrl = _self.icon;
		
		if(TabActive()) return;
		
		var userDisabled = localStorage.getItem("UserDisableNotifications");
		if(userDisabled != null && userDisabled == true) return;
		
		opt = options || {};
		var notification = new NotificationEvent(iconUrl, title, body, category, opt);
		
		// You can specify a Notification specific click handler or a global handler.
		// If neither are specified, then nothing happens from the click, in terms
		// of Initium functions (window should still focus).
		notification.onclick = onclick || _self.defaultClick || null;
		notification.onerror = onerror || _self.defaultError || null;
		
		// Should definitely exist, but the implementation might exist when we 
		// fire the notification instead of before, so allow this for now.
		if(_self.requestPermission != null)
			_self.requestPermission();
		
		_self.onNotification(notification);
	}
};

// Allow Ajax loaded scripts to be cached.
//jQuery.cachedScript = function( url, options ) {
//	// Allow user to set any option except for dataType, cache, and url
//	options = $.extend( options || {}, {
//		dataType: "script",
//		cache: true,
//		url: url
//	});
// 
//	// Use $.ajax() since it is more flexible than $.getScript
//	// Return the jqXHR object so we can chain callbacks
//	return jQuery.ajax( options );
//};

var notificationScript = "/odp/javascript/BrowserPopupNotifications-impl.js";
/* Default to browser popup notifications. If any other version, set the associated script location.
 * 
 * if(checkIsPhonegap) notificationScript = "/odp/javascript/GCMPopupNotifications-impl.js";
 * else if(checkIsIphone) notificationScript = "/odp/javascript/APNPopupNotifications-impl.js";
 */

// Load the detected notification version script. Each impl should have a 
// SetNotificationHandler method that instantiates and assigns the global notifyHandler object.
//$.cachedScript(notificationScript, {async:false}).done(function() {
//	SetNotificationHandler();
//});

$(document).ready(function(){
	SetNotificationHandler();
});
