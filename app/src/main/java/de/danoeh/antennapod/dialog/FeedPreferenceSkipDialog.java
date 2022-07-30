package de.danoeh.antennapod.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import androidx.appcompat.app.AlertDialog;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.databinding.FeedPrefSkipDialogBinding;

/**
 * Displays a dialog with a username and password text field and an optional checkbox to save username and preferences.
 */
public abstract class FeedPreferenceSkipDialog extends AlertDialog.Builder {

    public FeedPreferenceSkipDialog(Context context, int skipIntroInitialValue,
                                    int skipEndInitialValue) {
        super(context);
        setTitle(R.string.pref_feed_skip);
        final FeedPrefSkipDialogBinding binding =
                FeedPrefSkipDialogBinding.inflate(LayoutInflater.from(context));
        setView(binding.getRoot());

        binding.etxtSkipIntro.setText(String.valueOf(skipIntroInitialValue));
        binding.etxtSkipEnd.setText(String.valueOf(skipEndInitialValue));

        setNegativeButton(R.string.cancel_label, null);
        setPositiveButton(R.string.confirm_label, (dialog, which) -> {
            int skipIntro;
            int skipEnding;
            try {
                skipIntro = Integer.parseInt(binding.etxtSkipIntro.getText().toString());
            } catch (NumberFormatException e) {
                skipIntro = 0;
            }

            try {
                skipEnding = Integer.parseInt(binding.etxtSkipEnd.getText().toString());
            } catch (NumberFormatException e) {
                skipEnding = 0;
            }
            onConfirmed(skipIntro, skipEnding);
        });
    }

    protected abstract void onConfirmed(int skipIntro, int skipEnding);
}
