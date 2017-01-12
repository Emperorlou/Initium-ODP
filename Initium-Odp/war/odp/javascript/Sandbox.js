var viewportContainer = document.getElementById("viewportcontainer");
var viewport = document.getElementById("viewport");
var grid = document.getElementById("grid");
var objects = document.getElementsByClassName('gridObject');
var scaleRate = Number($("#zoom").val());
var hexEdge = Number($("#hexEdge").val());
var scale = 1;
var maxZoom = 2.4;
var minZoom = .005;
var imgSize = 128;
var treeWidth = 192;
var treeHeight = 256;
var reachedZoom = false;
var gridWidth = Number($("#hexEdge").val());
var gridHeight = gridWidth;
var $picUrlPath = "https://initium-resources.appspot.com/images/newCombat/";

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


window.onload = function() {
    document.onmousedown = startDrag;
    document.onmouseup = stopDrag;
    //document.ontouchend = stopDrag;
    //document.ontouchstart = startDrag;
    document.body.addEventListener('touchend', stopDrag);
    document.body.addEventListener('touchmove', touchMoveDiv);
    document.body.addEventListener('touchstart', function (e) {

        if (e.touches.length == 1) {
            var targ = e.target ? e.target : e.srcElement;
            if (targ.className != 'gridBackground' && targ.className != 'grid' && targ.className != 'vp' && targ.className !=  'vpcontainer') {return};
            e.preventDefault();

            // calculate event X, Y coordinates
            offsetX = e.touches[0].clientX;
            offsetY = e.touches[0].clientY;

            if(!grid.style.left) { grid.style.left='0px'};
            if (!grid.style.top) { grid.style.top='0px'};
            coordX = parseInt(grid.style.left);
            coordY = parseInt(grid.style.top);

            if(!grid.style.left) { grid.style.left='0px'};
            if (!grid.style.top) { grid.style.top='0px'};

            drag = true;
            document.ontouchmove=dragDiv;
        } else if (e.touches.length == 2) { // If two fingers are touching
            var targ = e.target ? e.target : e.srcElement;
            if (targ.className != 'gridBackground' && targ.className != 'grid' && targ.className != 'vp' && targ.className !=  'vpcontainer') {return};
            e.preventDefault();

            // calculate event X, Y coordinates
            offsetX1 = e.touches[0].clientX;
            offsetY1 = e.touches[0].clientY;
            offsetX2 = e.touches[1].clientX;
            offsetY2 = e.touches[1].clientY;

            zoom = true;
            document.ontouchmove=zoomDiv;
        }
    });
}

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
    scaleRate = Number($("#zoom").val());
    loadMap();
}

function scaleTiles() {

    var gridTileWidth = Number($("#hexEdge").val());
    var forestry = Number($("#forestry").val());
    var gridCellWidth = 64 * scale;
    var totalGridWidth = gridTileWidth * gridCellWidth;


    prevGridWidth = grid.offsetWidth;
    prevGridHeight = grid.offsetHeight;
    currGridWidth = totalGridWidth;
    currGridHeight = totalGridWidth;
    diffGridWidth = totalGridWidth - prevGridWidth;
    diffGridHeight = totalGridWidth - prevGridHeight;
    grid.style.height = currGridHeight + "px";
    grid.style.width = currGridWidth + "px";

    originX = grid.offsetLeft + viewport.offsetLeft + viewportContainer.offsetLeft;
    originY = grid.offsetTop + viewport.offsetTop + viewportContainer.offsetTop - $(window).scrollTop();
    dx = Math.abs(event.clientX - originX);
    dy = Math.abs(event.clientY - originY);
    widthRatio = currGridWidth / prevGridWidth;
    heightRatio = currGridHeight / prevGridHeight;

    newDx = dx * widthRatio;
    newDy = dy * heightRatio;
    diffX = Math.abs(newDx - dx);
    diffY = Math.abs(newDy - dy);

    if (event.clientY < originY && diffGridWidth < 0 || event.clientY > originY && diffGridWidth > 0) {
        diffY = diffY * -1;
    }
    if (event.clientX < originX && diffGridWidth < 0 || event.clientX > originX && diffGridWidth > 0) {
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

    grid.style.top = newY + "px";
    grid.style.left = newX + "px";

    // Please leave for debugging zoom
    //var c = document.getElementById("myCanvas");
    //var ctx = c.getContext("2d");
    //ctx.beginPath();
    //ctx.moveTo(originX, originY);
    //ctx.lineTo(newX, newY);
    //ctx.stroke();

    // Update all tiles
    for (var y = 0; y < gridWidth; y++) {
        for (var x = 0; x < gridHeight; x++) {

            // Update all grid cells, and background images
            var scaledImgSize = imgSize * scale;
            var top = gridCells[x][y].yCoord * gridCellWidth;
            var left = gridCells[x][y].xCoord * gridCellWidth;

            gridCells[x][y].cellDiv.style.width = gridCellWidth + "px";
            gridCells[x][y].cellDiv.style.height = gridCellWidth + "px";
            gridCells[x][y].cellDiv.style.margin = (gridCellWidth / 2) + "px";
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
                    var top = gridObjects[currKey].yCoord * gridCellWidth + gridCellWidth / 2 - (gridObjects[currKey].yAttach * scale) - (gridObjects[currKey].yOffset * scale);
                    var left = gridObjects[currKey].xCoord * gridCellWidth + gridCellWidth / 2 - (gridObjects[currKey].xAttach * scale) - (gridObjects[currKey].xOffset * scale);

                    gridObjects[currKey].div.style.width = treeWidth * scale + "px";
                    gridObjects[currKey].div.style.height = treeHeight * scale + "px";
                    gridObjects[currKey].div.style.margin = (gridCellWidth / 2) + "px";
                    gridObjects[currKey].div.style.top = top + "px";
                    gridObjects[currKey].div.style.left = left + "px";
                }
            }
        }
    }
}

function loadMap() {

    var displayGridLines = document.getElementById('displayGridLines').checked;
    var gridTileWidth = Number($("#hexEdge").val());
    var forestry = Number($("#forestry").val());
    var imgSize = 128 * scale;
    var gridCellWidth = 64 * scale;
    var totalGridWidth = gridTileWidth * gridCellWidth;
    var offsetX = viewportContainer.offsetWidth/2-(totalGridWidth/2);
    var offsetY = viewportContainer.offsetHeight/2-(totalGridWidth/2);
    var groundHtml = "";
    var cellHtml = "";
    var zOffset = 10;

    grid.style.height = totalGridWidth + "px";
    grid.style.width = totalGridWidth + "px";
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
        data:{width:hexEdge, seed:$("#seed").val(), forestry:forestry},
        type: 'POST',
        success: function(responseJson) {
            $.each(responseJson['backgroundTiles'], function (index, value) {
                $.each(value, function (innerIndex, backgroundObject) {

                    var gridCell = new GridCell(
                        "",
                        "",
                        backgroundObject.zIndex,
                        innerIndex,
                        index,
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
                    $hexBody += " background-image:url(" + $picUrlPath + backgroundObject.backgroundFile + ");";
                    $hexBody += "\">";
                    $hexBody += "</div>";
                    cellHtml += $hexBody;

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

                    groundHtml += $hexBody;
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

                var top = (gridObject.yCoord+1) * treeHeight - (gridObject.yAttach) - (gridObject.yOffset);
                var left = (gridObject.xCoord+1) * gridCellWidth - (gridObject.xAttach * scale) - (gridObject.xOffset * scale);

                var cgridObject = new GridObject(
                    "",
                    gridObject.xOffset,
                    gridObject.yOffset,
                    gridObject.xCoord,
                    gridObject.yCoord,
                    gridObject.xAttach,
                    gridObject.yAttach,
                    gridObject.width,
                    gridObject.height);
                var key = gridObject.xCoord + "-" + gridObject.yCoord;
                gridObjects[key] = cgridObject;
                var gridCell = gridCells[gridObject.xCoord][gridObject.yCoord];
                gridCell.objectKeys[gridCell.objectKeys.length] = key;

                $hexBody = "<div id=\"object" + gridObject.xCoord + "_" + gridObject.yCoord + "\" " + "class=\"gridObject\"";
                $hexBody += " data-key=\"" + key + "\"";
                $hexBody += " style=\"";
                $hexBody += " z-index:" + zOffset + (Number(top)) + ";";
                $hexBody += " background-image:url(" + $picUrlPath + gridObject.fileName + ");";
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

function startDrag(e) {
    // determine event object
    if (!e) {
        var e = window.event;
    }
    // IE uses srcElement, others use target
    var targ = e.target ? e.target : e.srcElement;

    if (targ.className != 'gridBackground' &&
        targ.className != 'grid' &&
        targ.className != 'gridCell' &&
        targ.className != 'vp' &&
        targ.className != 'vpcontainer' &&
        targ.className != 'gridObject' &&
        targ.className != 'gridLayer' &&
        targ.className != 'objectLayer') {return};
    // calculate event X, Y coordinates
    offsetX = e.clientX;
    offsetY = e.clientY;

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
    grid.style.left=coordX+e.clientX-offsetX+'px';
    grid.style.top=coordY+e.clientY-offsetY+'px';
    $('html, body').stop().animate({}, 500, 'linear');
    return false;
}
function touchMoveDiv(e) {
    if (!drag) {return};
    if (!e) { var e= window.event};
    // move div element
    grid.style.left=coordX+e.touches[0].clientX-offsetX+'px';
    grid.style.top=coordY+e.touches[0].clientY-offsetY+'px';
    return false;
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
        zoomIn();
    } else {
        zoomOut();
    }
    scaleTiles();
    $('html, body').stop().animate({}, 500, 'linear');
    return false;
}
function stopDrag() {
    drag=false;
}

function GridCell(backgroundDiv, cellDiv, zindex, xCoord, yCoord, objectKeys) {
    this.backgroundDiv = backgroundDiv;
    this.cellDiv = cellDiv;
    this.zIndex = zindex;
    this.xCoord = xCoord;
    this.yCoord = yCoord;
    this.objectKeys = objectKeys;
}

function GridObject(div, xOffset, yOffset, xCoord, yCoord, xAttach, yAttach, width, height) {
    this.div = div;
    this.xOffset = xOffset;
    this.yOffset = yOffset;
    this.xCoord = xCoord;
    this.yCoord = yCoord;
    this.xAttach = xAttach;
    this.yAttach = yAttach;
    this.width = width;
    this.height = height;
};