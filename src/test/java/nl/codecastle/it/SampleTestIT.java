package nl.codecastle.it;

import nl.codecastle.extension.SeisekiExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(SeisekiExtension.class)
public class SampleTestIT {
    private final static Logger LOG = LoggerFactory.getLogger(SampleTestIT.class);
    @Test
    public void verySimpleTest() {
        LOG.debug("Running a test!");
    }

    @Test
    public void verySimpleTestTwo() {
        LOG.debug("Running a test!");
        aVerySimpleMethod();
    }

    private void aVerySimpleMethod() {
        LOG.debug("Logging from a very simple method!");
    }
}