package se.skotselytor.gps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.Intent;

public class MainActivity extends Activity {
    private static final int LOCATION_REQUEST = 1001;
    private WebView webView;
    private GeolocationPermissions.Callback geoCallback;
    private String geoOrigin;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestHighestRefreshRate();

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            }, LOCATION_REQUEST);
        }

        webView = new WebView(this);
        setContentView(webView);

        // Hårdvaruaccelererad rendering krävs för att WebView ska kunna
        // rita om så ofta som skärmens uppdateringsfrekvens (90/120Hz)
        // tillåter, i stället för att begränsas till mjukvarurendering.
        webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setGeolocationEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.addJavascriptInterface(new WebAppInterface(this), "Android");

        webView.setWebViewClient(new WebViewClient());

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                geoOrigin = origin;
                geoCallback = callback;

                boolean granted =
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

                if (granted) {
                    callback.invoke(origin, true, false);
                } else {
                    requestPermissions(new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    }, LOCATION_REQUEST);
                }
            }
        });

        webView.loadUrl("file:///android_asset/index.html");
    }

    /**
     * Ber systemet använda skärmens högsta tillgängliga uppdateringsfrekvens
     * (t.ex. 90Hz/120Hz på enheter som stödjer det) för denna aktivitet i
     * stället för standard 60Hz. Detta är den nativa (Android-sidiga) delen
     * av 120Hz-stödet; det kompletterar de JS-sidiga renderingsoptimeringarna
     * i index.html som minskar hur ofta tunga kartberäkningar görs.
     */
    private void requestHighestRefreshRate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Display display = getDisplay();
                if (display != null) {
                    Display.Mode[] modes = display.getSupportedModes();
                    Display.Mode best = display.getMode();
                    for (Display.Mode m : modes) {
                        if (m.getRefreshRate() > best.getRefreshRate()) {
                            best = m;
                        }
                    }
                    WindowManager.LayoutParams params = getWindow().getAttributes();
                    params.preferredDisplayModeId = best.getModeId();
                    getWindow().setAttributes(params);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Display display = getWindowManager().getDefaultDisplay();
                float best = display.getRefreshRate();
                for (Display.Mode m : display.getSupportedModes()) {
                    if (m.getRefreshRate() > best) best = m.getRefreshRate();
                }
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.preferredRefreshRate = best;
                getWindow().setAttributes(params);
            }
        } catch (Exception ignored) {
            // Om enheten inte stödjer variabel uppdateringsfrekvens fortsätter
            // appen helt enkelt med standardfrekvensen.
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST) {
            boolean granted = false;
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    granted = true;
                    break;
                }
            }

            if (geoCallback != null && geoOrigin != null) {
                geoCallback.invoke(geoOrigin, granted, false);
                geoCallback = null;
                geoOrigin = null;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    public void startBackgroundTracking() {
        Intent intent = new Intent(this, LocationForegroundService.class);
        startService(intent);
    }

    public void stopBackgroundTracking() {
        Intent intent = new Intent(this, LocationForegroundService.class);
        stopService(intent);
    }
}
