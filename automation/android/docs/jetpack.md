# Jetpack for Automation

[Frictionless Android testing: write once, run everywhere (Google I/O '18)
](https://www.youtube.com/watch?v=wYMIadv9iF8)

- Espresso for UI testing
- JUnit 4 for test running
- [truth](https://github.com/google/truth) for assertions
- Jacoco for code coverage
- Mockito for mocking
- Dagger 2 for dependency injection

## AndroidX

[AndroidX](https://developer.android.com/topic/libraries/support-library/androidx-overview) differentiates between `android.*` packages that are built into the Android operating system and `androix.*` which develop independently.

- `com.android.support.test.*` becomes `androidx.test.*`

## Testing Recommendations

- Use legacy espresso until androidx.test has a stable release
- Data model can be unit tested directly since it doesn't depend on Android classes
- If we need to test models with Android dependencies, [Roboletric](http://robolectric.org/blog/2018/05/09/robolectric-4-0-alpha/) is adopting Espresso's API

AndroidX Espresso is a package change only. The API is the same.

## Sunflower example

I upgraded the [android-sunflower](https://github.com/bootstraponline/android-sunflower/commit/d62a4e7edbe868b2e915dd7a19d088c5b3740ebe) example to use Jetpack Espresso. The code compiles and promptly crashes on startup with:

> java.lang.RuntimeException: Unable to instantiate instrumentation ComponentInfo{com.google.samples.apps.sunflower.test/androidx.test.runner.AndroidJUnit4}: java.lang.InstantiationException: java.lang.Class<androidx.test.runner.AndroidJUnit4> has no zero argument constructor

This is a sign that the alpha4 release of espresso is not ready for production.
