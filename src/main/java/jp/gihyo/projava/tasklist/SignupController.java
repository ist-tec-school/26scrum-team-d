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

    @Autowired // コンストラクタでインジェクション
    public SignupController(TaskListDao dao) {
        this.dao = dao;
    }

    // ★ 道具（Bean）を受け取るための準備
    private final TaskListDao dao;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SignupController(TaskListDao dao, PasswordEncoder passwordEncoder) {
        this.dao = dao;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/signup")
    public String displaySignup(Model model) {
        List<Map<String, Object>> departments = dao.findAllDepartments();
        List<Map<String, Object>> sections = dao.findAllSections();

        model.addAttribute("departments",departments);
        model.addAttribute("sections",sections);

        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String name,
                         @RequestParam("username") String email,
                         @RequestParam String password) {
        // パスワードを暗号化
        String encodedPassword = passwordEncoder.encode(password);
        // DBへ保存（TaskListDaoにこのメソッドがある前提です）
        dao.createUser(name, email, encodedPassword);

        return "redirect:/login";
    }
}
