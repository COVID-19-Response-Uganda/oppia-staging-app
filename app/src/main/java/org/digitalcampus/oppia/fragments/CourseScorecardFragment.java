/* 
 * This file is part of OppiaMobile - https://digital-campus.org/
 * 
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidplot.pie.PieChart;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.adapter.CourseQuizzesGridAdapter;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.QuizStats;
import org.digitalcampus.oppia.task.ParseCourseXMLTask;
import org.digitalcampus.oppia.utils.ui.ProgressBarAnimator;
import org.digitalcampus.oppia.utils.ui.ScorecardPieChart;

import java.util.ArrayList;

public class CourseScorecardFragment extends AppFragment implements ParseCourseXMLTask.OnParseXmlListener, AdapterView.OnItemClickListener {

	private Course course = null;
    private boolean firstTimeOpened = true;
    private GridView quizzesGrid;
    private ArrayList<QuizStats> quizStats = new ArrayList<>();
    private CourseQuizzesGridAdapter quizzesAdapter;
    ParseCourseXMLTask xmlTask;

    private TextView highlightPretest;
    private TextView highlightAttempted;
    private TextView highlightPassed;
    private TextView activitiesTotal;
    private TextView activitiesCompleted;
    private ProgressBar quizzesProgressBar;
    private View quizzesView;
    private View quizzesContainer;

    private ProgressBar loadingSpinner;
    private PieChart scorecardPieChart;

    public static CourseScorecardFragment newInstance(Course course) {
		CourseScorecardFragment myFragment = new CourseScorecardFragment();
		Bundle args = new Bundle();
	    args.putSerializable(Course.TAG, course);
	    myFragment.setArguments(args);
	    return myFragment;
	}

    @Override
    public void onCreate( Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.course = (Course) getArguments().getSerializable(Course.TAG);
        xmlTask = new ParseCourseXMLTask(getActivity());
        xmlTask.setListener(this);
        xmlTask.execute(course);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View vv = inflater.inflate(R.layout.fragment_scorecard, container, false);
        // refresh course to get most recent info (otherwise gets the info from when course first opened)
        DbHelper db = DbHelper.getInstance(super.getActivity());
        long userId = db.getUserId(SessionManager.getUsername(getActivity()));
        this.course = db.getCourse(this.course.getCourseId(), userId);

        quizzesGrid = vv.findViewById(R.id.scorecard_grid_quizzes);
        scorecardPieChart = vv.findViewById(R.id.scorecard_pie_chart);

        highlightPretest = vv.findViewById(R.id.tv_ranking);
        highlightAttempted = vv.findViewById(R.id.highlight_attempted);
        highlightPassed = vv.findViewById(R.id.highlight_passed);
        quizzesProgressBar = vv.findViewById(R.id.progress_quizzes);
        quizzesView = vv.findViewById(R.id.quizzes_view);
        quizzesContainer = vv.findViewById(R.id.scorecard_quizzes_container);
        activitiesTotal = vv.findViewById(R.id.scorecard_activities_total);
        activitiesCompleted = vv.findViewById(R.id.scorecard_activities_completed);

        loadingSpinner = vv.findViewById(R.id.loading_spinner);
        loadingSpinner.setVisibility(View.VISIBLE);
		return vv;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        ScorecardPieChart spc = new ScorecardPieChart(super.getActivity(), scorecardPieChart, this.course);
        spc.drawChart(0, 0.55f, false, firstTimeOpened);
        firstTimeOpened = false;
        activitiesTotal.setText(""+course.getNoActivities());
        activitiesCompleted.setText(""+course.getNoActivitiesCompleted());

        quizzesAdapter = new CourseQuizzesGridAdapter(getActivity(), quizStats);
        quizzesGrid.setAdapter(quizzesAdapter);
        quizzesGrid.setOnItemClickListener(this);
	}

    //@Override
    public void onParseComplete(CompleteCourse parsed) {

        ArrayList<Activity> baseline = parsed.getBaselineActivities();
        
    	DbHelper db = DbHelper.getInstance(super.getActivity());
        long userId = db.getUserId(SessionManager.getUsername(getActivity()));
        ArrayList<Activity> quizActs = db.getCourseQuizzes(course.getCourseId());
        ArrayList<QuizStats> quizzes = new ArrayList<>();
        for (Activity a: quizActs){
        	// get the max score for the quiz for the user
        	QuizStats qs = db.getQuizAttempt(a.getDigest(), userId);
        	quizzes.add(qs);
        }

        int quizzesAttempted = 0, quizzesPassed = 0;

        for (QuizStats qs: quizzes){
        	if (qs.isAttempted()){
        		quizzesAttempted++;
        	}
        	if (qs.isPassed()){
        		quizzesPassed++;
        	}
        }
        
        int pretestScore = -1;
        for (Activity baselineAct : baseline){
            if (!baselineAct.getActType().equals("quiz")) continue;
            QuizStats pretest = db.getQuizAttempt(baselineAct.getDigest(), userId);
            pretestScore = pretest.getPercent();
        }
        
        quizStats.clear();
        quizStats.addAll(quizzes);
        if (quizStats.isEmpty()){
            quizzesContainer.setVisibility(View.GONE);
            return;
        }
        
        highlightPretest.setText(pretestScore >= 0 ? (pretestScore + "%") : "-");
        highlightAttempted.setText("" + quizzesAttempted);
        highlightPassed.setText("" + quizzesPassed);
        quizzesAdapter.notifyDataSetChanged();

        loadingSpinner.setVisibility(View.GONE);
        AlphaAnimation fadeInAnimation = new AlphaAnimation(0f, 1f);
        fadeInAnimation.setDuration(700);
        fadeInAnimation.setFillAfter(true);

        quizzesProgressBar.setProgress(0);
        quizzesProgressBar.setSecondaryProgress(0);

        quizzesView.setVisibility(View.VISIBLE);
        quizzesView.startAnimation(fadeInAnimation);

        quizzesProgressBar.setMax(quizStats.size());
        ProgressBarAnimator animator = new ProgressBarAnimator(quizzesProgressBar);
        animator.setStartDelay(500);
        animator.animateBoth(quizzesPassed, quizzesAttempted);
    }

    @Override
    public void onStop(){
        super.onStop();
        if (xmlTask != null){
            xmlTask.setListener(null);
        }
    }

    //@Override
    public void onParseError() {
        // no need to do anything
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        QuizStats quiz = quizzesAdapter.getItem(i);

        Intent returnIntent = new Intent();
        returnIntent.putExtra(CourseIndexActivity.JUMPTO_TAG, quiz.getDigest());
        getActivity().setResult(CourseIndexActivity.RESULT_JUMPTO, returnIntent);
        getActivity().finish();
    }
}
