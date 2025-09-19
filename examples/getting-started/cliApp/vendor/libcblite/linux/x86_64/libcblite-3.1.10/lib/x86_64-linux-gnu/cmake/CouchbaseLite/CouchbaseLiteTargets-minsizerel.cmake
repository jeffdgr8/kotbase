#----------------------------------------------------------------
# Generated CMake target import file for configuration "MinSizeRel".
#----------------------------------------------------------------

# Commands may need to know the format version.
set(CMAKE_IMPORT_FILE_VERSION 1)

# Import target "cblite" for configuration "MinSizeRel"
set_property(TARGET cblite APPEND PROPERTY IMPORTED_CONFIGURATIONS MINSIZEREL)
set_target_properties(cblite PROPERTIES
<<<<<<<< HEAD:examples/getting-started/cliApp/vendor/libcblite/linux/x86_64/libcblite-3.1.10/lib/x86_64-linux-gnu/cmake/CouchbaseLite/CouchbaseLiteTargets-minsizerel.cmake
  IMPORTED_LOCATION_MINSIZEREL "${_IMPORT_PREFIX}/lib/x86_64-linux-gnu/libcblite.so.3.1.10"
========
  IMPORTED_LOCATION_MINSIZEREL "${_IMPORT_PREFIX}/lib/aarch64-linux-gnu/libcblite.so.3.2.1"
>>>>>>>> ed58ae4fc (Update Couchbase Lite to 3.2.1):couchbase-lite-ee/vendor/libcblite/linux/arm64/libcblite-3.2.1/lib/aarch64-linux-gnu/cmake/CouchbaseLite/CouchbaseLiteTargets-minsizerel.cmake
  IMPORTED_SONAME_MINSIZEREL "libcblite.so.3"
  )

list(APPEND _IMPORT_CHECK_TARGETS cblite )
<<<<<<<< HEAD:examples/getting-started/cliApp/vendor/libcblite/linux/x86_64/libcblite-3.1.10/lib/x86_64-linux-gnu/cmake/CouchbaseLite/CouchbaseLiteTargets-minsizerel.cmake
list(APPEND _IMPORT_CHECK_FILES_FOR_cblite "${_IMPORT_PREFIX}/lib/x86_64-linux-gnu/libcblite.so.3.1.10" )
========
list(APPEND _IMPORT_CHECK_FILES_FOR_cblite "${_IMPORT_PREFIX}/lib/aarch64-linux-gnu/libcblite.so.3.2.1" )
>>>>>>>> ed58ae4fc (Update Couchbase Lite to 3.2.1):couchbase-lite-ee/vendor/libcblite/linux/arm64/libcblite-3.2.1/lib/aarch64-linux-gnu/cmake/CouchbaseLite/CouchbaseLiteTargets-minsizerel.cmake

# Commands beyond this point should not need to know the version.
set(CMAKE_IMPORT_FILE_VERSION)
