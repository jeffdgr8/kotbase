# Change Log

## 3.2.4-1.2.0
> 24 Oct 2025

* [Vector Search](https://kotbase.dev/current/vector-search/) ([#57](
  https://github.com/jeffdgr8/kotbase/pull/57)) — Couchbase Lite [3.2 API](
  https://docs.couchbase.com/couchbase-lite/3.2/cbl-whatsnew.html) ([#54](https://github.com/jeffdgr8/kotbase/pull/54))
    * [Android SDK](https://docs.couchbase.com/couchbase-lite/3.2/android/releasenotes.html#maint-3-2-4)
    * [Java SDK](https://docs.couchbase.com/couchbase-lite/3.2/java/releasenotes.html#maint-3-2-4)
    * [Objective-C SDK](https://docs.couchbase.com/couchbase-lite/3.2/objc/releasenotes.html#maint-3-2-4)
    * [C SDK](https://docs.couchbase.com/couchbase-lite/3.2/c/releasenotes.html#maint-3-2-4)
* [Support](https://jira.issues.couchbase.com/browse/CBL-6884) 16 KB page sizes ([#47](
  https://github.com/jeffdgr8/kotbase/issues/47))
* New log sink API ([#55](https://github.com/jeffdgr8/kotbase/pull/55))
* Paging extensions now use AndroidX Paging directly ([#52](https://github.com/jeffdgr8/kotbase/pull/52))
* Predictive model registration now available on Linux and Mingw platforms ([#57](
  https://github.com/jeffdgr8/kotbase/pull/57))
* Migrate kotlinx-datetime to 0.7.1 ([#48](https://github.com/jeffdgr8/kotbase/pull/48))
    * `kotlinx.datetime.Instant` is now `kotlin.time.Instant` in the Kotbase API
    * kotlinx-datetime is no longer an API dependency
* Update other dependencies
* Fix some memory allocation bugs on Linux & Mingw platforms ([#58](https://github.com/jeffdgr8/kotbase/pull/58), [#60](
  https://github.com/jeffdgr8/kotbase/pull/60))

## 3.1.11-1.1.2
> 1 Sep 2025

* Update Couchbase Lite dependency to 3.1.11 (JVM & Android) & 3.1.10 (Objective-C & C) ([#50](https://github.com/jeffdgr8/kotbase/pull/50))
    * [Android SDK](https://docs.couchbase.com/couchbase-lite/3.1/android/releasenotes.html#maint-3-1-11)
    * [Java SDK](https://docs.couchbase.com/couchbase-lite/3.1/java/releasenotes.html#maint-3-1-11)
    * [Objective-C SDK](https://docs.couchbase.com/couchbase-lite/3.1/objc/releasenotes.html#maint-3-1-10)
    * [C SDK](https://docs.couchbase.com/couchbase-lite/3.1/c/releasenotes.html#maint-3-1-10)
* Add full sync API to database configuration ([#49](https://github.com/jeffdgr8/kotbase/pull/49))
* Update to Kotlin 2.2.10
* Use AtomicFu compiler plugin ([#44](https://github.com/jeffdgr8/kotbase/pull/44))
* Update dependencies

## 3.1.9-1.1.1
> 28 Oct 2024

* Update Couchbase Lite dependency to 3.1.9 ([#29](https://github.com/jeffdgr8/kotbase/pull/29))
    * [Android SDK](https://docs.couchbase.com/couchbase-lite/3.1/android/releasenotes.html#maint-3-1-9)
    * [Java SDK](https://docs.couchbase.com/couchbase-lite/3.1/java/releasenotes.html#maint-3-1-9)
    * [Objective-C SDK](https://docs.couchbase.com/couchbase-lite/3.1/objc/releasenotes.html#maint-3-1-9)
    * [C SDK](https://docs.couchbase.com/couchbase-lite/3.1/c/releasenotes.html#maint-3-1-9)
* Update dependencies ([#30](https://github.com/jeffdgr8/kotbase/pull/30))

## 3.1.3-1.1.0
> 1 Feb 2023

* [Scopes and Collections](https://kotbase.dev/current/scopes-and-collections/) — Couchbase Lite [3.1 API](
  https://docs.couchbase.com/couchbase-lite/3.1/cbl-whatsnew.html) ([#11](https://github.com/jeffdgr8/kotbase/pull/11))
    * [Android SDK v3.1.3](https://docs.couchbase.com/couchbase-lite/3.1/android/releasenotes.html#maint-3-1-3)
    * [Java SDK v3.1.3](https://docs.couchbase.com/couchbase-lite/3.1/java/releasenotes.html#maint-3-1-3)
    * [Objective-C SDK v3.1.4](https://docs.couchbase.com/couchbase-lite/3.1/objc/releasenotes.html#maint-3-1-4)
    * [C SDK v3.1.3](https://docs.couchbase.com/couchbase-lite/3.1/c/releasenotes.html#maint-3-1-3)
* Update to Kotlin 1.9.22 ([8546e4b](
  https://github.com/jeffdgr8/kotbase/commit/8546e4ba1ffacacfd05194da7deaec8e47851700))
* Handle empty log domain set ([00db837](
  https://github.com/jeffdgr8/kotbase/commit/00db8379c5657a8c3719c897811c43540f517378))
* **Source-incompatible change:** Convert `@Throws` getter functions to properties ([#12](
  https://github.com/jeffdgr8/kotbase/pull/12))
    * `Database.getIndexes()` -> `Database.indexes`
    * `Replicator.getPendingDocumentIds()` -> `Replicator.pendingDocumentIds`
* Make `Expression`, `as`, and `from` query builder functions `infix` ([#14](
  https://github.com/jeffdgr8/kotbase/pull/14))

### KTX extensions:

* Add `Expression` math operator functions ([148399d](
  https://github.com/jeffdgr8/kotbase/commit/148399d8e692a9f32d8fe82d00e544d1e72ba573))
* Add `fetchContext` to `documentFlow`, default to `Dispatchers.IO` ([2abe61a](
  https://github.com/jeffdgr8/kotbase/commit/2abe61ab52dd98edd4b90b029d4277ccbd9332e0))
* Add `mutableArrayOf`, `mutableDictOf`, and `mutableDocOf`, collection and doc creation functions ([#13](
  https://github.com/jeffdgr8/kotbase/pull/13))
* `selectDistinct`, `from`, `as`, and `groupBy` convenience query builder functions ([#14](
  https://github.com/jeffdgr8/kotbase/pull/14))

## 3.0.15-1.0.1
> 15 Dec 2023

* Make `Replicator` `AutoCloseable` ([#2](https://github.com/jeffdgr8/kotbase/pull/2))
* Avoid memory leaks with `memScoped` `toFLString()` ([#3](https://github.com/jeffdgr8/kotbase/pull/3))
* Update Couchbase Lite to 3.0.15 ([#4](https://github.com/jeffdgr8/kotbase/pull/4)):
    * [Android SDK v3.0.15](https://docs.couchbase.com/couchbase-lite/3.0/android/releasenotes.html#maint-3-0-15)
    * [Java SDK v3.0.15](https://docs.couchbase.com/couchbase-lite/3.0/java/releasenotes.html#maint-3-0-15)
    * [Objective-C SDK v3.0.15](https://docs.couchbase.com/couchbase-lite/3.0/objc/releasenotes.html#maint-3-0-15)
    * [C SDK v3.0.15](https://docs.couchbase.com/couchbase-lite/3.0/c/releasenotes.html#maint-3-0-15)
* Update to Kotlin 1.9.21 ([#5](https://github.com/jeffdgr8/kotbase/pull/5))
* K2 compiler compatibility ([#7](https://github.com/jeffdgr8/kotbase/pull/7))
* Update kotlinx-serialization, kotlinx-datetime, and kotlinx-atomicfu ([#8](
  https://github.com/jeffdgr8/kotbase/pull/8))
* Use default hierarchy template source set names ([#9](https://github.com/jeffdgr8/kotbase/pull/9))

## 3.0.12-1.0.0
> 1 Nov 2023

Initial public release

Using Couchbase Lite:

* [Android SDK v3.0.12](https://docs.couchbase.com/couchbase-lite/3.0/android/releasenotes.html#maint-3-0-12)
* [Java SDK v3.0.12](https://docs.couchbase.com/couchbase-lite/3.0/java/releasenotes.html#maint-3-0-12)
* [Objective-C SDK v3.0.12](https://docs.couchbase.com/couchbase-lite/3.0/objc/releasenotes.html#maint-3-0-12)
* [C SDK v3.0.12](https://docs.couchbase.com/couchbase-lite/3.0/c/releasenotes.html#maint-3-0-12)
