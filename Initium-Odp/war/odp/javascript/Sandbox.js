function getTilePos() {

    var $pxWidth = $("#width").val();
    var $pxHeight = $("#height").val();
    var $offset = ~~document.getElementById("offset").value;
    var $tileMaxPx = 128;
    document.getElementById("somediv").style.maxWidth = $pxWidth + "px";
    document.getElementById("somediv").style.maxHeight = $pxHeight + "px";

    var $shownTilePx = $tileMaxPx - $offset;

    var $tileColumns = Math.ceil($pxWidth / $shownTilePx);
    var $tileRows = Math.ceil($pxHeight / $shownTilePx);

    $.ajax({
        url: "SandboxServlet",
        data:{width:$tileColumns, height:$tileRows, seed:$("#seed").val()},
        type: 'POST',
        success: function(responseJson) {

            // Remove all current images
            var images = document.getElementsByTagName('img');
            var l = images.length;
            for (var i = 0; i < l; i++) {
                images[0].parentNode.removeChild(images[0]);
            }

            // Iterate through JSON and place images at offset distances, starting at 0x64
            var $select = $("#somediv");

            var $xCoord = 0;
            var $yCoord = 300;

            var $width = $tileMaxPx / Math.ceil($pxWidth / $tileMaxPx);
            var $height = $tileMaxPx / Math.ceil($pxHeight / $tileMaxPx);
            var $picUrlPath = "https://initium-resources.appspot.com/images/newCombat/";

            //document.getElementById('somediv').style.width=$pxWidth;
            //document.getElementById('somediv').style.height=$pxHeight;
            $.each(responseJson, function (index, value) {
                $.each(value, function (innerIndex, innerValue) {
                    $("<img>")
                        .css({
                            "position":"relative",
                            "float": "left",
                            "margin-top": -($offset) +"%",
                            "margin-bottom": -($offset) +"%",
                            "margin-left": -($offset) +"%",
                            "margin-right": -($offset) +"%",
                            "width":$width + "%",
                            "padding-top":"0%",
                            "display": "flex",
                            "justify-content": "center",
                            "align-items": "center",
                            "text-align": "center"
                        })
                        //.text("TEST").appendTo($select);
                        .attr("src", $picUrlPath.concat(innerValue.fileName)).appendTo($select);
                    $xCoord = $xCoord   + $offset;
                });
                $xCoord = 0;
                $yCoord = $yCoord + $offset;
            });
        }
    });
}