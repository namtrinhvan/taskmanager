package tds.op.taskmanager.representation;

import lombok.Data;

@Data
public class StaffDTO {
    private Long id;
    private String name;
    private String email;
    private String picture;
}