var uiTutorial = new RevealTutorial();

var chapter1 = new RevealTutorialChapter("chapter1");

//chapter1.addHiddenElement("#newui-progress-link");
//chapter1.addHiddenElement(".chat_box");
//chapter1.addHiddenElement("#buttonbar");
//chapter1.addHiddenElement(".main-bottomhalf");

chapter1.addStep(null, "A quick UI walkthrough", "I'm just going to quickly go over each of the main parts of the UI in case you've been having some trouble. <b>Click anywhere on the screen to continue to the next box.</b>");
chapter1.addStep("#locationName", "Where you are", "This is the name of the location where you are in the game world.");
chapter1.addStep("#pullout-button", "Game Options", "Here you'll find your profile where you can donate, re-roll your character, and manage your character's group settings. You will also find a help button and the logout link here.");
chapter1.addStep("#mainMoneyIndicator", "How much gold you have", "This is how much gold you have, which you can use to buy stuff from other players. You get gold from killing monsters, or from selling equipment or services to other players.<br><br>This view also tells you how much donation credit you have available. Clicking on it will bring you to a page where you can donate to get more credit.");
chapter1.addStep("#map-button", "Game world map", "This opens the game world map which is a snapshot of a map created by the players and is an open-source and on-going project.");
chapter1.addStep("#inventory-button", "Your inventory and equipment", "This opens up a window that shows your current inventory and the equipment your character currently has on. It also allows you to equip yourself with weapons and armor.");
chapter1.addStep("#settings-button", "Game settings", "Clicking here will open a window that allows you to change game settings including volume controls, bandwidth controls (like removing banners to save on bandwidth usage), and a few other things.");
//chapter1.addStep("#header-mute", "Quick mute", "This allows you to quickly mute or unmute the sound for the game with one click.");
chapter1.addStep("#inBannerCharacterWidget", "Your character", "This area gives you some quick information about your character. If you have any buffs or debufs, they will be seen here as well. Clicking on buffs will give you detailed information about it. The red bar represents your hitpoints. If you have 0 or less hitpoints, you are either unconscious or dead!");
chapter1.addStep(".avatar-equip-backing", "Your stats and equipment", "Clicking here will show details about your equipment, your stats (strength, dexterity, intelligence), as well as how much you are carrying. If you hover your mouse over this area, you will also get a quick breakdown of your current strength, dexterity, intelligence, and how much weight you're carrying (unavailable on mobile).");
chapter1.addStep("#character-switcher", "Character switcher", "Clicking on your name here will allow you to switch to and manage your characters. This is a feature that is only available to premium members.");
chapter1.addStep("#immovablesPanel", "Special items", "This area will show interactive icons for special items in the area. These are usually items that are permanent or semi-permanent fixtures for the location, like a firepit.");
chapter1.addStep("#thisLocation-button", "This location", "Clicking here will bring up a list of options for actions you can take in your current location. For example, exploring, looking at nearby items on the ground, looking for nearby shops..etc.");
chapter1.addStep("#navigation-button", "Leave this location", "This will bring up a list of locations you can move to from your current position in the game world. This includes buildings as well as the exits from this location.");

uiTutorial.addChapter(chapter1);






//var chapter2 = new RevealTutorialChapter("chapter2");
//
//chapter2.addHiddenElement("#buttonbar");
//chapter2.addHiddenElement(".main-bottomhalf");
//
//chapter2.addStep("#chat_box_minimize_button", "Minimize chat window", "If you don't want to use chat, you can use this button to minimize and restore the chat window.");
//chapter2.addStep(".fullscreenChatButton", "Maximize chat window", "Handy for when you're only interested in chatting. This button will maximize the chat window so it becomes full screen.");
//chapter2.addStep("#GlobalChat_tab", "Global chat", "Every player across the entire game server is able to use this channel to chat.");
//chapter2.addStep("#LocationChat_tab", "Location chat", "Only players that are in your current location (your location is shown in the top left of the screen) can see the chat written here. Use it to talk to people only in your current location.");
//chapter2.addStep("#GroupChat_tab", "Group chat", "Only players that are in your group can see the chat written here. Use it to privately talk to people who are in your group.");
//chapter2.addStep("#PartyChat_tab", "Party chat", "Only players that are in your party can see the chat written here. Use it to privately talk to people who are in your party.");
//chapter2.addStep("#PrivateChat_tab", "Private chat", "This tab is for private 1 on 1 chat between you and one other player. You can initiate a private chat with a player by clicking on their name, then pressing the Private Chat link at the top.");
//chapter2.addStep(".chat_tab_help_button", "Chat commands help", "This will give you a list of the available chat commands. They can be quite handy.");
//chapter2.addStep("#ignore-players", "Ignore player management", "If certain players are spamming the chat, you can ignore and unignore them with the options here. Furthermore, type /ignore charactername in chat in order to put up a handy link for ignoring that player with one click. You can also click on a character's name and then click the Ignore link at the top to ignore that player.");
//
//uiTutorial.addChapter(chapter2);






var chapter3 = new RevealTutorialChapter("chapter3");

chapter3.addStep("#questButton img", "Your quests", "This opens the quest list page. If you are a new player, you are automatically given a series of quests to complete to help you learn the game. Go here to start and finish quests.");
chapter3.addStep("#inventionButton img", "Invention/Crafting system", "This opens the invention page. This page is where you initiate experiments, manage and implement your character's ideas, and construct items and buildings.");
chapter3.addStep("#manageStore img", "Manage your store", "This button will take you to your storefront management page. This page allows you to setup your storefront by specifying which items you would like to sell to other players and for how much. More help can be found in the storefront page itself.");
chapter3.addStep("#toggleStorefront img", "Enable/disable your store", "This button will turn on and off your storefront. Since you cannot move while vending, you will need to turn off your store before you go off adventuring. This button makes turning your store on and off quick and easy.");
chapter3.addStep("#togglePartyJoin img", "Enable/disable party joins", "This is the party join button. When enabled (without the red cross), other characters will be able to join you in a party. If you are not already in a party then when someone joins you, you will automatically become the party leader. More information on parties and how they work can be found in the game mechanics page. Type /mechanics in chat to get that link.");
chapter3.addStep("#toggleDuel img", "Enable/disable duel requests", "This button allows you to control whether or not you are accepting duel requests. When enabled, other players are able to request to duel with you. You will be given the option to accept a duel request or deny it. When you accept, you will be whisked away into a special arena where you and the other player will engage in battle. More information on the different types of duels and how they work can be found in the game mechanics page. Type /mechanics in chat to get that link.");
chapter3.addStep("#toggleCloak img", "Enable/disable your cloak", "This button will not allow other players to see your character stats, referral stats, or equipment. It can be an important tool in PvP to hide your equipment so other players are less prepared to attack you since they do not know what you're weak to. However if you're not planning on doing PvP any time soon, keeping this option off makes it easier for people to see what you have and to help you - or just to show off your great gear.");

uiTutorial.addChapter(chapter3);






var chapter4 = new RevealTutorialChapter("chapter4");

chapter4.addStep("#main-explore", "Look around for something interesting", "This is one of the more important buttons in the game. Clicking here will cause your character to explore your current location in search of anything that might be of interest. This is how you find new locations, new monsters to fight, and new collectables (things like iron ore deposits, berries..etc).");
chapter4.addStep("#main-explore-ignorecombatsites", "Explore but ignore old sites", "Some locations can have a lot of old combat sites that are left over from other players. This variant of the explore button will cause your character to ignore old combat and collection sites when looking for things of interest.");
chapter4.addStep("#main-merchantlist", "Show stores around here", "This will open a page that shows you all the stores that are in your current location. Note the premium token exchange can also be found here but that page is server-wide and not location specific.");
chapter4.addStep("#main-itemlist", "Show items around here", "This will open a page that shows you all the items that are on the ground in your current location. You can pick up items from the ground or drop items from your inventory this way.<br><br>Pro tip: If you're just starting the game, you usually find some usable equipment that has been thrown away in your starting area - that can get you started!");
chapter4.addStep("#main-characterlist", "Show characters near you", "This will open a page that shows you the characters that are the same location as you. If there are a lot of characters then only the first 50 will be shown.");

chapter4.addStep(null, "That's it!", "If you're new and having trouble figuring out what to do first, click on the exclaimation mark on the button bar and try following the quests there. They will teach you how to play the game by getting you to play the game.");

uiTutorial.addChapter(chapter4);
