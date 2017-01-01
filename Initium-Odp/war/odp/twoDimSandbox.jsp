Width: <input type='text' id='width' value=20 />
Height: <input type='text' id='height' value=20 />
Seed: <input type='text' id='seed' value=123456 />
<button id="somebutton">press here</button>

<div id="somediv"></div>

<script type="text/javascript" src="/odp/javascript/Sandbox.js"></script>
<script>
    $(document).on("click", "#somebutton", function() {
        getTilePos();
    });
</script>
