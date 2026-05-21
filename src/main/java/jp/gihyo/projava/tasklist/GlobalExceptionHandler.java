package jp.gihyo.projava.tasklist;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * アプリケーション内で発生したすべての予期せぬ例外（Exception）をキャッチする
     */
    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {
        // コンソール（ログ）にエラーの具体的な原因（スタックトレース）を出力する
        System.err.println("【システム例外発生】: " + ex.getMessage());
        ex.printStackTrace();

        // ユーザー向けの画面に表示するメッセージをセット
        model.addAttribute("errorMessage", "申し訳ありません。システムに一時的な不具合が発生しました。");
        model.addAttribute("detailMessage", ex.getMessage());

        // templates/error.html を呼び出す
        return "error";
    }
}