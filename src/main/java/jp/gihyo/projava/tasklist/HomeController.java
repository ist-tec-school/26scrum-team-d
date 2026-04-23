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
            Integer taskUserId,
            Integer projectId,
            String projectName,
            String description,
            String deadline,
            int done
    ) {}

    private List<TaskItem> taskItems = new ArrayList<>();
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

        List<TaskItem> taskItems = dao.findFiltered(status, projectId, deptId, sectionId, keyword);

        model.addAttribute("taskList", taskItems);
        model.addAttribute("userList", dao.findAllUsers());
        model.addAttribute("projectList", dao.findAllProjects());
        model.addAttribute("deptList", dao.findAllDepartments());

        // --- ここを修正 ---
        // 部署が選択されている（all以外）場合は、その部署の課だけを取得する
        if (!"all".equals(deptId)) {
            model.addAttribute("sectionList", dao.findSectionsByDeptId(Integer.parseInt(deptId)));
        } else {
            model.addAttribute("sectionList", dao.findAllSections());
        }
        // -----------------

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
                   @RequestParam(value="taskUserId",required = false) Integer taskUserId,
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
        // ★ここを修正：nullの代わりに targetProjectId を渡す
        TaskItem item = new TaskItem(id, task, taskUserId, targetProjectId, "", description, deadline, 0);

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
                      @RequestParam(value="taskUserId",required=false) Integer taskUserId,
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

        // 引数の最後から2番目を targetProjectId に変
        TaskItem taskItem = new TaskItem(id, task, taskUserId, targetProjectId, "", description, deadline, done);

        dao.update(taskItem);
        return "redirect:/list";
    }
}