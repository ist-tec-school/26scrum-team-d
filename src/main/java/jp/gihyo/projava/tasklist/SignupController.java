package jp.gihyo.projava.tasklist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SignupController {

    private final TaskListDao dao;

    @Autowired
    public SignupController(TaskListDao dao) {
        this.dao = dao;
    }

    @GetMapping("/signup")
    public String displaySignup() {
        return "signup";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam("name") String name,
                               @RequestParam("username") String email,
                               @RequestParam("password") String password,
                               Model model) {

        boolean hasError = false;

        // 1. 名前の空欄チェック (バックエンド)
        if (name.isBlank()) {
            model.addAttribute("nameError", "名前を入力してください。");
            hasError = true;
        }

        // 2. メールアドレスの形式とドメインチェック (バックエンド)
        if (!email.endsWith("@example.com")) {
            model.addAttribute("emailError", "メールアドレスは @example.com である必要があります。");
            hasError = true;
        } else if (dao.findUserByEmail(email) != null) {
            // 重複チェック (TaskListDaoを利用)
            model.addAttribute("emailError", "すでに登録されているメールアドレスです。");
            hasError = true;
        }

        // 3. パスワードの空欄チェック (バックエンド)
        if (password.isBlank()) {
            model.addAttribute("passwordError", "パスワードを入力してください。");
            hasError = true;
        }

        // 一つでもエラーがあれば、登録させずに画面に戻す
        if (hasError) {
            return "signup";
        }

        // 全てクリアならログイン画面へ
        return "redirect:/login?register_success";
    }
}