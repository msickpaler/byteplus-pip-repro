# BytePlus Picture-in-Picture reproduction

This repository contains three versions of the same minimal player:

- `native/`: a complete Swift/UIKit Xcode project with no React Native. Start here.
- `native-android/`: a single Android Activity with BytePlus and system PiP, without React Native. See [Android setup](native-android/README.md).
- The repository root: the same player inside a React Native/Expo app, matching our production environment.

## Native Xcode project (`native/`)

A single `UIViewController` with a full-screen `TTVideoEngine(ownPlayer: true).playerView` and a play/pause button. The player code is identical to the React Native version.

Requires a physical iPhone, Xcode, and CocoaPods.

1. Put your BytePlus license at `native/BytePlusPiPRepro/license.lic`.
2. Copy `native/ReproConfig.example.plist` to `native/BytePlusPiPRepro/ReproConfig.plist` and set your app ID and VOD playback information (see "VOD source" below).
3. Set the bundle identifier supported by your license and your signing team in `native/Config/Project.xcconfig`, or in Xcode under Signing & Capabilities.
4. Run `pod install` in `native/`, open `native/BytePlusPiPRepro.xcworkspace`, and run the `BytePlusPiPRepro` scheme on the iPhone.

The steps in "Reproduce" below apply to both versions.

## React Native/Expo version (repository root)

One React Native screen, one BytePlus VOD video, and one play/pause button over the full-screen player view. Uses TTVideoEngine's built-in Picture-in-Picture with `ownPlayer: true`. Playback starts only when the button is pressed. No player delegates, custom restoration logic, layout/lifecycle playback handlers, AVPlayer, sample-buffer layer, navigation, sign-in flow, or analytics.

Requires a physical iPhone, Xcode, CocoaPods, and Node (see `.node-version`). The SDK used here does not support the simulator. Expo Go cannot load this native module.

1. Copy `local.config.example.json` to `local.config.json`. Set your BytePlus app ID, a bundle identifier supported by your license, and the VOD playback information described below. Optionally add `appleTeamId` for signing.
2. Put your BytePlus license at `license.lic` in the repository root.
3. Run `npm ci`, `npm run prebuild`, and `npm run pods`.
4. Run `npm run ios:release` to build and install without Metro. For development, use `npm run ios`.

The license, local configuration, and generated iOS project are ignored by Git. Do not share built apps or the generated `ios` directory: they contain your license and playback credentials. Share tracked source files only.

## VOD source

Set `video.videoId` to the BytePlus VOD video ID and `video.url` to its VOD playback URL. For encrypted content, set `video.auth` to the encrypted decryption key returned with that URL; otherwise use an empty string. The native player passes this key through `VEKKeyDecryptEncryptedDecryptionKey_NSString` and uses `TTVideoEngineUrlSource`, matching the source app's VOD playback path. `auth` is not a `playAuthToken`.

Obtain these values using your own backend or BytePlus setup. This sample contains no backend credentials or service client. If the URL or key expires, update the local configuration, run prebuild again, and rebuild the app.

## Reproduce

Enable **Settings > General > Picture in Picture > Start PiP Automatically** on the iPhone. Open the app and press **Play / Pause** to start the configured VOD video. Go to the iPhone Home Screen, then use the system Picture-in-Picture restore button to return. Repeat after pausing in Picture-in-Picture. Observe whether the video briefly changes scale or goes black during restoration. The app's button label is fixed; each press toggles the SDK's current playback state.

SDK: `TTSDKFramework/Player 1.52.100.4-standard`. Integration reference: https://docs.byteplus.com/en/docs/byteplus-vod/docs-ios-player-picture-in-picture
