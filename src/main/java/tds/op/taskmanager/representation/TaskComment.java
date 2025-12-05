package tds.op.taskmanager.representation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class TaskComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long taskId;
    
    /**
     * ID của người viết comment (Staff.id).
     */
    private Long ownerId; 
    
    /**
     * Nếu reply một comment khác, đây là ID của comment đó.
     * Nếu là comment gốc, giá trị này có thể null.
     */
    private Long targetId; 
    
    private String message;
    
    private String timestamp; // Format hiển thị (VD: "2023-10-20 14:30")
    
    private long epoch; // Dùng để sắp xếp thời gian chính xác (System.currentTimeMillis())
}