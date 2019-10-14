package com.universeprojects.miniup.server.dbentities;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Text;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.dbentities.QuestObjective.TutorialStep;

public class QuestObjective
{
	final public ODPDBAccess db;
	final public Integer index;
	final public QuestDefEntity questDef;
	final public EmbeddedEntity data;
	
	public QuestObjective(ODPDBAccess db, EmbeddedEntity data, QuestDefEntity parent, int objectiveIndex)
	{
		this.db = db;
		this.index = objectiveIndex;
		this.data = data;
		this.questDef = parent;
	}
	
	public QuestDefEntity getQuestDef()
	{
		return questDef;
	}
	
	public int getObjectiveIndex()
	{
		return index;
	}
	
	public String getName()
	{
		return (String)data.getProperty("name");
	}

	public boolean isComplete()
	{
		return GameUtils.booleanEquals(data.getProperty("complete"), true);
	}

	public Double getProgress()
	{
		return (Double)data.getProperty("progress");
	}
	
	public Double getProgressTotal()
	{
		return (Double)data.getProperty("progressTotal");
	}
	
	public List<List<String>> getNeededCharacterFieldValues()
	{
		return db.getValueFromFieldTypeFieldFilter2DCollection(data, "neededCharacterFieldValues");
	}
	public List<List<String>> getNeededInventoryItemFieldValues()
	{
		return db.getValueFromFieldTypeFieldFilter2DCollection(data, "neededInventoryItemFieldValues");
	}
	public List<List<String>> getNeededLocationFieldValues()
	{
		return db.getValueFromFieldTypeFieldFilter2DCollection(data, "neededLocationFieldValues");
	}
	
	public boolean isForceCompleteEntireQuest()
	{
		return GameUtils.equals(data.getProperty("forceCompleteEntireQuest"), true);
	}

	public List<TutorialStep> getTutorialSteps()
	{
		List<EmbeddedEntity> raw = (List<EmbeddedEntity>)data.getProperty("tutorialElements");
		if (raw==null || raw.isEmpty()) return null;
		
		List<TutorialStep> result = new ArrayList<>();
		for(int i = 0; i<raw.size(); i++)
		{
			result.add(new TutorialStep(raw.get(i)));
		}
		
		return result;
	}
	
	
	public void flagCompleted()
	{
		data.setProperty("complete", true);
	}

	public class TutorialStep
	{
		EmbeddedEntity data;
		public TutorialStep(EmbeddedEntity data)
		{
			this.data = data;
		}
		
		public String getName()
		{
			return (String)data.getProperty("name");
		}
		
		public String getDescription()
		{
			Text text = ((Text)data.getProperty("description"));
			if (text==null) return null;
			return text.getValue();
		}
		
		public String getElementHighlight()
		{
			return (String)data.getProperty("jqueryIdElementHighlight");
		}
		
		public String getVisibleTrigger()
		{
			return (String)data.getProperty("jqueryIdVisibleTrigger");
		}

		public String getVisibleUntrigger()
		{
			return (String)data.getProperty("jqueryIdVisibleUntrigger");
		}
		
		public boolean isMultipart()
		{
			return GameUtils.equals(data.getProperty("multipart"), true);
		}
		
		
		

		public Object generateJavascript()
		{
			StringBuilder sb = new StringBuilder();
			sb.append(".addStep('")
				.append(getElementHighlight()).append("', '")
				.append(WebUtils.jsSafe(getName())).append("', '")
				.append(WebUtils.jsSafe(getDescription().replace("\n", "<br>").replaceAll("\r", ""))).append("', ")
				.append(isMultipart());
			
			if (getVisibleTrigger()==null)
				sb.append(", null");
			else
				sb.append(", '").append(getVisibleTrigger()).append("'");

			if (getVisibleUntrigger()==null)
				sb.append(", null");
			else
				sb.append(", '").append(getVisibleUntrigger()).append("'");
			
			sb.append(")");
			return sb.toString();
		}
	}

	public String generateTutorialStepsJs()
	{
		List<TutorialStep> steps = getTutorialSteps();
		if (steps==null) return null;

		StringBuilder sb = new StringBuilder();
		sb.append("setTimeout(function(){new UITutorial('").append(WebUtils.jsSafe(getName())).append("')");
		for(TutorialStep step:steps)
		{
			sb.append(step.generateJavascript());
		}
		sb.append(".run();}, 2000);");
		return sb.toString();
	}
	
}
