package jp.gihyo.projava.tasklist;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SignupController {

    @GetMapping("/signup")
    public String displaySignup() {
        return "signup";
    }
}
