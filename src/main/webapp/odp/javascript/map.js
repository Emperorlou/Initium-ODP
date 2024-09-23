// Requires 'javascript/openseadragon/openseadragon.min.js'
// Requires 'javascript/script.js

var viewer;

function openMap()
{
	if (viewer!=null) viewer.destroy();

	exitFullscreenChat();
	
	var stackIndex = incrementStackIndex();
	var pagePopupId = "page-popup"+stackIndex;
	var mapId = pagePopupId+"-map";
	
	$("#page-popup-root").append("<div id='"+pagePopupId+"' class='page-popup v3-window1'><a class='page-popup-Reload' onclick='reloadPagePopup()'>&#8635;</a><a class='page-popup-X' onclick='closePagePopup()'>X</a><center><h3>Player-Made Game Map</h3><br/><span>Last updated: September 1st, 2020</span></center><div id='"+mapId+"' class='page-popup-map'></div></div>");
	$("body").scrollTo("#buttonbar");
	createLocalMapViewer(mapId);
}

// Disposes of the viewer object
function closeMap()
{
	try
	{
		viewer.destroy();
	}
	catch(e)
	{
		
	}
}

function createLocalMapViewer(id)
{
	var quality = getMapQuality();
    viewer = OpenSeadragon({
        id: id,
        prefixUrl: "/images/map/controls/",
        springStiffness: 5,
        animationTime: 0.2,
        tileSources: "/images/map/" + quality + ".xml"
        
    });
}

function createCommunityMapViewer(id)
{
    viewer = OpenSeadragon({
        id: id,
        prefixUrl: "/images/map/controls/",
        springStiffness: 5,
        animationTime: 0.2,
        tileSources: {
        	Image: {
	            xmlns:    "http://schemas.microsoft.com/deepzoom/2008",
	            Url:      "https://s3.amazonaws.com/imappy/initium_map/",
	            Format:   "jpg", 
	            Overlap:  "1", 
	            TileSize: "254",
	            Size: {
	                Height: "3093",
	                Width:  "7024"
	            }
        	}
        }
    });
}
