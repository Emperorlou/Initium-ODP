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
	
	$("#page-popup-root").append("<div id='"+pagePopupId+"' class='page-popup'><div id='"+mapId+"' class='page-popup-map'></div></div>");
	createLocalMapViewer(mapId);
}

// Disposes of the viewer objecz
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
    viewer = OpenSeadragon({
        id: id,
        prefixUrl: "https://s3.amazonaws.com/imappy/openseadragon/images/",
        springStiffness: 5,
        animationTime: 0.2,
        tileSources: "odp/javascript/images/map/map.xml"
    });
}

function createCommunityMapViewer(id)
{
    viewer = OpenSeadragon({
        id: id,
        prefixUrl: "https://s3.amazonaws.com/imappy/openseadragon/images/",
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
