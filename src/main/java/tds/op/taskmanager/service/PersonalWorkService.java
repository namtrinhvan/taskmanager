package tds.op.taskmanager.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tds.op.taskmanager.repository.*;
import tds.op.taskmanager.representation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PersonalWorkService {

    private final PlanRepository planRepository;
    private final TaskRepository taskRepository;
    private final ActionRepository actionRepository;
    private final ActionExecutorRepository actionExecutorRepository;

    public PersonalWorkService(PlanRepository planRepository,
                               TaskRepository taskRepository,
                               ActionRepository actionRepository,
                               ActionExecutorRepository actionExecutorRepository) {
        this.planRepository = planRepository;
        this.taskRepository = taskRepository;
        this.actionRepository = actionRepository;
        this.actionExecutorRepository = actionExecutorRepository;
    }

    /** 1. Lấy danh sách Plan tôi tham gia */
    public List<PlanDTO> getMyPlans(Long staffId) {
        List<Plan> plans = planRepository.findPlansByParticipant(staffId);
        return plans.stream().map(this::toPlanDTO).collect(Collectors.toList());
    }

    /** 2. Lấy danh sách Task tôi được giao */
    public List<TaskDTO> getMyAssignedTasks(Long staffId) {
        List<Task> tasks = taskRepository.findTasksByExecutor(staffId);
        return tasks.stream().map(this::toTaskDTO).collect(Collectors.toList());
    }

    /** 3. Lấy danh sách Action tôi được giao */
    public List<ActionDTO> getMyAssignedActions(Long staffId) {
        List<Action> actions = actionRepository.findActionsByExecutor(staffId);
        return actions.stream().map(this::toActionDTO).collect(Collectors.toList());
    }

    /** * 4. Cập nhật trạng thái Action
     * Thay đổi: Dùng TaskStatus (PENDING <-> COMPLETED)
     */
    @Transactional
    public void updateActionStatus(Long actionId, Long staffId, TaskStatus newStatus) {
        // Check Action tồn tại
        Action action = actionRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("Action not found"));

        // Check quyền: staffId phải nằm trong executorId của bảng ActionExecutor
        boolean isExecutor = actionExecutorRepository.findByActionId(actionId).stream()
                .anyMatch(ae -> ae.getExecutorId().equals(staffId)); // Đã sửa getter thành getExecutorId

        if (!isExecutor) {
            throw new SecurityException("User is not assigned to this action");
        }

        // Update status
        action.setStatus(newStatus);
        actionRepository.save(action);
    }

    // --- MAPPERS CHUẨN (Khớp với file Entity/DTO bạn gửi) ---

    private PlanDTO toPlanDTO(Plan p) {
        PlanDTO dto = new PlanDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setStartMonth(p.getStartMonth());
        dto.setEndMonth(p.getEndMonth());
        // Map Unit qua ID nếu cần thiết
        return dto;
    }

    private TaskDTO toTaskDTO(Task t) {
        TaskDTO dto = new TaskDTO();
        dto.setId(t.getId());
        dto.setUuid(t.getUuid()); //
        dto.setName(t.getName());
        dto.setDescription(t.getDescription());
        dto.setMonth(t.getMonth());
        
        // Mapping Date Fields
        dto.setInitialStartDate(t.getInitialStartDate());
        dto.setActualStartDate(t.getActualStartDate());
        dto.setInitialDeadline(t.getInitialDeadline());
        dto.setCurrentDeadline(t.getCurrentDeadline());
        dto.setEndDate(t.getEndDate());
        
        dto.setStatus(t.getStatus()); 
        dto.setProgress(t.getProgress());
        dto.setPlan(null); // Để null hoặc map PlanDTO rút gọn để tránh loop
        return dto;
    }

    private ActionDTO toActionDTO(Action a) {
        ActionDTO dto = new ActionDTO();
        dto.setId(a.getId());
        dto.setTaskId(a.getTaskId()); //
        dto.setName(a.getName());
        dto.setDescription(a.getDescription());
        dto.setDeadline(a.getDeadline());
        dto.setStatus(a.getStatus()); // dùng Enum TaskStatus
        return dto;
    }
}