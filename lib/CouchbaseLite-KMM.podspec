Pod::Spec.new do |spec|
    spec.name                     = 'CouchbaseLite-KMM'
    spec.version                  = '3.0.0'
    spec.homepage                 = 'https://udobny.com/couchbase-lite-kmm'
    spec.source                   = { :http=> ''}
    spec.authors                  = 'Couchbase, Jeff Lockhart'
    spec.license                  = 'Apache License, Version 2.0'
    spec.summary                  = 'Couchbase Lite for Kotlin Multiplatform'
    spec.vendored_frameworks      = 'build/cocoapods/framework/CouchbaseLite_KMM.framework'
    spec.libraries                = 'c++'
    spec.ios.deployment_target = '9.0'
    spec.dependency 'CouchbaseLite'
                
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':lib',
        'PRODUCT_MODULE_NAME' => 'CouchbaseLite_KMM',
    }
                
    spec.script_phases = [
        {
            :name => 'Build CouchbaseLite-KMM',
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