var scale = $("#zoom").val();
var maxZoom = 2.4;

$(document).ready(function () {
    loadMap();
});

$(window).on("wheel", function(e) {

    focusedEl = document.activeElement;
    var target = $(e.target);
    var elId = target.attr('class');
    if (elId == "hexagon" || "hexBackground") {
        e.preventDefault();
        var max = maxZoom;
        var min = .2;
        if (focusedEl.hasAttribute('max')) {
            max = focusedEl.getAttribute('max');
        }
        if (focusedEl.hasAttribute('min')) {
            min = focusedEl.getAttribute('min');
        }
        var value = parseInt(focusedEl.value, 10);
        if (e.originalEvent.deltaY < 0) {
            scale += .2;
            if (max !== null && scale > max) {
                scale = max;
            }
        } else {
            scale -= .2;
            if (min !== null && scale < min) {
                scale = min;
            }
        }
        zoom();
        focusedEl.value = value;
    }
    $('html, body').stop().animate({
    },500, 'linear');
});

function pressedButton() {
    scale = $("#zoom").val();
    loadMap();
}

function zoom() {

    var hexEdge = $("#hexEdge").val();
    var hexSize = 32*scale;
    var maxHexSize = 32*maxZoom;
    var hexHeight = hexSize * 2;
    var hexWidth = Math.sqrt(3) / 2 * hexHeight;
    var hexDiag = hexEdge * 2 - 1;
    var imgSize = 128;
    var xOffset = (hexDiag*maxHexSize)/2;
    var offsetY = 100;

    // Update all tiles
    var hexTiles = document.getElementsByClassName('hexagon');
    var l = hexTiles.length;
    for (var index = 0; index < l; index++) {

        var tagIndex = hexTiles[index].id.substr(3, hexTiles[index].id.length);
        var tileIndexes = tagIndex.split("_");
        var r = Number(tileIndexes[0]);
        var q = Number(tileIndexes[1]);
        var i = r + "_" + q;
        var top = (offsetY + hexSize * 3 / 2 * r);
        var left = xOffset + (hexSize * Math.sqrt(3) * (q + r / 2));

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
    var hexWidth = Math.sqrt(3) / 2 * hexHeight;
    var hexDiag = hexEdge * 2 - 1;
    var imgSize = 128;
    var outerLoop=0;
    var reachedDiag = false;
    var htmlString = "";
    var xOffset = (hexDiag*maxHexSize)/2;
    var offsetY = 100;
    var $picUrlPath = "https://initium-resources.appspot.com/images/newCombat/";

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
            $.each(responseJson, function (index, value) {
                $.each(value, function (innerIndex, innerValue) {

                    var r = index;
                    var q = innerIndex - outerLoop;
                    var i = r + "_" + q;
                    var top = (offsetY + hexSize * 3 / 2 * r);
                    var left = (xOffset + hexSize * Math.sqrt(3) * (q + r / 2));

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