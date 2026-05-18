package jp.gihyo.projava.tasklist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/gantt")
public class GanttController {
    private final TaskListDao dao;

    @Autowired
    public GanttController(TaskListDao dao){
        this.dao = dao;
    }

    /**
     * ガントチャート画面を表示する
     * URL: http://localhost:8080/gantt
     * @return templates/gantt.html
     */
    @GetMapping
    public String showGantt(Model model,
                            @AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam(value = "startDate", required = false) String startDateStr,
                            @RequestParam(value = "scope", defaultValue = "all") String scope,
                            @RequestParam(value = "status", defaultValue = "all") String status,
                            @RequestParam(value = "projectId", defaultValue = "all") String projectId,
                            @RequestParam(value = "deptId", defaultValue = "all") String deptId,
                            @RequestParam(value = "sectionId", defaultValue = "all") String sectionId,
                            @RequestParam(value = "keyword", defaultValue = "") String keyword,
                            @RequestParam(value = "taskUserId", defaultValue = "all") String taskUserId, // 担当者フィルター用
                            @RequestParam(value = "startDate", required = false) String startDate) {
        LocalDate baseDate = (startDateStr != null && !startDateStr.isEmpty())
                ? LocalDate.parse(startDateStr)
                : LocalDate.now();

        List<String> timeScaleHeaders = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i < 61; i++) {
            timeScaleHeaders.add(baseDate.plusDays(i).format(formatter));
        }


        // 1. ログイン中のユーザーIDを取得（引数の7番目で利用）
        Integer currentUserId = null;
        if (userDetails != null) {
            Map<String, Object> user = dao.findUserByEmail(userDetails.getUsername());
            if (user != null && user.get("user_id") != null) {
                currentUserId = ((Number) user.get("user_id")).intValue();
            }
        }

        // 2. 画面から送られてきた5軸条件でSQLを実行
        List<HomeController.TaskItem> taskItems = dao.findByCondition(
                status, projectId, deptId, sectionId, keyword, scope, currentUserId
        );
        model.addAttribute("taskList", taskItems);

        // 3. セレクトボックスを表示するための各マスターデータをDBから取得してセット
        model.addAttribute("projectList", dao.findAllProjects());
        model.addAttribute("deptList", dao.findAllDepartments());
        model.addAttribute("sectionList", dao.findAllSections());
        model.addAttribute("userList", dao.findAllUsers());

        // 4. 現在選ばれているフィルターの値を画面に戻す（選択状態をキープするため）
        model.addAttribute("selectedScope", scope);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedProject", projectId);
        model.addAttribute("selectedDept", deptId);
        model.addAttribute("selectedSection", sectionId);
        model.addAttribute("keyword", keyword);

        // 5. 日程表示用の初期設定
        String todayStr = LocalDate.now().toString();
        model.addAttribute("today", todayStr);
        model.addAttribute("selectedStartDate", startDate != null ? startDate : todayStr);

        // ※ もしすでに別の日付ヘッダーロジックを実装済みの場合は、以下のif文は削除してください
        model.addAttribute("timeScaleHeaders", timeScaleHeaders);
        return "gantt";
    }
}