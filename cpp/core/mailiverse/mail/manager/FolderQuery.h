/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_model_FolderQuery_h__
#define __mailiverse_mail_model_FolderQuery_h__

#include "mailiverse/utilities/Monitor.h"
#include "mailiverse/utilities/Log.h"
#include "Master.h"
#include "EventDispatcher.h"
#include "Events.h"
#include "../model/Lib.h"
#include <algorithm>

namespace mailiverse {
namespace mail {
namespace manager {

class FolderQuery
{
public:
	utilities::EmptyMonitor monitor;
	Vector<model::ConversationPtr> *results;
	int *resultsBegin, *resultsEnd;

protected:
	Master *master;
	model::FolderPtr folder;
	int blockSize;
	Vector<model::ConversationPtr> releaseQueue;
	
protected:

	void onEmptyFilter ()
	{
		LogDebug(mailiverse::mail::manager::FolderQuery, "onEmptyFilter");

		match = NULL;
		linkResults();
		
		releaseRaw();
		request(0);
		
		releaseQueued();
	}
	
	void onFilter ()
	{
		LogDebug(mailiverse::mail::manager::FolderQuery, "onFilter");

		match = new model::Dictionary(filter);
		linkResults();
		
		releaseFilter();
		request(0);
		
		releaseQueued();
	}


	void releaseQueued()
	{
		releaseQueue.clear();
	}
	
	void releaseFilter ()
	{
		LogDebug(mailiverse::mail::manager::FolderQuery, "releaseFilter");
	
		releaseQueue.addAll(filterResults);
		
		filterDirty = false;
		filteringBuffer.clear();
		filteringChecked.clear();
		filterResults.clear();
		filterBegin = 0;
		filterEnd = 0;
		filterRequestBegin = 0;
		filterRequestEnd = 0;
		filterLastBegin = 0;
		filterLastEnd = 0;
		filterLastBlock = -1;
	}

	void releaseRaw ()
	{
		LogDebug(mailiverse::mail::manager::FolderQuery, "releaseRaw");

		releaseQueue.addAll(rawResults);
		
		conversationBuffer.clear();
		indexBuffer.clear();
		rawResults.clear();
		rawBegin = 0;
		rawEnd = 0;
		rawLastBegin = 0;
		rawLastEnd = 0;
	}
	
	void linkResults ()
	{
		LogDebug(mailiverse::mail::manager::FolderQuery, "linkResults");

		if (match)
		{
			results = &filterResults;
			resultsBegin = &filterBegin;
			resultsEnd = &filterEnd;
		}
		else
		{
			results = &rawResults;
			resultsBegin = &rawBegin;
			resultsEnd = &rawEnd;
		}
	}
	
protected:

	template<typename T>
	void clearRangeAndMark (T &t, int &last, int current, bool left)
	{
		LogDebug(mailiverse::mail::manager::FolderQuery, "clearRangeAndMark last" << last << " current " << current);

		if (left)
		{
			for (int i=last; i<current; ++i)
				t[i] = NULL;

			last = current;
		}
		else
		{
			for (int i=current; i<last; ++i)
				t[i] = NULL;
				
			last = current;
		}
	}

protected:

	String filter;
	model::DictionaryPtr match;
	bool filterDirty;
	
	Map<int, cache::ID> filteringBuffer;
	Set<int> filteringChecked;
	Vector<model::ConversationPtr> filterResults;
	int filterRequestBegin, filterRequestEnd;
	int filterLastBegin, filterLastEnd;
	int filterBegin, filterEnd;
	int filterLastBlock;

	void requestFilteredIndex (int index)
	{
		LogDebug(mailiverse::mail::manager::FolderQuery, "requestFilteredIndex " << index);

		int block = index / blockSize;
		int begin = std::max(0, (block-1) * blockSize);
		int end = (block + 2) * blockSize;

		requestFilterRange(begin, end);
	}
	
	void requestFilterRange (int begin, int end)
	{
		LogDebug(mailiverse::mail::manager::FolderQuery, "requestFilterRange begin " << begin << " end " << end);

		if (begin != filterRequestBegin || end != filterRequestEnd)
		{
			filterRequestBegin = begin;
			filterRequestEnd = end;
			
			renderFilteredResults();
		}
	}
	
	void renderFilteredResults (int step=0)
	{
		LogDebug(mailiverse::mail::manager::FolderQuery, "renderFilteredResults step " << step);

		if (filterDirty)
		{
			filterDirty = false;
			filterBegin = std::min(filterRequestBegin, (int)filteringBuffer.size());
			filterEnd = std::min(filterRequestEnd, (int)filteringBuffer.size());
			
			clearRangeAndMark(filterResults, filterLastBegin, filterBegin, true);
			clearRangeAndMark(filterResults, filterLastEnd, filterEnd, false);
			filterResults.resize(filterEnd);

			int j=0;
			for (auto &i : filteringBuffer)
			{
				if (filterBegin <= j && j <filterEnd)
				{
					filterResults[j] = master->getCacheManager()->getConversation(
						model::Record(i.second,Date(0))
					);
				}
				
				j++;
			}
		}
		
//		filterRequestMoreResultsIfNeccessary(step+1);
		
		if (step == 0)
		{
			master->getEventPropagator()->signal(
				Events::FolderListing, 
				utilities::newArg<model::ConversationPtr>((model::Conversation *)NULL)
			);
		}
	}
	
	bool filterRequestMoreResultsIfNeccessary (int step)
	{
		LogDebug(mailiverse::mail::manager::FolderQuery, "filterRequestMoreResultsIfNeccessary");
		
		if (filteringBuffer.size() >= filterRequestEnd)
			return false;
			
		if (noMoreConversations())
			return false;

		if (!allConversationsLoaded())
			return true;			
			
		filteringChecked.clear();
		requestRawBlock(++filterLastBlock);
		
		if (checkForLoadedConversations())
			renderFilteredResults(step);

		return true;
	}
	
	bool allConversationsLoaded ()
	{
		for (auto &i : indexBuffer)
		{
			if (!i.second->isLoaded())
				return false;
		}
		
		return true;
	}
	
	bool noMoreConversations ()
	{
		return conversationBuffer.empty();
	}

	bool checkForLoadedConversations ()
	{
		int conversationsHandled = 0;
		for (auto &i : conversationBuffer)
		{
			if (i.first->isLoaded())
			{
				if (onConversation(i.first))
					conversationsHandled++;
			}
		}
		
		return conversationsHandled > 0;
	}
	
	/**
	 * returns whether this converstaion was handled, not whether it was added to the filter results
	 */
	bool onConversation (model::Conversation *conversation)
	{
		LogDebug(mailiverse::mail::manager::FolderQuery, "onConversation");

		auto i = conversationBuffer.find(conversation);
		if (i != conversationBuffer.end())
		{
			LogDebug(mailiverse::mail::manager::FolderQuery, "onConversation " << i->second);
		
			if (match)
			{
				if (!filteringChecked.contains(i->second))
				{
					if (conversation->getHeader()->getDictionary()->matches(match))
					{
						LogDebug(mailiverse::mail::manager::FolderQuery, "onConversation matches");
						
						filteringBuffer[i->second] = conversation->getID();
						filterDirty = true;
						
					}
					
					filteringChecked.add(i->second);
					return true;
				}
			}
			else
			{
				return true;
			}
		}
		
		return false;
	}
	
	int rawLastBegin, rawLastEnd;
	Vector<model::ConversationPtr> rawResults;
	int rawBegin, rawEnd;
	Map<model::ConversationPtr, int> conversationBuffer;
	Map<int, model::Conversation *> indexBuffer;
	
	void renderResults ()
	{
		LogDebug(mailiverse::mail::manager::FolderQuery, "renderResults");
		
		int beginIndex = INT_MAX;
		int endIndex = -1;
		int numResults = 0;
		for (auto &i : indexBuffer)
		{
			if (i.second->isLoaded())
				numResults++;
				
			endIndex = std::max(i.first, endIndex);
			beginIndex = std::min(i.first, beginIndex);
		}
		
		if (indexBuffer.empty())
			beginIndex = 0;
			
		endIndex++;

		LogDebug(
			mailiverse::mail::manager::FolderQuery, 
			"renderResults beginIndex " << beginIndex << " endIndex " << endIndex << " numResults " << numResults
		);

		if (rawResults.size() <= endIndex)
			rawResults.resize(endIndex);

		for (int i = rawBegin; i< beginIndex; ++i)
			rawResults[i] = NULL;
			
		for (int i= endIndex; i<rawEnd; ++i)
			rawResults[i] = NULL;

		for (auto &i : indexBuffer)
			rawResults[i.first] = i.second;
			
		rawBegin = beginIndex;
		rawEnd = endIndex;
	}

	void requestRawIndex (int index)
	{
		LogDebug(mailiverse::mail::manager::FolderQuery, "requestRawIndex " << index);
		
		requestRawBlock(index / blockSize);
	}

	void requestRawBlock (int block)
	{
		LogDebug(mailiverse::mail::manager::FolderQuery, "requestRawBlock " << block);

		int l = std::max(0, (block-1) * blockSize);
		int r = (block + 2) * blockSize;
		requestRawRange(l, r);
	}
	
	void requestRawRange (int begin, int end)
	{
		LogDebug(mailiverse::mail::manager::FolderQuery, "requestRawRange:begin begin " << begin << " end " << end);

		for (int i = rawLastBegin;i < begin; ++i)
		{
			auto j = indexBuffer.find(i);
			if (j!=indexBuffer.end())
			{
				conversationBuffer.erase(j->second);
				indexBuffer.erase(j);
			}
		}

		for (int i = end;i < rawLastEnd; ++i)
		{
			auto j = indexBuffer.find(i);
			if (j!=indexBuffer.end())
			{
				conversationBuffer.erase(j->second);
				indexBuffer.erase(j);
			}
		}
		
		rawLastBegin = begin;
		rawLastEnd = end;

		for ( ;begin<end; ++begin)
		{
			auto i =indexBuffer.find(begin);
			if (i == indexBuffer.end() || i->second == NULL)
				break;
		}
		
		for ( ; end > begin; end--)
		{
			auto i =indexBuffer.find(end-1);
			if (i == indexBuffer.end() || i->second == NULL)
				break;
		}
		
		LogDebug(mailiverse::mail::manager::FolderQuery, "requestRawRange:RANGE begin " << begin << " end " << end);
		
		if (end==begin)
			return;
		
		Vector<model::ConversationPtr> conversations = 
			folder->getConversations(begin,end-begin);
			
		int I=0;
		for (auto &i : conversations)
		{
			int index = I + begin;
			conversationBuffer[i] = index;
			indexBuffer[index] = i;
			I++;
		}

		renderResults();
	}
	
public:
	FolderQuery(Master *_master, int _blockSize=5) :
		master(_master),
		blockSize(_blockSize)
	{
		LogDebug(mailiverse::mail::manager::FolderQuery, "FolderQuery");
		
		releaseMemory();
		linkResults();
		
		master->getEventPropagator()->add (
			manager::Events::LoadConversation,
			this,
			utilities::newbindC_G<model::Conversation *>(
				this, &FolderQuery::onLoadConversation
			)
		);
	}

	virtual ~FolderQuery ()
	{
		LogDebug(mailiverse::mail::manager::FolderQuery, "~FolderQuery");

		master->getEventPropagator()->remove(
			this
		);
	}
	
	void underlyingResultsChanged ()
	{
		utilities::EmptyMonitor::Writer m(monitor);
		clearResults();
	}

	void setFolder (model::Folder *folder)
	{
		LogDebug(mailiverse::mail::manager::FolderQuery, "setFolder " << folder);

		utilities::EmptyMonitor::Writer m(monitor);

		if (folder != this->folder)
		{
			this->folder = folder;
			underlyingResultsChanged();
			// releaseMemory();
		}
	}
	
	bool ready ()
	{
		utilities::EmptyMonitor::Writer m(monitor);
		
		return this->folder;
	}

	void request (int index)
	{
		LogDebug(mailiverse::mail::manager::FolderQuery, "request " << index);

		utilities::EmptyMonitor::Writer m(monitor);

		if (match)
			requestFilteredIndex(index);
		else
			requestRawIndex(index);
	}
		
	bool onFilterStep ()
	{
		LogDebug(mailiverse::mail::manager::FolderQuery, "onFilterStep");

		utilities::EmptyMonitor::Writer m(monitor);

		bool result = filterRequestMoreResultsIfNeccessary(0);
		releaseQueued();
		
		return result;
	}
	
	void setFilter (const String &filter)
	{
		LogDebug(mailiverse::mail::manager::FolderQuery, "setFilter " << filter);

		utilities::EmptyMonitor::Writer m(monitor);

		if (filter == this->filter)
			return;
			
		bool isSubset = !this->filter.empty() && utilities::startsWith(filter, this->filter);
		this->filter = filter;
		
		if (filter.empty())
		{
			onEmptyFilter();
			return;
		}
		else
		{
			onFilter();
		}	
	}
	
	void clearResults ()
	{
		LogDebug(mailiverse::mail::manager::FolderQuery, "releaseMemory");
		utilities::EmptyMonitor::Writer m(monitor);
		
		releaseRaw();
		releaseFilter();
	}
	
	void releaseMemory ()
	{
		utilities::EmptyMonitor::Writer m(monitor);
		
		clearResults();
		releaseQueued();
	}
	
	
	void onLoadConversation (model::Conversation *conversation)
	{
		utilities::EmptyMonitor::Writer m(monitor);

		// did we actually find one
		if (onConversation(conversation))
		{
			if (match)
			{
				renderFilteredResults();
			}
			else
			{
				master->getEventPropagator()->signal(
					Events::FolderListing, 
					utilities::newArg<model::ConversationPtr>(conversation)
				);
			}
		}
	}
	
	
};

DECLARE_SMARTPTR(FolderQuery);

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */
#endif /* __mailiverse_mail_model_FolderSimple_h__ */
