import TTSDKFramework
import UIKit

@main
final class AppDelegate: UIResponder, UIApplicationDelegate {
  var window: UIWindow?

  func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
  ) -> Bool {
    let config = ReproConfig.load()
    TTSDKManager.start(with: TTSDKConfiguration.defaultConfiguration(
      withAppID: config.appId,
      licenseName: "license.lic"
    ))

    let window = UIWindow(frame: UIScreen.main.bounds)
    window.rootViewController = PlayerViewController(video: config.video)
    window.makeKeyAndVisible()
    self.window = window
    return true
  }
}

struct ReproConfig {
  struct Video {
    let videoId: String
    let url: String
    let auth: String
  }

  let appId: String
  let video: Video

  static func load() -> ReproConfig {
    guard Bundle.main.url(forResource: "license", withExtension: "lic") != nil,
          let url = Bundle.main.url(forResource: "ReproConfig", withExtension: "plist"),
          let values = NSDictionary(contentsOf: url) as? [String: Any],
          let appId = values["appId"] as? String,
          let video = values["video"] as? [String: String],
          let videoId = video["videoId"],
          let playURL = video["url"] else {
      fatalError("Add license.lic and ReproConfig.plist before building. See README.md.")
    }
    return ReproConfig(
      appId: appId,
      video: Video(videoId: videoId, url: playURL, auth: video["auth"] ?? "")
    )
  }
}
