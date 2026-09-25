# Alphabet Launcher

A minimal Android home screen built for the NovaFocus Android Developer assignment. A vertical A-Z bar bends toward your finger as you drag, and the left side instantly swaps between a clock/favourites view and a live, filtered list of apps installed on the device.

## Features

**Core**
- [x] Home screen with live clock/date + a short favourites list (real installed apps, not placeholders)
- [x] Vertical A-Z bar, pinned to the right edge, with a star above A and a dot below Z
- [x] Real installed-app list read via `PackageManager`, cached once (not re-queried on touch)
- [x] Curve animation — the bar bends toward the finger while dragging, hand-written (no animation library)
- [x] Letter bubble showing the currently selected letter
- [x] Instant filtered list (name + icon) for the selected letter, sorted A-Z, case-insensitive
- [x] Empty-letter state ("No apps") for letters with nothing installed
- [x] Spring-back to a straight line on release
- [x] Tap an app in the list to launch it

**Bonus**
- [x] Haptic tick when the selected letter changes
- [x] Spring overshoot on release
- [ ] _(fill in any others you added — default launcher registration, swipe-up search, favourites persistence, light/dark theme, etc.)_

## Setup

1. Clone the repo and open it in Android Studio.
2. Let Gradle sync — no manual configuration needed.
3. Run on a physical device (recommended — the animation and haptics are the point) or an emulator on API 26+.

## Architecture

- **`AppRepositoryViewModel`** — the only place that talks to `PackageManager`. Queries installed launchable apps once on startup (off the main thread), groups them by first letter, and exposes the result as a `StateFlow`. Also handles launching an app by package name.
- **`AlphabetBar`** — the star, the 26 letters, and the dot live in a single list sharing one coordinate space, laid out with `Arrangement.SpaceBetween` so it adapts to any screen height or aspect ratio. A drag gesture converts the touch's Y position into a fractional index into that list; every item's horizontal offset is a pure function of its distance from that index. The same index drives the letter bubble and the letter reported upward.
- **`HomeScreen`** — owns the resting-vs-filtered state, the clock tick, and the favourites list (resolved from the same cached app data, so favourites get real icons for free). Renders both the favourites and the filtered list through a shared `AppRow` composable.

## How the curve animation works

The bar tracks the finger as a single fractional value (e.g. `13.4` for a touch between N and O), not a snapped whole letter — that's what keeps the curve smooth instead of stepped. Every item's horizontal offset — star, letter, or dot — is a pure function of its distance from that value: a cosine falloff gives full displacement at distance 0 and eases to zero over a ~5-item window, producing the V-shaped bulge toward the finger. Offsets are applied through a layout-phase `Modifier.offset { }` lambda rather than a state-driven recomposition, which is what keeps it steady at 60fps during fast drags. On release, the same tracking value animates back to "off" with a `spring()` spec, giving the slight overshoot-and-settle bounce.

Star, letters, and dot are kept in one unified list on purpose: keeping them in separate layout blocks caused the touch-to-letter mapping and the curve's visual center to drift out of sync by about one row. A single shared coordinate space removes that failure mode entirely, and as a side effect lets the star and dot bend near the ends too, matching the reference video.

## Libraries used

| Library | Version | Why |
|---|---|---|
| Jetpack Compose | _(fill in your BOM version)_ | UI, plus the offset/gesture APIs the curve animation is built on |
| Kotlin Coroutines | _(fill in)_ | Loading the installed-app list off the main thread, and the live clock tick |
| `androidx.core:core-ktx` | _(fill in)_ | `Drawable.toBitmap()` for rendering app icons in Compose |
| _(add anything else you pulled in)_ | | |

No animation or physics library was used for the curve itself — the falloff and spring-back are hand-written, per the assignment's ask that the core animation not be pulled from a library.

## Bugs found and fixed during testing

- **Off-by-one letter selection.** Testing on a real device showed the selected letter consistently lagging the actual touch position by about one row. Cause: the star's extra row was included in the same measured height used to convert touch position into a letter index. Fixed by unifying the star, letters, and dot into one list with one shared measurement.
- **Bottom letters clipped on a real phone.** The emulator's tall aspect ratio hid an overflow bug — hardcoded per-letter padding made the 28-item column taller than a real phone's shorter screen. Fixed by switching to `fillMaxHeight()` + `Arrangement.SpaceBetween`, so spacing adapts to whatever height is actually available.

## AI / tutorial disclosure

_(Be specific and honest here — for example: "Used Claude to sketch the initial gesture-detection structure, the cosine-falloff formula, and to diagnose two bugs found during device testing (touch-mapping drift, real-device layout overflow); wrote and adapted the PackageManager caching, filtering, and screen layout myself." State whatever is actually true for your build.)_

## Known limitations

_(List anything you didn't get to — e.g. bonus features skipped, edge cases not handled, devices not tested on.)_
