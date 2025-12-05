package tds.op.taskmanager.service;

import org.springframework.stereotype.Service;
import tds.op.taskmanager.repository.PlanRepository;
import tds.op.taskmanager.representation.Plan;
import tds.op.taskmanager.representation.PlanDTO;
import tds.op.taskmanager.representation.UnitDTO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    /** CREATE new plan */
    public PlanDTO createPlan(PlanDTO dto) {

        if (dto == null) return null;
        if (dto.getUnit() == null || dto.getUnit().getId() == 0) {
            throw new IllegalArgumentException("Unit of plan cannot be null.");
        }

        Plan plan = new Plan();
        plan.setName(dto.getName());
        plan.setStartMonth(dto.getStartMonth());
        plan.setEndMonth(dto.getEndMonth());
        plan.setUnitId(dto.getUnit().getId());

        planRepository.save(plan);

        dto.setId(plan.getId());
        return dto;
    }

    /** GET plan by id */
    public PlanDTO getPlan(Long id) {
        if (id == null) return null;

        Optional<Plan> planOpt = planRepository.findById(id);
        return planOpt.map(this::toDTO).orElse(null);
    }

    /** GET all plans of a unit */
    public List<PlanDTO> getPlansAsCreator(Long unitId) {
        return planRepository.findByUnitId(unitId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    /** UPDATE plan */
    public PlanDTO updatePlan(PlanDTO dto) {
        if (dto == null || dto.getId() == null) return null;

        Optional<Plan> planOpt = planRepository.findById(dto.getId());
        if (planOpt.isEmpty()) return null;

        Plan plan = planOpt.get();

        if (dto.getName() != null) plan.setName(dto.getName());
        if (dto.getStartMonth() != null) plan.setStartMonth(dto.getStartMonth());
        if (dto.getEndMonth() != null) plan.setEndMonth(dto.getEndMonth());
        if (dto.getUnit() != null) plan.setUnitId(dto.getUnit().getId());

        planRepository.save(plan);

        return toDTO(plan);
    }

    /** DELETE plan */
    public boolean deletePlan(Long id) {
        Optional<Plan> plan = planRepository.findById(id);
        if (plan.isEmpty()) return false;

        planRepository.delete(plan.get());
        return true;
    }

    /** Convert entity → DTO */
    private PlanDTO toDTO(Plan plan) {
        PlanDTO dto = new PlanDTO();
        dto.setId(plan.getId());
        dto.setName(plan.getName());
        dto.setStartMonth(plan.getStartMonth());
        dto.setEndMonth(plan.getEndMonth());

        // only map unitId → UnitDTO.id (lightweight)
        UnitDTO unit = new UnitDTO();
        unit.setId(plan.getUnitId());
        dto.setUnit(unit);

        return dto;
    }

    /**Lấy danh sách plan mà Unit tham gia (được giao việc)*/
    public List<PlanDTO> findPlansAsMember(Long unitId) {
        // Gọi repository để lấy các Plan thỏa mãn điều kiện
        List<Plan> involvedPlans = planRepository.findPlansAsMember(unitId);

        // Reuse lại hàm toDTO có sẵn để convert
        return involvedPlans.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
