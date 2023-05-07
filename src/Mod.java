import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mod {

    public String name;
    public File fileDirectory;

    public Map<String, String> locales = new HashMap<>();


    public Mod(String name, File fileDirectory) {
        this.name = name;
        this.fileDirectory = fileDirectory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getLocales() {
        return locales;
    }

    public File getFileDirectory() {
        return fileDirectory;
    }
}
