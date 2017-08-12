package nl.codecastle.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads properties file from a given location.
 */
public class PropertiesReader {

    private final Properties properties;
    private final Properties defaultProperties;

    /**
     * Reads in the properties from the given path.
     * Also reads in the defaults properties from the "default.properties" file.
     *
     * @param filePath the path of the project properties file
     */
    public PropertiesReader(String filePath) {

        properties = new Properties();
        defaultProperties = new Properties();

        try {
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream defaultPropertiesStream = classLoader.getResourceAsStream("default.properties");
            InputStream projectPropertiesStream = classLoader.getResourceAsStream(filePath);
            assert (defaultPropertiesStream != null);
            defaultProperties.load(defaultPropertiesStream);
            if (projectPropertiesStream != null) {
                properties.load(projectPropertiesStream);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the value for the given key from the project properties file.
     * If the file is missing it retrieves a value from the default properties.
     *
     * @param key name of the property needed
     * @return the value for the given key
     */
    public String getValue(String key) {
        if (properties.containsKey(key)) {
            return properties.getProperty(key);
        } else {
            return defaultProperties.getProperty(key);
        }
    }
}
