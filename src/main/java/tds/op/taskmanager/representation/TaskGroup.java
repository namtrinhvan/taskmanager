package tds.op.taskmanager.representation;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
/**
 * Class này đại diện cho một nhóm các task (các lặp lại của một hạng mục công việc)*/
public class TaskGroup {
    private String uuid;
    private String name;
    private String description;
    private List<TaskDTO> tasks = new ArrayList<>();
}
