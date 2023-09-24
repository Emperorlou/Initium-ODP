
function clearAllType1Buttons()
{
	$(".type1-button").html("");	
}

/**
 * This adds a button to the button bar.
 * @param position
 * @param imageUrl
 * @param shortcut
 * @param javascript
 */
function addType1Button(position, imageUrl, shortcut, javascript)
{
	var btn = $(".type1-button#button"+position);
	if (btn==null || btn.length==0) throw "Invalid button position: "+position;
	
	var html = "";
	if (shortcut!=null)
		html+="<span>"+shortcut+"</span>";
	
	html+="<img src='"+imageUrl+"'/>";
	
	btn.html(html);
}