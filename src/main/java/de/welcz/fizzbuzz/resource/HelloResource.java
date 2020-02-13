package de.welcz.fizzbuzz.resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HelloResource {
    String envVarTag = "myString";
    @GetMapping
    public String hello() {
        return "Hello Devops v.2.0.26;  Fizz-Buzz Game Path:  /api/v1/fizz-buzz/numbers/15" + envVarTag;
    }
}
