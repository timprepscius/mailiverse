/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import core.util.Comparators;
import core.util.FastRandom;
import core.util.LogNull;
import core.util.LogOut;
import core.util.Pair;
import core.util.Strings;

public class Dictionary implements Serializable
{
	private static final long serialVersionUID = 1L;
	static LogNull log = new LogNull(Dictionary.class);
	static FastRandom random = new FastRandom();
	
	public Map<String, Integer> vocabulary;
	int bayesianSize=0;
	
	public Dictionary ()
	{
		vocabulary = new HashMap<String, Integer>();
	}
	
	public Dictionary (String filter)
	{
		this();
		
		add(filter);
	}
	
	public Dictionary (Mail mail)
	{
		this();
		
		add (mail);
	}
	
	public Map<String, Integer> getVocabulary ()
	{
		return vocabulary;
	}
	
	final String TOKENS = " \t\r\n!@#$%^&*()_+-=`~{}[]\\|;:'\",./<>?";
	
	public Dictionary add(String text)
	{
		if (text != null)
		{
			StringTokenizer st = new StringTokenizer(text,TOKENS);
			while (st.hasMoreTokens())
			{
				String token = st.nextToken().toLowerCase();
				
				int occurences = 0;
				
				if (vocabulary.containsKey(token))
					occurences = vocabulary.get(token);

				bayesianSize ++;
				vocabulary.put(token, new Integer(occurences+1));
			}
		}
		
		return this;
	}
	
	public Dictionary add (Mail mail)
	{
		if (mail.getHeader().getAuthor()!=null)
			add (mail.getHeader().getAuthor().toString());
		
		if (mail.getHeader().getRecipients()!=null)
			for (Identity i : mail.getHeader().getRecipients().getAll())
				add(i.toString());
		
		add (mail.getBody().getText());
		add (mail.getHeader().getSubject());
		
		log.debug(this, "after add", toSerializableString());
		
		return this;
	}
	
	public boolean matches (Dictionary filter)
	{
		final String Q = "\"";

		for (Entry<String, Integer> i : filter.vocabulary.entrySet())
		{
			String match = i.getKey();
			
			boolean exact = (match.startsWith(Q) && match.endsWith(Q));
		
			if (exact)
			{
				match = match.substring(1, match.length()-1);
				log.debug("match is surrounded by quotes, using exact match:",match);
			}
			
			if (!vocabulary.containsKey(match))
			{
				if (exact)
					return false;
				
				boolean found = false;
				for (Entry<String, Integer> j : vocabulary.entrySet())
				{
					if (j.getKey().startsWith(match))
					{
						found = true;
						break;
					}
				}	
				if (!found)
					return false;
			}
		}
		
		return true;
	}
	
	public String toSerializableString()
	{
		String[] strings = new String[vocabulary.size()];
		
		int j=0;
		for (Entry<String, Integer> i : vocabulary.entrySet())
		{
			strings[j++] = i.getKey() + ":" +  i.getValue();
		}
		
		return Strings.concat(strings, ",");
	}

	public void fromSerializableString(String string)
	{
		String[] strings = string.split(",");
		
		for (String i : strings)
		{
			if (i.isEmpty())
				continue;
			
			try
			{
				String[] split = i.split(":");
				int occurences = Integer.parseInt(split[1]);
				
				bayesianSize += occurences;
				vocabulary.put(split[0], occurences);
			}
			catch (Exception e)
			{
				log.exception(e);
				continue;
			}
		}
	}

	public void add (Dictionary dictionary)
	{
		for (Entry<String, Integer> i : dictionary.vocabulary.entrySet())
		{
			int occurences = 0;
			String token = i.getKey();
			
			if (vocabulary.containsKey(i))
				occurences = vocabulary.get(token);

			bayesianSize += i.getValue();
			vocabulary.put(token, new Integer(occurences+i.getValue()));
		}
		
		bayesianPrune();
	}
	
	public void subtract(Dictionary dictionary) 
	{
		for (Entry<String, Integer> i : dictionary.vocabulary.entrySet())
		{
			int occurences = 0;
			String token = i.getKey();
			
			if (vocabulary.containsKey(i))
				occurences = vocabulary.get(token);

			bayesianSize -= i.getValue();
			vocabulary.put(token, new Integer(occurences-i.getValue()));
		}

		bayesianPrune();
	}
	
	protected float bayesianProbabilityOfTerm (String term)
	{
		Integer v = vocabulary.get(term);
		if (v == null)
			return 0.0f;
		
		log.trace(this, "bayesianProbabilityOfTerm", term, v, "+1 /", bayesianSize);
		
		return (float)(v + 1)/(float)bayesianSize;
	}
	
	public float bayesianProbability (Dictionary match)
	{
		float probability = 0.0f;
		for (Entry<String, Integer> i : match.vocabulary.entrySet())
		{
//			probability += (float)i.getValue() * bayesianProbabilityOfTerm(i.getKey());
			probability += bayesianProbabilityOfTerm(i.getKey());
		}
		
		return probability;
	}
	
	void bayesianPrune ()
	{
		List<Pair<String, Integer>> remove = new ArrayList<Pair<String,Integer>>();
		
		// remove all negative and zero values
		for (Entry<String, Integer> i : vocabulary.entrySet())
		{
			if (i.getValue() <= 0)
				remove.add(new Pair<String, Integer>(i.getKey(), i.getValue()));
		}
		
		for (Pair<String, Integer> i : remove)
		{
			bayesianSize -= i.second;
			vocabulary.remove(i.first);
		}
		
		remove.clear();
		
		// remove some parts of the remaining
		for (Entry<String, Integer> i : vocabulary.entrySet())
			remove.add(new Pair<String, Integer>(i.getKey(), i.getValue()));
		
		Collections.sort(remove, new Comparators.SortBySecondNatural<Integer>());
		
		float numToPossiblyRemove = remove.size() - 100;
		float numOkAfterThreshold = 100;
		if (numToPossiblyRemove < numOkAfterThreshold)
			return;
		
		float probabilityOfRemoval = numToPossiblyRemove/(numToPossiblyRemove + numOkAfterThreshold);
		log.trace(this, "bayesianPrune", probabilityOfRemoval, numToPossiblyRemove, numOkAfterThreshold);
		
		for (int i=0; i<numToPossiblyRemove; ++i)
		{
			if (random.nextFloat() < probabilityOfRemoval)
			{
				Pair<String,Integer> term = remove.get(i);
				bayesianSize -= term.second;
				vocabulary.remove(term.first);
			}
		}
	}

	public boolean bayesianMatches(Dictionary dictionary) 
	{
		float result = bayesianProbability(dictionary);
		log.debug(this, "bayesianProbability", result, this.toSerializableString(), dictionary.toSerializableString());
		
		return result > 0.5f;
	}

}
