# MemoAndroid

A android app for memos. [https://github.com/usememos/memos.git](https://github.com/usememos/memos.git)

This program is modified from [https://github.com/mudkipme/MoeMemosAndroid.git](https://github.com/mudkipme/MoeMemosAndroid.git)

Please give them a star, they made such a good app.

🍦`kotlin` is so nice!

## Why

Origin is a app for every person who uses memos.
But I want to have a app for myself or my family, I hate the host url and
username on `Login` page.
Make a custom version is IMPORTANT.

## Difference

1. Moved Sidebar
2. Moved Explore
3. BottomBar Navigation And Different Screen Size Responsible Layout
4. Some Different UI/UX

## How to

There will be no prebuilt apks.

You should build your own from source.

The host url and username could be in the file:

```text
./app/src/main/kotlin/cufoon/memo/android/ui/page/login/EndPoint.kt
```

It should contain content like following:

```kotlin
package cufoon.memo.android.ui.page.login

// I'm not sure, but there's no problem with URL ending with /
const val MEMOS_HOST_URL = "https://.../"
const val MEMOS_DEFAULT_USERNAME = "..."
```

Then open your android studio, just have a try! 😄

*tips: proxy is set in gradle.properties, if you are not in China, you can remove it*
