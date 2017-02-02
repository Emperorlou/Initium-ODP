<%@ page language="java" contentType="text/html; charset=UTF8" pageEncoding="UTF8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<script type='text/javascript'>
function changeInventionTab(event)
{
	var element = $(event.target);
	var id = element.attr("id");
	var code = id.substring(0, id.indexOf("-")-1);
	
	$(".invention-content").hide();
	$("#"+code+"-content").show();
}

</script>

<div id='tab-row'>
	<div class='tab-row-tab' id='experimentation-tab'>Experiments</div>
	<div class='tab-row-tab' id='idea-tab'>Ideas</div>
	<div class='tab-row-tab' id='itemConstruction-tab'>Item Construction</div>
	<div class='tab-row-tab' id='buildingConstruction-tab'>Building Construction</div>
</div>
<div class='invention-content' id='experimentation-content'>

</div>
<div class='invention-content' id='idea-content'>

</div>
<div class='invention-content' id='itemConstruction-content'>

</div>
<div class='invention-content' id='buildingConstruction-content'>

</div>