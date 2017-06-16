<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<div class="item-popup">
	<input type='hidden' id='popupItemId' value='${item.itemId}'/>
	<c:if test="${isItemOwner}"><a href='#' onclick='customizeItemOrderPage(${item.itemId})' style='float:left'>Customize</a></c:if>
	<div class='normal-container'>
		<a onclick='shareItem(${item.itemId})' style='float:right' title='Clicking this will show the item in chat in the location you`re currently in.'>Share</a>
		<br/>
		<br/>
		<c:if test="${item.isContainer}"><p class='main-item-controls' style='display:block;margin:5px;'><a onclick='doSetLabel(event, ${item.itemId})' style='font-size:13px;'>Rename</a></p></c:if>
		<div class='item-popup-header'>
			<div class='icon'>
				<c:if test="${item.quantity != null}"><div name='itemQuantity' class='main-item-quantity-indicator-container'><div class='main-item-quantity-indicator'>${item.quantity}</div></div></c:if>
				<img src='${item.icon}' border='0'/>
			</div>
			<div style='width:230px'>
				<span name='itemName' class='${item.quality}'>${item.name}</span>
				<div name='itemClass' class='main-highlight' style='font-size:14px'>${item.itemClass}</div>
			</div>
		</div>
		<div>
			<p>${item.itemSlot}</p>
			<div class='item-popup-stats'>
				<c:if test="${item.dexpen != null}"><div name='dexterityPenalty' class='item-popup-field' title='This is the percentage that the wearer`s dexterity will be reduced when making dexterity based rolls. Dexterity penalties stack.'>Dexterity penalty: <div class='main-item-subnote'>${item.dexpen}%</div></div></c:if>
				<c:if test="${item.strReq != null}"><div name='strengthRequirement' class='item-popup-field' title='This is the strength the wearer is required to have in order to equip this item.'>Strength requirement: <div class='main-item-subnote'>${item.strReq}</div></div></c:if>
				<c:if test="${item.requirements}"><br/><br/></c:if>
				<!-- Damage -->
				<c:if test="${item.weaponDamage != null}"><div name='weaponDamage' class='item-popup-field'>Weapon damage: <div class='main-item-subnote'>${item.weaponDamage}</div></div></c:if>
				<c:if test="${item.weaponCriticalChance != null}"><div name='weaponDamageCriticalChance' class='item-popup-field'> - Critical chance: <div class='main-item-subnote'>${item.weaponCriticalChance}% <span title='This is the total crit chance after your character`s crit chance has been taken into account.'>(${item.weaponCriticalChanceBuffed}%)</span></div></div></c:if>
				<c:if test="${item.weaponCriticalMultiplier != null}"><div name='weaponDamageCriticalMultiplier' class='item-popup-field'> - Critical hit multiplier: <div class='main-item-subnote'>${item.weaponCriticalMultiplier}x</div></div></c:if>
				<c:if test="${item.weaponDamageType != null}"><div name='weaponDamageType' class='item-popup-field'> - Damage Type: <div class='main-item-subnote'>${item.weaponDamageType}</div></div></c:if>
				<c:if test="${item.weaponDamageMax != null}"><div name='weaponDamageSummary' class='item-popup-field-summary' title='This handy summary shows you the max damage that the weapon is capable of, and the average damage the weapon will do over time.'> - (${item.weaponDamageMax} max dmg, ${item.weaponDamageAvg} avg dmg)</div></c:if>
				<!-- Defense -->
				<c:if test="${item.blockChance != null}"><div name='blockChance' class='item-popup-field' title='This is the odds of this armor blocking a hit on the body part that the armor is meant to protect.'>Block chance: <div class='main-item-subnote'>${item.blockChance}%</div></div></c:if>
				<c:if test="${item.damageReduction != null}"><div name='damageReduction' class='item-popup-field' title='This is the maximum amount of damage that this armor will absorb if it successfully blocks a hit.'> - Damage reduction: <div class='main-item-subnote'>${item.damageReduction}</div></div></c:if>
				<c:if test="${item.blockBludgeoningCapability != null}"><div name='blockBludgeoningCapability' class='item-popup-field' title='This describes this armor`s ability to block bludgeoning attacks. Excellent increases the damage reduction by x2.'> - Block bludgeon: <div class='main-item-subnote'>${item.blockBludgeoningCapability}</div></div></c:if>
				<c:if test="${item.blockPiercingCapability != null}"><div name='blockPiercingCapability' class='item-popup-field' title='This describes this armor`s ability to block piercing attacks. Excellent increases the damage reduction by x2.'> - Block piercing: <div class='main-item-subnote'>${item.blockPiercingCapability}</div></div></c:if>
				<c:if test="${item.blockSlashingCapability != null}"><div name='blockSlashingCapability' class='item-popup-field' title='This describes this armor`s ability to block slashing attacks. Excellent increases the damage reduction by x2.'> - Block slashing: <div class='main-item-subnote'>${item.blockSlashingCapability}</div></div></c:if>
				<!-- Misc -->
				<c:if test="${item.maxSpace != null}"><div name='maxSpace' class='item-popup-field' title='The amount of space inside this item for storing things.'>Storage space: <div class='main-item-subnote'>${item.maxSpace}</div></div></c:if>
				<c:if test="${item.maxWeight != null}"><div name='maxWeight' class='item-popup-field' title='The amount of weight this item can carry.'>Storage weight: <div class='main-item-subnote'>${item.maxWeight}</div></div></c:if>
				<c:if test="${item.weight != null}"><div name='weight' class='item-popup-field' title='The item`s weight in grams.'>Weight:&nbsp;<div class='main-item-subnote'>${item.weight}</div></div></c:if>
				<c:if test="${item.space != null}"><div name='space' class='item-popup-field' title='The amount of space this item roughtly takes up when placed in a box in cubic centimeters.'>Space:&nbsp;<div class='main-item-subnote'>${item.space}</div></div></c:if>
				<c:if test="${item.warmth != null}"><div name='warmth' class='item-popup-field' title='The amount of warmth this item provides the wearer.'>Warmth: <div class='main-item-subnote'>${item.warmth} units</div></div></c:if>
				<c:if test="${item.durability != null}"><div name='durability' class='item-popup-field' title='The number of uses this item still has left. Once it reaches 0, the item is destroyed.'>Durability: <div class='main-item-subnote'>${item.durability}</div></div></c:if>
				<c:if test="${item.aspectList != null}"><div class='simple-aspect-list'>${item.aspectList}</div></c:if>
			</div>
			<br/>
			<c:if test="${item.description != null}"><br/>
			<div name='description' class='item-flavor-description'>${item.description}</div></c:if>
		</div>
		<c:if test="${item.ownerOnlyHtml != null}"><div>${item.ownerOnlyHtml}</div></c:if>
		
		<c:if test="${item.popupEntries != null}">
		<c:forEach items="${item.popupEntries}" var="entry">
		<p>
			<a onclick='<c:out value="${entry.clickJavascript}"/>'>${entry.name}</a> 
			<br/>
			${entry.description}
		</p>
		</c:forEach>
		</c:if>
	</div>
	<c:if test="${showChippedTokenUI}">
	<div class='normal-container'>
		<p>
		<a onclick='combineChippedTokens(event, ${item.itemId})'>Make Premium Token</a><br>
		If this stack of Chipped Tokens has a quantity of at least 100, clicking the above link will turn 100 of these tokens into an Initium Premium Membership token.
		</p>
	</div>
	</c:if>
	<c:if test="${showPremiumTokenUI}">
	<div class='normal-container'>
		<p>
		<a href='ServletUserControl?type=usePremiumToken&itemId=${item.itemId}'>Deposit to Account</a> <br>
		Click this link to use this token on yourself. This will give your account premium membership. 
		If you already have a premium membership, 5 dollars will be added towards your donation credit 
		which you can use to gift premium to someone else, order a customization, or to create another 
		token like this at a later date.
		</p>
	
		<p>
		<a onclick='splitPremiumToken(event, ${item.itemId})'>Make 100 Chipped Tokens</a> <br>
		Clicking this option will give you a single stack of 100 Chipped Tokens in your inventory.
		You can later turn those tokens back into an Initium Premium Membership token, or trade the Chipped Tokens like they were any other item.
		</p>
	</div>
	</c:if>
	
	<c:if test="${comparisons != null}">
	<p id='item-comparison-link'><a onclick='$("#item-comparisons").show(); $("#item-comparison-link").hide();'>Compare with existing equipment</a></p>
	<div id='item-comparisons' style='display:none;'>
		<c:forEach items="${comparisons}" var="comparison">
		<hr/>
		<div class='normal-container'>
			<a onclick='shareItem(${comparison.itemId})' style='float:right' title='Clicking this will show the item in chat in the location you`re currently in.'>Share</a>
			<br/>
			<br/>
			<div class='item-popup-header'>
				<div class='icon'>
					<c:if test="${comparison.quantity != null}"><div class='main-item-quantity-indicator-container'><div class='main-item-quantity-indicator'>${comparison.quantity}</div></div></c:if>
					<img src='${comparison.icon}' border='0'/>
				</div>
				<div style='width:230px'>
					<span class='${comparison.quality}'>${comparison.name}</span>
					<div class='main-highlight' style='font-size:14px'>${comparison.itemClass}</div>
				</div>
			</div>
			<div>
				<p>${comparison.itemSlot}</p>
				<div class='item-popup-stats'>
					<c:if test="${comparison.dexpen != null}"><div class='item-popup-field' title='This is the percentage that the wearer`s dexterity will be reduced when making dexterity based rolls. Dexterity penalties stack.'>Dexterity penalty: <div class='main-item-subnote'>${comparison.dexpen}%</div></div></c:if>
					<c:if test="${comparison.strReq != null}"><div class='item-popup-field' title='This is the strength the wearer is required to have in order to equip this item.'>Strength requirement: <div class='main-item-subnote'>${comparison.strReq}</div></div></c:if>
					<c:if test="${comparison.requirements}"><br/><br/></c:if>
					<!-- Damage -->
					<c:if test="${comparison.weaponDamage != null}"><div class='item-popup-field'>Weapon damage: <div class='main-item-subnote'>${comparison.weaponDamage}</div></div></c:if>
					<c:if test="${comparison.weaponCriticalChance != null}"><div class='item-popup-field'> - Critical chance: <div class='main-item-subnote'>${comparison.weaponCriticalChance}% <span title='This is the total crit chance after your character`s crit chance has been taken into account.'>(${comparison.weaponCriticalChanceBuffed}%)</span></div></div></c:if>
					<c:if test="${comparison.weaponCriticalMultiplier != null}"><div class='item-popup-field'> - Critical hit multiplier: <div class='main-item-subnote'>${comparison.weaponCriticalMultiplier}x</div></div></c:if>
					<c:if test="${comparison.weaponDamageType != null}"><div class='item-popup-field'> - Damage Type: <div class='main-item-subnote'>${comparison.weaponDamageType}</div></div></c:if>
					<c:if test="${comparison.weaponDamageMax != null}"><div class='item-popup-field-summary' title='This handy summary shows you the max damage that the weapon is capable of, and the average damage the weapon will do over time.'> - (${comparison.weaponDamageMax} max dmg, ${comparison.weaponDamageAvg} avg dmg)</div></c:if>
					<!-- Defense -->
					<c:if test="${comparison.blockChance != null}"><div class='item-popup-field' title='This is the odds of this armor blocking a hit on the body part that the armor is meant to protect.'>Block chance: <div class='main-item-subnote'>${comparison.blockChance}%</div></div></c:if>
					<c:if test="${comparison.damageReduction != null}"><div class='item-popup-field' title='This is the maximum amount of damage that this armor will absorb if it successfully blocks a hit.'> - Damage reduction: <div class='main-item-subnote'>${comparison.damageReduction}</div></div></c:if>
					<c:if test="${comparison.blockBludgeoningCapability != null}"><div class='item-popup-field' title='This describes this armor`s ability to block bludgeoning attacks. Excellent increases the damage reduction by x2.'> - Block bludgeoning: <div class='main-item-subnote'>${comparison.blockBludgeoningCapability}</div></div></c:if>
					<c:if test="${comparison.blockPiercingCapability != null}"><div class='item-popup-field' title='This describes this armor`s ability to block piercing attacks. Excellent increases the damage reduction by x2.'> - Block piercing: <div class='main-item-subnote'>${comparison.blockPiercingCapability}</div></div></c:if>
					<c:if test="${comparison.blockSlashingCapability != null}"><div class='item-popup-field' title='This describes this armor`s ability to block slashing attacks. Excellent increases the damage reduction by x2.'> - Block slashing: <div class='main-item-subnote'>${comparison.blockSlashingCapability}</div></div></c:if>
					<!-- Misc -->
					<c:if test="${comparison.maxSpace != null}"><div class='item-popup-field' title='The amount of space inside this item for storing things.'>Storage space: <div class='main-item-subnote'>${comparison.maxSpace}</div></div></c:if>
					<c:if test="${comparison.maxWeight != null}"><div class='item-popup-field' title='The amount of weight this item can carry.'>Storage weight: <div class='main-item-subnote'>${comparison.maxWeight}</div></div></c:if>
					<c:if test="${comparison.weight != null}"><div class='item-popup-field' title='The item`s weight in grams.'>Weight:&nbsp;<div class='main-item-subnote'>${comparison.weight}</div></div></c:if>
					<c:if test="${comparison.space != null}"><div class='item-popup-field' title='The amount of space this item roughtly takes up when placed in a box in cubic centimeters.'>Space:&nbsp;<div class='main-item-subnote'>${comparison.space}</div></div></c:if>
					<c:if test="${comparison.warmth != null}"><div class='item-popup-field' title='The amount of warmth this item provides the wearer.'>Warmth: <div class='main-item-subnote'>${comparison.warmth} units</div></div></c:if>
					<c:if test="${comparison.durability != null}"><div class='item-popup-field' title='The number of uses this item still has left. Once it reaches 0, the item is destroyed.'>Durability: <div class='main-item-subnote'>${comparison.durability}</div></div></c:if>
				</div>
				<br/>
				<c:if test="${comparison.description != null}"><br/>
				<div class='item-flavor-description'>${comparison.description}</div></c:if>
			</div>
		</div>
		</c:forEach>
	</div>
	</c:if>
</div>