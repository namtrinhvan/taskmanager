package tds.op.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tds.op.taskmanager.representation.TaskEvent;

import java.util.List;

@Repository
public interface TaskEventRepository extends JpaRepository<TaskEvent, Long> {
    // Tìm các sự kiện liên quan đến task cụ thể, sắp xếp mới nhất trước
    List<TaskEvent> findByTaskIdOrderByIdDesc(Long taskId);
}