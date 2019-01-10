<h1 align="center">Network Mocking for Android UI Tests</h1>

**Goals**

* Improve the speed and stability of UI tests by mocking network interactions

* Do so without adding unnecessary complexity to the development/testing process

---


**Our network stack**

* **OkHttpClient** - An OkHttpClient singleton is used for the vast majority of network interactions in our apps, and accounts for the bulk of the tests that need to be covered.

* **Apollo (GraphQL)** - The Apollo client uses the OkHttpClient singleton, but this does not guarantee that solutions which work for OkHttpClient will automatically work for Apollo.

* **Picasso / Glide** - Image loading libraries. To my knowledge there are currently no UI tests which make assertions against network-loaded images. With some setup and a lot of code changes it may be possible to have both Glide and Picasso use the OkHttpClient singleton, but for the scope of our goals I do not consider mocking this functionality to be a necessity.

* **WebView** - Certain features of the app (like manual login) require the use of WebViews. As far as I can tell there is no sane approach to directly mock the network interactions of WebView using OkHttpClient. Tests which interact with WebViews would require a solution like a mock server or proxy. Fortunately, the number of tests in the category are relatively few (thanks to token login) and being unable to mock these tests might not be a dealbreaker for an otherwise viable solution.

* **Data Seeding** - Data seeding (OkHttp) presents a unique challenge in that nearly all tests make assertions against unique data generated during the seeding process. If we were to only mock the network interactions of the app itself, these tests would fail due to a data mismatch between the live seeded data and the mocked app data. Therefore, we need a solution that also mocks the network for seeding and correctly matches the mocks with the associated tests.

    * **Non-network generated data** - On a related note, some tests also use randomly generated non-network data (e.g. using utility functions like *randomString() *and* randomDouble()*). Because the tests assert against this data, it will need to be mockable in such a way as to be consistent between test runs. This might be a simple as using hard-coded values instead of random ones.

In my opinion, any solution for network mocking would need to address *at least* **OkHttpClient** and **Data Seeding**. This would cover almost all existing UI tests, serve to reinforce test stability, and significantly improve overall test speed.

* * *


**Explored options**

* **[okhttp/mockwebserver](https://github.com/square/okhttp/tree/master/mockwebserver)**

    * **Pros** - Provided as part of the OkHttp project, so it should be well maintained. It seems to have a solid foundation and appears to be fairly flexible. It could make a great starting point if we end up needing to implement a custom solution.

    * **Cons** - It doesn’t try to do much out-of-the-box. It might require a large amount of manual entry (disruptive to current workflow) or a lot of new structure in order to be workable. Requires app-side changes that could touch a lot of code.

* **[RESTMock](https://github.com/andrzejchm/RESTMock)**

    * **Pros** - Built on mockwebserver. More out-of-the-box functionality. Can read responses from asset files. Can create response chains and add arbitrary delays.

    * **Cons** - The project only has a handful of contributors and hasn’t been update in the past 4 months. Would require the same app-side changes as mockwebserver.

* **[OkReplay](https://github.com/airbnb/okreplay)**

    * **Pros** - VCR-like functionality significantly reduces (or eliminates) manual mocking. A decent contributor count. Updated within the past month. Reasonably flexible. Gradle plugin for managing tapes. App-side changes would likely be minimal, possible that changes to test flow would be minimal as well.

    * **Cons** - More opinionated / less flexible. Make take some persuasion to fit our use case.

---

<h1 align="center">Proof-of-Concept using <i>OkReplay</i></h1>

**Overview of results**

* Proof-of-Concept built using OkReplay. Makes heavy use of the VCR functionality.

* Tested against Teacher App.

* Avoids the need to mock the data seeding project by wrapping seed calls and reading/writing responses as faked network interactions on the tape. This same approach also works for wrapping locally-generated unique data (e.g. random strings).

* Works with GraphQL (only used in student context page) after some modifications.

* Aside from adding the *@OkReplay* annotation and wrapping randomly-generated data in a few places, no modifications were made to individual tests.

* With airplane mode *disabled*, **100% of recorded tests passed** for Teacher.

* With airplane mode *enabled*, **98%** of tests (153 of 156) passed. The 3 failed tests all use WebViews.

    * Note that this data was obtained after disabling the "This action requires an internet connection" dialog. Prior to this modification, nearly one third of tests failed due to its appearance.

* Overall, tests execute in appx. **60% less time** using mocked data (18 min) compared to using live data (46 min) on my local machine.

* * *


**Current behavior**

* Tapes are stored as yml files, one for each individual test. These are easily editable. In read mode they are read from the app’s bundled assets (this can be changed if needed). In write mode they are written to /sdcard/okreplay.

* The OkReplay gradle plugin adds two tasks - one for copying written tapes from a device’s sdcard to the project’s assets directory, and one for clearing written tapes from the device.

* Globally, tests can be switched from read mode to write mode by modifying the TapeMode value in InstructureTest.kt. The tape mode can be overridden for an individual test by specifying the TapeMode for that test’s *@OkReplay* annotation, which is convenient when adding or modifying individual tests.

* For tests that expect different responses to the same request (e.g. request course, change name, request course again to confirm name change), TapeMode.READ_SEQUENTIAL should be specified in the test’s annotation. If the global tape mode is writable, these tests will be overridden to use TestMode.WRITE_SEQUENTIAL.

* * *


**Limitations**

* **Does not work** for tests that use WebViews (e.g. slow login)

* Will not work (yet) for network image testing - Picasso/Glide.

* * *


**What’s next / To do**

* Allow configuration via gradle properties. This will enable automation options such as running only specific test categories or re-recording all tests without manually changing the tape mode in code.

* Set up InstructureRunner to skip the data seeding health check in read mode (it is currently disabled for all tests in this PoC).

* Ensure that tests not annotated with @OkReplay still execute normally.

* Determine if using OkReplay’s PermissionRule is necessary, given our use of GrantPermissionRule. 

* * *


**Other Considerations**

* Some tests are sensitive to timestamps (e.g. unsubmitted vs missing assignment). If not addressed, this could result in additional flakiness which directly opposes the stated goal of improved test stability. There are a few different ways we could handle this:

    * Don’t mock these tests; always run them live.

    * Record new tapes for these tests on a daily or weekly basis. This could be accomplished by adding a simple annotation to the affected tests and having Bitrise automatically re-record them and submit a PR on a recurring schedule.

    * Mock these tests using a different library that allows for dynamic responses.

    * Add functionality to allow replacing values in the response body (configurable per test).

* OkReplay expects the tapes for playback to be located in the app’s assets (this is configurable), but do we want to actually store them there?

* Should the tapes be considered private data? I assume we’ll want to exclude them from the open source repo either way.

* Our options are fairly limited for WebView network mocking. We can either run these tests live or use a mock server or proxy. It may technically be possible to use the OkHttpClient to fulfill WebView requests, but doing so would add unnecessary complexity and fragility.

* We should avoid mixing live and mocked data in the same test where possible.

* * *


**Conclusion**

Although not a perfect out-of-the-box match for our needs, **OkReplay** currently seems to be our best option for network mocking at the application level. With additional changes and polish, I expect the presented proof-of-concept could become a workable solution capable of achieving the desired goal of improving overall test speed and stability without introducing unnecessary complexity or significant changes to our development/testing workflow.

However, this solution does fail to cover 2% of tests. If we interpret our goals to mean that 100% of tests should be mockable then OkReplay alone will not be enough and we would need to perform additional research into other solutions such as WireMock.

