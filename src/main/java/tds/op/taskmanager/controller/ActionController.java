package tds.op.taskmanager.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tds.op.taskmanager.representation.ActionDTO;
import tds.op.taskmanager.service.ActionService;

import java.util.List;

@RestController
@RequestMapping("/api/task/action")
public class ActionController {

    private final ActionService actionService;

    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }

    // =========================================================================
    // 1. GET ACTIONS
    // =========================================================================

    /**
     * Lấy danh sách các Action thuộc về một Task cụ thể.
     * Response sẽ bao gồm danh sách executors (nhiều người) cho mỗi Action.
     * * URL: GET /api/task/action/task/{taskId}
     */
    @CrossOrigin("http://localhost:5173")
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<ActionDTO>> getActionsByTaskId(@PathVariable Long taskId) {
        List<ActionDTO> actions = actionService.getActionsByTaskId(taskId);
        return ResponseEntity.ok(actions);
    }

    // =========================================================================
    // 2. CREATE ACTION (Multi-Executor Logic)
    // =========================================================================

    /**
     * Tạo mới một Action.
     * Điểm nâng cấp: Body (ActionDTO) chấp nhận một danh sách 'executors'.
     * Service sẽ tự động lưu Action và tạo các bản ghi vào bảng ActionExecutor.
     * * URL: POST /api/task/action
     * Body Example:
     * {
     * "taskId": 101,
     * "name": "Design Database",
     * "description": "Vẽ ERD",
     * "deadline": "2023-12-31",
     * "executors": [
     * { "id": 1 },
     * { "id": 2 }
     * ]
     * }
     */
    @CrossOrigin("http://localhost:5173")
    @PostMapping
    public ResponseEntity<Void> createAction(@RequestBody ActionDTO actionDTO) {
        try {
            // ActionService của (A) đã xử lý logic lưu list executors
            actionService.createAction(actionDTO);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =========================================================================
    // 3. DELETE ACTION
    // =========================================================================

    /**
     * Xóa một Action.
     * Hệ thống sẽ tự động xóa các liên kết executor liên quan trong bảng ActionExecutor trước khi xóa Action.
     * * URL: DELETE /api/task/action/{actionId}
     */
    @CrossOrigin("http://localhost:5173")
    @DeleteMapping("/{actionId}")
    public ResponseEntity<Void> deleteAction(@PathVariable Long actionId) {
        actionService.deleteAction(actionId);
        return ResponseEntity.noContent().build();
    }
}