package jp.gihyo.projava.tasklist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class SignupController {
    private final TaskListDao dao;

    @Autowired // コンストラクタでインジェクション
    public SignupController(TaskListDao dao) {
        this.dao = dao;
    }

    @GetMapping("/signup")
    public String displaySignup(Model model) {
        List<Map<String, Object>> departments = dao.findAllDepartments();
        List<Map<String, Object>> sections = dao.findAllSections();

        model.addAttribute("departments",departments);
        model.addAttribute("sections",sections);

        return "signup";
    }
}
