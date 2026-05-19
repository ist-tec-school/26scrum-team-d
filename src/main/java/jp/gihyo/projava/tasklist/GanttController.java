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
        for (int i = 0; i < 365; i++) {
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
        List<GanttDisplayItem> displayList = new ArrayList<>();
        Integer lastProjectId = null;

        for (HomeController.TaskItem item : taskItems) {
            Integer currentProjectId = item.projectId();

            // プロジェクトが切り替わったタイミング（または最初のループ）でプロジェクト行を生成
            if (lastProjectId == null || !lastProjectId.equals(currentProjectId)) {
                String projectName = (item.projectName() != null && !item.projectName().isBlank())
                        ? item.projectName()
                        : "プロジェクト未割当";

                // プロジェクト行を追加
                displayList.add(new GanttDisplayItem(projectName));
                lastProjectId = currentProjectId;
            }

            // 通常のタスク行を追加
            displayList.add(new GanttDisplayItem(item));
        }

        model.addAttribute("taskList", displayList);

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

    // =================================================================
    // 💡 【新設】ガントチャート表示専用のタスク情報保持クラス
    // =================================================================
    public static class GanttDisplayItem {
        private final HomeController.TaskItem originalTask;
        private final boolean isGroup;
        private final String displayTitle;

        // 通常タスク用のコンストラクタ
        public GanttDisplayItem(HomeController.TaskItem task) {
            this.originalTask = task;
            this.isGroup = false;
            this.displayTitle = task.task();
        }

        // プロジェクト（グループ）行用のコンストラクタ
        public GanttDisplayItem(String projectName) {
            this.originalTask = null;
            this.isGroup = true;
            this.displayTitle = projectName;
        }

        // HTML（Thymeleaf）から呼び出すためのGetter群
        public HomeController.TaskItem getTask() { return originalTask; }
        public boolean isGroup() { return isGroup; }
        public String getDisplayTitle() { return displayTitle; }
    }
}