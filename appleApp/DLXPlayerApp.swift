import DLXShared
import SwiftUI

@main
struct DLXPlayerApp: App {
    var body: some Scene {
        WindowGroup {
            Text(AppInfoKt.APP_DISPLAY_NAME)
                .font(.largeTitle)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }
}
