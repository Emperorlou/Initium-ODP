<%@ page contentType="text/html;charset=UTF-8" language="java" import="com.universeprojects.miniup.server.RandomTileGenerator" %>

<html>
<head>
    <title>Sandbox 2D Tiles</title>
</head>
<body id="somebody">

Width: <input type='text' id='width' value=20 />
Height: <input type='text' id='height' value=20 />
Seed: <input type='text' id='seed' value=123456 />
<button id="somebutton">press here</button>

<script src="http://code.jquery.com/jquery-latest.min.js"></script>
<script>
    $(document).on("click", "#somebutton", function() {
        $.ajax({
            url: "MyServlet",
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
    });
</script>

</body>
</html>
