package ru.fazziclay.opentoday.app;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;

import org.json.JSONObject;

import java.io.File;
import java.util.Random;

import ru.fazziclay.javaneoutil.FileUtil;
import ru.fazziclay.opentoday.BuildConfig;
import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.datafixer.DataFixer;
import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.receiver.QuickNoteReceiver;
import ru.fazziclay.opentoday.app.receiver.ItemsTickReceiver;
import ru.fazziclay.opentoday.app.settings.SettingsManager;
import ru.fazziclay.opentoday.debug.TestItemViewGenerator;
import ru.fazziclay.opentoday.ui.activity.CrashReportActivity;
import ru.fazziclay.opentoday.util.DebugUtil;
import ru.fazziclay.opentoday.util.Profiler;

@SuppressWarnings("PointlessBooleanExpression") // for debug variables
public class App extends Application {
    // Application
    public static final int APPLICATION_DATA_VERSION = 5;
    public static final String VERSION_NAME = BuildConfig.VERSION_NAME;
    public static final int VERSION_CODE = BuildConfig.VERSION_CODE;
    public static final String APPLICATION_ID = BuildConfig.APPLICATION_ID;

    // Notifications
    public static final String NOTIFICATION_QUCIKNOTE_CHANNEL = QuickNoteReceiver.NOTIFICATION_CHANNEL;
    public static final String NOTIFICATION_ITEMS_CHANNEL = "items_notifications";
    private static final String NOTIFICATION_CRASH_CHANNEL = "crash_report";

    // DEBUG
    public static final boolean DEBUG = BuildConfig.DEBUG;
    public static final boolean DEBUG_TICK_NOTIFICATION = (DEBUG & false);
    public static final int DEBUG_MAIN_ACTIVITY_START_SLEEP = (DEBUG & false) ? 6000 : 0;
    public static final int DEBUG_APP_START_SLEEP = (DEBUG & false) ? 1000 : 0;
    public static Class<? extends Activity> DEBUG_MAIN_ACTIVITY = (DEBUG & false) ? TestItemViewGenerator.class : null;

    // Instance
    private static volatile App instance = null;
    public static App get(Context context) {
        return (App) context.getApplicationContext();
    }
    public static App get() {
        return instance;
    }

    // Application
    private ItemManager itemManager;
    private SettingsManager settingsManager;
    private boolean appInForeground = false;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        setupCrashReporter();
        /* debug */ DebugUtil.sleep(DEBUG_APP_START_SLEEP);

        Profiler appProfiler = new Profiler("App onCreate");
        appProfiler.point("DataFixer");
        DataFixer dataFixer = new DataFixer(this);
        dataFixer.fixToCurrentVersion();

        appProfiler.point("version file");
        try {
            FileUtil.setText(new File(getExternalFilesDir(""), "version"), new JSONObject()
                    .put("product", "OpenToday")
                    .put("developer", "FazziCLAY ( https://fazziclay.github.io )")
                    .put("licence", "GNU GPLv3")
                    .put("data_version", APPLICATION_DATA_VERSION)
                    .put("latest_start", System.currentTimeMillis())
                    .toString(4));
        } catch (Exception e) {
            throw new RuntimeException("Exception!", e);
        }

        appProfiler.point("Init");
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        itemManager = new ItemManager(new File(getExternalFilesDir(""), "item_data.json"));
        settingsManager = new SettingsManager(new File(getExternalFilesDir(""), "settings.json"));

        AppCompatDelegate.setDefaultNightMode(settingsManager.getTheme());
        notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_QUCIKNOTE_CHANNEL, getString(R.string.notification_quickNote_title), NotificationManager.IMPORTANCE_HIGH));
        notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_ITEMS_CHANNEL, getString(R.string.notification_items_title), NotificationManager.IMPORTANCE_HIGH));

        appProfiler.point("AlarmManager");
        AlarmManager alarmManager = getSystemService(AlarmManager.class);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60*1000, PendingIntent.getBroadcast(this, 0, new Intent(this, ItemsTickReceiver.class), 0));
        appProfiler.end();
    }

    private void setupCrashReporter() {
        Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            App.crash(App.this, CrashReport.create(thread, throwable, System.currentTimeMillis(), System.nanoTime(), Thread.getAllStackTraces()));
            if (defaultHandler != null) {
                if (DEBUG) DebugUtil.sleep(7000);
                defaultHandler.uncaughtException(thread, throwable);
            }
        });
    }

    public static void crash(Context context, CrashReport crashReport) {
        // File
        File file = new File(context.getExternalCacheDir(), "crash_report/" + crashReport.getID().toString());
        FileUtil.setText(file, crashReport.convertToText());

        // === NOTIFICATION ===
        final NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_CRASH_CHANNEL, context.getString(R.string.notification_crash_title), NotificationManager.IMPORTANCE_DEFAULT));

        int flag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flag = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
        } else {
            flag = PendingIntent.FLAG_UPDATE_CURRENT;
        }

        notificationManager.notify(new Random().nextInt(), new NotificationCompat.Builder(context, App.NOTIFICATION_CRASH_CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.crash_notification_title))
                .setContentText(context.getString(R.string.crash_notification_text))
                .setSubText(context.getString(R.string.crash_notification_subtext))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(context.getString(R.string.crash_notification_big_text))
                        .setBigContentTitle(context.getString(R.string.crash_notification_big_title))
                        .setSummaryText(context.getString(R.string.crash_notification_big_summary)))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, CrashReportActivity.class).putExtra("path", file.getAbsolutePath()), flag))
                .setAutoCancel(true)
                .build());
    }

    // getters & setters
    public ItemManager getItemManager() { return itemManager; }
    public SettingsManager getSettingsManager() { return this.settingsManager; }
    public boolean isAppInForeground() { return appInForeground; }
    public void setAppInForeground(boolean appInForeground) { this.appInForeground = appInForeground; }
    // not getters & setters :)
}
