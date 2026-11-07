import AVFoundation
import CryptoKit
import ExpoModulesCore
import TTSDKFramework
import UIKit

final class BytePlusPlayerView: ExpoView {
  private let engine = TTVideoEngine(ownPlayer: true)
  private let button = UIButton(type: .system)

  required init(appContext: AppContext? = nil) {
    super.init(appContext: appContext)
    backgroundColor = .black
    do {
      try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default)
      try AVAudioSession.sharedInstance().setActive(true)
    } catch {
      NSLog("[BytePlusRepro] Audio session: %@", String(describing: error))
    }

    engine.enablePictureInPicture = TTVideoEngine.isPictureInPictureSupported()
    engine.canStartPictureInPictureAutomaticallyFromInline = true
    engine.setOptionForKey(VEKKey.VEKKeyViewScaleMode_ENUM.rawValue,
                          value: TTVideoEngineScalingMode.aspectFit.rawValue)

    let videoView = engine.playerView
    videoView.translatesAutoresizingMaskIntoConstraints = false
    addSubview(videoView)
    button.translatesAutoresizingMaskIntoConstraints = false
    button.setTitle("Play / Pause", for: .normal)
    button.setTitleColor(.white, for: .normal)
    button.backgroundColor = UIColor.black.withAlphaComponent(0.6)
    button.layer.cornerRadius = 24
    button.addTarget(self, action: #selector(togglePlayback), for: .touchUpInside)
    addSubview(button)
    NSLayoutConstraint.activate([
      videoView.topAnchor.constraint(equalTo: topAnchor),
      videoView.leadingAnchor.constraint(equalTo: leadingAnchor),
      videoView.trailingAnchor.constraint(equalTo: trailingAnchor),
      videoView.bottomAnchor.constraint(equalTo: bottomAnchor),
      button.centerXAnchor.constraint(equalTo: centerXAnchor),
      button.bottomAnchor.constraint(equalTo: safeAreaLayoutGuide.bottomAnchor, constant: -16),
      button.widthAnchor.constraint(equalToConstant: 132),
      button.heightAnchor.constraint(equalToConstant: 48),
    ])

    guard let video = Bundle.main.object(forInfoDictionaryKey: "BYTEPLUS_VIDEO") as? [String: String],
          let videoId = video["videoId"], let url = video["url"] else {
      fatalError("Set video.videoId and video.url in local.config.json before building.")
    }
    if let auth = video["auth"], !auth.isEmpty {
      engine.setOptionForKey(VEKKey.VEKKeyDecryptEncryptedDecryptionKey_NSString.rawValue,
                            value: auth)
    }
    let cacheKey = Insecure.MD5.hash(data: Data(url.utf8))
      .map { String(format: "%02hhx", $0) }.joined()
    engine.setVideoEngineVideoSource(TTVideoEngineUrlSource(
      url: url, cacheKey: cacheKey, videoId: videoId
    ))
  }

  @objc private func togglePlayback() {
    switch engine.playbackState {
    case .playing: engine.pause()
    default: engine.play()
    }
  }

  deinit {
    engine.stopPictureInPicture()
    engine.closeAysnc()
  }
}
