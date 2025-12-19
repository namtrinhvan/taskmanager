package tds.op.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tds.op.taskmanager.representation.Task;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByPlanId(Long planId);
    List<Task> findByPlanIdAndAssigneeId(Long planId, Long assigneeId);
    List<Task> findByParentTaskId(Long parentTaskId);

    // --- FIX LOGIC: Chỉ lấy Task "gốc" của Unit (loại bỏ các task con nội bộ) ---
    // Logic: Lấy Task thuộc Unit này VÀ (Không có cha HOẶC Cha của nó KHÔNG thuộc Unit này)
    @Query("SELECT t FROM Task t WHERE t.planId = :planId AND t.assigneeId = :unitId " +
           "AND (t.parentTaskId IS NULL OR t.parentTaskId NOT IN " +
           "(SELECT pt.id FROM Task pt WHERE pt.assigneeId = :unitId))")
    List<Task> findRootTasksByPlanAndUnit(@Param("planId") Long planId, @Param("unitId") Long unitId);

    @Query("SELECT t FROM Task t " +
            "JOIN TaskExecutor te ON te.taskId = t.id " +
            "WHERE te.executorId = :staffId " + // Đã sửa thành executorId
            "ORDER BY t.currentDeadline ASC") // Sắp xếp theo currentDeadline thay vì deadline cũ
    List<Task> findTasksByExecutor(@Param("staffId") Long staffId);
}