package dk.patientassist.test.runners;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

/**
 * Cucumber JUnit 5 runner class.
 *
 * This runner will:
 * - Look for .feature files in src/test/resources/features/
 * - Look for step definitions in dk.patientassist.test.steps
 * - Be executed by Maven Surefire plugin during mvn test
 */
@Suite
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "dk.patientassist.test.steps")
public class RunCucumberTest {
    // No implementation required. Cucumber + JUnit 5 handles discovery.
}
