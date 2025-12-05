package tds.op.taskmanager.representation;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String startMonth; // YYYY-MM
    private String endMonth;  // YYYY-MM
    private Long unitId;
}
