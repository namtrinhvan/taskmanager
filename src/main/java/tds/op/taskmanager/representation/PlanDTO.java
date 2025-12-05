package tds.op.taskmanager.representation;

import lombok.Data;

@Data
public class PlanDTO {
    private Long id;
    private String name;
    private String startMonth; // YYYY-MM
    private String endMonth;   // YYYY-MM
    
    // Trả về cả object Unit để hiển thị tên đơn vị sở hữu plan
    private UnitDTO unit; 
}