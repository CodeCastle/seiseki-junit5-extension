package nl.codecastle.it;

import nl.codecastle.extension.SeisekiExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SeisekiExtension.class)
@Tag("integration")
public class SampleTestIT {

    @Test
    public void verySimpleTest() {
        System.out.println("Running very simple test.");
    }
}