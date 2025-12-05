package tds.op.taskmanager.representation;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class UnitDTO {
    private Long id;
    private String name;
    private String head; // Email/Tên người đứng đầu
    private UnitLevel level;
    
    private Long parentUnitId; // ID cha
    
    // Danh sách con (Dùng để hiển thị TreeView)
    private List<UnitDTO> children = new ArrayList<>();
}