Pod::Spec.new do |spec|
    spec.name                     = 'CouchbaseLite-Enterprise-KMP'
    spec.version                  = '3.0.5'
    spec.homepage                 = 'https://github.com/udobny/couchbase-lite-kmp'
    spec.source                   = { :http=> ''}
    spec.authors                  = 'Couchbase, Jeff Lockhart'
    spec.license                  = 'Custom, Apache License, Version 2.0'
    spec.summary                  = 'Couchbase Lite Enterprise Edition for Kotlin Multiplatform'
    spec.vendored_frameworks      = 'build/cocoapods/framework/CouchbaseLite_Enterprise_KMP.framework'
    spec.libraries                = 'c++'
    spec.ios.deployment_target = '9.0'
    spec.osx.deployment_target = '10.11'
    spec.dependency 'CouchbaseLite-Enterprise', '3.0.2'
                
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':couchbase-lite-ee',
        'PRODUCT_MODULE_NAME' => 'CouchbaseLite_Enterprise_KMP',
    }
                
    spec.script_phases = [
        {
            :name => 'Build CouchbaseLite-Enterprise-KMP',
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