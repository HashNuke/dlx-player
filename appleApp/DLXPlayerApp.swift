import DLXShared
import SwiftUI

@main
struct DLXPlayerApp: App {
    var body: some Scene {
        WindowGroup {
            Text(AppInfoKt.appDisplayName)
                .font(.largeTitle)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }
}
