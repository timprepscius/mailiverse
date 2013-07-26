/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "Events.h"

using namespace mailiverse::mail::manager;
using namespace mailiverse;

const String
	Events::Login = "onLogin",
	Events::Initialize_Start = "Initialize_Start",
	Events::Initialize_FirstRun = "Initialize_FirstRun",
	Events::Initialize_IndexedCacheLoadFailed = "Initialize_IndexedCacheLoadFailed",
	Events::Initialize_IndexedCacheLoadComplete = "Initialize_IndexedCacheLoadComplete",
	Events::Initialize_FolderLoadComplete = "Initialize_FolderLoadComplete",
	Events::Initialized = "onInitialized",
	Events::NewMail = "onNewMail",
	Events::DeleteMail = "onDeleteMail",
	Events::LoadMail = "onLoadMail",
	Events::LogNull = "onLogNull",
	Events::NewFolder = "onNewFolder",
	Events::DeleteFolder = "onDeleteFolder",
	Events::LoadFolder = "onLoadFolder",
	Events::NewConversation = "onNewConversation",
	Events::ChangedConversation = "onChangedConversation",
	Events::DeleteConversation = "onDeleteConversation",
	Events::InitiateLoadConversation = "onInitiateLoadConversation",
	Events::LoadConversation = "onLoadConversation",
	Events::FolderListing = "onFolderListing",
	Events::SendSucceeded = "onSendSucceeded",
	Events::SendFailed = "onSendFailed",
	Events::CheckRequest = "onCheckRequest",
	Events::CheckBegin = "onCheckBegin",
	Events::CheckSuccess = "onCheckSuccess",
	Events::CheckFailure = "onCheckFailure",
	Events::CheckEnd = "onCheckEnd",
	Events::CheckStep = "onCheckStep",
	Events::UploadBegin = "onUploadBegin",
	Events::UploadEnd = "onUploadEnd",
	Events::DownloadBegin = "onDownloadBegin",
	Events::DownloadEnd = "onDownloadEnd",
	Events::OriginalLoaded = "onOriginalLoaded",
	Events::CacheDirty = "onCacheDirty",
	Events::CacheClean = "onCacheClean",
	Events::CacheFailure = "onCacheFailure",
	Events::CacheBegin = "onCacheBegin",
	Events::CacheEnd = "onCacheEnd",
	Events::CacheSuccess = "onCacheSuccess",
	Events::SettingsChanged = "onSettingsChanged",
	Events::SettingsLoaded = "onSettingsLoaded"
	;
