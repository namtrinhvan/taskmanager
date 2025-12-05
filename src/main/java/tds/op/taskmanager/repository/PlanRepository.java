package tds.op.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tds.op.taskmanager.representation.Plan;

import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByUnitId(Long unitId);
    /**
     * Tìm tất cả Plan mà Unit này được giao ít nhất 1 task (là assignee).
     * Logic: Select Plan P where P.id nằm trong danh sách (planId của các Task có assignee = unitId)
     */
    @Query("SELECT DISTINCT p FROM Plan p WHERE p.id IN (SELECT t.planId FROM Task t WHERE t.assigneeId = :unitId)")
    List<Plan> findPlansAsMember(@Param("unitId") Long unitId);
}
