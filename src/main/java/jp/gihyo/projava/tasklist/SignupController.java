package jp.gihyo.projava.tasklist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
<<<<<<< feature/TEAMD-7_TEAMD-123
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
=======
import org.springframework.transaction.annotation.Transactional;
>>>>>>> development

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
    @Transactional(rollbackFor = Exception.class)
    public String signup(@RequestParam String name,
                         @RequestParam("username") String email,
                         @RequestParam String password,
                         @RequestParam Integer deptId,
                         @RequestParam(required = false)String newDepartmentName,
                         @RequestParam Integer sectionId,
                         @RequestParam(required = false)String newSectionName,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (Integer.valueOf(0).equals(deptId) && !newDepartmentName.isBlank()) {
            if (dao.findDeptIdByName(newDepartmentName) != null) {
                model.addAttribute("errorMessage", "その部署は既に登録されています。");
                return displaySignup(model);
            }
        }
        Integer targetDeptId = deptId;
        if (Integer.valueOf(0).equals(deptId) && !newDepartmentName.isBlank()) {
            targetDeptId = dao.addDepartment(newDepartmentName);
        }

        if (Integer.valueOf(0).equals(sectionId) && !newSectionName.isBlank()) {
            if (dao.findSectionIdByName(newSectionName, targetDeptId) != null) {
                model.addAttribute("errorMessage", "その課は指定された部署内に既に登録されています。");
                return displaySignup(model);
            }
        }
        Integer targetSectionId = sectionId;
        if (Integer.valueOf(0).equals(sectionId) && !newSectionName.isBlank()) {
            targetSectionId = dao.addSection(newSectionName, targetDeptId);
        }

        boolean hasError = false;
        // バリデーション
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
        // パスワードを暗号化
        String encodedPassword = passwordEncoder.encode(password);
        // DBへ保存（TaskListDaoにこのメソッドがある前提です）
        dao.createUser(name, email, encodedPassword, targetSectionId);

        redirectAttributes.addFlashAttribute("signupSuccess", "新しいユーザーを作成しました");
        return "redirect:/login";
    }
}