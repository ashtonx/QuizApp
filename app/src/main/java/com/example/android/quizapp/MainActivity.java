package com.example.android.quizapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.util.Log.wtf;

//4-10 questions
//question types:
// free text response.
// checkboxes (multiple  answers?)
// radio - one answer
// submit button.

public class MainActivity extends AppCompatActivity {
//GLOBAL Types
    private final int NUMBER_OF_QUESTIONS = 10; // for now 10, maybe random 4-10 or settings later
    ArrayList<QuizData> Qdata; //store QA data
    public enum Qtype {FREE, MULTIPLE, SINGLE};
    Qtype[] type = new Qtype[NUMBER_OF_QUESTIONS]; //size of [number of questions]

    XmlParser parser = new XmlParser();
    List<Kana> ktmp = null;
    InputStream kana = null;
    String k_romaji=null;
    String k_hiragana=null;
    String k_katakana=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void getData(){
        try {
            kana = getAssets().open("kana.xml");
            ktmp = parser.parse(kana);
            kana.close();
        }
        catch(IOException e){}
        catch(XmlPullParserException y){}
    }


    public String getString(){
        getData();
        String ts="";
        Kana tk = new Kana("","","");
        for (int i = 0; i<ktmp.size();++i){
            tk = (Kana) ktmp.get(i);
            ts+=tk.romaji+"\t"+tk.hiragana+"\t"+tk.katakana+"\n";
        }
        return ts;
    }
    public void refresh (View v){
        TextView mtxt = (TextView) findViewById(R.id.screen_text);
        String s = getString();

        mtxt.setText(s);
        wtf("refresh", s);
    }

    public class QuizData {
        Qtype type;
        Kana data;
    }

    private void randomize(){
        Collections.shuffle(Qdata); // randomize questions;
        //FILL Qtype at least one of each,
        //Shuffle Qtype
    }
}

//TODO Store QA Data
// format: [Romaji] [Hiragana] [Katakana]
// in separate file.
// options:
// xml - lot of work, will still need to store it somewhere..
// Shared Preferences
// Internal Storage -  would still need to parse it...
// External Storage - pointless
// SQLite DB - bit overdone but not bad ?

//TODO Read in QA Data
// in: data stream
// out: QA Data - Array of Q Class
// TODO Randomize Order
// in: Randomize QAdata,
// Qtype[] = fill 1-3, at least one of each.
//TODO Generate question.
// in: int curr
// get type
// Questions[]
// get random fake answers
//generate Q,A,A,A
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