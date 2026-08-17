# Khma 🎧

**ხმა** (*khma* — Georgian for "sound / voice") — a clean, offline-friendly
**podcast player** for Android.

Subscribe to any podcast by its RSS feed, stream or download episodes, and keep
listening with the screen off — background playback with lock-screen controls,
built on **Media3 / ExoPlayer**.

## Planned features

- 📡 **Subscribe by RSS** — add any podcast feed; episodes parsed and stored locally.
- ▶️ **Background playback** — Media3 `ExoPlayer` in a `MediaSessionService`, with
  lock-screen / notification controls, audio focus, and Bluetooth/headset buttons.
- ⏱️ **Resume where you left off** — per-episode playback position persisted.
- ⏬ **Offline** — download episodes with `WorkManager` and play the local file.
- 🗂️ **Library** — subscriptions, episode lists, and a now-playing screen (Compose + Material 3).
- 🎚️ **Playback niceties** — variable speed, skip forward/back, sleep timer *(stretch)*.

## Tech

Single-module Android app: **Jetpack Compose** + Material 3, **Media3** (ExoPlayer +
MediaSession) for playback, **Room** for subscriptions/episodes and playback state,
**WorkManager** for downloads, **OkHttp** + `XmlPullParser` for RSS, **Coil** for artwork.

## Structure

```
data/     Room entities/DAO, RSS fetching + parsing, repository
playback/ Media3 MediaSessionService + player controller
ui/       Compose library, episode list, now-playing
```

## Status

🚧 Day 1 — README-first. See [issues](../../issues) for the roadmap.

## License

[MIT](LICENSE)
