$(document).ready(function () {
    loadMap();
});

function loadMap() {
    var offsetX = 100;
    var offsetY = 100;
    var scale = $("#zoom").val();

    var hexSize = 32*scale;
    var hexHeight = hexSize * 2;
    var hexWidth = Math.sqrt(3) / 2 * hexHeight;
    var hexEdge = $("#hexEdge").val();
    var hexDiag = hexEdge * 2 - 1;
    var horizontalDist = hexWidth;
    var verticalDist = hexHeight * 3 / 4;
    var imgSize = 128;
    var i=0;
    var outerLoop=0;
    var reachedDiag = false;
    var hexTBBase = hexWidth / Math.sqrt(2);
    var hexLeft = (Math.sqrt(Math.pow(hexTBBase, 2) * 2) - hexTBBase) / 2;
    var htmlString = "";
    var xOffset = (hexDiag*hexSize)/2;
    var $picUrlPath = "https://initium-resources.appspot.com/images/newCombat/";

    document.getElementById("viewportcontainer").style.position = "relative";
    document.getElementById("viewport").style.position = "absolute";
    document.getElementById("ground-layer").style.position = "absolute";

    // Remove all current tiles
    var images = document.getElementsByClassName('hexagon');
    var l = images.length;
    for (var i = 0; i < l; i++) {
        images[0].parentNode.removeChild(images[0]);
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
                    var top = (offsetY + hexSize * 3 / 2 * r);
                    var left = (xOffset + hexSize * Math.sqrt(3) * (q + r / 2));

                    $hexBody = "<div";
                    $hexBody += " id=\"hex" + i + "\"";
                    $hexBody += " class=\"hexagon\"";

                    $hexBody += " style=\"";
                    $hexBody += "width: " + hexWidth + 'px' + ";";
                    $hexBody += " height:" + hexSize + 'px' + ";";
                    $hexBody += " margin:" + (hexSize / 2) + 'px' + ";";
                    $hexBody += " top:" + top + 'px' + ";";
                    $hexBody += " left:" +  left + 'px' + ";";
                    $hexBody += "\">";

                    $hexBody += "<div id=\"hex" + i + "Top\" " + "class=\"hexTop\"";
                    $hexBody += " style=\"";
                    $hexBody += "width: " + hexTBBase + 'px' + ";";
                    $hexBody += " height:" + hexTBBase + 'px' + ";";
                    $hexBody += " top:" + (hexTBBase / -2) + 'px' + ";";
                    $hexBody += " left:" +  hexLeft + 'px' + ";";
                    $hexBody += "\">";
                    $hexBody += "</div>";

                    $hexBody += "<div id=\"hex" + i + "Bot\" " + "class=\"hexBottom\"";
                    $hexBody += " style=\"";
                    $hexBody += "width: " + hexTBBase + 'px' + ";";
                    $hexBody += " height:" + hexTBBase + 'px' + ";";
                    $hexBody += " bottom:" + (hexTBBase / -2) + 'px' + ";";
                    $hexBody += " left:" +  hexLeft + 'px' + ";";
                    $hexBody += "\">";
                    $hexBody += "</div>";

                    $hexBody += "<style>#hex" + i + ":before{";
                    $hexBody += "background-image:url(" + $picUrlPath + innerValue.fileName + ")";
                    $hexBody += ";}</style>";
                    $hexBody += "<style>#hex" + i + ":before{";
                    $hexBody += "width: " + imgSize * scale + "px";
                    $hexBody += ";}</style>";
                    $hexBody += "<style>#hex" + i + ":before{";
                    $hexBody += "height: " + imgSize * scale + "px";
                    $hexBody += ";}</style>";
                    $hexBody += "<style>#hex" + i + ":before{";
                    $hexBody += "margin-left: " + imgSize / -4 * scale + "px";
                    $hexBody += ";}</style>";
                    $hexBody += "<style>#hex" + i + ":before{";
                    $hexBody += "margin-top: " + imgSize * -3 / 8 * scale + "px";
                    $hexBody += ";}</style>";
                    $hexBody += "</div>";

                    htmlString += $hexBody;

                    i++;
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