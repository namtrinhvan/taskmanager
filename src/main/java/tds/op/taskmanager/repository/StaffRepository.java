package tds.op.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tds.op.taskmanager.representation.Staff;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
}
