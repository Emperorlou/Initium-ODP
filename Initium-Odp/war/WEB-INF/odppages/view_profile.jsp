<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>

<c:if test='${userId!=null}'>
<br>
<div class='smallbox donatebox'>
	<p
		title='This is how much donation credit you have to spend on things'>
		Available credit
		<br>
		${totalDonations}
	</p>
	<br>
	<p title='This is how much you`ve personally donated'>
		Total personal donations
		<br>
		${donationHistory}
	</p>
	<br>
	<form action="https://www.paypal.com/cgi-bin/webscr" method="post" target="_top">
		<input type='hidden' name='custom' value='${userId}' /> 
		<input type="hidden" name="cmd" value="_s-xclick"> 
		<input type="hidden" name="hosted_button_id" value="3XHAZVVPB3KH2">
		<input type="image" src="https://initium-resources.appspot.com/images/ui/paypal-donate-button.png" border="0" name="submit" alt="PayPal - The safer, easier way to pay online!"> 
		<img alt="" border="0" src="https://www.paypalobjects.com/en_US/i/scr/pixel.gif" width="1" height="1">
	</form>
	<img src='https://initium-resources.appspot.com/images/paypal-cards.jpg' border=0 />
	<div class='fineprint'>
		You <u>do not</u> need a PayPal account when you donate using credit or bank cards.
	</div>
	<br>
	<div class='fineprint'>PayPal takes 30 cents + 2.5% from every donation</div>
</div>
</c:if>

<h2>User Account</h2>
<p>You joined Initium on ${joinDate}.</p>
<p>
	<c:if test="${isVerified==false }">
		<div class='highlightbox-red'>You have not yet verified your email address. If you need to re-send the verification email, <a onclick='resendVerificationEmail()'>click here to re-send it now</a>.</div>
	</c:if>
</p>

<h4>Account options</h4>
<p>
	<a onclick='changeEmailAddress("${userEmail}")'>Change your account's email address</a>
</p>

<h4>Premium Membership</h4>
<c:if test="${premium!=true}">
	<div class='highlightbox-red'>You are using a FREE account. ANY donation (of at least 5 dollars) will upgrade your account to premium.</div>
	<p>Premium members enjoy the following benefits:</p>
</c:if>

<c:if test="${premium==true}">
	<div class='highlightbox-green'>You are using a PREMIUM account. Thank-you for supporting our development efforts and server costs!</div>
	<p>As a premium member, you enjoy the following benefits:</p>
</c:if>

<div class='paragraph'>
	<ul>
		<li>When you die, your body will be able to be 'rescued' within a certain time frame.</li>
		<li>If you bought a house, your character will not forget about it if he dies.</li>
		<li>Multiple character support! Easily switch between your characters.</li>
		<li>Your name will be gilded in a beautiful crimson red.</li>
	</ul>
	In the future, premium members will also have the following benefits:
	<ul>
		<li>Leave reviews for other characters and help moderate the server through a unique voting system.</li>
		<li>No banner ads! (not that there are any ads at the moment)</li>
	</ul>

	<hr>

	<h3>Give the gift of premium</h3>
	<p>
		Now you can give other players a premium account. All you have to do
		is have at least $10 is donations on your account and click the button
		below. $5 will be subtracted from your account and credited to the
		user account of your choosing.
		<br>
		<br> <a class='big-link' onclick='giftPremium()'>Gift a premium account to someone</a>
	</p>

	<hr>

	<h3>Trade using premium membership tokens</h3>
	<p>
		You can now turn a premium membership into an in-game item that you
		can trade with other players! With this token, you can grant an
		account premium membership, or you can gift a premium membership to
		another player. Please note that this token has no real-world value;
		the token cannot be converted to cash and taken out of the game. The
		token can only be used to gift premium membership to someone, or to
		give yourself a premium membership. 
		<br>
		<br> 
		All you have to do is have at least $10 is donations on your
		account and click the button below. $5 will be subtracted from your
		account and you will find a new premium membership token in your
		inventory. 
		<br>
		<br> 
		<a class='big-link' onclick='newPremiumToken()'>Create 1 premium membership token</a>
	</p>
</div>

<hr>

<div class='main-description'>
	<h2>Your Group</h2>
	<c:if test="${inGroup==false}">
		<p>
			You are not currently in a group. 
			<br>
			<br> 
			If you wish to join a group, you can find somebody who is
			already in a group and click on their name. Then click on the group
			that he is in to go to their group's join page. Once you have
			requested to join, the group administrator will have to approve or
			reject your application. 
			<br>
			<br> 
			<a class='big-link' href='#' onclick='createNewGroup()'>Create a new group</a>
		</p>
	</c:if>
	<c:if test="${inGroup==true}">
		<p>
			<c:if test="${groupStatus=='Applied'}">
				You have applied to ${groupName} and you are waiting for approval from a group admin.
				<br>
				<br>
				<a class='big-link' onclick='leaveGroup()'>Cancel Application</a>
			</c:if>
			<c:if test="${groupStatus=='Member' || groupStatus=='Admin'}">
				You are a <span class='main-highlight'>${groupStatus}</span> of the group <span class='main-highlight'>${groupName}</span>.
				<br>
				<br>
				<span class='main-highlight'><c:out value="${groupDescription}" /></span>
				<c:if test="${isLeavingGroup!=true}">
					<a class='big-link' href='#' onclick='leaveGroup()'>Leave this group</a>
					<br><br>
				</c:if>
				<c:if test="${isLeavingGroup==true}">
					<br>
					You are leaving the group but still have to wait <c:out value="${leavingGroupWaitTime}" />.
					<a class='big-link' onclick='cancelLeaveGroup()'>Cancel leaving group</a>
					<br><br>
				</c:if>
			</c:if>
			<a class='big-link' onclick='viewGroup(${groupId})'>View Group Page</a>
		</p>
	</c:if>

</div>

<hr>

<h2>Your Character</h2>
<p>
	<br> <br> <a class='big-link' href='#' onclick='deleteAndRecreateCharacter("${charName}")' id='btnNewCharacter'>Delete your character and recreate</a>
</p>

<c:if test="${premium==true}">
<p>
	<br> <br> <a class="big-link" href="#" onclick="rediscoverHouses(event)" id="btnRediscoverHouses">Rediscover All Owned Houses</a>
</p>
</c:if>
<div class='mobile-spacer'></div>