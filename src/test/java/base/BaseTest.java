package base;

import spribe.task.api.core.RequestSpecFactory;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;


public abstract class BaseTest {

    protected RequestSpecification spec;

    @BeforeClass(alwaysRun = true)
    public void setupSpec() {
        spec = RequestSpecFactory.defaultSpec();
    }
}
