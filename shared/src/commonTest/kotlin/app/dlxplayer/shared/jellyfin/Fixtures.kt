package app.dlxplayer.shared.jellyfin

/**
 * Response fixtures captured from the local Jellyfin reference server
 * (Docker `jellyfin/jellyfin:latest`, v12.4.0). They record the real wire
 * format — including key casing — so the client is tested against ground
 * truth. The automated tests replay these strings through Ktor's
 * MockEngine and never talk to the reference server itself.
 */

internal val PUBLIC_SYSTEM_INFO_JSON: String =
    """
    {
        "LocalAddress": "http://172.17.0.2:8096",
        "ServerName": "DLX Reference",
        "Version": "12.4.0",
        "ProductName": "Jellyfin Server",
        "OperatingSystem": "",
        "Id": "4a828cdb4fc0494184c5d45b616d6423",
        "StartupWizardCompleted": true
    }
    """.trimIndent()

internal val AUTHENTICATION_RESULT_JSON: String =
    """
    {
        "User": {
            "Name": "root",
            "ServerId": "4a828cdb4fc0494184c5d45b616d6423",
            "Id": "2861f5b8509e46e9a6dca9a1c396501f",
            "HasPassword": true,
            "HasConfiguredPassword": true,
            "HasConfiguredEasyPassword": false,
            "EnableAutoLogin": false,
            "LastLoginDate": "2026-11-22T17:08:34.8879141Z",
            "LastActivityDate": "2026-11-22T17:08:34.8879141Z",
            "Configuration": {
                "PlayDefaultAudioTrack": true,
                "SubtitleLanguagePreference": "",
                "DisplayMissingEpisodes": false,
                "GroupedFolders": [],
                "SubtitleMode": "Default",
                "DisplayCollectionsView": false,
                "EnableLocalPassword": false,
                "OrderedViews": [],
                "LatestItemsExcludes": [],
                "MyMediaExcludes": [],
                "HidePlayedInLatest": true,
                "RememberAudioSelections": true,
                "RememberSubtitleSelections": true,
                "EnableNextEpisodeAutoPlay": true,
                "CastReceiverId": "F007D354"
            },
            "Policy": {
                "IsAdministrator": true,
                "IsHidden": true,
                "EnableCollectionManagement": false,
                "EnableSubtitleManagement": false,
                "EnableLyricManagement": false,
                "IsDisabled": false,
                "BlockedTags": [],
                "AllowedTags": [],
                "EnableUserPreferenceAccess": true,
                "AccessSchedules": [],
                "BlockUnratedItems": [],
                "EnableRemoteControlOfOtherUsers": true,
                "EnableSharedDeviceControl": true,
                "EnableRemoteAccess": true,
                "EnableLiveTvManagement": false,
                "EnableLiveTvAccess": true,
                "EnableMediaPlayback": true,
                "EnableAudioPlaybackTranscoding": true,
                "EnableVideoPlaybackTranscoding": true,
                "EnablePlaybackRemuxing": true,
                "ForceRemoteSourceTranscoding": false,
                "EnableContentDeletion": true,
                "EnableContentDeletionFromFolders": [],
                "EnableContentDownloading": true,
                "EnableSyncTranscoding": true,
                "EnableMediaConversion": true,
                "EnabledDevices": [],
                "EnableAllDevices": true,
                "EnabledChannels": [],
                "EnableAllChannels": true,
                "EnabledFolders": [],
                "EnableAllFolders": true,
                "InvalidLoginAttemptCount": 0,
                "LoginAttemptsBeforeLockout": -1,
                "MaxActiveSessions": 0,
                "EnablePublicSharing": true,
                "BlockedMediaFolders": [],
                "BlockedChannels": [],
                "RemoteClientBitrateLimit": 0,
                "AuthenticationProviderId": "Jellyfin.Server.Implementations.Users.DefaultAuthenticationProvider",
                "PasswordResetProviderId": "Jellyfin.Server.Implementations.Users.DefaultPasswordResetProvider",
                "SyncPlayAccess": "CreateAndJoinGroups"
            }
        },
        "SessionInfo": {
            "PlayState": {
                "CanSeek": false,
                "IsPaused": false,
                "IsMuted": false,
                "RepeatMode": "RepeatNone",
                "PlaybackOrder": "Default"
            },
            "AdditionalUsers": [],
            "Capabilities": {
                "PlayableMediaTypes": [],
                "SupportedCommands": [],
                "SupportsMediaControl": false,
                "SupportsPersistentIdentifier": true
            },
            "RemoteEndPoint": "172.17.0.1",
            "PlayableMediaTypes": [],
            "Id": "100db57848c0fdfe9b429f4530869122",
            "UserId": "2861f5b8509e46e9a6dca9a1c396501f",
            "UserName": "root",
            "Client": "DLX Player",
            "LastActivityDate": "2026-11-22T17:08:35.0768266Z",
            "LastPlaybackCheckIn": "0001-01-01T00:00:00.0000000Z",
            "DeviceName": "Dev Bootstrap",
            "DeviceId": "bootstrap-1",
            "ApplicationVersion": "0.1.0",
            "IsActive": true,
            "SupportsMediaControl": false,
            "SupportsRemoteControl": false,
            "NowPlayingQueue": [],
            "HasCustomDeviceName": false,
            "ServerId": "4a828cdb4fc0494184c5d45b616d6423",
            "SupportedCommands": []
        },
        "AccessToken": "f6547397831d44718e44f89bc0d66ced",
        "ServerId": "4a828cdb4fc0494184c5d45b616d6423"
    }
    """.trimIndent()

/** `GET /Users/Me` with a valid token (captured 2026-11-23). */
internal val USERS_ME_JSON: String =
    """
    {
        "Name": "root",
        "ServerId": "4a828cdb4fc0494184c5d45b616d6423",
        "Id": "2861f5b8509e46e9a6dca9a1c396501f",
        "HasPassword": true,
        "HasConfiguredPassword": true,
        "HasConfiguredEasyPassword": false,
        "EnableAutoLogin": false,
        "LastLoginDate": "2026-11-23T04:25:38.8474949Z",
        "LastActivityDate": "2026-11-23T04:25:38.8474949Z",
        "Configuration": {
            "PlayDefaultAudioTrack": true,
            "SubtitleLanguagePreference": "",
            "DisplayMissingEpisodes": false,
            "GroupedFolders": [],
            "SubtitleMode": "Default",
            "DisplayCollectionsView": false,
            "EnableLocalPassword": false,
            "OrderedViews": [],
            "LatestItemsExcludes": [],
            "MyMediaExcludes": [],
            "HidePlayedInLatest": true,
            "RememberAudioSelections": true,
            "RememberSubtitleSelections": true,
            "EnableNextEpisodeAutoPlay": true,
            "CastReceiverId": "F007D354"
        },
        "Policy": {
            "IsAdministrator": true,
            "IsHidden": true,
            "EnableCollectionManagement": false,
            "EnableSubtitleManagement": false,
            "EnableLyricManagement": false,
            "IsDisabled": false,
            "BlockedTags": [],
            "AllowedTags": [],
            "EnableUserPreferenceAccess": true,
            "AccessSchedules": [],
            "BlockUnratedItems": [],
            "EnableRemoteControlOfOtherUsers": true,
            "EnableSharedDeviceControl": true,
            "EnableRemoteAccess": true,
            "EnableLiveTvManagement": false,
            "EnableLiveTvAccess": true,
            "EnableMediaPlayback": true,
            "EnableAudioPlaybackTranscoding": true,
            "EnableVideoPlaybackTranscoding": true,
            "EnablePlaybackRemuxing": true,
            "ForceRemoteSourceTranscoding": false,
            "EnableContentDeletion": true,
            "EnableContentDeletionFromFolders": [],
            "EnableContentDownloading": true,
            "EnableSyncTranscoding": true,
            "EnableMediaConversion": true,
            "EnabledDevices": [],
            "EnableAllDevices": true,
            "EnabledChannels": [],
            "EnableAllChannels": true,
            "EnabledFolders": [],
            "EnableAllFolders": true,
            "InvalidLoginAttemptCount": 0,
            "LoginAttemptsBeforeLockout": -1,
            "MaxActiveSessions": 0,
            "EnablePublicSharing": true,
            "BlockedMediaFolders": [],
            "BlockedChannels": [],
            "RemoteClientBitrateLimit": 0,
            "AuthenticationProviderId": "Jellyfin.Server.Implementations.Users.DefaultAuthenticationProvider",
            "PasswordResetProviderId": "Jellyfin.Server.Implementations.Users.DefaultPasswordResetProvider",
            "SyncPlayAccess": "CreateAndJoinGroups"
        }
    }
    """.trimIndent()

/** `GET /System/Info` with a valid token (captured 2026-11-23). */
internal val SYSTEM_INFO_JSON: String =
    """
    {
        "OperatingSystemDisplayName": "",
        "HasPendingRestart": false,
        "IsShuttingDown": false,
        "SupportsLibraryMonitor": true,
        "WebSocketPortNumber": 8096,
        "CompletedInstallations": [],
        "CanSelfRestart": true,
        "CanLaunchWebBrowser": false,
        "ProgramDataPath": "/config",
        "WebPath": "/jellyfin/jellyfin-web",
        "ItemsByNamePath": "/config/metadata",
        "CachePath": "/cache",
        "LogPath": "/config/log",
        "InternalMetadataPath": "/config/metadata",
        "TranscodingTempPath": "/cache/transcodes",
        "CastReceiverApplications": [
            {
                "Id": "F007D354",
                "Name": "Stable"
            },
            {
                "Id": "6F511C87",
                "Name": "Unstable"
            }
        ],
        "HasUpdateAvailable": false,
        "EncoderLocation": "System",
        "SystemArchitecture": "X64",
        "LocalAddress": "http://172.17.0.2:8096",
        "ServerName": "DLX Reference",
        "Version": "12.4.0",
        "ProductName": "Jellyfin Server",
        "OperatingSystem": "",
        "Id": "4a828cdb4fc0494184c5d45b616d6423",
        "StartupWizardCompleted": true
    }
    """.trimIndent()

/** Device identity shared by the hermetic tests. */
internal val TEST_DEVICE =
    DeviceInfo(
        clientName = "DLX Player",
        deviceName = "Pixel 9",
        deviceId = "device-1",
        version = "0.1.0",
    )

/** `GET /Users/{userId}/Views` with a valid token (captured 2026-11-23). */
internal val VIEWS_JSON: String =
    """
    {
        "Items": [
            {
                "Name": "Movies",
                "ServerId": "8453f97817bc46b7bafd71b3d2c3848c",
                "Id": "f137a2dd21bbc1b99aa5c0f6bf02a805",
                "Etag": "606825731e971ba89101aebf55b1bad4",
                "DateCreated": "2026-11-23T04:47:57.2112601Z",
                "DateLastMediaAdded": "0001-01-01T00:00:00.0000000Z",
                "CanDelete": false,
                "CanDownload": false,
                "SortName": "movies",
                "ExternalUrls": [],
                "Path": "/config/root/default/Movies",
                "EnableMediaSourceDisplay": true,
                "ChannelId": null,
                "Taglines": [],
                "Genres": [],
                "PlayAccess": "Full",
                "RemoteTrailers": [],
                "ProviderIds": {},
                "IsFolder": true,
                "ParentId": "e9d5075a555c1cbc394eec4cef295274",
                "Type": "CollectionFolder",
                "People": [],
                "Studios": [],
                "GenreItems": [],
                "LocalTrailerCount": 0,
                "UserData": {
                    "PlaybackPositionTicks": 0,
                    "PlayCount": 0,
                    "IsFavorite": false,
                    "Played": false,
                    "Key": "f137a2dd-21bb-c1b9-9aa5-c0f6bf02a805",
                    "ItemId": "f137a2dd21bbc1b99aa5c0f6bf02a805"
                },
                "ChildCount": 1,
                "SpecialFeatureCount": 0,
                "DisplayPreferencesId": "f137a2dd21bbc1b99aa5c0f6bf02a805",
                "Tags": [],
                "PrimaryImageAspectRatio": 1.7777777777777777,
                "CollectionType": "movies",
                "ImageTags": {
                    "Primary": "c5a9cb51b49db07463198bc1adbfb7ec"
                },
                "BackdropImageTags": [],
                "ImageBlurHashes": {
                    "Primary": {
                        "c5a9cb51b49db07463198bc1adbfb7ec": "WMB|Tw#fIVuwn,]hkBW.WVayaff8E0W-xujca#j=R+WVf*WYbbbX"
                    }
                },
                "Chapters": [],
                "LocationType": "FileSystem",
                "MediaType": "Unknown",
                "LockedFields": [],
                "LockData": false
            },
            {
                "Name": "Music",
                "ServerId": "8453f97817bc46b7bafd71b3d2c3848c",
                "Id": "7e64e319657a9516ec78490da03edccb",
                "Etag": "3fb4d25f7aff5fb6080de48fe3a29723",
                "DateCreated": "2026-11-23T04:47:58.8916765Z",
                "DateLastMediaAdded": "0001-01-01T00:00:00.0000000Z",
                "CanDelete": false,
                "CanDownload": false,
                "SortName": "music",
                "ExternalUrls": [],
                "Path": "/config/root/default/Music",
                "EnableMediaSourceDisplay": true,
                "ChannelId": null,
                "Taglines": [],
                "Genres": [],
                "PlayAccess": "Full",
                "RemoteTrailers": [],
                "ProviderIds": {},
                "IsFolder": true,
                "ParentId": "e9d5075a555c1cbc394eec4cef295274",
                "Type": "CollectionFolder",
                "People": [],
                "Studios": [],
                "GenreItems": [],
                "LocalTrailerCount": 0,
                "UserData": {
                    "PlaybackPositionTicks": 0,
                    "PlayCount": 0,
                    "IsFavorite": false,
                    "Played": false,
                    "Key": "7e64e319-657a-9516-ec78-490da03edccb",
                    "ItemId": "7e64e319657a9516ec78490da03edccb"
                },
                "ChildCount": 1,
                "SpecialFeatureCount": 0,
                "DisplayPreferencesId": "7e64e319657a9516ec78490da03edccb",
                "Tags": [],
                "CollectionType": "music",
                "ImageTags": {},
                "BackdropImageTags": [],
                "ImageBlurHashes": {},
                "Chapters": [],
                "LocationType": "FileSystem",
                "MediaType": "Unknown",
                "LockedFields": [],
                "LockData": false
            },
            {
                "Name": "Shows",
                "ServerId": "8453f97817bc46b7bafd71b3d2c3848c",
                "Id": "a656b907eb3a73532e40e44b968d0225",
                "Etag": "c3186be2089552e6553ddcf7c090ce83",
                "DateCreated": "2026-11-23T04:47:57.6088263Z",
                "DateLastMediaAdded": "0001-01-01T00:00:00.0000000Z",
                "CanDelete": false,
                "CanDownload": false,
                "SortName": "shows",
                "ExternalUrls": [],
                "Path": "/config/root/default/Shows",
                "EnableMediaSourceDisplay": true,
                "ChannelId": null,
                "Taglines": [],
                "Genres": [],
                "PlayAccess": "Full",
                "RemoteTrailers": [],
                "ProviderIds": {},
                "IsFolder": true,
                "ParentId": "e9d5075a555c1cbc394eec4cef295274",
                "Type": "CollectionFolder",
                "People": [],
                "Studios": [],
                "GenreItems": [],
                "LocalTrailerCount": 0,
                "UserData": {
                    "PlaybackPositionTicks": 0,
                    "PlayCount": 0,
                    "IsFavorite": false,
                    "Played": false,
                    "Key": "a656b907-eb3a-7353-2e40-e44b968d0225",
                    "ItemId": "a656b907eb3a73532e40e44b968d0225"
                },
                "ChildCount": 1,
                "SpecialFeatureCount": 0,
                "DisplayPreferencesId": "a656b907eb3a73532e40e44b968d0225",
                "Tags": [],
                "CollectionType": "tvshows",
                "ImageTags": {},
                "BackdropImageTags": [],
                "ImageBlurHashes": {},
                "Chapters": [],
                "LocationType": "FileSystem",
                "MediaType": "Unknown",
                "LockedFields": [],
                "LockData": false
            }
        ],
        "TotalRecordCount": 3,
        "StartIndex": 0
    }
    """.trimIndent()

/** `GET /Users/{userId}/Items?parentId=…&startIndex=0&limit=20&sortBy=SortName`
 * with `fields=Overview,Genres&mediaTypes=Video` (captured 2026-11-23). */
internal val ITEMS_JSON: String =
    """
    {
        "Items": [
            {
                "Name": "Sample Movie",
                "ServerId": "8453f97817bc46b7bafd71b3d2c3848c",
                "Id": "a1d6b5566e7586a2ff99614f1950c129",
                "Container": "mov,mp4,m4a,3gp,3g2,mj2",
                "ChannelId": null,
                "Genres": [],
                "RunTimeTicks": 600000000,
                "ProductionYear": 2026,
                "IsFolder": false,
                "Type": "Movie",
                "GenreItems": [],
                "UserData": {
                    "PlaybackPositionTicks": 0,
                    "PlayCount": 0,
                    "IsFavorite": false,
                    "Played": false,
                    "Key": "a1d6b556-6e75-86a2-ff99-614f1950c129",
                    "ItemId": "a1d6b5566e7586a2ff99614f1950c129"
                },
                "VideoType": "VideoFile",
                "ImageTags": {
                    "Primary": "97f8cfef56c10461f535af44a7bcc136"
                },
                "BackdropImageTags": [],
                "ImageBlurHashes": {
                    "Primary": {
                        "97f8cfef56c10461f535af44a7bcc136": "W~KxSK${'$'}YSw*Zi%|royWnW;e;ahjZX4X4axe:ahfibvbEayV{a${'$'}k8"
                    }
                },
                "LocationType": "FileSystem",
                "MediaType": "Video"
            }
        ],
        "TotalRecordCount": 1,
        "StartIndex": 0
    }
    """.trimIndent()

/** `GET /Users/{userId}/Items/Resume?mediaTypes=Video` after reporting a stop at 2min
 * of the 400s sample movie (captured 2026-11-23; the server drops resume positions
 * for items shorter than MinResumeDurationSeconds=300, hence the long movie). */
internal val RESUME_JSON: String =
    """
    {
        "Items": [
            {
                "Name": "Sample Movie",
                "ServerId": "1dbcdd642f644482b13bce814f8e6ccb",
                "Id": "a1d6b5566e7586a2ff99614f1950c129",
                "Container": "mov,mp4,m4a,3gp,3g2,mj2",
                "ChannelId": null,
                "RunTimeTicks": 4000000000,
                "ProductionYear": 2026,
                "IsFolder": false,
                "Type": "Movie",
                "UserData": {
                    "PlayedPercentage": 30,
                    "PlaybackPositionTicks": 1200000000,
                    "PlayCount": 1,
                    "IsFavorite": false,
                    "LastPlayedDate": "2026-11-23T04:59:38.0488824Z",
                    "Played": false,
                    "Key": "a1d6b556-6e75-86a2-ff99-614f1950c129",
                    "ItemId": "a1d6b5566e7586a2ff99614f1950c129"
                },
                "VideoType": "VideoFile",
                "ImageTags": {
                    "Primary": "603d1cf70d73764bf09b90479631ab3f"
                },
                "BackdropImageTags": [],
                "ImageBlurHashes": {
                    "Primary": {
                        "603d1cf70d73764bf09b90479631ab3f": "W~KxPFxOS]*Zi~|=o#X3W;e?W.n%X6X4WCWFWWj?ovniayafnmj?"
                    }
                },
                "LocationType": "FileSystem",
                "MediaType": "Video"
            }
        ],
        "TotalRecordCount": 1,
        "StartIndex": 0
    }
    """.trimIndent()

/** `GET /Shows/NextUp?UserId=…` with episode 1 partially watched (captured 2026-11-23):
 * the server's next-up is the in-progress episode itself, carrying season and
 * series ids plus the resume position in UserData. */
internal val NEXTUP_JSON: String =
    """
    {
        "Items": [
            {
                "Name": "Demo Show S01E01 - Episode 1",
                "ServerId": "1dbcdd642f644482b13bce814f8e6ccb",
                "Id": "6becb517aba42ed659f2a5280c9988f3",
                "Container": "mov,mp4,m4a,3gp,3g2,mj2",
                "ChannelId": null,
                "RunTimeTicks": 3200000000,
                "IndexNumber": 1,
                "ParentIndexNumber": 1,
                "IsFolder": false,
                "Type": "Episode",
                "UserData": {
                    "PlayedPercentage": 18.75,
                    "PlaybackPositionTicks": 600000000,
                    "PlayCount": 1,
                    "IsFavorite": false,
                    "LastPlayedDate": "2026-11-23T05:00:06.0800982Z",
                    "Played": false,
                    "Key": "6becb517-aba4-2ed6-59f2-a5280c9988f3",
                    "ItemId": "6becb517aba42ed659f2a5280c9988f3"
                },
                "SeriesName": "Demo Show",
                "SeriesId": "997a914658183ac33bb8e7b24f7129f7",
                "SeasonId": "2cfd607d4da7e741c01adc926ef8f7b0",
                "SeasonName": "Season 1",
                "VideoType": "VideoFile",
                "ImageTags": {
                    "Primary": "f0a681594b744fdf1f097dc39afdae52"
                },
                "BackdropImageTags": [],
                "ImageBlurHashes": {
                    "Primary": {
                        "f0a681594b744fdf1f097dc39afdae52": "W~KxPE${'$'}YW-*Zj0|=t7W-W;aPWpjaWYSxX4aiWXfiXRj=WVWBnmj?"
                    }
                },
                "LocationType": "FileSystem",
                "MediaType": "Video"
            }
        ],
        "TotalRecordCount": 1,
        "StartIndex": 0
    }
    """.trimIndent()
