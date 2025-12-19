package tds.op.taskmanager.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tds.op.taskmanager.representation.PlanDTO;
import tds.op.taskmanager.service.PlanService;

import java.util.List;

@RestController
@RequestMapping("/api/plan")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    /**
     * Tạo Plan mới.
     * Endpoint: POST /api/plan
     */
    @CrossOrigin("http://localhost:5173")
    @PostMapping
    public ResponseEntity<PlanDTO> createPlan(@RequestBody PlanDTO dto) {
        try {
            PlanDTO created = planService.createPlan(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lấy chi tiết Plan.
     * Endpoint: GET /api/plan/{id}
     */
    @CrossOrigin("http://localhost:5173")
    @GetMapping("/{id}")
    public ResponseEntity<PlanDTO> getPlan(@PathVariable Long id) {
        PlanDTO dto = planService.getPlan(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    /**
     * Lấy các Plan do Unit này TẠO ra (Unit là chủ sở hữu).
     * Endpoint: GET /api/plan/unit/{unitId}
     * Note: Đã map với hàm `getPlansAsCreator` trong Service A.
     */
    @CrossOrigin("http://localhost:5173")
    @GetMapping("/unit/{unitId}")
    public ResponseEntity<List<PlanDTO>> getPlansByUnit(@PathVariable Long unitId) {
        List<PlanDTO> list = planService.getPlansAsCreator(unitId);
        return ResponseEntity.ok(list);
    }

    /**
     * Lấy các Plan mà Unit này THAM GIA (Được giao task nhưng không phải chủ Plan).
     * Endpoint: GET /api/plan/participant/{unitId}
     * Note: Đã map với hàm `findPlansAsMember` trong Service A.
     */
    @CrossOrigin("http://localhost:5173")
    @GetMapping("/participant/{unitId}")
    public ResponseEntity<List<PlanDTO>> getPlansWhereUnitIsParticipant(@PathVariable Long unitId) {
        List<PlanDTO> list = planService.findPlansAsMember(unitId);
        return ResponseEntity.ok(list);
    }

    /**
     * Cập nhật Plan.
     * Endpoint: PUT /api/plan/{id}
     */
    @CrossOrigin("http://localhost:5173")
    @PutMapping("/{id}")
    public ResponseEntity<PlanDTO> updatePlan(@PathVariable Long id, @RequestBody PlanDTO dto) {
        dto.setId(id);
        PlanDTO updated = planService.updatePlan(dto);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    /**
     * Xóa Plan.
     * Endpoint: DELETE /api/plan/{id}
     */
    @CrossOrigin("http://localhost:5173")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        boolean deleted = planService.deletePlan(id);
        if (!deleted) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}