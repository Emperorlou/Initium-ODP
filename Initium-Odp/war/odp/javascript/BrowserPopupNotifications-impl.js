// If we don't have Notification but we do have webkitNotifications, the interfaces should be the same so just use it
if (typeof window.Notification === 'undefined' && window.webkitNotifications !== 'undefined')
	window.Notification = window.webkitNotifications;

// The Browser notification handler.
// Uses the Notification API: https://developer.mozilla.org/en-US/docs/Web/API/notification
function BrowserPopupNotifications()
{
	var handler = new PopupNotifications();

	// Notification.requestPermission will exit immediately if permission === "denied",
	// so safe to call before every request.
	handler.requestPermission = function() 
    {
        if (typeof window.Notification === 'undefined') 
            return;
        
        if (Notification.permission !== 'granted') {
            Notification.requestPermission();
			localStorage.setItem("DesktopNotificationsDenied", false);
        }
    }

	// If it tries to fire after a permission denial, notify the user on first call (since last
	// permission request) and prevent further popups.
	handler.onDenied = function(notifyEvent)
	{
		var notificationsDisabled = localStorage.getItem("DesktopNotificationsDenied");
		if(notificationsDisabled == null || notificationsDisabled == false)
		{
			popupMessage("Notifications Disabled", "You have declined notification requests. No further notifications will show.", false);
			localStorage.setItem("DesktopNotificationsDenied", true);
		}
	}

	// Notification API uses tag to be able to label specific notifications to resend/supercede.
	// If no category/tag is specified, just assign a random Guid. 
	var guid = function() {
		function s4() {
			return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
		}
		return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
	};

	handler.onNotification = function(notifyEvent){
		if (typeof window.Notification === 'undefined') 
	        return;
		
	    if (Notification.permission === 'granted') {
	        var opt = {};
	    	opt.tag = notifyEvent.category || guid();
	        opt.body = notifyEvent.body;
	        opt.icon = notifyEvent.icon;
	        
	        var n = new Notification(notifyEvent.title, opt);
	        n.onclick = function () {
	        	window.focus();
	        	notifyEvent.onclick && notifyEvent.onclick(notifyEvent);
	            this.close();
	        };
	        n.onerror = function () {
	        	notifyEvent.onerror && notifyEvent.onerror(notifyEvent);
	        }
	        
	        if(notifyEvent.options.timeout != null)
        	{
	        	setTimeout(function() { n.close(); }, notifyEvent.options.timeout);
        	}
	    }
	    else if (Notification.permission === 'denied') {
	    	handler.ondenied && handler.onDenied(notifyEvent);
	    }
	};
	
	return handler;
}

// Offsetting the assignment of the handler to a separate function, which should get called
// after successfully loading this full script.
function SetNotificationHandler()
{
	notifyHandler = BrowserPopupNotifications();
}