package spellingcorrector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * A Java impl of Peter Norvig's spell corrector [http://norvig.com/spell-correct.html]
 */
public class SpellCorrect {

    public static final String WORD_DELIM = "[\\p{Punct}\\s]+";
    public static String WORDS_FILE = "/home/sachin/dev/github/spellingcorrector/src/big.txt";
    private static HashMap<String, Integer> frequencyMap = new HashMap<String, Integer>();
    private static HashMap<String, String> test1Words = new HashMap<String, String>();
    private static HashMap<String, String> test2Words = new HashMap<String, String>();
//    private static Set<String> printWords = new HashSet<String>(Arrays.asList("economtric", "embaras", "colate", "orentated", "unequivocaly", "generataed", "guidlines"));
//    private static Set<String> printWords = new HashSet<String>(Arrays.asList("reciet", "adres", "rember", "juse", "accesing"));
    private static Set<String> printWords = new HashSet<String>(Arrays.asList("thay", "cleark", "wer", "bonas", "plesent")); // 'they' (4939)
//    private static Set<String> printWords = new HashSet<String>(Arrays.asList("wonted", "planed", "forth", "et"));
//    private static Set<String> printWords = new HashSet<String>(Arrays.asList("where", "latter", "advice")); // 'later' (116) 'were' (452)
//    private static Set<String> printWords = new HashSet<String>(Arrays.asList("hown", "ther", "quies", "natior", "thear", "carrers")); // their (2955)
//    private static Set<String> printWords = new HashSet<String>(Arrays.asList("aranging", "sumarys", "aurgument", "humor", "oranisation", "oranised"));

    public static void main(String[] args) throws IOException {
        getWordFrequency();

        test(test1Words);
        test(test2Words);
    }

    public static void test(HashMap<String, String> testWordsMap) throws IOException {
        long start = System.currentTimeMillis();
        int correctCount = 0;
        int incorrectCount = 0;
        int unknownCount = 0;
        for (String expected : testWordsMap.keySet()) {
            // smoothing
//            if(frequencyMap.get(expected) == null) {
//                frequencyMap.put(expected, 1);
//            }

            String[] testWords = testWordsMap.get(expected).split(" ");
            for(String testWord : testWords) {
                String actual = correct(testWord);
                if(expected.equals(actual)) {
                    correctCount++;
                    //System.out.println("correct(" + testWord + ") => " + actual);
                } else  {
                    incorrectCount++;
                    if(printWords.contains(testWord)) {
                        System.out.println("correct(" + testWord + ") => " + actual + " (" + frequencyMap.get(actual) +
                                "); expected " + expected + " (" + frequencyMap.get(expected) + ")");
                    }
                }

                // unknown
                if(frequencyMap.get(expected) == null) {
                    unknownCount++;
                }
            }
        }
//        System.out.println("coat: " + frequencyMap.get("coat"));
        System.out.println("\ncorrect: " + correctCount + ", incorrect: " + incorrectCount + ", unknown: " + unknownCount + ", pct: " + ((double)correctCount/(correctCount + incorrectCount)) +
                ", Time: " + ((System.currentTimeMillis() - start) / (1000)) + " seconds\n");
    }

    // P(c) [language model] - get frequency of all alphabetic words (converted to lowercase)
    public static void getWordFrequency() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(WORDS_FILE));
        String line;

        while ((line = br.readLine()) != null) {
            // read words
            Scanner scanner = new Scanner(line.toLowerCase()).useDelimiter(WORD_DELIM);
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
        addEdit1Candidates(word, candidates1, false);
        String bestCandidate = returnBestCandidate(candidates1);
        if (bestCandidate != null) {
            return bestCandidate;
        }

        // edit distance 2 candidates: just apply edit1 to all the results of edit1
        HashSet<String> candidates2 = new HashSet<String>();
        for (String candidate : candidates1) {
            addEdit1Candidates(candidate, candidates2, true);
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
            if (candidateFrequency != null && candidateFrequency > max) {
                max = candidateFrequency;
                bestCandidate = candidate;
            }
        }
        return bestCandidate;
    }

    // valid words at edit distance 1
    public static void addEdit1Candidates(String word, HashSet<String> candidates, boolean keepOnlyKnown) {
        int length = word.length();

        // deletion (remove one letter)
        for (int i = 0; i < length; i++) {
            String editedWord = word.substring(0, i) + word.substring(i + 1, length);
            if (!keepOnlyKnown || frequencyMap.get(editedWord) != null) {
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
            if (!keepOnlyKnown || frequencyMap.get(editedWord) != null) {
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
                if (!keepOnlyKnown || frequencyMap.get(editedWord) != null) {
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
                if (!keepOnlyKnown || frequencyMap.get(editedWord) != null) {
                    candidates.add(editedWord);
                }
            }
        }
    }

    static {
        test1Words.put("access", "acess");
        test1Words.put("accessing", "accesing");
        test1Words.put("accommodation", "accomodation acommodation acomodation");
        test1Words.put("account", "acount");
        test1Words.put("address", "adress adres");
        test1Words.put("addressable", "addresable");
        test1Words.put("arranged", "aranged arrainged");
        test1Words.put("arrangeing", "aranging");
        test1Words.put("arrangement", "arragment");
        test1Words.put("articles", "articals");
        test1Words.put("aunt", "annt anut arnt");
        test1Words.put("auxiliary", "auxillary");
        test1Words.put("available", "avaible");
        test1Words.put("awful", "awfall afful");
        test1Words.put("basically", "basicaly");
        test1Words.put("beginning", "begining");
        test1Words.put("benefit", "benifit");
        test1Words.put("benefits", "benifits");
        test1Words.put("between", "beetween");
        test1Words.put("bicycle", "bicycal bycicle bycycle");
        test1Words.put("biscuits", "biscits biscutes biscuts bisquits buiscits buiscuts");
        test1Words.put("built", "biult");
        test1Words.put("cake", "cak");
        test1Words.put("career", "carrer");
        test1Words.put("cemetery", "cemetary semetary");
        test1Words.put("centrally", "centraly");
        test1Words.put("certain", "cirtain");
        test1Words.put("challenges", "chalenges chalenges");
        test1Words.put("chapter", "chaper chaphter chaptur");
        test1Words.put("choice", "choise");
        test1Words.put("choosing", "chosing");
        test1Words.put("clerical", "clearical");
        test1Words.put("committee", "comittee");
        test1Words.put("compare", "compair");
        test1Words.put("completely", "completly");
        test1Words.put("consider", "concider");
        test1Words.put("considerable", "conciderable");
        test1Words.put("contented", "contenpted contende contended contentid");
        test1Words.put("curtains", "cartains certans courtens cuaritains curtans curtians curtions");
        test1Words.put("decide", "descide");
        test1Words.put("decided", "descided");
        test1Words.put("definitely", "definately difinately");
        test1Words.put("definition", "defenition");
        test1Words.put("definitions", "defenitions");
        test1Words.put("description", "discription");
        test1Words.put("desiccate", "desicate dessicate dessiccate");
        test1Words.put("diagrammatically", "diagrammaticaally");
        test1Words.put("different", "diffrent");
        test1Words.put("driven", "dirven");
        test1Words.put("ecstasy", "exstacy ecstacy");
        test1Words.put("embarrass", "embaras embarass");
        test1Words.put("establishing", "astablishing establising");
        test1Words.put("experience", "experance experiance");
        test1Words.put("experiences", "experances");
        test1Words.put("extended", "extented");
        test1Words.put("extremely", "extreamly");
        test1Words.put("fails", "failes");
        test1Words.put("families", "familes");
        test1Words.put("february", "febuary");
        test1Words.put("further", "futher");
        test1Words.put("gallery", "galery gallary gallerry gallrey");
        test1Words.put("hierarchal", "hierachial");
        test1Words.put("hierarchy", "hierchy");
        test1Words.put("inconvenient", "inconvienient inconvient inconvinient");
        test1Words.put("independent", "independant independant");
        test1Words.put("initial", "intial");
        test1Words.put("initials", "inetials inistals initails initals intials");
        test1Words.put("juice", "guic juce jucie juise juse");
        test1Words.put("latest", "lates latets latiest latist");
        test1Words.put("laugh", "lagh lauf laught lugh");
        test1Words.put("level", "leval");
        test1Words.put("levels", "levals");
        test1Words.put("liaison", "liaision liason");
        test1Words.put("lieu", "liew");
        test1Words.put("literature", "litriture");
        test1Words.put("loans", "lones");
        test1Words.put("locally", "localy");
        test1Words.put("magnificent", "magnificnet magificent magnifcent magnifecent magnifiscant magnifisent magnificant");
        test1Words.put("management", "managment");
        test1Words.put("meant", "ment");
        test1Words.put("minuscule", "miniscule");
        test1Words.put("minutes", "muinets");
        test1Words.put("monitoring", "monitering");
        test1Words.put("necessary", "neccesary necesary neccesary necassary necassery neccasary");
        test1Words.put("occurrence", "occurence occurence");
        test1Words.put("often", "ofen offen offten ofton");
        test1Words.put("opposite", "opisite oppasite oppesite oppisit oppisite opposit oppossite oppossitte");
        test1Words.put("parallel", "paralel paralell parrallel parralell parrallell");
        test1Words.put("particular", "particulaur");
        test1Words.put("perhaps", "perhapse");
        test1Words.put("personnel", "personnell");
        test1Words.put("planned", "planed");
        test1Words.put("poem", "poame");
        test1Words.put("poems", "poims pomes");
        test1Words.put("poetry", "poartry poertry poetre poety powetry");
        test1Words.put("position", "possition");
        test1Words.put("possible", "possable");
        test1Words.put("pretend", "pertend protend prtend pritend");
        test1Words.put("problem", "problam proble promblem proplen");
        test1Words.put("pronunciation", "pronounciation");
        test1Words.put("purple", "perple perpul poarple");
        test1Words.put("questionnaire", "questionaire");
        test1Words.put("really", "realy relley relly");
        test1Words.put("receipt", "receit receite reciet recipt");
        test1Words.put("receive", "recieve");
        test1Words.put("refreshment", "reafreshment refreshmant refresment refressmunt");
        test1Words.put("remember", "rember remeber rememmer rermember");
        test1Words.put("remind", "remine remined");
        test1Words.put("scarcely", "scarcly scarecly scarely scarsely");
        test1Words.put("scissors", "scisors sissors");
        test1Words.put("separate", "seperate");
        test1Words.put("singular", "singulaur");
        test1Words.put("someone", "somone");
        test1Words.put("sources", "sorces");
        test1Words.put("southern", "southen");
        test1Words.put("special", "speaical specail specal speical");
        test1Words.put("splendid", "spledid splended splened splended");
        test1Words.put("standardizing", "stanerdizing");
        test1Words.put("stomach", "stomac stomache stomec stumache");
        test1Words.put("supersede", "supercede superceed");
        test1Words.put("there", "ther");
        test1Words.put("totally", "totaly");
        test1Words.put("transferred", "transfred");
        test1Words.put("transportability", "transportibility");
        test1Words.put("triangular", "triangulaur");
        test1Words.put("understand", "undersand undistand");
        test1Words.put("unexpected", "unexpcted unexpeted unexspected");
        test1Words.put("unfortunately", "unfortunatly");
        test1Words.put("unique", "uneque");
        test1Words.put("useful", "usefull");
        test1Words.put("valuable", "valubale valuble");
        test1Words.put("variable", "varable");
        test1Words.put("variant", "vairiant");
        test1Words.put("various", "vairious");
        test1Words.put("visited", "fisited viseted vistid vistied");
        test1Words.put("visitors", "vistors");
        test1Words.put("voluntary", "volantry");
        test1Words.put("voting", "voteing");
        test1Words.put("wanted", "wantid wonted");
        test1Words.put("whether", "wether");
        test1Words.put("wrote", "rote wote");

        test2Words.put("forbidden", "forbiden");
        test2Words.put("decisions", "deciscions descisions");
        test2Words.put("supposedly", "supposidly");
        test2Words.put("embellishing", "embelishing");
        test2Words.put("technique", "tecnique");
        test2Words.put("permanently", "perminantly");
        test2Words.put("confirmation", "confermation");
        test2Words.put("appointment", "appoitment");
        test2Words.put("progression", "progresion");
        test2Words.put("accompanying", "acompaning");
        test2Words.put("applicable", "aplicable");
        test2Words.put("regained", "regined");
        test2Words.put("guidelines", "guidlines");
        test2Words.put("surrounding", "serounding");
        test2Words.put("titles", "tittles");
        test2Words.put("unavailable", "unavailble");
        test2Words.put("advantageous", "advantageos");
        test2Words.put("brief", "brif");
        test2Words.put("appeal", "apeal");
        test2Words.put("consisting", "consisiting");
        test2Words.put("clerk", "cleark clerck");
        test2Words.put("component", "componant");
        test2Words.put("favourable", "faverable");
        test2Words.put("separation", "seperation");
        test2Words.put("search", "serch");
        test2Words.put("receive", "recieve");
        test2Words.put("employees", "emploies");
        test2Words.put("prior", "piror");
        test2Words.put("resulting", "reulting");
        test2Words.put("suggestion", "sugestion");
        test2Words.put("opinion", "oppinion");
        test2Words.put("cancellation", "cancelation");
        test2Words.put("criticism", "citisum");
        test2Words.put("useful", "usful");
        test2Words.put("humour", "humor");
        test2Words.put("anomalies", "anomolies");
        test2Words.put("would", "whould");
        test2Words.put("doubt", "doupt");
        test2Words.put("examination", "eximination");
        test2Words.put("therefore", "therefoe");
        test2Words.put("recommend", "recomend");
        test2Words.put("separated", "seperated");
        test2Words.put("successful", "sucssuful succesful");
        test2Words.put("apparent", "apparant");
        test2Words.put("occurred", "occureed");
        test2Words.put("particular", "paerticulaur");
        test2Words.put("pivoting", "pivting");
        test2Words.put("announcing", "anouncing");
        test2Words.put("challenge", "chalange");
        test2Words.put("arrangements", "araingements");
        test2Words.put("proportions", "proprtions");
        test2Words.put("organized", "oranised");
        test2Words.put("accept", "acept");
        test2Words.put("dependence", "dependance");
        test2Words.put("unequalled", "unequaled");
        test2Words.put("numbers", "numbuers");
        test2Words.put("sense", "sence");
        test2Words.put("conversely", "conversly");
        test2Words.put("provide", "provid");
        test2Words.put("arrangement", "arrangment");
        test2Words.put("responsibilities", "responsiblities");
        test2Words.put("fourth", "forth");
        test2Words.put("ordinary", "ordenary");
        test2Words.put("description", "desription descvription desacription");
        test2Words.put("inconceivable", "inconcievable");
        test2Words.put("data", "dsata");
        test2Words.put("register", "rgister");
        test2Words.put("supervision", "supervison");
        test2Words.put("encompassing", "encompasing");
        test2Words.put("negligible", "negligable");
        test2Words.put("allow", "alow");
        test2Words.put("operations", "operatins");
        test2Words.put("executed", "executted");
        test2Words.put("interpretation", "interpritation");
        test2Words.put("hierarchy", "heiarky");
        test2Words.put("indeed", "indead");
        test2Words.put("years", "yesars");
        test2Words.put("through", "throut");
        test2Words.put("committee", "committe");
        test2Words.put("inquiries", "equiries");
        test2Words.put("before", "befor");
        test2Words.put("continued", "contuned");
        test2Words.put("permanent", "perminant");
        test2Words.put("choose", "chose");
        test2Words.put("virtually", "vertually");
        test2Words.put("correspondence", "correspondance");
        test2Words.put("eventually", "eventully");
        test2Words.put("lonely", "lonley");
        test2Words.put("profession", "preffeson");
        test2Words.put("they", "thay");
        test2Words.put("now", "noe");
        test2Words.put("desperately", "despratly");
        test2Words.put("university", "unversity");
        test2Words.put("adjournment", "adjurnment");
        test2Words.put("possibilities", "possablities");
        test2Words.put("stopped", "stoped");
        test2Words.put("mean", "meen");
        test2Words.put("weighted", "wagted");
        test2Words.put("adequately", "adequattly");
        test2Words.put("shown", "hown");
        test2Words.put("matrix", "matriiix");
        test2Words.put("profit", "proffit");
        test2Words.put("encourage", "encorage");
        test2Words.put("collate", "colate");
        test2Words.put("disaggregate", "disaggreagte disaggreaget");
        test2Words.put("receiving", "recieving reciving");
        test2Words.put("proviso", "provisoe");
        test2Words.put("umbrella", "umberalla");
        test2Words.put("approached", "aproached");
        test2Words.put("pleasant", "plesent");
        test2Words.put("difficulty", "dificulty");
        test2Words.put("appointments", "apointments");
        test2Words.put("base", "basse");
        test2Words.put("conditioning", "conditining");
        test2Words.put("earliest", "earlyest");
        test2Words.put("beginning", "begining");
        test2Words.put("universally", "universaly");
        test2Words.put("unresolved", "unresloved");
        test2Words.put("length", "lengh");
        test2Words.put("exponentially", "exponentualy");
        test2Words.put("utilized", "utalised");
        test2Words.put("set", "et");
        test2Words.put("surveys", "servays");
        test2Words.put("families", "familys");
        test2Words.put("system", "sysem");
        test2Words.put("approximately", "aproximatly");
        test2Words.put("their", "ther");
        test2Words.put("scheme", "scheem");
        test2Words.put("speaking", "speeking");
        test2Words.put("repetitive", "repetative");
        test2Words.put("inefficient", "ineffiect");
        test2Words.put("geneva", "geniva");
        test2Words.put("exactly", "exsactly");
        test2Words.put("immediate", "imediate");
        test2Words.put("appreciation", "apreciation");
        test2Words.put("luckily", "luckeley");
        test2Words.put("eliminated", "elimiated");
        test2Words.put("believe", "belive");
        test2Words.put("appreciated", "apreciated");
        test2Words.put("readjusted", "reajusted");
        test2Words.put("were", "wer where");
        test2Words.put("feeling", "fealing");
        test2Words.put("and", "anf");
        test2Words.put("false", "faulse");
        test2Words.put("seen", "seeen");
        test2Words.put("interrogating", "interogationg");
        test2Words.put("academically", "academicly");
        test2Words.put("relatively", "relativly relitivly");
        test2Words.put("traditionally", "traditionaly");
        test2Words.put("studying", "studing");
        test2Words.put("majority", "majorty");
        test2Words.put("build", "biuld");
        test2Words.put("aggravating", "agravating");
        test2Words.put("transactions", "trasactions");
        test2Words.put("arguing", "aurguing");
        test2Words.put("sheets", "sheertes");
        test2Words.put("successive", "sucsesive sucessive");
        test2Words.put("segment", "segemnt");
        test2Words.put("especially", "especaily");
        test2Words.put("later", "latter");
        test2Words.put("senior", "sienior");
        test2Words.put("dragged", "draged");
        test2Words.put("atmosphere", "atmospher");
        test2Words.put("drastically", "drasticaly");
        test2Words.put("particularly", "particulary");
        test2Words.put("visitor", "vistor");
        test2Words.put("session", "sesion");
        test2Words.put("continually", "contually");
        test2Words.put("availability", "avaiblity");
        test2Words.put("busy", "buisy");
        test2Words.put("parameters", "perametres");
        test2Words.put("surroundings", "suroundings seroundings");
        test2Words.put("employed", "emploied");
        test2Words.put("adequate", "adiquate");
        test2Words.put("handle", "handel");
        test2Words.put("means", "meens");
        test2Words.put("familiar", "familer");
        test2Words.put("between", "beeteen");
        test2Words.put("overall", "overal");
        test2Words.put("timing", "timeing");
        test2Words.put("committees", "comittees commitees");
        test2Words.put("queries", "quies");
        test2Words.put("econometric", "economtric");
        test2Words.put("erroneous", "errounous");
        test2Words.put("decides", "descides");
        test2Words.put("reference", "refereence refference");
        test2Words.put("intelligence", "inteligence");
        test2Words.put("edition", "ediion ediition");
        test2Words.put("are", "arte");
        test2Words.put("apologies", "appologies");
        test2Words.put("thermawear", "thermawere thermawhere");
        test2Words.put("techniques", "tecniques");
        test2Words.put("voluntary", "volantary");
        test2Words.put("subsequent", "subsequant subsiquent");
        test2Words.put("currently", "curruntly");
        test2Words.put("forecast", "forcast");
        test2Words.put("weapons", "wepons");
        test2Words.put("routine", "rouint");
        test2Words.put("neither", "niether");
        test2Words.put("approach", "aproach");
        test2Words.put("available", "availble");
        test2Words.put("recently", "reciently");
        test2Words.put("ability", "ablity");
        test2Words.put("nature", "natior");
        test2Words.put("commercial", "comersial");
        test2Words.put("agencies", "agences");
        test2Words.put("however", "howeverr");
        test2Words.put("suggested", "sugested");
        test2Words.put("career", "carear");
        test2Words.put("many", "mony");
        test2Words.put("annual", "anual");
        test2Words.put("according", "acording");
        test2Words.put("receives", "recives recieves");
        test2Words.put("interesting", "intresting");
        test2Words.put("expense", "expence");
        test2Words.put("relevant", "relavent relevaant");
        test2Words.put("table", "tasble");
        test2Words.put("throughout", "throuout");
        test2Words.put("conference", "conferance");
        test2Words.put("sensible", "sensable");
        test2Words.put("described", "discribed describd");
        test2Words.put("union", "unioun");
        test2Words.put("interest", "intrest");
        test2Words.put("flexible", "flexable");
        test2Words.put("refered", "reffered");
        test2Words.put("controlled", "controled");
        test2Words.put("sufficient", "suficient");
        test2Words.put("dissension", "desention");
        test2Words.put("adaptable", "adabtable");
        test2Words.put("representative", "representitive");
        test2Words.put("irrelevant", "irrelavent");
        test2Words.put("unnecessarily", "unessasarily");
        test2Words.put("applied", "upplied");
        test2Words.put("apologised", "appologised");
        test2Words.put("these", "thees thess");
        test2Words.put("choices", "choises");
        test2Words.put("will", "wil");
        test2Words.put("procedure", "proceduer");
        test2Words.put("shortened", "shortend");
        test2Words.put("manually", "manualy");
        test2Words.put("disappointing", "dissapoiting");
        test2Words.put("excessively", "exessively");
        test2Words.put("comments", "coments");
        test2Words.put("containing", "containg");
        test2Words.put("develop", "develope");
        test2Words.put("credit", "creadit");
        test2Words.put("government", "goverment");
        test2Words.put("acquaintances", "aquantences");
        test2Words.put("orientated", "orentated");
        test2Words.put("widely", "widly");
        test2Words.put("advise", "advice");
        test2Words.put("difficult", "dificult");
        test2Words.put("investigated", "investegated");
        test2Words.put("bonus", "bonas");
        test2Words.put("conceived", "concieved");
        test2Words.put("nationally", "nationaly");
        test2Words.put("compared", "comppared compased");
        test2Words.put("moving", "moveing");
        test2Words.put("necessity", "nessesity");
        test2Words.put("opportunity", "oppertunity oppotunity opperttunity");
        test2Words.put("thoughts", "thorts");
        test2Words.put("equalled", "equaled");
        test2Words.put("variety", "variatry");
        test2Words.put("analysis", "analiss analsis analisis");
        test2Words.put("patterns", "pattarns");
        test2Words.put("qualities", "quaties");
        test2Words.put("easily", "easyly");
        test2Words.put("organization", "oranisation oragnisation");
        test2Words.put("the", "thw hte thi");
        test2Words.put("corporate", "corparate");
        test2Words.put("composed", "compossed");
        test2Words.put("enormously", "enomosly");
        test2Words.put("financially", "financialy");
        test2Words.put("functionally", "functionaly");
        test2Words.put("discipline", "disiplin");
        test2Words.put("announcement", "anouncement");
        test2Words.put("progresses", "progressess");
        test2Words.put("except", "excxept");
        test2Words.put("recommending", "recomending");
        test2Words.put("mathematically", "mathematicaly");
        test2Words.put("source", "sorce");
        test2Words.put("combine", "comibine");
        test2Words.put("input", "inut");
        test2Words.put("careers", "currers carrers");
        test2Words.put("resolved", "resoved");
        test2Words.put("demands", "diemands");
        test2Words.put("unequivocally", "unequivocaly");
        test2Words.put("suffering", "suufering");
        test2Words.put("immediately", "imidatly imediatly");
        test2Words.put("accepted", "acepted");
        test2Words.put("projects", "projeccts");
        test2Words.put("necessary", "necasery nessasary nessisary neccassary");
        test2Words.put("journalism", "journaism");
        test2Words.put("unnecessary", "unessessay");
        test2Words.put("night", "nite");
        test2Words.put("output", "oputput");
        test2Words.put("security", "seurity");
        test2Words.put("essential", "esential");
        test2Words.put("beneficial", "benificial benficial");
        test2Words.put("explaining", "explaning");
        test2Words.put("supplementary", "suplementary");
        test2Words.put("questionnaire", "questionare");
        test2Words.put("employment", "empolyment");
        test2Words.put("proceeding", "proceding");
        test2Words.put("decision", "descisions descision");
        test2Words.put("per", "pere");
        test2Words.put("discretion", "discresion");
        test2Words.put("reaching", "reching");
        test2Words.put("analysed", "analised");
        test2Words.put("expansion", "expanion");
        test2Words.put("although", "athough");
        test2Words.put("subtract", "subtrcat");
        test2Words.put("analysing", "aalysing");
        test2Words.put("comparison", "comparrison");
        test2Words.put("months", "monthes");
        test2Words.put("hierarchal", "hierachial");
        test2Words.put("misleading", "missleading");
        test2Words.put("commit", "comit");
        test2Words.put("auguments", "aurgument");
        test2Words.put("within", "withing");
        test2Words.put("obtaining", "optaning");
        test2Words.put("accounts", "acounts");
        test2Words.put("primarily", "pimarily");
        test2Words.put("operator", "opertor");
        test2Words.put("accumulated", "acumulated");
        test2Words.put("extremely", "extreemly");
        test2Words.put("there", "thear");
        test2Words.put("summarys", "sumarys");
        test2Words.put("analyse", "analiss");
        test2Words.put("understandable", "understadable");
        test2Words.put("safeguard", "safegaurd");
        test2Words.put("consist", "consisit");
        test2Words.put("declarations", "declaratrions");
        test2Words.put("minutes", "muinutes muiuets");
        test2Words.put("associated", "assosiated");
        test2Words.put("accessibility", "accessability");
        test2Words.put("examine", "examin");
        test2Words.put("surveying", "servaying");
        test2Words.put("politics", "polatics");
        test2Words.put("annoying", "anoying");
        test2Words.put("again", "agiin");
        test2Words.put("assessing", "accesing");
        test2Words.put("ideally", "idealy");
        test2Words.put("scrutinized", "scrutiniesed");
        test2Words.put("simular", "similar");
        test2Words.put("personnel", "personel");
        test2Words.put("whereas", "wheras");
        test2Words.put("when", "whn");
        test2Words.put("geographically", "goegraphicaly");
        test2Words.put("gaining", "ganing");
        test2Words.put("requested", "rquested");
        test2Words.put("separate", "seporate");
        test2Words.put("students", "studens");
        test2Words.put("prepared", "prepaired");
        test2Words.put("generated", "generataed");
        test2Words.put("graphically", "graphicaly");
        test2Words.put("suited", "suted");
        test2Words.put("variable", "varible vaiable");
        test2Words.put("building", "biulding");
        test2Words.put("required", "reequired");
        test2Words.put("necessitates", "nessisitates");
        test2Words.put("together", "togehter");
        test2Words.put("profits", "proffits");
    }

}


