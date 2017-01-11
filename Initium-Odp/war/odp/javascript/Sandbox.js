var scale = 1;
var viewportContainer = document.getElementById("viewportcontainer");
var viewport = document.getElementById("viewport");
//var groundLayer = document.getElementById("ground-layer");
var grid = document.getElementById("grid");
var hexTiles = document.getElementsByClassName('gridCell');
var objects = document.getElementsByClassName('gridObject');
var maxZoom = 2.4;
var minZoom = .05;
var scaleRate = Number($("#zoom").val());
var $picUrlPath = "https://initium-resources.appspot.com/images/newCombat/";
var hexEdge = Number($("#hexEdge").val());
var reachedZoom = false;
var imgSize = 128;
var treeWidth = 192;
var treeHeight = 256;

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
            scale += scaleRate;
            if (maxZoom !== null && scale > maxZoom) {
                scale = maxZoom;
            }
        } else {
            scale -= scaleRate;
            if (minZoom !== null && scale < minZoom) {
                scale = minZoom;
            }
        }
        scaleTiles();
        $('html, body').stop().animate({}, 500, 'linear');
    }
});

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
    var l = hexTiles.length;
    for (var index = 0; index < l; index++) {

        var top = hexTiles[index].dataset.ycoord * gridCellWidth;
        var left = hexTiles[index].dataset.xcoord * gridCellWidth;

        hexTiles[index].style.width = gridCellWidth + "px";
        hexTiles[index].style.height = gridCellWidth + "px";
        hexTiles[index].style.margin = (gridCellWidth / 2) + "px";
        hexTiles[index].style.top = top + "px";
        hexTiles[index].style.left = left + "px";

        //var child = hexTiles[index].children[0];
        for (var i = 0, len = hexTiles[index].children.length; i < len; i++) {
            scaleChildTiles(hexTiles[index].children[i]);
        }
    }

    // Update all objects
    var l = objects.length;
    for (var index = 0; index < l; index++) {

        var top = objects[index].dataset.ycoord * gridCellWidth - (objects[index].dataset.yattach * scale);
        var left = objects[index].dataset.xcoord * gridCellWidth - (objects[index].dataset.xattach * scale);

        objects[index].style.width = treeWidth * scale + "px";
        objects[index].style.height = treeHeight * scale + "px";
        objects[index].style.margin = (gridCellWidth / 2) + "px";
        objects[index].style.top = top + "px";
        objects[index].style.left = left + "px";
    }
}

function scaleChildTiles(child) {
    var scaledImgSize = imgSize * scale;
    child.style.width = scaledImgSize + 'px';
    child.style.height = scaledImgSize + 'px';
    child.style.top = scaledImgSize / -2 + 'px';
    child.style.left = scaledImgSize / -2 + 'px';
}

function loadMap() {

    var gridTileWidth = Number($("#hexEdge").val());
    var forestry = Number($("#forestry").val());
    var imgSize = 128 * scale;
    var gridCellWidth = 64 * scale;
    var totalGridWidth = gridTileWidth * gridCellWidth;
    var offsetX = viewportContainer.offsetWidth/2-(totalGridWidth/2);
    var offsetY = totalGridWidth/2;
    var htmlString = "";
    var zOffset = 10;

    grid.style.height = totalGridWidth + "px";
    grid.style.width = totalGridWidth + "px";
    grid.style.top = offsetY + "px";
    grid.style.left = offsetX + "px";

    viewportContainer.style.position = "relative";
    viewport.style.position = "absolute";
    grid.style.position = "relative";

    // Remove all current tiles
    var l = hexTiles.length;
    for (var i = 0; i < l; i++) {
        hexTiles[0].parentNode.removeChild(hexTiles[0]);
    }
    var l = objects.length;
    for (var i = 0; i < l; i++) {
        objects[0].parentNode.removeChild(objects[0]);
    }

    $.ajax({
        url: "SandboxServlet",
        data:{width:hexEdge, seed:$("#seed").val(), forestry:forestry},
        type: 'POST',
        success: function(responseJson) {
            $.each(responseJson['backgroundTiles'], function (index, value) {
                $.each(value, function (innerIndex, innerValue) {

                    var top = (gridCellWidth * index);
                    var left = (gridCellWidth * innerIndex);

                    $hexBody = "<div";
                    $hexBody += " id=\"hex" + index + "_" + innerIndex + "\"";
                    $hexBody += " class=\"gridCell\"";
                    $hexBody += " data-xcoord=\"" + innerIndex + "\"";
                    $hexBody += " data-ycoord=\"" + index + "\"";
                    $hexBody += " style=\"";
                    $hexBody += "width: " + gridCellWidth + 'px' + ";";
                    $hexBody += " height:" + gridCellWidth + 'px' + ";";
                    $hexBody += " top:" + top + 'px' + ";";
                    $hexBody += " left:" +  left + 'px' + ";";
                    $hexBody += "\">";

                    $hexBody += "<div id=\"hex" + i + "Back\" " + "class=\"gridBackground\"";
                    $hexBody += " style=\"";
                    $hexBody += "width: " + imgSize + 'px' + ";";
                    $hexBody += " height:" + imgSize + 'px' + ";";
                    $hexBody += " top:" + imgSize/ -2 + 'px' + ";";
                    $hexBody += " left:" +  imgSize / -2 + 'px' + ";";
                    $hexBody += " z-index:" + innerValue.zIndex + ";";
                    $hexBody += " background-image:url(" + $picUrlPath + innerValue.backgroundFile + ");";
                    $hexBody += "\">";
                    $hexBody += "</div>";
                    $hexBody += "</div>";

                    htmlString += $hexBody;
                });
            });

            $('#ground-layer').append(htmlString);

            htmlString = "";
            $.each(responseJson['objectMap'], function (objectKey, gridObject) {

                var top = (gridCellWidth * gridObject.xCoord);
                var left = (gridCellWidth * gridObject.yCoord);

                $hexBody = "<div id=\"object" + i + "_" + "\" " + "class=\"gridObject\"";
                $hexBody += " data-xcoord=\"" + gridObject.xCoord + "\"";
                $hexBody += " data-ycoord=\"" + gridObject.yCoord + "\"";
                $hexBody += " data-xattach=\"" + gridObject.xAttach + "\"";
                $hexBody += " data-yattach=\"" + gridObject.yAttach + "\"";
                $hexBody += " style=\"";
                $hexBody += " top:" + top - gridObject.xAttach + 'px' + ";";
                $hexBody += " left:" +  left + gridObject.xAttach + 'px' + ";";
                $hexBody += " z-index:" + zOffset + gridObject.yCoord + ";";
                $hexBody += " background-image:url(" + $picUrlPath + gridObject.fileName + ");";
                $hexBody += "\">";
                $hexBody += "</div>";
                htmlString += $hexBody;
            });

            $('#object-layer').append(htmlString);
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
        scale += scaleRate;
        if (maxZoom !== null && scale > maxZoom) {
            scale = maxZoom;
        }
    } else {
        scale -= scaleRate;
        if (minZoom !== null && scale < minZoom) {
            scale = minZoom;
        }
    }
    scaleTiles();
    $('html, body').stop().animate({}, 500, 'linear');
    return false;
}
function stopDrag() {
    drag=false;
}