package tds.op.taskmanager.representation;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "task_events")
public class TaskEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
}
