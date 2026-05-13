package jp.gihyo.projava.tasklist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class SignupController {

    private final TaskListDao dao;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SignupController(TaskListDao dao, PasswordEncoder passwordEncoder) {
        this.dao = dao;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/signup")
    public String displaySignup(Model model) {
        // 部署と課のリストを取得して画面に渡す（メンバーの追加機能）
        List<Map<String, Object>> departments = dao.findAllDepartments();
        List<Map<String, Object>> sections = dao.findAllSections();
        model.addAttribute("departments", departments);
        model.addAttribute("sections", sections);
        return "signup";
    }

    @PostMapping("/signup")
    public String registerUser(@RequestParam("name") String name,
                               @RequestParam("username") String email,
                               @RequestParam("password") String password,
                               Model model) {

        boolean hasError = false;

        // 1. あなたのバリデーション（入力チェック）
        if (name.isBlank()) {
            model.addAttribute("nameError", "名前を入力してください。");
            hasError = true;
        }

        if (!email.endsWith("@example.com")) {
            model.addAttribute("emailError", "メールアドレスは @example.com である必要があります。");
            hasError = true;
        } else if (dao.findUserByEmail(email) != null) {
            model.addAttribute("emailError", "すでに登録されているメールアドレスです。");
            hasError = true;
        }

        if (password.isBlank()) {
            model.addAttribute("passwordError", "パスワードを入力してください。");
            hasError = true;
        }

        // エラーがあれば、再度リストを取得して画面に戻す
        if (hasError) {
            List<Map<String, Object>> departments = dao.findAllDepartments();
            List<Map<String, Object>> sections = dao.findAllSections();
            model.addAttribute("departments", departments);
            model.addAttribute("sections", sections);
            return "signup";
        }

        // 2. メンバーの暗号化とDB保存
        String encodedPassword = passwordEncoder.encode(password);
        dao.createUser(name, email, encodedPassword);

        return "redirect:/login?register_success";
    }
}