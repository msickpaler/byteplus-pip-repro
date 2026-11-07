const local = require('./local.config.json');

if (!local.video?.videoId || !local.video?.url) {
  throw new Error('Set video.videoId and video.url in local.config.json.');
}

module.exports = {
  name: 'BytePlusRepro',
  slug: 'byteplus-pip-repro',
  version: '1.0.0',
  platforms: ['ios'],
  orientation: 'portrait',
  ios: {
    bundleIdentifier: local.bundleIdentifier,
    ...(local.appleTeamId ? { appleTeamId: local.appleTeamId } : {}),
    infoPlist: {
      UIBackgroundModes: ['audio'],
      BYTEPLUS_APP_ID: local.appId,
      BYTEPLUS_VIDEO: local.video,
      ITSAppUsesNonExemptEncryption: false,
    },
  },
  plugins: ['./plugins/with-byteplus'],
};
