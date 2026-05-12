package jp.gihyo.projava.tasklist;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SignupController {

    // "/signup" というURLにアクセスが来たら、signup.html を表示させる設定
    @GetMapping("/signup")
    public String displaySignup() {
        return "signup";
    }
}
