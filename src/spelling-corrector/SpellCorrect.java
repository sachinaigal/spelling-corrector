package practice.apple;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

/**
 * A Java impl of Peter Norvig's spell corrector [http://norvig.com/spell-correct.html]
 */
public class SpellCorrect {

    public static String WORDS_FILE = "/home/sachin/dev/big.txt";
    private static HashMap<String, Integer> frequencyMap = new HashMap<String, Integer>();

    public static void main(String[] args) throws IOException {

        long start = System.currentTimeMillis();

        getWordFrequency();

        HashMap<String, String> testWords = new HashMap<String, String>();
        testWords.put("acess", "access");
        testWords.put("accesing", "accessing");
        testWords.put("accomodation", "accommodation");
        testWords.put("acommodation", "accommodation");
        testWords.put("acomodation", "accommodation");
        testWords.put("acount", "account");
//        evaluate(testWords);
        for (String word : testWords.keySet()) {
            System.out.println(word + "\t\t\t" + correct(word));
        }
        System.out.println("\nTime: " + ((System.currentTimeMillis() - start) / (1000)) + " seconds");
    }

    /*    public static void getWordFrequency() throws IOException {

        }*/
    // P(c) [language model] - get frequency of all alphabetic words (converted to lowercase)
    public static void getWordFrequency() throws IOException {
        BufferedReader br = null;
        br = new BufferedReader(new FileReader(WORDS_FILE));
        String line = null;

        while ((line = br.readLine()) != null) {
            // read words
            Scanner scanner = new Scanner(line.toLowerCase());
            while (scanner.hasNext()) {
                String word = scanner.next();
                // index only words with letters
                boolean validWord = true;
                int length = word.length();
                for (int i = 0; i < length; i++) {
                    char ch = word.charAt(i);
                    if (ch < 'a' || ch > 'z') {
                        validWord = false;
                        break;
                    }
                }

                if (validWord) {
                    Integer currentFrequency = frequencyMap.get(word);
                    frequencyMap.put(word, currentFrequency == null ? 1 : ++currentFrequency);
                }
            }
            scanner.close();
        }
        br.close();
    }

    /**
     * P(w|c) [error model]
     * Use trivial model: all known edit1 words are infinitely more probable than known edit2 words and infinitely less probable than a known word itself
     */
    public static String correct(String word) {
        word = word.toLowerCase();

        // known word - no correction needed
        if (frequencyMap.get(word) != null) {
            return word;
        }

        // edit distance 1 candidates
        HashSet<String> candidates1 = new HashSet<String>();
        addEdit1Candidates(word, candidates1);
        String bestCandidate = returnBestCandidate(candidates1);
        if (bestCandidate != null) {
            return bestCandidate;
        }

        // edit distance 2 candidates: just apply edit1 to all the results of edit1
        HashSet<String> candidates2 = new HashSet<String>();
        for (String candidate : candidates1) {
            addEdit1Candidates(candidate, candidates2);
        }
        bestCandidate = returnBestCandidate(candidates2);
        if (bestCandidate != null) {
            return bestCandidate;
        }

        // no known corrections
        return word;
    }

    // return candidate with max frequency
    public static String returnBestCandidate(HashSet<String> candidates) {
        int max = 0;
        String bestCandidate = null;
        for (String candidate : candidates) {
            Integer candidateFrequency = frequencyMap.get(candidate);
            if (candidateFrequency > max) {
                max = candidateFrequency;
                bestCandidate = candidate;
            }
        }
        return bestCandidate;
    }

    // valid words at edit distance 1
    public static void addEdit1Candidates(String word, HashSet<String> candidates) {
        int length = word.length();

        // deletion (remove one letter)
        for (int i = 0; i < length; i++) {
            String editedWord = word.substring(0, i) + word.substring(i + 1, length);
            if (frequencyMap.get(editedWord) != null) {
                candidates.add(editedWord);
            }
        }

        // transposition (swap adjacent letters)
        char[] wordCharArray = word.toCharArray();
        for (int i = 0; i < length - 1; i++) {
            char temp = wordCharArray[i];
            wordCharArray[i] = wordCharArray[i + 1];
            wordCharArray[i + 1] = temp;

            String editedWord = new String(wordCharArray);
            if (frequencyMap.get(editedWord) != null) {
                candidates.add(editedWord);
            }

            // revert to original word
            temp = wordCharArray[i];
            wordCharArray[i] = wordCharArray[i + 1];
            wordCharArray[i + 1] = temp;
        }

        // alteration (change one letter to another)
        for (int i = 0; i < length; i++) {
            for (char ch = 'a'; ch <= 'z'; ch++) {
                char oldChar = wordCharArray[i];

                wordCharArray[i] = ch;

                String editedWord = new String(wordCharArray);
                if (frequencyMap.get(editedWord) != null) {
                    candidates.add(editedWord);
                }

                // revert to original char
                wordCharArray[i] = oldChar;
            }
        }

        // insertion (add a letter)
        for (int i = 0; i < length; i++) {
            for (char ch = 'a'; ch <= 'z'; ch++) {
                String editedWord = word.substring(0, i) + ch + word.substring(i, length);
                if (frequencyMap.get(editedWord) != null) {
                    candidates.add(editedWord);
                }
            }
        }
    }
}


