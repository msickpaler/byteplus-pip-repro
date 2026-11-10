import AVFoundation
import CryptoKit
import TTSDKFramework
import UIKit

final class PlayerViewController: UIViewController {
  private let engine = TTVideoEngine(ownPlayer: true)
  private let button = UIButton(type: .system)
  private let video: ReproConfig.Video

  init(video: ReproConfig.Video) {
    self.video = video
    super.init(nibName: nil, bundle: nil)
  }

  @available(*, unavailable)
  required init?(coder: NSCoder) {
    fatalError("init(coder:) is not supported")
  }

  override var preferredStatusBarStyle: UIStatusBarStyle { .lightContent }

  override func viewDidLoad() {
    super.viewDidLoad()
    view.backgroundColor = .black
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
    view.addSubview(videoView)
    button.translatesAutoresizingMaskIntoConstraints = false
    button.setTitle("Play / Pause", for: .normal)
    button.setTitleColor(.white, for: .normal)
    button.backgroundColor = UIColor.black.withAlphaComponent(0.6)
    button.layer.cornerRadius = 24
    button.addTarget(self, action: #selector(togglePlayback), for: .touchUpInside)
    view.addSubview(button)
    NSLayoutConstraint.activate([
      videoView.topAnchor.constraint(equalTo: view.topAnchor),
      videoView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
      videoView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
      videoView.bottomAnchor.constraint(equalTo: view.bottomAnchor),
      button.centerXAnchor.constraint(equalTo: view.centerXAnchor),
      button.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -16),
      button.widthAnchor.constraint(equalToConstant: 132),
      button.heightAnchor.constraint(equalToConstant: 48),
    ])

    if !video.auth.isEmpty {
      engine.setOptionForKey(VEKKey.VEKKeyDecryptEncryptedDecryptionKey_NSString.rawValue,
                            value: video.auth)
    }
    let cacheKey = Insecure.MD5.hash(data: Data(video.url.utf8))
      .map { String(format: "%02hhx", $0) }.joined()
    engine.setVideoEngineVideoSource(TTVideoEngineUrlSource(
      url: video.url, cacheKey: cacheKey, videoId: video.videoId
    ))
  }

  @objc private func togglePlayback() {
    switch engine.playbackState {
    case .playing: engine.pause()
    default: engine.play()
    }
  }
}
