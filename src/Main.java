import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Main {
    public static List<String> langs = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Hello world!");

        langs.add("russian");
        langs.add("braz_por");
        langs.add("polish");
        langs.add("simp_chinese");
        langs.add("spanish");
        langs.add("french");
        langs.add("german");

        File output = new File("output");
        if(output.exists() && output.isDirectory()) {
            deleteDirectory(output);
        }

        Main main = new Main();
        main.run();


    }

    static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public static List<Mod> mods = new ArrayList<>();
    public Map<String, String> replaceLocales = new HashMap<>();

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public void run() {

        File modDirectory = new File("C:\\Program Files (x86)\\Steam\\steamapps\\workshop\\content\\281990");

        for (String lang : langs) {
            mods.clear();
            replaceLocales.clear();

            Map ds = fetchPlayset();



            ds.forEach((k,v) -> {
                System.out.println(k + " " + v);
            });

            sortByValue(connect()).entrySet().forEach(steamId -> {
                File file = new File(modDirectory + "\\" + steamId.getKey());
                if (file.exists() && file.isDirectory()) {
                    try {
                        this.processMod(file, lang);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    System.out.println("Not found " + steamId.getKey());
                }
            });

            File outputDir = new File("output\\" + lang);

            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            if (replaceLocales.size() != 0) {

                File outputDirReplace = new File("output\\" + lang + "\\replace");

                if (!outputDirReplace.exists()) {
                    outputDirReplace.mkdirs();
                }

                StringBuilder sb = new StringBuilder();
                String ls = System.getProperty("line.separator");

                sb.append("l_" + lang + ":");
                sb.append(ls);
                sb.append(ls);

                for (Map.Entry<String, String> entry : replaceLocales.entrySet()) {
                    sb.append("    ");
                    sb.append(entry.getKey())
                            .append(" ")
                            .append(entry.getValue().replace("\\ n", "\\n"));
                    sb.append(ls);
                }

                sb.append(ls);

                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(outputDirReplace.getAbsolutePath() + "\\mass_replace_l_" + lang + ".yml"));

                    writer.write("\ufeff");
                    writer.write(sb.toString());

                    writer.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            mods.forEach(mod -> {
                int localSize = mod.getLocales().keySet().size();
                System.out.println(mod.getName() + ": " + localSize);

                if (localSize != 0) {

                    StringBuilder sb = new StringBuilder();
                    String ls = System.getProperty("line.separator");

                    sb.append("l_" + lang + ":");
                    sb.append(ls);
                    sb.append(ls);

                    for (Map.Entry<String, String> entry : mod.getLocales().entrySet()) {
                        sb.append("    ");
                        sb.append(entry.getKey())
                                .append(" ")
                                .append(entry.getValue().replace("\\ n", "\\n"));
                        sb.append(ls);
                    }

                    sb.append(ls);

                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(outputDir.getAbsolutePath() + "\\" + mod.getName() + "_l_" + lang + ".yml"));

                        writer.write("\ufeff");
                        writer.write(sb.toString());

                        writer.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    private void processMod(File modDirectory, String locale) throws IOException {
        File localizationDirectory = new File(modDirectory.getAbsolutePath() + "\\localisation");

        if (localizationDirectory.exists()) {
            for (File file : localizationDirectory.listFiles()) {
                Mod mod = new Mod(modDirectory.getName(), modDirectory);

                processLocalization(mod, file, "english", false);
                processLocalization(mod, file, locale, false);

                mods.add(mod);
            }
        }

        localizationDirectory = new File(modDirectory.getAbsolutePath() + "\\localization");

        if (localizationDirectory.exists()) {
            for (File file : localizationDirectory.listFiles()) {
                Mod mod = new Mod(modDirectory.getName(), modDirectory);

                processLocalization(mod, file, "english", false);
                processLocalization(mod, file, locale, false);

                mods.add(mod);
            }
        }
    }

    public static final String regex =  "^(\\s+)?(\\S+:?\\d?+)(\\s+)?(\"[\\S ]+\")(\\s+)?$";
    public static final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);


    public static Map<String, String> fetchPlayset() {
        Connection conn = null;
        Map<String, String> mods = new HashMap<>();
        try {
            // db parameters
            String url = "jdbc:sqlite:C:\\Users\\Work\\Documents\\Paradox Interactive\\Stellaris\\launcher-v2.sqlite";
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name, id FROM playsets");
            while (rs.next()) {
                mods.put(rs.getString("id"), rs.getString("name"));
            }
            System.out.println("Connection to SQLite has been established.");

            return mods;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }

        return null;
    }

    public static Map<String, String> connect() {
        Connection conn = null;
        Map<String, String> mods = new HashMap<>();
        try {
            // db parameters
            String url = "jdbc:sqlite:C:\\Users\\Work\\Documents\\Paradox Interactive\\Stellaris\\launcher-v2.sqlite";
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT m.steamId, position FROM playsets_mods join mods m on m.id = playsets_mods.modId where playsetId = \"3149368e-e272-4af7-972c-a0a212fba9a8\"");
            while (rs.next()) {
                mods.put(rs.getString("steamId"), rs.getString("position"));
            }
            System.out.println("Connection to SQLite has been established.");

            return mods;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }

        return null;
    }

    private void processLocalization(Mod mod, File file, String local, boolean replace) throws IOException {
        if (file.isDirectory()) {
            if (file.getName().equals("replace")) {
                for (File file2 : file.listFiles()) {
                    this.processLocalization(mod, file2, local, true);
                }
            } else {
                for (File file2 : file.listFiles()) {
                    this.processLocalization(mod, file2, local, false);
                }
            }
        } else {

            String localization = null;

            if (file.getName().contains(local)) {
                localization = local;
//                System.out.println("Find localization " + local + ": " + file.getAbsolutePath());
            }

            if (localization != null) {
//                System.out.println(localization + ", " + file.getName());

                BufferedReader reader = new BufferedReader(new FileReader(file.getAbsoluteFile()));

                String line = null;

                while ((line = reader.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);

                    if (matcher.find()) {

                        String name = matcher.group(2);
                        String value = matcher.group(4);

                        if (replace) {
                            replaceLocales.put(name, value);
                        } else {
                            mod.getLocales().put(name, value);
                        }
                    }
                }

//        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                reader.close();
            }
        }
    }

}