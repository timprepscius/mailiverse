/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#include "Log.h"

#include <assert.h>
#include <stdio.h>
#include <set>
#include <map>
#include <time.h>
#include <fstream>

// # CROSS-PLATFORM HACK #
#ifdef WIN32
#include <Windows.h> // for OutputDebugString
#endif

const char *LogDebugStreamName = "Debug";
const char *LogReleaseStreamName = "Release";
const char *LogErrorStreamName = "Error";
bool LogToConsoleActivated = true;

//==========================================================================//

//=============================================================================
// yes this throws a little memory away, but with multiple threads, you never
// know which one will die first, and if the primary thread dies first,
// then this set will be destructed, and then all logging will cause exceptions!

struct LessByStrCmp
{
	bool operator()(const char *s1, const char *s2) const
	{
		if (s1 == s2)
			return false;

		return strcmp (s1,s2) < 0;
	}
};

struct Logger
{
	FILE *file;
	bool fileFailed;
	bool allLogsActivated;

	typedef std::set<const char *, LessByStrCmp> LogSet;
	LogSet activatedLogs;
	LogSet deactivatedLogs;
	std::map<const char *, unsigned int, LessByStrCmp> unusedLogStreams;

	bool matches (const LogSet &set, const char *l)
	{
		if (set.find(l) != set.end())
			return true;

		LogSet::const_iterator i;
		for (i=set.begin(); i!=set.end(); ++i)
		{
			const char *pos = strchr (*i, '*');
			if (pos != NULL)
				if (strncmp (*i, l, pos - *i)==0)
					return true;
		}

		return false;
	}

	Logger () :
		file (NULL),
		fileFailed (false),
		allLogsActivated (false)
	{
	}

	~Logger ()
	{
		if (file)
			fclose (file);

		// activated logs are _dynamically_ read from a file
		while (!activatedLogs.empty())
		{
			delete[] ((char *)(*activatedLogs.begin()));
			activatedLogs.erase (activatedLogs.begin());
		}

		while (!deactivatedLogs.empty())
		{
			delete[] ((char *)(*deactivatedLogs.begin()));
			deactivatedLogs.erase(deactivatedLogs.begin());
		}

		// unused log streams are static, cause they come from the code
		// executing via __LINE__ and __FILE__ etc
		unusedLogStreams.clear();
	}
} ;

static Logger *logger = NULL;

char * __GetLogTimeStamp__ ()
{
#ifdef WIN32
	__time64_t ltime;
	_time64( &ltime );

	char *str = _ctime64(&ltime);
	int threadID = GetCurrentThreadId();
#else
	time_t ltime;
	::time(&ltime);
	char *str = ctime(&ltime);
	long threadID = (long)pthread_self();
#endif

	static char copy[8][256];
	static volatile int rotation = 0;
	rotation++;

	str[strlen(str)-1] = ' ';
	sprintf (copy[rotation % 8], "%s%d ", str, (int)threadID);
	
	return copy[rotation % 8];
}

void _WriteToConsoleImpl_ (const std::string &s)
{
	fputs (__GetLogTimeStamp__(), stdout);
	fputs (s.c_str(), stdout);
	fflush(stdout);
}

void _WriteToDebuggerImpl_ (const std::string &s)
{
	#ifdef WIN32
		OutputDebugString (__GetLogTimeStamp__());
		OutputDebugString (s.c_str());
	#endif
}

extern "C" void _WriteToLogCSTR_ (const char *s);
void _WriteToLogCSTR_ (const char *s)
{
	_WriteToLog_(std::string(s)+"\n");
}

void _ActivateLog_ (const char *s)
{
	#ifdef LOG_DEBUG
		const bool DEBUG_STATUS = true;
	#else
		const bool DEBUG_STATUS = false;
	#endif
	
	if (!logger) return;

	Logger::LogSet::iterator i = logger->activatedLogs.find(s);

	if (i==logger->activatedLogs.end())
	{
		if (s[0] == '-')
		{
			char *copy = strcpy (new char[strlen(s)], s+1);
			logger->deactivatedLogs.insert (copy);
		}
		else
		{
			char *copy = strcpy (new char[strlen(s)+1], s);
			logger->activatedLogs.insert (copy);

			if (strcmp (s, "*")==0)
			{
				logger->allLogsActivated = true;
			}
		}
	}
}

void _DeactivateLog_ (const char *s)
{
	if (!logger) return;

	if (s[0] == '-')
	{
		char *ds = ((char *)s)+1;
		Logger::LogSet::iterator i = logger->deactivatedLogs.find(ds);
		if (i!=logger->deactivatedLogs.end())
		{
			delete[]((char *)(*i));
			logger->deactivatedLogs.erase (i);
		}
	}
	else
	{
		Logger::LogSet::iterator i = logger->activatedLogs.find(s);
		if (i!=logger->activatedLogs.end())
		{
			if (strcmp (s, "*")==0)
			{
				logger->allLogsActivated = false;
			}

			delete[]((char *)(*i));
			logger->activatedLogs.erase (i);
		}
	}
}

void _DeactivateLogs_ ()
{
	if (!logger) return;

	while (!logger->activatedLogs.empty())
		_DeactivateLog_ (*logger->activatedLogs.begin());

	while (!logger->deactivatedLogs.empty())
		_DeactivateLog_ (*logger->deactivatedLogs.begin());

	logger->unusedLogStreams.clear();
}

bool _IsLogActivated_ (const char *s)
{
	if (!logger) return false;

	return (
		(logger->allLogsActivated || logger->matches (logger->activatedLogs, s)) &&
		(!logger->matches (logger->deactivatedLogs, s))	
	);
}

void _RecordUnusedLog_ (const char *s)
{
	if (!logger) return;

	std::map<const char *, unsigned int, LessByStrCmp>::iterator i = 
		logger->unusedLogStreams.find (s);

	if (i!=logger->unusedLogStreams.end())
	{
		(*i).second++;
	}
	else
	{
		logger->unusedLogStreams[s] = 1;
	}
}

void _StartLogging ()
{
	assert (!logger);

	logger = new Logger();
}

void _StopLogging ()
{
	assert (logger);

	Logger *temp = logger;
	logger = NULL;

	delete temp;
}

void _LogUnusedLogs_ ()
{
	if (!logger) return;

	LogToConsole ("Used inactive logs:\n");
	LogToLog ("Used inactive logs:\n");

	std::map<const char *, unsigned int, LessByStrCmp>::iterator i;
	for (i=logger->unusedLogStreams.begin(); i!=logger->unusedLogStreams.end(); i++)
	{
		LogToDebugger ("\t" << (*i).first << "\t" << (*i).second << "\n");
		LogToLog ("\t" << (*i).first << "\t" << (*i).second << "\n");
		LogToConsole ("\t" << (*i).first << "\t" << (*i).second << "\n");
	}
}

