package com.gerardoaugusto.myflagquiz;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
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
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onClick(View view) {
            Button clicked= (Button) view;
            String textCl=clicked.getText().toString();
            String answer=getCountryName(correctAnswer);
            totalGuesses++;
            if (textCl.equals(answer)){
                correctAnswers++;
                answerTextView.setText(answer+"!");
                answerTextView.setTextColor(getResources().getColor(R.color.correct_answer,getContext().getTheme()));

                disableButtons();
                if (correctAnswers==FLAGS_IN_QUIZ){
                    DialogFragment quizResults=new DialogFragment(){
                        @NonNull
                        @Override
                        public Dialog onCreateDialog(Bundle savedInstanceState) {
                            AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
                            builder.setMessage(getString(R.string.results,totalGuesses,1000/(double) totalGuesses));
                            builder.setPositiveButton(R.string.reset_quiz, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    resetQuiz();
                                }
                            });
                            return builder.create();
                        }
                    };
                    quizResults.setCancelable(false);
                    quizResults.show(getFragmentManager(),"Quiz results");
                }else{
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            animate(true);
                        }
                    },2000);
                }
            }else{
                flagImageView.startAnimation(shakeAnimation);

                answerTextView.setText(R.string.incorrect_answer);
                answerTextView.setTextColor(getResources().getColor(R.color.incorrect_answer,getContext().getTheme()));
                clicked.setEnabled(false);
            }
        }
    };
    //Desactiva todos los botones activos
    //Es llamado cuando se contesta correctamente
    private void disableButtons(){
        for (int row=0;row<guessRows;row++){
            for (int column=0;column<guessLinearLayouts[row].getChildCount();column++){
                guessLinearLayouts[row].getChildAt(column).setEnabled(false);
            }
        }
    }
    //Obtiene las preferencias y muestra solo la cantidad de filas adecuadas
    public void updateGuessRows(SharedPreferences sharedPreferences)
    {
        String choices=sharedPreferences.getString(MainActivity.CHOICES,null);
        guessRows=2;
        for (LinearLayout row :guessLinearLayouts)
        {
            row.setVisibility(GONE);
        }
        for (int i=0;i<guessRows;i++){
            guessLinearLayouts[i].setVisibility(VISIBLE);
        }
    }
    //Restablece el conjunto de regiones
    public void updateRegions(SharedPreferences sharedPreferences){
        regionsSet=sharedPreferences.getStringSet(MainActivity.REGIONS,null);
    }
    //Restablece la lista de paises y selecciona 10 de manera aleatoria
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void resetQuiz()
    {
        AssetManager assets= getActivity().getAssets();
        fileNameList.clear();

        try{
            for (String name:regionsSet)
            {
                String[] paths=assets.list(name);

                for (String path:paths)
                {
                    fileNameList.add(path.replace(".png",""));
                }
            }
        } catch (IOException e) {
            Log.e(TAG,"Ha ocurrido un error al cargar el recurso",e);
        }
        totalGuesses=0;
        correctAnswers=0;
        quizCountriesList.clear();

        int flagCounter=1;
        int numberOfFlags=fileNameList.size();
        while(flagCounter<FLAGS_IN_QUIZ){
            int randomIndex=random.nextInt(numberOfFlags);
            String file=fileNameList.get(randomIndex);
            if (!quizCountriesList.contains(file)){
                quizCountriesList.add(file);
                flagCounter++;
            }
        }
        loadNextFlag();
    }
    //  Carga la primer bandera y los respectivos botones
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void loadNextFlag(){
        //Obtiene el primer elemento de la lista de paises
        String nextImage=quizCountriesList.remove(0);
        correctAnswer=nextImage;
        answerTextView.setText("");
        questionNumberTextView.setText(getString(R.string.question,(correctAnswers+1),FLAGS_IN_QUIZ));

        AssetManager assets=getActivity().getAssets();
        //Obtiene el nombre de la region de la imagen actual
        String region=nextImage.substring(0,nextImage.indexOf("-"));
        //Obtiene la imagen y la establece en el ImageView
        try(InputStream stream=assets.open(region+"/"+nextImage+".png")){
            Drawable flag=Drawable.createFromStream(stream,nextImage);
            flagImageView.setImageDrawable(flag);

            animate(false);
        } catch (IOException e) {
            Log.e(TAG,"Se ha producido un error al cargar"+nextImage,e);
        }
        //Mezcla los elementos de la coleccion
        Collections.shuffle(quizCountriesList);
        //Pone la respuesta correcta al final
        int indexcorect=quizCountriesList.indexOf(correctAnswer);
        quizCountriesList.add(quizCountriesList.remove(indexcorect));
        //Establece el texto de los botones mostrados
        for (int row=0;row<guessRows;row++)
        {
            for (int column=0;column<guessLinearLayouts[row].getChildCount();column++){
                Button but= (Button) guessLinearLayouts[row].getChildAt(column);
                but.setEnabled(true);

                String filename=fileNameList.get((row*2)+column);
                but.setText(getCountryName(filename));
            }
        }
        int randrow=random.nextInt(guessRows);
        int randcol=random.nextInt(2);
        String correctCountry=getCountryName(correctAnswer);
        ((Button) guessLinearLayouts[randrow].getChildAt(randcol)).setText(correctCountry);
    }
    //Se encarga de mostrar la bandera actual utilizando la animacion de reveelacion circular
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void animate(boolean animateOut){
        if (correctAnswers==0)
        {
            return;
        }
        int centerX=quizLinearLayout.getWidth()/2;
        int centerY=quizLinearLayout.getHeight()/2;
        int radius=Math.max(quizLinearLayout.getWidth(),quizLinearLayout.getHeight());

        Animator animator;
        if(animateOut){
            animator= ViewAnimationUtils.createCircularReveal(quizLinearLayout,centerX,centerY,radius,0);
            animator.addListener(new AnimatorListenerAdapter() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onAnimationEnd(Animator animation) {
                    loadNextFlag();
                    super.onAnimationEnd(animation);
                }
            });
        }else{
            animator= ViewAnimationUtils.createCircularReveal(quizLinearLayout,centerX,centerY,0,radius);
        }
        animator.setDuration(300);
        animator.start();
    }
    private String getCountryName(String name)
    {
        return name.substring(name.indexOf("-")+1).replace("_"," ");
    }
}
