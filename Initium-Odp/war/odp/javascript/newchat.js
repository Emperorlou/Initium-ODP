//const eventServerUrl = "https://eventserver.universeprojects.com:8080/";
//var socket;
//if (!window.console) window.console = {};
//if (!window.console.log) window.console.log = function () { };
//window.openChatConnection = function () {
//    const token = document.getElementById("chat_token").value;
//    const url = eventServerUrl+"socket?token="+token;
//    const options = {};
//    window.sockJsConnected = false;
//    socket = new SockJS(url, null, options);
//    var printMessage = function (message) {
//        const messageHtml = window.constructChatMessageHtmlFrom(message);
//        $("#chat_messages").prepend(messageHtml);
//    };
//    socket.onopen = function(event) {
//        console.log("connected: "+JSON.stringify(event));
//        window.sockJsConnected = true;
//    };
//    socket.onclose = function (event) {
//        console.log("close: "+JSON.stringify(event));
//        window.sockJsConnected = false;
//    };
//    socket.onerror = function (event) {
//        console.log("error: "+JSON.stringify(event));
//    };
//    socket.onmessage = function (event) {
//        var data = JSON.parse(event.data);
//        for(var i in data.messages) {
//            printMessage(data.messages[i])
//        }
//    };
//    window.onbeforeunload = function () {
//        if(window.sockJsConnected) {
//            socket.close();
//        }
//    }
//};
//
//
///* 
// * Old Message object:
// * 	message.characterId
// * 	message.message
// * 	message.createdDate
// * 	message.code
// * 	message.nickname
// * 	message.nicknameStyled
// * 	message.mode
// */
//
//
///*
// * message structure:
// * 	String message.senderUserId			- Some ID (probably going to make it into characterId)
// * 	String message.senderDisplayName 	- Html probably
// * 	String message.text					- Raw text for message
// * 	String message.channel 						- The channel this message belongs to
// * 	Long message.timestamp				- Timestamp in UTC
// * 	Json message.additionalData					- A set of properties that was sent along with this message
// * 
// */
//
///*
// * New Mappings:
// * 	message.senderUserId = old.characterId
// * 	message.senderDisplayName = old.nickname
// * 	message.text = old.message
// * 	message.channel = old.code
// * 	message.timestamp = old.createdDate
// * 	message.additionalData.senderDisplayNameStyled = old.nicknameStyled
// * 	message.additionalData.mode = old.mode
// */
//
//window.constructChatMessageHtmlFrom = function (message){
//    var nickname = message.senderDisplayName;
//    var text = message.text;
//    var messageDate = new Date(message.timestamp);
//    var hours = messageDate.getHours();
//    if(hours < 10) hours = "0"+hours;
//    var minutes = messageDate.getMinutes();
//    if(minutes < 10) minutes = "0"+minutes;
//
//   return "" +
//        "<div class='chatMessage-main'>" +
//        "<span class='chatMessage-time' title='" +
//        messageDate.toLocaleDateString() +
//        "'>[" +
//        hours + ":" + minutes +
//        "]</span> " +
//        "<span class='chatMessage-nickname'>" +
//        nickname +
//        "</span>" +
//        ": " +
//        "<span class='chatMessage-text'>" +
//        text +
//        "</span>" +
//        "</div>";
//};
//
//window.sendChatUpdate = function () {
//    socket.send("update")
//};
//
//window.delay = 2000;
//var waitingForSendResponse = false;
//var roomId=0;
//
//function sendMessage(message)
//{
//    var a = $.ajax(
//        {
//            url: "/ServletChat?roomId="+roomId+"&msg=" + message
//        });
//    a.always(function(){
//        waitingForSendResponse = false;
//    });
//
//    waitingForSendResponse = true;
//}
//
//roomId="GLOBAL";
//
//function submitMessage(event){
//	
//    if (waitingForSendResponse)
//        return;
//    const chatInputSel = $("#chat_input");
//
//    sendMessage(chatInputSel.val());
//    chatInputSel.val('');
//};
//window.openChatConnection();
//
//
//
//
//
//
//
//
//function EventServerMessager(chatServerUrl, idToken)
//{
//	var that = this;
//	
//	const eventServerUrl = "https://eventserver.universeprojects.com:8080/";
//	var socket;
//	if (!window.console) window.console = {};
//	if (!window.console.log) window.console.log = function () { };
//
//	this.waitingForSendResponse = false;
//
//	this.chatServer = chatServerUrl;
//	this.idToken = idToken;
//
//	this.channel = "GlobalChat";
//
//	this.ping = null;
//
//
//	this.sendMessage = function(message)
//	{
//		if(this.checkClientSideChatCommands!=null){
//			if(this.checkClientSideChatCommands(message))
//				return;
//		}
//
//		message = encode_utf8(message);
//
//		
//	    var a = $.ajax(
//	    {
//	    	url: "/ServletChat?roomId="+this.channel+"&msg=" + message
//	    });
//        a.always(function(){
//            that.waitingForSendResponse = false;
//        });
//
//		
////		var a = $.post(this.chatServer + "/messager",
////		{
////			channel:this.channel,
////			markers:this._getMarkersCombined(),
////			message:message,
////			"idToken2":this.idToken
////		});
//
//		a.done(function(data){
//			that._processMessage(data);
//			that.waitingForSendResponse = false;
//		});
//
//		a.fail(function(xhr, textStatus, error){
//			if (that.onError!=null)
//				that.onError(xhr, textStatus, error);
//			that.waitingForSendResponse = false;
//		});
//
//		this.waitingForSendResponse = true;
//	};
//
//
//	/*
//	 * New Mappings:
//	 * 	message.senderUserId = old.characterId
//	 * 	message.senderDisplayName = old.nickname
//	 * 	message.text = old.message
//	 * 	message.channel = old.code
//	 * 	message.timestamp = old.createdDate
//	 * 	message.additionalData.senderDisplayNameStyled = old.nicknameStyled
//	 * 	message.additionalData.mode = old.mode
//	 */
//
//	this._processMessage = function(data)
//	{
//		if (this.onMessagesChecked!=null)
//			this.onMessagesChecked();
//
//		if (data == null || data.length==null || data.length==0)
//			return;
//
//		var receivedMessage = false;
//		
//		for(var ii = 0; ii<data.length; ii++)
//		{
//			var newDataType = data[ii];
//			var message = 
//			{
//				characterId:newDataType.senderUserId,
//				nickname:newDataType.senderDisplayName,
//				message:newDataType.text,
//				code:newDataType.channel,
//				createdDate:newDataType.timestamp,
//				nicknameStyled:newDataType.additionalData.senderDisplayNameStyled,
//				mode:newDataType.additionalData.mode
//			};
//			
//			message.code = message.code.split("-")[0];
//
//			receivedMessage = true;
//
//			if (message.code.endsWith("Chat") || message.code === "GameMessages")
//			{
//				this.onChatMessage(message);
//			}
//			else
//			{
//				this.onNotificationMessage(message);
//			}
//		}
//
//    	if (receivedMessage)
//    		this.notifyChatIsActive();
//	};
//
//	this.lastGetMessageCall = null;
//	this.getMessages = function(force)
//	{
//    	if (this.waitingForSendResponse)
//    		return;
//
//    	var time = new Date().getTime();
//
//    	if (this.lastGetMessageCall!=null && this.lastGetMessageCall>time-delay && force!=true)
//    		return;
//    	this.lastGetMessageCall = time;
//
//		var a = $.ajax(
//		{
//			url: this.chatServer + "/messager?markers=" + this._getMarkersCombined()+"&idToken2="+encodeURIComponent(this.idToken)
//
//		});
//
//		a.done(function(data)
//		{
//			that.ping = new Date().getTime()-time;
//			that._processMessage(data, false);
//			that.firstGet = false;
////			checkChatIsIdle();
//		});
//
//		a.fail(function(xhr, textStatus, error)
//		{
//			that.ping = null;
//			if (that.onError!=null)
//				that.onError(xhr, textStatus, error);
////			checkChatIsIdle();
//		});
//	};
//
//
//  this.notifyChatIsActive = function ()
//  {
//
//  	if (this.chatIdleMode==true)
//	{
//  		clearInterval(this.messageChecker);
//		this.messageChecker = setInterval(function(){that.getMessages();}, this.delay);
//		this.chatIdleMode = false;
//	}
//
//  	this.chatIdleTime = new Date();
//  };
//
//
//
//
//    this.onChatMessage = null;
//    this.onNotificationMessage = null;
//    this.onGameMessage = null;
//    this.checkClientSideChatCommands = null;
//    /**
//     * Args: xhr, textStatus, error
//     */
//    this.onError = null;
//    this.onConnectionStateChange = null;
//    this.onMessagesChecked = null;
//
//
//
//
//    ///////////////////////////////////
//    // Constructor area
//
//    if (this.chatServer==null)
//    	this.chatServer = "";
//
//    const token = document.getElementById("chat_token").value;
//    const url = eventServerUrl+"socket?token="+token;
//    const options = {};
//    this.sockJsConnected = false;
//    this.socket = new SockJS(url, null, options);
//    var printMessage = function (message) {
//        const messageHtml = window.constructChatMessageHtmlFrom(message);
//        $("#chat_messages").prepend(messageHtml);
//    };
//    this.socket.onopen = function(event) {
//        console.log("connected: "+JSON.stringify(event));
//        window.sockJsConnected = true;
//    };
//    this.socket.onclose = function (event) {
//        console.log("close: "+JSON.stringify(event));
//        window.sockJsConnected = false;
//    };
//    this.socket.onerror = function (event) {
//        console.log("error: "+JSON.stringify(event));
//    };
//    this.socket.onmessage = function (event) {
//        var data = JSON.parse(event.data);
//        that._processMessages(data.messages);
//    };
//    window.onbeforeunload = function () {
//        if(window.sockJsConnected) {
//            that.socket.close();
//        }
//    }
//
//
//};