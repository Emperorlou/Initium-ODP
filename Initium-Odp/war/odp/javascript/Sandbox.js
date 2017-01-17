var viewportContainer = document.getElementById("viewportcontainer");
var viewport = document.getElementById("viewport");
var grid = document.getElementById("grid");
var gridCellLayer = document.getElementById("cell-layer");
var objects = document.getElementsByClassName('gridObject');
var scaleRate = Number($("#zoom").val());
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
var cursorObject = "";

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
            zoomOut();
        } else {
            zoomIn();
        }
        scaleTiles();
        $('html, body').stop().animate({}, 500, 'linear');
    }
});

function zoomOut() {
    scale += scaleRate*scale;
    if (maxZoom !== null && scale > maxZoom) {
        scale = maxZoom;
    }
}

function zoomIn() {
    scale -= scaleRate*scale;
    if (minZoom !== null && scale < minZoom) {
        scale = minZoom;
    }
}

function pressedButton() {
    firstLoad = true;
    scaleRate = Number($("#zoom").val());
    loadMap();
}

function scaleTiles() {

    var gridTileWidth = Number($("#gridWidth").val());
    var gridTileHeight = Number($("#gridHeight").val());
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
    originY = grid.offsetTop + viewport.offsetTop + viewportContainer.offsetTop + gridCellLayer.offsetTop - $(window).scrollTop();

    var userLocX = 0;
    var userLocY = 0;
    if (event) {
        if (event.clientX) {
            userLocX = event.clientX;
            userLocY = event.clientY;
        } else if (event.touches) {
            offsetX1 = e.touches[0].clientX;
            offsetY1 = e.touches[0].clientY;
            offsetX2 = e.touches[1].clientX;
            offsetY2 = e.touches[1].clientY;

            userLocX = (offsetX2 + offsetX1) / 2;
            userLocY = (offsetY2 + offsetY1) / 2;
        }
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
        var cursorTop = scaledGridCellHeight/2 + (cursorObject.yGridCoord * scaledGridCellHeight);
        var cursorLeft = scaledGridCellWidth/2 + (cursorObject.xGridCoord * scaledGridCellWidth);
        cursorObject.div.style.width = cursorWidth * scale * .4 + "px";
        cursorObject.div.style.height = cursorHeight * scale * .4 + "px";
        cursorObject.div.style.top = cursorTop + "px";
        cursorObject.div.style.left = cursorLeft + "px";
        cursorSubObjects = cursorObject.div.children;
        for (i=0; i<cursorSubObjects.length; i++) {
            if (cursorSubObjects[i].style.top != "") {
                cursorSubObjects[i].style.top = scaledGridCellHeight/2 + "px";
            }
            if (cursorSubObjects[i].style.left != "") {
                cursorSubObjects[i].style.left = scaledGridCellWidth/2 + "px";
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

    var displayGridLines = document.getElementById('displayGridLines').checked;
    var gridTileWidth = Number($("#gridWidth").val());
    var gridTileHeight = Number($("#gridHeight").val());
    var forestry = Number($("#forestry").val());
    var gridCellWidth = 64 * scale;
    var groundHtml = "";
    var cellHtml = "";
    var zOffset = 10;

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
        data:{width:gridTileWidth, height:gridTileHeight, seed:$("#seed").val(), forestry:forestry},
        type: 'POST',
        success: function(responseJson) {
            $.each(responseJson['backgroundTiles'], function (index, value) {
                $.each(value, function (innerIndex, backgroundObject) {

                    var gridCell = new GridCell(
                        "",
                        "",
                        backgroundObject.backgroundFile,
                        backgroundObject.zIndex,
                        []
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
            // Update scale/zoom of all elements
            scaleTiles();
        }
    });
}

window.onload = function() {

    // Listen for mouse input
    document.onmousedown = startDrag;
    document.onmouseup = stopDrag;

    // Listen for touch inputs
    document.body.addEventListener('touchend', stopDrag);
    document.body.addEventListener('touchmove', dragDiv);
    document.body.addEventListener('touchstart', startDrag);
}

function startDrag(e) {
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
function clickMap() {
    var scaledGridCellWidth = 64 * scale;
    var scaledGridCellHeight = 64 * scale;
    // For mouse clicks
    if (event.clientX) {
        offsetX = event.clientX;
        offsetY = event.clientY;
        // For touch input
    } else if (event.touches) {
        offsetX = event.touches[0].clientX;
        offsetY = event.touches[0].clientY;
    }
    // Determine where the click took place in the grid
    var gridRelx = offsetX - viewportContainer.offsetLeft - viewport.offsetLeft - grid.offsetLeft - (scaledGridCellWidth / 2);
    var gridRely = offsetY - viewportContainer.offsetTop - viewport.offsetTop - grid.offsetTop + $(window).scrollTop() - (scaledGridCellHeight / 2);
    var gridColumn = Math.floor(gridRelx / scaledGridCellWidth);
    var gridRow = Math.floor(gridRely / scaledGridCellHeight);

    if (gridRow < 0 || gridColumn < 0) {return}
    var cursorTop = scaledGridCellHeight/2 + (gridRow * scaledGridCellHeight);
    var cursorLeft = scaledGridCellWidth/2 + (gridColumn * scaledGridCellWidth);
    if (cursorObject == "") {
        // First select, build out cursor object
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
        htmlString += " filter:drop-shadow(0px 0px 3px #000000);";
        htmlString += "\">";
        htmlString += "</div>";
        htmlString += "<div id=\"topRightCursor\"" + " class=\"cursorSubObject\"";
        htmlString += " style=\"";
        htmlString += " z-index:" + 1000000 + ";";
        htmlString += " background:url(" + $picUrlPath + "selector1.png);";
        htmlString += " left:" + scaledGridCellWidth/2 + "px;";
        htmlString += " width:50%;";
        htmlString += " height:50%;";
        htmlString += " background-size:100%;";
        htmlString += " filter:drop-shadow(0px 0px 3px #000000);";
        htmlString += "\">";
        htmlString += "</div>";
        htmlString += "<div id=\"bottomRightCursor\"" + " class=\"cursorSubObject\"";
        htmlString += " style=\"";
        htmlString += " z-index:" + 1000000 + ";";
        htmlString += " background:url(" + $picUrlPath + "selector1.png);";
        htmlString += " top:" + scaledGridCellHeight/2 + "px;";
        htmlString += " left:" + scaledGridCellWidth/2 + "px;";
        htmlString += " width:50%;";
        htmlString += " height:50%;";
        htmlString += " background-size:100%;";
        htmlString += " transform:scaleX(-1);";
        htmlString += " transform:scaleY(-1);";
        htmlString += " filter:drop-shadow(0px 0px 3px #000000);";
        htmlString += "\">";
        htmlString += "</div>";
        htmlString += "<div id=\"bottomLeftCursor\"" + " class=\"cursorSubObject\"";
        htmlString += " style=\"";
        htmlString += " z-index:" + 1000000 + ";";
        htmlString += " background:url(" + $picUrlPath + "selector1.png);";
        htmlString += " top:" + scaledGridCellHeight/2 + "px;";
        htmlString += " width:50%;";
        htmlString += " height:50%;";
        htmlString += " background-size:100%;";
        htmlString += " transform:scale(-1, -1);";
        htmlString += " filter:drop-shadow(0px 0px 3px #000000);";
        htmlString += "\">";
        htmlString += "</div>";
        htmlString += "</div>";
        $('#ui-layer').append(htmlString);
        cursorObject = new CursorObject(document.getElementById('cursorObject'), gridColumn, gridRow);
    } else {
        // Only need to update cursor object
        cursorObject.div.style.top = cursorTop + "px";
        cursorObject.div.style.left = cursorLeft + "px";
        cursorObject.xGridCoord = gridColumn;
        cursorObject.yGridCoord = gridRow;
    }

    // Remove highlights from previously selected divs
    if (previouslySelectedBackground != null) {
        previouslySelectedBackground.backgroundDiv.style.background = "url(" + $picUrlPath + previouslySelectedBackground.filename + ") center center / 100%";
        previouslySelectedBackground.backgroundDiv.className = previouslySelectedBackground.backgroundDiv.className.replace( /(?:^|\s)highlighted(?!\S)/g , '' );
    }
    for (i=0; i<previouslySelectedObjects.length; i++) {
        if (previouslySelectedObjects[i].key == "o1") {
            previouslySelectedObjects[i].div.style.background = "url(" + $domain + previouslySelectedObjects[i].filename + ")";
        } else {
            previouslySelectedObjects[i].div.style.background = "url(" + $picUrlPath + previouslySelectedObjects[i].filename + ")";
        }
        previouslySelectedObjects[i].div.style.backgroundSize = "100%";
        previouslySelectedObjects[i].div.style.backgroundBlendMode = "";
        previouslySelectedObjects[i].div.className = previouslySelectedObjects[i].div.className.replace( /(?:^|\s)highlighted(?!\S)/g , '' );

    }
    // Highlight the background div
    gridCells[gridColumn][gridRow].backgroundDiv.style.background = gridCells[gridColumn][gridRow].backgroundDiv.style.background;
    gridCells[gridColumn][gridRow].backgroundDiv.className += " highlighted";
    previouslySelectedBackground  = gridCells[gridColumn][gridRow];
    // If we have an object for the user, highlight it
    if (gridCells[gridColumn][gridRow].objectKeys.length > 0) {
        tmpString = "";
        objectKeys = gridCells[gridColumn][gridRow].objectKeys;
        for (selectedIndex=0; selectedIndex<objectKeys.length; selectedIndex++){
            object = gridObjects[objectKeys[selectedIndex]];
            // Update the selected object list
            tmpString += "<br>" + object.name + "<br/>";
            // Highlight the objects in the viewport
            object.div.className += " highlighted";
            //object.div.style.backgroundBlendMode = "normal, overlay";
            // Add div to previouslySelected to remove highlight on later click
            previouslySelectedObjects[selectedIndex] = object;
        }
        // Update object list
        $("#selectedObjects").html(tmpString);
    } else {
        $("#selectedObjects").html('<br> No objects at this coordinate. </br>');
    }
};
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
    updatedX = coordX+userLocX-offsetX;
    updatedY = coordY+userLocY-offsetY;
    if (updatedX < viewport.offsetWidth && ((updatedX > coordX) || updatedX > (-1 * grid.offsetWidth))) {
        grid.style.left = coordX + userLocX - offsetX + 'px';
    }
    if (updatedY < viewport.offsetHeight && ((updatedY > coordY) || (updatedY > (-1 * grid.offsetHeight)))) {
        grid.style.top = coordY + userLocY - offsetY + 'px';
    }
    $('html, body').stop().animate({}, 500, 'linear');
    drugged = true;
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
        targ.className != 'objectLayer') {return false};
    return true;
}
function checkIfHoveringOverGrid() {
    var targ = event.target ? event.target : event.srcElement;
    if (targ.className != 'gridBackground' &&
        targ.className != 'grid' &&
        targ.className != 'gridCell' &&
        targ.className != 'gridObject' &&
        targ.className != 'gridLayer' &&
        targ.className != 'highlighted' &&
        targ.className != 'gridObject highlighted' &&
        targ.className != 'objectLayer') {return false};
    return true;
}
function stopDrag() {
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
    coffsetX1 = e.touches[0].clientX;
    coffsetY1 = e.touches[0].clientY;
    coffsetX2 = e.touches[1].clientX;
    coffsetY2 = e.touches[1].clientY;

    d1 = Math.sqrt( Math.pow((offsetX2 - offsetX1),2) + Math.pow((offsetY2 - offsetY1),2));
    d2 = Math.sqrt( Math.pow((coffsetX2 - coffsetX1),2) + Math.pow((coffsetY2 - coffsetY1),2));
    if (d1 < d2) {
        zoomOut();
    } else {
        zoomIn();
    }
    scaleTiles();
    $('html, body').stop().animate({}, 500, 'linear');
    return false;
}

function CursorObject(div, xGridCoord, yGridCoord) {
    this.div = div;
    this.xGridCoord = xGridCoord;
    this.yGridCoord = yGridCoord;
}

function GridCell(backgroundDiv, cellDiv, filename, zindex, objectKeys) {
    this.backgroundDiv = backgroundDiv;
    this.cellDiv = cellDiv;
    this.filename = filename;
    this.zIndex = zindex;
    this.objectKeys = objectKeys;
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