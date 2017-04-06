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

//4-10 questions
//question types:
// free text response.
// checkboxes (multiple  answers?)
// radio - one answer
// submit button.

public class MainActivity extends AppCompatActivity {
//GLOBAL Types
    private final int NUMBER_OF_QUESTIONS=10;
    private final int NUMBER_OF_MULTIPLE_ANSWERS=4;
    private final String FILE_NAME = "kana.xml";
    ArrayList<Kana> generated_questions=new ArrayList<>(); //for reference during checks
    public enum Qtype {FREE, MULTIPLE, SINGLE};
    Random rand = new Random();

    LinearLayout layoutCheckbox;
    LinearLayout layoutRadio;
    LinearLayout layoutText;
    LinearLayout QuestionsLL;

    List<Kana> parsed_data = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layoutCheckbox = (LinearLayout) findViewById(R.id.layout_checkbox);
        layoutRadio = (LinearLayout) findViewById(R.id.layout_radio);
        layoutText = (LinearLayout) findViewById(R.id.layout_text_entry);
        QuestionsLL= (LinearLayout) findViewById(R.id.questions);
        start();
    }

    public void start(){
        parsed_data = parseFile(FILE_NAME);
        generateQuiz();
    }

    private void generateQuiz(){
        Collections.shuffle(parsed_data);
        Qtype tmp_type;

        for (int i = 0; i<NUMBER_OF_QUESTIONS; ++i) {
            tmp_type = Qtype.values()[rand.nextInt(Qtype.values().length)];
            switch(tmp_type){
                case SINGLE:
                    generateSingleQuestion(parsed_data.get(i),i);
                    break;
                case MULTIPLE:
                    generateMultipleQuestion(parsed_data.get(i),i);
                    break;
                case FREE:
                    generateFreeQuestion(parsed_data.get(i),i);
            }
            generated_questions.add(parsed_data.get(i));
        }
        parsed_data.clear(); //no need for it anymore;
    }

    private void generateSingleQuestion(Kana data, int qIt) {
        String question;
        String answer_type;
        String question_char;
        Vector<String> answers = new Vector<>(NUMBER_OF_MULTIPLE_ANSWERS);

        Boolean questionRomaji = rand.nextBoolean();
        if (questionRomaji) {
            boolean AnswerHiragana = rand.nextBoolean();

            if (AnswerHiragana) {
                answer_type = "Hiragana";
                answers.add(data.hiragana);
            } else {
                answer_type = "Katakana";
                answers.add(data.katakana);
            }
            question_char=data.romaji;
            getRandomKana(answers);
        } else {
            answer_type="Romaji";
            answers.add(data.romaji);
            getRandomRomaji(answers);
            boolean QuestionHiragana = rand.nextBoolean();

            if (QuestionHiragana) question_char=data.hiragana;
            else question_char=data.katakana;
        }
        question = "Choose "+answer_type+" for "+question_char+" :";
        Collections.shuffle(answers);
        displaySingleQuestion(qIt,question,answer_type,answers);
    }

    private void generateMultipleQuestion(Kana data, int qIt){
        String question = "Chose Hiragana and Katakana for "+ data.romaji +" :";
        Vector<String> answers = new Vector<>(NUMBER_OF_MULTIPLE_ANSWERS);
        answers.add(data.hiragana);
        answers.add(data.katakana);
        getRandomKana(answers);
        Collections.shuffle(answers);
        displayMultipleQuestion(qIt,question,answers);
    }
    private void generateFreeQuestion(Kana data, int qIt){
        //assuming users don't have an input for katakana, that and i'm already overdoing this app;
        String question;
        String question_char;
        boolean questionHiragana = rand.nextBoolean();
        if(questionHiragana) question_char=data.hiragana;
        else question_char=data.katakana;
        question = "Type Romaji for "+question_char+" :";

        displayFreeQuestion(qIt, question);
    }

    public void displaySingleQuestion(int qIt, String question,String answer_type, Vector<String> answers){
        View tmp = getLayoutInflater().inflate(R.layout.question_radio,layoutRadio,false);

        TextView tv = (TextView) tmp.findViewById(R.id.question_field);
        tv.setText("("+(qIt+1)+") "+ question);

        RadioButton[] viewAnswers = new RadioButton[NUMBER_OF_MULTIPLE_ANSWERS];
        viewAnswers[0]=(RadioButton) tmp.findViewById(R.id.answer1);
        viewAnswers[1]=(RadioButton) tmp.findViewById(R.id.answer2);
        viewAnswers[2]=(RadioButton) tmp.findViewById(R.id.answer3);
        viewAnswers[3]=(RadioButton) tmp.findViewById(R.id.answer4);
        //wonder if there's a way around it...
        for (int i=0; i< NUMBER_OF_MULTIPLE_ANSWERS;++i){
            viewAnswers[i].setText(answers.get(i));
        }
        QuestionsLL.addView(tmp);
        //log
        Log.i("displaySingle","Question("+qIt+"): "+question+" ("+answer_type+")");
        for (int i = 0; i<answers.size();++i){
            Log.i("displaySingle","Answer "+i+" "+answers.elementAt(i));
        }
    }
    public void displayMultipleQuestion(int qIt, String question, Vector<String> answers){
        View tmp = getLayoutInflater().inflate(R.layout.question_checkbox,layoutCheckbox,false);

        TextView tv = (TextView) tmp.findViewById(R.id.question_field);
        tv.setText("("+(qIt+1)+") "+question);

        CheckBox[] viewAnswers = new CheckBox[NUMBER_OF_MULTIPLE_ANSWERS];
        viewAnswers[0]=(CheckBox) tmp.findViewById(R.id.answer1);
        viewAnswers[1]=(CheckBox) tmp.findViewById(R.id.answer2);
        viewAnswers[2]=(CheckBox) tmp.findViewById(R.id.answer3);
        viewAnswers[3]=(CheckBox) tmp.findViewById(R.id.answer4);

        for (int i=0; i< NUMBER_OF_MULTIPLE_ANSWERS;++i){
            viewAnswers[i].setText(answers.get(i));
        }
        QuestionsLL.addView(tmp);
        //log
        Log.i("displayMulti","Question ("+qIt+"): "+question);
        for (int i = 0; i<answers.size();++i){
            Log.i("displayMulti","Answer ("+i+"): "+answers.elementAt(i));
        }
    }
    public void displayFreeQuestion(int qIt, String question){
        View tmp = getLayoutInflater().inflate(R.layout.question_text_entry,layoutText,false);
        TextView tv = (TextView) tmp.findViewById(R.id.question_field);
        tv.setText("("+(qIt+1)+") "+question);
        QuestionsLL.addView(tmp);
        //log
        Log.i("displayFree","Question ("+qIt+"): "+question);
    }

    private void getRandomRomaji(Vector<String> answers){
        while (answers.size()<NUMBER_OF_MULTIPLE_ANSWERS)
        answers.add(parsed_data.get(rand.nextInt(parsed_data.size())).romaji);
    }

    private void getRandomKana(Vector<String> answers){
        Boolean kana;
        while(answers.size()<NUMBER_OF_MULTIPLE_ANSWERS){
            kana = rand.nextBoolean();//1 hiragana, 0 katakana
            if (kana) {
                answers.add(parsed_data.get(rand.nextInt(parsed_data.size())).hiragana);
            } else {
                answers.add(parsed_data.get(rand.nextInt(parsed_data.size())).katakana);
            }
        }
    }

    private List parseFile(String in) {
        List out = new ArrayList();
        XmlParser parser = new XmlParser();
        InputStream is = null;
        try {
            is = getAssets().open(in);
            out = parser.parse(is);
            is.close();
        }
        catch (IOException e) {
            Log.e("getData", e.getMessage());
        }
        catch (XmlPullParserException e) {
            Log.e("getData", e.getMessage());
        }
        return out;
    }
}


//TODO Display Question
// in: Q, A, A, A
// out: formated display
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
//check if done, if not generate new question if it's ok display Finished.
//TODO Finished
// Display Results, option to reset.
//TODO Settings ?