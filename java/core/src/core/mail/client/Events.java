/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client;

public class Events
{
	public static final String 
		Login = "onLogin",
		Initialize_Start = "Initialize_Start",
		Initialize_IndexedCacheAcquired = "Initialize_IndexedCacheAcquired",
		Initialize_IndexedCacheLoadFailed = "Initialize_IndexedCacheLoadFailed",
		Initialize_IndexedCacheLoadComplete = "Initialize_IndexedCacheLoadComplete",
		Initialize_FolderLoadComplete = "Initialize_FolderLoadComplete",
		FirstRunInitialization = "onFirstRunInitialization",
		Initialized = "onInitialized",
		NewMail = "onNewMail",
		DeleteMail = "onDeleteMail",
		LoadMail = "onLoadMail",
		LogNull = "onLogNull",
		NewFolder = "onNewFolder",
		DeleteFolder = "onDeleteFolder",
		LoadFolder = "onLoadFolder",
		LoadFolderPart = "onLoadFolderPart",
		NewConversation = "onNewConversation",
		ChangedConversation = "onChangedConversation",
		DeleteConversation = "onDeleteConversation",
		LoadConversation = "onLoadConversation",
		LoadConversationBlock = "onLoadConversationBlock",
		SendSucceeded = "onSendSucceeded",
		SendFailed = "onSendFailed",
		CheckRequest = "onCheckRequest",
		CheckBegin = "onCheckBegin",
		CheckSuccess = "onCheckSuccess",
		CheckFailure = "onCheckFailure",
		CheckEnd = "onCheckEnd",
		CheckStep = "onCheckStep",
		UploadBegin = "onUploadBegin",
		UploadEnd = "onUploadEnd",
		DownloadBegin = "onDownloadBegin",
		DownloadEnd = "onDownloadEnd",
		OriginalLoaded = "onOriginalLoaded",
		CacheDirty = "onCacheDirty",
		CacheClean = "onCacheClean",
		CacheFailure = "onCacheFailure",
		CacheBegin = "onCacheBegin",
		CacheEnd = "onCacheEnd",
		CacheSuccess = "onCacheSuccess",
		LoadAttachments = "onAttachmentsLoaded",
		LoadAttachmentsFailed = "onAttachmentsLoadedFailed";
}
