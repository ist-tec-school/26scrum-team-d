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
     */
    @GetMapping
    public String showGantt(Model model,
                            @AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam(value = "timeScale", defaultValue = "day") String timeScale, // 💡 追加：日・週・月
                            @RequestParam(value = "startDate", required = false) String startDateStr,  // 💡 修正：変数名を統一
                            @RequestParam(value = "scope", defaultValue = "all") String scope,
                            @RequestParam(value = "status", defaultValue = "all") String status,
                            @RequestParam(value = "projectId", defaultValue = "all") String projectId,
                            @RequestParam(value = "deptId", defaultValue = "all") String deptId,
                            @RequestParam(value = "sectionId", defaultValue = "all") String sectionId,
                            @RequestParam(value = "keyword", defaultValue = "") String keyword) {

        // 1. ログインユーザーのIDを取得（自分のタスク絞り込み用）
        int currentUserId = 0;
        if (userDetails != null) {
            Map<String, Object> user = dao.findUserByEmail(userDetails.getUsername());
            if (user != null && user.get("user_id") != null) {
                currentUserId = ((Number) user.get("user_id")).intValue();
            }
        }

        // 2. 画面から送られてきた5軸条件でSQLを実行し、タスク一覧をセット
        List<HomeController.TaskItem> taskItems = dao.findByCondition(
                status, projectId, deptId, sectionId, keyword, scope, currentUserId
        );
        model.addAttribute("taskList", taskItems);

        // 3. セレクトボックスを表示するための各マスターデータをセット
        model.addAttribute("projectList", dao.findAllProjects());
        model.addAttribute("deptList", dao.findAllDepartments());
        model.addAttribute("sectionList", dao.findAllSections());
        model.addAttribute("userList", dao.findAllUsers());

        // 4. 現在選ばれているフィルターの値を画面に戻す（選択キープ用）
        model.addAttribute("selectedScope", scope);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedProject", projectId);
        model.addAttribute("selectedDept", deptId);
        model.addAttribute("selectedSection", sectionId);
        model.addAttribute("keyword", keyword);

        // 5. 💡【タイムスケール（日付バー）生成ロジックの修正】
        LocalDate baseDate = (startDateStr != null && !startDateStr.isEmpty()) ? LocalDate.parse(startDateStr) : LocalDate.now();
        List<TimeScaleHeader> timeScaleHeaders = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy/MM");

        if ("week".equals(timeScale)) {
            // 【週表示】表示開始日から5週間分
            LocalDate currentWeekStart = baseDate;
            for (int i = 0; i < 5; i++) {
                LocalDate currentWeekEnd = currentWeekStart.plusDays(6);
                String label = currentWeekStart.getMonthValue() + "/" + currentWeekStart.getDayOfMonth() + "週";
                timeScaleHeaders.add(new TimeScaleHeader(label, currentWeekStart, currentWeekEnd, ""));
                currentWeekStart = currentWeekStart.plusWeeks(1);
            }
        } else if ("month".equals(timeScale)) {
            // 【月表示】表示開始月から5ヶ月分
            LocalDate currentMonthStart = baseDate.withDayOfMonth(1);
            for (int i = 0; i < 5; i++) {
                LocalDate currentMonthEnd = currentMonthStart.withDayOfMonth(currentMonthStart.lengthOfMonth());
                String label = currentMonthStart.format(monthFormatter);
                timeScaleHeaders.add(new TimeScaleHeader(label, currentMonthStart, currentMonthEnd, ""));
                currentMonthStart = currentMonthStart.plusMonths(1);
            }
        } else {
            // 【日表示】表示開始日から7日間分 (デフォルト)
            for (int i = 0; i < 7; i++) {
                LocalDate targetDate = baseDate.plusDays(i);
                String label = targetDate.getMonthValue() + "/" + targetDate.getDayOfMonth();
                timeScaleHeaders.add(new TimeScaleHeader(label, targetDate, targetDate, targetDate.toString()));
            }
        }

        String todayStr = LocalDate.now().toString();
        model.addAttribute("today", todayStr);
        model.addAttribute("selectedStartDate", startDateStr != null ? startDateStr : todayStr);
        model.addAttribute("timeScaleHeaders", timeScaleHeaders);
        model.addAttribute("selectedTimeScale", timeScale);

        return "gantt";
    }

    // =================================================================
    // 💡 タイムスケールヘッダーの情報を保持する独立したクラス
    // =================================================================
    public static class TimeScaleHeader {
        private final String displayLabel;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final String dateStr;

        public TimeScaleHeader(String displayLabel, LocalDate startDate, LocalDate endDate, String dateStr) {
            this.displayLabel = displayLabel;
            this.startDate = startDate;
            this.endDate = endDate;
            this.dateStr = dateStr;
        }

        public String getDisplayLabel() { return displayLabel; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public String getDateStr() { return dateStr; }
    }

    // =================================================================
    // 💡 ガントチャート表示専用のタスク情報保持クラス
    // =================================================================
    public static class GanttDisplayItem {
        private final HomeController.TaskItem originalTask;
        private final boolean isGroup;
        private final String displayTitle;

        public GanttDisplayItem(HomeController.TaskItem task) {
            this.originalTask = task;
            this.isGroup = false;
            this.displayTitle = task.task();
        }

        public GanttDisplayItem(String projectName) {
            this.originalTask = null;
            this.isGroup = true;
            this.displayTitle = projectName;
        }

        public HomeController.TaskItem getTask() { return originalTask; }
        public boolean isGroup() { return isGroup; }
        public String getDisplayTitle() { return displayTitle; }
    }
}