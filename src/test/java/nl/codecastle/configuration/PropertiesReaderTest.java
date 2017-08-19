package nl.codecastle.configuration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class PropertiesReaderTest {

    private static final String DEFAULT_PROJECT_NAME = "default";
    private static final String PROJECT_NAME_KEY = "project.name";
    private static final String SERVER_ENDPOINT_KEY = "server.endpoint";
    private PropertiesReader propertiesReader;

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
        assertThat(propertiesReader.getValue(PROJECT_NAME_KEY)).isEqualTo("test");
        assertThat(propertiesReader.getValue(SERVER_ENDPOINT_KEY)).isEqualTo("http://localhost:8383/api");
    }

}
