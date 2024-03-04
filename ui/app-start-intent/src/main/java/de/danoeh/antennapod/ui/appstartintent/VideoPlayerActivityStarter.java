package de.danoeh.antennapod.ui.appstartintent;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.PendingIntentCompat;

/**
 * Launches the video player activity of the app with specific arguments.
 * Does not require a dependency on the actual implementation of the activity.
 */
public class VideoPlayerActivityStarter {
    public static final String INTENT = "de.danoeh.antennapod.intents.VIDEO_PLAYER";
    private final Intent intent;
    private final Context context;

    public VideoPlayerActivityStarter(Context context) {
        this.context = context;
        intent = new Intent(INTENT);
        intent.setPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
    }

    public Intent getIntent() {
        return intent;
    }

    public PendingIntent getPendingIntent() {
        return PendingIntentCompat.getActivity(context, R.id.pending_intent_video_player, getIntent(),
                PendingIntent.FLAG_UPDATE_CURRENT, false);
    }

    public void start() {
        context.startActivity(getIntent());
    }
}
