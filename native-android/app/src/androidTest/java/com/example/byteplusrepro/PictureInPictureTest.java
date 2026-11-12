package com.example.byteplusrepro;

import android.content.Intent;
import android.os.SystemClock;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PictureInPictureTest {
    private static void await(String description, BooleanSupplier condition) {
        long deadline = SystemClock.uptimeMillis() + 15000;
        while (!condition.getAsBoolean() && SystemClock.uptimeMillis() < deadline) {
            SystemClock.sleep(100);
        }
        assertTrue(description, condition.getAsBoolean());
    }

    @Test public void 再生中と停止中のPiP復帰で同じプレイヤーと位置を保つ() throws Exception {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        try (ActivityScenario<PlayerActivity> scenario = ActivityScenario.launch(PlayerActivity.class)) {
            AtomicInteger position = new AtomicInteger();
            AtomicBoolean pip = new AtomicBoolean();
            AtomicInteger engineIdentity = new AtomicInteger();
            scenario.onActivity(a -> {
                engineIdentity.set(System.identityHashCode(a.engine));
                a.togglePlayback();
            });
            await("動画の再生時間が進む", () -> {
                scenario.onActivity(a -> position.set(a.engine.getCurrentPlaybackTime()));
                return position.get() > 1500;
            });
            int beforePip = position.get();
            device.pressHome();
            await("ホームでPiPへ移る", () -> {
                scenario.onActivity(a -> pip.set(a.isInPictureInPictureMode()));
                return pip.get();
            });
            await("PiPでも再生時間が進む", () -> {
                scenario.onActivity(a -> position.set(a.engine.getCurrentPlaybackTime()));
                return position.get() > beforePip + 1500;
            });
            restore();
            await("全画面へ復帰する", () -> {
                scenario.onActivity(a -> pip.set(a.isInPictureInPictureMode()));
                return !pip.get();
            });
            scenario.onActivity(a -> {
                assertEquals(engineIdentity.get(), System.identityHashCode(a.engine));
                assertTrue(a.engine.isShouldPlay());
                assertTrue(a.engine.getCurrentPlaybackTime() >= beforePip);
            });
            device.pressHome();
            await("再びPiPへ移る", () -> {
                scenario.onActivity(a -> pip.set(a.isInPictureInPictureMode()));
                return pip.get();
            });
            scenario.onActivity(a -> { a.togglePlayback(); position.set(a.engine.getCurrentPlaybackTime()); });
            restore();
            await("停止状態で全画面へ復帰する", () -> {
                scenario.onActivity(a -> pip.set(a.isInPictureInPictureMode()));
                return !pip.get();
            });
            scenario.onActivity(a -> {
                assertEquals(engineIdentity.get(), System.identityHashCode(a.engine));
                assertFalse(a.engine.isShouldPlay());
                assertTrue(Math.abs(a.engine.getCurrentPlaybackTime() - position.get()) < 300);
            });
            device.pressHome();
            device.waitForIdle();
            scenario.onActivity(a -> assertFalse("停止中は自動PiPしない", a.isInPictureInPictureMode()));
        }
    }

    private static void restore() {
        var context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.startActivity(new Intent(context, PlayerActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
