package jp.gihyo.projava.tasklist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.security.Principal;
import java.util.Map;

@Controller
public class PasswordController {

    private final TaskListDao dao;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordController(TaskListDao dao, PasswordEncoder passwordEncoder) {
        this.dao = dao;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * パスワード変更処理
     * フロントエンドから非同期（fetch等）で呼ばれることを想定し、
     * 文字列（success/error）をレスポンスとして返します。
     */

    @PostMapping("/api/user/update-password")
    @ResponseBody
    public String updatePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            Principal principal) {

        // 1. ログイン中のユーザーのメールアドレスを取得
        String email = principal.getName();

        // 2. データベースから現在のユーザー情報を取得
        Map<String, Object> user = dao.findUserByEmail(email);
        if (user == null) {
            return "error: user_not_found";
        }

        String dbPassword = (String) user.get("PASSWORD");

        // 3. 入力された「現在のパスワード」が正しいか検証
        if (!passwordEncoder.matches(currentPassword, dbPassword)) {
            // コンソールにログを出しておくと後で確認しやすいです
            System.out.println("パスワード変更失敗: 現在のパスワードが一致しません (" + email + ")");
            return "error: current_password_incorrect";
        }

        // 4. 新しいパスワードのバリデーション（例：8文字以上）
        if (newPassword == null || newPassword.length() < 8) {
            return "error: password_too_short";
        }

        // 5. 新しいパスワードをハッシュ化して保存
        String encodedPassword = passwordEncoder.encode(newPassword);
        dao.updatePassword(email, encodedPassword);

        System.out.println("パスワード変更成功: " + email);
        return "success";
    }
}