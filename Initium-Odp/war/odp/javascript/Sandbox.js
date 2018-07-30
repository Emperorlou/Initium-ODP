var viewportContainer = document.getElementById("viewportcontainer");
var viewport = document.getElementById("viewport");
var menu = document.getElementById("menu");
var grid = document.getElementById("grid");
var gridCellLayer = document.getElementById("cell-layer");
var objects = document.getElementsByClassName('gridObject');
var dragDelta = 10;
var map2dScale = 1;
var maxZoom = 4;
var minZoom = .2;
var imgSizeX = 128;
var imgSizeY = 64;
var gridCellWidth = 64;
var gridCellHeight = 32;
var cursorWidth = 164;
var cursorHeight = 166;
var objectZOffset = 100;
var drugged = false;
var reachedZoom = false;
var $picUrlPath = "https://initium-resources.appspot.com/";
var $domain = "https://initium-resources.appspot.com/";
var firstLoad = true;
var previouslySelectedBackground = [];
var previouslySelectedObjects = [];
var previouslyHighlightedBackground = [];
var previouslyHighlightedObjects = [];
var cursorObject = "";
var buildingObject = "";
var clickTimer;
var zoomTouchX;
var zoomTouchY;
var zoomTouch = false;
var timeBetweenLeftClick = 0;
var dragging = false;
var spacePressed = false;
var usingKeys = false;
var isMenuVisible = false;
var menuBuilt = false;
var displayGridLines = false;
var snapToGrid = true;
var keepSelectedCenter = true;
var placingBuilding = false;
var hoveringOverViewport = false;

/**
 * Grid Objects is a HashMap of all objects in the grid
 * key
 * value: GridObject
 * @type {{}}
 */
var gridObjects = {};
/**
 * gridCells is a 2D array of GridCells corresponding to the grid
 * @type {Array}
 */
var gridCells = [];

$(document).ready(function () {
    loadMap();
    
    
});

$('#viewportcontainer').on({
    'mousewheel': function (event) {
        event.preventDefault();
        if (event.originalEvent.deltaY < 0) {
            zoomIn(event, 1);
        } else {
            zoomOut(event, 1);
        }
        $('html, body').stop().animate({}, 500, 'linear');
    }
});

$('#viewportcontainer').on({'dblclick': function (event){
		if (event.which==1)
			zoomIn(event, 1);
		else if (event.which==3)
			zoomOut(event, 1);
			
	}
});

$('#viewportcontainer').on({'mouseup': function (event){
	if (event.which==3 && event.originalEvent.detail>1)
		zoomOut(event, 1);
}
});

function zoomIn(event, additionalScale, onCenter) {
    map2dScale += scaleRate*map2dScale*additionalScale;
    if (maxZoom !== null && map2dScale > maxZoom) {
        map2dScale = maxZoom;
    }
    scaleTiles(event, onCenter);
}

function zoomOut(event, additionalScale, onCenter) {
    map2dScale -= scaleRate*map2dScale*additionalScale;
    if (minZoom !== null && map2dScale < minZoom) {
        map2dScale = minZoom;
    }
    scaleTiles(event, onCenter);
}

function pressedButton() {
    firstLoad = true;
    if (cursorObject != "") {
        cursorObject.div.remove();
        cursorObject = "";
    }
    scaleRate = isNaN($("#zoom").val()) ? .3 : $("#zoom").val();
    loadMap();
    openMenu();
}

function scaleTiles(event, onCenter) {

//    gridTileWidth = isNaN(Number($("#gridWidth").val())) ? 20 : Number($("#gridWidth").val());
//    gridTileHeight = isNaN(Number($("#gridHeight").val())) ? 20 : Number($("#gridHeight").val());
    var forestry = Number($("#forestry").val());
    var scaledGridCellWidth = gridCellWidth * map2dScale;
    var scaledGridCellHeight = gridCellHeight * map2dScale;
    var totalGridWidth = gridTileWidth * scaledGridCellWidth;
    var totalGridHeight = gridTileHeight * scaledGridCellHeight;

    prevGridWidth = $(grid).width();
    prevGridHeight = $(grid).height();
    currGridWidth = totalGridWidth;
    currGridHeight = totalGridHeight;
    diffGridWidth = totalGridWidth - prevGridWidth;
    diffGridHeight = totalGridWidth - prevGridHeight;
    grid.style.height = currGridHeight + "px";
    grid.style.width = currGridWidth + "px";
    originX = grid.getBoundingClientRect().left;
    originY = grid.getBoundingClientRect().top + -$(window).scrollTop();

    if (!onCenter) {
        var userLocX = 0;
        var userLocY = 0;
        if (event) {
            if (event.clientX) {
                // Check for a mouse position
                userLocX = event.clientX;
                userLocY = event.clientY;
            } else if (zoomTouch) {
                userLocX = zoomTouchX;
                userLocY = zoomTouchY;
            } else if (event.changedTouches && event.changedTouches.length == 1) {
                // Check for double tap on mobile
                userLocX = event.changedTouches[0].clientX;
                userLocY = event.changedTouches[0].clientY;
            } else if (event.touches && event.touches[0] && event.touches[0].clientX && event.touches[1] && event.touches[1].clientX) {
                // Check for zooming on mobile, find midpoint of pinch gesture
                offsetX1 = event.touches[0].clientX;
                offsetY1 = event.touches[0].clientY;
                offsetX2 = event.touches[1].clientX;
                offsetY2 = event.touches[1].clientY;

                userLocX = (offsetX2 + offsetX1) / 2;
                userLocY = (offsetY2 + offsetY1) / 2;
                zoomTouchX = userLocX;
                zoomTouchY = userLocY;
                zoomTouch = true;
            } else if (event.changedTouches && event.changedTouches.length > 1) {
                // Check for zooming on mobile, find midpoint of pinch gesture
                offsetX1 = event.changedTouches[0].clientX;
                offsetY1 = event.changedTouches[0].clientY;
                offsetX2 = event.changedTouches[1].clientX;
                offsetY2 = event.changedTouches[1].clientY;

                userLocX = (offsetX2 + offsetX1) / 2;
                userLocY = (offsetY2 + offsetY1) / 2;
                zoomTouchX = userLocX;
                zoomTouchY = userLocY;
                zoomTouch = true;
            } else {
                // Couldn't find mouse/finger position(s), last resort zoom to center of viewport
                userLocX = $(window).width()/2 + viewport.getBoundingClientRect().left;
                userLocY = $(window).height()/2 + viewport.getBoundingClientRect().top + - $(window).scrollTop();
            }
        }
    } else {
        // Zoom to center of viewport
        userLocX = $(window).width()/2 + viewport.getBoundingClientRect().left;
        userLocY = $(window).height()/2 + viewport.getBoundingClientRect().top + - $(window).scrollTop();
    }

    dx = Math.abs(userLocX - originX);
    dy = Math.abs(userLocY - originY);
    widthRatio = currGridWidth / prevGridWidth;
    heightRatio = currGridHeight / prevGridHeight;

    newDx = dx * widthRatio;
    newDy = dy * heightRatio;
    diffX = Math.abs(newDx - dx);
    diffY = Math.abs(newDy - dy);

    if (userLocY < originY && diffGridWidth < 0 || userLocY > originY && diffGridWidth > 0) {
        diffY = diffY * -1;
    }
    if (userLocX < originX && diffGridWidth < 0 || userLocX > originX && diffGridWidth > 0) {
        diffX = diffX * -1;
    }

    if (reachedZoom) {
        if (map2dScale > minZoom && map2dScale < maxZoom) {
            reachedZoom = false;
        } else {
            diffX = 0;
            diffY = 0;
        }
    }
    else if (map2dScale == minZoom || map2dScale == maxZoom) {
        reachedZoom = true;
    }

    newX = grid.offsetLeft + diffX;
    newY = grid.offsetTop + diffY;

    if (!firstLoad) {
        if (newY < ($(window).height() * 1.5) && newY > (-1.5 * $(grid).height())) {
            grid.style.top = newY + "px";
        }
        if (newX < ($(window).width() * 1.5) && newX > (-1.5 * $(grid).width())) {
            grid.style.left = newX + "px";
        }
    } else {
        firstLoad = false;
    }

    if (!reachedZoom && buildingObject != "" && buildingObject != undefined) {
        buildingObject.div.style.width = buildingObject.width * map2dScale * buildingObject.scale + "px";
        buildingObject.div.style.height = buildingObject.height * map2dScale * buildingObject.scale + "px";
        if (snapToGrid) {
            var currCoord = getCoordOfMouse(event);
            var top = currCoord.yGridCoord * scaledGridCellHeight + scaledGridCellHeight / 2 - (buildingObject.yImageOrigin * map2dScale) - ((buildingObject.yGridCellOffset/2) * map2dScale);
            var left = currCoord.xGridCoord * scaledGridCellWidth + scaledGridCellWidth / 2 - (buildingObject.xImageOrigin * map2dScale) - (buildingObject.xGridCellOffset * map2dScale);
            buildingObject.div.style.margin = (scaledGridCellWidth / 2) + "px";
            buildingObject.div.style.left = (left + grid.getBoundingClientRect().left) - viewportContainer.getBoundingClientRect().left + "px";
            buildingObject.div.style.top = (top + grid.getBoundingClientRect().top) - viewportContainer.getBoundingClientRect().top + "px";
        } else {
            buildingObject.div.style.left = event.pageX - (buildingObject.xImageOrigin * map2dScale) - viewportContainer.getBoundingClientRect().left + "px";
            buildingObject.div.style.top = event.pageY - (buildingObject.yImageOrigin * map2dScale) - viewportContainer.getBoundingClientRect().top + "px";
        }
    }

    if (cursorObject != "") {
        var cursorTop = (cursorObject.yGridCoord * scaledGridCellHeight);
        var cursorLeft = (cursorObject.xGridCoord * scaledGridCellWidth);
        var scaledCursorHeight = cursorHeight * map2dScale * .4;
        var scaledCursorWidth = cursorWidth * map2dScale * .4;
        cursorObject.div.style.width = cursorWidth * map2dScale * .4 + "px";
        cursorObject.div.style.height = cursorHeight * map2dScale * .4 + "px";
        cursorObject.div.style.top = (cursorTop-15) + "px";
        cursorObject.div.style.left = (cursorLeft-15) + "px";
        cursorSubObjects = cursorObject.div.children;
        for (i=0; i<cursorSubObjects.length; i++) {
            if (cursorSubObjects[i].style.top != "") {
                cursorSubObjects[i].style.top = (scaledGridCellHeight + scaledCursorHeight/2) + "px";
            }
            if (cursorSubObjects[i].style.left != "") {
                cursorSubObjects[i].style.left = (scaledGridCellWidth + scaledCursorWidth/2) + "px";
            }
        }
    }

    var scaledImgSizeX = imgSizeX * map2dScale;
    var scaledImgSizeY = imgSizeY * map2dScale;
    // Update all tiles
    for (var x = 0; x < gridTileWidth; x++) {
        for (var y = 0; y < gridTileHeight; y++) {

            // Update all grid cells, and background images
            var top = y * scaledGridCellHeight;
            var left = x * scaledGridCellWidth;

            gridCells[x][y].cellDiv.style.width = scaledGridCellWidth + "px";
            gridCells[x][y].cellDiv.style.height = scaledGridCellHeight + "px";
            gridCells[x][y].cellDiv.style.margin = (scaledGridCellWidth / 2) + "px";
            gridCells[x][y].cellDiv.style.top = top + "px";
            gridCells[x][y].cellDiv.style.left = left + "px";

            gridCells[x][y].backgroundDiv.style.width = scaledImgSizeX + "px";
            gridCells[x][y].backgroundDiv.style.height = scaledImgSizeY + "px";
            gridCells[x][y].backgroundDiv.style.top = top + "px";
            gridCells[x][y].backgroundDiv.style.left = left + "px";

            // Update all objects
            if (gridCells[x][y].objectKeys.length > 0) {
                for (var keyIndex = 0; keyIndex < gridCells[x][y].objectKeys.length; keyIndex++) {
                    var currKey = gridCells[x][y].objectKeys[keyIndex];
                    var top = gridObjects[currKey].yGridCoord * scaledGridCellHeight + (scaledGridCellHeight / 2) - (gridObjects[currKey].yImageOrigin * map2dScale) - (gridObjects[currKey].yGridCellOffset * map2dScale);
                    var left = gridObjects[currKey].xGridCoord * scaledGridCellWidth + (scaledGridCellWidth / 2) - (gridObjects[currKey].xImageOrigin * map2dScale) - (gridObjects[currKey].xGridCellOffset * map2dScale);

                    gridObjects[currKey].div.style.width = gridObjects[currKey].width * gridObjects[currKey].scale * map2dScale + "px";
                    gridObjects[currKey].div.style.height = gridObjects[currKey].height * gridObjects[currKey].scale * map2dScale + "px";
                    gridObjects[currKey].div.style.margin = (scaledGridCellWidth / 2) + "px";
                    gridObjects[currKey].div.style.top = top + "px";
                    gridObjects[currKey].div.style.left = left + "px";
                }
            }
        }
    }
}

function loadMap() {

	var setPageConentsBackToInvisible = false;
	if ($("#contents").is(":visible")==false)
	{
		$("#contents").show();
		setPageConentsBackToInvisible = true;
	}
	
    snapToGrid = (document.getElementById('snapToGrid') == null) ? true : document.getElementById('snapToGrid').checked;
    scaleRate = isNaN($("#zoom").val()) ? .3 : $("#zoom").val();
    dragDelta = ($("#dragDelta").val() == undefined) ? 10 : $("#dragDelta").val();
    var seed = isNaN(Number($("#seed").val())) ? 123456 : Number($("#seed").val());
    var forestry = isNaN(Number($("#forestry").val())) ? 2 : Number($("#forestry").val());
    map2dScale = isNaN($("#zoom").val()) ? .3 : $("#zoom").val();

//    var gridCellWidth = 64 * map2dScale;

    var totalGridWidth = gridTileWidth * gridCellWidth;
    var totalGridHeight = gridTileHeight * gridCellHeight;
    var offsetX = $(viewportContainer).width()/2-(totalGridWidth/2);
    var offsetY = $(viewportContainer).height()/2-(totalGridHeight/2);
    grid.style.height = totalGridWidth + "px";
    grid.style.width = totalGridHeight + "px";
    grid.style.top = offsetY + "px";
    grid.style.left = offsetX + "px";

    document.getElementById("main-viewport-container").appendChild(viewportContainer);

    // Remove all current grid tiles
    jQuery('#cell-layer').html('');
    jQuery('#ground-layer').html('');
    jQuery('#object-layer').html('');

    buildMap(JSON.parse(mapData));

    //$.ajax({
    //    url: "SandboxServlet",
    //    data:{width:gridTileWidth, height:gridTileHeight, seed:seed, forestry:forestry},
    //    type: 'POST',
    //    success: function(responseJson) {
    //        buildMap(responseJson);
    //    }
    //});
    buildMenu();
    
    if (setPageConentsBackToInvisible == true)
    {
    	$("#contents").hide();
    }
}


function openMenu() {
    if (isMenuVisible) {
        isMenuVisible = false;
        document.getElementById('menu').style.visibility = 'hidden';
    } else {
        isMenuVisible = true;
        document.getElementById('menu').style.display = 'inline';
        document.getElementById('menu').style.visibility = 'visible';
    }
}

function buildMenu() {
    if (!menuBuilt) {
        htmlString =
            "<table> <tr>" +
            "<td>" + "Zoom Rate: " + "</td><td>" + "<input type='text' id='zoom' value=.3 /> " + "</td>" +
            "<td>" + "Zoom Delta: " + "</td><td>" + "<input type='text' id='zoomDelta' value=.25 /> " + "</td>" +
            "</tr> <tr>" +
            "<td>" + "Drag Delta: " + "</td><td>" + "<input type='text' id='dragDelta' value=5 /> " + "</td>" +
            "<td>" + "Snap to Grid: <input type='checkbox' id='snapToGrid' checked> " + "</td>" +
            "</tr> <tr>" +
            "<td>" + "Grid Lines: <input type='checkbox' id='displayGridLines'> " + "</td>" +
            "<td>" + "Center on Selected: <input type='checkbox' id='keepSelectedCenter' checked> " + "</td>" +
            "</tr> <tr>" +
            "</tr> </table>" +
            "<center>" + "<button id='somebutton'>Update</button>" + "</center>" +
            "<br>";


        jQuery('#menu').html(htmlString);
        document.getElementById('menu').style.width = $(window).width() * (2 / 3) + "px";
        document.getElementById('menu').style.left = $(window).width() * (1 / 6) + "px";
        document.getElementById('menu').style.top = $(window).height() / 2 - menu.offsetHeight / 2 + "px";
        menuBuilt = true;3
    }
}

function buildMap(responseJson) {
    displayGridLines = (document.getElementById('displayGridLines') == null) ? false : document.getElementById('displayGridLines').checked;
    keepSelectedCenter = (document.getElementById('keepSelectedCenter') == null) ? true : document.getElementById('keepSelectedCenter').checked;
    var groundHtml = "";
    var cellHtml = "";
    $.each(responseJson['backgroundTiles'], function (index, value) {
        $.each(value, function (innerIndex, backgroundObject) {

            var gridCell = new GridCell(
                "",
                "",
                backgroundObject.backgroundFile,
                backgroundObject.zIndex,
                [],
                index,
                innerIndex
            );

            var key = index + "-" + innerIndex;
            if (innerIndex == 0) {
                gridCells[index] = [];
            }
            gridCells[index][innerIndex] = gridCell;

            $hexBody = "<div id=\"hex" + key + "Back\"";
            $hexBody += " class=\"gridBackground\"";
            $hexBody += " data-xCoord=\"" + index + "\"";
            $hexBody += " data-yCoord=\"" + innerIndex + "\"";
            $hexBody += " style=\"";
            $hexBody += " z-index:" + backgroundObject.zIndex + ";";
            if (backgroundObject.backgroundFile.indexOf("http")==0)
            	$hexBody += " background:url(" + backgroundObject.backgroundFile + ") center center;";
            else
            	$hexBody += " background:url(" + $picUrlPath + backgroundObject.backgroundFile + ") center center;";
            $hexBody += " background-size:100% 100%;";
            $hexBody += "\">";
            $hexBody += "</div>";
            groundHtml += $hexBody;

            $hexBody = "<div";
            $hexBody += " id=\"hex" + key + "\"";
            $hexBody += " class=\"gridCell\"";
            $hexBody += " data-key=\"" + key + "\"";
            $hexBody += " style=\"";
            if (displayGridLines) {
                $hexBody += " border: 1px solid black;";
            }
            $hexBody += " z-index: 11;";
            $hexBody += "\">";
            $hexBody += "</div>";

            cellHtml += $hexBody;
        });
    });

    // Append HTML
    $('#ground-layer').append(groundHtml);
    $('#cell-layer').append(cellHtml);
    // Gather DOM elements
    var gridCellElements = document.getElementsByClassName('gridCell');
    var gridBackgroundElements = document.getElementsByClassName('gridBackground');
    // Attach elements to gridBackground structure
    for (var i = 0; i < gridBackgroundElements.length; i++) {
        gridCells[gridBackgroundElements[i].dataset.xcoord][gridBackgroundElements[i].dataset.ycoord].backgroundDiv = gridBackgroundElements[i];
        gridCells[gridBackgroundElements[i].dataset.xcoord][gridBackgroundElements[i].dataset.ycoord].cellDiv = gridCellElements[i];
    }

    htmlString = "";
    $.each(responseJson['objectMap'], function (objectKey, gridObject) {
        htmlString += addGridObjectToMap(gridObject);
    });

    //// Append HTML
    //$('#object-layer').append(htmlString);
    
    //// Gather DOM elements
    //var gridObjectElements = document.getElementsByClassName('gridObject');
    //// Attach elements to gridObject structure
    //for (var i = 0; i < gridObjectElements.length; i++) {
    //    gridObjects[objects[i].dataset.key].div = gridObjectElements[i];
    //}
    // Move grid to center, scale, and update all tiles
    centerGridOnScreen();
}

window.onload = function() {

    // Listen for mouse input
    document.onmousedown = startDrag;
    document.onmouseup = stopDrag;

    //document.onmouseover = startHover;
    document.onmousemove=startHover;
    document.onkeydown = keyPress;
    document.onkeyup = keyUnpress;

    // Listen for touch inputs
    document.body.addEventListener('touchend', stopDrag);
    document.body.addEventListener('touchmove', dragDiv, {passive: true});
    document.body.addEventListener('touchstart', startDrag);
}

function currentCoord() {
    // Get currently highlighted or selected coordinates
    var xCoord;
    var yCoord;
    if (previouslyHighlightedBackground != null && previouslyHighlightedBackground.length > 0) {
        xCoord = previouslyHighlightedBackground[0].xGridCoord;
        yCoord = previouslyHighlightedBackground[0].yGridCoord;
    } else if (previouslySelectedBackground != null && previouslySelectedBackground.length > 0) {
        xCoord = previouslySelectedBackground[0].xGridCoord;
        yCoord = previouslySelectedBackground[0].yGridCoord;
    } else {
        return;
    }
    return new CoordObject(xCoord, yCoord);
}
function panGrid(xOffset, yOffset) {
    grid.style.left = grid.offsetLeft + xOffset + 'px';
    grid.style.top = grid.offsetTop + yOffset + 'px';
}
function moveCellOnScreen(xCoord, yCoord) {
    scaledGridCellWidth = gridCellWidth * map2dScale;
    scaledGridCellHeight = gridCellHeight * map2dScale;
    xDist = xCoord * scaledGridCellWidth;
    yDist = yCoord * scaledGridCellHeight;
    if (grid.offsetLeft + xDist < 0) {
        panGrid(gridCellWidth, 0);
    } else if (grid.offsetLeft + xDist + scaledGridCellWidth > $(viewportContainer).width()) {
        panGrid(-gridCellWidth, 0);
    }
    else if (grid.offsetTop + yDist < 0) {
        panGrid(0, gridCellHeight);
    } else if (grid.offsetTop + yDist + scaledGridCellHeight > $(viewportContainer).height()) {
        panGrid(0, -gridCellHeight);
    }

}
function centerCellOnScreen(xCoord, yCoord) {
    // Move grid to center the currently selected cell
    scaledGridCellWidth = gridCellWidth * map2dScale;
    scaledGridCellHeight = gridCellHeight * map2dScale;
    xGrid = xCoord * scaledGridCellWidth + scaledGridCellWidth;
    yGrid = yCoord * scaledGridCellHeight + scaledGridCellHeight;
    xView = grid.offsetLeft + xGrid;
    yView = grid.offsetTop + yGrid;
    xGridOrigin = xView - $(viewportContainer).width()/2;
    yGridOrigin = yView - $(viewportContainer).height()/2;
    grid.style.left = (grid.offsetLeft - xGridOrigin) + "px" ;
    grid.style.top = (grid.offsetTop - yGridOrigin) + "px";
}
function centerGridOnScreen(event) {
    actualGridWidth = (gridCellWidth + (gridCellWidth * gridTileWidth));
    actualGridHeight = (gridCellHeight + (gridCellHeight * gridTileHeight));
    widthScale = $(viewport).width() / actualGridWidth * 0.8;
    heightScale = $(viewport).height() / actualGridHeight * 0.8;
    if (widthScale < heightScale) {
        map2dScale = widthScale;
    } else {
        map2dScale = heightScale;
    }
    scaleTiles(event, true);
    scaledGridWidth = (gridCellWidth * map2dScale  + (gridCellWidth * map2dScale * gridTileWidth));
    scaledGridHeight = (gridCellHeight * map2dScale + (gridCellHeight * map2dScale * gridTileHeight));
    grid.style.left = (($(viewport).width() - scaledGridWidth)/2) + "px";
    grid.style.top = (($(viewport).height() - scaledGridHeight)/2) + "px";
}
function keyUnpress(event) {
    if (event.keyCode == 32) {
        spacePressed = false;
    }
}
function getCenterCell() {
    scaledGridCellWidth = gridCellWidth * map2dScale;
    scaledGridCellHeight = gridCellHeight * map2dScale;
    xMid = Math.round(($(viewportContainer).width()/2 - viewportContainer.offsetLeft - viewport.offsetLeft - grid.offsetLeft - (scaledGridCellWidth/2)) / scaledGridCellWidth);
    yMid = Math.round(($(viewportContainer).height()/2 - viewportContainer.offsetTop - viewport.offsetTop - grid.offsetTop + $(window).scrollTop() - (scaledGridCellHeight/2))/ scaledGridCellHeight);
    if (xMid > gridTileWidth) {xMid = gridTileWidth}
    if (yMid > gridTileWidth) {yMid = gridTileHeight}
    if (xMid < 0) {xMid = 0}
    if (yMid < 0) {yMid = 0}
    return new CoordObject(xMid, yMid);
}
function keyPress(event) {
    if (!hoveringOverViewport) {
        return;
    }
    // Prevent normal key presses
    event.preventDefault();
    if (usingKeys || !keepSelectedCenter) {
        currCoord = currentCoord();
    } else {
        currCoord = getCenterCell();
    }
    var isShift;
    if (event) {
        key = event.keyCode;
        isShift = !!event.shiftKey;
    } else {
        key = event.which;
        isShift = !!event.shiftKey;
    }
    panOffset = 20;
    switch(event.which) {
        case 32:
            spacePressed = true;
            break;
        case 13: // enter
            if (isShift) {
                //panGrid();
            } else {
                updateCursor(currCoord.yGridCoord, currCoord.xGridCoord);
                updateHighlights(previouslySelectedBackground, previouslySelectedObjects, (currCoord.xGridCoord), (currCoord.yGridCoord), true);
            }
            break;
        case 37: // left
            if (isShift) {
                centerGridOnScreen();
            } else if (spacePressed) {
                panGrid(-panOffset, 0);
            } else {
                newXCoord = (currCoord.xGridCoord - 1);
                newYCoord = (currCoord.yGridCoord);
                if (newYCoord < 0 || newXCoord < 0 || newYCoord > (gridTileHeight-1) || newXCoord > (gridTileWidth-1)) {return}
                updateHighlights(previouslyHighlightedBackground, previouslyHighlightedObjects, newXCoord, newYCoord, false);
                if (keepSelectedCenter) {
                    centerCellOnScreen(newXCoord, newYCoord);
                } else {
                    moveCellOnScreen(newXCoord, newYCoord);
                }
            }
            break;
        case 38: // up
            if (isShift) {
                zoomIn(2, true);
            } else if (spacePressed) {
                panGrid(0, -panOffset);
            } else {
                newXCoord = (currCoord.xGridCoord);
                newYCoord = (currCoord.yGridCoord - 1);
                if (newYCoord < 0 || newXCoord < 0 || newYCoord > (gridTileHeight-1) || newXCoord > (gridTileWidth-1)) {return}
                updateHighlights(previouslyHighlightedBackground, previouslyHighlightedObjects, newXCoord, newYCoord, false);
                if (keepSelectedCenter) {
                    centerCellOnScreen(newXCoord, newYCoord);
                } else {
                    moveCellOnScreen(newXCoord, newYCoord);
                }
            }
            break;
        case 39: // right
            if (isShift) {
                if (previouslySelectedBackground != null && previouslySelectedBackground.length > 0) {
                    // Remove previously highlighted cell
                    removeHighlights(previouslyHighlightedBackground[0], previouslyHighlightedObjects, false);
                    centerCellOnScreen(previouslySelectedBackground[0].xGridCoord, previouslySelectedBackground[0].yGridCoord);
                }
            } else if (spacePressed) {
                panGrid(panOffset, 0);
            } else {
                newXCoord = (currCoord.xGridCoord + 1);
                newYCoord = (currCoord.yGridCoord);
                if (newYCoord < 0 || newXCoord < 0 || newYCoord > (gridTileHeight-1) || newXCoord > (gridTileWidth-1)) {return}
                updateHighlights(previouslyHighlightedBackground, previouslyHighlightedObjects, newXCoord, newYCoord, false);
                if (keepSelectedCenter) {
                    centerCellOnScreen(newXCoord, newYCoord);
                } else {
                    moveCellOnScreen(newXCoord, newYCoord);
                }
            }
            break;
        case 40: // down
            if (isShift) {
                zoomOut(2, true);
            } else if (spacePressed) {
                panGrid(0, panOffset);
            } else {
                newXCoord = (currCoord.xGridCoord);
                newYCoord = (currCoord.yGridCoord + 1);
                if (newYCoord < 0 || newXCoord < 0 || newYCoord > (gridTileHeight-1) || newXCoord > (gridTileWidth-1)) {return}
                updateHighlights(previouslyHighlightedBackground, previouslyHighlightedObjects, newXCoord, newYCoord, false);
                if (keepSelectedCenter) {
                    centerCellOnScreen(newXCoord, newYCoord);
                } else {
                    moveCellOnScreen(newXCoord, newYCoord);
                }
            }
            break;

        default: return;
    }
    if (isShift || spacePressed) {
        usingKeys = false;
    } else {
        usingKeys = true;
    }
}


//$('#viewport').on('contextmenu', function(){
//    return false;
//});

function startHover(event) {
    usingKeys = false;
    if (dragging) {return}
    if (!checkIfHoveringOverViewport(event)) {
        hoveringOverViewport = false;
        return;
    } else {
        hoveringOverViewport = true;
    }
    var currCoord = getCoordOfMouse(event);
    if (currCoord.yGridCoord < 0 || currCoord.xGridCoord < 0 || currCoord.yGridCoord > (gridTileHeight-1) || currCoord.xGridCoord > (gridTileWidth-1)) {return}
    // Update building position, if we're are placing a building
    if (placingBuilding && buildingObject != "") {
        if (snapToGrid) {
            var scaledGridCellWidth = gridCellWidth * map2dScale;
            var scaledGridCellHeight = gridCellHeight * map2dScale;
            var top = currCoord.yGridCoord * scaledGridCellWidth + scaledGridCellWidth / 2 - (buildingObject.yImageOrigin * map2dScale) - (buildingObject.yGridCellOffset/2 * map2dScale);
            var left = currCoord.xGridCoord * scaledGridCellWidth + scaledGridCellWidth / 2 - (buildingObject.xImageOrigin * map2dScale) - (buildingObject.xGridCellOffset * map2dScale);
            buildingObject.div.style.margin = (scaledGridCellWidth / 2) + "px";
            buildingObject.div.style.left = (left + grid.getBoundingClientRect().left) - viewportContainer.getBoundingClientRect().left + "px";
            buildingObject.div.style.top = (top + grid.getBoundingClientRect().top) - viewportContainer.getBoundingClientRect().top + "px";
        } else {
            buildingObject.div.style.left = (event.pageX - (buildingObject.xImageOrigin * map2dScale) - viewportContainer.getBoundingClientRect().left) + "px";
            buildingObject.div.style.top = (event.pageY - (buildingObject.yImageOrigin * map2dScale) - viewportContainer.getBoundingClientRect().top) + "px";
        }
        updateHighlights(previouslyHighlightedBackground, previouslyHighlightedObjects, currCoord.xGridCoord, currCoord.yGridCoord, false, buildingObject);
    } else {
        updateHighlights(previouslyHighlightedBackground, previouslyHighlightedObjects, currCoord.xGridCoord, currCoord.yGridCoord, false);
    }
}

function startDrag(event) {
    dragging = true;
    viewportContainer.style.cursor = "move";
    if (!checkIfHoveringOverViewport(event)) {
        return;
    }

    // Prevent normal panning
    event.preventDefault();
    drugged = false;
    // calculate event X, Y coordinates
    if (event) {
        // For mouse clicks
        if (event.clientX) {
            offsetX = event.clientX;
            offsetY = event.clientY;
            updateGridOnUI();
            // For touch input
        } else if (event.touches) {
            if (event.touches.length == 1) {
                offsetX = event.touches[0].clientX;
                offsetY = event.touches[0].clientY;
                document.ontouchmove=dragDiv;
                updateGridOnUI();
            } else {
                offsetX1 = event.touches[0].clientX;
                offsetY1 = event.touches[0].clientY;
                offsetX2 = event.touches[1].clientX;
                offsetY2 = event.touches[1].clientY;
                zoom = true;
                document.ontouchmove=zoomDiv;
            }
        }
    }
}

function checkDoubleClick() {
    if (timeBetweenLeftClick > 17) {
        timeBetweenLeftClick = 0;
        window.clearInterval(clickTimer);
        return false;
    } else if (timeBetweenLeftClick > 0) {
        timeBetweenLeftClick = 0;
        window.clearInterval(clickTimer);
        return true;
    } else {
        clickTimer = window.setInterval(timerIncrement, 25);
    }
}
function timerIncrement() {
    if (timeBetweenLeftClick > 17) {
        timeBetweenLeftClick = 0;
        window.clearInterval(clickTimer);
    } else {
        timeBetweenLeftClick += 1;
    }
}

function getCoordOfMouse(event) {
    var scaledGridCellWidth = gridCellWidth * map2dScale;
    var scaledGridCellHeight = gridCellHeight * map2dScale;
    // For mouse clicks
    if (event.clientX) {
        offsetX = event.clientX;
        offsetY = event.clientY;
        // For touch input
    } else if (event.changedTouches && event.changedTouches[0].clientX) {
        offsetX = event.changedTouches[0].clientX;
        offsetY = event.changedTouches[0].clientY;
    }
    // Determine where the click took place in the grid
    var gridRelx = offsetX - grid.getBoundingClientRect().left - (scaledGridCellWidth / 2);
    var gridRely = offsetY - grid.getBoundingClientRect().top + $(window).scrollTop() - (scaledGridCellHeight / 2);
    var gridColumn = Math.floor(gridRelx / scaledGridCellWidth);
    var gridRow = Math.floor(gridRely / scaledGridCellHeight);
    return new CoordObject(gridColumn, gridRow);
}
function updateCursor(gridRow, gridColumn) {
	
    var scaledGridCellWidth = gridCellWidth * map2dScale;
    var scaledGridCellHeight = gridCellHeight * map2dScale;
    
    var cursorTop = (gridRow * scaledGridCellHeight)-15;
    var cursorLeft = (gridColumn * scaledGridCellWidth);
    var scaledCursorHeight = cursorHeight * map2dScale * .4;
    var scaledCursorWidth = cursorWidth * map2dScale * .4;
    if (cursorObject == "") {
        // First select, build out cursor object
        $('#ui-layer').append(buildCursorHTML(cursorTop, cursorLeft, scaledCursorHeight, scaledCursorWidth, scaledGridCellHeight, scaledGridCellWidth));
        cursorObject = new CursorObject(document.getElementById('cursorObject'), gridColumn, gridRow);
    } else {
        // Only need to update cursor object
        cursorObject.div.style.top = cursorTop + "px";
        cursorObject.div.style.left = cursorLeft + "px";
        cursorObject.xGridCoord = gridColumn;
        cursorObject.yGridCoord = gridRow;

        cursorSubObjects = cursorObject.div.children;
        for (i=0; i<cursorSubObjects.length; i++) {
            if (cursorSubObjects[i].style.top != "") {
                cursorSubObjects[i].style.top = (scaledGridCellHeight + scaledCursorHeight/2) + "px";
            }
            if (cursorSubObjects[i].style.left != "") {
                cursorSubObjects[i].style.left = (scaledGridCellWidth + scaledCursorWidth/2) + "px";
            }
        }
    }
    
    
    
}

function clickMap(event) {
    event.preventDefault();
    if (checkDoubleClick()) {
        if (event.which == 3) {
            zoomOut(2, false);
        } else {
            zoomIn(2, false);
        }
    }
    var currCoord = getCoordOfMouse(event);
    if (currCoord.yGridCoord < 0 || currCoord.xGridCoord < 0 || currCoord.yGridCoord > (gridTileHeight-1) || currCoord.xGridCoord > (gridTileWidth-1)) {return}

    if (placingBuilding) {
        buildingObject.xGridCoord = currCoord.xGridCoord;
        buildingObject.yGridCoord = currCoord.yGridCoord;
        addGridObjectToMap(buildingObject);
        placingBuilding = false;
        buildingObject.div.remove();
        buildingObject = "";
    } else {
        updateCursor(currCoord.yGridCoord, currCoord.xGridCoord);
        updateHighlights(previouslySelectedBackground, previouslySelectedObjects, currCoord.xGridCoord, currCoord.yGridCoord, true);
    }
};

function removeHighlights(previouslyUpdatedBackground, previouslyUpdatedObjects, selection) {
    if (selection) {
        className = "gridSelected";
    } else {
        className = "highlighted";
    }
    // Remove highlights from previously selected divs
    if (previouslyUpdatedBackground != null && previouslyUpdatedBackground.length > 0) {
        for (i=0; i<previouslyUpdatedBackground.length; i++) {
            if (i>=20) break;
            if (i<0) break;
            if (previouslyUpdatedBackground[i].filename.indexOf("http")==0)
            	previouslyUpdatedBackground[i].backgroundDiv.style.background = "url(" + previouslyUpdatedBackground[i].filename + ") center center / 100% 100%";
            else
            	previouslyUpdatedBackground[i].backgroundDiv.style.background = "url(" + $picUrlPath + previouslyUpdatedBackground[i].filename + ") center center / 100% 100%";
            if (selection) {
                previouslyUpdatedBackground[i].backgroundDiv.className = previouslyUpdatedBackground[i].backgroundDiv.className.replace(/(?:^|\s)gridSelected(?!\S)/g, '');
            } else {
                previouslyUpdatedBackground[i].backgroundDiv.className = previouslyUpdatedBackground[i].backgroundDiv.className.replace(/(?:^|\s)highlighted(?!\S)/g, '');
            }
        }
    }
    for (i=0; i<previouslyUpdatedObjects.length; i++) {
        if (previouslyUpdatedObjects[i].key.includes("tempKey:")) {
        	if (previouslyUpdatedObjects[i].filename.indexOf("http")==0)
        		previouslyUpdatedObjects[i].div.style.background = "url(" + previouslyUpdatedObjects[i].filename + ");";
        	else
        		previouslyUpdatedObjects[i].div.style.background = "url(" + $picUrlPath + previouslyUpdatedObjects[i].filename + ");";
        }
        else if (previouslyUpdatedObjects[i].key == "o1") {
            previouslyUpdatedObjects[i].div.style.background = "url(" + $domain + previouslyUpdatedObjects[i].filename + ")";
        } else {
            if (previouslyUpdatedObjects[i].filename == "house1_4.png") {
                previouslyUpdatedObjects[i].div.style.background = "url(" + "http://opengameart.org/sites/default/files/" + previouslyUpdatedObjects[i].filename + ");";
            } else if (previouslyUpdatedObjects[i].filename == "city.svg_.png") {
                previouslyUpdatedObjects[i].div.style.background = "url(" + "http://opengameart.org/sites/default/files/city.svg_.png);";
            } else {
            	if (previouslyUpdatedObjects[i].filename.indexOf("http")==0)
            		previouslyUpdatedObjects[i].div.style.background = "url(" + previouslyUpdatedObjects[i].filename + ");";
            	else
            		previouslyUpdatedObjects[i].div.style.background = "url(" + $picUrlPath + previouslyUpdatedObjects[i].filename + ");";
            }
        }
        previouslyUpdatedObjects[i].div.style.backgroundSize = "100% 100%";
        previouslyUpdatedObjects[i].div.style.backgroundBlendMode = "";
        if (selection) {
            previouslyUpdatedObjects[i].div.className = previouslyUpdatedObjects[i].div.className.replace( /(?:^|\s)gridSelected(?!\S)/g , '' );
        } else {
            previouslyUpdatedObjects[i].div.className = previouslyUpdatedObjects[i].div.className.replace( /(?:^|\s)highlighted(?!\S)/g , '' );
        }
    }
    if (selection) {
        previouslySelectedBackground = [];
    } else {
        previouslyHighlightedBackground = [];
    }
    previouslyUpdatedObjects = [];
}

function updateHighlightsAtCoord(previouslyUpdatedBackground, previouslyUpdatedObjects, gridColumn, gridRow, selection) {
    // Highlight the background div
    gridCells[gridColumn][gridRow].backgroundDiv.className += " " + className;
    previouslyUpdatedBackground.push(gridCells[gridColumn][gridRow]);
    // If we have an objects at this coord, highlight them and display their names
    if (gridCells[gridColumn][gridRow].objectKeys.length > 0) {
        tmpString = "";
        objectKeys = gridCells[gridColumn][gridRow].objectKeys;
        for (selectedIndex=0; selectedIndex<objectKeys.length; selectedIndex++){
            object = gridObjects[objectKeys[selectedIndex]];
            // Update the selected object list
            tmpString += "<br>" + object.name + "<br/>";
            // Highlight the objects in the viewport
            object.div.className += " " + className;
            //object.div.style.backgroundBlendMode = "normal, overlay";
            // Add div to previouslySelected to remove highlight on later click
            previouslyUpdatedObjects.push(object);
        }
        // Update object list
        if (selection) {
            $("#selectedObjects").html(tmpString);
        }
    } else {
        if (selection) {
            $("#selectedObjects").html('<br> No objects at this coordinate. </br>');
        }
    }
    var updatedMap = new Object();
    updatedMap[0] = previouslyUpdatedBackground;
    updatedMap[1] = previouslyUpdatedObjects;
    return updatedMap;
}

function updateHighlights(previouslyUpdatedBackground, previouslyUpdatedObjects, gridColumn, gridRow, selection, gridObject) {
    // Remove previous highlights
    removeHighlights(previouslyUpdatedBackground, previouslyUpdatedObjects, selection);
    previouslyUpdatedBackground = [];
    if (gridObject == null || gridObject == undefined) {
        updatedMap = updateHighlightsAtCoord(previouslyUpdatedBackground, previouslyUpdatedObjects, gridColumn, gridRow, selection);
        previouslyUpdatedBackground = updatedMap[0];
        previouslyUpdatedObjects = updatedMap[1];
    } else {
        // Check footprint of object, highlight all cells of object's footprint
        if (gridObject.xFootprint >= gridObject.yFootprint) {
            for (i = 0; i < gridObject.xFootprint; i++) {
                for (j = 0; j < gridObject.yFootprint; j++) {
                    if (gridColumn+i>=20 || gridColumn+i<0) break;
                    if (gridRow+j>=20 || gridRow+j<0) break;
                    updatedMap = updateHighlightsAtCoord(previouslyUpdatedBackground, previouslyUpdatedObjects, gridColumn+i, gridRow+j, selection);
                    previouslyUpdatedBackground = updatedMap[0];
                    previouslyUpdatedObjects = updatedMap[1];
                }
            }
        } else {
            for (i = 0; i < gridObject.yFootprint; i++) {
                for (j = 0; j < gridObject.xFootprint; j++) {
                    if (gridColumn+j>=20 || gridColumn+j<0) break;
                    if (gridRow+i>=20 || gridRow+i<0) break;
                    updatedMap = updateHighlightsAtCoord(previouslyUpdatedBackground, previouslyUpdatedObjects, gridColumn+j, gridRow+i, selection);
                    previouslyUpdatedBackground = updatedMap[0];
                    previouslyUpdatedObjects = updatedMap[1];
                }
            }
        }
    }

    if (selection) {
        previouslySelectedBackground = previouslyUpdatedBackground;
    } else {
        previouslyHighlightedBackground = previouslyUpdatedBackground;
    }
}

function buildCursorHTML(cursorTop, cursorLeft, scaledCursorHeight, scaledCursorWidth, scaledGridCellHeight, scaledGridCellWidth) {
	cursorTop-=15;
    htmlString = "<div id=\"cursorObject\"" + " class=\"cursorObject\"";
    htmlString += " style=\"";
    htmlString += " top:" + cursorTop + "px;";
    htmlString += " left:" + cursorLeft + "px;";
    htmlString += " width:" + cursorWidth * map2dScale * .4 + "px;";
    htmlString += " height:" + cursorHeight * map2dScale * .4 + "px;";
    htmlString += "\">";
    htmlString += "<div id=\"topLeftCursor\"" + " class=\"cursorSubObject\"";
    htmlString += " style=\"";
    htmlString += " z-index:" + 1000000 + ";";
    htmlString += " background:url(" + $picUrlPath + "images/newCombat/selector1.png);";
    htmlString += " width:50%;";
    htmlString += " height:50%;";
    htmlString += " background-size:100% 100%;";
    htmlString += " transform:scaleX(-1);";
    htmlString += "\">";
    htmlString += "</div>";
    htmlString += "<div id=\"topRightCursor\"" + " class=\"cursorSubObject\"";
    htmlString += " style=\"";
    htmlString += " z-index:" + 1000000 + ";";
    htmlString += " background:url(" + $picUrlPath + "images/newCombat/selector1.png);";
    htmlString += " left:" + (scaledGridCellWidth + scaledCursorWidth/2) + "px;";
    htmlString += " width:50%;";
    htmlString += " height:50%;";
    htmlString += " background-size:100% 100%;";
    htmlString += "\">";
    htmlString += "</div>";
    htmlString += "<div id=\"bottomRightCursor\"" + " class=\"cursorSubObject\"";
    htmlString += " style=\"";
    htmlString += " z-index:" + 1000000 + ";";
    htmlString += " background:url(" + $picUrlPath + "images/newCombat/selector1.png);";
    htmlString += " top:" + (scaledGridCellHeight + scaledCursorHeight/2) + "px;";
    htmlString += " left:" + (scaledGridCellWidth + scaledCursorWidth/2) + "px;";
    htmlString += " width:50%;";
    htmlString += " height:50%;";
    htmlString += " background-size:100% 100%;";
    htmlString += " transform:scaleX(-1);";
    htmlString += " transform:scaleY(-1);";
    htmlString += "\">";
    htmlString += "</div>";
    htmlString += "<div id=\"bottomLeftCursor\"" + " class=\"cursorSubObject\"";
    htmlString += " style=\"";
    htmlString += " z-index:" + 1000000 + ";";
    htmlString += " background:url(" + $picUrlPath + "images/newCombat/selector1.png);";
    htmlString += " top:" + (scaledGridCellHeight + scaledCursorHeight/2) + "px;";
    htmlString += " width:50%;";
    htmlString += " height:50%;";
    htmlString += " background-size:100% 100%;";
    htmlString += " transform:scale(-1, -1);";
    htmlString += "\">";
    htmlString += "</div>";
    
    // the inspect button
//    htmlString += "<div class='main-button cursorSubObject' id='gridmap-inspect-button' onclick='alert(0)'";
//    htmlString += "style='";
//    htmlString += " z-index:" + 1000000 + ";";
//    htmlString += "'>Inspect";
//    htmlString += "</div>";
//    htmlString += "";
//    htmlString += "";
//    htmlString += "";
    
    htmlString += "</div>";
    
    return htmlString;
}
function updateGridOnUI() {
    if(!grid.style.left) { grid.style.left='0px'};
    if (!grid.style.top) { grid.style.top='0px'};

    // calculate integer values for top and left 
    // properties
    coordX = parseInt(grid.style.left);
    coordY = parseInt(grid.style.top);
    if (placingBuilding) {
        buildingCoordX = parseInt(buildingObject.div.style.left);
        buildingCoordY = parseInt(buildingObject.div.style.top);
    }
    drag = true;

    // move div element
    document.onmousemove=dragDiv;
}
function dragDiv(event) {
    if (!drag) {return};

    // Update building placement as well, if we're are placing a building
    if (placingBuilding && buildingObject != "" && !snapToGrid) {
        if (snapToGrid) {

            var scaledGridCellWidth = gridCellWidth * map2dScale;
            var scaledGridCellHeight = gridCellHeight * map2dScale;
            // For mouse clicks
            if (event.clientX) {
                buildingOffsetX = event.clientX;
                buildingOffsetY = event.clientY;
                // For touch input
            } else if (event.changedTouches && event.changedTouches[0].clientX) {
                buildingOffsetX = event.changedTouches[0].clientX;
                buildingOffsetY = event.changedTouches[0].clientY;
            }
            // Determine where the click took place in the grid
            var gridRelx = buildingOffsetX - grid.getBoundingClientRect().left - (scaledGridCellWidth / 2);
            var gridRely = buildingOffsetY - grid.getBoundingClientRect().top + $(window).scrollTop() - (scaledGridCellHeight / 2);
            var gridColumn = Math.floor(gridRelx / scaledGridCellWidth);
            var gridRow = Math.floor(gridRely / scaledGridCellHeight);

            var top = gridRow * scaledGridCellWidth + scaledGridCellWidth / 2 - (buildingObject.yImageOrigin * map2dScale) - (buildingObject.yGridCellOffset/2 * map2dScale);
            var left = gridColumn * scaledGridCellWidth + scaledGridCellWidth / 2 - (buildingObject.xImageOrigin * map2dScale) - (buildingObject.xGridCellOffset * map2dScale);
            buildingObject.div.style.margin = (scaledGridCellWidth / 2) + "px";
            buildingObject.div.style.left = (left + grid.getBoundingClientRect().left) - viewportContainer.getBoundingClientRect().left + "px";
            buildingObject.div.style.top = (top + grid.getBoundingClientRect().top) - viewportContainer.getBoundingClientRect().top + "px";
        } else {
            buildingObject.div.style.left = (event.pageX - (buildingObject.xImageOrigin * map2dScale) - viewportContainer.getBoundingClientRect().left) + "px";
            buildingObject.div.style.top = (event.pageY - (buildingObject.yImageOrigin * map2dScale) - viewportContainer.getBoundingClientRect().top) + "px";
        }
    }
    // move div element
    if (event) {
        if (event.clientX) {
            userLocX = event.clientX;
            userLocY = event.clientY;
        } else if (event.touches) {
            userLocX = event.touches[0].clientX;
            userLocY = event.touches[0].clientY;
        }
    }
    //dragDelta = isNaN($("#dragDelta").val()) ? 10 : $("#dragDelta").val();$("#dragDelta").val();
    deltaX = userLocX - offsetX;
    deltaY = userLocY - offsetY;
    updatedX = coordX+deltaX;
    updatedY = coordY+deltaY;
    if (Math.abs(deltaX) > dragDelta || Math.abs(deltaY) > dragDelta) {
        if (updatedX < $(window).width() && ((updatedX > coordX) || updatedX > (-1 * $(grid).width()))) {
            grid.style.left = coordX + userLocX - offsetX + 'px';
            if (placingBuilding) {
                buildingObject.div.style.left = buildingCoordX + userLocX - offsetX + 'px';
            }
        }
        if (updatedY < $(window).height() && ((updatedY > coordY) || (updatedY > (-1 * $(grid).height())))) {
            grid.style.top = coordY + userLocY - offsetY + 'px';
            if (placingBuilding) {
                buildingObject.div.style.top = buildingCoordY + userLocY - offsetY + 'px';
            }
        }
        $('html, body').stop().animate({}, 500, 'linear');
        drugged = true;
    }
    return false;
}

function checkIfHoveringOverViewport(event) {
    var targ = event.target ? event.target : event.srcElement;
    if (checkIfHoveringOverGrid(event)) {return true;}
    else if (targ.className != 'vp' &&
        targ.className != 'vpcontainer') {return false};
    return true;
}
function checkIfHoveringOverGrid(event) {
    var targ = event.target ? event.target : event.srcElement;
    if (targ.className != 'gridBackground' &&
        targ.className != 'gridBackground gridSelected' &&
        targ.className != 'gridBackground highlighted' &&
        targ.className != 'gridBackground highlighted gridSelected' &&
        targ.className != 'gridBackground gridSelected highlighted' &&
        targ.className != 'grid' &&
        targ.className != 'gridCell' &&
        targ.className != 'gridLayer' &&
        targ.className != 'highlighted' &&
        targ.className != 'gridObject' &&
        targ.className != 'gridObject highlighted' &&
        targ.className != 'gridObject gridSelected' &&
        targ.className != 'gridObject gridSelected highlighted' &&
        targ.className != 'gridObject highlighted gridSelected' &&
        targ.className != 'hoveringObject' &&
        targ.className != 'cursorObject' &&
        targ.className != 'cursorSubObject' &&
        targ.className != 'objectLayer') {return false};
    return true;
}
function stopDrag(event) {
    dragging = false;
    viewportContainer.style.cursor = "default";
    document.onmousemove=startHover;
    if (!drugged && checkIfHoveringOverGrid(event)) {
        // User clicked on map without dragging
        clickMap(event);
    }
    drag=false;
    if (!event.touches || event.touches.length < 2) {
        zoomTouch = false;
    }
}

function zoomDiv(event) {
    if (!zoom) {return};
    // find direction
    // calculate event X, Y coordinates
    coffsetX1 = coffsetY1 = coffsetX2 = coffsetY2 = -1;
    if (event.touches && event.touches[0] && event.touches[0].clientX && event.touches[1] && event.touches[1].clientX) {
        // Check for touch posiions for zooming on mobile
        coffsetX1 = event.touches[0].clientX;
        coffsetY1 = event.touches[0].clientY;
        coffsetX2 = event.touches[1].clientX;
        coffsetY2 = event.touches[1].clientY;
    } else if (event.changedTouches && event.changedTouches.length > 1) {
        // Check for recent touch posiions for zooming on mobile
        coffsetX1 = event.changedTouches[0].clientX;
        coffsetY1 = event.changedTouches[0].clientY;
        coffsetX2 = event.changedTouches[1].clientX;
        coffsetY2 = event.changedTouches[1].clientY;
    }
    if (offsetX1 != -1) {
        zoomDelta = isNaN($("#zoomDelta").val()) ? .25 : $("#zoomDelta").val();

        d1 = Math.sqrt(Math.pow((offsetX2 - offsetX1), 2) + Math.pow((offsetY2 - offsetY1), 2));
        d2 = Math.sqrt(Math.pow((coffsetX2 - coffsetX1), 2) + Math.pow((coffsetY2 - coffsetY1), 2));
        delta = Math.abs(d1 - d2);
        if (delta > zoomDelta) {
            if (d1 < d2) {
                zoomIn(event, .25, true);
            } else {
                zoomOut(event, .25, true);
            }
        }
    }
    $('html, body').stop().animate({}, 500, 'linear');
    return false;
}

// --------------------------------------------------------------------------------------- //
// --------------------------------------- Objects --------------------------------------- //
// --------------------------------------------------------------------------------------- //
function CursorObject(div, xGridCoord, yGridCoord) {
    this.div = div;
    this.xGridCoord = xGridCoord;
    this.yGridCoord = yGridCoord;
}

function GridCell(backgroundDiv, cellDiv, filename, zindex, objectKeys, xGridCoord, yGridCoord) {
    this.backgroundDiv = backgroundDiv;
    this.cellDiv = cellDiv;
    this.filename = filename;
    this.zIndex = zindex;
    this.objectKeys = objectKeys;
    this.xGridCoord = xGridCoord;
    this.yGridCoord = yGridCoord;
}

function GridObject(key, div, filename, name, xGridCellOffset, yGridCellOffset, xGridCoord, yGridCoord, xImageOrigin, yImageOrigin, xFootprint, yFootprint, width, height, scale) {
    this.key = key;
    this.div = div;
    this.filename = filename;
    this.name = name;
    this.xGridCellOffset = xGridCellOffset;
    this.yGridCellOffset = yGridCellOffset;
    this.xGridCoord = xGridCoord;
    this.yGridCoord = yGridCoord;
    this.xImageOrigin = xImageOrigin;
    this.yImageOrigin = yImageOrigin;
    this.xFootprint = xFootprint;
    this.yFootprint = yFootprint;
    this.width = width;
    this.height = height;
    this.scale = scale;
};

function CoordObject(xGridCoord, yGridCoord) {
    this.xGridCoord = xGridCoord;
    this.yGridCoord = yGridCoord;
}

// --------------------------------------------------------------------------------------- //
// ------------------------- Manipulating FE from BE responses --------------------------- //
// --------------------------------------------------------------------------------------- //
/**
 * Server response can optionally have:
 *      GridCells (A list of gridCell objects for updating the gridMap)
 *      GridObject (A map of gridObject objects for updating the objects on the gridMap)
 * @param returnJson
 */
function updateGridFromServer(returnJson) {
    // Check if we have any gridCells for updating
    if (returnJson.GridCells) {
        // Update all passed gridCells
        for (index=0; index < returnJson.GridCells.length; index++) {
            updateGridCell(returnJson.GridCells[index]);
        }
    }
    // Check if we have anyt gridObjects for updating
    if (returnJson.GridObjects) {
        // Update all passed gridObjects
        for (index=0; index < returnJson.GridObjects.length; index++) {
            updateGridObject(returnJson.GridObjects[index]);
        }
    }
}

/**
 * Updates a current gridCell of the map, depending on the populated fields of the passed back gridCell
 * @param gridCell
 */
function updateGridCell(gridCell) {
    // For all updates we need to have a coord
    if (gridCell.xGridCoord != undefined && gridCell.yGridCoord != undefined) {
        currentCell = gridCells[gridCell.xGridCoord][gridCell.yGridCoord];
        if (gridCell.backgroundFile) {
            currentCell.filename = gridCell.backgroundFile;
            currentCell.backgroundDiv.style.background = "url(" + $picUrlPath + gridCell.backgroundFile + ") center center";
            currentCell.backgroundDiv.style.backgroundSize = "100% 100%";
        }
    }
}

/**
 * Takes a gridObject returned from BE and udpates the FE data accordingly
 * @param gridObject
 */
function updateGridObject(gridObject) {
    // For all updates we need to have a key
    if (gridObject.key != undefined) {
        currentObject = gridObjects[gridObject.key];
        // Update the new object
        if (gridObject.filename != undefined) {
        	if (gridObject.filename.indexOf("http")==0)
        		currentObject.div.style.backgroundImage = gridObject.filename + ")";
        	else
        		currentObject.div.style.backgroundImage = $picUrlPath + gridObject.filename + ")";
        }
        if (gridObject.xGridCoord != undefined) {
            currentObject.xGridCoord = gridObject.xGridCoord;
        }
        if (gridObject.yGridCoord != undefined) {
            currentObject.yGridCoord = gridObject.yGridCoord;
        }
        if (gridObject.xGridCellOffset != undefined) {
            currentObject.xGridCellOffset = gridObject.xGridCellOffset;
        }
        if (gridObject.yGridCellOffset != undefined) {
            currentObject.yGridCellOffset = gridObject.yGridCellOffset;
        }
        if (gridObject.xImageOrigin != undefined) {
            currentObject.xImageOrigin = gridObject.xImageOrigin;
        }
        if (gridObject.yImageOrigin != undefined) {
            currentObject.yImageOrigin = gridObject.yImageOrigin;
        }
        if (gridObject.width != undefined) {
            currentObject.width = gridObject.width;
        }
        if (gridObject.height != undefined) {
            currentObject.height = gridObject.height;
        }

        // Update necessary grid meta-data
        // If this object is marked for deletion, remove it's key from the gridCells and delete from the hashMap
        if (gridObject.markForDeletion) {
            delete gridObjects[gridObject.key];
            gridCells[gridObject.xGridCoord][gridObject.yGridCoord].objectKeys.remove(gridObject.key);
        }
        // If this object is marked for removal, remove it's key from the gridCells
        else if (gridObject.markForRemoval) {
            gridCells[gridObject.xGridCoord][gridObject.yGridCoord].objectKeys.remove(gridObject.key);
        }
        // Simple update or addition of gridObject
        else {
            // If we have updated coords, use them. Otherwise use the object's old coords
            xCoord = currentObject.xGridCoord;
            yCoord = currentObject.yGridCoord;
            if (gridObject.xGridCoord != undefined) xCoord = gridObject.xGridCoord;
            if (gridObject.yGridCoord != undefined) yCoord = gridObject.yGridCoord;

            // If this object is not marked for removal, see if it exists at the coord, if not add it
            if (!gridCells[xCoord][yCoord].objectKeys.contains(gridObject.key)) {
                gridCells[xCoord][yCoord].objectKeys.add(gridObject.key);
            }
            // If this object is not marked for removal, see if it exists in the object hash, if not add it
            if (gridObjects[gridObject.key] != undefined) {
                // If we add the object to the map, we should have all image related fields defined
                if (gridObject.filename && gridObject.name && gridObject.xGridCellOffset && gridObject.yGridCellOffset &&
                    gridObject.xGridCoord && gridObject.yGridCoord && gridObject.xImageOrigin && gridObject.yImageOrigin &&
                    gridObject.width && gridObject.height) {
                    addGridObjectToMap(gridObject);
                }
            }
        }
    }
}

/**
 * Adds gridObject to the map
 * Builds HTML for gridObject according to the gridObject's attributes
 * Adds object to the gridCell structure
 * @param gridObject
 * Returns HTML string for adding to DOM
 */
function addGridObjectToMap(gridObject) {
	// Don't bother adding it to the map if it's off the map
	if (gridObject.xGridCoord<0 || gridObject.xGridCoord>gridTileWidth || gridObject.yGridCoord<0 || gridObject.yGridCoord>gridTileHeight)
		return;	
	
	var scaledGridCellWidth = gridCellWidth * map2dScale;
    var scaledGridCellHeight = gridCellHeight * map2dScale;	

    var objectScale = gridObject.scale ? gridObject.scale : 1;
    var gridMapObjectWidth = gridObject.width * map2dScale * objectScale;
    var gridMapObjectHeight = gridObject.height * map2dScale * objectScale;
    
    var left = (gridObject.xGridCoord+1) * gridCellWidth - (gridObject.xImageOrigin * map2dScale) - (gridObject.xGridCellOffset * map2dScale);
    var topPos = gridObject.yGridCoord * scaledGridCellHeight + scaledGridCellHeight / 2 - (gridObject.yImageOrigin * map2dScale) - ((gridObject.yGridCellOffset)/2 * map2dScale/2);
    var leftPos = gridObject.xGridCoord * scaledGridCellWidth + scaledGridCellWidth / 2 - (gridObject.xImageOrigin * map2dScale) - ((gridObject.xGridCellOffset) * map2dScale);
    var topZ = topPos + gridMapObjectHeight;
    var key = gridObject.key;

    // If undefined footprint, assume 1x1
    if (gridObject.xFootprint == undefined || gridObject.yFootprint == undefined) {
        var gridCell = gridCells[gridObject.xGridCoord][gridObject.yGridCoord];
        gridCell.objectKeys.push(key);
    }
    // Check footprint of object, add object to all cells it will sit in
    else if (gridObject.xFootprint >= gridObject.yFootprint) {
        for (i = 0; i < gridObject.xFootprint; i++) {
            for (j = 0; j < gridObject.yFootprint; j++) {
                var gridCell = gridCells[gridObject.xGridCoord+i][gridObject.yGridCoord+j];
                gridCell.objectKeys.push(key);
            }
        }
    } else {
        for (i = 0; i < gridObject.yFootprint; i++) {
            for (j = 0; j < gridObject.xFootprint; j++) {
                var gridCell = gridCells[gridObject.xGridCoord+j][gridObject.yGridCoord+i];
                gridCell.objectKeys.push(key);
            }
        }
    }

    
    $hexBody = "<div id=\"object" + gridObject.xGridCoord + "_" + gridObject.yGridCoord + "_" + key + "\" " + "class=\"gridObject\"";
    $hexBody += " data-key=\"" + key + "\"";
    $hexBody += " style=\"";
    $hexBody += " z-index:" + (parseInt(objectZOffset) + parseInt(topZ)) + ";";
    $hexBody += " top:" + topPos + "px;";
    $hexBody += " left:" + leftPos + "px;";
    $hexBody += " margin:" + (scaledGridCellWidth / 2) + "px;";
    $hexBody += " width:" + (gridMapObjectWidth) + "px;";
    $hexBody += " height:" + (gridMapObjectHeight) + "px;";
    $hexBody += " z-index:" + (parseInt(objectZOffset) + parseInt(topZ)) + ";";
    if (gridObject.key == "o1") {
        $hexBody += " background:url(" + $domain + gridObject.filename + ");";
    } else {
        if (gridObject.filename == "house1_4.png") {
            $hexBody += " background: url(&quot;http://opengameart.org/sites/default/files/house1_4.png&quot;) 0% 0% / 100% 100%;";
        } else if (gridObject.filename == "city.svg_.png") {
            $hexBody += " background: url(&quot;http://opengameart.org/sites/default/files/city.svg_.png&quot;) 0% 0% / 100% 100%;";
        } else {
        	if (gridObject.filename.indexOf("http")==0)
        		$hexBody += " background:url(" + gridObject.filename + ");";
        	else
        		$hexBody += " background:url(" + $picUrlPath + gridObject.filename + ");";
        }
    }
    $hexBody += " background-size:100% 100%;";
    $hexBody += "\"";
    $hexBody += " wtfz-index=\"" + (Number(objectZOffset) + Number(topZ)) + "\"";
    $hexBody += ">";
    $hexBody += "</div>";

    $('#object-layer').append($hexBody);
    objectDiv = document.getElementById("object" + gridObject.xGridCoord + "_" + gridObject.yGridCoord + "_" + key);

    var cgridObject = new GridObject(
        gridObject.key,
        objectDiv,
        gridObject.filename,
        gridObject.name,
        gridObject.xGridCellOffset-(gridCellWidth/2),
        (gridObject.yGridCellOffset-(gridCellHeight/2))/2,
        gridObject.xGridCoord,
        gridObject.yGridCoord,
        gridObject.xImageOrigin,
        gridObject.yImageOrigin,
        gridObject.xFootprint ? gridObject.xFootprint : 1,
        gridObject.yFootprint ? gridObject.yFootprint : 1,
        gridObject.width,
        gridObject.height,
        objectScale);
    gridObjects[key] = cgridObject;

    return $hexBody;
}

// --------------------------------------------------------------------------------------- //
// ------------------------------------ Map Functions ------------------------------------ //
// --------------------------------------------------------------------------------------- //
function mapPlow(event) {
    if (cursorObject != "") {
        doCommand(event, "MapPlow", {"xGridCoord": cursorObject.xGridCoord, "yGridCoord": cursorObject.yGridCoord}, function (data, error) {
            if (error) return;
            updateGridFromServer(data);
        });
    }
}

function mapPlaceHouse(event) {
    // All background URL crap (opengameart) in here, and throughout logic, is temporary for testing
    placingBuilding = true;
    buildingHtml = "<div id='buildingObject' class='hoveringObject' style=\"" +
        "background: url('http://opengameart.org/sites/default/files/house1_4.png') center center; background-size:100%; top:" +
        (event.clientY - grid.offsetTop) + "; left:" + (event.clientX - grid.offsetLeft) + "; position:absolute\"></div>";
    $('#viewportcontainer').append(buildingHtml);
    buildingObject = new GridObject("house", document.getElementById("buildingObject"), "house1_4.png", "house", 0, 0, 0, 0, 67 *.6, 160 *.6, 1, 1, 163, 228, .6);
    buildingObject.div.style.width = buildingObject.width * map2dScale * buildingObject.scale + "px";
    buildingObject.div.style.height = buildingObject.height * map2dScale * buildingObject.scale + "px";
}

function mapPlaceCity(event) {
    // All background URL crap (opengameart) in here, and throughout logic, is temporary for testing
    placingBuilding = true;
    buildingHtml = "<div id='buildingObject' class='hoveringObject' style=\"" +
        "background: url('http://opengameart.org/sites/default/files/city.svg_.png') center center; background-size:100%; top:" +
        (event.clientY - grid.offsetTop) + "; left:" + (event.clientX - grid.offsetLeft) + "; position:absolute\"></div>";
    $('#viewportcontainer').append(buildingHtml);
    buildingObject = new GridObject("city", document.getElementById("buildingObject"), "city.svg_.png", "city", 0, 0, 0, 0, 80, 120, 5, 4, 128, 128, 3);
    buildingObject.div.style.width = buildingObject.width * map2dScale * buildingObject.scale + "px";
    buildingObject.div.style.height = buildingObject.height * map2dScale * buildingObject.scale + "px";
}


function inspectCellContents()
{
	var gridCoord = currentCoord();
	
	makeIntoPopupFromUrl("/odp/gridmapcellcontents?tileX="+gridCoord.xGridCoord+"&tileY="+gridCoord.yGridCoord, "Items Here", true);	
}