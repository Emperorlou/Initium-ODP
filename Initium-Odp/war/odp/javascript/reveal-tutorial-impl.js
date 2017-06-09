var introTutorial = new RevealTutorial();

var chapter1 = new RevealTutorialChapter("chapter1");

chapter1.addHiddenElement("#newui-progress-link");
chapter1.addHiddenElement(".chat_box");
chapter1.addHiddenElement("#buttonbar");
chapter1.addHiddenElement(".main-bottomhalf");

chapter1.addStep(null, "Welcome to Initium!", "I'll now give you a brief tour of the controls so you know what to do. At any time you can skip this tutorial by simply pressing the Exit button.<br><br>Pro tip: You can also tap anywhere on the screen to continue instead of hitting next." +
		"<h5>Before we begin, a few useful things..</h5>" +
		"<p>" +
		"<a href='https://www.reddit.com/r/initium' target='_blank'>Initium on Reddit</a>" +
		"<br>Subscribe here to get active with our community. Post bugs and issues here too." +
		"<br><br>" +
		"<a href='http://initium.wikia.com/wiki/The_Starter_and_Progression_Guide' target='_blank'>The player-made guide</a>" +
		"<br>Consider this an extension of this tutorial which moves beyond the UI." +
		"<br><br>" +
		"<a href='http://initium.wikia.com/wiki/Staub%27s_FAQ_Guide' target='_blank'>The player-made FAQ</a>" +
		"<br>The players have put together a page for common questions and answers." +
		"</p>");
chapter1.addStep(".header-location", "Where you are", "This is the name of the location where you are in the game world.");
chapter1.addStep("#header-map", "Game world map", "This opens the game world map which is a snapshot of a map created by the players and is an open-source and on-going project.");
chapter1.addStep("#header-inventory", "Your inventory and equipment", "This opens up a window that shows your current inventory and the equipment your character currently has on. It also allows you to equip yourself with weapons and armor.");
chapter1.addStep("#header-gold", "How much gold you have", "This is how much gold you have, which you can use to buy stuff from other players. You get gold from killing monsters, or from selling equipment or services to other players.");
chapter1.addStep("#header-settings", "Game settings", "Clicking here will open a window that allows you to change game settings including volume controls, bandwidth controls (like removing banners to save on bandwidth usage, and a few other things.");
chapter1.addStep("#header-mute", "Quick mute", "This allows you to quickly mute or unmute the sound for the game with one click.");
chapter1.addStep("#inBannerCharacterWidget", "Your character", "This area gives you some quick information about your character. If you have any buffs or debufs, they will be seen here as well. Clicking on buffs will give you detailed information about it. The red bar represents your hitpoints. If you have 0 or less hitpoints, you are either unconscious or dead!");
chapter1.addStep(".avatar-equip-backing", "Your stats and equipment", "Clicking here will show details about your equipment, your stats (strength, dexterity, intelligence), as well as how much you are carrying. If you hover your mouse over this area, you will also get a quick breakdown of your current strength, dexterity, intelligence, and how much weight you're carrying (unavailable on mobile).");
chapter1.addStep("a[rel=#profile]", "Your profile and other options", "Clicking on your own name here will give you some other important options including: Logging out, character switching (for premium members only), profile view, and referral stats.");
chapter1.addStep("a[rel=#profile]", "Player group membership", "You will also find options for managing your membership with a player group here.");
chapter1.addStep("#immovablesPanel", "Special items", "This area will show interactive icons for special items in the area. These are usually items that are permanent or semi-permanent fixtures for the location, like a firepit.");

introTutorial.addChapter(chapter1);






var chapter2 = new RevealTutorialChapter("chapter2");

chapter2.addHiddenElement("#buttonbar");
chapter2.addHiddenElement(".main-bottomhalf");

chapter2.addStep("#chat_box_minimize_button", "Minimize chat window", "If you don't want to use chat, you can use this button to minimize and restore the chat window.");
chapter2.addStep(".fullscreenChatButton", "Maximize chat window", "Handy for when you're only interested in chatting. This button will maximize the chat window so it becomes full screen.");
chapter2.addStep("#GlobalChat_tab", "Global chat", "Every player across the entire game server is able to use this channel to chat.");
chapter2.addStep("#LocationChat_tab", "Location chat", "Only players that are in your current location (your location is shown in the top left of the screen) can see the chat written here. Use it to talk to people only in your current location.");
chapter2.addStep("#GroupChat_tab", "Group chat", "Only players that are in your group can see the chat written here. Use it to privately talk to people who are in your group.");
chapter2.addStep("#PartyChat_tab", "Party chat", "Only players that are in your party can see the chat written here. Use it to privately talk to people who are in your party.");
chapter2.addStep("#PrivateChat_tab", "Private chat", "This tab is for private 1 on 1 chat between you and one other player. You can initiate a private chat with a player by clicking on their name, then pressing the Private Chat link at the top.");
chapter2.addStep(".chat_tab_help_button", "Chat commands help", "This will give you a list of the available chat commands. They can be quite handy.");
chapter2.addStep("#ignore-players", "Ignore player management", "If certain players are spamming the chat, you can ignore and unignore them with the options here. Furthermore, type /ignore charactername in chat in order to put up a handy link for ignoring that player with one click. You can also click on a character's name and then click the Ignore link at the top to ignore that player.");

introTutorial.addChapter(chapter2);






var chapter3 = new RevealTutorialChapter("chapter3");

chapter3.addHiddenElement(".main-bottomhalf");

chapter3.addStep("#inventionButton img", "Invention/Crafting system", "This opens the invention page. This page is where you initiate experiments, manage and implement your character's ideas, and construct items and buildings.");
chapter3.addStep("#manageStore img", "Manage your store", "This button will take you to your storefront management page. This page allows you to setup your storefront by specifying which items you would like to sell to other players and for how much. More help can be found in the storefront page itself.");
chapter3.addStep("#toggleStorefront img", "Enable/disable your store", "This button will turn on and off your storefront. Since you cannot move while vending, you will need to turn off your store before you go off adventuring. This button makes turning your store on and off quick and easy.");
chapter3.addStep("#togglePartyJoin img", "Enable/disable party joins", "This is the party join button. When enabled (without the red cross), other characters will be able to join you in a party. If you are not already in a party then when someone joins you, you will automatically become the party leader. More information on parties and how they work can be found in the game mechanics page. Type /mechanics in chat to get that link.");
chapter3.addStep("#toggleDuel img", "Enable/disable duel requests", "This button allows you to control whether or not you are accepting duel requests. When enabled, other players are able to request to duel with you. You will be given the option to accept a duel request or deny it. When you accept, you will be whisked away into a special arena where you and the other player will engage in battle. More information on the different types of duels and how they work can be found in the game mechanics page. Type /mechanics in chat to get that link.");
chapter3.addStep("#toggleCloak img", "Enable/disable your cloak", "This button will not allow other players to see your character stats, referral stats, or equipment. It can be an important tool in PvP to hide your equipment so other players are less prepared to attack you since they do not know what you're weak to. However if you're not planning on doing PvP any time soon, keeping this option off makes it easier for people to see what you have and to help you - or just to show off your great gear.");

introTutorial.addChapter(chapter3);






var chapter4 = new RevealTutorialChapter("chapter4");

chapter4.addStep("#main-merchantlist", "Show stores around here", "This will open a page that shows you all the stores that are in your current location. Note the premium token exchange can also be found here but that page is server-wide and not location specific.");
chapter4.addStep("#main-itemlist", "Show items around here", "This will open a page that shows you all the items that are on the ground in your current location. You can pick up items from the ground or drop items from your inventory this way.<br><br>Pro tip: If you're just starting the game, you usually find some usable equipment that has been thrown away in your starting area - that can get you started!");
chapter4.addStep("#main-characterlist", "Show characters near you", "This will open a page that shows you the characters that are the same location as you. If there are a lot of characters then only the first 50 will be shown.");
chapter4.addStep("#main-explore", "Look around for something interesting", "This is one of the more important buttons in the game. Clicking here will cause your character to explore your current location in search of anything that might be of interest. This is how you find new locations, new monsters to fight, and new collectables (things like iron ore deposits, berries..etc).");
chapter4.addStep("#main-explore-ignorecombatsites", "Explore but ignore old sites", "Some locations can have a lot of old combat sites that are left over from other players. This variant of the explore button will cause your character to ignore old combat and collection sites when looking for things of interest.");

chapter4.addStep(null, "What to do next?", "First thing you're going to want to do is get some basic equipment from the ground in Aera. There is usually at least something there to get you started. " +
		"Then you can head out to Aera Countryside (in the list of buttons at the bottom of the screen). There you can use the Explore button to find some monsters to fight, or some new places to go. " +
		"You might find your way into the Troll Camp, and with more exploring you'll find the Troll Cave Entrance. This is a long instance-like area that is perfect for gaining strength and acquiring " +
		"your first good chunk of gold. It is recommended to buy a house at the Town Hall in Aera as soon as possible and storing some spare equipment there for when you inevitably die in this harsh game, so be prepared!.<br><br>" +
		"If you would like a more extensive guide, type /guide in chat to bring up the player-made starter guide for new players. Enjoy the game and be careful out there!");

introTutorial.addChapter(chapter4);
