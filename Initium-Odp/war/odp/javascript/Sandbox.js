var scale = $("#zoom").val();
var viewport = document.getElementById("viewport");
var maxZoom = 2.4;

window.onload = function() {
    document.onmousedown = startDrag;
    document.onmouseup = stopDrag;
    //document.ontouchend = stopDrag;
    //document.ontouchstart = startDrag;
    document.body.addEventListener('touchend', stopDrag);
    document.body.addEventListener('touchmove', touchMoveDiv);
    addEventListener('touchstart', function (e) {

        if (e.touches.length == 1) {
            var targ = e.target ? e.target : e.srcElement;
            if (targ.className != 'hexBackground' && targ.className != 'groundLayerContainer' && targ.className != 'vp' && targ.className !=  'vpcontainer') {return};
            e.preventDefault();

            // calculate event X, Y coordinates
            offsetX = e.touches[0].clientX;
            offsetY = e.touches[0].clientY;

            coordX = parseInt(viewport.style.left);
            coordY = parseInt(viewport.style.top);

            if(!viewport.style.left) { viewport.style.left='0px'};
            if (!viewport.style.top) { viewport.style.top='0px'};

            drag = true;
            document.ontouchmove=dragDiv;
        } else if (e.touches.length == 2) { // If two fingers are touching
            var targ = e.target ? e.target : e.srcElement;
            if (targ.className != 'hexBackground' && targ.className != 'groundLayerContainer' && targ.className != 'vp' && targ.className !=  'vpcontainer') {return};
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
        var max = maxZoom;
        var min = .1;
        if (e.originalEvent.deltaY < 0) {
            scale += .1;
            if (max !== null && scale > max) {
                scale = max;
            }
        } else {
            scale -= .1;
            if (min !== null && scale < min) {
                scale = min;
            }
        }
        scaleTiles();
        $('html, body').stop().animate({}, 500, 'linear');
    }
});

function pressedButton() {
    scale = $("#zoom").val();
    loadMap();
}

function scaleTiles() {

    var hexSize = 32*scale;
    var maxHexSize = 32*maxZoom;
    var hexHeight = hexSize * 2;
    var maxHexHeight = maxHexSize * 2;
    var hexWidth = Math.sqrt(3) / 2 * hexHeight;
    var hexDiag = hexEdge * 2 - 1;
    var imgSize = 128;
    var offsetX = (window.innerWidth/2)-((hexEdge/2)*hexWidth);
    var offsetY = maxHexHeight/2;
    document.getElementById("ground-layer").style.width = hexDiag*hexWidth;
    document.getElementById("ground-layer").style.height = (hexEdge*3-1)*hexSize;
    document.getElementById("ground-layer").style.top = offsetY;
    document.getElementById("ground-layer").style.left = offsetX;

    // Update all tiles
    var hexTiles = document.getElementsByClassName('hexagon');
    var l = hexTiles.length;
    for (var index = 0; index < l; index++) {

        var tagIndex = hexTiles[index].id.substr(3, hexTiles[index].id.length);
        var tileIndexes = tagIndex.split("_");
        var r = Number(tileIndexes[0]);
        var q = Number(tileIndexes[1]);
        var top = (hexSize * 3 / 2 * r);
        var left = hexSize * Math.sqrt(3) * (q + r / 2);

        hexTiles[index].style.width = hexWidth + "px";
        hexTiles[index].style.height = hexSize + "px";
        hexTiles[index].style.margin = (hexSize / 2) + "px";
        hexTiles[index].style.top = top + "px";
        hexTiles[index].style.left = left + "px";

        var hexBack = hexTiles[index].children[0];
        hexBack.style.width = imgSize * scale + 'px';
        hexBack.style.height = imgSize * scale + 'px';
        hexBack.style.marginLeft = (imgSize / -4) * scale + 'px';
        hexBack.style.marginTop= (imgSize * -3 / 8) * scale + 'px';
    }
}

function loadMap() {

    var hexEdge = $("#hexEdge").val();
    var hexSize = 32*scale;
    var maxHexSize = 32*maxZoom;
    var hexHeight = hexSize * 2;
    var maxHexHeight = maxHexSize * 2;
    var hexWidth = Math.sqrt(3) / 2 * hexHeight;
    var hexDiag = hexEdge * 2 - 1;
    var imgSize = 128;
    var outerLoop=0;
    var reachedDiag = false;
    var htmlString = "";
    var offsetX = (window.innerWidth/2)-((hexEdge/2)*hexWidth);
    var offsetY = maxHexHeight/2;
    var $picUrlPath = "https://initium-resources.appspot.com/images/newCombat/";

    document.getElementById("ground-layer").style.width = hexDiag*hexSize;
    document.getElementById("ground-layer").style.height = hexDiag*hexHeight;
    document.getElementById("ground-layer").style.top = offsetY;
    document.getElementById("ground-layer").style.left = offsetX;

    document.getElementById("viewportcontainer").style.position = "relative";
    document.getElementById("viewport").style.position = "absolute";
    document.getElementById("ground-layer").style.position = "absolute";

    // Remove all current tiles
    var hexTiles = document.getElementsByClassName('hexagon');
    var l = hexTiles.length;
    for (var i = 0; i < l; i++) {
        hexTiles[0].parentNode.removeChild(hexTiles[0]);
    }

    $.ajax({
        url: "SandboxServlet",
        data:{width:hexEdge, seed:$("#seed").val()},
        type: 'POST',
        success: function(responseJson) {
            $.each(responseJson['hexTiles'], function (index, value) {
                $.each(value, function (innerIndex, innerValue) {

                    var r = index;
                    var q = innerIndex - outerLoop;
                    var i = r + "_" + q;
                    var top = (hexSize * 3 / 2 * r);
                    var left = (hexSize * Math.sqrt(3) * (q + r / 2));

                    $hexBody = "<div";
                    $hexBody += " id=\"hex" + i + "\"";
                    $hexBody += " class=\"hexagon\"";
                    $hexBody += " data-pos=\"" + i + "\"";
                    $hexBody += " style=\"";
                    $hexBody += "width: " + hexWidth + 'px' + ";";
                    $hexBody += " height:" + hexSize + 'px' + ";";
                    $hexBody += " margin:" + (hexSize / 2) + 'px' + ";";
                    $hexBody += " top:" + top + 'px' + ";";
                    $hexBody += " left:" +  left + 'px' + ";";
                    $hexBody += "\">";

                    $hexBody += "<div id=\"hex" + i + "Back\" " + "class=\"hexBackground\"";
                    $hexBody += " style=\"";
                    $hexBody += "width: " + imgSize * scale + 'px' + ";";
                    $hexBody += " height:" + imgSize * scale + 'px' + ";";
                    $hexBody += " margin-left:" + imgSize / -4 * scale + 'px' + ";";
                    $hexBody += " margin-top:" + imgSize * -3 / 8 * scale + 'px' + ";";
                    $hexBody += " background-image:url(" + $picUrlPath + innerValue.fileName + ");";
                    $hexBody += "\">";
                    $hexBody += "</div>";
                    $hexBody += "</div>";

                    htmlString += $hexBody;
                });
                if (outerLoop == (hexEdge-1) || reachedDiag) {
                    reachedDiag = true;
                } else {
                    outerLoop++;
                }
            });
            $('#ground-layer').append(htmlString);
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

    if (targ.className != 'hexBackground' && targ.className != 'groundLayerContainer' && targ.className != 'vp' && targ.className !=  'vpcontainer') {return};
    // calculate event X, Y coordinates
    offsetX = e.clientX;
    offsetY = e.clientY;

    if(!viewport.style.left) { viewport.style.left='0px'};
    if (!viewport.style.top) { viewport.style.top='0px'};

    // calculate integer values for top and left 
    // properties
    coordX = parseInt(viewport.style.left);
    coordY = parseInt(viewport.style.top);
    drag = true;

    // move div element
    document.onmousemove=dragDiv;
}
function dragDiv(e) {
    if (!drag) {return};
    if (!e) { var e= window.event};
    // move div element
    viewport.style.left=coordX+e.clientX-offsetX+'px';
    viewport.style.top=coordY+e.clientY-offsetY+'px';
    return false;
}
function touchMoveDiv(e) {
    if (!drag) {return};
    if (!e) { var e= window.event};
    // move div element
    viewport.style.left=coordX+e.touches[0].clientX-offsetX+'px';
    viewport.style.top=coordY+e.touches[0].clientY-offsetY+'px';
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
    var max = maxZoom;
    var min = .1;
    if (d1 > d2) {
        scale += .1;
        if (max !== null && scale > max) {
            scale = max;
        }
    } else {
        scale -= .1;
        if (min !== null && scale < min) {
            scale = min;
        }
    }
    scaleTiles();
    return false;
}
function stopDrag() {
    drag=false;
}