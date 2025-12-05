package tds.op.taskmanager.representation;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskEventDTO {
    private Long id;
    private Long taskId;
    private String note;
    
    private TaskStatus prevStatus;
    private TaskStatus nextStatus;
    
    private LocalDateTime createdDate;
    
    // Thông tin người tạo sự kiện (Audit)
    private StaffDTO createdBy; 
}