package me.xlui.spring;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "<html>" +
                "<head><title>Test Page</title></head>" +
                "<body><div align=\"center\">Hello World!</div><br><br><div align=\"center\">This website shows you have successfully integrated <b>Travis-CI</b></div>" +
                "</body></html>";
    }
}
