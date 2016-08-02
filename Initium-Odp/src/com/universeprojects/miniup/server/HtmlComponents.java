package com.universeprojects.miniup.server;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;

public class HtmlComponents {

	public static String generateInvItemHtml(CachedEntity item) {
		
		if (item==null)
			return " ";
		
		String result = "";
			   result+="<div class='invItem' ref="+item.getKey().getId()+">";
			   result+="<div class='main-item'>";
			   result+="<div class='main-item-container'>";
			   result+=GameUtils.renderItem(item);
			   result+="<br>";
			   result+="			<div class='main-item-controls'>";
			   result+="				<a onclick='storeSellItemNew(event,"+item.getKey().getId()+")'>Sell This</a>";
			   result+="			</div>";
			   result+="		</div>";
			   result+="	</div>";
			   result+="</div>";
			   result+="<br>";
			   
		return result;
	}
	
	public static String generateSellItemHtml(ODPDBAccess db, CachedEntity saleItem, HttpServletRequest request) {
		
		CachedEntity item = db.getEntity((Key)saleItem.getProperty("itemKey"));
		Double storeSale = (Double)db.getCurrentCharacter(request).getProperty("storeSale");
		if (storeSale == null)
		{
			storeSale = 100.0;
		}
		String itemPopupAttribute = "";
		String itemName = "";
		String itemIconElement = "";
		if (item!=null)
		{
			itemName = (String)item.getProperty("name");
			itemPopupAttribute = "class='clue "+GameUtils.determineQuality(item.getProperties())+"' rel='viewitemmini.jsp?itemId="+item.getKey().getId()+"'";
			itemIconElement = "<img src='"+item.getProperty("icon")+"' border=0/>"; 
		}
		Long cost = (Long)saleItem.getProperty("dogecoins");
		cost=Math.round(cost.doubleValue()*(storeSale/100));
		String finalCost = cost.toString();
		String statusText = (String)saleItem.getProperty("status");
		if (statusText.equals("Sold"))
		{
			String soldTo = "";
			if (saleItem.getProperty("soldTo")!=null)
			{
				CachedEntity soldToChar = db.getEntity((Key)saleItem.getProperty("soldTo"));
				if (soldToChar!=null)
					soldTo = " to "+(String)soldToChar.getProperty("name");
			}
			statusText = "<div class='saleItem-sold'>"+statusText+soldTo+"</div>";
		}
		
		String result = "";
			   result+="<div class='saleItem' ref="+saleItem.getKey().getId()+">";
		   	   result+="<div class='main-item'>";
		   	   result+=" ";
		   	   result+="<div class='main-item-container'>";
		   	   result+="<a onclick='storeDeleteItemNew(event,"+saleItem.getKey().getId()+")' class='main-item-bigx'>X</a> <a "+itemPopupAttribute+">"+itemIconElement+""+itemName+"</a> <div class='main-item-storefront-status'>(<img src='images/dogecoin-18px.png' class='small-dogecoin-icon' border=0/>"+finalCost+" - "+statusText+")</div>";
//		   	   result+="<br>";
//		   	   result+="<div class='main-item-controls'>";
//		   	   result+="</div>";
		   	   result+="</div>";
		   	   result+="</div>";
		   	   result+="</div>";
		   	   
   	   return result;
	}
	
	public static String generateStoreItemHtml(ODPDBAccess db, CachedEntity storeCharacter, CachedEntity item, CachedEntity saleItem, HttpServletRequest request){
		
        String itemName = "(Item Destroyed)";
        String itemPopupAttribute = "";
        String itemIconElement = "";
        Double storeSale = (Double)storeCharacter.getProperty("storeSale");
        if (item!=null)
        {
            itemName = (String)item.getProperty("name");
            itemPopupAttribute = "class='clue "+GameUtils.determineQuality(item.getProperties())+"' rel='viewitemmini.jsp?itemId="+item.getKey().getId()+"'";
            itemIconElement = "<img src='"+item.getProperty("icon")+"' border=0/>"; 
        }
        
        Long cost = (Long)item.getProperty("store-dogecoins");
        cost=Math.round(cost.doubleValue()*(storeSale/100));
        String finalCost = GameUtils.formatNumber(cost, false);
        
      
        String result ="";
        		result+="<div class='saleItem' ref="+saleItem.getKey().getId()+">";
				result+="<div class='main-item'>";
	   	       	result+="<span><img src='images/dogecoin-18px.png' class='small-dogecoin-icon' border=0/>"+finalCost+"</span>";
	   	       	result+="<span>";
	   	    if ("Selling".equals(item.getProperty("store-status")))
	   	    	result+="<a "+itemPopupAttribute+">"+itemIconElement+""+itemName+"</a> - <a onclick='storeBuyItemNew(\""+itemName.replace("'", "`")+"\",\""+finalCost+"\","+storeCharacter.getKey().getId()+","+((Key)item.getProperty("store-saleItemKey")).getId()+", "+item.getKey().getId()+")'>Buy this</a>";
	   	    else if ("Sold".equals(item.getProperty("store-status")))   
	   	    	result+="<a "+itemPopupAttribute+">"+itemIconElement+""+itemName+"</a> - <div class='saleItem-sold'>SOLD</div>";
	   	       	result+="</span>";
	   	    	result+="</div>";
	   	       	result+="</div>";
	   	       	result+="<br>";
		
		return result;
	}

	public static String generateTradeInvItemHtml(CachedEntity item, ODPDBAccess db, CachedDatastoreService ds, HttpServletRequest request) {
		
		if (item==null)
			return " ";
		
		List<CachedEntity> saleItems = db.getFilteredList("SaleItem", "characterKey", db.getCurrentCharacter(request).getKey());
		
		String saleText = "";
		// Determine if this item is for sale or not and mark it as such after
		for(CachedEntity saleItem:saleItems)
		{
			if (((Key)saleItem.getProperty("itemKey")).getId()==item.getKey().getId())
			{
				saleText = "<div class='main-item-subnote' style='color:#FF0000'> - Selling</div>";
				break;
			}
		}
		
		String result = "";
			   result+="<div class='invItem' ref="+item.getKey().getId()+">";
			   result+="<div class='main-item'>";
			   result+="<div class='main-item-container'>";
			   result+=GameUtils.renderItem(item)+saleText;
			   result+="<br>";
			   result+="			<div class='main-item-controls'>";
			   result+="				<a onclick='tradeAddItemNew("+item.getKey().getId()+")'>Add to trade window</a>";
			   result+="			</div>";
			   result+="		</div>";
			   result+="	</div>";
			   result+="</div>";
			   result+="<br>";
			   
		return result;
	}
	
	public static String generatePlayerTradeItemHtml(CachedEntity item, CachedEntity character){
		
		String result = "";
			   result+="<div class='tradeItem' ref"+item.getKey().getId()+">";
		       result+="<div class='main-item'>";
		       result+="<div class='main-item-container'>";
		       result+=GameUtils.renderItem(item);
		       result+="<br>";
		       result+="			<div class='main-item-controls'>";
		       result+="				<a onclick='tradeRemoveItemNew(event,"+item.getKey().getId()+","+character.getKey().getId()+")'>Remove</a>";
		       result+="			</div>";
		       result+="		</div>";
		       result+="	</div>";
		       result+="</div>";
		       result+="<br>";
		
		
		return result;
	}
	
}
