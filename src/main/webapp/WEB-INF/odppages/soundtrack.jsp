<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no"/> 
<title>Initium Soundtrack</title>
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jplayer/2.9.2/jplayer/jquery.jplayer.min.js" integrity="sha256-YAIw54P6OPiIkUJq8S3ayOEunEz/MiK2AxDY0oFLNBs=" crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jplayer/2.9.2/add-on/jplayer.playlist.min.js" integrity="sha256-UjXdMG9nbF2ZmDKNlSt8Y9WToEHCRNTZfp1g5XCiOxQ=" crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jplayer/2.9.2/add-on/jquery.jplayer.inspector.min.js" integrity="sha256-pMpIiMchFwrWYxZMq5zJkGrKGE4aKYFZVFnTnPG+EOU=" crossorigin="anonymous"></script>

<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jplayer/2.9.2/skin/pink.flag/css/jplayer.pink.flag.css" integrity="sha256-zha0/QWidlbw6pW0mFxmdJmFHWOPdTxJA62rO/zY+7E=" crossorigin="anonymous" />

<script type='text/javascript'>
$(document).ready(function(){
	
var myPlaylist = new jPlayerPlaylist({
	  jPlayer: "#jquery_jplayer_N",
	  cssSelectorAncestor: "#jp_container_N"
	}, 
		[
		<c:forEach var="track" items="${tracks}">
		  {title:"<c:out value='${track.name}'/>",artist:"",mp3:"<c:out value='${track.url}'/>"},
		</c:forEach>
		]
	, {
	  playlistOptions: {
	    enableRemoveControls: true
	  },
	  swfPath: "https://cdnjs.cloudflare.com/ajax/libs/jplayer/2.9.2/jplayer/jquery.jplayer.swf",
	  supplied: "ogg, mp3",
	  smoothPlayBar: true,
	  keyEnabled: true,
	  audioFullScreen: true // Allows the audio poster to go full screen via keyboard
	});
});
</script>

<script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-62104245-1', 'auto');
  ga('send', 'pageview');

</script>


<style>

	@font-face {
	    font-family: DOS;
	    src: url(FRIZQUAD.TTF);
	}
	
	
	body
	{
		font-family: DOS, Monospace;
		color:#CCCCCC;
		margin:0px;
		background-color: #111111;
		background-image:url("https://initium-resources.appspot.com/images/ui3/background1.jpg");
	}
	
	a
	{
		text-decoration: none;
	}

	.center
	{
		margin: 0 auto;
	}
	.header
	{
		margin-top:5px;
		margin-bottom:10px;
		text-align:center;
	}
	.header img
	{
		margin: 0 auto;
		max-width:100%;	
	}
	.subtitle
	{
		margin-top:5px;
		font-size:1.5em;
		text-shadow: 0px 4px 5px #000;
	}
	.footer
	{
		background-image:url("https://initium-resources.appspot.com/images/ui/newui/header1-background.png");
		height:27px;
		min-height:27px;
		max-height:27px;
		width:100%;
	}
	
	/* header1 button */
	.header1-button:before
	{
		content: ' ';
		position:absolute;
		background-image:url("https://initium-resources.appspot.com/images/ui/newui/header1-button-left.png");
		min-width:7px;
		height:27px;
		min-height:27px;
		max-height:27px;
		top:0px;
		left:0px;
	}
	.header1-button
	{
		color:#DDDDDD;
		text-transform:uppercase;
	
		cursor:pointer;
		display:table-cell;
		position:relative;
		background-image:url("https://initium-resources.appspot.com/images/ui/newui/header1-button-middle.png");
		height:21px;
		max-height:21px;
		min-height:27px;
		padding-top:5px;
		padding-left:9px;
		padding-right:7px;
		vertical-align:top;
	
		text-shadow: 1px 1px 0px rgba(0, 0, 0, 1);
	}
	.header1-button:after
	{
		content: ' ';
		position:absolute;
		background-image:url("https://initium-resources.appspot.com/images/ui/newui/header1-button-right.png");
		min-width:7px;
		height:27px;
		min-height:27px;
		max-height:27px;
		top:0px;
		right:0px;
	}
	.header1-button:hover:before, .header1-button.selected:before
	{
		background-image:url("https://initium-resources.appspot.com/images/ui/newui/header1-button-left-hover.png");
	}
	.header1-button:hover, .header1-button.selected
	{
		background-image:url("https://initium-resources.appspot.com/images/ui/newui/header1-button-middle-hover.png");
	}
	.header1-button:hover:after, .header1-button.selected:after
	{
		background-image:url("https://initium-resources.appspot.com/images/ui/newui/header1-button-right-hover.png");
	}
	
	.header1-spacer
	{
		display:table-cell;
		background-image:url("https://initium-resources.appspot.com/images/ui/newui/header1-spacer.png");
		min-width:11px;
		height:27px;
		min-height:27px;
		max-height:27px;
	}
	
	.v3-main-button
	{
		border-style: solid;
		border-width: 17px 32px 17px 31px;
		-moz-border-image: url(https://initium-resources.appspot.com/images/ui3/button1.jpg) 17 32 17 31 repeat stretch;
		-webkit-border-image: url(https://initium-resources.appspot.com/images/ui3/button1.jpg) 17 32 17 31 repeat stretch;
		-o-border-image: url(https://initium-resources.appspot.com/images/ui3/button1.jpg) 17 32 17 31 repeat stretch;
		border-image: url(https://initium-resources.appspot.com/images/ui3/button1.jpg) 17 32 17 31 fill repeat stretch;
		text-shadow: 2px 2px 3px rgba(0, 0, 0, 1);
		max-height:56px;
		display:inline-block;
		margin:3px;
		position:relative;
		cursor:pointer;
		color:#FFFFFF;
		
		box-shadow: 0px 5px 24px #000;		
		
	}
	.v3-main-button:hover
	{
		text-shadow: 0px 0px 20px yellow;
	}
	
	.jp-type-playlist
	{
		box-shadow: 0px 5px 24px #000;
	}
	
	
</style>
</head>
<body>
<div style='margin:0 auto;display:table;'>
	<a class='v3-main-button' href='https://www.playinitium.com'>Play the game</a>
</div>
<div class='header'>
	<img src='https://initium-resources.appspot.com/images/ui3/logo3.png' border=0/>
	<div class='subtitle'>Official Soundtrack</div>
</div>
<div id="jp_container_N" class="jp-video jp-video-270p center" role="application" aria-label="media player">
	<div class="jp-type-playlist">
		<div id="jquery_jplayer_N" class="jp-jplayer" style="width: 480px; height: 270px;"><img id="jp_poster_0" style="width: 480px; height: 270px; display: inline;" src="http://www.jplayer.org/audio/poster/The_Stark_Palace_640x360.png"><audio id="jp_audio_0" preload="metadata" src="http://www.jplayer.org/audio/ogg/TSP-01-Cro_magnon_man.ogg" title="Cro Magnon Man"></audio><video id="jp_video_0" preload="metadata" style="width: 0px; height: 0px;" title="Cro Magnon Man"></video></div>
		<div class="jp-gui">
			<div class="jp-video-play" style="display: none;">
				<button class="jp-video-play-icon" role="button" tabindex="0">play</button>
			</div>
			<div class="jp-interface">
				<div class="jp-progress">
					<div class="jp-seek-bar" style="width: 100%;">
						<div class="jp-play-bar" style="width: 0%;"></div>
					</div>
				</div>
				<div class="jp-current-time" role="timer" aria-label="time">00:00</div>
				<div class="jp-duration" role="timer" aria-label="duration">04:27</div>
				<div class="jp-details" style="display: none;">
					<div class="jp-title" aria-label="title">Cro Magnon Man</div>
				</div>
				<div class="jp-controls-holder">
					<div class="jp-volume-controls">
						<button class="jp-mute" role="button" tabindex="0">mute</button>
						<button class="jp-volume-max" role="button" tabindex="0">max volume</button>
						<div class="jp-volume-bar">
							<div class="jp-volume-bar-value" style="width: 80%;"></div>
						</div>
					</div>
					<div class="jp-controls">
						<button class="jp-previous" role="button" tabindex="0">previous</button>
						<button class="jp-play" role="button" tabindex="0">play</button>
						<button class="jp-stop" role="button" tabindex="0">stop</button>
						<button class="jp-next" role="button" tabindex="0">next</button>
					</div>
					<div class="jp-toggles">
						<button class="jp-repeat" role="button" tabindex="0">repeat</button>
						<button class="jp-shuffle" role="button" tabindex="0">shuffle</button>
						<button class="jp-full-screen" role="button" tabindex="0">full screen</button>
					</div>
				</div>
			</div>
		</div>
		<div class="jp-playlist">
			<ul style="display: block;">
			<c:forEach var="track" items="${tracks}">
			<li class="jp-playlist-current"><div><a href="javascript:;" class="jp-playlist-item-remove">×</a><a href="javascript:;" class="jp-playlist-item jp-playlist-current" tabindex="0">${track.name} <span class="jp-artist"></span></a></div></li>
			</c:forEach>
			</ul>
		</div>
		<div class="jp-no-solution" style="display: none;">
			<span>Update Required</span>
			To play the media you will need to either update your browser to a recent version or update your <a href="http://get.adobe.com/flashplayer/" target="_blank">Flash plugin</a>.
		</div>
	</div>
</div>

</body>
</html>