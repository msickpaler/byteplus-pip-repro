package com.example.byteplusrepro;

import android.app.Application;
import com.pandora.common.env.Env;
import com.pandora.common.env.config.Config;
import com.pandora.common.env.config.VodConfig;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class ReproApplication extends Application {
    JSONObject config;

    @Override public void onCreate() {
        super.onCreate();
        try (InputStream input = getAssets().open("local.config.json")) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) bytes.write(buffer, 0, count);
            config = new JSONObject(bytes.toString(StandardCharsets.UTF_8.name()));
            Env.start(new Config.Builder()
                .setApplicationContext(this)
                .setAppID(config.getString("appId"))
                .setAppName(config.getString("appName"))
                .setAppVersion("1.0.0")
                .setAppChannel("local-repro")
                .setLicenseUri("assets:///license.lic")
                .setVodConfig(new VodConfig.Builder(this).build())
                .build());
        } catch (Exception error) {
            throw new IllegalStateException("BytePlus local configuration could not be loaded");
        }
    }
}
