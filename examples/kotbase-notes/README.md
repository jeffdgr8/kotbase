# Kotbase Notes

The Kotbase Notes app is a full-featured MVVM Kotlin Multiplatform app using Kotbase for local storage. The app also
connects to [Couchbase Sync Gateway](https://www.couchbase.com/products/sync-gateway/) to authenticate a user and
synchronize data to a [Couchbase Server](https://www.couchbase.com/products/server/) backend database and between devices.

The app targets Android, iOS, and JVM Desktop platforms.

* `/kotbase-notes` is for code that will be shared across the Compose Multiplatform application.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains the iOS Xcode project. Even if you’re sharing your UI with Compose Multiplatform, you need this
  entry point for your iOS app. This is also where you should add SwiftUI code for your project.
  - The iOS app uses CocoaPods to link to the Kotlin shared framework as well as the CouchbaseLite native framework
    dependency.

## Configure Sync Gateway

You need to configure [Couchbase Server](
https://docs.couchbase.com/server/current/getting-started/do-a-quick-install.html) and [Sync Gateway](
https://docs.couchbase.com/sync-gateway/current/get-started-install.html) for the app to authenticate and connect to
sync data.

* Create a bucket `kotbase-notes`.
* Create a scope `user-<username>`.
* Create a `notes` collection within this scope.
* Configure the database `kotbase-notes` in Sync Gateway for the user's scope and collection.
    * Use this sync function:
    ```javascript
    function(doc, oldDoc, meta) {
        channelId = "user=<username>.notes";
        // Assign document to channel
        channel(channelId);
        // Grant user access to channel
        access("<username>", channelId);
    }
    ```
* Create user `<username>` in Sync Gateway.
    * Set the password.
    * Set collection access for `user-<username>.notes` with `"admin_channels": ["user=<username>.notes"]`.

## Roadmap

* [ ] SwiftUI for iOS
* [ ] Sync Gateway configuration app
