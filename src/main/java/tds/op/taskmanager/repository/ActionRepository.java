package tds.op.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tds.op.taskmanager.representation.Action;

import java.util.List;

@Repository
public interface ActionRepository extends JpaRepository<Action, Long> {
    // Tìm các hành động thuộc về một task cụ thể
    List<Action> findByTaskId(Long taskId);

    @Query("SELECT a FROM Action a " +
            "JOIN ActionExecutor ae ON ae.actionId = a.id " +
            "WHERE ae.executorId = :staffId " + // Đã sửa thành executorId
            "ORDER BY a.deadline ASC")
    List<Action> findActionsByExecutor(@Param("staffId") Long staffId);
}