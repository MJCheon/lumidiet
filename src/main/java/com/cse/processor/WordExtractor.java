package com.cse.processor;

import com.clearspring.analytics.util.Lists;
import com.cse.entity.InnerWord;
import com.cse.entity.Word;
import com.twitter.penguin.korean.KoreanPosJava;
import com.twitter.penguin.korean.KoreanTokenJava;
import com.twitter.penguin.korean.TwitterKoreanProcessorJava;
import com.twitter.penguin.korean.phrase_extractor.KoreanPhraseExtractor;
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer;
import scala.collection.Seq;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by bullet on 16. 9. 8.
 * 문장 또는 본문에서의 Stemming 작업
 * 고유명사의 경우 100%로 제대로 된 고유 명사가 나오진 않음
 * 명사, 고유명사 이외의 숫자가 포함된 단어 (1세, 12경기) 같은 단어도 추출
 * 또한 시간 (2014년, 15시) 등의 단어도 추출
 */
public class WordExtractor implements Serializable{
    private static String allJosa = "었,께서,에서,에게,보다,라고,이여,이시여,마따나,이며,부터,로부터,으로부터,야말로,이라며," +
            "라며,라서,로서,로써,에다,까지,마저,조차,따라,토록,커녕,든지,이든지,나마,이나마,았";
    private static String[] josaList = allJosa.split("\\,");

    /**
     * 문장으로부터 순서대로 단어를 추출하여
     * 한 줄로 반환
     * @param paragraph 페이지 본문
     * @return 단어리스트
     */
    public static List<String> extractWordList(String paragraph){
        List<KoreanTokenJava> stemmedLinelist = stemmingLine(paragraph);
        ArrayList<String> nounList = getProperNourns(extractPhrases(paragraph));

        List<String> wordList = Lists.newArrayList();


        for(int i=0; i<stemmedLinelist.size(); i++) {
            KoreanTokenJava token = stemmedLinelist.get(i);
            String word = "";
            if ((token.getPos().equals(KoreanPosJava.Noun) || token.getPos().equals(KoreanPosJava.ProperNoun))) {
                word = token.getText();
                if (token.getLength() > 1) {  // 2글자 이상의 명사 출력 ("12경기" 같은 것도 포함)
                    if (i != 0 && stemmedLinelist.get(i - 1).getPos().equals(KoreanPosJava.Number)) {
                        if (!isTime(stemmedLinelist.get(i - 1)))
                            word = stemmedLinelist.get(i - 1).getText() + word;
                    }
                }
                else if (token.getLength() == 1) {    // "1세" 같은 것 출력
                    if (i != 0 && stemmedLinelist.get(i - 1).getPos().equals(KoreanPosJava.Number)) {
                        if (!isTime(stemmedLinelist.get(i - 1)))
                            word = stemmedLinelist.get(i - 1).getText() + word;
                    }
                    else
                        word = "";
                }
            } else if (token.getPos().equals(KoreanPosJava.Number) && token.getLength() > 1) {      // 년월일 같은 것들 출력
                if (isTime(token))
                    word = token.getText();
            }
            else
                word = "";

            if(word.equals(""))
                continue;

            word = foundProperNoun(nounList, word);
            word = removeJosa(word);

            wordList.add(word);
        }

        return wordList;
    }

    /**
     * 페이지의 전체 본문에서 단어를 추출
     * 중복되는 단어의 경우 Count.
     * 각 단어별로 TF 계산
     * @param pageId 페이지 번호
     * @param body 페이지 본문
     * @return
     */
    public static List<Word> extractWordFromParagraph(int pageId, String body){
        Hashtable<String, InnerWord> innerWordHashTable = new Hashtable<>();
        double allCnt = 0;
        List<KoreanTokenJava> stemmedLinelist = stemmingLine(body);
        ArrayList<String> nameList = getProperNourns(extractPhrases(body));

        if(stemmedLinelist.size() == 0)
            return Lists.newArrayList();

        for(int i=0; i<stemmedLinelist.size(); i++) {
            KoreanTokenJava token = stemmedLinelist.get(i);
            String word = "";
            if ((token.getPos().equals(KoreanPosJava.Noun) || token.getPos().equals(KoreanPosJava.ProperNoun))) {
                word = token.getText();
                // 2글자 이상의 명사 출력 ("12경기" 같은 것도 포함)
                if (token.getLength() > 1) {
                    if (i != 0 && stemmedLinelist.get(i - 1).getPos().equals(KoreanPosJava.Number)) {
                        if (!isTime(stemmedLinelist.get(i - 1)))
                            word = stemmedLinelist.get(i - 1).getText() + word;
                    }
                }
                // "1세" 같은 것 출력
                else if (token.getLength() == 1) {
                    if (i != 0 && stemmedLinelist.get(i - 1).getPos().equals(KoreanPosJava.Number)) {
                        if (!isTime(stemmedLinelist.get(i - 1)))
                            word = stemmedLinelist.get(i - 1).getText() + word;
                    }
                    else
                        word = "";
                }
            }
            // 시간에 관한 것들 출력
            else if (token.getPos().equals(KoreanPosJava.Number) && token.getLength() > 1) {
                if (isTime(token))
                    word = token.getText();
            }
            else
                word = "";

            if(word.equals(""))
                continue;

            word = foundProperNoun(nameList, word);
            word = removeJosa(word);

            allCnt++;

            if(innerWordHashTable.containsKey(word))
                innerWordHashTable.get(word).increaseCnt();
            else
                innerWordHashTable.put(word, new InnerWord(word));
        }

        if(innerWordHashTable.size()==0)
            return Lists.newArrayList();

        List<Word> wordList = Lists.newArrayList();

        for (InnerWord innerWord : innerWordHashTable.values()) {
            Word w = new Word(pageId, innerWord.getWord());
            w.setTf((innerWord.getCnt() / allCnt));
            w.setCnt(innerWord.getCnt());
            wordList.add(w);
        }

        return wordList;
    }

    private static List<KoreanTokenJava> stemmingLine(String line){
        try {
            CharSequence normalized = TwitterKoreanProcessorJava.normalize(line);
            Seq<KoreanTokenizer.KoreanToken> tokens = TwitterKoreanProcessorJava.tokenize(normalized);
            Seq<KoreanTokenizer.KoreanToken> stemmed = TwitterKoreanProcessorJava.stem(tokens);
            return TwitterKoreanProcessorJava.tokensToJavaKoreanTokenList(stemmed);
        }
        catch (Exception e){
            return com.google.common.collect.Lists.newArrayList();
        }
    }

    /**
     * 고유명사를 추출하기 위한 어근화
     * @param line 문장 또는 전체 본문
     * @return
     */
    public static List<KoreanPhraseExtractor.KoreanPhrase> extractPhrases(String line){
        CharSequence normalized = TwitterKoreanProcessorJava.normalize(line);
        Seq<KoreanTokenizer.KoreanToken> tokens = TwitterKoreanProcessorJava.tokenize(normalized);
        return TwitterKoreanProcessorJava.extractPhrases(tokens, false, false);
    }

    private static boolean isTime(KoreanTokenJava koreanTokenJava){
        String txt = koreanTokenJava.getText();
        if(txt.contains("년") || txt.contains("월") || txt.contains("일"))
            return true;
        else if(txt.contains("시") || txt.contains("분") || txt.contains("초"))
            return true;
        else
            return false;
    }

    /**
     * 고유명사에 대한 List를 반환
     * @param phraseList 어근화한 것들
     * @return 고유명사에 해당하는 단어들
     */
    private static ArrayList<String> getProperNourns(List<KoreanPhraseExtractor.KoreanPhrase> phraseList){
        ArrayList<String> nameList = new ArrayList<String>();
        for(KoreanPhraseExtractor.KoreanPhrase phrase : phraseList){
            String word = phrase.text();
            if(word.length() > 2 && word.length() < 4 && !word.contains(" ") && !word.contains(",")){
                nameList.add(word);
            }
        }

        return nameList;
    }

    /**
     * 단어의 조사가 포함되는 경우, 제거
     * @param word 추출한 단어
     * @return
     */
    private static String removeJosa(String word){
        for(String josa : josaList){
            if(word.contains(josa))
                word = "";
        }

        return word;
    }

    /**
     * 추출한 단어가 고유명사인 경우 Replace
     * @param nameList 고유명사에 해당하는 단어들
     * @param word 고유명사
     * @return
     */
    private static String foundProperNoun(ArrayList<String> nameList, String word){
        for(String name : nameList){
            if(name.contains(word))
                return name;
        }
        return word;
    }
}
