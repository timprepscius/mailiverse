/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __Utilities_File_h__
#define __Utilities_File_h__

/**
 * File utility methods used throughout the application
 */

#include <string>
#include <set>
#include <list>

#include "GenericFile.h"
#include <fstream>

namespace mailiverse {
namespace utilities {

GenericFile::In *encapsulateFile (std::ifstream *in);

//  '\\?\'
class ifstream : public std::ifstream
{
	typedef std::ifstream Super;

	protected:
		FILE *file;

	public:
	ifstream (const std::wstring &name, ios_base::openmode _Mode = std::ios::in);
		virtual ~ifstream ();
} ;

class ofstream : public std::ofstream
{
	typedef std::ofstream Super;

	protected:
		FILE *file;

	public:
	ofstream (const std::wstring &name, ios_base::openmode _Mode = std::ios::out);
		virtual ~ofstream ();
} ;

/**
 *
 */
extern const char ServerDirectoryDelimiter, LocalDirectoryDelimiter;
extern const wchar_t ServerDirectoryDelimiterW, LocalDirectoryDelimiterW;

extern const char * DelimiterSet;
extern const wchar_t *DelimiterSetW;

std::string getApplicationDataPath (const std::string &path);

/**
 * creates the given path if currently non-existant
 *
 * @param	path		the path to create
 * @return	{success=true, failure=false}
 */
bool ensurePath(const std::string &path, char delimiter=LocalDirectoryDelimiter);
bool ensurePath(const std::wstring &path, wchar_t delimiter=LocalDirectoryDelimiterW);

/**
 * verifies the existance of a file
 *
 * @param	file		the name of the file to verify
 * @return	false if file does not exist or is a directory, true otherwise
 */
bool fileExists(const std::string &file);
bool fileExists(const std::wstring &file);

/**
 * returns the size of the file, 0 if it does not exist
 */
long fileSize(const std::string &file);
long fileSize(const std::wstring &file);

/**
 * @return	the directory which this executable lives within
 */
std::wstring getExecutableDirectory ();

/**
 * converts a file path's delimiters from one character to another
 *
 * @param	s			the file name
 * @param	from		the character to convert from
 * @param	to			the character to which from will be converted
 * @return	the new file name
 */
std::string convertDelimiter (const std::string &s, char from=ServerDirectoryDelimiter, char to=LocalDirectoryDelimiter);
std::wstring convertDelimiter (const std::wstring &s, wchar_t from=ServerDirectoryDelimiterW, wchar_t to=LocalDirectoryDelimiterW);

/**
 * gets a file names extension
 */
std::string getFileNameExtension(const std::string &);
std::wstring getFileNameExtension(const std::wstring &);

/**
 * gets a file name excluding its extension
 */
std::string getFileNameExcludingExtension (const std::string &);
std::wstring getFileNameExcludingExtension (const std::wstring &);

/**
 * kills a directory whether empty or not
 */
bool killDirectory (const std::string &directory);
bool killDirectory (const std::wstring &directory);

/**
 * kills a file, returns true on success, false on failure
 */
bool killFile (const std::string &file);
bool killFile (const std::wstring &file);

/**
 * renames a file
 */
bool renameFile (const std::string &from, const std::string &to);
bool renameFile (const std::wstring &from, const std::wstring &to);

/**
 *
 */
bool copyFile (const std::string &from, const std::string &to, bool binary);
bool copyFile (const std::wstring &from, const std::wstring &to, bool binary);

/**
 * Given a string like asdf/rert/gfds.sdf, returns 'gfds.sdf',
 * detects both '/' and '\\' delimiters.
 */
std::string getFileName(const std::string & fullFileName);
std::wstring getFileName(const std::wstring & fullFileName);

/**
 * Given a string like asdf/rert/gfds.sdf, returns 'asdf/rert/',
 * detects both '/' and '\\' delimiters.
 */
std::string getDirectory(const std::string & fullFileName, bool includeLastSlash=true);
std::wstring getDirectory(const std::wstring & fullFileName, bool includeLastSlash=true);

/**
 * Returns the last modified time of the given file, 0 if there
 * was no such file found.
 */
time_t getFileLastModifiedTime(const std::string & fullFileName);
time_t getFileLastModifiedTime(const std::wstring & fullFileName);

/**
 * Returns the last modified time of the given file, 0 if there
 * was no such file found.
 */
bool setFileLastModifiedTime(const std::string & fullFileName, time_t);
bool setFileLastModifiedTime(const std::wstring & fullFileName, time_t);

/**
 * Returns the last modified time of the given file, 0 if there
 * was no such file found.
 */
time_t getFileCreationTime (const std::string & fullFileName);
time_t getFileCreationTime (const std::wstring & fullFileName);

/**
 * Returns the last modified time of the given file, 0 if there
 * was no such file found.
 */
time_t getFileAccessTime (const std::string & fullFileName);
time_t getFileAccessTime (const std::wstring & fullFileName);

/**
 * Returns the files that are contained in the given directory.
 */
std::list<std::wstring> getFilesInDirectory (const std::wstring &directory, bool recursive=true);
std::list<std::string> getFilesInDirectory (const std::string &directory, bool recursive=true);

/**
 * Returns the files that are contained in the given directory.
 */
std::list<std::wstring> getAllEntitiesInDirectory (const std::wstring &directory, bool recursive=true);
std::list<std::string> getAllEntitiesInDirectory (const std::string &directory, bool recursive=true);

/**
 * converts invalid characters to valid characters using %
 */
std::string mungifyPath (const std::string &path);
std::wstring mungifyPath (const std::wstring &path);

} // namespace utilites
} // namespace mailiverse

#endif
