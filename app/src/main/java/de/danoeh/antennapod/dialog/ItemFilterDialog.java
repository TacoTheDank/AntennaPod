package de.danoeh.antennapod.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import de.danoeh.antennapod.core.feed.FeedItemFilterGroup;
import de.danoeh.antennapod.databinding.FilterDialogBinding;
import de.danoeh.antennapod.databinding.FilterDialogRowBinding;
import de.danoeh.antennapod.model.feed.FeedItemFilter;
import de.danoeh.antennapod.ui.common.RecursiveRadioGroup;

import java.util.HashSet;
import java.util.Set;

public abstract class ItemFilterDialog extends BottomSheetDialogFragment {
    protected static final String ARGUMENT_FILTER = "filter";
    private FilterDialogBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FilterDialogBinding.inflate(inflater);
        FeedItemFilter filter = (FeedItemFilter) getArguments().getSerializable(ARGUMENT_FILTER);

        for (FeedItemFilterGroup item : FeedItemFilterGroup.values()) {
            final FilterDialogRowBinding rowBinding =
                    FilterDialogRowBinding.inflate(inflater);
            rowBinding.getRoot().setOnCheckedChangeListener((group, checkedId) ->
                    onFilterChanged(getNewFilterValues()));
            rowBinding.filterDialogRadioButton1.setText(item.values[0].displayName);
            rowBinding.filterDialogRadioButton1.setTag(item.values[0].filterId);
            rowBinding.filterDialogRadioButton2.setText(item.values[1].displayName);
            rowBinding.filterDialogRadioButton2.setTag(item.values[1].filterId);
            binding.filterRows.addView(rowBinding.getRoot());
        }

        for (String filterId : filter.getValues()) {
            if (!TextUtils.isEmpty(filterId)) {
                RadioButton button = binding.getRoot().findViewWithTag(filterId);
                if (button != null) {
                    button.setChecked(true);
                }
            }
        }
        return binding.getRoot();
    }

    protected Set<String> getNewFilterValues() {
        final Set<String> newFilterValues = new HashSet<>();
        for (int i = 0; i < binding.filterRows.getChildCount(); i++) {
            if (!(binding.filterRows.getChildAt(i) instanceof RecursiveRadioGroup)) {
                continue;
            }
            RecursiveRadioGroup group = (RecursiveRadioGroup) binding.filterRows.getChildAt(i);
            if (group.getCheckedButton() != null) {
                String tag = (String) group.getCheckedButton().getTag();
                if (tag != null) { // Clear buttons use no tag
                    newFilterValues.add((String) group.getCheckedButton().getTag());
                }
            }
        }
        return newFilterValues;
    }

    abstract void onFilterChanged(Set<String> newFilterValues);
}
