package tds.op.taskmanager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tds.op.taskmanager.representation.StaffDTO;
import tds.op.taskmanager.service.StaffService;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    /**
     * Lấy toàn bộ nhân viên (dùng cho các dropdown search global).
     * URL: GET /api/staff
     */
    @CrossOrigin("http://localhost:5173")
    @GetMapping
    public ResponseEntity<List<StaffDTO>> getAllStaff() {
        return ResponseEntity.ok(staffService.getAllStaff());
    }

    /**
     * Tạo nhân viên mới.
     * URL: POST /api/staff
     */
    @CrossOrigin("http://localhost:5173")
    @PostMapping
    public ResponseEntity<StaffDTO> createStaff(@RequestBody StaffDTO staffDTO) {
        return ResponseEntity.ok(staffService.createStaff(staffDTO));
    }

    /**
     * Cập nhật thông tin nhân viên.
     * URL: PUT /api/staff/{id}
     */
    @CrossOrigin("http://localhost:5173")
    @PutMapping("/{id}")
    public ResponseEntity<StaffDTO> updateStaff(@PathVariable Long id, @RequestBody StaffDTO staffDTO) {
        return ResponseEntity.ok(staffService.updateStaff(id, staffDTO));
    }

    /**
     * Xóa nhân viên (Soft delete hoặc Hard delete tùy service).
     * URL: DELETE /api/staff/{id}
     */
    @CrossOrigin("http://localhost:5173")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStaff(@PathVariable Long id) {
        staffService.deleteStaff(id);
        return ResponseEntity.ok().build();
    }
}