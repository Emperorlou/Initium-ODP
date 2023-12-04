var currentUITutorial = null;
function UITutorial(id)
{
	this.debugMode = true;
	
	this.timer = null;
	
	this.id = id;
	
	this.chapters = [];
	this.chaptersMap = {};
	
	this.currentChapter = 0;
	this.currentStep = 0;
	
	this.log = function(text, obj) {
		if (this.debugMode == true) {
			console.log("ui-tutorial.js: " + text, obj);
		}
	}

	// Uses the builder pattern
	this.addStep = function(selector, title, text, multipart, activationSelector, deactivationSelector)
	{
		this.log("addStep called. ", [selector, title, text, multipart, activationSelector, deactivationSelector]);
		if (this.chapters.length==0)
		{
			var chapter = new UITutorialChapter(this.id);
			this.addChapter(chapter);
		}
		
		this.getCurrentChapter().addStep(selector, title, text, multipart, activationSelector, deactivationSelector);
		
		return this;
	}
	
	this.addChapter = function(chapter)
	{
		this.log("addChapter called. ", chapter);
		this.chapters.push(chapter);
		this.chaptersMap[chapter.id] = chapter;
	}
	
	
	this.run = function()
	{
		this.log("run called. ");
		if (currentUITutorial!=null)
			currentUITutorial.exitTutorial();
		
		currentUITutorial = this;

		this.currentChapter = 0;
		this.currentStep = -1;
		
		this._hideForCurrentChapter();
		
		var that = this;
		this.timer = setInterval(function(){that.update(that)}, 1000);
		
		this.update(this);
	}
	
	this.update = function(tutorial)
	{
		if (tutorial == null) return;
		
		// If the quest window is open, don't bother doing this yet
		if ($(".quest-window-container").length>0) return;
		if ($("body").hasClass("pace-done")==false) return;

		this.log("update running: ", tutorial);		
		
		var chapter = tutorial.getCurrentChapter();
		
		var stepToActivate = -1;
		for(var i = 0; i<chapter.steps.length; i++)
		{
			if (chapter.steps[i].multipart==true && this._isTutorialSeen(i, this.currentChapter)==false)
			{
				if (tutorial.currentStep<=i)
				{
					stepToActivate = i;
					break;
				}
			}
			else if (this._isTutorialStopped(i, this.currentChapter)!=true && chapter.steps[i].checkForActivation()==true)
				stepToActivate = i;
		}
		
		if (stepToActivate>tutorial.currentStep)
		{
			tutorial._hideAll();
			if (stepToActivate > -1)
				tutorial._showPopup(this.currentChapter, stepToActivate);
			else
				tutorial.currentStep = -1;
		}
	}
	
	
	this.isOnLastStep = function()
	{
		if (this.currentChapter>=this.chapters.length-1 && this.currentStep>=this.chapters[this.currentChapter].steps.length-1)
			return true;
		
		return false;
	}

	this.exitTutorial = function(event)
	{
		this.log("exitTutorial called: ", event);
				
		clearInterval(this.timer);
		this._showAll();
		$(".revealTutorial-ui").remove();
		
		if (event!=null)
			event.stopPropagation();
	}
	
	this.getCurrentChapter = function()
	{
		return this.chapters[this.currentChapter];
	}
	
	this.getCurrentStep = function()
	{
		return this.chapters[this.currentChapter].steps[this.currentStep];
	}
	
	this.getNextStep = function()
	{
		if (this.isOnLastStep()==true) return null;
		var chapter = this.chapters[this.currentChapter];
		if (this.currentStep>=chapter.getStepCount()-1)
			return null;
		
		var stepIndex = this.currentStep + 1;
		
		return this.chapters[this.currentChapter].steps[stepIndex];
	}
	
	this.nextStep = function()
	{
		this.log("nextStep called: ", event);
		
		if (this.isOnLastStep()==true) return false;
		var chapter = this.chapters[this.currentChapter];
		if (this.currentStep>=chapter.getStepCount()-1)
			return false;
		
		this.currentStep++;
		
		this._showPopup(this.currentChapter, this.currentStep);
		
		return true;
	}
	
	this.clearPopup = function()
	{
		$(".revealTutorial-ui").remove();
		
		this._markTutorialSeen();
	}
	
	
	
	
	this._isTutorialSeen = function(stepIndex, chapterIndex)
	{
		var id = this.id;
		var chapterId = this.getCurrentChapter().chapterId;
		if (chapterIndex==null)
			chapterId = this.chapters[this.currentChapter].chapterId;
		if (stepIndex==null)
			stepIndex = this.currentStep;

		if (chapterId==null || stepIndex==null || chapterIndex==-1 || stepIndex==-1) return false;
		
		
		var key = "UITutorialMarker-"+id+"-"+chapterId+"-"+stepIndex;
		//return false;
		return sessionStorage.getItem(key)=="seen";
	}
	
	this._isTutorialStopped = function(stepIndex, chapterIndex)
	{
		var id = this.id;
		var chapterId = this.getCurrentChapter().chapterId;
		if (chapterIndex==null)
			chapterId = this.chapters[this.currentChapter].chapterId;
		if (stepIndex==null)
			stepIndex = this.currentStep;

		if (chapterId==null || stepIndex==null || chapterIndex==-1 || stepIndex==-1) return false;
		
		
		var key = "UITutorialMarker-"+id+"-"+chapterId+"-"+stepIndex;
		//return false;
		return sessionStorage.getItem(key)=="stop";
	}
	
	this._markTutorialSeen = function(stepIndex, chapterIndex)
	{
		var id = this.id;
		var chapterId = this.getCurrentChapter().chapterId;
		if (chapterIndex==null)
			chapterId = this.chapters[this.currentChapter].chapterId;
		if (stepIndex==null)
			stepIndex = this.currentStep;

		if (chapterId==null || stepIndex==null || chapterIndex==-1 || stepIndex==-1) return;
		
		var key = "UITutorialMarker-"+id+"-"+chapterId+"-"+stepIndex;
		sessionStorage.setItem(key, "seen");
	}
	
	this._markTutorialStop = function(stepIndex, chapterIndex)
	{
		var id = this.id;
		var chapterId = this.getCurrentChapter().chapterId;
		if (chapterIndex==null)
			chapterId = this.chapters[this.currentChapter].chapterId;
		if (stepIndex==null)
			stepIndex = this.currentStep;

		if (chapterId==null || stepIndex==null || chapterIndex==-1 || stepIndex==-1) return;
		
		var key = "UITutorialMarker-"+id+"-"+chapterId+"-"+stepIndex;
		sessionStorage.setItem(key, "stop");
	}
	
	this._hideElement = function(selector)
	{
		$(selector).css("visibility", "hidden");
	}

	this._showElement = function(selector)
	{
		$(selector).css("visibility", "visible");
	}
	
	this._showPopup = function(chapterIndex, stepIndex)
	{
		this.clearPopup();
		
		if (this.isOnLastStep())
		{
			this._showAll();
			return;
		}
		
		if (chapterIndex>=this.chapters.length) return;
		var chapter = this.chapters[chapterIndex];
		if (stepIndex>=chapter.steps.length)
		{
			chapterIndex++;
			stepIndex = 0;
			this._showPopup(chapterIndex, stepIndex);
			return;
		}
		
		this._showForCurrentChapter();
		this.currentChapter = chapterIndex;
		this.currentStep = stepIndex;
		this._hideForCurrentChapter();
		
		var tutorialStep = chapter.steps[stepIndex];

		var element = null;
		var elementless = true;
		if (tutorialStep.targetSelector!=null)
		{
			element = $(tutorialStep.targetSelector);
			if (element.length>0 && element.is(":hidden")==false && element.is(":visible")!=false)
				elementless = false;
		}
		
		var jqWindow = $(window);
		var elementPos = {left:jqWindow.width()/2, top:50};
		var elementWidth = 50;
		var elementHeight = 50;
		
		if (elementless==false)
		{
			elementPos = element.offset();
			elementWidth = element.width();
			elementHeight = element.height();
		}
		var popupLeft = elementPos.left+(elementWidth/2)-(305/2);
		if (popupLeft>$(window).width()-305-16) popupLeft=$(window).width()-305-16;
		if (popupLeft<0) popupLeft = 0;
		var popupTop = elementPos.top+elementHeight+3;

		
		var uniqueId = chapterIndex+"_"+stepIndex;
		
		var nextJs = "currentUITutorial.clearPopup(); currentUITutorial.update();";
		
		var popupHtml = "<div onclick='"+nextJs+"' class='revealTutorial-blockout revealTutorial-ui'></div>";
		if (elementless==false)
			popupHtml+= "<div class='revealTutorial-highlight revealTutorial-ui' style='left:"+(element.offset().left)+"px;top:"+(element.offset().top)+"px;'></div><div class='revealTutorial-highlight-arrow revealTutorial-ui animated bounce' style='top:"+(popupTop)+"px;left:"+(elementPos.left+(elementWidth/2)-20)+"px;'></div>";
		popupHtml+="<div onclick='"+nextJs+"' class='cluetip ui-widget ui-widget-content ui-cluetip clue-right-rounded cluetip-rounded ui-corner-all revealTutorial-ui revealTutorial-popup v3-window3' id='rtPopup"+uniqueId+"' style='top:"+(popupTop+20)+"px; left:"+popupLeft+"px; position: absolute; z-index: 10000000;'>";
		if (tutorialStep.title!=null)
			popupHtml += "<h5>"+tutorialStep.title+"</h5>";
		if (tutorialStep.description!=null)
			popupHtml += tutorialStep.description;
		
		var nextButtonText = "Okay";
		if (this.isOnLastStep()) nextButtonText = "Okay";

		
		popupHtml += "<p style='height:20px;'><a onclick='currentUITutorial._markTutorialStop(); currentUITutorial.exitTutorial(event)' class='reveal-tutorial-nextButton' style='float:left'>Stop</a><a onclick='"+nextJs+"' class='reveal-tutorial-nextButton' style='float:right'>"+nextButtonText+"</a></p>";
		popupHtml += "</div>";
		
		popupHtml += "<script type='text/javascript'>";
		popupHtml += "$('.revealTutorial-highlight').click(function(){" +
				"$('"+tutorialStep.targetSelector+"').click();" +
				"currentUITutorial.clearPopup();" +
				"currentUITutorial.update();" +
				"});"
		popupHtml += "</script>";
		
		$("body").append(popupHtml);
		
	}

	this._hideForCurrentChapter = function()
	{
		if (this.chapters.length==0) return;
		
		var chapter = this.chapters[this.currentChapter];
		for(var elementIndex = 0; elementIndex<chapter.hideElementSelectors.length; elementIndex++)
		{
			var selector = chapter.hideElementSelectors[elementIndex];
			this._hideElement(selector);
		}
	}
	
	this._showForCurrentChapter = function()
	{
		if (this.chapters.length==0) return;
		
		var chapter = this.chapters[this.currentChapter];
		for(var elementIndex = 0; elementIndex<chapter.hideElementSelectors.length; elementIndex++)
		{
			var selector = chapter.hideElementSelectors[elementIndex];
			this._showElement(selector);
		}
	}
	
	this._hideAll = function()
	{
		// Go through each chapter and hide the elements they care about
		for(var chapterIndex = 0; chapterIndex<this.chapters.length; chapterIndex++)
		{
			var chapter = this.chapters[chapterIndex];
			for(var elementIndex = 0; elementIndex<chapter.hideElementSelectors.length; elementIndex++)
			{
				var selector = chapter.hideElementSelectors[elementIndex];
				this._hideElement(selector);
			}
		}
	}
	
	this._showAll = function()
	{
		// Go through each chapter and hide the elements they care about
		for(var chapterIndex = 0; chapterIndex<this.chapters.length; chapterIndex++)
		{
			var chapter = this.chapters[chapterIndex];
			for(var elementIndex = 0; elementIndex<chapter.hideElementSelectors.length; elementIndex++)
			{
				var selector = chapter.hideElementSelectors[elementIndex];
				this._showElement(selector);
			}
		}
	}

	
}


function UITutorialChapter(id, initialSteps)
{
	
	this.chapterId = id;
	
	this.hideElementSelectors = []; 
	this.steps = initialSteps;

	
	// "CONSTRUCTOR"
	{
		if (this.steps==null) this.steps = [];
	}
	
	
	this.addHiddenElement = function(selector)
	{
		this.hideElementSelectors.push(selector);
	}
	
	this.addStep = function(selector, title, text, multipart)
	{
		this.steps.push(new UITutorialStep(selector, title, text, multipart));
	}
	
	this.getStepCount = function()
	{
		this.steps.length;
	}
	
	
}

function UITutorialStep(targetSelector, title, text, multipart, activationSelector, deactivationSelector)
{
	this.targetSelector = targetSelector;
	this.title = title;
	this.description = text;
	this.multipart = multipart;
	this.activationSelector = activationSelector;
	this.deactivationSelector = deactivationSelector;
	
	this.checkForActivation = function()
	{
		if (multipart) return false;
		
		var selector = this.activationSelector;
		if (selector==null)
			selector = this.targetSelector;
		
		if (selector==null || $(selector).length>0)
			return true;
		
		return false;
	}
	
	
}


