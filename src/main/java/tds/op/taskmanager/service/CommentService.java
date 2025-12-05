package tds.op.taskmanager.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tds.op.taskmanager.repository.StaffRepository;
import tds.op.taskmanager.repository.TaskCommentRepository;
import tds.op.taskmanager.representation.Staff;
import tds.op.taskmanager.representation.StaffDTO;
import tds.op.taskmanager.representation.TaskComment;
import tds.op.taskmanager.representation.TaskCommentDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final StaffRepository staffRepository;

    public CommentService(TaskCommentRepository taskCommentRepository, StaffRepository staffRepository) {
        this.taskCommentRepository = taskCommentRepository;
        this.staffRepository = staffRepository;
    }

    @Transactional
    public void addComment(TaskCommentDTO dto) {
        TaskComment comment = new TaskComment();
        comment.setTaskId(dto.getTaskId());
        comment.setMessage(dto.getMessage());
        comment.setEpoch(System.currentTimeMillis());
        comment.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        if (dto.getOwner() != null) {
            comment.setOwnerId(dto.getOwner().getId());
        }
        if (dto.getTarget() != null) {
            comment.setTargetId(dto.getTarget().getId());
        }
        
        taskCommentRepository.save(comment);
    }

    public List<TaskCommentDTO> getTaskComments(Long taskId) {
        List<TaskComment> comments = taskCommentRepository.findByTaskId(taskId);
        if (comments.isEmpty()) return Collections.emptyList();

        // 1. Map ID -> DTO tạm để lookup
        Map<Long, TaskCommentDTO> dtoMap = comments.stream()
                .map(this::convertCommentToDTO)
                .collect(Collectors.toMap(TaskCommentDTO::getId, c -> c));

        // 2. Link target (reply logic)
        List<TaskCommentDTO> result = new ArrayList<>();
        for (TaskComment c : comments) {
            TaskCommentDTO dto = dtoMap.get(c.getId());
            if (c.getTargetId() != null) {
                // Nếu comment này reply một comment khác, gán object target vào
                dto.setTarget(dtoMap.get(c.getTargetId()));
            }
            result.add(dto);
        }
        
        // 3. Sort theo thời gian
        result.sort(Comparator.comparingLong(TaskCommentDTO::getEpoch));
        return result;
    }

    /**
     * Hàm xóa toàn bộ comment của một Task (Dùng khi xóa Task cha).
     */
    @Transactional
    public void deleteCommentsByTaskId(Long taskId) {
        List<TaskComment> comments = taskCommentRepository.findByTaskId(taskId);
        if (!comments.isEmpty()) {
            taskCommentRepository.deleteAll(comments);
        }
    }

    // --- Helper Mappers ---
    private TaskCommentDTO convertCommentToDTO(TaskComment c) {
        TaskCommentDTO dto = new TaskCommentDTO();
        dto.setId(c.getId());
        dto.setTaskId(c.getTaskId());
        dto.setMessage(c.getMessage());
        dto.setTimestamp(c.getTimestamp());
        dto.setEpoch(c.getEpoch());
        
        if (c.getOwnerId() != null) {
            staffRepository.findById(c.getOwnerId())
                .ifPresent(s -> dto.setOwner(convertStaffToDTO(s)));
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