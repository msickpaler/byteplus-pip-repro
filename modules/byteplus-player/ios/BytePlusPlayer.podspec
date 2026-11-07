Pod::Spec.new do |s|
  s.name = 'BytePlusPlayer'
  s.version = '1.0.0'
  s.summary = 'Minimal BytePlus Picture-in-Picture reproduction'
  s.description = s.summary
  s.license = { :type => 'UNLICENSED' }
  s.author = 'Reproduction app'
  s.homepage = 'https://docs.byteplus.com/en/docs/byteplus-vod/docs-ios-player-picture-in-picture'
  s.source = { :git => 'https://github.com/byteplus-sdk/VideoOneSolutions.git' }
  s.platform = :ios, '16.4'
  s.swift_version = '5.9'
  s.static_framework = true
  s.source_files = '*.swift'
  s.dependency 'ExpoModulesCore'
  s.dependency 'TTSDKFramework/Player', '1.52.100.4-standard'
end
