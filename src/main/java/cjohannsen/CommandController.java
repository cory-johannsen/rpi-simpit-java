package cjohannsen;

import cjohannsen.protocol.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class CommandController {
    private static final Logger logger = LoggerFactory.getLogger(CommandController.class);

    private final ApplicationState applicationState;
    private final SimpitHost simpitHost;

    @Autowired
    public CommandController(final ApplicationState applicationState, SimpitHost simpitHost) {
        this.applicationState = applicationState;
        this.simpitHost = simpitHost;
    }

    @RequestMapping(value = "/status",
            method = GET,
            produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public String status(@RequestBody(required = false) String body)  {

        return applicationState.toString();
    }

    @RequestMapping(value = "actiongroup/standard/activate",
            method = POST,
            produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public String activateStandardActionGroup(@RequestBody(required = true) String body) {
        logger.info("index: " + body);
        simpitHost.activateStandardActionGroup(MessageType.ActionGroupIndex.valueOf(body.trim()));
        return "OK";
    }

    @RequestMapping(value = "actiongroup/standard/deactivate",
            method = POST,
            produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public String deactivateStandardActionGroup(@RequestBody(required = true) String body) {
        logger.info("index: " + body);
        simpitHost.deactivateStandardActionGroup(MessageType.ActionGroupIndex.valueOf(body.trim()));
        return "OK";
    }

    @RequestMapping(value = "actiongroup/standard/toggle",
            method = POST,
            produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public String toggleStandardActionGroup(@RequestBody(required = true) String body) {
        logger.info("index: " + body);
        simpitHost.toggleStandardActionGroup(MessageType.ActionGroupIndex.valueOf(body.trim()));
        return "OK";
    }

    @RequestMapping(value = "actiongroup/custom/activate",
            method = POST,
            produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public String activateCustomActionGroup(@RequestBody(required = true) String body) {
        logger.info("index: " + body);
        simpitHost.activateCustomActionGroup(Integer.valueOf(body));
        return "OK";
    }

    @RequestMapping(value = "actiongroup/custom/deactivate",
            method = POST,
            produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public String deactivateCustomActionGroup(@RequestBody(required = true) String body) {
        logger.info("index: " + body);
        simpitHost.deactivateCustomActionGroup(Integer.valueOf(body));
        return "OK";
    }

    @RequestMapping(value = "actiongroup/custom/toggle",
            method = POST,
            produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public String toggleCustomActionGroup(@RequestBody(required = true) String body) {
        logger.info("index: " + body);
        simpitHost.toggleCustomActionGroup(Integer.valueOf(body));
        return "OK";
    }
}
