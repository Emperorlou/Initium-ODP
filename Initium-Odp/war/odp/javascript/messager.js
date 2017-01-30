String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

String.prototype.startsWith = function(prefix){
	if (this.lastIndexOf(prefix, 0) === 0)
		return true;
	else
		return false;
};

function Messager(delay, idleDelay, secondsToIdle, chatServerUrl, idToken) 
{
	
	var that = this;
	this.markers = [];
	
	this.chatIdleMode = false;
	this.messageChecker = null;
	this.waitingForSendResponse = false;
	
	this.chatIdleTime = new Date();
	this.chatSecondsToIdle = 30;
	
	this.chatServer = chatServerUrl;
	this.idToken = idToken;
	
	this.channel = "GlobalChat";
	
	this.firstGet = true;

	this.ping = null;
	

	$(window).focus(function() {
		clearInterval(that.messageChecker);
		that.messageChecker = setInterval(function(){that.getMessages();}, delay);
	});
	$(window).blur(function() {
		clearInterval(that.messageChecker);
		that.messageChecker = setInterval(function(){that.getMessages();}, idleDelay);
	});	
	
	this._getMarkersCombined = function()
	{
		var markersList = "";
		
		if (this.markers.length>0)
			for(var i = 0; i<this.markers.length; i++)
			{
				markersList += this.markers[i]+",";
			}
		
		return markersList;
	};

	this.sendMessage = function(message)
	{
		if(this.checkClientSideChatCommands!=null){
			if(this.checkClientSideChatCommands(message))
				return;
		}

		message = encode_utf8(message);
		
		var a = $.post(this.chatServer + "/messager",
		{
			channel:this.channel,
			markers:this._getMarkersCombined(),
			message:message,
			"idToken":this.idToken
		});
		
		a.done(function(data){
			that._processMessage(data);
			that.waitingForSendResponse = false;
		});
		
		a.fail(function(xhr, textStatus, error){
			if (that.onError!=null)
				that.onError(xhr, textStatus, error);
			that.waitingForSendResponse = false;
		});
		
		this.waitingForSendResponse = true;
		
		this.notifyChatIsActive();
	};
	
	
	
	this._processMessage = function(data)
	{
		if (this.onMessagesChecked!=null)
			this.onMessagesChecked();
		
		if (data == null || data.length==null || data.length==0)
			return;
		
		var receivedMessage = false;
    	for(var i = 0; i<data.length; i++)
		{
    		var currentMarker = this.markers[i];
    		
    		
    		var messageList = data[i];
    		if (messageList!=null)
    		{
	    		for(var ii = 0; ii<messageList.length; ii++)
	    		{
	    			var message = messageList[ii];
	    			
	    			var incomingMarker = message.marker;
	    			if (currentMarker>=incomingMarker)
	    				continue;
	    			
	    			receivedMessage = true;
	    			
	    			this.markers[i] = incomingMarker;
	    			if (message.code.endsWith("Chat"))
	    			{
	    				this.onChatMessage(message);
	    			}
	    			else
	    			{
	    				this.onNotificationMessage(message);
	    			}
	    		}
    		}
    		else
    		{
    			this.markers[i] = null;
    		}
    		
		}
    	
    	if (receivedMessage)
    		this.notifyChatIsActive();
	};
	
	this.lastGetMessageCall = null;
	this.getMessages = function()
	{
    	if (this.waitingForSendResponse)
    		return;
	
    	var time = new Date().getTime();
    	
    	if (this.lastGetMessageCall!=null && this.lastGetMessageCall>time-delay)
    		return;
    	this.lastGetMessageCall = time;
    	
		var a = $.ajax(
		{
			url: this.chatServer + "/messager?markers=" + this._getMarkersCombined()+"&idToken="+this.idToken
			
		});
		
		a.done(function(data)
		{
			that.ping = new Date().getTime()-time;
			that._processMessage(data, false);
			that.firstGet = false;
//			checkChatIsIdle();
		});
		
		a.fail(function(xhr, textStatus, error)
		{
			that.ping = null;
			if (that.onError!=null)
				that.onError(xhr, textStatus, error);
//			checkChatIsIdle();
		});
	};

	
  this.notifyChatIsActive = function ()
  {

  	if (this.chatIdleMode==true)
	{
  		clearInterval(this.messageChecker);
		this.messageChecker = setInterval(function(){that.getMessages();}, this.delay);
		this.chatIdleMode = false;
	}
  	
  	this.chatIdleTime = new Date();
  };
	
	

    
    this.onChatMessage = null;
    this.onNotificationMessage = null;
    this.checkClientSideChatCommands = null;
    /**
     * Args: xhr, textStatus, error
     */
    this.onError = null;
    this.onConnectionStateChange = null;
    this.onMessagesChecked = null;
    
    
    
    
    ///////////////////////////////////
    // Constructor area
    
    if (this.chatServer==null)
    	this.chatServer = "";
    
    this.getMessages();
    
    setTimeout(function(){
    	that.messageChecker = setInterval(function(){that.getMessages();}, delay);
    }, 10000);

	
	
};


