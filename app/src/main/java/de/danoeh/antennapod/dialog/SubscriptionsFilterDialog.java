package de.danoeh.antennapod.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;

import androidx.appcompat.app.AlertDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.databinding.FilterDialogBinding;
import de.danoeh.antennapod.databinding.FilterDialogRowBinding;
import de.danoeh.antennapod.event.UnreadItemsUpdateEvent;
import de.danoeh.antennapod.model.feed.SubscriptionsFilter;
import de.danoeh.antennapod.core.feed.SubscriptionsFilterGroup;
import de.danoeh.antennapod.core.preferences.UserPreferences;
import de.danoeh.antennapod.ui.common.RecursiveRadioGroup;

public class SubscriptionsFilterDialog {
    public static void showDialog(Context context) {
        SubscriptionsFilter subscriptionsFilter = UserPreferences.getSubscriptionsFilter();
        final Set<String> filterValues = new HashSet<>(Arrays.asList(subscriptionsFilter.getValues()));
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.pref_filter_feed_title));

        final FilterDialogBinding binding = FilterDialogBinding.inflate(LayoutInflater.from(context));
        builder.setView(binding.getRoot());

        for (SubscriptionsFilterGroup item : SubscriptionsFilterGroup.values()) {
            final FilterDialogRowBinding rowBinding =
                    FilterDialogRowBinding.inflate(LayoutInflater.from(context));
            rowBinding.filterDialogRadioButton1.setText(item.values[0].displayName);
            rowBinding.filterDialogRadioButton1.setTag(item.values[0].filterId);
            if (item.values.length == 2) {
                rowBinding.filterDialogRadioButton2.setText(item.values[1].displayName);
                rowBinding.filterDialogRadioButton2.setTag(item.values[1].filterId);
            } else {
                rowBinding.filterDialogRadioButton2.setVisibility(View.GONE);
            }
            binding.filterRows.addView(rowBinding.getRoot());
        }

        for (String filterId : filterValues) {
            if (!TextUtils.isEmpty(filterId)) {
                ((RadioButton) binding.getRoot().findViewWithTag(filterId)).setChecked(true);
            }
        }

        builder.setPositiveButton(R.string.confirm_label, (dialog, which) -> {
            filterValues.clear();
            for (int i = 0; i < binding.filterRows.getChildCount(); i++) {
                if (!(binding.filterRows.getChildAt(i) instanceof RecursiveRadioGroup)) {
                    continue;
                }
                RecursiveRadioGroup group = (RecursiveRadioGroup) binding.filterRows.getChildAt(i);
                if (group.getCheckedButton() != null) {
                    String tag = (String) group.getCheckedButton().getTag();
                    if (tag != null) { // Clear buttons use no tag
                        filterValues.add((String) group.getCheckedButton().getTag());
                    }
                }
            }
            updateFilter(filterValues);
        });
        builder.setNegativeButton(R.string.cancel_label, null);
        builder.show();
    }

    private static void updateFilter(Set<String> filterValues) {
        SubscriptionsFilter subscriptionsFilter = new SubscriptionsFilter(filterValues.toArray(new String[0]));
        UserPreferences.setSubscriptionsFilter(subscriptionsFilter);
        EventBus.getDefault().post(new UnreadItemsUpdateEvent());
    }
}
