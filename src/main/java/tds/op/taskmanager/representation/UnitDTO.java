package tds.op.taskmanager.representation;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UnitDTO {
    private long id;
    private String name;
    private String head; //Email của người đứng đầu
    private UnitLevel level;
    private List<UnitDTO> children = new ArrayList<>();
}
