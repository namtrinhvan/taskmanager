package tds.op.taskmanager.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tds.op.taskmanager.representation.StaffDTO;
import tds.op.taskmanager.representation.UnitDTO;
import tds.op.taskmanager.service.UnitService;

import java.util.List;

@RestController
@RequestMapping("/api/unit")
public class UnitController {

    private final UnitService unitService;

    public UnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    /**
     * Tạo mới một Unit (Phòng ban/Đội nhóm).
     * Endpoint: POST /api/unit
     * Query Param: parentId (optional) - Nếu không truyền hoặc = 0 thì là Root Unit (Cấp cao nhất).
     */
    @CrossOrigin("http://localhost:5173")
    @PostMapping
    public ResponseEntity<UnitDTO> createUnit(
            @RequestBody UnitDTO dto,
            @RequestParam(required = false, defaultValue = "0") Long parentId
    ) {
        UnitDTO created = unitService.createUnit(dto, parentId);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Lấy toàn bộ cấu trúc tổ chức dạng cây (Tree).
     * Dùng để vẽ sơ đồ tổ chức bên FE.
     * Endpoint: GET /api/unit/structure
     */
    @CrossOrigin("http://localhost:5173")
    @GetMapping("/structure")
    public ResponseEntity<List<UnitDTO>> getCorporateStructure() {
        return ResponseEntity.ok(unitService.getCorporateStructure());
    }

    /**
     * Lấy cấu trúc cây bắt đầu từ một unit cụ thể.
     * Endpoint: GET /api/unit/structure/{id}
     */
    @CrossOrigin("http://localhost:5173")
    @GetMapping("/structure/{id}")
    public ResponseEntity<UnitDTO> getStructureByRootId(@PathVariable Long id) {
        UnitDTO tree = unitService.getStructure(id);
        if (tree == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tree);
    }

    /**
     * Lấy danh sách các Unit con trực tiếp (Level + 1).
     * Dùng khi click mở rộng một node trên cây.
     * Endpoint: GET /api/unit/{id}/children
     */
    @CrossOrigin("http://localhost:5173")
    @GetMapping("/{id}/children")
    public ResponseEntity<List<UnitDTO>> getUnitChildren(@PathVariable Long id) {
        return ResponseEntity.ok(unitService.getUnitChildren(id));
    }

    /**
     * Lấy danh sách nhân viên trực thuộc Unit này (Chỉ level này, không đệ quy).
     * Endpoint: GET /api/unit/{id}/staff
     */
    @CrossOrigin("http://localhost:5173")
    @GetMapping("/{id}/staff")
    public ResponseEntity<List<StaffDTO>> getUnitStaff(@PathVariable Long id) {
        return ResponseEntity.ok(unitService.getUnitStaff(id));
    }

    /**
     * Thêm danh sách nhân viên vào một Unit.
     * Endpoint: POST /api/unit/{id}/staff
     * Body: [1, 2, 3] (List Staff IDs)
     */
    @CrossOrigin("http://localhost:5173")
    @PostMapping("/{id}/staff")
    public ResponseEntity<String> addStaffToUnit(
            @PathVariable Long id,
            @RequestBody List<Long> staffIds
    ) {
        try {
            unitService.addStaffToUnit(id, staffIds);
            return ResponseEntity.ok("Added " + staffIds.size() + " staff to unit successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding staff: " + e.getMessage());
        }
    }

    /**
     * Lấy TOÀN BỘ nhân viên thuộc Unit này VÀ các Unit con cháu (Đệ quy).
     * Tính năng quan trọng để lọc Task của cả phòng ban lớn.
     * Endpoint: GET /api/unit/{id}/all-staff
     */
    @CrossOrigin("http://localhost:5173")
    @GetMapping("/{id}/all-staff")
    public ResponseEntity<List<StaffDTO>> getAllStaffUnderUnit(@PathVariable Long id) {
        return ResponseEntity.ok(unitService.getAllStaffUnderUnit(id));
    }

    /**
     * Xóa một Unit.
     * Endpoint: DELETE /api/unit/{id}
     */
    @CrossOrigin("http://localhost:5173")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUnit(@PathVariable Long id) {
        boolean deleted = unitService.deleteUnit(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Cannot delete unit. It may have child units or assigned staff.");
        }
    }
}