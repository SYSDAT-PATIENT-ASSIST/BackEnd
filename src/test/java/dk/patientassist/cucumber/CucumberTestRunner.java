package dk.patientassist.cucumber;

import io.cucumber.junit.platform.engine.Cucumber;

/**
 * Entry point for running Cucumber feature tests with JUnit 5.
 * Automatically picks up feature files in classpath and matching step definitions.
 */
@Cucumber
public class CucumberTestRunner {
    // No code needed – JUnit 5 discovers and runs features via @Cucumber annotation.
}
