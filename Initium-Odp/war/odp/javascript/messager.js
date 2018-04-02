String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

String.prototype.startsWith = function(prefix){
	if (this.lastIndexOf(prefix, 0) === 0)
		return true;
	else
		return false;
};
function EventServerMessager(eventServerUrl, idToken)
{
	var that = this;

	this._chatServer = eventServerUrl;
	this._idToken = idToken;
	
	this.socket = null;
	if (!window.console) window.console = {};
	if (!window.console.log) window.console.log = function () { };

	this.waitingForSendResponse = false;


	this.channel = "GlobalChat";

	this.ping = null;
	
	this.firstGet = true;
	
	this.url = null;

	this.sendMessage = function(message, targetCharacter)
	{
		ga('send', 'pageview', 'ServletChat');
		
		if(that.checkClientSideChatCommands!=null){
			if(that.checkClientSideChatCommands(message))
				return;
		}

		message = encode_utf8(message);

		
	    var a = $.post("/ServletChat",
		{
			roomId:that.channel,
			msg:message,
			targetCharacter:targetCharacter
		});
        a.always(function(){
            that.waitingForSendResponse = false;
        });

		
//		var a = $.post(that.chatServer + "/messager",
//		{
//			channel:that.channel,
//			markers:that._getMarkersCombined(),
//			message:message,
//			"idToken2":that.idToken
//		});

		a.done(function(data){
			that.waitingForSendResponse = false;
		});

		a.fail(function(xhr, textStatus, error){
			if (that.onError!=null)
				that.onError(error.message);
			that.waitingForSendResponse = false;
		});

		that.waitingForSendResponse = true;
	};


	/*
	 * New Mappings:
	 * 	message.senderUserId = old.characterId
	 * 	message.senderDisplayName = old.nickname
	 * 	message.text = old.message
	 * 	message.channel = old.code
	 * 	message.timestamp = old.createdDate
	 * 	message.additionalData.senderDisplayNameStyled = old.nicknameStyled
	 * 	message.additionalData.mode = old.mode
	 */
//	this.messagesReceived = [];	// Removing duplicates this way

	this._processMessage = function(data)
	{
		if (that.onMessagesChecked!=null)
			that.onMessagesChecked();

		if (data == null || data.length==null || data.length==0)
			return;

		var receivedMessage = false;
		
		
		for(var ii = 0; ii<data.length; ii++)
		{
			var newDataType = data[ii];

			receivedMessage = true;

			// A hacky workaround method of removing duplicate messages
//			var uniqueId = newDataType.timestamp+newDataType.text+newDataType.details;
//			if (that.messagesReceived.indexOf(uniqueId)>=0)
//				continue;
//			that.messagesReceived.push(uniqueId);
			// End hack
			
			if (newDataType.channel!="!Notifications")
			{
				var message = 
				{
					characterId:newDataType.senderUserId,
					nickname:newDataType.senderDisplayName,
					message:newDataType.text,
					code:newDataType.channel,
					createdDate:newDataType.timestamp,
					nicknameStyled:newDataType.additionalData.senderDisplayNameStyled,
					mode:newDataType.additionalData.mode,
					isHistory:newDataType.additionalData.__history
				};
				
				message.code = message.code.split("-")[0];
				if (message.nicknameStyled==null) message.nicknameStyled = message.nickname; 
				
				that.onChatMessage(message);
			}
			else
			{
				var message = 
				{
					type:newDataType.additionalData.type,
					details:newDataType.additionalData.details
				};
				
				that.onNotificationMessage(message);
			}
		}

    	if (receivedMessage)
    		that.notifyChatIsActive();
	};

	this.lastGetMessageCall = null;
	this.getMessages = function(){};

	this.notifyChatIsActive = function(){};

	this.reconnect = function(eventServerUrl, idToken)
	{
		if (that.socket!=null)
		{
			try
			{
				that.socket.close();
			}
			catch(e)
			{
				// Ignore errors
			}
		}
		
		if (eventServerUrl!=null)
			that._chatServer = eventServerUrl;
		if (idToken!=null)
			that._idToken = idToken;
		
		that.url = that._chatServer+"socket?token="+encodeURIComponent(that._idToken);
		that._connect();
	};
	
	this.disconnect = function()
	{
		if (that.socket!=null)
		{
			try
			{
				that.endAutoReconnections();
				window.eventServerReconnectTimer = {};	// Here we're setting the reconnect timer to non-null to block the auto reconnection from taking place
				that.socket.close();
			}
			catch(e)
			{
				// Ignore errors
			}
		}
	};
	
	this._connect = function()
	{
        if (that.onConnectionStateChange!=null)
        	that.onConnectionStateChange("connecting");
		if (that.socket!=null)
		{
			try
			{
				that.socket.close();
			}
			catch(e)
			{
				// Ignore errors
			}
		}
		
	    that.sockJsConnected = false;
	    if (that.url==null)
	    {
	    	console.log("Url not set!?");
	    	return;
	    }
	    that.socket = new SockJS(that.url, null, options);
	    that.socket.onopen = function(event) 
	    {
//	    	that.messagesReceived = [];	// Removing duplicates this way - Here we're clearing the unique messages we received so we don't accidentally ignore them when they are sent again
	    	that.firstGet = true;
	        console.log("connected: "+JSON.stringify(event));
	        that.sockJsConnected = true;

	        that.endAutoReconnections();
	        if (that.onConnectionStateChange!=null)
	        	that.onConnectionStateChange("connected");
	    };
	    that.socket.onclose = function (event) {
	    	if (event.wasClean==false)
		    	that.onError(event.reason);
	    		
	        console.log("close: "+JSON.stringify(event));
	        that.sockJsConnected = false;
	        
	    	that.startAutoReconnections();
	        if (that.onConnectionStateChange!=null)
	        	that.onConnectionStateChange("disconnected");
	    };
	    that.socket.onerror = function (event) {
	    	that.onError(event.reason);
	        console.log("error: "+JSON.stringify(event));
	    };
	    that.socket.onmessage = function (event) {
	    	that.endAutoReconnections();
	    	
	    	var data = JSON.parse(event.data);
	        if (data.messages!=null && data.messages.length>0 && data.messages[0].additionalData.__history!=true)
	        {
	        	that.firstGet = false;
	        	if (that.onFirstGetComplete)
	        		that.onFirstGetComplete();
	        }
	        that._processMessage(data.messages);
	    };
	    window.onbeforeunload = function () {
	        if(that.sockJsConnected) {
	            that.socket.close();
	        }
	    };
		  
	};

	this.startAutoReconnections = function(){
        if (window.eventServerReconnectTimer==null)
        {
        	console.log("Auto-retrying connection every 3 seconds starting now...");
        	window.eventServerReconnectTimer = setInterval(that.reconnect, 3000);
        }
	};
	
	this.endAutoReconnections = function(){
        if (window.eventServerReconnectTimer!=null)
        {
	        clearInterval(window.eventServerReconnectTimer);
	        window.eventServerReconnectTimer = null;
        }
	};

    this.onChatMessage = null;
    this.onNotificationMessage = null;
    this.onGameMessage = null;
    this.checkClientSideChatCommands = null;
    /**
     * Args: xhr, textStatus, error
     */
    this.onError = null;
    this.onConnectionStateChange = null;
    this.onMessagesChecked = null;




    ///////////////////////////////////
    // Constructor area

    this.endAutoReconnections();	// Just in case we've already been constructed before, we'll end auto reconnections so we don't cause connection loops
    
    if (this._chatServer==null)
    	this._chatServer = "";

    this.url = this._chatServer+"socket?token="+encodeURIComponent(this._idToken);
    const options = {};
    
    this._connect();

};