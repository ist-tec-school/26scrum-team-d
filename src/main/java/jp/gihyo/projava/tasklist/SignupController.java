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

    // SignupController.java の signup メソッドを以下のように修正・追記します

    @PostMapping("/signup")
    public String signup(@RequestParam String name,
                         @RequestParam("username") String email,
                         @RequestParam String password,
                         @RequestParam Integer deptId,
                         @RequestParam(required = false) String newDepartmentName,
                         @RequestParam Integer sectionId,
                         @RequestParam(required = false) String newSectionName,
                         Model model) {

        boolean hasError = false; // エラーがあるかどうかを判定するフラグです。

        // --- 1. 部署のバリデーション ---
        if (deptId == null) { // 1行解説：部署が選択されていない（null）場合をチェックします。
            model.addAttribute("deptError", "部署を選択してください。"); // 1行解説：画面に表示するエラーメッセージをセットします。
            hasError = true; // 1行解説：エラーがあったのでフラグを「真(true)」にします。
        } else if (deptId == 0 && (newDepartmentName == null || newDepartmentName.isBlank())) { // 1行解説：「新規登録」を選んだのに名前が空の場合をチェックします。
            model.addAttribute("deptError", "新しい部署名を入力してください。"); // 1行解説：部署名未入力のエラーメッセージをセットします。
            hasError = true; // 1行解説：エラーフラグを立てます。
        }

        // --- 2. 課のバリデーション ---
        if (sectionId == null) { // 1行解説：課が選択されていない場合をチェックします。
            model.addAttribute("sectionError", "課を選択してください。"); // 1行解説：課の選択を促すメッセージをセットします。
            hasError = true; // 1行解説：エラーフラグを立てます。
        } else if (sectionId == 0 && (newSectionName == null || newSectionName.isBlank())) { // 1行解説：「新規登録」を選んだのに課の名前が空の場合をチェックします。
            model.addAttribute("sectionError", "新しい課名を入力してください。"); // 1行解説：課名未入力のエラーメッセージをセットします。
            hasError = true; // 1行解説：エラーフラグを立てます。
        }

        // --- 3. メールアドレスのバリデーション (@example.com 限定) ---
        if (!email.endsWith("@example.com")) { // 1行解説：入力されたメールが「@example.com」で終わっていないか判定します。
            model.addAttribute("emailError", "メールアドレスは @example.com である必要があります。"); // 1行解説：ドメイン制限のエラーメッセージをセットします。
            hasError = true; // 1行解説：エラーフラグを立てます。
        }

        // 1行解説：★独立したチェックにするか、上のelseとして繋げますが、保存処理との連動が重要です。
        if (dao.findUserByEmail(email) != null) {
            model.addAttribute("emailError", "すでに登録されているメールアドレスです。");
            hasError = true;
        }

        // --- 4. 名前とパスワードの既存バリデーション ---
        if (name.isBlank()) { // 1行解説：名前が空でないかチェックします。
            model.addAttribute("nameError", "名前を入力してください。"); // 1行解説：エラーメッセージをセットします。
            hasError = true; // 1行解説：エラーフラグを立てます。
        }
        if (password.isBlank()) { // 1行解説：パスワードが空でないかチェックします。
            model.addAttribute("passwordError", "パスワードを入力してください。"); // 1行解説：エラーメッセージをセットします。
            hasError = true; // 1行解説：エラーフラグを立てます。
        }

        // エラーがある場合は、入力画面に戻す
        if (hasError) { // 1行解説：一つでもエラーがあれば、この中に入ります。
            List<Map<String, Object>> departments = dao.findAllDepartments(); // 1行解説：プルダウンを再表示するために部署リストを取得します。
            List<Map<String, Object>> sections = dao.findAllSections(); // 1行解説：同様に課のリストを取得します。
            model.addAttribute("departments", departments); // 1行解説：取得したリストを画面に渡します。
            model.addAttribute("sections", sections); // 1行解説：取得したリストを画面に渡します。
            return "signup"; // 1行解説：登録処理は行わず、新規登録画面を再度表示します。
        }

        // --- エラーがなければ保存処理へ ---
        // (省略：dao.addDepartment や dao.createUser などの既存処理)
        return "redirect:/login?register"; // 1行解説：すべて成功したらログイン画面へリダイレクトします。
    }
}