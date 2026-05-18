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
                            @RequestParam(value = "keyword", defaultValue = "") String keyword) {

        List<HomeController.TaskItem> taskItems = dao.findByCondition("all", "all", "all", "all", "", "all", null);

        model.addAttribute("taskList", taskItems);

        return "gantt";
    }
}
