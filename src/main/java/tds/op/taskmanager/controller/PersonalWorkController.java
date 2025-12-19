package tds.op.taskmanager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tds.op.taskmanager.representation.ActionDTO;
import tds.op.taskmanager.representation.PlanDTO;
import tds.op.taskmanager.representation.TaskDTO;
import tds.op.taskmanager.representation.TaskStatus;
import tds.op.taskmanager.service.PersonalWorkService;

import java.util.List;

@RestController
@RequestMapping("/api/my-work")
public class PersonalWorkController {

    private final PersonalWorkService personalWorkService;

    public PersonalWorkController(PersonalWorkService personalWorkService) {
        this.personalWorkService = personalWorkService;
    }

    // GET /api/my-work/plans?staffId=...
    @CrossOrigin("http://localhost:5173")
    @GetMapping("/plans")
    public ResponseEntity<List<PlanDTO>> getMyPlans(@RequestParam Long staffId) {
        return ResponseEntity.ok(personalWorkService.getMyPlans(staffId));
    }

    // GET /api/my-work/tasks?staffId=...
    @CrossOrigin("http://localhost:5173")
    @GetMapping("/tasks")
    public ResponseEntity<List<TaskDTO>> getMyTasks(@RequestParam Long staffId) {
        return ResponseEntity.ok(personalWorkService.getMyAssignedTasks(staffId));
    }

    // GET /api/my-work/actions?staffId=...
    @CrossOrigin("http://localhost:5173")
    @GetMapping("/actions")
    public ResponseEntity<List<ActionDTO>> getMyActions(@RequestParam Long staffId) {
        return ResponseEntity.ok(personalWorkService.getMyAssignedActions(staffId));
    }

    /**
     * Cập nhật trạng thái Action.
     * URL: PUT /api/my-work/action/{actionId}/status?staffId=1&status=COMPLETED
     */
    @CrossOrigin("http://localhost:5173")
    @PutMapping("/action/{actionId}/status")
    public ResponseEntity<?> updateActionStatus(
            @PathVariable Long actionId, 
            @RequestParam Long staffId,
            @RequestParam TaskStatus status) {
        try {
            personalWorkService.updateActionStatus(actionId, staffId, status);
            return ResponseEntity.ok("Action status updated to " + status);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}