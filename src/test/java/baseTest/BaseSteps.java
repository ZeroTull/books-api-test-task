package baseTest;

import api.service.BookService;
import core.restModel.RestResponse;
import tests.Hooks;

/**
 * Super-class for all step-definition classes (no hooks allowed).
 */
public abstract class BaseSteps {
    protected RestResponse lastResp;

    protected BookService service() {
        return Hooks.getService();
    }
}