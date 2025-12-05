package tds.op.taskmanager.representation;

import lombok.Data;

@Data
public class TaskCommentDTO {
    private Long id;
    private Long taskId;
    private String message;
    
    private String timestamp; // String format sẵn cho FE
    private long epoch;       // Long để sort
    
    // Người comment
    private StaffDTO owner;
    
    // Reply comment nào (nếu có)
    private TaskCommentDTO target; 
}