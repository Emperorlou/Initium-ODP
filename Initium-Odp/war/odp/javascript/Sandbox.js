var hexagons = [
    new Hexagon(0, 0, "tile-grass1"),
    new Hexagon(1, 0, "tile-grass2"),
    new Hexagon(2, 0, "tile-grass3"),
    new Hexagon(-1, 1, "tile-grass4"),
    new Hexagon(0, 1, "tile-grass5"),
    new Hexagon(1, 1, "tile-grass6"),
    new Hexagon(2, 1, "tile-grass1"),
    new Hexagon(-1, 2, "tile-grass2"),
    new Hexagon(0, 2, "tile-grass3"),
    new Hexagon(1, 2, "tile-grass4"),
];

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

    document.getElementById("viewportcontainer").style.position = "relative";
    document.getElementById("viewport").style.position = "absolute";
    document.getElementById("ground-layer").style.position = "absolute";

    // Remove all current images
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
            $.each(responseJson, function (index, value) {
                $.each(value, function (innerIndex, innerValue) {
                    //var r = hexagons[i].r;
                    //var q = hexagons[i].q;
                    //var a = hexagons[i].a;

                    var r = index;
                    var q = innerIndex - outerLoop;
                    var $picUrlPath = "https://initium-resources.appspot.com/images/newCombat/";

                    $('#ground-layer').append('<div id="hex' + i + '" class="hexagon" />')

                    var $hexBody = $("#hex" + i);

                    $hexBody.append('<div id="hex' + i + 'Top" class="hexTop"/>');
                    $hexBody.append('<div id="hex' + i + 'Bot" class="hexBottom"/>');

                    $hexBody.css("position", "relative");
                    $hexBody.css("width", hexWidth + 'px');
                    $hexBody.css("height", hexSize + 'px');
                    $hexBody.css("margin", hexSize / 2 + 'px');
                    $hexBody.css("top", offsetY + hexSize * 3 / 2 * r + 'px');
                    $hexBody.css("left", offsetX + hexSize * Math.sqrt(3) * (q + r / 2) + 'px');
                    $hexBody.append('<style>#hex' + i + ':before{background-image:url(https://initium-resources.appspot.com/images/newCombat/' + innerValue.fileName + ');}</style>');
                    $hexBody.append('<style>#hex' + i + ':before{width: ' + imgSize * scale + 'px;}</style>'); //width
                    $hexBody.append('<style>#hex' + i + ':before{height: ' + imgSize * scale + 'px ;}</style>'); //height
                    $hexBody.append('<style>#hex' + i + ':before{margin-left: ' + imgSize / -4 * scale + 'px ;}</style>'); //margin-left
                    $hexBody.append('<style>#hex' + i + ':before{margin-top: ' + imgSize * -3 / 8 * scale + 'px ;}</style>'); //margin-top
                    //$hexBody.append('<style>#hex' + i + ':before{z-index: ' + innerValue.zIndex + ' ;}</style>'); //margin-top
                    //$hexBody.append('<style>#hex' + i + ':before{  }</style>'); //z-index


                    var $hexTop = $("#hex" + i + "Top");
                    var $hexBot = $("#hex" + i + "Bot");
                    var hexTBBase = hexWidth / Math.sqrt(2);
                    var hexLeft = (Math.sqrt(Math.pow(hexTBBase, 2) * 2) - hexTBBase) / 2;

                    $hexTop.css("width", hexTBBase + 'px');
                    $hexTop.css("height", hexTBBase + 'px');
                    $hexTop.css("left", hexLeft + 'px');
                    $hexTop.css("top", hexTBBase / -2 + 'px');

                    $hexBot.css("width", hexTBBase + 'px');
                    $hexBot.css("height", hexTBBase + 'px');
                    $hexBot.css("left", hexLeft + 'px');
                    $hexBot.css("bottom", hexTBBase / -2 + 'px');
                    i++;
                });
                if (outerLoop == (hexEdge-1) || reachedDiag) {
                    reachedDiag = true;
                    //outerLoop--;
                } else {
                    outerLoop++;
                }
            });
        }
    });
}

function Hexagon(collumn, row, asset) {
    this.r = row;
    this.q = collumn;
    this.a = asset;
}