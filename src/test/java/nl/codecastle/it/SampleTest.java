package nl.codecastle.it;

import nl.codecastle.extension.SeisekiExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(SeisekiExtension.class)
public class SampleTest {
    private final static Logger LOG = LoggerFactory.getLogger(SampleTest.class);
    @Test
    public void verySimpleTest() {
        LOG.debug("Running a test!");
        System.out.println("Running very simple test.");
    }
}