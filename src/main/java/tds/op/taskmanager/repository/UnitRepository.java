package tds.op.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tds.op.taskmanager.representation.Unit;

import java.util.List;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long
        > {
    public List<Unit> findByParentUnitId(Long parentUnitId);
}
