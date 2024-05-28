package de.danoeh.antennapod.ui.preferences.screen.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.google.android.material.tabs.TabLayoutMediator;
import de.danoeh.antennapod.ui.common.databinding.PagerFragmentBinding;
import de.danoeh.antennapod.ui.preferences.R;

/**
 * Displays the 'about->Contributors' pager screen.
 */
public class ContributorsPagerFragment extends Fragment {

    private PagerFragmentBinding binding;

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = PagerFragmentBinding.inflate(inflater, container, false);
        final ContributorsStateAdapter adapter = new ContributorsStateAdapter(this);
        binding.pagerViewpager2.setAdapter(adapter);
        new TabLayoutMediator(binding.pagerTabLayout, binding.pagerViewpager2, (tab, position) ->
                tab.setText(adapter.getPageTitle(position))
        ).attach();
        binding.pagerToolbar.setVisibility(View.GONE);

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.contributors);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class ContributorsStateAdapter extends FragmentStateAdapter {
        private static final int POS_DEVELOPERS = 0;
        private static final int POS_TRANSLATORS = 1;
        private static final int POS_SPECIAL_THANKS = 2;
        private static final int TOTAL_COUNT = 3;

        ContributorsStateAdapter(@NonNull final Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(final int position) {
            switch (position) {
                case POS_TRANSLATORS:
                    return new TranslatorsFragment();
                case POS_SPECIAL_THANKS:
                    return new SpecialThanksFragment();
                default:
                case POS_DEVELOPERS:
                    return new DevelopersFragment();
            }
        }

        @Override
        public int getItemCount() {
            return TOTAL_COUNT;
        }

        public int getPageTitle(final int position) {
            switch (position) {
                case POS_TRANSLATORS:
                    return R.string.translators;
                case POS_SPECIAL_THANKS:
                    return R.string.special_thanks;
                default:
                case POS_DEVELOPERS:
                    return R.string.developers;
            }
        }
    }
}
