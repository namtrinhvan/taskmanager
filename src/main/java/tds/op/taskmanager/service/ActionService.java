package tds.op.taskmanager.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tds.op.taskmanager.repository.ActionExecutorRepository;
import tds.op.taskmanager.repository.ActionRepository;
import tds.op.taskmanager.repository.StaffRepository;
import tds.op.taskmanager.representation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ActionService {

    private final ActionRepository actionRepository;
    private final ActionExecutorRepository actionExecutorRepository;
    private final StaffRepository staffRepository;

    public ActionService(ActionRepository actionRepository,
                         ActionExecutorRepository actionExecutorRepository,
                         StaffRepository staffRepository) {
        this.actionRepository = actionRepository;
        this.actionExecutorRepository = actionExecutorRepository;
        this.staffRepository = staffRepository;
    }

    @Transactional
    public void createAction(ActionDTO dto) {
        if (dto == null || dto.getTaskId() == null) return;

        // 1. Save Action
        Action action = new Action();
        action.setTaskId(dto.getTaskId());
        action.setName(dto.getName());
        action.setDescription(dto.getDescription());
        action.setDeadline(dto.getDeadline());
        action.setStatus(TaskStatus.PENDING);

        Action savedAction = actionRepository.save(action);

        // 2. Save ActionExecutors (Logic đa người thực hiện)
        if (dto.getExecutors() != null && !dto.getExecutors().isEmpty()) {
            List<ActionExecutor> links = new ArrayList<>();
            for (StaffDTO staff : dto.getExecutors()) {
                ActionExecutor ae = new ActionExecutor();
                ae.setActionId(savedAction.getId());
                ae.setExecutorId(staff.getId());
                links.add(ae);
            }
            actionExecutorRepository.saveAll(links);
        }
    }

    public List<ActionDTO> getActionsByTaskId(Long taskId) {
        List<Action> actions = actionRepository.findByTaskId(taskId);
        return actions.stream().map(this::convertActionToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void deleteAction(Long actionId) {
        if (actionRepository.existsById(actionId)) {
            // Xóa liên kết người thực hiện trước
            List<ActionExecutor> executors = actionExecutorRepository.findByActionId(actionId);
            actionExecutorRepository.deleteAll(executors);

            // Sau đó xóa Action
            actionRepository.deleteById(actionId);
        }
    }

    /**
     * Hàm xóa toàn bộ Action của một Task (Dùng khi xóa Task cha).
     */
    @Transactional
    public void deleteActionsByTaskId(Long taskId) {
        List<Action> actions = actionRepository.findByTaskId(taskId);
        for (Action action : actions) {
            deleteAction(action.getId());
        }
    }

    public Double calculateProgress(Long taskId) {
        List<Action> actions = actionRepository.findByTaskId(taskId);

        if (actions.isEmpty()) {
            return null; // Không có action -> Trả về null
        }

        long completedCount = actions.stream()
                .filter(a -> a.getStatus() == TaskStatus.COMPLETED) //
                .count();

        // Tính tỷ lệ: số action hoàn thành / tổng số action
        return (double) completedCount / actions.size();
    }
    // --- Helper Mappers ---
    private ActionDTO convertActionToDTO(Action action) {
        ActionDTO dto = new ActionDTO();
        dto.setId(action.getId());
        dto.setTaskId(action.getTaskId());
        dto.setName(action.getName());
        dto.setDescription(action.getDescription());
        dto.setDeadline(action.getDeadline());
        dto.setStatus(action.getStatus());

        // Map Executors của Action (Query từ bảng ActionExecutor)
        List<ActionExecutor> links = actionExecutorRepository.findByActionId(action.getId());
        if (!links.isEmpty()) {
            List<Long> staffIds = links.stream().map(ActionExecutor::getExecutorId).collect(Collectors.toList());
            List<Staff> staffs = staffRepository.findAllById(staffIds);
            dto.setExecutors(staffs.stream().map(this::convertStaffToDTO).collect(Collectors.toList()));
        }

        return dto;
    }

    private StaffDTO convertStaffToDTO(Staff s) {
        StaffDTO dto = new StaffDTO();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setEmail(s.getEmail());
        dto.setPicture(s.getPicture());
        return dto;
    }
}