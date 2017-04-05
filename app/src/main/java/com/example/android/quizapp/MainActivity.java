package com.example.android.quizapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

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
   // ArrayList<QuizData> Qdata; //store QA data
    public enum Qtype {FREE, MULTIPLE, SINGLE};
    Random rand = new Random();

    List<Kana> parsed_data = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                    generateSingleQuestion(parsed_data.get(i));
                    break;
                case MULTIPLE:
                    generateMultipleQuestion(parsed_data.get(i));
                    break;
                case FREE:
                    generateFreeQuestion(parsed_data.get(i));
            }
        }
    }

    private void generateSingleQuestion(Kana data) {
        String question = "Choose ";
        Vector<String> answers = new Vector<>(NUMBER_OF_MULTIPLE_ANSWERS);

        Boolean questionRomaji = rand.nextBoolean();
        if (questionRomaji) {
            getRandomKana(answers);
            boolean AnswerHiragana = rand.nextBoolean();
            if (AnswerHiragana) {
                question += "Hiragana for " + data.romaji + " :";
                answers.set(0, data.hiragana);
            } else {
                question += "Katakana for " + data.romaji + " :";
                answers.set(0, data.katakana);
            }
        } else { //if question is not romaji,
            getRandomRomaji(answers);
            answers.set(0, data.romaji);
            question += "Romaji for ";
            boolean QuestionHiragana = rand.nextBoolean();
            if (QuestionHiragana) question += data.hiragana + " :";
            else question += data.katakana +": ";

        } // damn this is a mess
        Collections.shuffle(answers);
        displaySingleQuestion(question,answers);
    }

    private void generateMultipleQuestion(Kana data){
        String question = "Chose Hiragana and Katakana for "+ data.romaji +" :";
        Vector<String> answers = new Vector<>(NUMBER_OF_MULTIPLE_ANSWERS);
        getRandomKana(answers);
        answers.set(0,data.hiragana);
        answers.set(1,data.katakana);
        Collections.shuffle(answers);
        displayMultipleQuestion(question,answers);
    }
    private void generateFreeQuestion(Kana data){
        //assuming users don't have an input for katakana, that and i'm already overdoing this app;
        String question="Type Romaji for ";
        boolean questionHiragana = rand.nextBoolean();
        if(questionHiragana) question +=data.hiragana + " :";
        else question+=data.katakana + " :";
        displayFreeQuestion(question);
    }

    public void displaySingleQuestion(String question, Vector<String> answers){
        Log.i("displaySingle","Question: "+question);
        for (int i = 0; i<answers.size();++i){
            Log.i("displaySingle","Answer "+i+" "+answers.elementAt(i));
        }
    }
    public void displayMultipleQuestion(String question, Vector<String> answers){
        Log.i("displayMulti","Question: "+question);
        for (int i = 0; i<answers.size();++i){
            Log.i("displayMulti","Answer "+i+" "+answers.elementAt(i));
        }
    }
    public void displayFreeQuestion(String question){
        Log.i("displayFree",question);
    }

    private void getRandomRomaji(Vector<String> answers){
        for (int i = 0; i<NUMBER_OF_MULTIPLE_ANSWERS;++i)
        answers.add(parsed_data.get(rand.nextInt(parsed_data.size())).romaji);
    }

    private void getRandomKana(Vector<String> answers){
        Boolean kana;
        for(int i = 0; i<NUMBER_OF_MULTIPLE_ANSWERS;++i) {
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