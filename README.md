# Mobile Shared

Shared open source code for Instructure's mobile apps published as artifacts.

# Maven

The [espresso lib is published on bintray](https://bintray.com/instructure/maven/espresso).

- `snapshot` is released after every push to master
- tagged versions are automatically published as stable releases

```gradle
repositories {
    maven {
        url  "https://dl.bintray.com/instructure/maven" 
    }
}
    
dependencies {
    compile("com.github.instructure:espresso:snapshot") // snapshot release
    compile("com.github.instructure:espresso:1.0.0") // stable release
}
```
