# Native Bridge Android
This Kotlin library is the native part of a RPC communication bridge between your app and the webview javascript. [To function, it relies on a javascript counterpart to be setup](https://github.com/nrkno/nativebridge/).

The library has been tested on Android 4.4.4 and later, but will probably also work on older Android versions.

**Note:** For simplicity, we're using the term *"webview javascript"* to mean *"the javascript hosted in a site shown in the webview"*.

## Download
The library will soon be made available in a public Maven repo. Until then use the latest `.aar` in [Releases](https://github.com/nrkno/nativebridge-android/releases).

The library has a dependency on Jackson 2.8.9, which needs to be added for the `.aar` to compile:
```gradle
compile "com.fasterxml.jackson.core:jackson-core:2.8.9"
compile "com.fasterxml.jackson.core:jackson-databind:2.8.9"
compile "com.fasterxml.jackson.module:jackson-module-kotlin:2.8.9"
```

## Usage
Instead of using ```android.webkit.WebView```, use ```no.nrk.NativeBridgeWebView```. If you're already extending ```WebView```, you must instead extend ```NativeBridgeWebView```.

For deserializing and serializing data, create classes implementing the ```TopicData.In``` and ```TopicData.Out``` interfaces. ```TopicData.In``` corresponds to the JSON received _from_ the webview javascript, while ```TopicData.Out``` corresponds to the data we want to pass _to_ the webview javascript.

You will then need to add a handler. The handler makes it possible for the native app and the webview javascript to communicate. To do this, use the `nativeBridgeWebView.connection.addHandler(topic: String, callback: (T, WebViewConnection) -> Unit)`. The first parameter, `topic`, is a string that you and the developer responsible for implementing the javascript has agreed on. The webview javascript and the native app use the same `topic` to communicate. The second parameter is a closure, where `T` is your class implementing `TopicData.In`, and `WebViewConnection` is the `webview.connection` that can be used to send `TopicData.Out` to the webview javascript. 

#### Example:
```kotlin
webview.connection.addHandler("someTopic", { _ : SomeTopic.In, connection ->
    connection.send("someTopic", SomeTopic.Out("data"))
  }
)
```

## Error handling
If an error occurs, the app will pass data about this to the webview javascript, so the webview javascript can act accordingly. 

If we have a `topic` available, the errors will be passed on the same `topic`. If not, the errors will be passed on the topic `errors`.

### Possible errors
As of version 1.0.0, the following errors can be returned:

| Error situation | Error code | Error message |
| --- | --- | --- |
| Payload passed from webview cannot be deserialized to a topic.  | 1  | ```Illegal payload format``` |
| Missing topic object in payload: a `topic` object was not found in the payload passed from the webview  | 2  | ```Missing field: 'topic'```  |
| Missing data object in payload: a `data` object was not found in the payload passed from the webview  | 3  | ```Missing field: 'data'```  |
| No topic handler defined for topic  | 4  | ```Missing topic handler```  |
| Data doesn't match data expected by the topic handler  | 5  | ```Invalid data for topic. Expected data topic '$topic'```  |

### Example error response:
When we're able to determine a `topic`:
```json
{
  "topic": "someTopic",
  "errors": [
    {
      "code": 1,
      "message": "Missing topic handler"
    }
  ]
}
```

When we're not able to determine the `topic`, we will pass the errors on the `errors` topic:
```json
{
  "topic": "errors",
  "errors": [
    {
      "code": 2,
      "message": "Missing field: 'topic'"
    }
  ]
}
```

## License
MIT. See [LICENSE.txt](https://github.com/nrkno/nativebridge-android/blob/master/LICENSE.txt) for details.
