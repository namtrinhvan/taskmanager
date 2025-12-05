package tds.op.taskmanager.representation;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class TaskDTO {
    private Long id;

    // --- Logic mới: Grouping ---
    private String uuid; // Định danh nhóm công việc lặp lại

    private String name;
    private String description;
    private String month; // YYYY-MM

    // --- Logic mới: Quản lý thời gian chặt chẽ ---
    private LocalDate initialStartDate; // Kế hoạch
    private LocalDate actualStartDate;  // Thực tế

    private LocalDate initialDeadline;  // Deadline gốc
    private LocalDate currentDeadline;  // Deadline hiện tại (sau gia hạn)
    private LocalDate endDate;          // Ngày hoàn thành

    private TaskStatus status;

    // --- References (Dạng Object để hiển thị tên/ảnh) ---
    private PlanDTO plan;
    private TaskDTO parentTask; // Nếu là sub-task

    private UnitDTO assigner; // Đơn vị giao
    private UnitDTO assignee; // Đơn vị nhận

    // --- Collections ---
    // Danh sách người thực hiện (Lấy từ bảng TaskExecutor)
    private List<StaffDTO> executors = new ArrayList<>();

    //Vì một task cha chỉ được tạo một task con duy nhất.
    private TaskDTO childTask;
    private double progress; //Tiến độ.
}