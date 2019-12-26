package de.welcz.fizzbuzz.resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HelloResource {
    @GetMapping
    public String hello() {
        return "Hello Devops v.20; deploy time: 4:10;  Fizz-Buzz Game Path:  /api/v1/fizz-buzz/numbers/15";
    }
}
