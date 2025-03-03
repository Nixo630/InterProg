package gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private static int count(String str, char motif) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == motif) {
                count++;
            }
        }
        return count;
    }

    private static List<String> parseSpeech(String input) {
        String[] names = input.strip().split(" ");
        List<String> result = new ArrayList<>();
        String initials = names[names.length-1];

        for(int i = 0; i < names.length-1; i+=2) {
            result.add(names[i]+" "+names[i+1]);
        }

        if (!initials.contains("-")) {
            result.add(initials);
        }

        return result;
    }

    private static List<String> parseRolesInServices(String input) {
        List<String> result = new ArrayList<>();

        String regex = "(Accueil[\\s]*\\S+|\\S+)";
        Pattern pattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String tmp = matcher.group(1);
            tmp = tmp.toLowerCase().replace("accueil", "accueil ");
            tmp = tmp.toLowerCase().replace("  "," ");
            result.add(tmp.trim());
        }

        return result;
    }

    private static List<String> VCMParser(String fileContent) throws IOException {
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(fileContent))) {
            String line;
            String date = "";

            while ((line = reader.readLine()) != null) {
                if (line.matches("\\d{4}/\\d{2}/\\d{2} \\| .*")) {
                    date = line.substring(0, 10);
                }

                Pattern pattern = Pattern.compile("^(\\d{1,2}:\\d{2})\\s+(cantique\\s+\\d+|\\d+\\.|\\S+\\s+\\S+introduction)\\s+.*\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)$",Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String time = matcher.group(1);
                    String point = matcher.group(2);
                    if (point.toLowerCase().contains("introduction")) {
                        point = "president";
                    }
                    if (matcher.group(3).contains("&")) {
                        //donc si c'est un sujet de l'ecole theocratique
                        //alors 2 personnes ont des roles
                        Pattern pattern2 = Pattern.compile("^(\\d{1,2}:\\d{2})\\s+(cantique\\s+\\d+|\\d+\\.|\\S+\\s+\\S+introduction)\\s+.*\\s+(\\S+)\\s+(\\S+)\\s+&\\s+(\\S+)\\s+(\\S+)$",Pattern.CASE_INSENSITIVE);
                        Matcher matcher2 = pattern2.matcher(line);
                        if (matcher2.find()) {
                            result.add(date + "_" + point + "_" + matcher2.group(3) + " " + matcher2.group(4));
                        }
                    }
                    String name = matcher.group(4);
                    String surname = matcher.group(5);

                    result.add(date + "_" + point + "_" + name + " " + surname);
                }
            }
        }
        return result;
    }

    private static List<String> servicesParser(String fileContent) throws IOException {
        Properties properties = Settings.loadProperties();
        String[] roleServices = properties.getProperty("roles3").split(Settings.ROLES_SEPARATOR);

        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(fileContent))) {
            String line;
            String date = "";
            String day = "";
            boolean inDate = false;
            List<String> roles = new ArrayList<>();
            int count = 1;//car le premier est DATE
            boolean firstMicro = false;

            while ((line = reader.readLine()) != null) {
                if (line.matches("\\s*(samedi|jeudi|mardi)\\s*$")) {
                    day = line;
                    date = reader.readLine().strip();;
                    if (!date.contains("/")) {
                        date = reader.readLine().strip();;
                    }
                    inDate = true;
                    firstMicro = false;
                    count = 1;//car le premier est DATE
                    continue;//on passe au suivant pour commencer a prendre les noms
                }
                else if (line.toLowerCase().contains("date") || line.toLowerCase().contains("accueil") || line.toLowerCase().contains("hall") || line.toLowerCase().contains("salle")) {
                    String txt = "";
                    while (!line.matches("\\s*(samedi|jeudi|mardi)\\s*$")) {
                        txt += line;
                        line = reader.readLine();
                    }
                    day = line;
                    date = reader.readLine().strip();
                    if (!date.contains("/")) {
                        date = reader.readLine().strip();;
                    }
                    inDate = true;
                    roles = parseRolesInServices(txt);

                    if (roleServices.length != 0 && roleServices[0] != "disactivate") {
                        roles = new ArrayList<>();
                        for (String role : roleServices) {
                            roles.add(role);
                        }
                    }
                    continue;//on passe au suivant pour commencer a prendre les noms
                }

                if (inDate) {
                    String[] names = line.split(" ");
                    int total = names.length;
                    String nextName = null;
                    if (total % 2 == 1) {
                        nextName = reader.readLine();
                        total += 1;
                    }
                    String[] parsedNames = new String[total/2];//total forcement pair
                    for (int i = 0; i < parsedNames.length; i++) {
                        parsedNames[i] = "";
                    }
                    for (int i = 0; i < names.length; i++) {
                        parsedNames[i/2] += names[i];
                        if (i % 2 == 0) {
                            parsedNames[i/2] += " ";
                        }
                    }
                    if (total != names.length) {
                        parsedNames[parsedNames.length-1] += nextName;
                    }
                    for (int i = 0; i < parsedNames.length; i++) {
                        if (count < roles.size()) {
                            result.add(date+"_"+roles.get(count)+"_"+parsedNames[i]);
                            if (firstMicro || (!roles.get(count).toLowerCase().contains("micro") && !roles.get(count).toLowerCase().contains("perche") && !roles.get(count).toLowerCase().contains("perchiste"))) {
                                count += 1;
                            }
                            else if (roles.get(count).toLowerCase().contains("micro") || roles.get(count).toLowerCase().contains("perche") || roles.get(count).toLowerCase().contains("perchiste")) {
                                firstMicro = true;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private static List<String> parseSpeakerInfo(String fileContent) throws IOException {
        Properties properties = Settings.loadProperties();
        String[] roles = properties.getProperty("roles2").split(Settings.ROLES_SEPARATOR);

        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(fileContent))) {
            String line;
            boolean beginParse = false;
            String date = null;
            Pattern patternDash = Pattern.compile(".*-\\s*$",Pattern.CASE_INSENSITIVE);
            while ((line = reader.readLine()) != null) {
                Pattern pattern = Pattern.compile("\\s*(\\d{1,2}/\\d{1,2}).*$",Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    date = matcher.group(1);

                    Pattern pattern2 = Pattern.compile("\\s*(\\d{1,2}/\\d{1,2})\\s*-\\s*$",Pattern.CASE_INSENSITIVE);
                    Matcher matcher2 = pattern2.matcher(line);//donc si la ligne est vide et contient un -

                    if (!matcher2.find()) {//s'il n'y a pas '-' a la fin de la ligne et que la ligne n'est pas vide
                        Pattern pattern4 = Pattern.compile(".*\\?\\s*(\\S+.*)$",Pattern.CASE_INSENSITIVE);
                        Matcher matcher4 = pattern4.matcher(line);//s'il y a du texte apres le point d'interrogation
                        String namesAfterSpeech = "";

                        if (Parser.count(line,'?') >= 2) {//car parfois il y a 2 ? dans un titre de discours
                            Pattern pattern5 = Pattern.compile(".*\\?.*\\?\\s*(\\S+.*)$",Pattern.CASE_INSENSITIVE);
                            Matcher matcher5 = pattern5.matcher(line);

                            if (matcher5.find()) {//donc si du texte apres le second ?
                                namesAfterSpeech = matcher5.group(1);//le texte apres le second ?

                                List<String> tmp = parseSpeech(namesAfterSpeech);
                                int count = 0;
                                String role = "";
                                for (String item : tmp) {
                                    if (count == 0) {
                                        if (roles.length != 0 && roles[0] != "disactivate") {
                                            role = roles[count];
                                        }
                                        else {
                                            role = "orateur";
                                        }
                                    }
                                    else if (count == 1) {
                                        if (roles.length != 0 && roles[0] != "disactivate") {
                                            role = roles[count];
                                        }
                                        else {
                                            role = "president";
                                        }
                                    }
                                    else if (count == 2) {
                                        if (roles.length != 0 && roles[0] != "disactivate") {
                                            role = roles[count];
                                        }
                                        else {
                                            role = "lecteur";
                                        }
                                    }
                                    else {
                                        if (roles.length != 0 && roles[0] != "disactivate") {
                                            role = roles[count];
                                        }
                                        else {
                                            role = "priere";
                                        }
                                    }
                                    result.add(date+"_"+role+"_"+item);
                                    count += 1;
                                }

                                beginParse = false;
                                continue;
                            }
                        }
                        else if (matcher4.find()) {
                            namesAfterSpeech = matcher4.group(1);

                            List<String> tmp = parseSpeech(namesAfterSpeech);
                            int count = 0;
                            String role = "";
                            for (String item : tmp) {
                                if (count == 0) {
                                    if (roles.length != 0 && roles[0] != "disactivate") {
                                        role = roles[count];
                                    }
                                    else {
                                        role = "orateur";
                                    }
                                }
                                else if (count == 1) {
                                    if (roles.length != 0 && roles[0] != "disactivate") {
                                        role = roles[count];
                                    }
                                    else {
                                        role = "president";
                                    }
                                }
                                else if (count == 2) {
                                    if (roles.length != 0 && roles[0] != "disactivate") {
                                        role = roles[count];
                                    }
                                    else {
                                        role = "lecteur";
                                    }
                                }
                                else {
                                    if (roles.length != 0 && roles[0] != "disactivate") {
                                        role = roles[count];
                                    }
                                    else {
                                        role = "priere";
                                    }
                                }
                                result.add(date+"_"+role+"_"+item);
                                count += 1;
                            }

                            beginParse = false;
                            continue;
                        }

                        beginParse = true;
                        continue;
                    }
                    else {
                        beginParse = false;
                    }
                }
                else if (beginParse) {
                    //nom orateur
                    String name = "";
                    int j = 0;
                    while(line.charAt(j) != '(') {
                        name += line.charAt(j);
                        j += 1;
                        if (j >= line.length()) {
                            line = reader.readLine();
                            j = 0;
                        }
                    }
                    if (roles.length != 0 && roles[0] != "disactivate") {
                        result.add(date+"_"+roles[0]+"_"+name);
                    }
                    else {
                        result.add(date+"_"+"orateur"+"_"+name);
                    }
                    while(!line.contains(")")) {
                        line = reader.readLine();
                    }
                    line = reader.readLine();
                    //autres noms pour les autres roles
                    String[] names = line.split(" ");
                    int total = names.length;
                    String initials = null;
                    if (total != 5 && !patternDash.matcher(line).find()) {
                        initials = reader.readLine();
                        total += 1;
                    }
                    String[] parsedNames = new String[total/2+1];
                    for (int i = 0; i < parsedNames.length; i++) {
                        parsedNames[i] = "";
                    }
                    for (int i = 0; i < names.length; i++) {
                        parsedNames[i/2] += names[i];
                        if (i % 2 == 0) {
                            parsedNames[i/2] += " ";
                        }
                    }
                    if (total != names.length) {
                        parsedNames[parsedNames.length-1] += initials;
                    }

                    String role = "";
                    for (int i = 0; i < parsedNames.length; i++) {
                        if (!patternDash.matcher(parsedNames[i]).find()) {//si ce nom n'est pas une ligne vide (donc tiret -)
                            if (i == 0) {
                                role = "president";
                            }
                            else if (i == 1) {
                                role = "lecteur";
                            }
                            else {
                                role = "priere";
                            }
                            if (roles.length != 0 && roles[0] != "disactivate") {
                                role = roles[i+1];//car orateur est deja pris
                            }
                            result.add(date+"_"+role+"_"+parsedNames[i]);
                        }
                    }
                    beginParse = false;
                }
            }
        }
        return result;
    }

    public static List<String> parseAll(String texte, int fileToParse) {
        Properties properties = Settings.loadProperties();
        String[] prisoxExceptions = properties.getProperty("exceptions").split(Settings.EXCEPTIONS_SEPARATOR);

        for (int i = 0; i < prisoxExceptions.length; i++) {
            String[] splited = prisoxExceptions[i].split(" ");
            String regex = "("+splited[0]+"[\\s|\\n]*"+splited[1]+")";//"(priso[\\s|\\n]*priso)"
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(texte);
            while (matcher.find()) {
                String prisoxException = matcher.group(1).toLowerCase();
                texte = texte.toLowerCase().replace(prisoxException, prisoxExceptions[i].replace(" ","-"));
            }
        }

        try {
            if (fileToParse == 0) {
                List<String> parsedVCM = VCMParser(texte);
                for (String item : parsedVCM) {
                    System.out.println(item);
                }
                return parsedVCM;
            }
            else if (fileToParse == 1) {
                List<String> speakersInfo = parseSpeakerInfo(texte);
                for (String item : speakersInfo) {
                    System.out.println(item);
                }
                return speakersInfo;
            }
            else if (fileToParse == 2) {
                texte = texte.toLowerCase().replace("  "," ");
                texte = texte.toLowerCase().replace("   "," ");
                texte = texte.toLowerCase().replace("    "," ");

                List<String> parsedServices = servicesParser(texte);
                for (String item : parsedServices) {
                    System.out.println(item);
                }
                return parsedServices;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}