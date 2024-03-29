package androidTestFiles.org.digitalcampus.oppia.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import org.digitalcampus.oppia.activity.CourseQuizAttemptsActivity;
import org.digitalcampus.oppia.model.QuizStats;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidTestFiles.database.BaseTestDB;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

@RunWith(AndroidJUnit4.class)
public class CourseQuizAttemptsActivityTest extends BaseTestDB {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private Context context;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    }


    @Test
    public void testActivityOpenWithAttempts() {

        getTestDataManager().addUsers();
        getTestDataManager().addCourses();

        QuizStats qs = new QuizStats();
        qs.setSectionTitle("my section");
        qs.setQuizTitle("my quiz");
        qs.setDigest("1234");
        qs.setNumAttempts(10);
        qs.setAverageScore(7);
        Intent intent = new Intent();
        intent.putExtra(QuizStats.TAG, qs);

        try (ActivityScenario<CourseQuizAttemptsActivity> scenario = ActivityScenario.launch(CourseQuizAttemptsActivity.class)) {
            // TODO assertions
        }

    }

    @Test
    public void testActivityOpenWithNoAttempts() {

        getTestDataManager().addUsers();
        getTestDataManager().addCourses();

        QuizStats qs = new QuizStats();
        qs.setSectionTitle("my section");
        qs.setQuizTitle("my quiz");
        qs.setDigest("1234");
        qs.setNumAttempts(0);
        qs.setAverageScore(0);
        Intent intent = new Intent();
        intent.putExtra(QuizStats.TAG, qs);
        try (ActivityScenario<CourseQuizAttemptsActivity> scenario = ActivityScenario.launch(CourseQuizAttemptsActivity.class)) {
            // TODO assertions
        }
    }

}
