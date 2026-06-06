# Skötselytor GPS - Android-app

Detta är en Android Studio-projektmapp som gör webbappen till en riktig Android-app via WebView.

## Vad appen gör
- Visar Skötselytor GPS som installerbar Android-app.
- Begär platsbehörighet från Android.
- Tillåter geolocation inne i WebView.
- Laddar kartan från Linköpings ArcGIS-tjänst.
- Sparar klippstatus och GPS-spår lokalt i appens webblagring.

## Så bygger du APK
1. Installera Android Studio.
2. Öppna denna mapp: `skotselytor_android_app`.
3. Vänta tills Gradle sync är klar.
4. Välj: Build > Build App Bundle(s) / APK(s) > Build APK(s).
5. APK hamnar normalt i:
   app/build/outputs/apk/debug/app-debug.apk

## Installera på mobilen
- Skicka APK-filen till mobilen.
- Tillåt installation från okända källor om Android frågar.
- Starta appen och tillåt plats.

## Viktigt
Kartan kräver internet.
Status/spår sparas lokalt på telefonen, inte i molnet.
