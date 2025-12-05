package tds.op.taskmanager.representation;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate; //

@Data
public class TaskEventDTO {
    private Long id;
    private Long taskId;
    private String note;
    
    private TaskStatus prevStatus;
    private TaskStatus nextStatus;
    
    // --- Fields mới ---
    private LocalDate prevDeadline;
    private LocalDate nextDeadline;
    
    private LocalDateTime createdDate;
    
    private StaffDTO createdBy; 
}