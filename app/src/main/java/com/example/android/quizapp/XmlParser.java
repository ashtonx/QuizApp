package com.example.android.quizapp;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

//I weren't sure if storing data in xml was a good idea, after this i'm confident it was a bad one...
public class XmlParser {
    private static final String ns = null;

        public static final String TAG_START = "SeiOn";
        public static final String TAG_ENTRY = "Character";
        public static final String TAG_ROMAJI = "Romaji";
        public static final String TAG_HIRAGANA = "Hiragana";
        public static final String TAG_KATAKANA = "Katakana";

        public List parse(InputStream in) throws XmlPullParserException, IOException{
        try{
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally { in.close();}
    }

    private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException{
        List entries = new ArrayList(); //entries

        parser.require(XmlPullParser.START_TAG, ns, TAG_START);
        while (parser.next() != XmlPullParser.END_TAG){
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(TAG_ENTRY)) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    private Kana readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG,ns,TAG_ENTRY);
        String romaji=null;
        String hiragana=null;
        String katakana=null;
        while (parser.next() != XmlPullParser.END_TAG){
            if (parser.getEventType() != XmlPullParser.START_TAG){
                continue;
            }
            String name = parser.getName();
            if (name.equals(TAG_ROMAJI)){
                romaji=readEntryElement(parser,TAG_ROMAJI);
            } else if (name.equals(TAG_HIRAGANA)){
                hiragana=readEntryElement(parser,TAG_HIRAGANA);
            } else if (name.equals(TAG_KATAKANA)){
                katakana=readEntryElement(parser,TAG_KATAKANA);
            } else {
                skip(parser);
            }
        }
        return new Kana(romaji,hiragana,katakana);
    }

    private String readEntryElement(XmlPullParser parser, String TAG) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, TAG);
        String element = readText(parser);
        parser.require(XmlPullParser.END_TAG,ns,TAG);
        return element;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException{
        String result = "";
        if (parser.next() == XmlPullParser.TEXT){
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException{
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth=1;
        while (depth !=0){
            switch (parser.next()){
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
