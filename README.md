# Native Bridge Android

The Android library is written in Kotlin.

## Download
Add the dependency to your app level build.gradle file:
```java
dependencies {
  compile 'no.nrk.nativebridge:nativebridge:1.0.0-SNAPSHOT'
}
```

Note that the library has a dependency on Jackson.

## Usage
Instead of using ```android.webkit.WebView```, use ```no.nrk.NativeBridgeWebView```. If you're already extending ```WebView```, you must instead extend ```NativeBridgeWebView```.

Create classes implementing the ```TopicData.In``` and ```TopicData.Out``` interfaces. ```TopicData.In``` corresponds to the JSON received _from_ the webview, while ```TopicData.Out``` corresponds to the data we want to pass _to_ the webview.

Register a handler for the datatype, and you've successfully established a bridge between your app and the webview:
```java
webview.connection.addHandler("someTopic", { _ : SomeTopic.In, connection ->
    connection.send("someTopic", SomeTopic.Out("data"))
  }
)
```
