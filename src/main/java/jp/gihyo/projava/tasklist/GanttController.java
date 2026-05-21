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
                            @RequestParam(value = "timeScale", defaultValue = "day") String timeScale,
                            @RequestParam(value = "scope", defaultValue = "all") String scope,
                            @RequestParam(value = "status", defaultValue = "all") String status,
                            @RequestParam(value = "projectId", defaultValue = "all") String projectId,
                            @RequestParam(value = "deptId", defaultValue = "all") String deptId,
                            @RequestParam(value = "sectionId", defaultValue = "all") String sectionId,
                            @RequestParam(value = "keyword", defaultValue = "") String keyword,
                            @RequestParam(value = "taskUserId", defaultValue = "all") String taskUserId, // 担当者フィルター用
                            @RequestParam(value = "startDate", required = false) String startDate) {
        LocalDate baseDate = (startDate != null && !startDate.isEmpty())
                ? LocalDate.parse(startDate)
                : LocalDate.now();

        List<String> timeScaleHeaders = new ArrayList<>();
        if ("week".equals(timeScale)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (int i = -10; i < 53; i++) {
                timeScaleHeaders.add(baseDate.plusWeeks(i).format(formatter));
            }
        } else if ("month".equals(timeScale)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            for (int i = -5; i < 12; i++) {
                timeScaleHeaders.add(baseDate.plusMonths(i).format(formatter));
            }
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (int i = -15; i < 365; i++) {
                timeScaleHeaders.add(baseDate.plusDays(i).format(formatter));
            }
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
        LocalDate today = LocalDate.now();
        LocalDate threeDaysLater = today.plusDays(3);

        int i = 0;
        while (i < taskItems.size()) {
            HomeController.TaskItem firstItem = taskItems.get(i);
            Integer currentProjectId = firstItem.projectId();

            // 現在のプロジェクト（または未割当）に属するタスクを切り出す
            List<HomeController.TaskItem> projectTasks = new ArrayList<>();
            while (i < taskItems.size() &&
                    ((currentProjectId == null && taskItems.get(i).projectId() == null) ||
                            (currentProjectId != null && currentProjectId.equals(taskItems.get(i).projectId())))) {
                projectTasks.add(taskItems.get(i));
                i++;
            }

            // プロジェクトヘッダー行の作成と集計
            String projectName = (firstItem.projectName() != null && !firstItem.projectName().isBlank())
                    ? firstItem.projectName()
                    : "プロジェクト未割当";

            GanttDisplayItem groupHeader = new GanttDisplayItem(projectName);
            groupHeader.aggregateStatus(projectTasks, today, threeDaysLater);
            displayList.add(groupHeader);

            // 子タスク行を順次追加
            for (HomeController.TaskItem task : projectTasks) {
                displayList.add(new GanttDisplayItem(task));
            }
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
        model.addAttribute("selectedTimeScale", timeScale);

        // 5. 日程表示用の初期設定
        String todayStr = LocalDate.now().toString();
        String threeDaysLaterStr = LocalDate.now().plusDays(3).toString();
        model.addAttribute("today", todayStr);
        model.addAttribute("threeDaysLater", threeDaysLaterStr);
        model.addAttribute("selectedStartDate", baseDate.toString());

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
        private int totalCount = 0;
        private int completedCount = 0;
        private int delayedCount = 0;
        private int urgentCount = 0;

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

        public void aggregateStatus(List<HomeController.TaskItem> tasks, LocalDate today, LocalDate threeDaysLater) {
            this.totalCount = tasks.size();

            for (HomeController.TaskItem t : tasks) {
                LocalDate startDate = (t.start_date() != null && !t.start_date().isBlank()) ? LocalDate.parse(t.start_date()) : null;
                LocalDate deadline = (t.deadline() != null && !t.deadline().isBlank()) ? LocalDate.parse(t.deadline()) : null;
                int done = t.done();

                if (done == 3) {
                    this.completedCount++;
                } else {
                    boolean isDelayed = false;
                    if (startDate != null && startDate.isBefore(today) && done == 0) {
                        isDelayed = true;
                    } else if (deadline != null && deadline.isBefore(today)) {
                        isDelayed = true;
                    }

                    if (isDelayed) {
                        this.delayedCount++;
                    } else if (deadline != null && !deadline.isBefore(today) && !deadline.isAfter(threeDaysLater)) {
                        this.urgentCount++;
                    }
                }
            }
        }

        // HTML（Thymeleaf）から呼び出すためのGetter群
        public HomeController.TaskItem getTask() { return originalTask; }
        public boolean isGroup() { return isGroup; }
        public String getDisplayTitle() { return displayTitle; }

        public int getTotalCount() { return totalCount; }
        public int getCompletedCount() { return completedCount; }
        public int getDelayedCount() { return delayedCount; }
        public int getUrgentCount() { return urgentCount; }
    }
}