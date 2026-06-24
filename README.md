# Night Dial — Android radio with automatic lyrics

A native Android app: pick a station on a tuner roller, it plays, reads the live
song title from the stream (ICY metadata, natively — no proxy, no CORS), and
shows the lyrics in large type. No server to run.

You build the APK once with GitHub Actions (in the cloud — you don't install the
Android toolchain), download it, and sideload it.

---

## 1. Put the project on GitHub

The easiest path from your phone is the Debian VM you already use.

In the VM (install git first if needed: `sudo apt update && sudo apt install -y git`):

```bash
cd ~                          # or wherever you keep projects
# get the project here (see "Getting the files in" below), then:
cd NightDialRadio
git init
git add .
git commit -m "Night Dial radio app"
git branch -M main
```

Create an empty repository on github.com (no README, from the phone browser is
fine), then connect and push:

```bash
git remote add origin https://github.com/USERNAME/NightDialRadio.git
git push -u origin main
```

GitHub will ask for a Personal Access Token as the password (Settings →
Developer settings → Personal access tokens → Fine-grained → repo access).

### Getting the files in
You downloaded `NightDialRadio.zip`. On the phone it lands in Downloads, which
the Terminal app shares into the VM. Find and unzip it:

```bash
find / -name 'NightDialRadio.zip' 2>/dev/null     # locate it
unzip /path/it/found/NightDialRadio.zip -d ~       # creates ~/NightDialRadio
```

(If you have a computer, it's even simpler: unzip there and push from there.)

---

## 2. Let GitHub build the APK

Pushing to `main` triggers the workflow in `.github/workflows/build.yml`.

- On github.com open the repo → **Actions** tab → watch the "Build APK" run.
- When it finishes green, open the run → **Artifacts** → download
  **NightDial-debug-apk**. That's a zip containing `app-debug.apk`.

If the run fails (red), open it, expand the failed step, and copy the last
20–30 lines. Most first-time failures are a version mismatch in the Gradle
files — send me those lines and I'll give you the one-line fix.

You can also re-run manually any time: Actions → Build APK → "Run workflow".

---

## 3. Install it on the phone

1. Download the artifact zip to the phone and extract `app-debug.apk`.
2. Tap the APK. Android/GrapheneOS will ask to allow installs from your
   browser/files app the first time — allow it, then confirm the install.
3. Open **Night Dial**, (optionally allow the notification when asked), tap a
   station. Audio plays; the song title and lyrics fill in on their own.

It's a *debug-signed* APK — perfect for installing on your own devices and
sharing with friends who sideload. (A signed *release* build is a later step if
you ever want the Play Store or a stable update signature.)

---

## Notes

- **Background play & lock screen:** it uses a Media3 MediaSessionService, so
  audio keeps going with the screen off and shows a media notification.
- **Lyrics:** same six providers as before (lrclib, NetEase, Lyrist, Genius,
  lyrics.ovh, ChartLyrics), tried in order. The "Find" box is a manual fallback
  for stretches when a station sends no song info.
- **Editing stations:** they're in
  `app/src/main/java/com/nightdial/radio/Stations.kt`. Edit, push, rebuild.
- **Cleartext:** most streams are `http://`; the app enables cleartext traffic
  so they play. That's expected for internet radio.
