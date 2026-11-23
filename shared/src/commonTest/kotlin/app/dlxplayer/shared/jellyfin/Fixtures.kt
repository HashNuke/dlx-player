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
