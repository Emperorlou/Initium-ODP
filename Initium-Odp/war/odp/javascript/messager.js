String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

String.prototype.startsWith = function(prefix){
	if (this.lastIndexOf(prefix, 0) === 0)
		return true;
	else
		return false;
};
function EventServerMessager(chatServerUrl, idToken)
{
	var that = this;
	
	const eventServerUrl = "https://eventserver.universeprojects.com:8080/";
	this.socket = null;
	if (!window.console) window.console = {};
	if (!window.console.log) window.console.log = function () { };

	this.waitingForSendResponse = false;

	this.chatServer = chatServerUrl;
	this.idToken = idToken;

	this.channel = "GlobalChat";

	this.ping = null;
	
	this.firstGet = true;
	
	this.reconnectTimer = null;


	this.sendMessage = function(message)
	{
		ga('send', 'pageview', 'ServletChat');
		
		if(this.checkClientSideChatCommands!=null){
			if(this.checkClientSideChatCommands(message))
				return;
		}

		message = encode_utf8(message);

		
	    var a = $.post("/ServletChat",
		{
			roomId:this.channel,
			msg:message,
		});
        a.always(function(){
            that.waitingForSendResponse = false;
        });

		
//		var a = $.post(this.chatServer + "/messager",
//		{
//			channel:this.channel,
//			markers:this._getMarkersCombined(),
//			message:message,
//			"idToken2":this.idToken
//		});

		a.done(function(data){
			that.waitingForSendResponse = false;
		});

		a.fail(function(xhr, textStatus, error){
			if (that.onError!=null)
				that.onError(xhr, textStatus, error);
			that.waitingForSendResponse = false;
		});

		this.waitingForSendResponse = true;
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

	this._processMessage = function(data)
	{
		if (this.onMessagesChecked!=null)
			this.onMessagesChecked();

		if (data == null || data.length==null || data.length==0)
			return;

		var receivedMessage = false;
		
		for(var ii = 0; ii<data.length; ii++)
		{
			var newDataType = data[ii];

			receivedMessage = true;

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
				};
				
				message.code = message.code.split("-")[0];
				if (message.nicknameStyled==null) message.nicknameStyled = message.nickname; 
				
				this.onChatMessage(message);
			}
			else
			{
				var message = 
				{
					type:newDataType.additionalData.type,
					details:newDataType.additionalData.details
				};
				
				this.onNotificationMessage(message);
			}
		}

    	if (receivedMessage)
    		this.notifyChatIsActive();
	};

	this.lastGetMessageCall = null;
	this.getMessages = function(){};

	this.notifyChatIsActive = function(){};

	this.reconnect = function()
	{
		this._connect();
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
	    that.socket = new SockJS(url, null, options);
	    that.socket.onopen = function(event) 
	    {
	    	that.firstGet = true;
	        console.log("connected: "+JSON.stringify(event));
	        that.sockJsConnected = true;

	        if (that.reconnectTimer!=null)
	        {
		        clearInterval(that.reconnectTimer);
		        that.reconnectTimer = null;
	        }
	        if (that.onConnectionStateChange!=null)
	        	that.onConnectionStateChange("connected");
	    };
	    that.socket.onclose = function (event) {
	    	
	        console.log("close: "+JSON.stringify(event));
	        that.sockJsConnected = false;
	        
	        if (that.reconnectTimer==null)
	        {
	        	console.log("Auto-retrying connection every 3 seconds starting now...");
	        	that.reconnectTimer = setInterval(that._connect, 3000);
	        }
	        if (that.onConnectionStateChange!=null)
	        	that.onConnectionStateChange("disconnected");
	    };
	    that.socket.onerror = function (event) {
	        console.log("error: "+JSON.stringify(event));
	    };
	    that.socket.onmessage = function (event) {
	        var data = JSON.parse(event.data);
	        if (data.messages.length>0 && data.messages[0].additionalData.__history!=true)
	        	that.firstGet = false;
	        that._processMessage(data.messages);
	    };
	    window.onbeforeunload = function () {
	        if(that.sockJsConnected) {
	            that.socket.close();
	        }
	    };
		  
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

    if (this.chatServer==null)
    	this.chatServer = "";

    const url = eventServerUrl+"socket?token="+encodeURIComponent(idToken);
    const options = {};
    
    this._connect();

};