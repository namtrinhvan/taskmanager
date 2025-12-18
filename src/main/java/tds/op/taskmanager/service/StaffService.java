package tds.op.taskmanager.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tds.op.taskmanager.repository.StaffRepository;
import tds.op.taskmanager.representation.Staff;
import tds.op.taskmanager.representation.StaffDTO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StaffService {

    private final StaffRepository staffRepository;

    public StaffService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    /**
     * Tạo mới một nhân viên vào Database.
     */
    @Transactional
    public StaffDTO createStaff(StaffDTO dto) {
        // Có thể thêm validate email trùng ở đây nếu cần
        Staff staff = new Staff();
        staff.setName(dto.getName());
        staff.setEmail(dto.getEmail());
        staff.setPicture(dto.getPicture());

        Staff savedStaff = staffRepository.save(staff);
        return toDTO(savedStaff);
    }

    /**
     * Cập nhật thông tin nhân viên.
     */
    @Transactional
    public StaffDTO updateStaff(Long id, StaffDTO dto) {
        Optional<Staff> staffOpt = staffRepository.findById(id);
        if (staffOpt.isEmpty()) {
            throw new IllegalArgumentException("Staff not found with ID: " + id);
        }

        Staff staff = staffOpt.get();
        staff.setName(dto.getName());
        staff.setEmail(dto.getEmail());
        staff.setPicture(dto.getPicture());
        
        // Không set ID lại
        Staff updatedStaff = staffRepository.save(staff);
        return toDTO(updatedStaff);
    }

    /**
     * Lấy toàn bộ danh sách nhân viên trong hệ thống.
     */
    public List<StaffDTO> getAllStaff() {
        return staffRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Xóa nhân viên khỏi hệ thống (Cẩn thận: Cần xử lý ràng buộc khóa ngoại trước khi xóa).
     */
    @Transactional
    public void deleteStaff(Long id) {
        if(staffRepository.existsById(id)){
             staffRepository.deleteById(id);
        }
    }

    // --- Mapper ---
    private StaffDTO toDTO(Staff staff) {
        StaffDTO dto = new StaffDTO();
        dto.setId(staff.getId());
        dto.setName(staff.getName());
        dto.setEmail(staff.getEmail());
        dto.setPicture(staff.getPicture());
        return dto;
    }
}