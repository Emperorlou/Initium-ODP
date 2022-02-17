<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ page language="java" contentType="text/html; charset=UTF8" pageEncoding="UTF8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<center><a class='standard-button-highlight' onclick='removeAllItemFilters();'><strong>Remove all of your Item Filters.</strong></a></center>

<br><br>
Here are your item filters. When you kill an instanced monster, all of the items will be compared to this list to see if they should be dropped or not.
<br><br>
The filter is based on the item's name, and it will filter out any items with equal or lower rarity.
<br><br>
<a class=item-junk>Grey</a> -> <a class=item-normal>White</a> -> <a class=item-rare>Bronze</a> -> <a class=item-unique>Gold</a>
<br><br>
<c:if test="${hasFilters == false}">
	You don't have any item filters. To add one, click on an item, then click the crossed out icon on the bottom of the popup.
</c:if>
<c:if test="${hasFilters == true}">
	<c:forEach var="filter" items="${itemFilters}">
		${filter}
	</c:forEach>
</c:if>