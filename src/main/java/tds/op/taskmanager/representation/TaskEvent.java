package tds.op.taskmanager.representation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate; //

@Data
@Entity
public class TaskEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long taskId;
    
    private String note; 
    
    // --- STATUS CHANGE ---
    @Enumerated(EnumType.STRING)
    private TaskStatus prevStatus;
    
    @Enumerated(EnumType.STRING)
    private TaskStatus nextStatus;
    
    // --- DEADLINE CHANGE (New Logic) ---
    private LocalDate prevDeadline; // Deadline cũ
    private LocalDate nextDeadline; // Deadline mới
    
    private LocalDateTime createdDate; 
    
    private Long createdBy;
}