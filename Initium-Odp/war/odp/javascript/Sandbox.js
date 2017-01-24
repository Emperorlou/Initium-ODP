var viewportContainer = document.getElementById("viewportcontainer");
var viewport = document.getElementById("viewport");
var menu = document.getElementById("menu");
var grid = document.getElementById("grid");
var gridCellLayer = document.getElementById("cell-layer");
var objects = document.getElementsByClassName('gridObject');
var dragDelta = 10;
var scale = 1;
var maxZoom = 2.4;
var minZoom = .05;
var imgSize = 128;
var gridCellWidth = 64;
var gridCellHeight = 64;
var cursorWidth = 164;
var cursorHeight = 166;
var drugged = false;
var reachedZoom = false;
var $picUrlPath = "https://initium-resources.appspot.com/images/newCombat/";
var $domain = "https://initium-resources.appspot.com/";
var firstLoad = true;
var previouslySelectedBackground;
var previouslySelectedObjects = [];
var previouslyHighlightedBackground;
var previouslyHighlightedObjects = [];
var cursorObject = "";
var clickTimer;
var timeBetweenLeftClick = 0;
var dragging = false;
var spacePressed = false;
var usingKeys = false;
var isMenuVisible = false;
var menuBuilt = false;
var displayGridLines = false;
var keepSelectedCenter = true;

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
    'mousewheel': function (e) {
        e.preventDefault();
        if (e.originalEvent.deltaY < 0) {
            zoomIn(1);
        } else {
            zoomOut(1);
        }
        $('html, body').stop().animate({}, 500, 'linear');
    }
});

function zoomIn(additionalScale, onCenter) {
    scale += scaleRate*scale*additionalScale;
    if (maxZoom !== null && scale > maxZoom) {
        scale = maxZoom;
    }
    scaleTiles(onCenter);
}

function zoomOut(additionalScale, onCenter) {
    scale -= scaleRate*scale*additionalScale;
    if (minZoom !== null && scale < minZoom) {
        scale = minZoom;
    }
    scaleTiles(onCenter);
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

function scaleTiles(onCenter) {

    gridTileWidth = isNaN(Number($("#gridWidth").val())) ? 20 : Number($("#gridWidth").val());
    gridTileHeight = isNaN(Number($("#gridHeight").val())) ? 20 : Number($("#gridHeight").val());
    var forestry = Number($("#forestry").val());
    var scaledGridCellWidth = 64 * scale;
    var scaledGridCellHeight = 64 * scale;
    var totalGridWidth = gridTileWidth * scaledGridCellWidth;
    var totalGridHeight = gridTileHeight * scaledGridCellHeight;

    prevGridWidth = grid.offsetWidth;
    prevGridHeight = grid.offsetHeight;
    currGridWidth = totalGridWidth;
    currGridHeight = totalGridHeight;
    diffGridWidth = totalGridWidth - prevGridWidth;
    diffGridHeight = totalGridWidth - prevGridHeight;
    grid.style.height = currGridHeight + "px";
    grid.style.width = currGridWidth + "px";

    originX = grid.offsetLeft + viewport.offsetLeft + viewportContainer.offsetLeft;
    originY = grid.offsetTop + viewport.offsetTop + viewportContainer.offsetTop + -$(window).scrollTop();

    if (!onCenter) {
        var userLocX = 0;
        var userLocY = 0;
        if (event) {
            if (event.clientX) {
                // Check for a mouse position
                userLocX = event.clientX;
                userLocY = event.clientY;
            } else if (event.changedTouches && event.changedTouches.length == 1) {
                // Check for double click on mobile
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
            } else if (event.changedTouches && event.changedTouches.length > 1) {
                // Check for zooming on mobile, find midpoint of pinch gesture
                offsetX1 = event.changedTouches[0].clientX;
                offsetY1 = event.changedTouches[0].clientY;
                offsetX2 = event.changedTouches[1].clientX;
                offsetY2 = event.changedTouches[1].clientY;

                userLocX = (offsetX2 + offsetX1) / 2;
                userLocY = (offsetY2 + offsetY1) / 2;
            } else {
                // Couldn't find mouse/finger position(s), last resort zoom to center of viewport
                userLocX = viewport.offsetWidth/2 + viewport.offsetLeft + viewportContainer.offsetLeft;
                userLocY = viewport.offsetHeight/2 + viewport.offsetTop + viewportContainer.offsetTop + - $(window).scrollTop();
            }
        }
    } else {
        // Zoom to center of viewport
        userLocX = viewport.offsetWidth/2 + viewport.offsetLeft + viewportContainer.offsetLeft;
        userLocY = viewport.offsetHeight/2 + viewport.offsetTop + viewportContainer.offsetTop + - $(window).scrollTop();
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
        if (scale > minZoom && scale < maxZoom) {
            reachedZoom = false;
        } else {
            diffX = 0;
            diffY = 0;
        }
    }
    else if (scale == minZoom || scale == maxZoom) {
        reachedZoom = true;
    }

    newX = grid.offsetLeft + diffX;
    newY = grid.offsetTop + diffY;

    if (!firstLoad) {
        if (newY < (viewport.offsetHeight * 1.5) && newY > (-1.5 * grid.offsetHeight)) {
            grid.style.top = newY + "px";
        }
        if (newX < (viewport.offsetWidth * 1.5) && newX > (-1.5 * grid.offsetWidth)) {
            grid.style.left = newX + "px";
        }
    } else {
        firstLoad = false;
    }

    // Please leave for debugging zoom
    //var c = document.getElementById("myCanvas");
    //var ctx = c.getContext("2d");
    //ctx.beginPath();
    //ctx.moveTo(originX, originY);
    //ctx.lineTo(newX, newY);
    //ctx.stroke();


    if (cursorObject != "") {
        var cursorTop = (cursorObject.yGridCoord * scaledGridCellHeight);
        var cursorLeft = (cursorObject.xGridCoord * scaledGridCellWidth);
        var scaledCursorHeight = cursorHeight * scale * .4;
        var scaledCursorWidth = cursorWidth * scale * .4;
        cursorObject.div.style.width = cursorWidth * scale * .4 + "px";
        cursorObject.div.style.height = cursorHeight * scale * .4 + "px";
        cursorObject.div.style.top = cursorTop + "px";
        cursorObject.div.style.left = cursorLeft + "px";
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

    var scaledImgSize = imgSize * scale;
    // Update all tiles
    for (var x = 0; x < gridTileWidth; x++) {
        for (var y = 0; y < gridTileHeight; y++) {

            // Update all grid cells, and background images
            var top = y * scaledGridCellWidth;
            var left = x * scaledGridCellWidth;

            gridCells[x][y].cellDiv.style.width = scaledGridCellWidth + "px";
            gridCells[x][y].cellDiv.style.height = scaledGridCellWidth + "px";
            gridCells[x][y].cellDiv.style.margin = (scaledGridCellWidth / 2) + "px";
            gridCells[x][y].cellDiv.style.top = top + "px";
            gridCells[x][y].cellDiv.style.left = left + "px";

            gridCells[x][y].backgroundDiv.style.width = scaledImgSize + "px";
            gridCells[x][y].backgroundDiv.style.height = scaledImgSize + "px";
            gridCells[x][y].backgroundDiv.style.top = top + "px";
            gridCells[x][y].backgroundDiv.style.left = left + "px";

            // Update all objects
            if (gridCells[x][y].objectKeys.length > 0) {
                for (var keyIndex = 0; keyIndex < gridCells[x][y].objectKeys.length; keyIndex++) {
                    var currKey = gridCells[x][y].objectKeys[keyIndex];
                    var top = gridObjects[currKey].yGridCoord * scaledGridCellWidth + scaledGridCellWidth / 2 - (gridObjects[currKey].yImageOrigin * scale) - (gridObjects[currKey].yGridCellOffset * scale);
                    var left = gridObjects[currKey].xGridCoord * scaledGridCellWidth + scaledGridCellWidth / 2 - (gridObjects[currKey].xImageOrigin * scale) - (gridObjects[currKey].xGridCellOffset * scale);

                    gridObjects[currKey].div.style.width = gridObjects[currKey].width * scale + "px";
                    gridObjects[currKey].div.style.height = gridObjects[currKey].height * scale + "px";
                    gridObjects[currKey].div.style.margin = (scaledGridCellWidth / 2) + "px";
                    gridObjects[currKey].div.style.top = top + "px";
                    gridObjects[currKey].div.style.left = left + "px";
                }
            }
        }
    }
}

function loadMap() {

    scaleRate = isNaN($("#zoom").val()) ? .3 : $("#zoom").val();
    dragDelta = ($("#dragDelta").val() == undefined) ? 10 : $("#dragDelta").val();
    gridTileWidth = isNaN(Number($("#gridWidth").val())) ? 20 : Number($("#gridWidth").val());
    gridTileHeight = isNaN(Number($("#gridHeight").val())) ? 20 : Number($("#gridHeight").val());
    var seed = isNaN(Number($("#seed").val())) ? 123456 : Number($("#seed").val());
    var forestry = isNaN(Number($("#forestry").val())) ? 2 : Number($("#forestry").val());
    scale = isNaN($("#zoom").val()) ? .3 : $("#zoom").val();

    var gridCellWidth = 64 * scale;

    var totalGridWidth = gridTileWidth * gridCellWidth;
    var totalGridHeight = gridTileHeight * gridCellWidth;
    var offsetX = viewportContainer.offsetWidth/2-(totalGridWidth/2);
    var offsetY = viewportContainer.offsetHeight/2-(totalGridHeight/2);
    grid.style.height = totalGridWidth + "px";
    grid.style.width = totalGridHeight + "px";
    grid.style.top = offsetY + "px";
    grid.style.left = offsetX + "px";

    viewportContainer.style.position = "relative";
    viewport.style.position = "absolute";
    grid.style.position = "relative";

    // Remove all current grid tiles
    jQuery('#cell-layer').html('');
    jQuery('#ground-layer').html('');
    jQuery('#object-layer').html('');

    $.ajax({
        url: "SandboxServlet",
        data:{width:gridTileWidth, height:gridTileHeight, seed:seed, forestry:forestry},
        type: 'POST',
        success: function(responseJson) {
            buildMap(responseJson);
        }
    });
    buildMenu();
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
            "<td>" + "Width: " + "</td><td>" + "<input type=\'text\' id=\'gridWidth\' value=20 /> " + "</td>" +
            "<td>" + "Height: " + "</td><td>" + "<input type='text' id='gridHeight' value=20 /> " + "</td>" +
            "</tr> <tr>" +
            "<td>" + "Zoom Rate: " + "</td><td>" + "<input type='text' id='zoom' value=.3 /> " + "</td>" +
            "<td>" + "Zoom Delta: " + "</td><td>" + "<input type='text' id='zoomDelta' value=.25 /> " + "</td>" +
            "</tr> <tr>" +
            "<td>" + "Drag Delta: " + "</td><td>" + "<input type='text' id='dragDelta' value=5 /> " + "</td>" +
            "<td>" + "Seed: " + "</td><td>" + "<input type='text' id='seed' value=123456 /> " + "</td>" +
            "</tr> <tr>" +
            "<td>" + "Forestry (0-10): " + "</td><td>" + "<input type='text' id='forestry' value=2 /> " + "</td>" +
            "</tr> <tr>" +
            "<td>" + "Grid Lines: <input type='checkbox' id='displayGridLines'> " + "</td>" +
            "<td>" + "Center on Selected: <input type='checkbox' id='keepSelectedCenter' checked> " + "</td>" +
            "</tr> <tr>" +
            "</tr> </table>" +
            "<center>" + "<button id='somebutton'>Update</button>" + "</center>" +
            "<br>";


        jQuery('#menu').html(htmlString);
        document.getElementById('menu').style.width = viewport.offsetWidth * (2 / 3) + "px";
        document.getElementById('menu').style.left = viewport.offsetWidth * (1 / 6) + "px";
        document.getElementById('menu').style.top = viewport.offsetHeight / 2 - menu.offsetHeight / 2 + "px";
        menuBuilt = true;3
    }
}

function buildMap(responseJson) {
    displayGridLines = (document.getElementById('displayGridLines') == null) ? false : document.getElementById('displayGridLines').checked;
    keepSelectedCenter = (document.getElementById('keepSelectedCenter') == null) ? true : document.getElementById('keepSelectedCenter').checked;
    var groundHtml = "";
    var cellHtml = "";
    var zOffset = 10;
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
            $hexBody += " background:url(" + $picUrlPath + backgroundObject.backgroundFile + ") center center;";
            $hexBody += " background-size:100%;";
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

        //var top = (gridObject.yGridCoord+1) * gridCellHeight - (gridObject.yGridCellOffset);
        var top = gridObject.height + (gridObject.yGridCoord * gridCellHeight + gridCellHeight / 2 - (gridObject.yImageOrigin) - (gridObject.yGridCellOffset));
        var left = (gridObject.xGridCoord+1) * gridCellWidth - (gridObject.xImageOrigin * scale) - (gridObject.xGridCellOffset * scale);

        var cgridObject = new GridObject(
            gridObject.key,
            "",
            gridObject.fileName,
            gridObject.name,
            gridObject.xGridCellOffset,
            gridObject.yGridCellOffset,
            gridObject.xGridCoord,
            gridObject.yGridCoord,
            gridObject.xImageOrigin,
            gridObject.yImageOrigin,
            gridObject.width,
            gridObject.height);
        var key = gridObject.fileName + ":" + gridObject.xGridCoord + "-" + gridObject.yGridCoord;
        gridObjects[key] = cgridObject;
        var gridCell = gridCells[gridObject.xGridCoord][gridObject.yGridCoord];
        gridCell.objectKeys[gridCell.objectKeys.length] = key;

        $hexBody = "<div id=\"object" + gridObject.xGridCoord + "_" + gridObject.yGridCoord + "\" " + "class=\"gridObject\"";
        $hexBody += " data-key=\"" + key + "\"";
        $hexBody += " style=\"";
        $hexBody += " z-index:" + (Number(zOffset) + Number(top)) + ";";
        if (gridObject.key == "o1") {
            $hexBody += " background:url(" + $domain + gridObject.fileName + ");";
        } else {
            $hexBody += " background:url(" + $picUrlPath + gridObject.fileName + ");";
        }
        $hexBody += " background-size:100%;";
        $hexBody += "\">";
        $hexBody += "</div>";
        htmlString += $hexBody;
    });

    // Append HTML
    $('#object-layer').append(htmlString);
    // Gather DOM elements
    var gridObjectElements = document.getElementsByClassName('gridObject');
    // Attach elements to gridObject structure
    for (var i = 0; i < gridObjectElements.length; i++) {
        gridObjects[objects[i].dataset.key].div = gridObjectElements[i];
    }
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
    if (previouslyHighlightedBackground != null) {
        xCoord = previouslyHighlightedBackground.xGridCoord;
        yCoord = previouslyHighlightedBackground.yGridCoord;
    } else if (previouslySelectedBackground != null) {
        xCoord = previouslySelectedBackground.xGridCoord;
        yCoord = previouslySelectedBackground.yGridCoord;
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
    scaledGridCellWidth = gridCellWidth * scale;
    scaledGridCellHeight = gridCellHeight * scale;
    xDist = xCoord * scaledGridCellWidth;
    yDist = yCoord * scaledGridCellHeight;
    if (grid.offsetLeft + xDist < 0) {
        panGrid(gridCellWidth, 0);
    } else if (grid.offsetLeft + xDist + scaledGridCellWidth > viewportContainer.offsetWidth) {
        panGrid(-gridCellWidth, 0);
    }
    else if (grid.offsetTop + yDist < 0) {
        panGrid(0, gridCellHeight);
    } else if (grid.offsetTop + yDist + scaledGridCellHeight > viewportContainer.offsetHeight) {
        panGrid(0, -gridCellHeight);
    }

}
function centerCellOnScreen(xCoord, yCoord) {
    // Move grid to center the currently selected cell
    scaledGridCellWidth = gridCellWidth * scale;
    scaledGridCellHeight = gridCellHeight * scale;
    xGrid = xCoord * scaledGridCellWidth + scaledGridCellWidth;
    yGrid = yCoord * scaledGridCellHeight + scaledGridCellHeight;
    xView = grid.offsetLeft + xGrid;
    yView = grid.offsetTop + yGrid;
    xGridOrigin = xView - viewportContainer.offsetWidth/2;
    yGridOrigin = yView - viewportContainer.offsetHeight/2;
    grid.style.left = (grid.offsetLeft - xGridOrigin) + "px" ;
    grid.style.top = (grid.offsetTop - yGridOrigin) + "px";
}
function centerGridOnScreen() {
    actualGridWidth = (gridCellWidth + (gridCellWidth * gridTileWidth));
    actualGridHeight = (gridCellHeight + (gridCellHeight * gridTileHeight));
    widthScale = viewport.offsetWidth / actualGridWidth;
    heightScale = viewport.offsetHeight / actualGridHeight;
    if (widthScale < heightScale) {
        scale = widthScale;
    } else {
        scale = heightScale;
    }
    scaleTiles(true);
    scaledGridWidth = (gridCellWidth * scale  + (gridCellWidth * scale * gridTileWidth));
    scaledGridHeight = (gridCellHeight * scale + (gridCellHeight * scale * gridTileHeight));
    grid.style.left = ((viewport.offsetWidth - scaledGridWidth)/2) + "px";
    grid.style.top = ((viewport.offsetHeight - scaledGridHeight)/2) + "px";
}
function keyUnpress() {
    if (!e) {
        var e = window.event;
    }
    if (e.keyCode == 32) {
        spacePressed = false;
    }
}
function getCenterCell() {
    scaledGridCellWidth = gridCellWidth * scale;
    scaledGridCellHeight = gridCellHeight * scale;
    xMid = Math.round((viewportContainer.offsetWidth/2 - viewportContainer.offsetLeft - viewport.offsetLeft - grid.offsetLeft - (scaledGridCellWidth/2)) / scaledGridCellWidth);
    yMid = Math.round((viewportContainer.offsetHeight/2 - viewportContainer.offsetTop - viewport.offsetTop - grid.offsetTop + $(window).scrollTop() - (scaledGridCellHeight/2))/ scaledGridCellHeight);
    if (xMid > gridTileWidth) {xMid = gridTileWidth}
    if (yMid > gridTileWidth) {yMid = gridTileHeight}
    if (xMid < 0) {xMid = 0}
    if (yMid < 0) {yMid = 0}
    return new CoordObject(xMid, yMid);
}
function keyPress() {
    if (!e) {
        var e = window.event;
    }
    if (usingKeys || !keepSelectedCenter) {
        currCoord = currentCoord();
    } else {
        currCoord = getCenterCell();
    }
    var isShift;
    if (window.event) {
        key = window.event.keyCode;
        isShift = !!window.event.shiftKey;
    } else {
        key = ev.which;
        isShift = !!ev.shiftKey;
    }
    panOffset = 20;
    switch(e.which) {
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
                if (previouslySelectedBackground != null) {
                    // Remove previously highlighted cell
                    removeHighlights(previouslyHighlightedBackground, previouslyHighlightedObjects, false);
                    centerCellOnScreen(previouslySelectedBackground.xGridCoord, previouslySelectedBackground.yGridCoord);
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
    e.preventDefault();
    if (isShift || spacePressed) {
        usingKeys = false;
    } else {
        usingKeys = true;
    }
}


$('#viewport').on('contextmenu', function(){
    return false;
});

function startHover(e) {
    usingKeys = false;
    if (dragging) {return}
    // determine event object
    if (!e) {
        var e = window.event;
    }
    if (!checkIfHoveringOverGrid()) {
        return;
    }
    var currCoord = getCoordOfMouse();
    if (currCoord.yGridCoord < 0 || currCoord.xGridCoord < 0 || currCoord.yGridCoord > (gridTileHeight-1) || currCoord.xGridCoord > (gridTileWidth-1)) {return}
    updateHighlights(previouslyHighlightedBackground, previouslyHighlightedObjects, currCoord.xGridCoord, currCoord.yGridCoord, false);
}

function startDrag(e) {
    dragging = true;
    // determine event object
    if (!e) {
        var e = window.event;
    }

    if (!checkIfHoveringOverViewport()) {
        return;
    }

    // Prevent normal panning
    e.preventDefault();
    drugged = false;
    // calculate event X, Y coordinates
    if (e) {
        // For mouse clicks
        if (e.clientX) {
            offsetX = e.clientX;
            offsetY = e.clientY;
            updateGrid();
            // For touch input
        } else if (event.touches) {
            if (event.touches.length == 1) {
                offsetX = e.touches[0].clientX;
                offsetY = e.touches[0].clientY;
                document.ontouchmove=dragDiv;
                updateGrid();
            } else {
                offsetX1 = e.touches[0].clientX;
                offsetY1 = e.touches[0].clientY;
                offsetX2 = e.touches[1].clientX;
                offsetY2 = e.touches[1].clientY;
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

function getCoordOfMouse() {
    var scaledGridCellWidth = 64 * scale;
    var scaledGridCellHeight = 64 * scale;
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
    var gridRelx = offsetX - viewportContainer.offsetLeft - viewport.offsetLeft - grid.offsetLeft - (scaledGridCellWidth / 2);
    var gridRely = offsetY - viewportContainer.offsetTop - viewport.offsetTop - grid.offsetTop + $(window).scrollTop() - (scaledGridCellHeight / 2);
    var gridColumn = Math.floor(gridRelx / scaledGridCellWidth);
    var gridRow = Math.floor(gridRely / scaledGridCellHeight);
    return new CoordObject(gridColumn, gridRow);
}
function updateCursor(gridRow, gridColumn) {
    var scaledGridCellWidth = 64 * scale;
    var scaledGridCellHeight = 64 * scale;
    var cursorTop = (gridRow * scaledGridCellHeight);
    var cursorLeft = (gridColumn * scaledGridCellWidth);
    var scaledCursorHeight = cursorHeight * scale * .4;
    var scaledCursorWidth = cursorWidth * scale * .4;
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

function clickMap() {
    event.preventDefault();
    if (checkDoubleClick()) {
        if (event.which == 3) {
            zoomOut(2, false);
        } else {
            zoomIn(2, false);
        }
    }
    var currCoord = getCoordOfMouse();
    if (currCoord.yGridCoord < 0 || currCoord.xGridCoord < 0 || currCoord.yGridCoord > (gridTileHeight-1) || currCoord.xGridCoord > (gridTileWidth-1)) {return}
    updateCursor(currCoord.yGridCoord, currCoord.xGridCoord);
    updateHighlights(previouslySelectedBackground, previouslySelectedObjects, currCoord.xGridCoord, currCoord.yGridCoord, true);
};

function removeHighlights(previouslyUpdatedBackground, previouslyUpdatedObjects, selection) {
    if (selection) {
        className = "gridSelected";
    } else {
        className = "highlighted";
    }
    // Remove highlights from previously selected divs
    if (previouslyUpdatedBackground != null) {
        previouslyUpdatedBackground.backgroundDiv.style.background = "url(" + $picUrlPath + previouslyUpdatedBackground.filename + ") center center / 100%";
        if (selection) {
            previouslyUpdatedBackground.backgroundDiv.className = previouslyUpdatedBackground.backgroundDiv.className.replace(/(?:^|\s)gridSelected(?!\S)/g, '');
        } else {
            previouslyUpdatedBackground.backgroundDiv.className = previouslyUpdatedBackground.backgroundDiv.className.replace(/(?:^|\s)highlighted(?!\S)/g, '');
        }
    }
    for (i=0; i<previouslyUpdatedObjects.length; i++) {
        if (previouslyUpdatedObjects[i].key == "o1") {
            previouslyUpdatedObjects[i].div.style.background = "url(" + $domain + previouslyUpdatedObjects[i].filename + ")";
        } else {
            previouslyUpdatedObjects[i].div.style.background = "url(" + $picUrlPath + previouslyUpdatedObjects[i].filename + ")";
        }
        previouslyUpdatedObjects[i].div.style.backgroundSize = "100%";
        previouslyUpdatedObjects[i].div.style.backgroundBlendMode = "";
        if (selection) {
            previouslyUpdatedObjects[i].div.className = previouslyUpdatedObjects[i].div.className.replace( /(?:^|\s)gridSelected(?!\S)/g , '' );
        } else {
            previouslyUpdatedObjects[i].div.className = previouslyUpdatedObjects[i].div.className.replace( /(?:^|\s)highlighted(?!\S)/g , '' );
        }
    }
    if (selection) {
        previouslySelectedBackground = null;
    } else {
        previouslyHighlightedBackground = null;
    }
    previouslyUpdatedObjects = [];
}

function updateHighlights(previouslyUpdatedBackground, previouslyUpdatedObjects, gridColumn, gridRow, selection) {
    // Remove previous highlights
    removeHighlights(previouslyUpdatedBackground, previouslyUpdatedObjects, selection);
    // Highlight the background div
    gridCells[gridColumn][gridRow].backgroundDiv.className += " " + className;
    previouslyUpdatedBackground  = gridCells[gridColumn][gridRow];
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
            previouslyUpdatedObjects[selectedIndex] = object;
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

    if (selection) {
        previouslySelectedBackground = previouslyUpdatedBackground;
    } else {
        previouslyHighlightedBackground = previouslyUpdatedBackground;
    }
}

function buildCursorHTML(cursorTop, cursorLeft, scaledCursorHeight, scaledCursorWidth, scaledGridCellHeight, scaledGridCellWidth) {
    htmlString = "<div id=\"cursorObject\"" + " class=\"cursorObject\"";
    htmlString += " style=\"";
    htmlString += " top:" + cursorTop + "px;";
    htmlString += " left:" + cursorLeft + "px;";
    htmlString += " width:" + cursorWidth * scale * .4 + "px;";
    htmlString += " height:" + cursorHeight * scale * .4 + "px;";
    htmlString += "\">";
    htmlString += "<div id=\"topLeftCursor\"" + " class=\"cursorSubObject\"";
    htmlString += " style=\"";
    htmlString += " z-index:" + 1000000 + ";";
    htmlString += " background:url(" + $picUrlPath + "selector1.png);";
    htmlString += " width:50%;";
    htmlString += " height:50%;";
    htmlString += " background-size:100%;";
    htmlString += " transform:scaleX(-1);";
    htmlString += "\">";
    htmlString += "</div>";
    htmlString += "<div id=\"topRightCursor\"" + " class=\"cursorSubObject\"";
    htmlString += " style=\"";
    htmlString += " z-index:" + 1000000 + ";";
    htmlString += " background:url(" + $picUrlPath + "selector1.png);";
    htmlString += " left:" + (scaledGridCellWidth + scaledCursorWidth/2) + "px;";
    htmlString += " width:50%;";
    htmlString += " height:50%;";
    htmlString += " background-size:100%;";
    htmlString += "\">";
    htmlString += "</div>";
    htmlString += "<div id=\"bottomRightCursor\"" + " class=\"cursorSubObject\"";
    htmlString += " style=\"";
    htmlString += " z-index:" + 1000000 + ";";
    htmlString += " background:url(" + $picUrlPath + "selector1.png);";
    htmlString += " top:" + (scaledGridCellHeight + scaledCursorHeight/2) + "px;";
    htmlString += " left:" + (scaledGridCellWidth + scaledCursorWidth/2) + "px;";
    htmlString += " width:50%;";
    htmlString += " height:50%;";
    htmlString += " background-size:100%;";
    htmlString += " transform:scaleX(-1);";
    htmlString += " transform:scaleY(-1);";
    htmlString += "\">";
    htmlString += "</div>";
    htmlString += "<div id=\"bottomLeftCursor\"" + " class=\"cursorSubObject\"";
    htmlString += " style=\"";
    htmlString += " z-index:" + 1000000 + ";";
    htmlString += " background:url(" + $picUrlPath + "selector1.png);";
    htmlString += " top:" + (scaledGridCellHeight + scaledCursorHeight/2) + "px;";
    htmlString += " width:50%;";
    htmlString += " height:50%;";
    htmlString += " background-size:100%;";
    htmlString += " transform:scale(-1, -1);";
    htmlString += "\">";
    htmlString += "</div>";
    htmlString += "</div>";
    return htmlString;
}
function updateGrid() {
    if(!grid.style.left) { grid.style.left='0px'};
    if (!grid.style.top) { grid.style.top='0px'};

    // calculate integer values for top and left 
    // properties
    coordX = parseInt(grid.style.left);
    coordY = parseInt(grid.style.top);
    drag = true;

    // move div element
    document.onmousemove=dragDiv;
}
function dragDiv(e) {
    if (!drag) {return};
    if (!e) { var e= window.event};
    // move div element
    if (e) {
        if (e.clientX) {
            userLocX = e.clientX;
            userLocY = e.clientY;
        } else if (event.touches) {
            userLocX = e.touches[0].clientX;
            userLocY = e.touches[0].clientY;
        }
    }
    //dragDelta = isNaN($("#dragDelta").val()) ? 10 : $("#dragDelta").val();$("#dragDelta").val();
    deltaX = userLocX - offsetX;
    deltaY = userLocY - offsetY;
    updatedX = coordX+deltaX;
    updatedY = coordY+deltaY;
    if (Math.abs(deltaX) > dragDelta || Math.abs(deltaY) > dragDelta) {
        if (updatedX < viewport.offsetWidth && ((updatedX > coordX) || updatedX > (-1 * grid.offsetWidth))) {
            grid.style.left = coordX + userLocX - offsetX + 'px';
        }
        if (updatedY < viewport.offsetHeight && ((updatedY > coordY) || (updatedY > (-1 * grid.offsetHeight)))) {
            grid.style.top = coordY + userLocY - offsetY + 'px';
        }
        $('html, body').stop().animate({}, 500, 'linear');
        drugged = true;
    }
    return false;
}

function checkIfHoveringOverViewport() {
    var targ = event.target ? event.target : event.srcElement;
    if (targ.className != 'gridBackground' &&
        targ.className != 'grid' &&
        targ.className != 'gridCell' &&
        targ.className != 'vp' &&
        targ.className != 'vpcontainer' &&
        targ.className != 'gridObject' &&
        targ.className != 'gridLayer' &&
        targ.className != 'highlighted' &&
        targ.className != 'gridObject highlighted' &&
        targ.className != 'cursorObject' &&
        targ.className != 'cursorSubObject' &&
        targ.className != 'objectLayer') {return false};
    return true;
}
function checkIfHoveringOverGrid() {
    var targ = event.target ? event.target : event.srcElement;
    if (targ.className != 'gridBackground' &&
        targ.className != 'gridBackground gridSelected' &&
        targ.className != 'gridBackground highlighted' &&
        targ.className != 'grid' &&
        targ.className != 'gridCell' &&
        targ.className != 'gridLayer' &&
        targ.className != 'highlighted' &&
        targ.className != 'gridObject' &&
        targ.className != 'gridObject highlighted' &&
        targ.className != 'gridObject gridSelected' &&
        targ.className != 'cursorObject' &&
        targ.className != 'cursorSubObject' &&
        targ.className != 'objectLayer') {return false};
    return true;
}
function stopDrag() {
    dragging = false;
    document.onmousemove=startHover;
    if (!drugged && checkIfHoveringOverGrid()) {
        // User clicked on map without dragging
        clickMap();
    }
    drag=false;
}

function zoomDiv(e) {
    if (!zoom) {return};
    if (!e) { var e= window.event};
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
        if (d1 < d2 && delta > zoomDelta) {
            zoomIn(1, false);
        } else {
            zoomOut(1, false);
        }
    }
    $('html, body').stop().animate({}, 500, 'linear');
    return false;
}

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

function GridObject(key, div, filename, name, xGridCellOffset, yGridCellOffset, xGridCoord, yGridCoord, xImageOrigin, yImageOrigin, width, height) {
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
    this.width = width;
    this.height = height;
};

function CoordObject(xGridCoord, yGridCoord) {
    this.xGridCoord = xGridCoord;
    this.yGridCoord = yGridCoord;
}

function mapPlow(coord) {
    $.ajax({
        url: "SandboxServlet",
        data: {width: gridTileWidth, height: gridTileHeight, seed: $("#seed").val(), forestry: forestry, command: "mapPlow", coord: currentCoord()},
        type: 'POST',
        success: function (responseJson) {
            buildMap(responseJson);
        }
    });
}