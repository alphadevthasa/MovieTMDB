# MovieDB

Native Android app that browses movies from the [TMDB API](https://api.themoviedb.org).

## Setup

1. Create a TMDB API Read Access Token at https://www.themoviedb.org/settings/api.
2. Add it to `local.properties` (this file is git-ignored):
   ```properties
   TMDB_ACCESS_TOKEN=your_read_access_token
   ```
3. Open the project in Android Studio and run the `app` configuration, or `./gradlew installDebug`.
