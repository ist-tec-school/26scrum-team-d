package jp.gihyo.projava.tasklist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;
import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;

@Controller
public class SignupController {
    private final TaskListDao dao;
    private final PasswordEncoder passwordEncoder;
    private final Tokenizer tokenizer = new Tokenizer();

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
                         @RequestParam(required = false)String newDepartmentKana,
                         @RequestParam Integer sectionId,
                         @RequestParam(required = false) String newSectionName,
                         RedirectAttributes redirectAttributes,
                         Model model) {

        boolean hasError = false;

        // 1. 部署・課の入力チェック＆重複バリデーション
        if (deptId == null) {
            model.addAttribute("deptError", "部署を選択してください。");
            hasError = true;
        } else if (deptId == 0) {
            if (newDepartmentName == null || newDepartmentName.isBlank()) {
                model.addAttribute("deptError", "新しい部署名を入力してください。");
                hasError = true;
            } else {
                // 💡 部署名の重複チェック
                String finalKana = convertToKana(newDepartmentKana);
                if (finalKana.isBlank() || finalKana.equals(newDepartmentName)) {
                    finalKana = convertToKana(newDepartmentName);
                }
                List<String> existingKanas = dao.findAllDeptKanas();
                if (isDuplicateDept(finalKana, existingKanas)) {
                    model.addAttribute("errorMessage", "その部署は既に登録されています。");
                    hasError = true;
                }
            }
        }

        if (sectionId == null) {
            model.addAttribute("sectionError", "課を選択してください。");
            hasError = true;
        } else if (sectionId == 0 && (newSectionName == null || newSectionName.isBlank())) {
            model.addAttribute("sectionError", "新しい課名を入力してください。");
            hasError = true;
        }

        // 2. ユーザー情報のバリデーション
        if (!email.endsWith("@example.com")) {
            model.addAttribute("emailError", "メールアドレスは @example.com である必要があります。");
            hasError = true;
        }

        if (dao.findUserByEmail(email) != null) {
            model.addAttribute("emailError", "すでに登録されているメールアドレスです。");
            hasError = true;
        }

        if (name.isBlank()) {
            model.addAttribute("nameError", "名前を入力してください。");
            hasError = true;
        }
        if (password.isBlank()) {
            model.addAttribute("passwordError", "パスワードを入力してください。");
            hasError = true;
        } else if (password.length() < 8) {
            model.addAttribute("passwordError", "パスワードは8文字以上で入力してください。");
            hasError = true;
        }

        // エラーがあれば、データベース操作は一切行わずに画面に戻す
        if (hasError) {
            List<Map<String, Object>> departments = dao.findAllDepartments();
            List<Map<String, Object>> sections = dao.findAllSections();
            model.addAttribute("departments", departments);
            model.addAttribute("sections", sections);
            return "signup";
        }

        // -------------------------------------------------------------
        // 🎉 3. ここまで来たらエラーなし！安全に登録処理を実行します
        // -------------------------------------------------------------
        Integer targetDeptId = deptId;
        if (Integer.valueOf(0).equals(deptId)) {
            String finalKana = convertToKana(newDepartmentKana).isBlank() ? convertToKana(newDepartmentName) : convertToKana(newDepartmentKana);
            // 「無ければ作る」メソッドで部署IDを取得
            targetDeptId = dao.findOrCreateDepartment(newDepartmentName, finalKana);
        }

        Integer targetSectionId = sectionId;
        if (Integer.valueOf(0).equals(sectionId)) {
            // 「無ければ作る」メソッドで課IDを取得
            targetSectionId = dao.findOrCreateSection(newSectionName, targetDeptId);
        }

        // パスワードを暗号化
        String encodedPassword = passwordEncoder.encode(password);

        // ユーザーをDBへ保存
        dao.createUser(name, email, encodedPassword, targetSectionId);

        redirectAttributes.addFlashAttribute("signupSuccess", "新しいユーザーを作成しました");
        return "redirect:/login";
    }
    @GetMapping("/api/check-dept")
    @ResponseBody
    public Map<String, Boolean> checkDept(@RequestParam String name) {
        // 入力された文字を強制的にカナ（ひらがな）に変換
        String inputKana = convertToKana(name);

        List<String> existingNames = dao.findAllDeptNames();
        List<String> existingKanas = dao.findAllDeptKanas();

        // 「変換後のカナ」が、既存の「漢字」または「カナ」と被っていないかチェック
        boolean isDuplicate = isDuplicateDept(inputKana, existingNames) ||
                isDuplicateDept(inputKana, existingKanas);

        return Map.of("isDuplicate", isDuplicate);
    }
    private boolean isDuplicateDept(String newName, List<String> existingNames) {
        String normalizedNew = normalize(newName);
        for (String existing : existingNames) {
            String normalizedExisting = normalize(existing);
            if (normalizedNew.contains(normalizedExisting) || normalizedExisting.contains(normalizedNew)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0x30A1 && c <= 0x30F6) {
                sb.append((char) (c - 0x60));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    private String convertToKana(String text) {
        if (text == null || text.isBlank()) return "";

        // すでにひらがなや英数字のみの場合はそのまま返す（解析ミス防止）
        if (text.matches("^[\\u3040-\\u309F\\u30A0-\\u30FFa-zA-Z0-9ー]*$")) {
            return normalize(text);
        }

        StringBuilder sb = new StringBuilder();
        List<Token> tokens = tokenizer.tokenize(text);
        for (Token token : tokens) {
            String reading = token.getReading();
            // カタカナで返ってくるので、normalizeメソッドでひらがなに直す
            if (reading.equals("*")) {
                // 読みが取れない（記号など）場合は元の文字を使う
                sb.append(token.getSurface());
            } else {
                sb.append(reading);
            }
        }
        return normalize(sb.toString()); // カタカナ→ひらがな変換も含む
    }



}