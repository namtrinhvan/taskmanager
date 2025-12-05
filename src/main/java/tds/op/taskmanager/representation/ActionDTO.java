package tds.op.taskmanager.representation;

import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ActionDTO {
    private Long id;
    private Long taskId;
    private String name;
    private String description;
    private LocalDate deadline;
    private TaskStatus status;

    // Logic mới: Một action có thể có nhiều người làm
    private List<StaffDTO> executors = new ArrayList<>(); 
}