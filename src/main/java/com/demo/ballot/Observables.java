package com.demo.ballot;

import java.util.HashMap;
import java.util.Map;

public class Observables 
{
	static private Observables OBSERVABLES = null;

	static private String secret = "173290aa-3501-37a9-a477-63fe8ab6d29d";
	
	final static private Map<String, Integer> candidates = new HashMap<>();

	final static public synchronized Observables getInstance() 
	{
		if (OBSERVABLES == null) 
		{
			OBSERVABLES = new Observables();

			initCandidates();
		}

		return OBSERVABLES;
	}

	private static final void initCandidates() 
	{
		candidates.put("Picolino", 0);
		candidates.put("Frajola", 0);
		candidates.put("Catatau", 0);
	}

	public Map<String, Integer> getCandidates() 
	{
		return candidates;
	}
	
	public static String getSecret() 
	{
		return secret;
	}
}
