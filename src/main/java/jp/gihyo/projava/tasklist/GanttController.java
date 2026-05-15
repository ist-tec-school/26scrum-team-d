package jp.gihyo.projava.tasklist;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/gantt")
public class GanttController {

    /**
     * ガントチャート画面を表示する
     * URL: http://localhost:8080/gantt
     * * @return templates/gantt.html
     */
    @GetMapping
    public String showGantt() {
        // 将来的にデータベースからデータを取得して画面に渡す場合は、
        // 引数に Model model を追加し、model.addAttribute("tasks", tasks) のように記述します。

        return "gantt"; // templates フォルダ内の gantt.html を呼び出す
    }
}
