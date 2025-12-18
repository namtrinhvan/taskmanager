package tds.op.taskmanager.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tds.op.taskmanager.representation.*;
import tds.op.taskmanager.service.CommentService;
import tds.op.taskmanager.service.TaskService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    private final TaskService taskService;
    private final CommentService commentService;

    // Inject cả CommentService để xử lý các nghiệp vụ liên quan đến comment ngay trong API task
    public TaskController(TaskService taskService, CommentService commentService) {
        this.taskService = taskService;
        this.commentService = commentService;
    }

    // =========================================================================
    // 1. GET DATA (Query)
    // =========================================================================

    /**
     * Lấy danh sách Task theo Plan, được gom nhóm theo UUID (TaskGroup).
     * Thay thế logic cũ trả về PlanTaskDTO.
     * URL: GET /api/task/plan/{planId}
     */
    @CrossOrigin("http://localhost:5173")
    @GetMapping("/plan/{planId}")
    public ResponseEntity<List<TaskGroup>> getTasksByPlan(@PathVariable Long planId) {
        List<TaskGroup> groups = taskService.getTasksByPlan(planId);
        return ResponseEntity.ok(groups);
    }

    /**
     * Lấy danh sách Task theo Plan và Unit cụ thể (dùng cho view của Unit).
     * URL: GET /api/task/plan/{planId}/{unitId}
     */
    @CrossOrigin("http://localhost:5173")
    @GetMapping("/plan/{planId}/{unitId}")
    public ResponseEntity<List<TaskGroup>> getTasksByPlanAndUnit(@PathVariable Long planId, @PathVariable Long unitId) {
        List<TaskGroup> groups = taskService.getTasksByPlanAndUnit(planId, unitId);
        return ResponseEntity.ok(groups);
    }

    /**
     * Lấy chi tiết Task bao gồm cả chuỗi phân công (Assignment Chain).
     * Logic mới: Trả về cả cây task cha -> con -> cháu để hiển thị flow giao việc.
     * URL: GET /api/task/{taskId}
     */
    @CrossOrigin("http://localhost:5173")
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDTO> getTaskDetail(@PathVariable Long taskId) {
        // Sử dụng getAssignmentChain để lấy đầy đủ context phân cấp và người thực hiện
        TaskDTO task = taskService.getAssignmentChain(taskId);
        if (task == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(task);
    }

    /**
     * Lấy lịch sử sự kiện của Task (Status change, Deadline change, Notes).
     * URL: GET /api/task/{taskId}/events
     */
    @CrossOrigin("http://localhost:5173")
    @GetMapping("/{taskId}/events")
    public ResponseEntity<List<TaskEventDTO>> getTaskEvents(@PathVariable Long taskId) {
        List<TaskEventDTO> events = taskService.getTaskEvents(taskId);
        return ResponseEntity.ok(events);
    }

    // =========================================================================
    // 2. COMMANDS (Create/Update/Delete)
    // =========================================================================

    /**
     * Tạo mới một Task.
     * URL: POST /api/task
     */
    @CrossOrigin("http://localhost:5173")
    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskDTO taskDTO) {
        try {
            TaskDTO created = taskService.createTask(taskDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating task: " + e.getMessage());
        }
    }

    /**
     * Cập nhật tiến độ của Task (0.0 -> 1.0).
     * URL: PATCH /api/task/{taskId}/progress?val=0.5
     */
    @CrossOrigin("http://localhost:5173")
    @PatchMapping("/{taskId}/progress")
    public ResponseEntity<?> updateProgress(@PathVariable Long taskId, @RequestParam double val) {
        try {
            taskService.updateTaskProgress(taskId, val);
            return ResponseEntity.ok("Progress updated to " + val);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid progress value");
        }
    }

    /**
     * Gia hạn Deadline cho Task (Tính năng mới).
     * URL: POST /api/task/{taskId}/extend-deadline
     * Params: newDate (yyyy-MM-dd), reason, staffId (người thực hiện gia hạn)
     */
    @CrossOrigin("http://localhost:5173")
    @PostMapping("/{taskId}/extend-deadline")
    public ResponseEntity<?> extendDeadline(@PathVariable Long taskId,
                                            @RequestParam String newDate,
                                            @RequestParam String reason,
                                            @RequestParam Long staffId) {
        try {
            LocalDate deadline = LocalDate.parse(newDate);
            taskService.extendDeadline(taskId, deadline, reason, staffId);
            return ResponseEntity.ok("Deadline extended successfully.");
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid date format. Please use yyyy-MM-dd");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Thêm người thực hiện vào Task.
     * URL: POST /api/task/{taskId}/executors
     * Body: [1, 2, 3] (List Staff IDs)
     */
    @CrossOrigin("http://localhost:5173")
    @PostMapping("/{taskId}/executors")
    public ResponseEntity<String> addExecutorsToTask(@PathVariable Long taskId, @RequestBody List<Long> staffIds) {
        try {
            taskService.addExecutorsToTask(taskId, staffIds);
            return ResponseEntity.ok("Executors added successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Xóa Task (Xóa cả comment, action, executor, events liên quan).
     * URL: DELETE /api/task/{taskId}
     */
    @CrossOrigin("http://localhost:5173")
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // 3. COMMENT MANAGEMENT (Interacting with CommentService)
    // =========================================================================

    /**
     * Lấy danh sách comment của Task.
     * URL: GET /api/task/{taskId}/comments
     */
    @CrossOrigin("http://localhost:5173")
    @GetMapping("/{taskId}/comments")
    public ResponseEntity<List<TaskCommentDTO>> getTaskComments(@PathVariable Long taskId) {
        return ResponseEntity.ok(commentService.getTaskComments(taskId));
    }

    /**
     * Thêm comment mới.
     * URL: POST /api/task/comment
     */
    @CrossOrigin("http://localhost:5173")
    @PostMapping("/comment")
    public ResponseEntity<Void> addComment(@RequestBody TaskCommentDTO taskCommentDTO) {
        commentService.addComment(taskCommentDTO);
        return ResponseEntity.ok().build();
    }
}