var rainStrength = 0;
var night = 0;
var pageLoadTime = new Date().getTime();



/*
* Gets the current server time in milliseconds.
*/
function getCurrentServerTime()
{
	var currentClientTime = new Date().getTime();

	var delta = serverTime + (currentClientTime-pageLoadTime); 
	return delta;
}

// Weather calculator
function getWeather()
{
	// <%=GameUtils.getWeather()%>
	var serverTime = getCurrentServerTime();
	
	var date = new Date(serverTime);
	
	var behindHour = new Date(date.getFullYear(), date.getMonth(), date.getDate(), date.getHours());
	var aheadHour = new Date(date.getFullYear(), date.getMonth(), date.getDate(), date.getHours()+1);
	var behindMs = behindHour.getTime();
	var aheadMs = aheadHour.getTime();

	
	var behindHourWeather = rnd((behindMs/3600000), 0, 1);
	var aheadHourWeather = rnd((aheadMs/3600000), 0, 1);
	
	// Now interpolate...
	var weatherDifference = aheadHourWeather-behindHourWeather;
	
	var hourProgression = (serverTime-behindHour)/3600000;
	
	var interpolationDelta = weatherDifference*hourProgression;
//	return 0;
	return behindHourWeather+interpolationDelta;
}	

var lightningTrigger = null;
function processLightning()
{
	if (window.biome=="Snow")
		return;
	
	var weather = getWeather();
	var serverTime = getCurrentServerTime();
	
	var lightningOdds = ((0.1+(weather-0.9))/1.5);
	
	serverTime=Math.round(serverTime/(1000*1));
	
	var random = rnd(serverTime, 0, 1);
	if (random<=lightningOdds)
	{
		if (lightningTrigger==null)
			lightningTrigger = true;
		var lightLevel = rnd(getCurrentServerTime(), -1.5, 0.8);
		if (lightLevel<0) lightLevel = 0;
		return lightLevel;
	}
	lightningTrigger = null;
	return 0;
}

var previousR = null;
var previousG = null;
var previousB = null;
var previousA = null;

function updateDayNightCycle(forceRefresh)
{
	// Determine if we're on a small screenc
	var smallScreen = false;
	var windowWidth = $(window).width();
	if (windowWidth<=1024)
		smallScreen = true;
	
	if (forceRefresh==true)
	{
		previousR = null;
		previousG = null;
		previousB = null;
		previousA = null;
	}
	var serverTime = getCurrentServerTime();
	
	//318.47133757961783439490445859873 = 1 second per day
	
	serverTime/=(318.47133757961783439490445859873*60*60*1.5);
	var amount = Math.sin(serverTime);
	if (amount<0) amount*=-1.0;
	amount*=3.0;
	amount-=1.56;
	
	if (window.isOutside=="FALSE" || window.isOutside==false)
		amount = 0;
	
	if (amount>1) amount = 1;
	if (amount<0) amount = 0;
	night = amount;
	
	var banner = $(".banner-shadowbox, .banner1");
	var banner2 = $(".banner-weather");
	
	if (Modernizr.backgroundblendmode==false)
	{
		banner.css("background", "url('"+bannerUrl+"') no-repeat center center");
		banner.css("background-size", "cover");
		return;
	}
	
	rainStrength = getWeather();
	
	
	if (rainStrength>=0 && isWeatherEnabled())
	{
		
		var cloudLevel = ((rainStrength-0.3)*1.3333333333334);
		if (cloudLevel<0) cloudLevel=0;
		if (amount<cloudLevel) amount = cloudLevel;
		
		var rHeaviestRain = 70;
		var gHeaviestRain = 75;
		var bHeaviestRain = 80;
		
		var r = 30;
		var g = 43;
		var b = 83;
		
		var delta = ((rHeaviestRain-r)*cloudLevel)*((night-1)*-1);
		r+=delta;
		r = Math.round(r);
		
		delta = ((gHeaviestRain-g)*cloudLevel)*((night-1)*-1);
		g+=delta;
		g = Math.round(g);
		
		delta = ((bHeaviestRain-b)*cloudLevel)*((night-1)*-1);
		b+=delta;
		b = Math.round(b);
		
		
		
		
		
/*			if (rainStrength>0.9)
			{
				var bg=	"url('"+magicallyDetermineAnimationUrl("https://initium-resources.appspot.com/images/effects/light-rain1-", ".jpg", rainFps, rainFrameCount, 4)+"') no-repeat center center, ";
				bg += 	"url('"+magicallyDetermineAnimationUrl("https://initium-resources.appspot.com/images/effects/light-rain1-", ".jpg", rainFps, rainFrameCount, 9)+"') no-repeat center center, "; 
				bg+=	"url('"+magicallyDetermineAnimationUrl("https://initium-resources.appspot.com/images/effects/light-rain1-", ".jpg", rainFps, rainFrameCount)+"') no-repeat center center, "; 
				bg+= 	"url('"+bannerUrl+"') no-repeat center center, "; 
				bg+=	"rgba(30, 43, 83, "+amount+")";
				banner.css("background", bg);
				banner.css("background-blend-mode", "screen, screen, screen, multiply");
			}
			else if (rainStrength>0.7)
			{
				var bg=	"url('"+magicallyDetermineAnimationUrl("https://initium-resources.appspot.com/images/effects/light-rain1-", ".jpg", rainFps, rainFrameCount, 7)+"') no-repeat center center, ";
				bg+=	"url('"+magicallyDetermineAnimationUrl("https://initium-resources.appspot.com/images/effects/light-rain1-", ".jpg", rainFps, rainFrameCount)+"') no-repeat center center, "; 
				bg+= 	"url('"+bannerUrl+"') no-repeat center center, "; 
				bg+=	"rgba(30, 43, 83, "+amount+")";
				banner.css("background", bg);
				banner.css("background-blend-mode", "screen, screen, multiply");
			}
			else */
		
		var lightning = processLightning();
		
		if (lightningTrigger==true && isSoundEffectsEnabled())
		{
			// Play a lightning sound!
			lightningTrigger = false;
			
			var fileIndex = parseInt(Math.random()*3+1);
			var distance = Math.random();
			var inverseDistance = (distance-1)*-1;
			playAudio("thunder"+fileIndex, inverseDistance, distance*10000);
		}
		
		if (lightning>0)
		{
			// Disable the light effects layer during lightning strikes
			$("#light-grid").css("filter", "brightness(0)");
			
			var rainGif = "hd-light-rain1.gif";
			var bg=	"url('https://initium-resources.appspot.com/images/effects/"+rainGif+"') no-repeat center center, ";
			bg+= 	"url('"+bannerUrl+"') no-repeat center center, "; 
			bg+=	"rgba(230, 230, 230, "+lightning+")";
			banner.css("background", bg);
			banner.css("background-blend-mode", "screen, color-dodge");
			banner.css("background-size", "cover");
			
			var bg2=	"url('https://initium-resources.appspot.com/images/effects/"+rainGif+"') no-repeat center center, ";
			bg2+=	"rgba(230, 230, 230, "+lightning+")";
			banner2.css("background", bg2);
			banner2.css("mix-blend-mode", "color-dodge");
			
			previousR = null;
			previousG = null;
			previousB = null;
			previousA = null;
			
			
			return;
		}
		
		
		// Check if we have made significant changes to the banner lighting. If not, then just get out.
		if (previousR!=null && previousG!=null && previousB!=null && previousA!=null)
		{
			var diffR = getDifference(previousR, r);
			var diffG = getDifference(previousG, g);
			var diffB = getDifference(previousB, b);
			var diffA = getDifference(previousA, amount);
			if (diffR<0.1 && diffG<0.1 && diffB<0.1 && diffA<0.001)
				return;
			
		}

		// Update the light effects layer
		$("#light-grid").css("filter", "brightness("+amount+")");
		
		
		previousR = r;
		previousG = g;
		previousB = b;
		previousA = amount;
		
		
		if (rainStrength>0.65 && window.biome=="Desert")
		{
			var bg=	"";
			bg+="url('https://initium-resources.appspot.com/images/effects/light-sandstorm1.gif') no-repeat center center, ";
			bg+= 	"url('"+bannerUrl+"') no-repeat center center, "; 
			bg+=	"rgba("+r+", "+g+", "+b+", "+amount+") ";
			banner.css("background", bg);
			banner.css("background-blend-mode", "screen, multiply");
			banner.css("background-size", "cover");

			var bg2="url('https://initium-resources.appspot.com/images/effects/light-sandstorm1.gif') no-repeat center center, ";
			bg2+=	"rgba("+r+", "+g+", "+b+", "+amount+") ";
			banner2.css("background", bg2);
			banner2.css("mix-blend-mode", "hard-light");
			
		}
		else if (rainStrength>0.65 && window.biome=="Snow")
		{
			var bg=	"";
			bg+="url('https://initium-resources.appspot.com/images/effects/medium-snow1.gif') no-repeat center center, ";
			bg+= 	"url('"+bannerUrl+"') no-repeat center center, "; 
			bg+=	"rgba("+r+", "+g+", "+b+", "+amount+") ";
			banner.css("background", bg);
			banner.css("background-blend-mode", "screen, multiply");
			banner.css("background-size", "cover");

			var bg2=	"";
			bg2+="url('https://initium-resources.appspot.com/images/effects/medium-snow1.gif') no-repeat center center, ";
			bg2+=	"rgba("+r+", "+g+", "+b+", "+amount+") ";
			banner2.css("background", bg2);
			banner2.css("mix-blend-mode", "screen");
		}
		else if (rainStrength>0.65)
		{
			var rainGif = "hd-light-rain1.gif";

			var bg=	"url('https://initium-resources.appspot.com/images/effects/"+rainGif+"') no-repeat center center, ";
			bg+= 	"url('"+bannerUrl+"') no-repeat center center, "; 
			bg+=	"rgba("+r+", "+g+", "+b+", "+amount+") ";
			banner.css("background", bg);
			banner.css("background-blend-mode", "screen, multiply");
			banner.css("background-size", "cover");

			var bg2=	"url('https://initium-resources.appspot.com/images/effects/"+rainGif+"') no-repeat center center, ";
			bg2+=	"rgba("+r+", "+g+", "+b+", "+amount*2+") ";
			banner2.css("background", bg2);
			banner2.css("mix-blend-mode", "hard-light");
		}
		else
		{
			banner.css("background", "url('"+bannerUrl+"') no-repeat center center, rgba("+r+", "+g+", "+b+", "+amount+")");
			banner.css("background-blend-mode", "multiply, normal");
			banner.css("background-size", "cover");

			banner2.css("background", "rgba("+r+", "+g+", "+b+", "+amount+")");
			banner2.css("mix-blend-mode", "hard-light");
		}
	}
	else
	{
		// Check if we have made significant changes to the banner lighting. If not, then just get out.
		if (previousA!=null)
		{
			var diff = getDifference(previousA, amount);
			if (diff<0.001)
				return;
			
		}

		previousR = null;
		previousG = null;
		previousB = null;
		previousA = amount;
		
		banner.css("background", "url('"+bannerUrl+"') no-repeat center center, rgba(30, 43, 83, "+amount+")");
		banner.css("background-blend-mode", "multiply, normal");
		banner.css("background-size", "cover");

		banner2.css("background", "rgba(30, 43, 83, "+amount+")");
		banner2.css("mix-blend-mode", "hard-light");
	}
}

function getDifference(from,to)
{
	var diff = from-to;
	if (diff<0) diff*=-1;
	
	return diff;		
}

function magicallyDetermineAnimationUrl(urlBase, urlEnd, fps, frameCount,offset)
{
	var currentFrame = new Date().getTime();
	currentFrame /= (1000/fps);
	currentFrame = Math.round(currentFrame % (frameCount-1))+1;
	
	// This will just randomize it
	// currentFrame = Math.floor((Math.random() * frameCount) + 1);
	if (offset)
	{
		currentFrame+=offset;
		if (currentFrame>frameCount)
			currentFrame-=frameCount;
	}
	
	var url = urlBase + currentFrame + urlEnd;
	return url;
}

/**
 * Call this method whenever the location changes so we can enable/disable
 * banner weather accordingly. We might have gone inside, or outside..etc.
 */
var bannerWeatherUpdateTimerId = null;
function updateBannerWeatherSystem()
{
	if (window.isOutside=="TRUE")
	{
		if (bannerWeatherUpdateTimerId!=null)
			clearInterval(bannerWeatherUpdateTimerId);
		
		bannerWeatherUpdateTimerId = setInterval(updateDayNightCycle, 100);
		
		updateDayNightCycle(true);
	}
	else
	{
		$("#light-grid").css("filter", "brightness(0)");
		if (bannerWeatherUpdateTimerId!=null)
		{
			clearInterval(bannerWeatherUpdateTimerId);
			bannerWeatherUpdateTimerId = null;
		}

		if (window.bannerUrl!=null)
		{
			var banner = $(".banner-shadowbox, .banner1");
			banner.css("background", "url('"+window.bannerUrl+"') no-repeat center center");
			banner.css("background-blend-mode", "normal");
			banner.css("background-size", "cover");
			
			var banner2 = $(".banner-daynight");
			banner2.css("background", "none");

		}
	}
}


