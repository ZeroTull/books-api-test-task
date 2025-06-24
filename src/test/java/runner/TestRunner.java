package runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.Listeners;
import utils.retry.QuorumRetryAnalyzer;
import utils.retry.RetryTransformer;

@CucumberOptions(
        features = "classpath:features",
        glue = "tests",
        plugin = {"pretty", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"}
)
@Listeners({
        RetryTransformer.class,
        QuorumRetryAnalyzer.QuorumListener.class   // counts passes
})
public class TestRunner extends AbstractTestNGCucumberTests {
}