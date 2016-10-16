var audioFilenames = [
"bird10",
"bird11",
"bird12",
"bird2",
"bird3",
"bird4",
"bird5",
"bird6",
"bird7",
"bird8",
"bird9",
"blacksmith1",
"campfire1",
"cicada1",
"construction1",
"crickets1",
"crow1",
"crow2",
"crowd1",
"crowd2",
"distant_drums1",
"drips1",
"music_eerie_electronic1",
"music_eerie_electronic2",
"music_eerie_flute1",
"rain1",
"rain2",
"stream1",
"thunder1",
"thunder2",
"thunder3",
"whispers1",
"wind1",
"wind2",
"wind3",
"wind4",
"oceanshore1",
"squirrel1",
"squirrel2",
"squirrel3",
"sailing1",
"seagull1",
"seagull2",
"seagull3",
"seagull4",
"seagull5"
];

var audioDescriptorPresets = 
{
"LightForest":"" +
		"wind1(100,20,true,clearday)&&" +
		"crickets1(100,10,true,clearnight)&&" +
		"bird2(10,10,false,clearday)&&" +
		"bird3(10,10,false,clearday)&&" +
		"bird4(10,10,false,clearday)&&" +
		"bird9(10,10,false,clearday)&&" +
		"bird10(10,10,false,clearday)&&" +
		"bird11(10,10,false,clearday)&&" +
		"squirrel1(5,10,false,clearday)&&" +
		"squirrel2(5,10,false,clearday)&&" +
		"squirrel3(5,10,false,clearday)&&" +
		"crow1(5,10,false,clearday)",
"DenseForest":"" +
		"wind1(100,20,true,clearday)&&" +
		"crickets1(100,10,true,clearnight)&&" +
		"bird2(10,10,false,clearday)&&" +
		"bird3(10,10,false,clearday)&&" +
		"bird4(10,10,false,clearday)&&" +
		"bird5(10,10,false,clearday)&&" +
		"bird6(10,10,false,clearday)&&" +
		"bird7(10,10,false,clearday)&&" +
		"bird8(10,10,false,clearday)&&" +
		"bird9(10,10,false,clearday)&&" +
		"bird10(10,10,false,clearday)&&" +
		"bird11(10,10,false,clearday)&&" +
		"bird12(10,10,false,clearday)&&" +
		"squirrel1(5,10,false,clearday)&&" +
		"squirrel2(5,10,false,clearday)&&" +
		"squirrel3(5,10,false,clearday)&&" +
		"crow1(5,7,false,clearday)",
"GrassyPlains":"" +
		"cicada1(100,10,true,clearday)&&" +
		"crickets1(100,10,true,clearnight)&&" +
		"bird2(5,10,false,clearday)&&" +
		"bird3(5,10,false,clearday)&&" +
		"bird6(5,10,false,clearday)&&" +
		"bird7(5,10,false,clearday)&&" +
		"bird11(5,10,false,clearday)&&" +
		"bird12(5,10,false,clearday)&&" +
		"crow1(5,5,false,clearday)",
"Mountains":"" +
		"wind2(100,15,true)",
"Ocean":"" +
		"oceanshore1(100,30,true)&&"+
		"wind1(100,10,true,day)&&" +
		"seagull1(15,10,false,day)&&"+
		"seagull2(15,10,false,day)&&"+
		"seagull3(15,10,false,day)&&"+
		"seagull4(15,60,false,day)&&"+
		"seagull5(15,60,false,day)",
"OceanSailing":"" +
		"sailing1(100,30,true)&&"+
		"seagull1(5,5,false,day)&&"+
		"seagull2(5,5,false,day)&&"+
		"seagull3(5,10,false,day)",
"ForestStream":"" +
		"stream1(100,30,true)&&" +
		"wind1(100,20,true,clearday)&&" +
		"crickets1(100,10,true,clearnight)&&" +
		"bird2(10,10,false,clearday)&&" +
		"bird4(10,10,false,clearday)&&" +
		"bird6(10,10,false,clearday)&&" +
		"bird8(10,10,false,clearday)&&" +
		"bird10(10,10,false,clearday)&&" +
		"bird12(10,10,false,clearday)&&" +
		"squirrel1(5,10,false,clearday)&&" +
		"squirrel2(5,10,false,clearday)&&" +
		"squirrel3(5,10,false,clearday)&&" +
		"crow1(5,10,false,clearday)",
"Windy":"" +
		"wind1(100,20,true)&&"+
		"wind3(100,40,true)",
"VeryWindy":"" +
		"wind1(100,30,true)&&"+
		"wind2(100,30,true)&&"+
		"wind4(100,40,true)"
};

var audioFilesToForceLoad = [
"rain1",
"wind2",
"rain2"
];

var lastEnvironmentAudioMode = "";

var audioDescriptor = [];
var audioDescriptor_filenames = [];
var audioDescriptorPreset = "";
var outsideAudio = false;

var audioInstances = [];
var loadedAudioCount = 0;
var loadingCount = 0;
var loadedFilenames = [];

var playingLoopedSounds = [];

var randomAudioRequestFrequency = 100;

var windInstance = null;
var rainInstance = null;
var rain2Instance = null;
var isThunderLoaded = false;

createjs.Sound.addEventListener("fileload", soundFileLoaded);
createjs.Sound.alternateExtensions = ["mp3"];
$(document).ready(function(){
	updateEnvironmentSoundEffectsVolume();
});

// The parameters passed to setAudioDescriptor when sound effects were
// disabled, so we can enable it later if desired
var requestedAudioDescriptor = null;

/**
 * This gets called from the pages that need sound. The audioDescriptor of the location
 * being displayed is to be passed in.
 * @param newAudioDescriptor
 * @returns {Array}
 */
function setAudioDescriptor(newAudioDescriptor, preset, isOutside)
{
	if (isSoundEffectsEnabled()==false)
	{
		requestedAudioDescriptor = [newAudioDescriptor, preset, isOutside];
		return;
	}

	
	outsideAudio = isOutside;
	audioDescriptorPreset = preset;
	
	audioDescriptor = [];
	audioDescriptor_filenames = [];
	if (newAudioDescriptor==null)
		newAudioDescriptor = "";
	if (newAudioDescriptorPreset!=null && newAudioDescriptorPreset.length==0)
		newAudioDescriptorPreset = null;

	var newAudioDescriptorPreset = audioDescriptorPresets[preset];
	if (newAudioDescriptorPreset!=null && newAudioDescriptor.length>0)
		newAudioDescriptor = newAudioDescriptorPreset+"&&"+newAudioDescriptor;
	else if (newAudioDescriptor.length==0 && newAudioDescriptorPreset!=null)
		newAudioDescriptor = newAudioDescriptorPreset;

	if (newAudioDescriptor.length>0)
	{
		var audioDescriptorParts = newAudioDescriptor.split("&&");
		for(var i = 0; i<audioDescriptorParts.length; i++)
		{
			var audio = audioDescriptorParts[i];
			audio = audio.replace(")", "");
			audio = audio.replace(" ", "");
			var audioParts = audio.split("(");
			var argString = audioParts[1];
			var arg = argString.split(",");
			
			var filename = audioParts[0];
			var odds = parseFloat(arg[0])/100;
			var volume = parseFloat(arg[1])/100;
			var fadeInOut = false;
			if (arg.length>=3 && arg[2] == "true")
				arg[2] = true;
			var audioMode = null;
			if (arg.length>=4)
				audioMode = arg[3];
			
			audioDescriptor.push({
				filename:filename,
				odds:odds,
				volume:volume,
				fadeInOut:fadeInOut,
				audioMode:audioMode
			});
			audioDescriptor_filenames.push(filename);
		}
	}	
	
	// Now trigger the registering and loading of sounds, but only if we haven't already done so for a given sound...
	// MANUALLY include the special audio sound effects to the loading as well...
	if(outsideAudio)
	{
		for(var i = 0; i<audioFilenames.length; i++)
		{
			if (loadedFilenames.indexOf(audioFilenames[i])==-1 && (audioDescriptor_filenames.indexOf(audioFilenames[i])>-1 || audioFilesToForceLoad.indexOf(audioFilenames[i])>-1))
			{
				loadingCount++;
				loadedFilenames.push(audioFilenames[i]);
				createjs.Sound.registerSound("https://initium-resources.appspot.com/audio/"+audioFilenames[i]+".ogg", audioFilenames[i]);
				audioInstances.push(createjs.Sound.createInstance(audioFilenames[i]));
			}
		}
	}
}


/*private*/ function soundFileLoaded(event)
{
	loadedAudioCount++;
	if (loadedAudioCount==loadingCount)
	{
		createjs.Sound.stop();
		playLoopedSounds();
		setInterval(playRandomSounds, randomAudioRequestFrequency);
	}
}

function getEnvironmentAudioMode()
{
	var mode = "";
	if (rainStrength<0.65)
		mode+="clear";
	else
		mode+="rainy";
	
	if (night>0.5)
		mode+="night";
	else 
		mode+="day";

	return mode;
}

function getAudioInstance(filename)
{
	var index = loadedFilenames.indexOf(filename);
	var audioInstance = audioInstances[index];
	return audioInstance;
}


/**
 * Special audio is for things that are not location specific. Currently only rain and wind.
 */
function updateSpecialAudio()
{
	if (outsideAudio == false)
		return;

	if (window.biome == "Snow")
		return;
	
	var rainVolume = rainStrength;
	rainVolume-=0.6;
	rainVolume*=3;
	if (rainVolume<0) rainVolume = 0;
	if (rainVolume>1) rainVolume = 1;
	
	var rainVolume2 = rainStrength;
	rainVolume2 -= 0.75;
	rainVolume2 *= 4;
	if (rainVolume2<0) rainVolume2 = 0;
	if (rainVolume2>1) rainVolume2 = 1;

	if (window.biome == "Desert")
	{
		rainVolume = 0;
		rainVolume2 = 0;
	}
	
	var windVolume = rainStrength;
	windVolume -= 0.65;
	windVolume *= 3;
	if (windVolume<0) windVolume = 0;
	if (windVolume>1) windVolume = 1;
	
	var rainInstancePreviousVol = rainInstance.volume;
	var rain2InstancePreviousVol = rain2Instance.volume;
	var windInstancePreviousVol = windInstance.volume;
	
	
	rainInstance.volume = rainVolume;
	rain2Instance.volume = rainVolume2;
	windInstance.volume = windVolume;
	
	if (rainInstance.volume==0 && rainInstancePreviousVol>0)
	{
		rainInstance.stop();
	}
	if (rain2Instance.volume==0 && rain2InstancePreviousVol>0)
	{
		rain2Instance.stop();
	}
	if (windInstance.volume==0 && windInstancePreviousVol>0)
	{
		windInstance.stop();
	}
	
	if (rainInstance.volume>0 && rainInstancePreviousVol==0)
	{
		var ppc = new createjs.PlayPropsConfig().set({interrupt: createjs.Sound.INTERRUPT_NONE, loop: -1, volume: rainInstance.volume});
		rainInstance.play(ppc);
	}
	if (rain2Instance.volume>0 && rain2InstancePreviousVol==0)
	{
		var ppc = new createjs.PlayPropsConfig().set({interrupt: createjs.Sound.INTERRUPT_NONE, loop: -1, volume: rain2Instance.volume});
		rain2Instance.play(ppc);
	}
	if (windInstance.volume>0 && windInstancePreviousVol==0)
	{
		var ppc = new createjs.PlayPropsConfig().set({interrupt: createjs.Sound.INTERRUPT_NONE, loop: -1, volume: windInstance.volume});
		windInstance.play(ppc);
	}
	
	
	// Now lets check if the rain is hard enough to possibly trigger lightning, if so, check if we haven't already 
	// preloaded lightning and then preload it if not...
	if (rainStrength>=0.8 && isThunderLoaded==false)
	{
		isThunderLoaded = true;
		playAudio("thunder1", 0, 0);
		playAudio("thunder2", 0, 0);
		playAudio("thunder3", 0, 0);
	}
}

/**
 * Reads the audioDescriptor array to determine which sounds are to be played randomly. It also
 * looks at window.rainStrength and window.night to further only use random sounds that require specific
 * situations. 
 */
function playRandomSounds()
{
	var mode = getEnvironmentAudioMode();
	
	// Check if we need to reset the looped sound effects while we're here
	if (mode!=lastEnvironmentAudioMode)
	{
		clearLoopedSounds();
		playLoopedSounds();
		
		lastEnvironmentAudioMode = mode;
	}
	
	updateSpecialAudio();
	
	if (audioDescriptor==null || audioDescriptor.length==0)
		return;
	
	for(var i = 0; i<audioDescriptor.length; i++)
	{
		var audio = audioDescriptor[i];
		
		if (audio.odds>=1)
			continue;	// If its 100% then it's considered looped and it was already started when the page loaded
		
		if (audio.audioMode!=null && mode.indexOf(audio.audioMode)==-1)
			continue;
		
		var audioInstance = getAudioInstance(audio.filename);
		
		var duration = audioInstance.duration;
		
		var adjustedOdds = audio.odds/(duration/randomAudioRequestFrequency);
		
		if (Math.random()<adjustedOdds)
		{
			var ppc = new createjs.PlayPropsConfig().set({interrupt: createjs.Sound.INTERRUPT_NONE, loop: 0, volume: audio.volume});
			
			audioInstance.play(ppc);
			console.log("Randomly playing: "+audio.filename);
		}
	}
}

/**
 * Reads the audioDescriptor array to determine which sounds are to be played continuously. It also
 * looks at window.rainStrength and window.night for specific effects that might be needed depending on the
 * current environment settings there.
 */
function playLoopedSounds()
{
	var mode = getEnvironmentAudioMode();
	
	for(var i = 0; i<audioDescriptor.length; i++)
	{
		var audio = audioDescriptor[i];
		
		if (audio.odds<1)
			continue;	// If its NOT 100 then it's NOT considered looped and we will not be playing it
		
		if (audio.audioMode!=null && mode.indexOf(audio.audioMode)==-1)
			continue;
		
		var audioInstance = getAudioInstance(audio.filename);
		
		var ppc = new createjs.PlayPropsConfig().set({interrupt: createjs.Sound.INTERRUPT_ANY, loop: -1, volume: audio.volume});
		
		audioInstance.play(ppc);
		playingLoopedSounds.push(audioInstance);
		console.log("Permanently playing: "+audio.filename);
	}
	
	// Always play the rain and wind, but keep volume to minimum at first (updateSpecialAudio will update the volume accordingly)
	if (outsideAudio)
	{
		var ppc = new createjs.PlayPropsConfig().set({interrupt: createjs.Sound.INTERRUPT_ANY, loop: -1, volume: 0});
		rainInstance = getAudioInstance("rain1");
		rain2Instance = getAudioInstance("rain2");
		windInstance = getAudioInstance("wind2");
		rainInstance.volume = 0;
		rain2Instance.volume = 0;
		windInstance.volume = 0;
		
		updateSpecialAudio();
	}
}

function clearLoopedSounds()
{
	for(var i = 0; i<playingLoopedSounds.length; i++)
		playingLoopedSounds[i].stop();
	
	playingLoopedSounds = [];
}

/**
 * Plays a single audio file once.
 * 
 * @param id
 * @param volume
 * @param delay
 */
function playAudio(id, volume, delay)
{
	var audioInstance = getAudioInstance(id);

	console.log("One-shot playing: "+id);
	if (audioInstance==null)
	{
		loadedFilenames.push(id);
		createjs.Sound.registerSound("https://initium-resources.appspot.com/audio/"+id+".ogg", id);
		audioInstances.push(createjs.Sound.createInstance(id));
		return;	// Don't play the sound, it wasn't loaded in yet and it'll be too late by the time it is.
	}
	
	var ppc = new createjs.PlayPropsConfig().set({interrupt: createjs.Sound.INTERRUPT_NONE, loop: 0, volume: volume, delay: delay});

	audioInstance.play(ppc);

}




