/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "File.h"

#include <assert.h>
#include <sys/types.h>
#include <sys/stat.h>

#include <fstream>
#include <iostream>
#include <list>


#include "Strings.h"

#ifdef WIN32
#include <shlobj.h>
#include <io.h>
#include <direct.h>

#pragma comment(lib, "shell32.lib")

EXTERN_C IMAGE_DOS_HEADER __ImageBase;

#else
#include <dirent.h>
#include <stdio.h>
#include <utime.h>
#endif

#ifdef MAC
#include <mach-o/dyld.h>
#include "CoreFoundation/CFBundle.h"
#endif

/**
 * because of GetCurrentDirectory... this should change when we use the registry
 * to find out where to store data
 */
#ifdef WIN32
#include <windows.h>
typedef struct _stat statStruct;
#define statFunc(x,y) _stat(x,y)
#define statFuncW(x,y) _wstat(x,y)
#define utimeW(x,y) _wutime(x,y)
#define S_IFDIR _S_IFDIR

#else
typedef struct stat statStruct;
#define statFunc(x,y) stat(x,y)
#define statFuncW(x,y) stat(mailiverse::utilities::convert(x).c_str(),y)
#define utimeW(x,y) utime(mailiverse::utilities::convert(x).c_str(), y)

#define _wfopen(x,y) fopen(mailiverse::utilities::convert(x).c_str(), mailiverse::utilities::convert(y).c_str())
#define MAX_PATH 1024

#include <unistd.h>
#endif

#ifdef WIN32
const char mailiverse::utilities::LocalDirectoryDelimiter = '\\';
const wchar_t mailiverse::utilities::LocalDirectoryDelimiterW = L'\\';
#else
const char mailiverse::utilities::LocalDirectoryDelimiter = '/';
const wchar_t mailiverse::utilities::LocalDirectoryDelimiterW = L'/';
#endif

const char mailiverse::utilities::ServerDirectoryDelimiter = '/';
const wchar_t mailiverse::utilities::ServerDirectoryDelimiterW = L'/';

const char * mailiverse::utilities::DelimiterSet = "/\\";
const wchar_t * mailiverse::utilities::DelimiterSetW = L"/\\";

namespace mailiverse {
namespace utilities {

class FileInFile : public GenericFile::In
{
	protected:
		std::ifstream *ifstream;

	public:
		FileInFile (std::ifstream *ifstream)
		{
			this->ifstream = ifstream;
		}
		virtual ~FileInFile ()
		{
			delete ifstream;
		}

		virtual long seek (long seek, int origin)
		{
			ifstream->seekg (seek, (std::ios::seekdir)origin);
			return tell();
		}

		virtual long tell ()
		{
			return ifstream->tellg();
		}

		virtual unsigned long read (char *buffer, unsigned long size)
		{
			ifstream->read(buffer, size);
			return ifstream->gcount();
		}

		virtual long size ()
		{
			long ptr = tell();
			seek (0,2);
			long size = tell();
			seek (ptr,0);
			return size;
		}
}  ;

} // namespace File
} // namespace Utilities

#ifdef WIN32

inline time_t FileTimeToTimeT( const FILETIME &ft )
{
	LONGLONG ll = ft.dwHighDateTime;
	ll <<= 32;
	ll |= ft.dwLowDateTime;

	LONGLONG result = ( ll - (LONGLONG)0x19DB1DED53E8000 ) / (LONGLONG)10000000;

	return (time_t)result;
}

// http://msdn.microsoft.com/en-us/library/ms724228(VS.85).aspx
void TimetToFileTime( time_t t, FILETIME &pft )
{
    LONGLONG ll = Int32x32To64(t, 10000000) + 116444736000000000;
    pft.dwLowDateTime = (DWORD) ll;
    pft.dwHighDateTime = ll >>32;
}

//http://msdn.microsoft.com/en-us/library/aa365247(VS.85).aspx
inline std::wstring toLongPath (const std::wstring &name)
{
	assert (name.length() > 0);

	if (name[0] == L'\\')
	{
		wchar_t drive[3];
		drive[0] = L'A' + (_getdrive()-1);
		drive[1] = L':';
		drive[2] = 0;

		std::wstring result = L"\\\\?\\";
		result += drive;
		result += name;

		return result;
	}
	else
	if (name.length()>=2 && name[1]==L':')
		return L"\\\\?\\" + name;

	return name;
}

//  '\\?\'
mailiverse::utilities::ifstream::ifstream (const std::wstring &name, ios_base::openmode _Mode) :
Super (
	   file = 
	   _wfopen(
			   toLongPath(name).c_str(), 
			   _Mode==std::ios::binary ? L"rb" : L"rt"
			   )
	   )
{
	if (!file)
		_Myios::setstate(ios_base::failbit);
}

mailiverse::utilities::ifstream::~ifstream ()
{ 
	close(); 
	if (file) 
		fclose(file); 
}

mailiverse::utilities::ofstream::ofstream (const std::wstring &name, ios_base::openmode _Mode) :
Super (
	   file = 
	   _wfopen(
			   toLongPath(name).c_str(), 
			   _Mode==std::ios::binary ? L"wb" : L"wt"
			   )
	   )
{
	if (!file)
		_Myios::setstate(ios_base::failbit);
}

mailiverse::utilities::ofstream::~ofstream ()
{ 
	close(); 
	if (file) 
		fclose(file); 
}

#else

inline std::wstring toLongPath(const std::wstring &name)
{
	assert (name.length() > 0);
	return name;	
}

//  '\\?\'
mailiverse::utilities::ifstream::ifstream (const std::wstring &name, ios_base::openmode _Mode) :
	Super (
		mailiverse::utilities::convert(name).c_str()
		//,_Mode
	)
{
}

mailiverse::utilities::ifstream::~ifstream ()
{ 
}

mailiverse::utilities::ofstream::ofstream (const std::wstring &name, ios_base::openmode _Mode) :
	Super (
		mailiverse::utilities::convert(name).c_str(),
		_Mode
	)
{
}

mailiverse::utilities::ofstream::~ofstream ()
{ 
}


#endif

mailiverse::utilities::GenericFile::In *mailiverse::utilities::encapsulateFile (std::ifstream *in)
{
	return new mailiverse::utilities::FileInFile (in);
}

#ifdef WIN32

std::string mailiverse::utilities::getApplicationDataPath (const std::string &application)
{
	wchar_t cpath[MAX_PATH];

	SHGetFolderPathW(NULL, CSIDL_COMMON_APPDATA, NULL, NULL, cpath);

	std::wstring path = cpath;
	path += mailiverse::utilities::convert("\\" + application);

	std::string apath = mailiverse::utilities::convert(path);
	mailiverse::utilities::ensurePath (apath + "\\");

	return apath;
}

#else

#endif

bool mailiverse::utilities::ensurePath (const std::string &path, char delimiter)
{
	return
		mailiverse::utilities::ensurePath (
			mailiverse::utilities::convert (path),
			mailiverse::utilities::convert (delimiter)
		);
}

bool mailiverse::utilities::ensurePath (const std::wstring &path, wchar_t delimiter)
{
	// find the end of the last portion of the path we need to create
	int finalPos = path.rfind (delimiter);
	
	int pos = 0;

	int colonpos = path.find(':', 0);
	int firstdelimiterpos = path.find(delimiter, 0);

	if (colonpos > 0 && firstdelimiterpos > 0 && colonpos < firstdelimiterpos)
	{
		// Skip the first one if there's a colon ('c:/blah/whaterver')
		pos = path.find (delimiter, pos+1);
	}

	while (pos<finalPos)
	{
		// find the next part of the path to create
		pos = path.find (delimiter, pos+1);
		

		// does it already exist
		
#ifdef WIN32

		// pull it out as a separate string
		std::wstring directory = toLongPath(path.substr (0, pos));

		WIN32_FILE_ATTRIBUTE_DATA data;
		bool exists = GetFileAttributesExW (directory.c_str(), GetFileExInfoStandard, &data) > 0;

		if (exists)
		{
			// if it does exist, but is not a directory, ensurePath fails
			if (!(data.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY))
				return false;
		}
		else
		{
			// if it does not exist create it, if this fails ensurePath fails
			if (_wmkdir (directory.c_str())!=0)
				return false;
		}

#else

		std::wstring directory = path.substr (0, pos);

		statStruct status;
		int exists = (statFuncW (directory.c_str(), &status)==0);

		if (exists)
		{
			// if it does exist, but is not a directory, ensurePath fails
			if (!(status.st_mode & S_IFDIR))
				return false;
		}
		else
		{
			// if it does not exist create it, if this fails ensurePath fails
			if (mkdir (mailiverse::utilities::convert(directory).c_str(), (S_IRWXU | S_IRWXG | S_IRWXO))!=0)
				return false;
		}
#endif

	}

	
	return true;
}

bool mailiverse::utilities::fileExists (const std::string &file)
{
	return
		mailiverse::utilities::fileExists (
			mailiverse::utilities::convert (file)
		);
}

bool mailiverse::utilities::fileExists (const std::wstring &file)
{
#ifdef WIN32

	WIN32_FILE_ATTRIBUTE_DATA data;
	bool exists = GetFileAttributesExW (toLongPath(file).c_str(), GetFileExInfoStandard, &data) > 0;

	return exists && !(data.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY);

#else
	statStruct status;
	if (statFuncW (toLongPath(file).c_str(), &status)==0)
	{
		// if it is not a directory return true
		if (!(status.st_mode & S_IFDIR))
			return true;
	}

	return false;

#endif
}

long mailiverse::utilities::fileSize(const std::string &file)
{
	return
		mailiverse::utilities::fileSize (
			mailiverse::utilities::convert (file)
		);
}

long mailiverse::utilities::fileSize(const std::wstring &file)
{
#ifdef WIN32

	WIN32_FILE_ATTRIBUTE_DATA data;
	bool exists = GetFileAttributesExW (toLongPath(file).c_str(), GetFileExInfoStandard, &data) > 0;

	if (exists)
		return data.nFileSizeLow;

	return 0;

#else
	statStruct status;

	if (statFuncW (toLongPath(file).c_str(), &status)==0)
	{
		// if it is not a directory return true
		return status.st_size;
	}

	return 0;
#endif

}

std::string mailiverse::utilities::convertDelimiter (const std::string &str, char from, char to)
{
	return
		mailiverse::utilities::convert (
			mailiverse::utilities::convertDelimiter (
				mailiverse::utilities::convert (str),
				mailiverse::utilities::convert (from),
				mailiverse::utilities::convert (to)
			)
		);
}

std::wstring mailiverse::utilities::convertDelimiter (const std::wstring &str, wchar_t from, wchar_t to)
{
	if (from==to) return str;
	if (str.empty()) return str;

	wchar_t *front, *s;
	front = s = wcscpy (new wchar_t[str.length()+1], str.c_str());

	while ((s=wcschr (s, from))!=NULL)
	{
		*s=to;
	}

	std::wstring stdstr = front;
	delete[] front;

	return stdstr;
}

std::wstring mailiverse::utilities::getExecutableDirectory ()
{

#ifdef WIN32
	wchar_t cpath[_MAX_PATH];
	int size = ::GetModuleFileNameW(
		(HINSTANCE)&__ImageBase, 
		cpath, 
		_MAX_PATH
	);

	std::wstring wpath(cpath);

	return
		getDirectory(
			wpath,
			false
		);

#elif defined(MAC)

	char path[PATH_MAX];
	char path2[PATH_MAX];

	CFBundleRef mainBundle = CFBundleGetBundleWithIdentifier ( CFSTR( "net.DimensionDoor.Plugin" ) );
	if (mainBundle)
	{
	    CFURLRef url;
		CFStringRef str;

		url = CFBundleCopyBundleURL(mainBundle);
		str = CFURLCopyFileSystemPath( url, kCFURLPOSIXPathStyle );
		CFStringGetCString( str, path, FILENAME_MAX, kCFStringEncodingASCII );
		CFRelease(str);
		CFRelease(url);

	        url = CFBundleCopyResourcesDirectoryURL(mainBundle);
		str = CFURLCopyFileSystemPath( url, kCFURLPOSIXPathStyle );
		CFStringGetCString( str, path2, FILENAME_MAX, kCFStringEncodingASCII );
		CFRelease(str);
		CFRelease(url);

		fprintf(stderr, "path is %s %s", path, path2);
		return mailiverse::utilities::convert(path) + L"/" + mailiverse::utilities::convert(path2);
	}

	uint32_t size=PATH_MAX;
	_NSGetExecutablePath(path, &size);
	if (size > 0)
	{
		path[size]=0;
		fprintf(stderr, "nsgetexecutable path is %s", path);
		std::wstring wpath = mailiverse::utilities::convert(path); // blahblah/MacOS/exename
		wpath = mailiverse::utilities::getDirectory(wpath, false); // blahblah/MacOS
		
		if (mailiverse::utilities::getFileName(wpath) == L"MacOS")
		{
			wpath = mailiverse::utilities::getDirectory(wpath, false); // blahblah
			wpath += L"/Resources";
			
			return wpath;
		}
	}
#endif
#ifndef WIN32
	char buffer[1024];
	getcwd(buffer, 1024);
	return mailiverse::utilities::convert(buffer);
#endif
}

std::string mailiverse::utilities::getFileNameExtension(const std::string & fileName)
{
	return
		mailiverse::utilities::convert (
			mailiverse::utilities::getFileNameExtension (
				mailiverse::utilities::convert (fileName)
			)
		);
}

std::wstring mailiverse::utilities::getFileNameExtension(const std::wstring & fileName)
{
	std::wstring extension;

	long lastPeriodPos = fileName.rfind(L".");
	if (lastPeriodPos >= 0)
	{
		extension = fileName.substr(lastPeriodPos+1, fileName.length()-lastPeriodPos+1);
	}

	return extension;
}

std::string mailiverse::utilities::getFileNameExcludingExtension (const std::string &fileName)
{
	return
		mailiverse::utilities::convert (
			mailiverse::utilities::getFileNameExcludingExtension (
				mailiverse::utilities::convert (fileName)
			)
		);
}

std::wstring mailiverse::utilities::getFileNameExcludingExtension (const std::wstring &fileName)
{
	long lastPeriodPos = fileName.rfind(L".");
	if (lastPeriodPos >= 0)
	{
		return fileName.substr(0, lastPeriodPos);
	}

	return fileName;
}

bool mailiverse::utilities::killDirectory (const std::string &directory)
{
	return
		mailiverse::utilities::killDirectory (
			mailiverse::utilities::convert (directory)
		);
}

bool mailiverse::utilities::killDirectory (const std::wstring &directory)
{
#ifdef WIN32
	WIN32_FIND_DATAW data;
	HANDLE handle = ::FindFirstFileW( (toLongPath(directory) + L"*").c_str(), &data );

	if (!handle)
		return false;

	do
	{
		if (data.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)
		{
			std::wstring directoryname(data.cFileName);

			if ( directoryname != L"." && directoryname != L".." )
			{
				if (!killDirectory (directory + directoryname + L"\\"))
					return false;
			}
		}
		else
		{
			if (!killFile (directory + L"\\" + std::wstring(data.cFileName)))
				return false;
		}
	}
	while (::FindNextFileW (handle, &data));

	::FindClose (handle);

	return (_wrmdir (toLongPath(directory).c_str()) == 0);
#else
	DIR *dir = ::opendir (mailiverse::utilities::convert(directory).c_str());

	std::list<std::wstring> files;
	std::list<std::wstring> directories;

	struct dirent *entry = NULL;

	while ((entry = ::readdir (dir)) != NULL)
	{
		if (entry->d_type == DT_REG)
		{
			std::wstring fileName = mailiverse::utilities::convert(entry->d_name);
			files.push_back(fileName);
		}
		else
		if (entry->d_type == DT_DIR && strcmp(entry->d_name, ".")!=0 && strcmp(entry->d_name, "..")!=0)
		{
			std::wstring fileName = mailiverse::utilities::convert(entry->d_name);
			directories.push_back (fileName);
		}
	}

	closedir (dir);

	std::list<std::wstring>::iterator i;
	for (i=directories.begin(); i!=directories.end(); ++i)
	{
		const std::wstring &name = *i;
		mailiverse::utilities::killDirectory (directory + L"/" + name);
	}
	
	for (i=files.begin(); i!=files.end(); ++i)
	{
		const std::wstring &name = *i;
		mailiverse::utilities::killFile (name);
	}
	
	killFile (directory);
#endif
}

bool mailiverse::utilities::killFile (const std::string &file)
{
	return
		mailiverse::utilities::killFile (
			mailiverse::utilities::convert (file)
		);
}

bool mailiverse::utilities::killFile (const std::wstring &file)
{
#ifdef WIN32
	return _wremove(toLongPath(file).c_str()) == 0;
#else
	return remove(mailiverse::utilities::convert(file).c_str()) == 0;
#endif
}

bool mailiverse::utilities::renameFile (const std::string &from, const std::string &to)
{
	return
		mailiverse::utilities::renameFile (
			mailiverse::utilities::convert (from),
			mailiverse::utilities::convert (to)
		);
}

bool mailiverse::utilities::renameFile (const std::wstring &from, const std::wstring &to)
{
#ifdef WIN32
	return _wrename (toLongPath(from).c_str(), toLongPath(to).c_str())==0;
#else
	return rename (mailiverse::utilities::convert(from).c_str(), mailiverse::utilities::convert(to).c_str())==0;
#endif	
}

std::string mailiverse::utilities::getFileName(const std::string & fullFileName)
{
	return
		mailiverse::utilities::convert (
			mailiverse::utilities::getFileName (
				mailiverse::utilities::convert (fullFileName)
			)
		);
}

std::wstring mailiverse::utilities::getFileName(const std::wstring & fullFileName)
{
	size_t lastDelimiterPos = fullFileName.find_last_of(DelimiterSetW);
	if (lastDelimiterPos == -1)
		return fullFileName;
	else
		return fullFileName.substr(lastDelimiterPos+1, fullFileName.length()-lastDelimiterPos-1);
}

std::string mailiverse::utilities::getDirectory(const std::string & fullFileName, bool includeLastSlash)
{
	return
		mailiverse::utilities::convert (
			mailiverse::utilities::getDirectory (
				mailiverse::utilities::convert (fullFileName),
				includeLastSlash
			)
		);
}

std::wstring mailiverse::utilities::getDirectory(const std::wstring & fullFileName, bool includeLastSlash)
{
	size_t lastDelimiterPos = fullFileName.find_last_of(DelimiterSetW);

	if (lastDelimiterPos == -1)
		return std::wstring();

	if (includeLastSlash)
		return fullFileName.substr(0, lastDelimiterPos+1);

	return fullFileName.substr(0, lastDelimiterPos);
}

time_t mailiverse::utilities::getFileLastModifiedTime(const std::string & fullFileName)
{
	return
		mailiverse::utilities::getFileLastModifiedTime (
			mailiverse::utilities::convert (fullFileName)
		);
}

time_t mailiverse::utilities::getFileLastModifiedTime(const std::wstring & fullFileName)
{
#ifdef WIN32

	WIN32_FILE_ATTRIBUTE_DATA data;
	bool exists = GetFileAttributesExW (toLongPath(fullFileName).c_str(), GetFileExInfoStandard, &data) > 0;

	if (exists)
		return FileTimeToTimeT(data.ftLastWriteTime);

	return 0;

#else

	statStruct status;

	int result = statFuncW(toLongPath(fullFileName).c_str(), &status);

	if (result != 0)
		return 0;
	
	return status.st_mtime;
#endif
}

bool mailiverse::utilities::setFileLastModifiedTime(const std::string & fullFileName, time_t time)
{
	return
		mailiverse::utilities::setFileLastModifiedTime (
			mailiverse::utilities::convert (fullFileName), time
		);
}

bool mailiverse::utilities::setFileLastModifiedTime(const std::wstring & fullFileName, time_t time)
{
#ifdef WIN32
	// http://msdn.microsoft.com/en-us/library/ms724205(v=VS.85).aspx


	HANDLE hFile = CreateFileW(fullFileName.c_str(), GENERIC_WRITE | GENERIC_READ, FILE_SHARE_WRITE | FILE_SHARE_READ, NULL, OPEN_EXISTING, 0, 0);
	if (!hFile)
		return false;

	FILETIME createTime, accessTime, writeTime;

	bool success = true;
	if (!GetFileTime(hFile,&createTime,&accessTime,&writeTime))
	{
		CloseHandle(hFile);
		return false;
	}
	
	TimetToFileTime (time, writeTime); 
	if (!SetFileTime(hFile,&createTime,&accessTime,&writeTime))
	{
		CloseHandle(hFile);
		return false;
	}

	CloseHandle(hFile);
	return true;
#else
	struct utimbuf timbuf;
	timbuf.actime = getFileAccessTime(fullFileName);
	timbuf.modtime = time;

	int result = utimeW(toLongPath(fullFileName).c_str(), &timbuf);

	if (result != 0)
		return false;
	
	return true;
#endif
}

time_t mailiverse::utilities::getFileCreationTime (const std::string & fullFileName)
{
	return
		mailiverse::utilities::getFileCreationTime (
			mailiverse::utilities::convert (fullFileName)
		);
}

time_t mailiverse::utilities::getFileCreationTime (const std::wstring & fullFileName)
{
#ifdef WIN32

	WIN32_FILE_ATTRIBUTE_DATA data;
	bool exists = GetFileAttributesExW (toLongPath(fullFileName).c_str(), GetFileExInfoStandard, &data) > 0;

	if (exists)
		return FileTimeToTimeT(data.ftCreationTime);

	return 0;

#else

	statStruct status;

	int result = statFuncW(toLongPath(fullFileName).c_str(), &status);

	if (result != 0)
		return 0;
	
	return status.st_ctime;

#endif
}

time_t mailiverse::utilities::getFileAccessTime (const std::string & fullFileName)
{
	return
		mailiverse::utilities::getFileAccessTime (
			mailiverse::utilities::convert (fullFileName)
		);
}

time_t mailiverse::utilities::getFileAccessTime (const std::wstring & fullFileName)
{
#ifdef WIN32

	WIN32_FILE_ATTRIBUTE_DATA data;
	bool exists = GetFileAttributesExW (toLongPath(fullFileName).c_str(), GetFileExInfoStandard, &data) > 0;

	if (exists)
		return FileTimeToTimeT(data.ftLastAccessTime);

	return 0;

#else

	statStruct status;

	int result = statFuncW(toLongPath(fullFileName).c_str(), &status);

	if (result != 0)
		return 0;
	
	return status.st_atime;

#endif
}

std::list<std::string> mailiverse::utilities::getFilesInDirectory (const std::string &directory, bool recursive)
{
	std::list<std::string> files;

	std::list<std::wstring> wfiles = 
		mailiverse::utilities::getFilesInDirectory (
			mailiverse::utilities::convert (directory),
			recursive
		);

	std::list<std::wstring>::iterator i;
	for (i=wfiles.begin(); i!=wfiles.end(); ++i)
	{
		files.push_back (mailiverse::utilities::convert (*i));
	}

	return files;
}

std::list<std::wstring> mailiverse::utilities::getFilesInDirectory (const std::wstring &directory, bool recursive)
{
	std::list<std::wstring> files;

#ifdef WIN32
	WIN32_FIND_DATAW data;
	HANDLE handle = ::FindFirstFileW( toLongPath(directory + L"*").c_str(), &data );

	if (!handle)
		return files;

	do
	{
		if (data.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)
		{
			std::wstring directoryname(data.cFileName);

			if ( (recursive) && (directoryname != L"." && directoryname != L".."))
			{
				std::list<std::wstring> recursivefiles = getFilesInDirectory (directory + directoryname + LocalDirectoryDelimiterW);

				std::list<std::wstring>::iterator i;
				for (i=recursivefiles.begin(); i!=recursivefiles.end(); ++i)
				{
					files.push_back( directoryname + LocalDirectoryDelimiterW + *i);
				}
			}
		}
		else
		{
			files.push_back (std::wstring(data.cFileName));
		}
	}
	while (::FindNextFileW (handle, &data));

	::FindClose (handle);
#else
	DIR *dir = ::opendir (mailiverse::utilities::convert(directory).c_str());
	if (!dir)
		return files;

	std::list<std::wstring> directories;

	struct dirent *entry = NULL;

	while ((entry = ::readdir (dir)) != NULL)
	{
		if (entry->d_type == DT_REG)
		{
			std::wstring name = mailiverse::utilities::convert(entry->d_name);
			files.push_back(name);
		}
		else
		if (recursive && entry->d_type == DT_DIR && strcmp(entry->d_name, ".")!=0 && strcmp(entry->d_name, "..")!=0)
		{
			std::wstring name = mailiverse::utilities::convert(entry->d_name);
			directories.push_back (name);
		}
	}

	closedir (dir);

	std::list<std::wstring>::iterator i;
	for (i=directories.begin(); i!=directories.end(); ++i)
	{
		const std::wstring &name = *i;
		std::list<std::wstring> recursive = mailiverse::utilities::getFilesInDirectory (directory + L"/" + name, true);
		std::list<std::wstring>::iterator i;
		for (i=recursive.begin(); i!=recursive.end(); ++i)
		{
			files.push_back (name + L"/" + *i);
		}
	}
#endif

	return files;
}

std::list<std::string> mailiverse::utilities::getAllEntitiesInDirectory (const std::string &directory, bool recursive)
{
	std::list<std::string> files;

	std::list<std::wstring> wfiles = 
		mailiverse::utilities::getAllEntitiesInDirectory (
			mailiverse::utilities::convert (directory),
			recursive
		);

	std::list<std::wstring>::iterator i;
	for (i=wfiles.begin(); i!=wfiles.end(); ++i)
	{
		files.push_back (mailiverse::utilities::convert (*i));
	}

	return files;
}

std::list<std::wstring> mailiverse::utilities::getAllEntitiesInDirectory (const std::wstring &directory, bool recursive)
{
	std::list<std::wstring> files;

#ifdef WIN32
	WIN32_FIND_DATAW data;
	HANDLE handle = ::FindFirstFileW( toLongPath(directory + L"*").c_str(), &data );

	if (!handle)
		return files;

	do
	{
		std::wstring directoryname(data.cFileName);

		if (directoryname != L"." && directoryname != L"..")
			files.push_back (std::wstring(data.cFileName));

		if (data.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)
		{
			if ( (recursive) && (directoryname != L"." && directoryname != L".."))
			{
				std::list<std::wstring> recursivefiles = getFilesInDirectory (directory + directoryname + LocalDirectoryDelimiterW);

				std::list<std::wstring>::iterator i;
				for (i=recursivefiles.begin(); i!=recursivefiles.end(); ++i)
				{
					files.push_back( directoryname + LocalDirectoryDelimiterW + *i);
				}
			}
		}
	}
	while (::FindNextFileW (handle, &data));

	::FindClose (handle);
#else
	DIR *dir = ::opendir (mailiverse::utilities::convert(directory).c_str());
	if (!dir)
		return files;

	std::list<std::wstring> directories;

	struct dirent *entry = NULL;

	while ((entry = ::readdir (dir)) != NULL)
	{
		std::wstring name = mailiverse::utilities::convert(entry->d_name);

		if (strcmp(entry->d_name, ".")!=0 && strcmp(entry->d_name, "..")!=0)
			files.push_back(name);
			
		if (recursive && entry->d_type == DT_DIR && strcmp(entry->d_name, ".")!=0 && strcmp(entry->d_name, "..")!=0)
		{
			directories.push_back (name);
		}
	}

	closedir (dir);

	std::list<std::wstring>::iterator i;
	for (i=directories.begin(); i!=directories.end(); ++i)
	{
		const std::wstring &name = *i;
		std::list<std::wstring> recursive = mailiverse::utilities::getAllEntitiesInDirectory (directory + L"/" + name, true);
		std::list<std::wstring>::iterator i;
		for (i=recursive.begin(); i!=recursive.end(); ++i)
		{
			files.push_back (name + L"/" + *i);
		}
	}
#endif

	return files;
}


bool mailiverse::utilities::copyFile (const std::string &from, const std::string &to, bool binary)
{
	return mailiverse::utilities::copyFile (
		mailiverse::utilities::convert (from),
		mailiverse::utilities::convert (to),
		binary
	);
}

bool mailiverse::utilities::copyFile (const std::wstring &from, const std::wstring &to, bool binary)
{
	mailiverse::utilities::ifstream in (from, binary ? std::ios::binary : std::ios::in);

	if (!in)
		return false;

	mailiverse::utilities::ofstream out (to, binary ? std::ios::binary : std::ios::out);
	
	if (!out)
		return false;

	while (out)
	{
		int c = in.get();
		
		if (!in)
			break;

		out.put(c);
	}

	if (!out)
	{
		out.close();
		mailiverse::utilities::killFile (to);
		return false;
	}

	return true;
}

std::string mailiverse::utilities::mungifyPath (const std::string &str)
{
	return mailiverse::utilities::convert (
		mailiverse::utilities::mungifyPath (
			mailiverse::utilities::convert (str)
		)
	);
}

std::wstring mailiverse::utilities::mungifyPath (const std::wstring &str)
{
	std::wstring mungify;

	// http://support.microsoft.com/?kbid=120138
	static wchar_t *symbols = L"$%\'`-@{}~!#()&_^+,.=[]\\/ ";

	unsigned int i;
	for (i=0; i<str.length(); ++i)
	{
		int c = str[i];
		if (! (isalnum(c) || (wcschr(symbols, c)!=NULL)) )
		{
			mungify += L"%" + mailiverse::utilities::convert(mailiverse::utilities::toString (c));
		}
		else
		{
			mungify += c;
		}
	}

	return mungify;
}
