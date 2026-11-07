import AVFoundation
import CryptoKit
import ExpoModulesCore
import TTSDKFramework
import UIKit

final class BytePlusPlayerView: ExpoView, TTVideoEngineDelegate {
  private let engine = TTVideoEngine(ownPlayer: true)
  private let button = UIButton(type: .system)
  private var hasStarted = false

  required init(appContext: AppContext? = nil) {
    super.init(appContext: appContext)
    backgroundColor = .black
    do {
      try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default)
      try AVAudioSession.sharedInstance().setActive(true)
    } catch {
      NSLog("[BytePlusRepro] Audio session: %@", String(describing: error))
    }

    engine.delegate = self
    engine.enablePictureInPicture = TTVideoEngine.isPictureInPictureSupported()
    engine.canStartPictureInPictureAutomaticallyFromInline = true
    engine.setOptionForKey(VEKKey.VEKKeyViewScaleMode_ENUM.rawValue,
                          value: TTVideoEngineScalingMode.aspectFit.rawValue)

    let videoView = engine.playerView
    videoView.translatesAutoresizingMaskIntoConstraints = false
    addSubview(videoView)
    button.translatesAutoresizingMaskIntoConstraints = false
    button.setTitle("Play", for: .normal)
    button.setTitleColor(.white, for: .normal)
    button.addTarget(self, action: #selector(togglePlayback), for: .touchUpInside)
    addSubview(button)
    NSLayoutConstraint.activate([
      videoView.topAnchor.constraint(equalTo: safeAreaLayoutGuide.topAnchor),
      videoView.leadingAnchor.constraint(equalTo: leadingAnchor),
      videoView.trailingAnchor.constraint(equalTo: trailingAnchor),
      videoView.bottomAnchor.constraint(equalTo: button.topAnchor),
      button.leadingAnchor.constraint(equalTo: leadingAnchor),
      button.trailingAnchor.constraint(equalTo: trailingAnchor),
      button.bottomAnchor.constraint(equalTo: safeAreaLayoutGuide.bottomAnchor),
      button.heightAnchor.constraint(equalToConstant: 56),
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

  override func layoutSubviews() {
    super.layoutSubviews()
    guard window != nil, bounds.width > 0, bounds.height > 0, !hasStarted else { return }
    hasStarted = true
    engine.play()
  }

  override func didMoveToWindow() {
    super.didMoveToWindow()
    if window != nil { setNeedsLayout() }
  }

  @objc private func togglePlayback() {
    switch engine.playbackState {
    case .playing: engine.pause()
    default: engine.play()
    }
  }

  func videoEngine(_ videoEngine: TTVideoEngine,
                   playbackStateDidChanged state: TTVideoEnginePlaybackState) {
    DispatchQueue.main.async { [weak self] in
      self?.button.setTitle(state == .playing ? "Pause" : "Play", for: .normal)
    }
  }

  func videoEngineDidFinish(_ videoEngine: TTVideoEngine, error: Error?) {
    if let error { NSLog("[BytePlusRepro] Playback: %@", String(describing: error)) }
  }
  func videoEngineDidFinish(_ videoEngine: TTVideoEngine, videoStatusException status: Int) {
    NSLog("[BytePlusRepro] Finished: %ld", status)
  }
  func videoEngineUserStopped(_ videoEngine: TTVideoEngine) {}
  func videoEngineCloseAysncFinish(_ videoEngine: TTVideoEngine) {}

  deinit {
    engine.delegate = nil
    engine.stopPictureInPicture()
    engine.closeAysnc()
  }
}
