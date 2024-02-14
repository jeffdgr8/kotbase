import SwiftUI
import KotbaseNotes

@main
struct iOSApp: App {
    @Environment(\.scenePhase) var scenePhase

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onChange(of: scenePhase) { newPhase in
                    let replicationService = Di().replicationService
                    switch newPhase {
                    case .active: replicationService.startReplication()
                    case .background: replicationService.stopReplication()
                    default: break
                    }
                }
        }
    }
}
