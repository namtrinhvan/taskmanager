package tds.op.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tds.op.taskmanager.representation.ActionExecutor;

import java.util.List;

@Repository
public interface ActionExecutorRepository extends JpaRepository<ActionExecutor, Long> {

    /**
     * Tìm tất cả các record phân công (executor links) cho một Action cụ thể.
     * Dùng để lấy danh sách staffId khi hiển thị chi tiết Action.
     */
    List<ActionExecutor> findByActionId(Long actionId);

    /**
     * (Optional) Tìm tất cả các Action mà một Staff cụ thể tham gia.
     * Có thể hữu ích nếu sau này bạn muốn hiển thị "Việc tôi cần làm" ở cấp độ Action.
     */
    List<ActionExecutor> findByExecutorId(Long executorId);
}