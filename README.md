# CapML
#### Capone Markup Language
A minimal, declarative markup language for Android UI. 
.capml files can be parsed from storage, and render elements quickly at runtime.

```
CapML
'~' - Denotes the type of Android UI Element.
'+' - Denotes the user facing content of the Element. Can be one line or many lines.
'=' - Denotes the key in the parser object's JsonObject.
'-' - Denotes end of defining element.
'#' - Denotes Comment.

All lines end in newline.

Ex.

~ET
+ Hint Value
-
```

The above produces:


![img.png](misc/img.png)

Current widgets -> Tag? Decorator?:
 - CheckBox -> CB
 - EditText -> ET
 - TextView -> TV
 - Spinner -> SP


### Prerequisites

 - Android SDK 30 which can be found [here](https://developer.android.com/studio/index.html#command-tools),
or more easily, [here](https://developer.android.com/studio)

 - You can use the Gradle Wrapper or download Gradle [here](https://gradle.org/install/)

 - Git

### Installing


If using Android Studio, create a new module under the root of the project.
You should be able to set up a Blank Activity Application from the menu.
In the Application's module level build.gradle, add the following line:

```groovy
dependencies{
    implementation(project(path: ":CapML"))
}
```

Run `./gradlew build` or `.\gradlew.bat build` to generate an aar file at
`./CapML/build/outputs/aar/CapML-debug.aar` if you want to add it to another project,

## Running the tests

Eventually? `./gradlew test` and `./gradlew connectedAndroidTest`

## Deployment

Build the aar file and add it to your project source code. 
Then add it as a dependency to your project.


## Contributing

Eventually will probably have guidelines

## Versioning

We use [Semantic Versioning](http://semver.org/) for versioning.

Current Version: 0.5.0

## Authors
- **Dennis Capone** - *Wrote parser*- 
  [dcap0](https://github.com/dcap0)
  
## License

See LICENSE.md

## Acknowledgement
 - That weird flow state.
