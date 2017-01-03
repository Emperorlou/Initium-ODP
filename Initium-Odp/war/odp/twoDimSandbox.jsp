Hex Edge: <input type='text' id='hexEdge' value=4 />
Zoom: <input type='text' id='zoom' value=1 />
<%--Offset: <input type='text' id='offset' value=8 />--%>
Seed: <input type='text' id='seed' value=123456 />
<button id="somebutton">press here</button>

<div id="ground-layer"></div>

<script src="http://code.jquery.com/jquery-latest.min.js"></script>
<script src="Sandbox.js"></script>
<script type="text/javascript">
    $(document).on("click", "#somebutton", function() {
        loadMap();
    });
</script>
