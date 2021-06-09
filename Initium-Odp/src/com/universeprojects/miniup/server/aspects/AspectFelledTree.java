package com.universeprojects.miniup.server.aspects;

import com.universeprojects.miniup.server.InitiumAspect;
import com.universeprojects.miniup.server.InitiumObject;

public class AspectFelledTree extends InitiumAspect
{

	public AspectFelledTree(InitiumObject object)
	{
		super(object);
	}

	@Override
	protected void initialize()
	{

	}

	@Override
	public Integer getVersion() {
		return 1;
	}


}
