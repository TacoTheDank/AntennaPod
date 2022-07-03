package de.test.antennapod.ui;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.fragment.QueueFragment;
import de.test.antennapod.EspressoTestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static de.test.antennapod.NthMatcher.first;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.endsWith;

/**
 * User interface tests for queue fragment.
 */
@RunWith(AndroidJUnit4.class)
public class QueueFragmentTest {

    @Before
    public void setUp() {
        EspressoTestUtils.clearPreferences();
        EspressoTestUtils.clearDatabase();
        EspressoTestUtils.setLastNavFragment(QueueFragment.TAG);
        ActivityScenario.launch(MainActivity.class);
        Intents.init();
    }

    @After
    public void tearDown() throws Exception {
        Intents.release();
    }

    @Test
    public void testLockEmptyQueue() {
        onView(first(EspressoTestUtils.actionBarOverflow())).perform(click());
        onView(withText(R.string.lock_queue)).perform(click());
        onView(allOf(withClassName(endsWith("Button")), withText(R.string.lock_queue))).perform(click());
        onView(first(EspressoTestUtils.actionBarOverflow())).perform(click());
        onView(withText(R.string.lock_queue)).perform(click());
    }

    @Test
    public void testSortEmptyQueue() {
        onView(first(EspressoTestUtils.actionBarOverflow())).perform(click());
        onView(withText(R.string.sort)).perform(click());
        onView(withText(R.string.random)).perform(click());
    }

    @Test
    public void testKeepEmptyQueueSorted() {
        onView(first(EspressoTestUtils.actionBarOverflow())).perform(click());
        onView(withText(R.string.sort)).perform(click());
        onView(withText(R.string.keep_sorted)).perform(click());
    }
}
