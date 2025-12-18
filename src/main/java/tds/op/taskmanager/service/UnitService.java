package tds.op.taskmanager.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tds.op.taskmanager.repository.*;
import tds.op.taskmanager.representation.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UnitService {

    private final StaffRepository staffRepository;
    private final UnitStaffRepository unitStaffRepository;
    private final UnitRepository unitRepository;

    public UnitService(StaffRepository staffRepository, UnitStaffRepository unitStaffRepository, UnitRepository unitRepository) {
        this.staffRepository = staffRepository;
        this.unitStaffRepository = unitStaffRepository;
        this.unitRepository = unitRepository;
    }

    /**
     * Create a unit or a full unit tree.
     * parentUnitId = null or 0 => root unit.
     * Updated: Supports recursive creation of children.
     */
    @Transactional(rollbackFor = Exception.class)
    public UnitDTO createUnit(UnitDTO dto, Long parentUnitId) {
        if (dto == null) return null;

        // 1. Save the current Unit
        Unit u = new Unit();
        u.setName(dto.getName());
        u.setHead(dto.getHead());
        u.setLevel(dto.getLevel());
        u.setParentUnitId(parentUnitId == null ? 0L : parentUnitId);

        unitRepository.save(u);

        // Reflect persisted id back to DTO
        dto.setId(u.getId());

        // 2. Recursively save children if they exist
        if (dto.getChildren() != null && !dto.getChildren().isEmpty()) {
            for (UnitDTO childDto : dto.getChildren()) {
                // The parent of the child is the current unit 'u'
                createUnit(childDto, u.getId());
            }
        }

        return dto;
    }

    /**
     * Return the entire corporate structure as a list of root UnitDTO trees.
     * parentUnitId == 0 means root.
     */
    public List<UnitDTO> getCorporateStructure() {
        List<Unit> all = unitRepository.findAll();
        return buildTree(all);
    }

    /**
     * Return tree starting from the given root unit id.
     */
    public UnitDTO getStructure(long rootId) {
        Optional<Unit> rootOpt = unitRepository.findById(rootId);
        if (rootOpt.isEmpty()) return null;

        List<Unit> all = unitRepository.findAll();
        Map<Long, List<Unit>> childrenMap = all.stream().filter(u -> u.getParentUnitId() != 0L).collect(Collectors.groupingBy(Unit::getParentUnitId));

        return toTree(rootOpt.get(), childrenMap);
    }

    /**
     * Delete unit if it has no child units and no staff links. Returns true if deleted.
     */
    @Transactional
    public boolean deleteUnit(long unitId) {
        Optional<Unit> opt = unitRepository.findById(unitId);
        if (opt.isEmpty()) return false;

        // refuse to delete if there are children
        List<Unit> children = unitRepository.findByParentUnitId(unitId);
        if (children != null && !children.isEmpty()) return false;

        // refuse delete if there are staff assigned
        List<UnitStaff> links = unitStaffRepository.findByUnitId(unitId);
        if (links != null && !links.isEmpty()) return false;

        unitRepository.delete(opt.get());
        return true;
    }

    /**
     * Get direct staff of a unit (not recursive).
     */
    public List<StaffDTO> getUnitStaff(long unitId) {
        List<UnitStaff> links = unitStaffRepository.findByUnitId(unitId);
        if (links == null || links.isEmpty()) return Collections.emptyList();

        return links.stream().map(UnitStaff::getStaffId).map(staffRepository::findById).filter(Optional::isPresent).map(Optional::get).map(this::toStaffDTO).collect(Collectors.toList());
    }

    /**
     * Get direct child units (one level) as DTOs.
     */
    public List<UnitDTO> getUnitChildren(long unitId) {
        List<Unit> children = unitRepository.findByParentUnitId(unitId);
        if (children == null || children.isEmpty()) return Collections.emptyList();

        return children.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Get ALL staff under a unit (including its own staff and staff in all descendant units).
     */
    public List<StaffDTO> getAllStaffUnderUnit(long rootUnitId) {

        // 1. Build a map parent -> children for fast traversal
        List<Unit> allUnits = unitRepository.findAll();
        Map<Long, List<Unit>> childrenMap = allUnits.stream().filter(u -> u.getParentUnitId() != 0).collect(Collectors.groupingBy(Unit::getParentUnitId));

        // 2. Collect all unit IDs under the root
        Set<Long> unitIds = new HashSet<>();
        collectUnitIdsRecursively(rootUnitId, childrenMap, unitIds);

        // Also include the root itself
        unitIds.add(rootUnitId);

        // 3. Query staff links for all units
        List<UnitStaff> links = unitStaffRepository.findByUnitIdIn(unitIds);

        if (links == null || links.isEmpty()) return Collections.emptyList();

        // 4. Convert each staff to DTO
        return links.stream().map(UnitStaff::getStaffId).map(staffRepository::findById).filter(Optional::isPresent).map(Optional::get).map(this::toStaffDTO).collect(Collectors.toList());
    }

    // -------------------------
    // Helper / mapping methods
    // -------------------------
    private StaffDTO toStaffDTO(Staff s) {
        StaffDTO dto = new StaffDTO();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setEmail(s.getEmail());
        dto.setPicture(s.getPicture());
        return dto;
    }

    private UnitDTO toDTO(Unit u) {
        UnitDTO dto = new UnitDTO();
        dto.setId(u.getId());
        dto.setName(u.getName());
        dto.setHead(u.getHead());
        dto.setLevel(u.getLevel());
        // children left empty here; used when building tree manually or returning simple DTO
        return dto;
    }

    /**
     * Build full tree(s) from flat list. Roots are units with parentUnitId == 0.
     */
    private List<UnitDTO> buildTree(List<Unit> allUnits) {
        Map<Long, List<Unit>> childrenMap = allUnits.stream().filter(u -> u.getParentUnitId() != 0L).collect(Collectors.groupingBy(Unit::getParentUnitId));

        List<Unit> roots = allUnits.stream().filter(u -> u.getParentUnitId() == 0L).toList();

        return roots.stream().map(r -> toTree(r, childrenMap)).collect(Collectors.toList());
    }

    private UnitDTO toTree(Unit root, Map<Long, List<Unit>> childrenMap) {
        UnitDTO dto = toDTO(root);
        List<Unit> children = childrenMap.get(root.getId());
        if (children != null) {
            for (Unit c : children) {
                dto.getChildren().add(toTree(c, childrenMap));
            }
        }
        return dto;
    }

    /**
     * DFS to collect unit IDs recursively.
     */
    private void collectUnitIdsRecursively(long currentId, Map<Long, List<Unit>> childrenMap, Set<Long> result) {
        List<Unit> children = childrenMap.get(currentId);
        if (children == null) return;

        for (Unit child : children) {
            result.add(child.getId());
            collectUnitIdsRecursively(child.getId(), childrenMap, result);
        }
    }

    /**
     * Thêm danh sách nhân viên vào Unit.
     * Bỏ qua nếu nhân viên đó đã có trong Unit rồi (tránh trùng lặp).
     */
    @Transactional
    public void addStaffToUnit(Long unitId, List<Long> staffIds) {
        // 1. Kiểm tra Unit có tồn tại không
        if (!unitRepository.existsById(unitId)) {
            throw new IllegalArgumentException("Unit not found with ID: " + unitId);
        }

        // 2. Lấy danh sách nhân viên hiện tại của Unit để check trùng
        List<UnitStaff> currentLinks = unitStaffRepository.findByUnitId(unitId);
        Set<Long> existingStaffIds = currentLinks.stream()
                .map(UnitStaff::getStaffId)
                .collect(Collectors.toSet());

        // 3. Duyệt danh sách ID gửi lên
        List<UnitStaff> newLinks = new ArrayList<>();
        for (Long staffId : staffIds) {
            // Nếu chưa tồn tại trong Unit này và Staff ID hợp lệ (có trong DB Staff)
            if (!existingStaffIds.contains(staffId) && staffRepository.existsById(staffId)) {
                UnitStaff link = new UnitStaff();
                link.setUnitId(unitId);
                link.setStaffId(staffId);
                newLinks.add(link);
            }
        }

        // 4. Lưu batch
        if (!newLinks.isEmpty()) {
            unitStaffRepository.saveAll(newLinks);
        }
    }
}