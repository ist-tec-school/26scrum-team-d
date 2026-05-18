package jp.gihyo.projava.tasklist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
     * * @return templates/gantt.html
     */
    @GetMapping
    public String showGantt(Model model,
                            @AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam(value = "scope", defaultValue = "all") String scope,
                            @RequestParam(value = "status", defaultValue = "all") String status,
                            @RequestParam(value = "projectId", defaultValue = "all") String projectId,
                            @RequestParam(value = "deptId", defaultValue = "all") String deptId,
                            @RequestParam(value = "sectionId", defaultValue = "all") String sectionId,
                            @RequestParam(value = "keyword", defaultValue = "") String keyword,
                            @RequestParam(value = "startDate", required = false) String startDate) {
                            @RequestParam(value = "taskUserId", defaultValue = "all") String taskUserId, // 担当者フィルター用
                            @RequestParam(value = "keyword", defaultValue = "") String keyword,
                            @RequestParam(value = "startDate", required = false) String startDate) {

        // 1. ログインユーザーのIDを取得（「自分のタスク」絞り込み用）
        Integer currentUserId = null;
        if (userDetails != null) {
            Map<String, Object> user = dao.findUserByEmail(userDetails.getUsername());
            if (user != null && user.get("user_id") != null) {
                currentUserId = ((Number) user.get("user_id")).intValue();
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
        // 2. 担当者フィルターとscopeの連動ロジック
        String targetScope = scope;
        Integer targetUserId = currentUserId;

        // 2. 画面から送られてきた5軸条件でSQLを実行（固定値の "all" から変数に変更）
        List<HomeController.TaskItem> taskItems = dao.findByCondition(
                status, projectId, deptId, sectionId, keyword, scope, currentUserId
        );
        // 「担当者」セレクトボックスで特定のユーザーが選ばれた場合、DAOの「mine」の仕組みを流用してその人で絞り込む
        if (!"all".equals(taskUserId)) {
            targetScope = "mine";
            targetUserId = Integer.parseInt(taskUserId);
        }

        // 3. 選択された条件をそのままDAOに引き渡して、タスクをSQLで絞り込む
        List<HomeController.TaskItem> taskItems = dao.findByCondition(
                status, projectId, deptId, sectionId, keyword, targetScope, targetUserId
        );
        model.addAttribute("taskList", taskItems);

        // 4. セレクトボックス（th:each）に表示するためのマスターデータをDBから取得してセット
        model.addAttribute("projectList", dao.findAllProjects());
        model.addAttribute("deptList", dao.findAllDepartments());
        model.addAttribute("sectionList", dao.findAllSections());
        model.addAttribute("userList", dao.findAllUsers());

        // 5. 現在選択されているフィルターの値をセット（th:selected で選択状態を維持するため）
        model.addAttribute("selectedScope", scope);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedProject", projectId);
        model.addAttribute("selectedDept", deptId);
        model.addAttribute("selectedSection", sectionId);
        model.addAttribute("selectedTaskUserId", taskUserId);
        model.addAttribute("keyword", keyword);

        // 6. タイムライン表示に必要な日付の初期値をセット（画面エラー防止）
        String todayStr = LocalDate.now().toString();
        model.addAttribute("today", todayStr);
        model.addAttribute("selectedStartDate", startDate != null ? startDate : todayStr);

        // ※ もしすでに別の日付ヘッダーロジックを実装済みの場合は、以下のif文は削除してください
        if (!model.containsAttribute("timeScaleHeaders")) {
            model.addAttribute("timeScaleHeaders", List.of("5/11", "5/12", "5/13", "5/14", "5/15", "5/16", "5/17"));
        }

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

        // 5. 日程表示用の初期設定（プルした日付の仕組みを引き継ぎます）
        String todayStr = LocalDate.now().toString();
        model.addAttribute("today", todayStr);
        model.addAttribute("selectedStartDate", startDate != null ? startDate : todayStr);
        model.addAttribute("timeScaleHeaders", List.of());

        return "gantt";
    }
}