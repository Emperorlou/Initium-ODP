<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<style>
	.table
	{
		display:table;
		width:100%;
	}
	.header-bar
	{
		width:100%;
		display:table-row;
	}
	.header-cell
	{
		display:table-cell;
		width:50%;
		border-bottom:2px dotted #777777;
		vertical-align:bottom;
	}
	#left
	{
		width:50%;
		display:table-cell;
		overflow:hidden;
		border-right:1px dotted #777777;
	}
	#right
	{
		width:50%;
		display:table-cell;
		overflow:hidden;
		border-left:1px dotted #777777;
		padding-left:2%;
	} 
	.move-left,.move-right
	{
		float:left;
	}
	
	.location-heading-style
	{
		position:relative;
	}
	.location-heading-style h5
	{
		position: absolute;
	    right: 4px;
	    bottom: 2px;
	    margin: 0px;
	}
	.location-heading-style .banner-background-image
	{
	    width: 100%;
	    height: 50px;
	    background-position: center center;
	    background-size: cover;
	    border-top-left-radius: 8px;
	    border-top-right-radius: 8px;
	}
</style>
<center><h1>Item Transfer</h1></center>
<div class='table'>
	<div class='header-bar'>
		<div class='header-cell'>
			${selfSideHeader}
		</div>
		<div class='header-cell' style='text-align:right'>
			${otherSideHeader}
		</div>
	</div>
	<div id='left'>
		${selfSideCommands}
		<c:forEach var="item" items="${selfSideItems}">
			${item}
		</c:forEach> 
	</div>
	<div id='right'>
		${otherSideCommands}
		<c:forEach var="item" items="${otherSideItems}">
			${item}
		</c:forEach> 
	</div>
</div>