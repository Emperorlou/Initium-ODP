var currentRevealTutorial = null;
function RevealTutorial()
{
	this.chapters = [];
	
	this.currentChapter = 0;
	this.currentStep = 0;
	
	this.addChapter = function(chapter)
	{
		this.chapters.push(chapter);
	}
	
	
	this._hideElement = function(selector)
	{
		$(selector).css("visibility", "hidden");
	}

	this._showElement = function(selector)
	{
		$(selector).css("visibility", "visible");
	}
	
	this.isOnLastStep = function()
	{
		if (this.currentChapter>=this.chapters.length-1 && this.currentStep>=this.chapters[this.currentChapter].steps.length-1)
			return true;
		
		return false;
	}

	this.exitTutorial = function(event)
	{
		this._showAll();
		$(".revealTutorial-ui").remove();
		
		event.stopPropagation();
	}
	
	this._showPopup = function(chapterIndex, stepIndex)
	{
		$(".revealTutorial-ui").remove();
		
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

		var elementless = true;
		if (tutorialStep.descriptionElementSelector!=null)
		{
			var element = $(tutorialStep.descriptionElementSelector);
			if (element.length>0)
				elementless = false;
		}
		
		var jqWindow = $(window);
		var elementPos = {left:jqWindow.width()/2, top:50};
		var elementWidth = 0;
		var elementHeight = 0;
		
		if (elementless==false)
		{
			elementPos = element.offset();
			elementWidth = element.width();
			elementHeight = element.height();
		}
		var popupLeft = elementPos.left+(elementWidth/2)-(320/2);
		if (popupLeft<0) popupLeft = 0;
		if (popupLeft>$(window).width()-320-16) popupLeft=$(window).width()-320-16;
		var popupTop = elementPos.top+elementHeight+3;

		
		var uniqueId = chapterIndex+"_"+stepIndex;
		
		var nextJs = "currentRevealTutorial._showPopup("+chapterIndex+","+(stepIndex+1)+")";
		
		var popupHtml = "<div onclick='"+nextJs+"' class='revealTutorial-blockout revealTutorial-ui'></div>";
		if (elementless==false)
			popupHtml+= "<div class='revealTutorial-highlight revealTutorial-ui' style='left:"+(elementPos.left)+"px;top:"+(elementPos.top)+"px;width:"+elementWidth+"px;height:"+elementHeight+"px;'></div><div class='revealTutorial-highlight-arrow revealTutorial-ui animated bounce' style='top:"+(popupTop)+"px;left:"+(elementPos.left+(elementWidth/2)-20)+"px;'></div>";
		popupHtml+="<div onclick='"+nextJs+"' class='cluetip ui-widget ui-widget-content ui-cluetip clue-right-rounded cluetip-rounded ui-corner-all revealTutorial-ui revealTutorial-popup' id='rtPopup"+uniqueId+"' style='top:"+(popupTop+20)+"px; left:"+popupLeft+"px; position: absolute; z-index: 10000000;'>";
		if (tutorialStep.title!=null)
			popupHtml += "<h5>"+tutorialStep.title+"</h5>";
		if (tutorialStep.description!=null)
			popupHtml += tutorialStep.description;
		
		var nextButtonText = "Next";
		if (this.isOnLastStep()) nextButtonText = "Finish";

		
		popupHtml += "<p style='height:20px;'><a onclick='currentRevealTutorial.exitTutorial(event)' class='reveal-tutorial-nextButton' style='float:left'>Exit</a><a onclick='"+nextJs+"' class='reveal-tutorial-nextButton' style='float:right'>"+nextButtonText+"</a></p>";
		popupHtml += "</div>";
		
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
	
	this.run = function()
	{
		currentRevealTutorial = this;

		if (this.isOnLastStep())
		{
			this.currentChapter = 0;
			this.currentStep = 0;
		}		
		
		// Now trigger the first popup
		this._showPopup(0,0);
		
		this._hideForCurrentChapter();
	}
	
}


function RevealTutorialChapter(id, initialSteps)
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
	
	this.addStep = function(selector, title, text)
	{
		this.steps.push(new RevealTutorialStep(selector, title, text));
	}
	
	
}

function RevealTutorialStep(selector, title, text)
{
	this.title = title;
	this.description = text;
	this.descriptionElementSelector = selector;
}