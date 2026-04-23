/*
「プロになるJava」サンプル
https://gihyo.jp/book/2022/978-4-297-12685-8

Takaaki Sugiyama 2022 copyright reserved.
License: CC0 1.0 Universal
*/
package jp.gihyo.projava.tasklist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;



@Controller
public class HomeController {
    record TaskItem(
            String id,
            String task,
            List<Integer> taskUserIds,
            Integer projectId,
            String projectName,
            String description,
            String deadline,
            int done
    ) {}

//    private List<TaskItem> taskItems = new ArrayList<>();
    private final TaskListDao dao;

    @Autowired
    HomeController(TaskListDao dao) {
        this.dao = dao;
    }

    @GetMapping("/list")
    String listItems(Model model,
                     @RequestParam(value = "status", defaultValue = "all") String status,
                     @RequestParam(value = "projectId", defaultValue = "all") String projectId,
                     @RequestParam(value = "deptId", defaultValue = "all") String deptId,
                     @RequestParam(value = "sectionId", defaultValue = "all") String sectionId,
                     @RequestParam(value = "keyword", defaultValue = "") String keyword) {

        // 1. DAOの新しいメソッド「findFiltered」だけで検索を完結させます。
        // これにより、statusもprojectIdもdeptIdもすべて組み合わされた結果が返ってきます。
        List<TaskItem> taskItems = dao.findFiltered(status, projectId, deptId, sectionId,keyword);

        // 2. 画面（Thymeleaf）に渡すデータをセット
        model.addAttribute("taskList", taskItems);
        model.addAttribute("userList", dao.findAllUsers());
        model.addAttribute("projectList", dao.findAllProjects());

        // 部署と課のリストもプルダウンに表示するために必要です（DAOにメソッドがある前提）
        model.addAttribute("deptList", dao.findAllDepartments());
        model.addAttribute("sectionList", dao.findAllSections());

        // 現在選ばれている値を保持（HTML側の th:selected や hidden で使用）
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedProject", projectId);
        model.addAttribute("selectedDept", deptId);
        model.addAttribute("selectedSection", sectionId);
        model.addAttribute("keyword", keyword);

        return "home";
    }

    @GetMapping("/add")
    String addItem(@RequestParam("task") String task,
                   @RequestParam(value="projectId", required=false) String projectId,
                   @RequestParam(value="newProjectName", required=false) String newProjectName,
                   @RequestParam(value="taskUserIds",required = false) List<Integer> taskUserIds,
                   @RequestParam("description") String description,
                   @RequestParam("deadline") String deadline) {

        Integer targetProjectId = null;
        if ("new".equals(projectId) && newProjectName != null) {
            // 新規登録して新しいIDを取得
            targetProjectId = dao.addProject(newProjectName);
        } else if (projectId != null && !projectId.isEmpty()) {
            // 既存のIDを数値に変換
            targetProjectId = Integer.parseInt(projectId);
        }

        String id = UUID.randomUUID().toString().substring(0, 8);
        // 担当者が一人も選択されていない場合は空のリストをセットする
        List<Integer> userIds = (taskUserIds != null) ? taskUserIds : new ArrayList<>();
        // ★ここを修正：nullの代わりに targetProjectId を渡す
        TaskItem item = new TaskItem(id, task, userIds, targetProjectId, "", description, deadline, 0);

        dao.add(item);
        return "redirect:/list";
    }

    @GetMapping("/delete")
    String deleteItem(@RequestParam("id") String id) {
        dao.delete(id);
        return "redirect:/list";
    }

    @GetMapping("/update")
    String updateItem(@RequestParam("id") String id,
                      @RequestParam("task") String task,
                      @RequestParam(value="taskUserIds",required=false)  List<Integer> taskUserIds,
                      @RequestParam(value="projectId",required=false) String projectId, // Stringで受け取る
                      @RequestParam(value="newProjectName",required=false) String newProjectName, // 追加
                      @RequestParam("description") String description,
                      @RequestParam("deadline") String deadline,
                      @RequestParam("done") int done) {

        Integer targetProjectId = null;
        if ("new".equals(projectId) && newProjectName != null && !newProjectName.isEmpty()) {
            targetProjectId = dao.addProject(newProjectName);
        } else if (projectId != null && !projectId.isEmpty()) {
            targetProjectId = Integer.parseInt(projectId);
        }

        List<Integer> userIds = (taskUserIds != null) ? taskUserIds : new ArrayList<>();

        TaskItem taskItem = new TaskItem(id, task, userIds, targetProjectId, "", description, deadline, done);

        dao.update(taskItem);
        return "redirect:/list";
    }
}