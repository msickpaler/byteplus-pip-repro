const local = require('./local.config.json');

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
      ITSAppUsesNonExemptEncryption: false,
    },
  },
  plugins: ['./plugins/with-byteplus'],
};
