# BytePlus Picture-in-Picture reproduction

One React Native screen, one fixed public MP4, and one native play/pause button. Uses TTVideoEngine's built-in Picture-in-Picture with `ownPlayer: true`. No custom restoration delegates, AVPlayer, sample-buffer layer, navigation, authentication, or analytics.

Requires a physical iPhone, Xcode, CocoaPods, and Node (see `.node-version`). The SDK used here does not support the simulator. Expo Go cannot load this native module.

1. Copy `local.config.example.json` to `local.config.json`. Set your BytePlus app ID and a bundle identifier supported by your license. Optionally add `appleTeamId` for signing.
2. Put your BytePlus license at `license.lic` in the repository root.
3. Run `npm ci`, `npm run prebuild`, and `npm run pods`.
4. Run `npm run ios:release` to build and install without Metro. For development, use `npm run ios`.

The license, local configuration, and generated iOS project are ignored by Git. Do not share built apps or the generated `ios` directory: they contain your license/configuration. Share tracked source files only.

## Reproduce

Enable **Settings > General > Picture in Picture > Start PiP Automatically** on the iPhone. The app starts playing the MP4 when opened. Go to the iPhone Home Screen, then use the system Picture-in-Picture restore button to return. Repeat after pausing in Picture-in-Picture. Observe whether the video briefly changes scale or goes black during restoration.

SDK: `TTSDKFramework/Player 1.52.100.4-standard`. Integration reference: https://docs.byteplus.com/en/docs/byteplus-vod/docs-ios-player-picture-in-picture
