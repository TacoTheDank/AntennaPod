package de.danoeh.antennapod.dialog;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.databinding.EpisodeFilterDialogBinding;
import de.danoeh.antennapod.model.feed.FeedFilter;

/**
 * Displays a dialog with a text box for filtering episodes and two radio buttons for exclusion/inclusion
 */
public abstract class EpisodeFilterDialog extends AlertDialog.Builder {
    private final FeedFilter initialFilter;

    public EpisodeFilterDialog(Context context, FeedFilter filter) {

        super(context);
        initialFilter = filter;
        setTitle(R.string.episode_filters_label);
        final EpisodeFilterDialogBinding binding =
                EpisodeFilterDialogBinding.inflate(LayoutInflater.from(context));
        setView(binding.getRoot());

        if (initialFilter.includeOnly()) {
            binding.radioFilterInclude.setChecked(true);
            binding.etxtEpisodeFilterText.setText(initialFilter.getIncludeFilter());
        } else if (initialFilter.excludeOnly()) {
            binding.radioFilterExclude.setChecked(true);
            binding.etxtEpisodeFilterText.setText(initialFilter.getExcludeFilter());
        } else {
            binding.radioFilterExclude.setChecked(false);
            binding.radioFilterInclude.setChecked(false);
            binding.etxtEpisodeFilterText.setText("");
        }
        if (initialFilter.hasMinimalDurationFilter()) {
            binding.checkboxFilterDuration.setChecked(true);
            // Store minimal duration in seconds, show in minutes
            binding.etxtEpisodeFilterDurationText.setText(
                    String.valueOf(initialFilter.getMinimalDurationFilter() / 60));
        }

        setNegativeButton(R.string.cancel_label, null);
        setPositiveButton(R.string.confirm_label, (dialog, which) -> {
                    String includeString = "";
                    String excludeString = "";
                    int minimalDuration = -1;
                    if (binding.radioFilterInclude.isChecked()) {
                        includeString = binding.etxtEpisodeFilterText.getText().toString();
                    } else {
                        excludeString = binding.etxtEpisodeFilterText.getText().toString();
                    }
                    if (binding.checkboxFilterDuration.isChecked()) {
                        try {
                            // Store minimal duration in seconds
                            minimalDuration = Integer.parseInt(
                                    binding.etxtEpisodeFilterDurationText.getText().toString()) * 60;
                        } catch (NumberFormatException e) {
                            // Do not change anything on error
                        }
                    }
                    onConfirmed(new FeedFilter(includeString, excludeString, minimalDuration));
                }
        );
    }

    protected abstract void onConfirmed(FeedFilter filter);
}
