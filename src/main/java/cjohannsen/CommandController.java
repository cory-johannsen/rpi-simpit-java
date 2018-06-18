package cjohannsen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class CommandController {

    private final ApplicationState applicationState;

    @Autowired
    public CommandController(final ApplicationState applicationState) {
        this.applicationState = applicationState;
    }

    @RequestMapping(value = "/status",
            method = GET,
            produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public String status(@RequestBody(required = false) String body)  {

        return applicationState.toString();
    }
}
