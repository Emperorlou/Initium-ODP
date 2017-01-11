function encode_utf8( s ) {
  return unescape( encodeURIComponent( s ) );
}

function decode_utf8( s ) {
  return decodeURIComponent( escape( s ) );
}

var messageCodes = [
             "GameMessages",
             "GlobalChat",
             "LocationChat",
             "GroupChat",
             "PartyChat",
             "PrivateChat",
             "Notifications"
             ];
var messager = new Messager(5000, 10000, 30, "https://chat-dot-playinitium.appspot.com", chatIdToken);

// Here we're overriding the default markers array to include the notifications marker because it is a special flower. Basically, we
// need to make sure we never execute the same notifications twice, so we'll always remember where we left off.
messager.markers = [null, null, null, null, null, getItem("NotificationsMarker")];

messager.onMessagesChecked = function()
{
	if (messager.ping==null)
		$("#ping").text("??");
	else
		$("#ping").text(messager.ping);
};

messager.onError = function(xhr, textStatus, error)
{
	$("#chat_messages").prepend("<div style='color:red'>"+textStatus+": "+error+"</div>");	
};



messager.onNotificationMessage = function(message)
{
	setItem("NotificationsMarker", message.marker);	 // Always remember the last notifications marker
	
	
	if (message.type == "fullpageRefresh")
		fullpageRefresh();
	if (message.type == "tradeStarted")
		_viewTrade();
	if (message.type == "tradeChanged")
		updateTradeWindow();
	if (message.type == "tradeCancelled")
		cancelledTradeWindow();
	if (message.type == "territoryChanged")
		updateTerritory();
};



messager.onChatMessage = function(chatMessage)
{
	if (!isCharNotMuted(chatMessage.characterId))
		return; //We quit the function if message is muted

	chatMessage.message = decode_utf8(chatMessage.message);

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
		
		try
		{
			if (characterId != chatMessage.characterId)
			{
				doPopupNotification(null, "New private message", chatMessage.nickname+": "+chatMessage.message, "PrivateMessage", null, function(){
					setPrivateChatTo(chatMessage.nickname, chatMessage.characterId);
				});
				
				// Change the title to include a * but only if the tab doesn't have focus
				flagUnreadMessages();
			}
			
		}
		catch(e)
		{
			// Ignore errors if there are any
		}
	}
	else if (chatMessage.mode==null)
	{
		if (chatMessage.message.length > 2000)
			chatMessage.message = "/me is a spammer. Kill him with fire!";
		var meMode = chatMessage.message.toLowerCase().startsWith("/me ");
		html+="<span class='chatMessage-text'>";
		if (chatMessage.characterId!=null && meMode==false)
			html+=chatMessage.nicknameStyled;
		else if (chatMessage.characterId!=null && meMode==true)
			html+=chatMessage.nicknameMeStyled;
		else
			html+="<span class='meModeNickname'>"+chatMessage.nicknameStyled+"</span>";
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
		if (!isMessageNotMuted(chatMessage.message))
			return; //We quit the function if message is muted

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
	$("#"+code+"-chat-indicator").text("").hide();
	localStorage.setItem("UnreadCount_"+code, 0);
	
	localStorage.setItem("DefaultChatTab", code);

	
	for(var i = 0; i<messageCodes.length; i++)
	{
		$("#"+messageCodes[i]+"_tab").removeClass("chat_tab_selected");
	}	
	
	$("#"+code+"_tab").addClass("chat_tab_selected");
	
	if (code === "GameMessages")
		$("#chat_form_wrapper").hide();
	else
		$("#chat_form_wrapper").show();
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
	var indicator = $("#"+code+"-chat-indicator");
	indicator.text(unread);
	indicator.show();
	
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
		{
			// Now update the chat tab unread counter
			var indicator = $("#"+code+"-chat-indicator");
			indicator.text("");
			indicator.hide();
		}
		else
		{
			// Now update the chat tab unread counter
			var indicator = $("#"+code+"-chat-indicator");
			indicator.text(unread);
			indicator.show();
		}
			
		
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

function refreshLists()
{
	setTimeout(function() {
		refreshIgnoreList();
		refreshSuggestedList();
		refreshIgnoredMessagesList();
   }, 10);
}

//Clears the ignore list from the cache.
function clearIgnoreList()
{
	saveIgnoredList('mutedPlayerIds', []);
	saveIgnoredList('mutedPlayerMessages', []);
	refreshLists();
}

//Refreshes the ignore list taking it from LocalStorage and appends to the list
function refreshIgnoreList()
{
	var mutedPlayerIds = getItemFromLocalStorage('mutedPlayerIds');

	$('#ignoreList').empty();
	if(mutedPlayerIds.length > 0)
	{
		mutedPlayerIds.forEach(function(item){
			$('#ignoreList').append('' +
				'<a onclick="removeIgnoredPerson(' + item["characterId"] + ')">X</a> ' + 
				item["name"] + '' +
				'<br>' +
				'');
		});
	}
}

//Refreshes the ignore list taking it from LocalStorage and appends to the list
function refreshIgnoredMessagesList() {
	var mutedPlayerIds = getItemFromLocalStorage('mutedPlayerMessages');

	if (mutedPlayerIds.length > 0) {
		mutedPlayerIds.forEach(function (item) {
			$('#ignoreList').append('' +
				'<a onclick="removeIgnoredMessage(\'' + item["message"] + '\')">X</a> ' + 
				item["message"] + '' +
				'<br>' +
				'');
		});
	}
}

//Refreshes the suggested list by taking first five unique recent chatters.
function refreshSuggestedList()
{
	var suggestedList = createRecentChattersList();

	$('#suggestedList').empty();
	if(suggestedList.length > 0)
	{
		suggestedList.forEach(function(item){
			$('#suggestedList').append('' +
				'<a onclick="ignoreAPlayer(\'' + item["characterId"] +'\', \''+ item["name"] + '\')">Ignore ' + item["name"] + '</a><br>');
		});
	}

	$('#suggestedList').append('<br><p>' +
		'<a onclick="ignoreAMessage(\'A new player has just joined\')">Ignore \"A new player has just joined\" message </a><br></p>');

}

function getItemFromLocalStorage(itemName){
	var item = localStorage.getItem( String(itemName));
	return item != null ? JSON.parse(item) : [];
}

function removeIgnoredPerson(characterId)
{
	var mutedPlayerIds = getItemFromLocalStorage('mutedPlayerIds');

	mutedPlayerIds = findAndRemove(mutedPlayerIds, 'characterId', String(characterId));
	saveIgnoredList('mutedPlayerIds', (mutedPlayerIds));
	refreshLists();
}

function removeIgnoredMessage(message) {
	var mutedPlayerIds = getItemFromLocalStorage('mutedPlayerMessages');

	mutedPlayerIds = findAndRemove(mutedPlayerIds, 'message', String(message));
	saveIgnoredList('mutedPlayerMessages', (mutedPlayerIds));
	refreshLists();
}

function findAndRemove(array, property, value) {
	array.forEach(function(result, index) {
		if (value.indexOf(result[property]) !== -1) {
			//Remove from array
			array.splice(index, 1);
		}
	});

	return array;
}


//Creates an object from recent chatters list, not bigger than 5
function createRecentChattersList()
{
	var object = [];
	$('.chat_box').find('a[rel*="viewcharactermini"]').each(function(){
		if(object.length > 5)
			return;

		var tempCharacterId = $(this).attr("rel").split("=")[1];
		var tempNickName = $(this).text();
		var tempObject = {"name":String(tempNickName),"characterId":String(tempCharacterId)};
		if(!findIfExists(object, 'characterId', String(tempCharacterId))) {
			object.push(tempObject);
		}
	});

	return object;
}

function findIfExists(array, property, value) {
	var exists = false;
	array.forEach(function(result) {
		if (value.indexOf(result[property]) !== -1) {
			exists = true;
			return;
		}
	});
	return exists;
}

function ignoreAPlayer(characterId, nickname)
{
	var playerObject = {'name':String(nickname), 'characterId':String(characterId)};
	var ignoreList = getItemFromLocalStorage('mutedPlayerIds');
	if(!findIfExists(ignoreList, 'characterId', String(characterId))) {
		ignoreList.push(playerObject);
		saveIgnoredList('mutedPlayerIds', ignoreList);
	}
	refreshLists();
}

function ignoreAMessage(messageText) {
	var ignoreList = getItemFromLocalStorage('mutedPlayerMessages');
	var playerObject = {'message': String(messageText), 'date': String($.now())};
	if (!findIfExists(ignoreList, 'message', String(messageText))) {
		ignoreList.push(playerObject);
		saveIgnoredList('mutedPlayerMessages', ignoreList);
	}
	refreshLists();
}

function saveIgnoredList(itemName, array)
{
	localStorage.setItem(String(itemName), JSON.stringify(array));
}

function isCharNotMuted(characterId)
{
	var mutedPlayerIds = getItemFromLocalStorage('mutedPlayerIds');

	if(!findIfExists(mutedPlayerIds, 'characterId', String(characterId))) {
		return true;
	}
	return false;
}

function isMessageNotMuted(message) {
	var mutedPlayerIds = getItemFromLocalStorage('mutedPlayerMessages');

	var timeNow = $.now();
	var diff = new Date(mutedPlayerIds["date"] - timeNow);
	var days = diff / 1000 / 60 / 60 / 24;
	if (days >= 1) {
		findAndRemove(mutedPlayerIds, 'message', message);
		saveIgnoredList('mutedPlayerMessages', mutedPlayerIds);
	}

	if (!findIfExists(mutedPlayerIds, 'message', String(message))) {
		return true;
	}
	return false;
}

$( document ).ajaxComplete(function( event, xhr, settings ) {
	if ( settings.url.match("^/odp/ajax_ignore" )) {
		refreshLists();
	}
});
