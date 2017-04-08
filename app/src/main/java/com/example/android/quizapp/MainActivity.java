package com.example.android.quizapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
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
// submit button.

public class MainActivity extends AppCompatActivity {
    //GLOBAL Types
    private final String FILE_NAME = "kana.xml";
    private final int NUMBER_OF_QUESTIONS = 10;
    private final int NUMBER_OF_MULTIPLE_ANSWERS = 4;
    List<Question> quizData = new ArrayList<>();

    LinearLayout layoutCheckbox;
    LinearLayout layoutRadio;
    LinearLayout layoutText;
    LinearLayout QuestionsLL;

    List<Kana> parsedData = null;
    Random rand = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layoutCheckbox = (LinearLayout) findViewById(R.id.layout_checkbox);
        layoutRadio = (LinearLayout) findViewById(R.id.layout_radio);
        layoutText = (LinearLayout) findViewById(R.id.layout_text_entry);
        QuestionsLL = (LinearLayout) findViewById(R.id.questions);
        start();
    }

    public void start() {
        parsedData = parseFile(FILE_NAME);
        quizData = generateQuiz(parsedData, NUMBER_OF_QUESTIONS,
                NUMBER_OF_MULTIPLE_ANSWERS);
        parsedData.clear();
        displayQuestions(quizData);
    }

    public void displayQuestions(List<Question> quizData) {
        View layout;
        TextView questionTV;
        CheckBox[] answersCheckBox = new CheckBox[NUMBER_OF_MULTIPLE_ANSWERS];
        RadioButton[] answersRadio = new RadioButton[NUMBER_OF_MULTIPLE_ANSWERS];

        for (int questionNumber = 0; questionNumber < quizData.size(); ++questionNumber) {
            Question question = quizData.get(questionNumber);
            switch (question.questionType) {
                case FREE:
                    layout = getLayoutInflater().inflate(R.layout.question_text_entry, layoutText, false);
                    questionTV = (TextView) layout.findViewById(R.id.question_field);
                    questionTV.setText("(" + (questionNumber + 1) + ") " + question.question);
                    questionTV.setTag(question.questionType);
                    questionTV.setId(questionNumber);
                    QuestionsLL.addView(layout);
                    break;
                case MULTIPLE:
                    layout = getLayoutInflater().inflate(R.layout.question_checkbox, layoutCheckbox, false);
                    questionTV = (TextView) layout.findViewById(R.id.question_field);
                    questionTV.setText("(" + (questionNumber + 1) + ") " + question.question);
                    questionTV.setTag(question.questionType);
                    answersCheckBox[0] = (CheckBox) layout.findViewById(R.id.answer1);
                    answersCheckBox[1] = (CheckBox) layout.findViewById(R.id.answer2);
                    answersCheckBox[2] = (CheckBox) layout.findViewById(R.id.answer3);
                    answersCheckBox[3] = (CheckBox) layout.findViewById(R.id.answer4);
                    for (int i = 0; i < NUMBER_OF_MULTIPLE_ANSWERS; ++i) {
                        answersCheckBox[i].setText(question.answers.get(i));
                    }
                    QuestionsLL.addView(layout);
                    //log
                    Log.i("displayMulti", "Question (" + questionNumber + "): " + question.question);
                    for (int i = 0; i < question.answers.size(); ++i) {
                        Log.i("displayMulti", "Answer (" + i + "): " + question.answers.elementAt(i));
                    }
                    break;

                case SINGLE:
                    View tmp = getLayoutInflater().inflate(R.layout.question_radio, layoutRadio, false);
                    TextView tv = (TextView) tmp.findViewById(R.id.question_field);
                    tv.setText("(" + (questionNumber + 1) + ") " + question.question);
                    tv.setTag(question.questionType);
                    answersRadio[0] = (RadioButton) tmp.findViewById(R.id.answer1);
                    answersRadio[1] = (RadioButton) tmp.findViewById(R.id.answer2);
                    answersRadio[2] = (RadioButton) tmp.findViewById(R.id.answer3);
                    answersRadio[3] = (RadioButton) tmp.findViewById(R.id.answer4);
                    //wonder if there's a way around it...
                    for (int i = 0; i < NUMBER_OF_MULTIPLE_ANSWERS; ++i) {
                        answersRadio[i].setText(question.answers.get(i));
                    }
                    QuestionsLL.addView(tmp);
                    //log
                    Log.i("displaySingle", "Question(" + questionNumber + "): " + question.question + " (" + question.answer_type.toString() + ")");
                    for (int i = 0; i < question.answers.size(); ++i) {
                        Log.i("displaySingle", "Answer " + i + " " + question.answers.elementAt(i));
                        break;
                    }
            }
        }
    }

    private List parseFile(String in) {
        List out = new ArrayList();
        XmlParser parser = new XmlParser();
        InputStream is;
        try {
            is = getAssets().open(in);
            out = parser.parse(is);
            is.close();
        } catch (IOException e) {
            Log.e("getData", e.getMessage());
        } catch (XmlPullParserException e) {
            Log.e("getData", e.getMessage());
        }
        return out;
    }

    public List generateQuiz(List<Kana> in, int numberOfQuestions, int numberOfAnswers) {
        List <Question> out = new ArrayList<>(numberOfQuestions);
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
            hiragana = rand.nextBoolean();
            if (hiragana) {
                answers.add(in.get(rand.nextInt(in.size())).hiragana);
            } else {
                answers.add(in.get(rand.nextInt(in.size())).katakana);
            }
        }
    }

    public enum QuestionType {FREE, MULTIPLE, SINGLE}

    public enum KanaType {ROMAJI, HIRAGANA, KATAKANA}

    public final class Question {
        public final Kana data;
        public final String question;
        public final QuestionType questionType;
        public final Vector<String> answers;
        public final KanaType answer_type;

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
        }

        Question(Kana data, String question, QuestionType questionType, Vector<String> answers,
                 KanaType answer_type) {
            this.data = data;
            this.question = question;
            this.questionType = questionType;
            this.answers = answers;
            this.answer_type = answer_type;
        }
    }
}


//TODO Get Answer
// view input, get answer(s).
//get answer to a question, send it for checks.
//TODO Check Answer
//compare answer(s) with questions ? find, compare.
//verify answer and send it to progress register
//TODO Register Progress
//Register which question it was
//Register wether it was correct or not.
//potentially register answer wether it was good or bad...
//TODO Check Progress
//check if done, if not generateQuiz new question if it's ok display Finished.
//TODO Finished
// Display Results, option to reset.
//TODO Clean and polish
//TODO Settings ?