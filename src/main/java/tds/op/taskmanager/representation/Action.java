package tds.op.taskmanager.representation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long taskId; // Link với Task qua ID
    
    private String name;
    
    private String description;
    
    private LocalDate deadline; 
    
    @Enumerated(EnumType.STRING)
    private TaskStatus status; // PENDING, COMPLETED,...

    // KHÔNG lưu executorId ở đây nữa. 
    // Việc phân công sẽ nằm ở bảng ActionExecutor.

    // Thêm vào class Action
    private Boolean isDone = false; // Mặc định là chưa xong

}