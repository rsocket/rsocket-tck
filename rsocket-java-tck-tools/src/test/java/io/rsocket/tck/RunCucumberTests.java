package io.rsocket.tck;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.Ignore;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {
                "pretty",
        },
        features = {
                "file:../rsocket-tck-features/features/"
        }
)
@Ignore
public class RunCucumberTests {
}
