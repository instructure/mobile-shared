# mobile-shared
Shared open source code for Instructure's mobile apps published as artifacts.

Pull in the espresso lib using:

```groovy
implementation('com.github.instructure.mobile-shared:espresso:51a6d4d723')
```

Use the latest commit hash from this repo.

# Publishing

The [espresso lib is published on jcenter](https://bintray.com/instructure/maven/espresso).

- `snapshot` is released after every push to master
- tagged versions are automatically published as stable releases

# Consuming

```gradle
repositories {
    maven {
        url  "https://dl.bintray.com/instructure/maven" 
    }
}
    
dependencies {
    compile("instructure:espresso:snapshot") // snapshot release
    compile("instructure:espresso:1.0.0") // stable release
}
```
