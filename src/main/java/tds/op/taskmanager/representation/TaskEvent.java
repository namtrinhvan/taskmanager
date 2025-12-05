package tds.op.taskmanager.representation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
public class TaskEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long taskId;
    
    private String note; // Ghi chú về sự thay đổi (VD: "Gia hạn deadline")
    
    @Enumerated(EnumType.STRING)
    private TaskStatus prevStatus; // Trạng thái cũ
    
    @Enumerated(EnumType.STRING)
    private TaskStatus nextStatus; // Trạng thái mới
    
    /**
     * Thời điểm diễn ra sự kiện.
     * Dùng LocalDateTime để tiện xử lý hơn String trong project mới.
     */
    private LocalDateTime createdDate; 
    
    private Long createdBy; // ID của người thực hiện thay đổi (Staff.id)
}