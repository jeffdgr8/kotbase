Pod::Spec.new do |spec|
    spec.name                     = 'couchbase_lite_paging'
    spec.version                  = '3.0.12-1.0.0-SNAPSHOT'
    spec.homepage                 = 'https://kotbase.dev/'
    spec.source                   = { :http=> ''}
    spec.authors                  = 'Jeff Lockhart, Couchbase'
    spec.license                  = 'Apache License, Version 2.0'
    spec.summary                  = 'Couchbase Lite Community Edition for Kotlin Multiplatform – AndroidX Paging Extensions'
    spec.vendored_frameworks      = 'build/cocoapods/framework/couchbase_lite_paging.framework'
    spec.libraries                = 'c++'
    spec.ios.deployment_target = '9.0'
    spec.osx.deployment_target = '10.11'
    spec.dependency 'CouchbaseLite', '3.0.12'
                
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':couchbase-lite-paging',
        'PRODUCT_MODULE_NAME' => 'couchbase_lite_paging',
    }
                
    spec.script_phases = [
        {
            :name => 'Build couchbase_lite_paging',
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