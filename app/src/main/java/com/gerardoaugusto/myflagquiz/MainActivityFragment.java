package com.gerardoaugusto.myflagquiz;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static android.view.View.*;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final String TAG = "FlagQuiz Activity";

    private static final int FLAGS_IN_QUIZ = 10;

    private List<String> fileNameList; // flag file names
    private List<String> quizCountriesList; // countries in current quiz
    private Set<String> regionsSet; // world regions in current quiz
    private String correctAnswer; // correct country for the current flag
    private int totalGuesses; // number of guesses made
    private int correctAnswers; // number of correct guesses
    private int guessRows; // number of rows displaying guess Buttons
    private SecureRandom random; // used to randomize the quiz
    private Handler handler; // used to delay loading next flag
    private Animation shakeAnimation; // animation for incorrect guess

    private LinearLayout quizLinearLayout; // layout that contains the quiz
    private TextView questionNumberTextView; // shows current question #
    private ImageView flagImageView; // displays a flag
    private LinearLayout[] guessLinearLayouts; // rows of answer Buttons
    private TextView answerTextView; // displays correct answer

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View view=inflater.inflate(R.layout.fragment_main, container, false);
        fileNameList= new ArrayList<>();
        quizCountriesList=new ArrayList<>();

        random=new SecureRandom();
        handler=new Handler();
        shakeAnimation= AnimationUtils.loadAnimation(getActivity(),R.anim.incorrect_shake);
        shakeAnimation.setRepeatCount(3);

        quizLinearLayout= (LinearLayout) view.findViewById(R.id.quizLinearLayout);
        questionNumberTextView= (TextView) view.findViewById(R.id.questionNumberTextView);
        flagImageView= (ImageView) view.findViewById(R.id.flagImageView);
        guessLinearLayouts=new LinearLayout[4];
        guessLinearLayouts[0]=(LinearLayout) view.findViewById(R.id.row1LinearLayout);
        guessLinearLayouts[1]=(LinearLayout) view.findViewById(R.id.row2LinearLayout);
        guessLinearLayouts[2]=(LinearLayout) view.findViewById(R.id.row3LinearLayout);
        guessLinearLayouts[3]=(LinearLayout) view.findViewById(R.id.row4LinearLayout);
        answerTextView= (TextView) view.findViewById(R.id.answerTextView);
        for (LinearLayout row:guessLinearLayouts)
        {
            for (int column=0;column<row.getChildCount();column++)
            {
                Button Myb= (Button) row.getChildAt(column);
                Myb.setOnClickListener(onClickList);
            }
        }
        questionNumberTextView.setText(getString(R.string.question,1,FLAGS_IN_QUIZ));
        return view;
    }
    private OnClickListener onClickList=new OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };
}
