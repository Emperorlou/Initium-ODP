<script>
$(document).ready(function() {
refreshLists();
});
</script>
<div class='mini-window-header-container'>
<div class="mini-window-header">
    <div class="mini-window-header-split">
        <a onclick="clearIgnoreList()">Clear ignored list</a><br><br>
        <a onclick="refreshLists()">Refresh</a>
    </div>
</div>
<div>
    <h5>Ignored list</h5><br>
    <p id="ignoreList"></p>
    <h5>Recent chatters list</h5><br>
    <div id="suggestedList"></div>
</div>
</div>