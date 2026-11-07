const { copyFileSync } = require('node:fs');
const path = require('node:path');
const { IOSConfig, withPodfile, withPodfileProperties, withXcodeProject } = require('expo/config-plugins');

module.exports = function withBytePlus(config) {
  config = withPodfile(config, (config) => {
    const source = "source 'https://github.com/byteplus-sdk/byteplus-specs.git'";
    if (!config.modResults.contents.includes(source)) {
      config.modResults.contents = `${source}\nsource 'https://cdn.cocoapods.org/'\n${config.modResults.contents}`;
    }
    return config;
  });
  config = withPodfileProperties(config, (config) => {
    config.modResults['ios.deploymentTarget'] = '16.4';
    return config;
  });
  return withXcodeProject(config, (config) => {
    const { projectRoot, platformProjectRoot, projectName } = config.modRequest;
    copyFileSync(path.join(projectRoot, 'license.lic'), path.join(platformProjectRoot, 'license.lic'));
    IOSConfig.XcodeUtils.addResourceFileToGroup({
      filepath: 'license.lic',
      groupName: projectName,
      isBuildFile: true,
      project: config.modResults,
    });
    return config;
  });
};
