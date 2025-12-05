package tds.op.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tds.op.taskmanager.representation.Action;

import java.util.List;

@Repository
public interface ActionRepository extends JpaRepository<Action, Long> {
    // Tìm các hành động thuộc về một task cụ thể
    List<Action> findByTaskId(Long taskId);
}