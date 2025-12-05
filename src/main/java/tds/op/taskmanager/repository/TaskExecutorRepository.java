package tds.op.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tds.op.taskmanager.representation.TaskExecutor;

import java.util.List;

@Repository
public interface TaskExecutorRepository extends JpaRepository<TaskExecutor, Long> {
    public List<TaskExecutor> findByTaskId(Long taskId);

}
