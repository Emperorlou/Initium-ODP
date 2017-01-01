function getTilePos() {
    $.ajax({
        url: "SandboxServlet",
        data:{width:$("#width").val(), height:$("#height").val(), seed:$("#seed").val()},
        type: 'POST',
        success: function(responseJson) {

            // Remove all current images
            var images = document.getElementsByTagName('img');
            var l = images.length;
            for (var i = 0; i < l; i++) {
                images[0].parentNode.removeChild(images[0]);
            }

            // Iterate through JSON and place images at offset distances, starting at 0x64
            var $select = $("#somebody");
            var $xCoord = 0;
            var $yCoord = 64;
            var $offset = 32;
            var $picUrlPath = "https://initium-resources.appspot.com/images/newCombat/";
            $.each(responseJson, function (index, value) {
                $.each(value, function (innerIndex, innerValue) {
                    $("<img>").css({"position":"absolute", "top":$yCoord, "left":$xCoord}).attr("src", $picUrlPath.concat(innerValue.fileName)).appendTo($select);
                    $xCoord = $xCoord + $offset;
                });
                $xCoord = 0;
                $yCoord = $yCoord + $offset;
            });
        }
    });
}
