package com.universeprojects.miniup.server.domain;

import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

//When a user signs up, they get one and only one of these.
public class User extends OdpDomain {
	public static final String KIND = "User";

	public User() {
		super(new CachedEntity(KIND));
	}

	public User(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	// We may periodically require a player to answer a question as a break from regular gameplay. This is done to try to make it harder to bot in the game.
	public void setBotCheck(Boolean botCheck) {
		getCachedEntity().setProperty("botCheck", botCheck);
	}

	public Boolean getBotCheck() {
		return (Boolean) getCachedEntity().getProperty("botCheck");
	}

	// The number of email bounces we found when trying to send emails to this users email address.
	public void setBounces(Long bounces) {
		getCachedEntity().setProperty("bounces", bounces);
	}

	public Long getBounces() {
		return (Long) getCachedEntity().getProperty("bounces");
	}

	// (Character)
	public void setCharacterKey(Key characterKey) {
		getCachedEntity().setProperty("characterKey", characterKey);
	}

	public Key getCharacterKey() {
		return (Key) getCachedEntity().getProperty("characterKey");
	}

	// (Character|userKey)
	public void setCharacters(List<Character> characters) {
		getCachedEntity().setProperty("characters", characters);
	}

	@SuppressWarnings("unchecked")
	public List<Character> getCharacters() {
		return (List<Character>) getCachedEntity().getProperty("characters");
	}

	// Have we sent a confirmation email for this user yet?
	public void setConfirmationSent(Boolean confirmationSent) {
		getCachedEntity().setProperty("confirmationSent", confirmationSent);
	}

	public Boolean getConfirmationSent() {
		return (Boolean) getCachedEntity().getProperty("confirmationSent");
	}

	public void setCreatedDate(Date createdDate) {
		getCachedEntity().setProperty("createdDate", createdDate);
	}

	public Date getCreatedDate() {
		return (Date) getCachedEntity().getProperty("createdDate");
	}

	// This is ACTUALLY the total donations this user has personally given through paypal, in cents. The 'totalDonations' is in fact 'availableDonations' but I can't change the field name anymore.
	public void setDonationHistory(Long donationHistory) {
		getCachedEntity().setProperty("donationHistory", donationHistory);
	}

	public Long getDonationHistory() {
		return (Long) getCachedEntity().getProperty("donationHistory");
	}

	public void setEmail(String email) {
		getCachedEntity().setProperty("email", email);
	}

	public String getEmail() {
		return (String) getCachedEntity().getProperty("email");
	}

	// If this is set to true, then the user's online status will not be displayed on other players' friends lists.
	public void setHideUserActivity(Boolean hideUserActivity) {
		getCachedEntity().setProperty("hideUserActivity", hideUserActivity);
	}

	public Boolean getHideUserActivity() {
		return (Boolean) getCachedEntity().getProperty("hideUserActivity");
	}

	public void setIsBotter(Boolean isBotter) {
		getCachedEntity().setProperty("isBotter", isBotter);
	}

	public Boolean getIsBotter() {
		return (Boolean) getCachedEntity().getProperty("isBotter");
	}

	// (Path|ownerKey)
	public void setOwnedPaths(List<Path> ownedPaths) {
		getCachedEntity().setProperty("ownedPaths", ownedPaths);
	}

	@SuppressWarnings("unchecked")
	public List<Path> getOwnedPaths() {
		return (List<Path>) getCachedEntity().getProperty("ownedPaths");
	}

	public void setPremium(Boolean premium) {
		getCachedEntity().setProperty("premium", premium);
	}

	public Boolean getPremium() {
		return (Boolean) getCachedEntity().getProperty("premium");
	}

	public void setReferralDonations(Integer referralDonations) {
		getCachedEntity().setProperty("referralDonations", referralDonations);
	}

	public Integer getReferralDonations() {
		return (Integer) getCachedEntity().getProperty("referralDonations");
	}

	public void setReferralSignups(Integer referralSignups) {
		getCachedEntity().setProperty("referralSignups", referralSignups);
	}

	public Integer getReferralSignups() {
		return (Integer) getCachedEntity().getProperty("referralSignups");
	}

	public void setReferralViews(Integer referralViews) {
		getCachedEntity().setProperty("referralViews", referralViews);
	}

	public Integer getReferralViews() {
		return (Integer) getCachedEntity().getProperty("referralViews");
	}

	public void setReferrerHeaderEntry(String referrerHeaderEntry) {
		getCachedEntity().setProperty("referrerHeaderEntry", referrerHeaderEntry);
	}

	public String getReferrerHeaderEntry() {
		return (String) getCachedEntity().getProperty("referrerHeaderEntry");
	}

	// The user that referred this user to Initium.
	public void setReferrerKey(Key referrerKey) {
		getCachedEntity().setProperty("referrerKey", referrerKey);
	}

	public Key getReferrerKey() {
		return (Key) getCachedEntity().getProperty("referrerKey");
	}

	// User is subscribed to the newsletter or not.
	public void setSubscribe(Boolean subscribe) {
		getCachedEntity().setProperty("subscribe", subscribe);
	}

	public Boolean getSubscribe() {
		return (Boolean) getCachedEntity().getProperty("subscribe");
	}

	// I can't rename this field but if I could I would call it availableDonations. This is the total available donation credit this user has to use for different donation rewards.
	public void setTotalDonations(Long totalDonations) {
		getCachedEntity().setProperty("totalDonations", totalDonations);
	}

	public Long getTotalDonations() {
		return (Long) getCachedEntity().getProperty("totalDonations");
	}

	// The name of the character that we want to accept. This is part of the character transfer service.
	public void setTransferCharacterName(String transferCharacterName) {
		getCachedEntity().setProperty("transferCharacterName", transferCharacterName);
	}

	public String getTransferCharacterName() {
		return (String) getCachedEntity().getProperty("transferCharacterName");
	}

	// If this user has ever used the custom orders feature, this will be true.
	public void setUsedCustomOrders(Boolean usedCustomOrders) {
		getCachedEntity().setProperty("usedCustomOrders", usedCustomOrders);
	}

	public Boolean getUsedCustomOrders() {
		return (Boolean) getCachedEntity().getProperty("usedCustomOrders");
	}

	public void setUsername(String username) {
		getCachedEntity().setProperty("username", username);
	}

	public String getUsername() {
		return (String) getCachedEntity().getProperty("username");
	}

	public void setVerified(Boolean verified) {
		getCachedEntity().setProperty("verified", verified);
	}

	public Boolean getVerified() {
		return (Boolean) getCachedEntity().getProperty("verified");
	}

}
