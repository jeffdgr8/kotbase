#----------------------------------------------------------------
# Generated CMake target import file for configuration "MinSizeRel".
#----------------------------------------------------------------

# Commands may need to know the format version.
set(CMAKE_IMPORT_FILE_VERSION 1)

# Import target "cblite" for configuration "MinSizeRel"
set_property(TARGET cblite APPEND PROPERTY IMPORTED_CONFIGURATIONS MINSIZEREL)
set_target_properties(cblite PROPERTIES
  IMPORTED_LOCATION_MINSIZEREL "${_IMPORT_PREFIX}/lib/x86_64-linux-gnu/libcblite.so.3.2.4"
  IMPORTED_SONAME_MINSIZEREL "libcblite.so.3"
  )

list(APPEND _cmake_import_check_targets cblite )
list(APPEND _cmake_import_check_files_for_cblite "${_IMPORT_PREFIX}/lib/x86_64-linux-gnu/libcblite.so.3.2.4" )

# Commands beyond this point should not need to know the version.
set(CMAKE_IMPORT_FILE_VERSION)
