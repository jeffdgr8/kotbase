Pod::Spec.new do |spec|
    spec.name                     = 'Kotbase-KTX'
    spec.version                  = '3.0.12-1.0.0-SNAPSHOT'
    spec.homepage                 = 'https://github.com/jeffdgr8/kotbase'
    spec.source                   = { :http=> ''}
    spec.authors                  = 'Couchbase, MOLO17, Jeff Lockhart'
    spec.license                  = 'Apache License, Version 2.0'
    spec.summary                  = 'Couchbase Lite for Kotlin Multiplatform Kotlin Extensions'
    spec.vendored_frameworks      = 'build/cocoapods/framework/Kotbase_KTX.framework'
    spec.libraries                = 'c++'
    spec.ios.deployment_target = '9.0'
    spec.osx.deployment_target = '10.11'
    spec.dependency 'CouchbaseLite', '3.0.12'
                
    if !Dir.exist?('build/cocoapods/framework/Kotbase_KTX.framework') || Dir.empty?('build/cocoapods/framework/Kotbase_KTX.framework')
        raise "

        Kotlin framework 'Kotbase_KTX' doesn't exist yet, so a proper Xcode project can't be generated.
        'pod install' should be executed after running ':generateDummyFramework' Gradle task:

            ./gradlew :couchbase-lite-ktx:generateDummyFramework

        Alternatively, proper pod installation is performed during Gradle sync in the IDE (if Podfile location is set)"
    end
                
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':couchbase-lite-ktx',
        'PRODUCT_MODULE_NAME' => 'Kotbase_KTX',
    }
                
    spec.script_phases = [
        {
            :name => 'Build Kotbase-KTX',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
                  echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
                  exit 0
                fi
                set -ev
                REPO_ROOT="$PODS_TARGET_SRCROOT"
                "$REPO_ROOT/../gradlew" -p "$REPO_ROOT" $KOTLIN_PROJECT_PATH:syncFramework \
                    -Pkotlin.native.cocoapods.platform=$PLATFORM_NAME \
                    -Pkotlin.native.cocoapods.archs="$ARCHS" \
                    -Pkotlin.native.cocoapods.configuration="$CONFIGURATION"
            SCRIPT
        }
    ]
                
end