import SwiftUI
import SharedKit

@main
struct iOSApp: App {
    init() {
        MainViewControllerKt.initKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
