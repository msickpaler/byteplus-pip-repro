import ExpoModulesCore

public class BytePlusPlayerModule: Module {
  public func definition() -> ModuleDefinition {
    Name("BytePlusPlayer")
    View(BytePlusPlayerView.self) {}
  }
}
