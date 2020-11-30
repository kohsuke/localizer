package org.jvnet.localizer;

import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;


import static org.junit.jupiter.api.Assertions.assertTrue;

@MavenJupiterExtension
public class LocalizerPluginIT {

    @MavenTest
    void smokeTest(MavenExecutionResult result) {
        // the project itself has a test that will fail if the Messages are not generated
        assertTrue(result.isSuccesful());
    }
}
