/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#ifndef __Utilities_Log_h__
#define __Utilities_Log_h__

/**
 * Debug and Error logging utility methods and macros used throughout 
 * the application
 */

#include <sstream>
#include <string>

/**
 * because defines are inherently not part of namespaces, although this file is
 * within the Utilities folder it is not within the Utilities namespace
 */

/**
 * a few constants to control which and how the log file is opened, they must
 * declared within the program (probably in the startup file)
 */
extern bool LogToConsoleActivated;

/**
 * the functions actually write strings
 */
//#define CAN_WRITE_TO_LOG
#define CAN_WRITE_TO_CONSOLE
//#define CAN_WRITE_TO_DEBUGGER 

#ifdef CAN_WRITE_TO_LOG
#define _WriteToLog_ _WriteToLogImpl_
void _WriteToLogImpl_ (const std::string &);
#else
#define _WriteToLog_
#endif

#ifdef CAN_WRITE_TO_DEBUGGER 
#define _WriteToDebugger_ _WriteToLogDebuggerImpl_
void _WriteToLogDebuggerImpl_ (const std::string &);
#else
#define _WriteToDebugger_
#endif

#ifdef CAN_WRITE_TO_CONSOLE
#define _WriteToConsole_ _WriteToConsoleImpl_
void _WriteToConsoleImpl_ (const std::string &);
#else
#define _WriteToConsole_
#endif


/**
 * functions to turn on/off logs and query their state
 */
void _ActivateLog_ (const char *);
void _DeactivateLog_ (const char *);
bool _IsLogActivated_ (const char *s);
void _RecordUnusedLog_ (const char *s);
void _LogUnusedLogs_ ();
void _DeactivateLogs_ ();

void RotateLogFile (const std::string &rotateTo);
void _StartLogging ();
void _StopLogging ();

//-----------------------------------------------------------------------------

//#define LOG_DEBUG

#ifdef _DEBUG
	#ifndef LOG_DEBUG
		#define LOG_DEBUG
	#endif
#endif

#define ActivateLog(x) _ActivateLog_(#x)
#define DeactivateLog(x) _DeactivateLog_(#x)
#define IsLogActivated(x) _IsLogActivated_(#x)
#define RecordUnusedLog(x) _RecordUnusedLog_(#x)
#define LogUnusedLogs() _LogUnusedLogs_()
#define DeactivateLogs() _DeactivateLogs_()
#define StartLogging() _StartLogging()
#define StopLogging() _StopLogging()
//-----------------------------------------------------------------------------

extern const char *LogDebugStreamName;
extern const char *LogReleaseStreamName;
extern const char *LogErrorStreamName;

#define _RouteLog_(route,x) { std::ostringstream _out; _out << x; route(_out.str()); }

#define LogToLog(z) _RouteLog_ (_WriteToLog_, z)
#define LogToDebugger(z) _RouteLog_ (_WriteToDebugger_, z)
#define LogToConsole(z) { if (LogToConsoleActivated) _RouteLog_(_WriteToConsole_,z) }

#define FormatDebugger(p,n,x) __FILE__ << "(" << __LINE__ << ") : (" << p << ") " << #n << " " << x << std::endl
#define Format3(p,n,x) p << ": " << #n << " " << x << std::endl
#define Format2(n,x) #n << " " << x << std::endl
#define Format1(x) x << std::endl

#define InitialFormat(x,y) \
	std::ostringstream __logStream; \
	__logStream << y; \
	std::string x = __logStream.str();

#if defined(_DEBUG) || defined(FORCE_DEBUG)

	#ifdef LOG_DEBUG
		#define LogDebug(n,x) \
			if (IsLogActivated(n)) \
			{ \
				InitialFormat(__log,x) \
				LogToDebugger(FormatDebugger(LogDebugStreamName,n,__log)) \
				LogToLog(Format3(LogDebugStreamName,n,__log)); \
				LogToConsole(Format2(n,__log)); \
			} \
			else \
			{ \
				RecordUnusedLog(n); \
			}
	#else
		#define LogDebug(n,x)
	#endif

	#define LogRelease(n,x) \
		if (IsLogActivated(n)) \
		{ \
			InitialFormat(__log,x) \
			LogToDebugger (Format3(LogReleaseStreamName,n,__log)) \
			LogToLog(Format3(LogReleaseStreamName,n,__log)); \
			LogToConsole(Format2(n,__log)); \
		} \
		else \
		{ \
			RecordUnusedLog(n); \
		}

	#define LogDebugError(n,x) \
		{ \
			InitialFormat(__log,x) \
			LogToDebugger(Format3(LogErrorStreamName,n,__log)) \
			LogToLog(Format3(LogErrorStreamName,n,__log)) \
			LogToConsole(Format2(n,__log)) \
		} 

	#define LogError(n,x) \
		{ \
			InitialFormat(__log,x) \
			LogToDebugger(Format3(LogErrorStreamName,n,__log)) \
			LogToLog(Format3(LogErrorStreamName,n,__log)) \
			LogToConsole(Format2(n,__log)) \
		} 

#else

	#ifdef LOG_DEBUG
		#define LogDebug(n,x) \
			if (IsLogActivated(n)) \
			{ \
				InitialFormat(__log,x) \
				LogToDebugger(FormatDebugger(LogDebugStreamName,n,__log)) \
				LogToLog(Format3(LogDebugStreamName,n,__log)); \
				LogToConsole(Format2(n,__log)); \
			} \
			else \
			{ \
				RecordUnusedLog(n); \
			}

		#define LogDebugError(n,x) \
			{ \
				InitialFormat(__log,x) \
				LogToDebugger(Format3(LogErrorStreamName,n,__log)) \
				LogToLog(Format3(LogErrorStreamName,n,__log)) \
				LogToConsole(Format2(n,__log)) \
			}
	#else
		#define LogDebug(n,x)
		#define LogDebugError(n,x)
	#endif

	#define LogRelease(n,x) \
		if (IsLogActivated(n)) \
		{ \
			InitialFormat(__log,x) \
			LogToLog(Format1(__log)); \
			LogToConsole(Format1(__log)); \
		} \
		else \
		{ \
			RecordUnusedLog(n); \
		}

	#define LogError(n,x) \
		{ \
			InitialFormat(__log,x) \
			LogToDebugger(Format3(LogErrorStreamName,n,__log)) \
			LogToLog(Format3(LogErrorStreamName,n,__log)) \
			LogToConsole(Format2(n,__log)) \
		}

#endif

#define BoolToStr(x) ((x)?"true":"false")

#endif
