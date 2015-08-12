## Project Ares Payment App

This project showcases a use case of the payworks platform -- accepting payments with a card reader straight from a web shop!

The project features 2 components

1. The Web shop, which the merchant uses to initiate a transaction.

2. The Payment app, which takes over the payment once it is started.

The Web shop starts the payment app from the browser, which makes use of the [payworks Pay Button][1] to process card payments using a card reader.

We trigger the native payment app using deep-linking. In this project we talk about how we do this on Android.

For a simple overview on how the integrate the Payment App with your Web shop, click [here][3].


Payment App
---------------------------

The Web shop starts the payment app through a predefined URI scheme. The application can intercept this by registering an `<intent-filter>` with the required `scheme` for an Activity that can react to it.
Â
The Activity in the `AndroidMainfest.xml` is defined like this:

```xml
<activity
  android:name=".AresDeepLinkActivity"
  android:autoRemoveFromRecents="true"
  android:launchMode="singleInstance"
  android:noHistory="true"
  android:theme="@android:style/Theme.NoDisplay" >
    <intent-filter>
      <action android:name="android.intent.action.VIEW" />
      <category android:name="android.intent.category.DEFAULT" />
      <category android:name="android.intent.category.BROWSABLE" />
      <data android:scheme="payworks" />
    </intent-filter>
</activity>
```

**AresDeepLinkActivity** here reacts to the URI scheme `payworks://`. The `<action>` and `<category>` tags for the  `<intent-filter>` are required as shown above in order to send the intent with the URI to the Activity.

Here, we use the following URI with the `payworks://` scheme:

```
payworks://transaction/charge?sessionIdentifier=zxcv1234
                             &providerMode=TEST
                             &accessoryFamily=MIURA_MPI
                             &merchantIdentifier=asdf1234
                             &merchantSecretKey=qwer1234
```

We get the URI in our Activity through an Intent.
```java
  Uri deepLinkUri = getIntent().getData();
```

We then extract the required parameters from the URI and start the transaction with the Pay Button SDK. Refer to the  **AresDeepLinkActivity** on how we do this.

**Note:**

We have the following attributes for the AresDeepLinkActivity:

1. `android:autoRemoveFromRecents="true"` - To prevent this from appearing in the multitasking screen. Starting the activity from the multitasking screen triggers an old Intent and also the activity must not be restartable without a scheme.

2. `android:launchMode="singleInstance"` `android:noHistory="true"` `android:finishOnTaskLaunch="true"` - To only have a single instance of the Activity and prevent triggering multiple instances of the same Activity.

License
---------------------
    ProjectAres : http://www.payworksmobile.com

    The MIT License (MIT)

    Copyright (c) 2015 payworks GmbH

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.


 [1]: http://www.payworks.mpymnt.com/paybutton#android
 [2]: http://payworksmobile.com/
 [3]: http://www.payworks.mpymnt.com/node/203
