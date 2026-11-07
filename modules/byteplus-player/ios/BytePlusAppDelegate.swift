import ExpoModulesCore
import TTSDKFramework

public class BytePlusAppDelegate: ExpoAppDelegateSubscriber {
  public func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
  ) -> Bool {
    guard let appId = Bundle.main.object(forInfoDictionaryKey: "BYTEPLUS_APP_ID") as? String,
          Bundle.main.url(forResource: "license", withExtension: "lic") != nil else {
      fatalError("Set local.config.json and add license.lic before building.")
    }
    let configuration = TTSDKConfiguration.defaultConfiguration(
      withAppID: appId,
      licenseName: "license.lic"
    )
    TTSDKManager.start(with: configuration)
    return true
  }
}
