package de.danoeh.antennapod.ui.statistics;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;
import de.danoeh.antennapod.ui.common.ConfirmationDialog;
import de.danoeh.antennapod.storage.database.DBWriter;
import de.danoeh.antennapod.event.StatisticsEvent;
import de.danoeh.antennapod.ui.common.PagedToolbarFragment;
import de.danoeh.antennapod.ui.common.databinding.PagerFragmentBinding;
import de.danoeh.antennapod.ui.statistics.downloads.DownloadStatisticsFragment;
import de.danoeh.antennapod.ui.statistics.subscriptions.SubscriptionStatisticsFragment;
import de.danoeh.antennapod.ui.statistics.years.YearsStatisticsFragment;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.greenrobot.eventbus.EventBus;

/**
 * Displays the 'statistics' screen
 */
public class StatisticsFragment extends PagedToolbarFragment {
    public static final String TAG = "StatisticsFragment";
    public static final String PREF_NAME = "StatisticsActivityPrefs";
    public static final String PREF_INCLUDE_MARKED_PLAYED = "countAll";
    public static final String PREF_FILTER_FROM = "filterFrom";
    public static final String PREF_FILTER_TO = "filterTo";
    private PagerFragmentBinding binding;

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        binding = PagerFragmentBinding.inflate(inflater, container, false);
        binding.pagerToolbar.setTitle(getString(R.string.statistics_label));
        binding.pagerToolbar.inflateMenu(R.menu.statistics);
        binding.pagerToolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        final StatisticsStateAdapter adapter = new StatisticsStateAdapter(this);
        binding.pagerViewpager2.setAdapter(adapter);
        super.setupPagedToolbar(binding.pagerToolbar, binding.pagerViewpager2);
        new TabLayoutMediator(binding.pagerTabLayout, binding.pagerViewpager2, (tab, position) ->
                tab.setText(adapter.getPageTitle(position))
        ).attach();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.statistics_reset) {
            confirmResetStatistics();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmResetStatistics() {
        ConfirmationDialog conDialog = new ConfirmationDialog(
                getActivity(),
                R.string.statistics_reset_data,
                R.string.statistics_reset_data_msg) {

            @Override
            public void onConfirmButtonPressed(DialogInterface dialog) {
                dialog.dismiss();
                doResetStatistics();
            }
        };
        conDialog.createNewDialog().show();
    }

    private void doResetStatistics() {
        getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
                .putBoolean(PREF_INCLUDE_MARKED_PLAYED, false)
                .putLong(PREF_FILTER_FROM, 0)
                .putLong(PREF_FILTER_TO, Long.MAX_VALUE)
                .apply();

        Disposable disposable = Completable.fromFuture(DBWriter.resetStatistics())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> EventBus.getDefault().post(new StatisticsEvent()),
                        error -> Log.e(TAG, Log.getStackTraceString(error)));
    }

    private static class StatisticsStateAdapter extends FragmentStateAdapter {
        private static final int POS_SUBSCRIPTIONS = 0;
        private static final int POS_YEARS = 1;
        private static final int POS_SPACE_TAKEN = 2;
        private static final int TOTAL_COUNT = 3;

        StatisticsStateAdapter(@NonNull final Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(final int position) {
            switch (position) {
                case POS_SUBSCRIPTIONS:
                    return new SubscriptionStatisticsFragment();
                case POS_YEARS:
                    return new YearsStatisticsFragment();
                default:
                case POS_SPACE_TAKEN:
                    return new DownloadStatisticsFragment();
            }
        }

        @Override
        public int getItemCount() {
            return TOTAL_COUNT;
        }

        public int getPageTitle(final int position) {
            switch (position) {
                case POS_SUBSCRIPTIONS:
                    return R.string.subscriptions_label;
                case POS_YEARS:
                    return R.string.years_statistics_label;
                default:
                case POS_SPACE_TAKEN:
                    return R.string.downloads_label;
            }
        }
    }
}
