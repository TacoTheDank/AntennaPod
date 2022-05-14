package de.danoeh.antennapod.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;

import java.util.Collections;
import java.util.List;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.core.dialog.ConfirmationDialog;
import de.danoeh.antennapod.databinding.ProgressDialogBinding;
import de.danoeh.antennapod.model.feed.Feed;
import de.danoeh.antennapod.core.storage.DBWriter;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RemoveFeedDialog {
    private static final String TAG = "RemoveFeedDialog";

    public static void show(Context context, Feed feed) {
        List<Feed> feeds = Collections.singletonList(feed);
        String message = getMessageId(context, feeds);
        showDialog(context, feeds, message);
    }

    public static void show(Context context, List<Feed> feeds) {
        String message = getMessageId(context, feeds);
        showDialog(context, feeds, message);
    }

    private static void showDialog(Context context, List<Feed> feeds, String message) {
        ConfirmationDialog dialog = new ConfirmationDialog(context, R.string.remove_feed_label, message) {
            @Override
            public void onConfirmButtonPressed(DialogInterface clickedDialog) {
                clickedDialog.dismiss();

                final ProgressDialogBinding progressBinding =
                        ProgressDialogBinding.inflate(LayoutInflater.from(context));
                progressBinding.dialogProgressText.setText(R.string.feed_remover_msg);
                final AlertDialog progressDialog = new AlertDialog.Builder(context)
                        .setView(progressBinding.getRoot())
                        .setCancelable(false)
                        .show();

                Completable.fromAction(() -> {
                    for (Feed feed : feeds) {
                        DBWriter.deleteFeed(context, feed.getId()).get();
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            () -> {
                                Log.d(TAG, "Feed(s) deleted");
                                progressDialog.dismiss();
                            }, error -> {
                                Log.e(TAG, Log.getStackTraceString(error));
                                progressDialog.dismiss();
                            });
            }
        };
        dialog.createNewDialog().show();
    }

    private static String getMessageId(Context context, List<Feed> feeds) {
        if (feeds.size() == 1) {
            if (feeds.get(0).isLocalFeed()) {
                return context.getString(R.string.feed_delete_confirmation_local_msg, feeds.get(0).getTitle());
            } else {
                return context.getString(R.string.feed_delete_confirmation_msg, feeds.get(0).getTitle());
            }
        } else {
            return context.getString(R.string.feed_delete_confirmation_msg_batch);
        }

    }
}
