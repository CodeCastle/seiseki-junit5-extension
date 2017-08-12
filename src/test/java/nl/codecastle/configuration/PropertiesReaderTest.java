package nl.codecastle.configuration;

import nl.codecastle.exceptions.SeisekiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class PropertiesReaderTest {

    private static final String DEFAULT_PROJECT_NAME = "default";
    private static final String PROJECT_NAME_KEY = "project.name";
    private static final String SERVER_ENDPOINT_KEY = "server.endpoint";
    private PropertiesReader propertiesReader;
    private ClassLoader classLoader = getClass().getClassLoader();

    @BeforeEach
    public void testSetup() throws SeisekiException {

    }


    @Test
    public void shouldReadDefaultValuesIfProjectValuesAreMissing() {

        propertiesReader = new PropertiesReader("incomplete.seiseki.properties");
        assertThat(propertiesReader.getValue(PROJECT_NAME_KEY)).isEqualTo(DEFAULT_PROJECT_NAME);
        assertThat(propertiesReader.getValue(SERVER_ENDPOINT_KEY)).isEqualTo("http://localhost:8080");
    }

    @Test
    public void shouldReadDefaultValuesIfPropertiesFileIsMissing() {

        propertiesReader = new PropertiesReader("unexisting.seiseki.properties");
        assertThat(propertiesReader.getValue(PROJECT_NAME_KEY)).isEqualTo(DEFAULT_PROJECT_NAME);
        assertThat(propertiesReader.getValue(SERVER_ENDPOINT_KEY)).isEqualTo("http://localhost:8080");
    }

    @Test
    public void shouldReadValuesFromProjectPropertiesWhenPresent() {
        propertiesReader = new PropertiesReader("seiseki.properties");
        assertThat(propertiesReader.getValue(PROJECT_NAME_KEY)).isEqualTo("keshiki");
        assertThat(propertiesReader.getValue(SERVER_ENDPOINT_KEY)).isEqualTo("http://localhost:8080/api");
    }

}
