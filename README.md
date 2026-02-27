# SubwayPing

You're out with friends. Every 10 minutes you pull out your phone to check the subway. You miss the train anyway. Worse: you end up on Instagram, see a work email, and the fun buzz is gone.

SubwayPing is a home screen widget: one red button. Tap it, put the phone away, and get notified when your train is close.

---

## How it works

Pick your line, station, and direction once. The widget sits on your home screen. Tap PING and a foreground service starts polling the MTA's live feed every 30 seconds. When your train is 3 minutes away, you get a high-priority notification. A softer buzz follows every 5 minutes with updated arrival times. Tap STOP when you're on the train.

---

## Features

- Home screen widget: one tap to start tracking, one tap to stop
- Real-time arrivals from MTA GTFS-RT protobuf feed (all 8 subway feed groups)
- 3-minute high-priority alert when your train is close
- 5-minute buzz reminders with updated arrival times
- Route picker: browse all lines, 200+ stations, and direction
- Saves favorite route so you never have to pick again
- Auto-stop timer in settings

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Widget | Glance AppWidget |
| Transit data | MTA GTFS-RT (protobuf) via OkHttp |
| Background | Foreground Service + Kotlin Coroutines + Flow |
| Storage | Room (saved routes), DataStore (preferences) |
| Architecture | MVVM + Repository pattern |

---

## Architecture

```
MTA GTFS-RT API (protobuf)
        ↓
  MtaFeedService + GtfsParser
  · Fetches binary protobuf feed
  · Parses arrival times for selected route
        ↓
  TrackingService (foreground)
  · Polls every 30 seconds
  · Computes next arrival minutes
  · Triggers notifications + widget update
        ↓
  SubwayPingWidget (Glance)        NotificationChannels
  · PING / STOP button             · High-priority (3 min)
  · Next arrival countdown         · Periodic buzz (5 min)
```

---

## Project structure

```
subway-ping/
└── app/src/main/java/com/subwayping/app/
    ├── data/
    │   ├── local/         # Room DB, DataStore, StationRepository
    │   └── remote/        # MtaFeedService, GtfsParser
    ├── service/           # TrackingService (foreground polling)
    ├── ui/
    │   ├── screens/       # Landing, Home, RoutePicker, Settings
    │   └── theme/         # Colors, typography (dark theme)
    └── widget/            # SubwayPingWidget (Glance)
```

---

## Running locally

1. Clone the repo
2. Open in Android Studio
3. Run on a device or emulator (Android API 26+)

No API key needed. The MTA GTFS-RT feed is publicly accessible.
