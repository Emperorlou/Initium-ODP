var messageCodes = [
             "GlobalChat",
             "LocationChat",
             "GroupChat",
             "PartyChat",
             "PrivateChat",
             "Notifications"
             ];
var messager = new Messager(4000, 15000, 30);

// Here we're overriding the default markers array to include the notifications marker because it is a special flower. Basically, we
// need to make sure we never execute the same notifications twice, so we'll always remember where we left off.
messager.markers = [null, null, null, null, null, getItem("NotificationsMarker")];



messager.onError = function(xhr, textStatus, error)
{
	$("#chat_messages").prepend("<div style='color:red'>"+textStatus+": "+error+"</div>");	
};



messager.onNotificationMessage = function(message)
{
	setItem("NotificationsMarker", message.marker);	 // Always remember the last notifications marker
	
	
	if (message.type == "fullpageRefresh")
		fullpageRefresh();
};



messager.onChatMessage = function(chatMessage)
{
	
	var html = "<div class='chatMessage-main'>";
	if (chatMessage.createdDate!=null)
	{
		var date = new Date(chatMessage.createdDate);
		var shortTime = date.toLocaleTimeString();
		var cutoff = shortTime.length;
		if (shortTime.indexOf(":")>0)
			cutoff = shortTime.lastIndexOf(":");
		else if (shortTime.indexOf(".")>0)
			cutoff = shortTime.lastIndexOf(".");
		
		shortTime = shortTime.substring(0, cutoff);
		var longDate = date.toLocaleDateString();
		
		html+="<span class='chatMessage-time' title='"+longDate+"'>";
		html+="["+shortTime+"] ";
		html+="</span>";
	}
	if (chatMessage.code=="PrivateChat")
	{
		html+="<a class='chatMessage-private-nickname' onclick='setPrivateChatTo(\""+chatMessage.nickname+"\", "+chatMessage.characterId+")'>"+chatMessage.nickname+"</a>";
		html+="<span class='chatMessage-text'>";
		html+=" ";
		html+=chatMessage.message;
		html+="</div>";
	}
	else if (chatMessage.mode==null)
	{
		if (chatMessage.message.length > 2000)
			chatMessage.message = "/me is a spammer. Kill him with fire!";
		var meMode = chatMessage.message.toLowerCase().startsWith("/me ");
		html+="<span class='chatMessage-text'>";
		if (chatMessage.characterId!=null && meMode==false)
			html+=chatMessage.nicknameStyled;
		else
			html+=chatMessage.nickname;
		html+="</span>";
		html+="<span class='chatMessage-text'>";
		if (meMode)
		{
			html+=" ";
			chatMessage.message = chatMessage.message.substring(4);
		}
		else
			html+=": ";
		html+=chatMessage.message;
		html+="</div>";
	}
	else if (chatMessage.mode=="admin")
	{
		html+="<span class='chatMessage-text-story'>";
		html+=chatMessage.message;
		html+="</div>";
	}
	html+="</div>";
	$("#chat_messages_"+chatMessage.code).prepend(html);
	
	notifyNewMessage(chatMessage.code);
};


function changeChatTab(code)
{
	var chat_messages = $('.chat_messages');
	chat_messages.hide();
	
	$("#chat_input").attr("placeholder", chatChannelPlaceholders[code]);
	
	messager.channel = code;
	$('#chat_messages_'+code).show();
	
	// Reset the unread counter
	$("#"+code+"_tab").text(code.substring(0, code.length-4));
	localStorage.setItem("UnreadCount_"+code, 0);
	
	localStorage.setItem("DefaultChatTab", code);

	
	for(var i = 0; i<messageCodes.length; i++)
	{
		$("#"+messageCodes[i]+"_tab").removeClass("chat_tab_selected");
	}	
	
	$("#"+code+"_tab").addClass("chat_tab_selected");
}

function notifyNewMessage(code)
{
	if (messager.firstGet)
		return;
	if (code == messager.channel)
		return;
	
	var unread = localStorage.getItem("UnreadCount_"+code);
	if (unread==null) unread = 0;
	unread++;
	localStorage.setItem("UnreadCount_"+code, unread);
	
	// Now update the chat tab unread counter
	$("#"+code+"_tab").text(code.substring(0, code.length-4)+"("+unread+")");
}

function getUnreadFor(code)
{
	var unread = localStorage.getItem("UnreadCount_"+code);
	if (unread==null)
		unread = 0;
	
	return unread;
}


//Sets the default tab 
$(document).ready(function(){

	var defaultChatTab = localStorage.getItem("DefaultChatTab");
	if (defaultChatTab!=null)
		changeChatTab(defaultChatTab);
	else
		changeChatTab("GlobalChat");

	for(var i = 0; i<messageCodes.length; i++)
	{
		var code = messageCodes[i];
		var unread = localStorage.getItem("UnreadCount_"+code);
		if (unread==null) unread = 0;
		
		// Now update the chat tab unread counter
		if (unread==0)
			$("#"+code+"_tab").text(code.substring(0, code.length-4));
		else
			$("#"+code+"_tab").text(code.substring(0, code.length-4)+"("+unread+")");
			
		
	}
	


    	    	
	$("#chat_form").submit(function (e) 
	{
			e.preventDefault();
			if (window.waitingForSendResponse)
				return;
			
			var message = $("#chat_input").val();
			
			if (messager.channel == "PrivateChat" && currentPrivateChatCharacterId!=null)
			{
				message = "#"+currentPrivateChatCharacterId + ": "+message;
			}
			else if (messager.channel == "PrivateChat" && currentPrivateChatCharacterName!=null)
			{
				message = currentPrivateChatCharacterName + ": "+message;
			}

			if (messager.channel == "PrivateChat" && currentPrivateChatCharacterName==null)
			{
				alert("You cannot chat privately until you select a person to chat privately with. Click on their name and then click on Private Chat.");
				return;
			}
			
			messager.sendMessage(message);
			$("#chat_input").val('');
	});

});


var chatChannelPlaceholders = 
{
	"GlobalChat":"Chat with everyone in the game world",
	"LocationChat":"Chat with anyone in your current location",
	"GroupChat":"Chat with anyone in your group",
	"PartyChat":"Chat with everyone in your party",
	"PrivateChat":"To private chat, click on someone's name and then Private Chat"
};
function setChatChannelPlaceholder(channel, text)
{
	chatChannelPlaceholders[channel] = text;
};


var currentPrivateChatCharacterName = localStorage.getItem("privateChatCharacterName");
var currentPrivateChatCharacterId = localStorage.getItem("privateChatCharacterId");
if (currentPrivateChatCharacterName!=null)
	chatChannelPlaceholders['PrivateChat'] = "Chat with "+currentPrivateChatCharacterName;
function setPrivateChatTo(charName, charId)
{
	currentPrivateChatCharacterName = charName;
	currentPrivateChatCharacterId = charId;
	localStorage.setItem("privateChatCharacterName", charName);
	localStorage.setItem("privateChatCharacterId", charId);
	
	chatChannelPlaceholders['PrivateChat'] = "Chat with "+currentPrivateChatCharacterName;
	
	changeChatTab("PrivateChat");
	
	$("#chat_input").focus();
};


function setItem(key, value) 
{
	if (window.localStorage === undefined)
	{
		document.cookie = key+'='+escape(value)+';path=/';	
	}
	else
	{
		window.localStorage.setItem(key, value);
	}
};

function getItem(key)
{
	if (window.localStorage === undefined)
	{
		val = document.cookie.match('(^|;)\\s*' + key + '\\s*=\\s*([^;]+)');
	    return val ? val.pop() : '';		
	}
	else
	{
		return window.localStorage.getItem(key);
	}
};