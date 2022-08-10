Pod::Spec.new do |spec|
    spec.name                     = 'CouchbaseLite-KMP'
    spec.version                  = '3.0.2'
    spec.homepage                 = 'https://github.com/udobny/couchbase-lite-kmp'
    spec.source                   = { :http=> ''}
    spec.authors                  = 'Couchbase, Jeff Lockhart'
    spec.license                  = 'Apache License, Version 2.0'
    spec.summary                  = 'Couchbase Lite for Kotlin Multiplatform'
    spec.vendored_frameworks      = 'build/cocoapods/framework/CouchbaseLite_KMP.framework'
    spec.libraries                = 'c++'
    spec.ios.deployment_target = '9.0'
    spec.dependency 'CouchbaseLite', '3.0.2'
                
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':core',
        'PRODUCT_MODULE_NAME' => 'CouchbaseLite_KMP',
    }
                
    spec.script_phases = [
        {
            :name => 'Build CouchbaseLite-KMP',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                if [ "YES" = "$COCOAPODS_SKIP_KOTLIN_BUILD" ]; then
                  echo "Skipping Gradle build task invocation due to COCOAPODS_SKIP_KOTLIN_BUILD environment variable set to \"YES\""
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