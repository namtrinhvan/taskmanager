package tds.op.taskmanager.representation;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     Do mỗi hạng mục công việc có thể lặp lại qua các tháng hoặc qua các đơn vị khác nhau,
     nên các record Task có uuid giống nhau sẽ được ngầm hiểu semantically là một hạng mục công việc được lặp lại.
     * */
    private String uuid;
    private String name;
    private String description;
    private String month; //YYYY-MM;
    private LocalDate initialStartDate; //Ngày bắt đầu dự kiến. //Là ngày mà người tạo task mong muốn nó bắt đầu.
    private LocalDate actualStartDate; //Ngày bắt đầu thực tế. Là ngày người dùng chuyển trạng thái sang IN_PROGRESS lần đầu tiên.
    private LocalDate initialDeadline; //Deadline ban đầu, được tạo ra khi tạo task hoặc khi người tạo task chỉnh sửa.
    private LocalDate currentDeadline; //Có thể kéo dài deadline, do vậy sẽ được lưu ở đây.
    private LocalDate endDate; //Ngày kết thúc. Sẽ được set trong logic khi chuyển trạng thái sang COMPLETED
    private Long planId; //ID của kế hoạch.
    private Long parentTaskId; //ID của record cha.
    private Long assignerId;
    private Long assigneeId;
    private TaskStatus status;
    private double progress;
}
