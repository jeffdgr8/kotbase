import SwiftUI
import shared
import KMPNativeCoroutinesAsync

struct ContentView: View {

    @State private var inputValue = ""
    @State private var replicate = false
    @State private var task: Task<Void, Error>? = nil

	var body: some View {
        VStack {
            HStack(spacing: 16) {
                Text("Input value")
                TextField("Doc update input value", text: $inputValue)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            HStack {
                Spacer()
                Button(action: {
                    task?.cancel()
                    task = Task {
                        await databaseWork(inputValue: inputValue, replicate: replicate)
                    }
                }) {
                    Text("Run database work")
                }
                .padding(.trailing, 16)
                Spacer()
                Text("Replicate")
                Toggle("", isOn: $replicate).labelsHidden()
                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 8)
            LogView()
        }
        .onDisappear {
            task?.cancel()
            task = nil
        }
	}
}

struct LogView: View {

    @State private var logOutput = ""
    @Namespace private var bottom

    var body: some View {
        ScrollView(.horizontal) {
            ScrollViewReader { proxy in
                ScrollView(.vertical) {
                    Text(logOutput)
                        .padding(8)
                        .font(.system(size: 12, design: .monospaced))
                        .id(bottom)
                }
                .task {
                    do {
                        for try await logs in asyncSequence(for: Log().output) {
                            logOutput = logs
                            withAnimation {
                                proxy.scrollTo(bottom)
                            }
                        }
                    } catch {
                        print("Failed with error: \(error)")
                    }
                }
            }
        }
        .background(Color(red: 0.8, green: 0.8, blue: 0.8))
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}

private let tag = "IOS_APP"

private func databaseWork(inputValue: String, replicate: Bool) async {
    let helper = SharedDbWork()
    helper.createDb(dbName: "iosApp-db")
    let docId = helper.createDoc()
    Log().i(tag: tag, msg: "Created document :: \(docId)")
    helper.retrieveDoc(docId: docId)
    helper.updateDoc(docId: docId, inputValue: inputValue)
    Log().i(tag: tag, msg: "Updated document :: \(docId)")
    helper.queryDocs()
    if replicate {
        do {
            for try await change in asyncSequence(for: helper.replicate()) {
                Log().i(tag: tag, msg: "Replicator Change :: \(change)")
            }
        } catch {
            print("Failed with error: \(error)")
        }
    }
}
