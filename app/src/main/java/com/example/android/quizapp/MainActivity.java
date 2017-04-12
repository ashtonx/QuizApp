package com.example.android.quizapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import static com.example.android.quizapp.MainActivity.KanaType.HIRAGANA;
import static com.example.android.quizapp.MainActivity.KanaType.KATAKANA;
import static com.example.android.quizapp.MainActivity.KanaType.ROMAJI;
import static com.example.android.quizapp.MainActivity.QuestionType.FREE;
import static com.example.android.quizapp.MainActivity.QuestionType.MULTIPLE;
import static com.example.android.quizapp.MainActivity.QuestionType.SINGLE;


//4-10 questions
//question types:
// free text response.
// checkboxes (multiple  answers?)
// radio - one answer
// submit score.

public class MainActivity extends AppCompatActivity {
    final int SUBMIT_ID = 42;
    //GLOBAL Types
    private final String FILE_NAME = "kana.xml";
    private final int NUMBER_OF_QUESTIONS = 10;
    private final int NUMBER_OF_MULTIPLE_ANSWERS = 4;
    private final String RETAINED_QUIZ_TAG = "quizData";
    List<Question> quizData = new ArrayList<>();
    LinearLayout layoutCheckbox;
    LinearLayout layoutRadio;
    LinearLayout layoutText;
    LinearLayout layoutScore;
    LinearLayout mainContent;
    List<Kana> parsedData = null;
    Random rand = new Random();
    boolean started = false;
    private RetainedFragment<List<Question>> dataFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layoutCheckbox = (LinearLayout) findViewById(R.id.layout_checkbox);
        layoutRadio = (LinearLayout) findViewById(R.id.layout_radio);
        layoutText = (LinearLayout) findViewById(R.id.layout_text_entry);
        layoutScore = (LinearLayout) findViewById(R.id.layout_score);
        mainContent = (LinearLayout) findViewById(R.id.content);

        FragmentManager fm = getFragmentManager();
        dataFragment = (RetainedFragment<List<Question>>) fm.findFragmentByTag(RETAINED_QUIZ_TAG);
        if (dataFragment == null) {
            dataFragment = new RetainedFragment<>();
            fm.beginTransaction().add(dataFragment, RETAINED_QUIZ_TAG).commit();
            parsedData = (List<Kana>) parseFile(FILE_NAME);
            quizData = (List<Question>) generateQuiz(parsedData, NUMBER_OF_QUESTIONS,
                    NUMBER_OF_MULTIPLE_ANSWERS);
            parsedData.clear();
            dataFragment.setData(quizData);
        }
        quizData = dataFragment.getData();
        displayQuestions(quizData);
    }

    public void displayQuestions(List<Question> quizData) {
        ArrayList<View> questions = new ArrayList<>();
        for (int questionNumber = 0; questionNumber < quizData.size(); ++questionNumber) {
            mainContent.addView(generateQuestionView(quizData.get(questionNumber), questionNumber));
        }
        View score = getLayoutInflater().inflate(R.layout.score, layoutScore, false);
        mainContent.addView(score);
    }

    public void checkScore(View v) {
        int correct = 0;
        int wrong = 0;
        for (Question question : quizData) {
            switch (question.questionType) {
                case FREE:
                    if (question.user_input.equalsIgnoreCase(question.data.romaji)) ++correct;
                    else ++wrong;
                    break;
                case MULTIPLE:
                    int checkboxCorrect = 0;
                    int countTrue = 0;
                    for (int i = 0; i < NUMBER_OF_MULTIPLE_ANSWERS; ++i) {
                        if (question.checked_answers[i]) {
                            countTrue++;
                            String tmp_answer = question.answers.get(i);
                            if (tmp_answer.equalsIgnoreCase(question.data.katakana) ||
                                    tmp_answer.equalsIgnoreCase(question.data.hiragana))
                                ++checkboxCorrect;
                        }
                    }
                    if (countTrue == 2 && checkboxCorrect == 2) ++correct;
                    else ++wrong;
                    break;
                case SINGLE:
                    switch (question.answer_type) {
                        case HIRAGANA:
                            if (question.user_input.equalsIgnoreCase(question.data.hiragana))
                                ++correct;
                            else ++wrong;
                            break;
                        case KATAKANA:
                            if (question.user_input.equalsIgnoreCase(question.data.katakana))
                                ++correct;
                            else ++wrong;
                            break;
                        case ROMAJI:
                            if (question.user_input.equalsIgnoreCase(question.data.romaji))
                                ++correct;
                            else ++wrong;
                            break;
                    }
                    break;
            }
        }
        displayScore(correct, wrong);
    }

    private void displayScore(int pointsCorrect, int pointsWrong) {
        TextView correctTV = (TextView) findViewById(R.id.scoreCorrect);
        TextView wrongTV = (TextView) findViewById(R.id.scoreWrong);
        correctTV.setText(getString(R.string.score_correct) + String.valueOf(pointsCorrect));
        wrongTV.setText(getString(R.string.score_wrong) + String.valueOf(pointsWrong));
    }

    private List parseFile(String in) {
        List out = new ArrayList();
        XmlParser parser = new XmlParser();
        InputStream is;
        try {
            is = getAssets().open(in);
            out = parser.parse(is);
            is.close();
        } catch (IOException io) {
            Log.e("getData", io.getMessage());
        } catch (XmlPullParserException xml) {
            Log.e("getData", xml.getMessage());
        }
        return out;
    }

    public List generateQuiz(List<Kana> in, int numberOfQuestions, int numberOfAnswers) {
        List<Question> out = new ArrayList<>(numberOfQuestions);
        Collections.shuffle(in);
        QuestionType tmp_type;
        for (int i = 0; i < numberOfQuestions; ++i) {
            tmp_type = QuestionType.values()[rand.nextInt(QuestionType.values().length)];
            switch (tmp_type) {
                case FREE:
                    out.add(generateQuestion(in, i, FREE, numberOfAnswers));
                    break;
                case MULTIPLE:
                    out.add(generateQuestion(in, i, MULTIPLE, numberOfAnswers));
                    break;
                case SINGLE:
                    out.add(generateQuestion(in, i, SINGLE, numberOfAnswers));
                    break;
            }
        }
        return out;
    }

    private Question generateQuestion(List<Kana> in, int iter, QuestionType questionType, int numberOfAnswers) {
        Kana data = in.get(iter);
        String questionString;
        String questionCharacter;
        Vector<String> answers = null;
        KanaType answerType;
        Question out = null;
        switch (questionType) {
            case FREE:
                boolean questionHiragana = rand.nextBoolean();
                if (questionHiragana) questionCharacter = data.hiragana;
                else questionCharacter = data.katakana;
                questionString = "Type Romaji for " + questionCharacter + " :";
                out = new Question(data, questionString, FREE);
                break;

            case MULTIPLE:
                questionString = "Chose Hiragana and Katakana for " + data.romaji + " :";
                answers = new Vector<>(numberOfAnswers);
                answers.add(data.hiragana);
                answers.add(data.katakana);
                getRandomKana(in, answers, numberOfAnswers);
                Collections.shuffle(answers);
                out = new Question(data, questionString, MULTIPLE, answers);
                break;

            case SINGLE:
                answers = new Vector<>(numberOfAnswers);
                boolean questionRomaji = rand.nextBoolean();
                if (questionRomaji) {
                    boolean answerHiragana = rand.nextBoolean();
                    if (answerHiragana) {
                        answerType = HIRAGANA;
                        answers.add(data.hiragana);
                    } else {
                        answerType = KATAKANA;
                        answers.add(data.katakana);
                    }
                    questionCharacter = data.romaji;
                    getRandomKana(in, answers, numberOfAnswers);
                } else {
                    answerType = ROMAJI;
                    answers.add(data.romaji);
                    getRandomRomaji(in, answers, numberOfAnswers);
                    boolean QuestionHiragana = rand.nextBoolean();
                    if (QuestionHiragana) questionCharacter = data.hiragana;
                    else questionCharacter = data.katakana;
                }
                questionString = "Choose " + answerType.toString() + " for " + questionCharacter + " :";
                Collections.shuffle(answers);
                out = new Question(data, questionString, SINGLE, answers, answerType);
        }
        return out;
    }

    private void getRandomRomaji(List<Kana> in, Vector<String> answers, int numberOfAnswers) {
        while (answers.size() < numberOfAnswers)
            answers.add(in.get(rand.nextInt(in.size())).romaji);
    }

    private void getRandomKana(List<Kana> in, Vector<String> answers, int numberOfAnswers) {
        Boolean hiragana;
        while (answers.size() < numberOfAnswers) {
            String tmp = null;
            hiragana = rand.nextBoolean();
            if (hiragana) {
                while (tmp == null || answers.contains(tmp))
                    tmp = in.get(rand.nextInt(in.size())).hiragana;
                answers.add(tmp);
            } else {
                while (tmp == null || answers.contains(tmp))
                    tmp = in.get(rand.nextInt(in.size())).katakana;
                answers.add(tmp);
            }
        }
    }

    private View generateQuestionView(Question question, int questionNumber) {
        View layout = null;
        TextView questionString;
        switch (question.questionType) {
            case FREE:
                layout = getLayoutInflater().inflate(R.layout.question_text_entry, layoutText, false);
                questionString = (TextView) layout.findViewById(R.id.question);
                questionString.setText("(" + questionNumber + ") " + question.question);
                EditText answerText = (EditText) layout.findViewById(R.id.answer);
                answerText.setOnFocusChangeListener(question);
                break;
            case MULTIPLE:
                layout = getLayoutInflater().inflate(R.layout.question_checkbox, layoutCheckbox, false);
                questionString = (TextView) layout.findViewById(R.id.question);
                questionString.setText("(" + questionNumber + ") " + question.question);
                LinearLayout answersLL = (LinearLayout) layout.findViewById(R.id.answers);
                CheckBox answerCheckbox;
                for (int i = 0; i < answersLL.getChildCount(); ++i) {
                    answerCheckbox = (CheckBox) answersLL.getChildAt(i);
                    answerCheckbox.setText(question.answers.get(i));
                    answerCheckbox.setId(i);
                    answerCheckbox.setOnClickListener(question);
                    if (question.user_input.equalsIgnoreCase(question.answers.get(i)))
                        answerCheckbox.setChecked(true);//retain answers
                }
                break;
            case SINGLE:
                layout = getLayoutInflater().inflate(R.layout.question_radio, layoutRadio, false);
                questionString = (TextView) layout.findViewById(R.id.question);
                questionString.setText("(" + (questionNumber + 1) + ") " + question.question);
                RadioGroup answersRG = (RadioGroup) layout.findViewById(R.id.answers);
                RadioButton answerRadio;
                for (int i = 0; i < answersRG.getChildCount(); ++i) {
                    answerRadio = (RadioButton) answersRG.getChildAt(i);
                    answerRadio.setText(question.answers.get(i));
                    answerRadio.setId(i);
                    answerRadio.setOnClickListener(question);
                    if (question.checked_answers[i]) answerRadio.setChecked(true); //retain answers
                }
                break;
        }
        return layout;
    }

    public enum QuestionType {FREE, MULTIPLE, SINGLE}

    public enum KanaType {ROMAJI, HIRAGANA, KATAKANA}

    public static class RetainedFragment<T> extends Fragment {
        public T data;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //retain this fragment
            setRetainInstance(true);
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }

    public final class Question implements View.OnClickListener, View.OnFocusChangeListener {
        public final Kana data;
        public final String question;
        public final QuestionType questionType;
        public final Vector<String> answers;
        public final KanaType answer_type;
        public String user_input = "wrong";
        public boolean[] checked_answers = new boolean[NUMBER_OF_MULTIPLE_ANSWERS];

        public double correct = 0;

        Question(Kana data, String question, QuestionType type) {
            //free question Constructor
            this.data = data;
            this.question = question;
            this.questionType = type;
            this.answers = null;
            this.answer_type = null;
        }

        Question(Kana data, String question, QuestionType questionType, Vector<String> answers) {
            this.data = data;
            this.question = question;
            this.questionType = questionType;
            this.answers = answers;
            this.answer_type = null;
            for (int i = 0; i < NUMBER_OF_MULTIPLE_ANSWERS; ++i) {
                checked_answers[i] = false;
            }
        }

        Question(Kana data, String question, QuestionType questionType, Vector<String> answers,
                 KanaType answer_type) {
            this.data = data;
            this.question = question;
            this.questionType = questionType;
            this.answers = answers;
            this.answer_type = answer_type;
        }

        public void onClick(View v) {
            EditText inputText = null;
            CheckBox inputCheckBox = null;
            RadioButton inputRadio = null;

            switch (questionType) {
                case MULTIPLE:
                    inputCheckBox = (CheckBox) v;
                    inputCheckBox.isChecked();
                    int pos = answers.indexOf(inputCheckBox.getText().toString());
                    checked_answers[pos] = (inputCheckBox.isChecked()) ? true : false;
                    break;
                case SINGLE:
                    inputRadio = (RadioButton) v;
                    user_input = inputRadio.getText().toString();
            }
        }

        public void onFocusChange(View v, boolean hasFocus) {
            EditText input = (EditText) v;
            if (!hasFocus) {
                user_input = input.getText().toString();
            }
        }
    }
}

//TODO Clean and polish
//TODO Settings ?