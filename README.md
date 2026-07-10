# AppFacade

Turn any web app into a standalone Android app.

AppFacade wraps URLs you choose in a native shell: each one gets its own
home-screen icon, opens fullscreen with no browser chrome, resumes exactly
where you left it, and can be locked behind your fingerprint. Built with
self-hosted web UIs in mind, but it works with any http(s) URL.

## Features

- **Standalone apps** — every URL you add becomes its own launcher icon with
  its own card in Recents; tapping the icon resumes the live instance instead
  of opening a new tab
- **Fingerprint lock (per app)** — lock any app on every loss of foreground;
  unlocks with biometrics or your lockscreen PIN. Locked content is hidden
  from the Recents switcher and screenshots.
- **Fullscreen (per app)** — immersive mode with system bars hidden; swipe
  from the edge to peek them
- **Dark mode** — the manager UI follows the system theme (Material You colors)
- **Cache management** — HTTP cache auto-clears when an app closes (logins
  untouched); manual "Clear cache" and "Deep clean" (full reset, with warning)
  for when a site accumulates bloat
- **Icon & name autofill** — favicon and page title are fetched when you add
  a URL, with a letter-tile fallback
- **File uploads** work inside the webview
- Plain-HTTP URLs are allowed (intended for LAN/self-hosted use — traffic to
  `http://` URLs is unencrypted)

## Install

Download the APK from the latest [GitHub Release](../../releases/latest) on
your phone and sideload it (allow "install unknown apps" for your browser or
file manager). Updates install over the previous version.

## Build from source

Standard Android Gradle project (AGP 9, JDK 17, `compileSdk 36`). Point
`local.properties` at an Android SDK (`sdk.dir=...`) and run:

```bash
./gradlew test assembleDebug
```

## Cutting a release (maintainers)

1. Bump `versionCode` and `versionName` in `app/build.gradle.kts`
2. Commit, then `git tag vX.Y.Z && git push origin master --tags`
3. CI builds, signs, and attaches the APK to a GitHub Release

Signing uses four repository secrets: `SIGNING_KEYSTORE_B64` (base64 keystore),
`SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`.
