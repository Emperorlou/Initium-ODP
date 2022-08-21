<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<div class="item-popup">
	<input type='hidden' id='popupItemId' value='${item.itemId}'/>
	
	<c:if test="${item.largeImage != null && item.largeImage != ''}">
		<div class='item-popup-largeimage' style='background-image:url(${item.largeImage})'></div>
	</c:if>
	
	<c:if test="${isItemOwner}"><a href='#' onclick='customizeItemOrderPage(${item.itemId})' style='float:left'>Customize</a></c:if>
	<div class='normal-container'>
		<a onclick='shareItem(${item.itemId})' style='float:right' title='Clicking this will show the item in chat in the location you`re currently in.'>Share</a>
		<br/>
		<br/>
		<c:if test="${item.isContainer}"><p class='main-item-controls' style='display:block;margin:5px;'><a onclick='doSetLabel(event, ${item.itemId})' style='font-size:13px;'>Rename</a></p></c:if>
		<div class='item-popup-header'>
			<div class='icon'>
				<c:if test="${item.quantity != null}"><div name='itemQuantity' class='main-item-quantity-indicator-container'><div class='main-item-quantity-indicator'>${item.quantity}</div></div></c:if>
		
				<c:if test="${item.isGridMapObjectImage==true}">
					<img src='${item.icon}' style='max-width:32px; max-height:48px;' border='0'/>
				</c:if>
				<c:if test="${item.isGridMapObjectImage==false}">
					<img src='${item.icon}' border='0'/>
				</c:if>
		
			</div>
			<div style='width:230px'>
				<span name='itemName' class='${item.quality}'>${item.name}</span>
				<div name='itemClass' class='main-highlight' style='font-size:14px'>${item.itemClass}</div>
			</div>
			<div>
				<c:forEach items="${item.slots}" var="slot">
					<c:if test="${slot.slotIsEmpty == true}"><div class='socket-base' minitip='${slot.slotName} &lt;br&gt; ${slot.slotTooltip}'></div></c:if>
					<c:if test="${slot.slotIsEmpty == false}"><div class='socket-base' minitip='${slot.slotName} &lt;br&gt; ${slot.slotModifierText}'><div class='socket-item' style='background-image:url(${slot.slotIcon})'></div></div></c:if>
				</c:forEach>
			</div>
			<div>
			<c:if test="${item.buffs==true}">
				<div class='buff-pane' onclick='$(this).hide(); $("#buffsList${item.itemId}").show();'>
				${item.printBuff}
				</div>
				<div id='buffsList${item.itemId}' style='display:none;'>
				<h5>Buffs/debuffs</h5>
				${item.buffList}
				</div>
			</c:if>
			</div>
		</div>
		<div>
			<p>${item.itemSlot}</p>
			<div class='item-popup-stats'>
				<c:if test="${item.strReq != null}"><div name='strengthRequirement' class='item-popup-field' title='This is the strength the wearer is required to have in order to equip this item.'>Strength requirement: <div class='main-item-subnote'>${item.strReq}</div></div>${statComp.strReq}<br/></c:if>
				<c:if test="${item.dexpen != null}"><div name='dexterityPenalty' class='item-popup-field' title='This is the percentage that the wearer`s dexterity will be reduced when making dexterity based rolls. Dexterity penalties stack.'>Dexterity penalty: <div class='main-item-subnote'>${item.dexpen}%</div></div>${statComp.dexpen}<br/></c:if>
				<c:if test="${item.strmod != null}"><div name='strengthModifier' class='item-popup-field' title='This is the percentage that the wearer`s strength will be modified when making strength based rolls. Strength modifiers stack.'>Strength modifier: <div class='main-item-subnote'>${item.strmod}%</div></div>${statComp.strmod}<br/></c:if>
				<c:if test="${item.intmod != null}"><div name='intelligenceModifier' class='item-popup-field' title='This is the percentage that the wearer`s intelligence will be modified when making intelligence based rolls. Intelligence modifiers stack.'>Intelligence modifier: <div class='main-item-subnote'>${item.intmod}%</div></div>${statComp.intmod}<br/></c:if>
				<c:if test="${item.requirements}"><br/></c:if>

				<!-- Tool stats -->
				<c:if test="${item.EaseOfUse != null}"><div name='attributeEaseOfUse' class='item-popup-field' title='This stat is used to compare how easy it is to use this tool compared to other tools that are similar.'>Ease of Use: <div class='main-item-subnote'>${item.EaseOfUse}</div></div>${statComp.EaseOfUse}<br/></c:if>
				<c:if test="${item.Strength != null}"><div name='attributeStrength' class='item-popup-field' title='This stat is used to compare how strong this item is compared to others that are similar.'>Strength: <div class='main-item-subnote'>${item.Strength}</div></div>${statComp.Strength}<br/></c:if>
				<c:if test="${item.Reliability != null}"><div name='attributeReliability' class='item-popup-field' title='This stat is used to compare how reliable this item is compared to others that are similar.'>Reliability: <div class='main-item-subnote'>${item.Reliability}</div></div>${statComp.Reliability}<br/></c:if>
				<c:if test="${item.Efficiency != null}"><div name='attributeEfficiency' class='item-popup-field' title='This stat is used to compare how efficient this item is at doing it`s job compared to others that are similar.'>Efficiency: <div class='main-item-subnote'>${item.Efficiency}</div></div>${statComp.Efficiency}<br/></c:if>
				<c:if test="${item.Complexity != null}"><div name='attributeComplexity' class='item-popup-field' title='This stat is used to compare how complex this item is compared to others that are similar. Generally lower complexity is better.'>Complexity: <div class='main-item-subnote'>${item.Complexity}</div></div>${statComp.Complexity}<br/></c:if>
				<c:if test="${item.Precision != null}"><div name='attributePrecision' class='item-popup-field' title='This stat is used to compare how precise this tool is compared to others that are similar.'>Precision: <div class='main-item-subnote'>${item.Precision}</div></div>${statComp.Precision}<br/></c:if>
				<c:if test="${item.Beauty != null}"><div name='attributeBeauty' class='item-popup-field' title='This is a stat for how beautiful this item is to behold.'>Beauty: <div class='main-item-subnote'>${item.Beauty}</div></div>${statComp.Beauty}<br/></c:if>
				<c:if test="${item.hasToolStats}"><br/></c:if>
				
				<!-- Damage -->
				<c:if test="${item.weaponDamage != null}"><div name='weaponDamage' class='item-popup-field'>Weapon damage: <div class='main-item-subnote'>${item.weaponDamage}</div></div></c:if>
				<c:if test="${item.weaponCriticalChance != null}"><div name='weaponDamageCriticalChance' class='item-popup-field'> - Critical chance: <div class='main-item-subnote'>${item.weaponCriticalChance}% <span title='This is the total crit chance after your character`s crit chance has been taken into account.'>(${item.weaponCriticalChanceBuffed}%)</span></div></div>${statComp.weaponCriticalChance}<br/></c:if>
				<c:if test="${item.weaponCriticalMultiplier != null}"><div name='weaponDamageCriticalMultiplier' class='item-popup-field'> - Critical hit multiplier: <div class='main-item-subnote'>${item.weaponCriticalMultiplier}x</div></div>${statComp.weaponCriticalMultiplier}<br></c:if>
				<c:if test="${item.weaponDamageType != null}"><div name='weaponDamageType' class='item-popup-field'> - Damage Type: <div class='main-item-subnote'>${item.weaponDamageType}</div></div></c:if>
				<c:if test="${item.weaponDamageMax != null}"><div name='weaponDamageSummary' class='item-popup-field-summary' title='This handy summary shows you the max damage that the weapon is capable of, and the average damage the weapon will do over time.'> - (${item.weaponDamageMax} max damage)</div>${statComp.weaponDamageMax}</c:if>
				<c:if test="${item.weaponDamageMax != null}"><div name='weaponDamageSummary' class='item-popup-field-summary' title='This handy summary shows you the max damage that the weapon is capable of, and the average damage the weapon will do over time.'> - (${item.weaponDamageAvg} average damage)</div>${statComp.weaponDamageAvg}</c:if>
				<!-- Defense -->
				<c:if test="${item.blockChance != null}"><div name='blockChance' class='item-popup-field' title='This is the odds of this armor blocking a hit on the body part that the armor is meant to protect.'>Block chance: <div class='main-item-subnote'>${item.blockChance}%</div></div>${statComp.blockChance}</c:if>
				<c:if test="${item.damageReduction != null}"><div name='damageReduction' class='item-popup-field' title='This is the maximum amount of damage that this armor will absorb if it successfully blocks a hit.'> - Damage reduction: <div class='main-item-subnote'>${item.damageReduction}</div></div>${statComp.damageReduction}</c:if>
				<c:if test="${item.blockBludgeoningCapability != null}"><div name='blockBludgeoningCapability' class='item-popup-field' title='This describes this armor`s ability to block bludgeoning attacks. Excellent increases the damage reduction by x2.'> - Block bludgeon: <div class='main-item-subnote'>${item.blockBludgeoningCapability}</div></div></c:if>
				<c:if test="${item.blockPiercingCapability != null}"><div name='blockPiercingCapability' class='item-popup-field' title='This describes this armor`s ability to block piercing attacks. Excellent increases the damage reduction by x2.'> - Block piercing: <div class='main-item-subnote'>${item.blockPiercingCapability}</div></div></c:if>
				<c:if test="${item.blockSlashingCapability != null}"><div name='blockSlashingCapability' class='item-popup-field' title='This describes this armor`s ability to block slashing attacks. Excellent increases the damage reduction by x2.'> - Block slashing: <div class='main-item-subnote'>${item.blockSlashingCapability}</div></div></c:if>
				<!-- Misc -->
				<c:if test="${item.maxSpace != null}"><div name='maxSpace' class='item-popup-field' title='The amount of space inside this item for storing things.'>Storage space: <div class='main-item-subnote'>${item.maxSpace}</div></div>${statComp.maxSpace}</c:if>
				<c:if test="${item.maxWeight != null}"><div name='maxWeight' class='item-popup-field' title='The amount of weight this item can carry.'>Storage weight: <div class='main-item-subnote'>${item.maxWeight}</div></div>${statComp.maxWeight}</c:if>
				<c:if test="${item.weight != null}"><div name='weight' class='item-popup-field' title='The item`s weight in grams.'>Weight:&nbsp;<div class='main-item-subnote'>${item.weight}</div></div>${statComp.weight}</c:if>
				<c:if test="${item.space != null}"><div name='space' class='item-popup-field' title='The amount of space this item roughtly takes up when placed in a box in cubic centimeters.'>Space:&nbsp;<div class='main-item-subnote'>${item.space}</div></div>${statComp.space}</c:if>
				<c:if test="${item.warmth != null}"><div name='warmth' class='item-popup-field' title='The amount of warmth this item provides the wearer.'>Warmth: <div class='main-item-subnote'>${item.warmth} units</div></div>${statComp.warmth}</c:if>
				<c:if test="${item.durability != null}"><div name='durability' class='item-popup-field' title='The number of uses this item still has left. Once it reaches 0, the item is destroyed.'>Durability: <div class='main-item-subnote'>${item.durability}</div></div>${statComp.durability}</c:if>
				<c:if test="${item.charges != null}"><div name='charges' class='item-popup-field' title='The number of activations this item has. Once this reaches 0, the item will no longer be able to be activated.'>Charges: <div class='main-item-subnote'>${item.charges}</div></div></c:if>
				<c:if test="${item.aspectList != null}"><div class='simple-aspect-list'>${item.aspectList}</div></c:if>
			</div>
			<c:if test="${item.modifiers!=null}">
				<br>
				<div name='modifiers' class='item-modifiers'>
					<c:forEach items="${item.modifiers}" var="modifier">
						<div>${modifier}</div>
					</c:forEach>
				</div>
			</c:if>
			<br/>
			<br/>
			<div name='description' class='item-flavor-description'>${item.description}</div>
		</div>
		<c:if test="${item.reachableHtml != null}"><div>${item.reachableHtml}</div></c:if>
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
	<c:if test="${newQuest && questComplete!=true}">
	<div class='normal-container'>
		<p>
		<a onclick='beginQuest(event, ${item.itemId})'>Begin Quest</a><br>
		There is a quest associated with this item. Click 'Begin Quest' to do this quest.
		</p>
	</div>
	</c:if>
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
		If you already have a premium membership, 500 credits will be added towards your donation credit 
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
	<c:if test="${showRelatedSkills}">
	<div id='related-skills' class='normal-container'>
	</div>
	</c:if>
	
	<div style='text-align:center'>
		<c:if test="${itemKey!=null }">
			<a onclick='doExperiment(event, "${itemKey}")' minitip='Experiment on this item'><img src='https://initium-resources.appspot.com/images/ui/tab-experiments-button.png'/></a>
		</c:if>
		<c:if test="${proceduralKey!=null }">
			<a onclick='doExperiment(event, "${proceduralKey}")' minitip='Experiment on this item'><img src='https://initium-resources.appspot.com/images/ui/tab-experiments-button.png'/></a>
		</c:if>
		<a onclick='loadRelatedSkills("${itemKey}")' minitip='Show skills that you have that related to this item'><img src='https://initium-resources.appspot.com/images/ui/invention1.png'/></a>
		<c:if test="${comparisons != null}">
			<span id='item-comparison-link'><a onclick='$("#item-comparisons").show(); $("#item-comparison-link").hide();' minitip='Compare with existing equipment'><img src='https://initium-resources.appspot.com/images/ui/compare-equipment1.png'/></a></span>
		</c:if>
		<c:if test="${reachable}">
		    <a onclick='addItemFilter(event, "${item.itemId}")' minitip='Flag items with this name and rarity(or lower) to be dropped automatically.'><img src='https://initium-resources.appspot.com/images/ui/storefrontDisabled.png'/></a>
		</c:if>
	</div>
	
	<c:if test="${comparisons != null}">
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
					<c:if test="${comparison.strReq != null}"><div class='item-popup-field' title='This is the strength the wearer is required to have in order to equip this item.'>Strength requirement: <div class='main-item-subnote'>${comparison.strReq}</div></div><br/><br/></c:if>
					<c:if test="${comparison.dexpen != null}"><div class='item-popup-field' title='This is the percentage that the wearer`s dexterity will be reduced when making dexterity based rolls. Dexterity penalties stack.'>Dexterity penalty: <div class='main-item-subnote'>${comparison.dexpen}%</div></div></c:if>
					<c:if test="${comparison.strmod != null}"><div class='item-popup-field' title='This is the percentage that the wearer`s strength will be modified when making strength based rolls. Strength modifiers stack.'>Strength modifier: <div class='main-item-subnote'>${comparison.strmod}%</div></div></c:if>
					<c:if test="${comparison.intmod != null}"><div class='item-popup-field' title='This is the percentage that the wearer`s intelligence will be modified when making intelligence based rolls. Intelligence modifiers stack.'>Intelligence modifier: <div class='main-item-subnote'>${comparison.intmod}%</div></div></c:if>
					<c:if test="${comparison.requirements}"><br/><br/></c:if>

					<!-- Tool stats -->
					<c:if test="${comparison.EaseOfUse != null}"><div class='item-popup-field' title='This stat is used to compare how easy it is to use this tool compared to other tools that are similar.'>Ease of Use: <div class='main-item-subnote'>${comparison.EaseOfUse}</div></div><br/></c:if>
					<c:if test="${comparison.Strength != null}"><div class='item-popup-field' title='This stat is used to compare how strong this item is compared to others that are similar.'>Strength: <div class='main-item-subnote'>${comparison.Strength}</div></div><br/></c:if>
					<c:if test="${comparison.Reliability != null}"><div class='item-popup-field' title='This stat is used to compare how reliable this item is compared to others that are similar.'>Reliability: <div class='main-item-subnote'>${comparison.Reliability}</div></div><br/></c:if>
					<c:if test="${comparison.Efficiency != null}"><div class='item-popup-field' title='This stat is used to compare how efficient this item is at doing it`s job compared to others that are similar.'>Efficiency: <div class='main-item-subnote'>${comparison.Efficiency}</div></div><br/></c:if>
					<c:if test="${comparison.Complexity != null}"><div class='item-popup-field' title='This stat is used to compare how complex this item is compared to others that are similar. Generally lower complexity is better.'>Complexity: <div class='main-item-subnote'>${comparison.Complexity}</div></div><br/></c:if>
					<c:if test="${comparison.Precision != null}"><div class='item-popup-field' title='This stat is used to compare how precise this tool is compared to others that are similar.'>Precision: <div class='main-item-subnote'>${comparison.Precision}</div></div><br/></c:if>
					<c:if test="${comparison.Beauty != null}"><div class='item-popup-field' title='This is a stat for how beautiful this item is to behold.'>Beauty: <div class='main-item-subnote'>${comparison.Beauty}</div></div><br/></c:if>
					<c:if test="${comparison.hasToolStats}"><br/></c:if>
					
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
				<c:if test="${comparison.modifiers!=null}">
					<br>
					<div name='modifiers' class='item-modifiers'>
						<c:forEach items="${comparison.modifiers}" var="modifier">
							<div>${modifier}</div>
						</c:forEach>
					</div>
				</c:if>
				<br/>
				<c:if test="${comparison.description != null}"><br/>
					<div class='item-flavor-description'>${comparison.description}</div>
				</c:if>
			</div>
		</div>
		</c:forEach>
	</div>
	</c:if>
</div>
