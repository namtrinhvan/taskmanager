package tds.op.taskmanager.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tds.op.taskmanager.repository.*;
import tds.op.taskmanager.representation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {

    // --- Core Repositories ---
    private final TaskRepository taskRepository;
    private final TaskExecutorRepository taskExecutorRepository;
    private final TaskEventRepository taskEventRepository;
    private final StaffRepository staffRepository;
    private final UnitRepository unitRepository;
    private final PlanRepository planRepository;

    // --- Dependent Services (Để clean code) ---
    private final ActionService actionService;
    private final CommentService commentService;

    public TaskService(TaskRepository taskRepository, TaskExecutorRepository taskExecutorRepository, TaskEventRepository taskEventRepository, StaffRepository staffRepository, UnitRepository unitRepository, PlanRepository planRepository, ActionService actionService, CommentService commentService) {
        this.taskRepository = taskRepository;
        this.taskExecutorRepository = taskExecutorRepository;
        this.taskEventRepository = taskEventRepository;
        this.staffRepository = staffRepository;
        this.unitRepository = unitRepository;
        this.planRepository = planRepository;
        this.actionService = actionService;
        this.commentService = commentService;
    }

    // =========================================================================
    // TASK MANAGEMENT
    // =========================================================================

    @Transactional
    public TaskDTO createTask(TaskDTO dto) {
        Task task = new Task();

        // 1. Map fields cơ bản
        task.setName(dto.getName());
        task.setDescription(dto.getDescription());
        task.setStatus(TaskStatus.PENDING);
        task.setMonth(dto.getMonth());
        task.setProgress(0.0);

        // 2. Logic UUID (Grouping)
        if (dto.getUuid() == null || dto.getUuid().isEmpty()) {
            task.setUuid(UUID.randomUUID().toString());
        } else {
            task.setUuid(dto.getUuid());
        }

        // 3. Logic Dates
        task.setInitialStartDate(dto.getInitialStartDate());
        task.setActualStartDate(null);
        task.setInitialDeadline(dto.getInitialDeadline());
        task.setCurrentDeadline(dto.getInitialDeadline());

        // 4. Map IDs (Foreign Keys)
        if (dto.getPlan() != null) task.setPlanId(dto.getPlan().getId());
        if (dto.getAssigner() != null) task.setAssignerId(dto.getAssigner().getId());
        if (dto.getAssignee() != null) task.setAssigneeId(dto.getAssignee().getId());

        if (dto.getParentTask() != null && dto.getParentTask().getId() != null) {
            task.setParentTaskId(dto.getParentTask().getId());
        }

        // 5. Save Task
        Task savedTask = taskRepository.save(task);

        // 6. Save Executors
        if (dto.getExecutors() != null && !dto.getExecutors().isEmpty()) {
            List<TaskExecutor> executors = new ArrayList<>();
            for (StaffDTO staff : dto.getExecutors()) {
                TaskExecutor te = new TaskExecutor();
                te.setTaskId(savedTask.getId());
                te.setExecutorId(staff.getId());
                executors.add(te);
            }
            taskExecutorRepository.saveAll(executors);
        }
        return getTaskDetail(savedTask.getId());
    }
// =========================================================================
    // GET TASKS BY PLAN (GROUPED BY UUID)
    // =========================================================================

    /**
     * Lấy danh sách Task thuộc về một Plan cụ thể.
     * Logic mới:
     * - Lấy tất cả task có planId tương ứng.
     * - Gom nhóm các task này dựa trên UUID (đại diện cho một đầu việc lặp lại).
     * - Trả về danh sách TaskGroup, mỗi group chứa thông tin chung và list các task con (các tháng).
     */
    public List<TaskGroup> getTasksByPlan(Long planId) {
        if (planId == null) {
            return Collections.emptyList();
        }

        // 1. Lấy danh sách Task từ DB
        List<Task> tasks = taskRepository.findByPlanId(planId);

        if (tasks.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Gom nhóm theo UUID
        // Map<String (uuid), List<Task>>
        Map<String, List<Task>> groupedByUuid = tasks.stream().collect(Collectors.groupingBy(Task::getUuid));

        // 3. Map sang List<TaskGroup>
        List<TaskGroup> result = new ArrayList<>();

        groupedByUuid.forEach((uuid, taskList) -> {
            if (taskList.isEmpty()) return;

            // Lấy thông tin chung từ phần tử đầu tiên trong nhóm
            // (Giả định rằng name và description của các task cùng UUID là giống nhau hoặc tương tự)
            Task firstTask = taskList.get(0);

            TaskGroup group = new TaskGroup();
            group.setUuid(uuid);
            group.setName(firstTask.getName());
            group.setDescription(firstTask.getDescription());

            // Convert list entity -> list DTO
            List<TaskDTO> taskDTOs = taskList.stream().map(this::convertToDTO) // Sử dụng hàm convert đã có logic tính progress & executors
                    .sorted(Comparator.comparing(t -> {
                        // Sort các task trong nhóm theo tháng (YYYY-MM)
                        try {
                            return YearMonth.parse(t.getMonth());
                        } catch (Exception e) {
                            // Fallback nếu format lỗi, đẩy xuống cuối
                            return YearMonth.now().plusYears(100);
                        }
                    })).collect(Collectors.toList());

            group.setTasks(taskDTOs);
            result.add(group);
        });

        // 4. Sắp xếp danh sách Group theo tên A-Z để dễ nhìn
        result.sort(Comparator.comparing(TaskGroup::getName));

        return result;
    }

    public TaskDTO getTaskDetail(Long taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) return null;

        Task task = taskOpt.get();
        TaskDTO dto = convertToDTO(task); // Map thông tin cơ bản và executors

        // Lấy Task con (Logic 1-1)
        List<Task> children = taskRepository.findByParentTaskId(taskId);
        if (!children.isEmpty()) {
            Task child = children.get(0);
            dto.setChildTask(convertToDTO(child));
        }

        // NOTE: Chúng ta KHÔNG set actions hay comments vào TaskDTO ở đây
        // vì TaskDTO không chứa list Action/Comment.
        // FE sẽ gọi API riêng của ActionService và CommentService để lấy dữ liệu đó.

        return dto;
    }

    public List<TaskGroup> getTasksByPlanAndUnit(Long planId, Long unitId) {
        if (planId == null || unitId == null) return Collections.emptyList();

        List<Task> tasks = taskRepository.findRootTasksByPlanAndUnit(planId, unitId);
        if (tasks.isEmpty()) return Collections.emptyList();

        Map<String, List<Task>> groupedByUuid = tasks.stream().collect(Collectors.groupingBy(Task::getUuid));

        List<TaskGroup> result = new ArrayList<>();

        groupedByUuid.forEach((uuid, taskList) -> {
            if (taskList.isEmpty()) return;
            Task firstTask = taskList.get(0);

            TaskGroup group = new TaskGroup();
            group.setUuid(uuid);
            group.setName(firstTask.getName());
            group.setDescription(firstTask.getDescription());

            List<TaskDTO> taskDTOs = taskList.stream().map(this::convertToDTO).sorted(Comparator.comparing(t -> {
                try {
                    return YearMonth.parse(t.getMonth());
                } catch (Exception e) {
                    return YearMonth.now();
                }
            })).collect(Collectors.toList());

            group.setTasks(taskDTOs);
            result.add(group);
        });

        result.sort(Comparator.comparing(TaskGroup::getName));
        return result;
    }

    @Transactional
    public void deleteTask(Long taskId) {
        if (taskRepository.existsById(taskId)) {
            // 1. Dùng ActionService để xóa sạch Actions liên quan
            actionService.deleteActionsByTaskId(taskId);

            // 2. Dùng CommentService để xóa sạch Comments liên quan
            commentService.deleteCommentsByTaskId(taskId);

            // 3. Xóa Executor links của Task
            List<TaskExecutor> executors = taskExecutorRepository.findByTaskId(taskId);
            taskExecutorRepository.deleteAll(executors);

            // 4. Xóa TaskEvents (Nếu cần thiết, hoặc để lại làm lịch sử Audit)
            List<TaskEvent> events = taskEventRepository.findByTaskIdOrderByIdDesc(taskId);
            taskEventRepository.deleteAll(events);

            // 5. Cuối cùng xóa Task
            taskRepository.deleteById(taskId);
        }
    }

    @Transactional
    public void addExecutorsToTask(Long taskId, List<Long> staffIds) {
        if (taskId == null || staffIds == null || staffIds.isEmpty()) return;
        if (!taskRepository.existsById(taskId)) return;

        List<TaskExecutor> current = taskExecutorRepository.findByTaskId(taskId);
        Set<Long> existingIds = current.stream().map(TaskExecutor::getExecutorId).collect(Collectors.toSet());

        List<TaskExecutor> newLinks = new ArrayList<>();
        for (Long sid : staffIds) {
            if (!existingIds.contains(sid) && staffRepository.existsById(sid)) {
                TaskExecutor te = new TaskExecutor();
                te.setTaskId(taskId);
                te.setExecutorId(sid);
                newLinks.add(te);
            }
        }
        if (!newLinks.isEmpty()) {
            taskExecutorRepository.saveAll(newLinks);
        }
    }

    public List<TaskEventDTO> getTaskEvents(Long taskId) {
        return taskEventRepository.findByTaskIdOrderByIdDesc(taskId).stream().map(e -> {
            TaskEventDTO dto = new TaskEventDTO();
            dto.setId(e.getId());
            dto.setTaskId(e.getTaskId());
            dto.setNote(e.getNote());
            dto.setPrevStatus(e.getPrevStatus());
            dto.setNextStatus(e.getNextStatus());
            dto.setCreatedDate(e.getCreatedDate());

            if (e.getCreatedBy() != null) {
                staffRepository.findById(e.getCreatedBy()).ifPresent(s -> dto.setCreatedBy(convertStaffToDTO(s)));
            }
            return dto;
        }).collect(Collectors.toList());
    }

    // =========================================================================
    // ASSEMBLER / MAPPERS
    // =========================================================================
    @Transactional
    public void updateTaskProgress(Long taskId, double progress) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            // Validate progress trong khoảng 0.0 - 1.0 (hoặc 0-100 tùy quy ước của bạn)
            if (progress < 0) progress = 0;
            if (progress > 1) progress = 1;

            task.setProgress(progress);
            taskRepository.save(task);
        }
    }

    private StaffDTO convertStaffToDTO(Staff s) {
        StaffDTO dto = new StaffDTO();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setEmail(s.getEmail());
        dto.setPicture(s.getPicture());
        return dto;
    }

    private UnitDTO convertUnitToDTO(Unit u) {
        UnitDTO dto = new UnitDTO();
        dto.setId(u.getId());
        dto.setName(u.getName());
        dto.setHead(u.getHead());
        dto.setLevel(u.getLevel());
        return dto;
    }

    // Hàm log tổng quát (Status hoặc Deadline đều dùng được)
    private void logTaskEvent(Long taskId, String note, TaskStatus prevS, TaskStatus nextS, LocalDate prevD, LocalDate nextD, Long staffId) {
        TaskEvent event = new TaskEvent();
        event.setTaskId(taskId);
        event.setNote(note);
        event.setPrevStatus(prevS);
        event.setNextStatus(nextS);

        // Set deadline changes
        event.setPrevDeadline(prevD);
        event.setNextDeadline(nextD);

        event.setCreatedDate(LocalDateTime.now());
        event.setCreatedBy(staffId);
        taskEventRepository.save(event);
    }

    // Hàm nghiệp vụ: Gia hạn Deadline
    @Transactional
    public void extendDeadline(Long taskId, LocalDate newDeadline, String reason, Long staffId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            LocalDate oldDeadline = task.getCurrentDeadline();

            // Cập nhật Task
            task.setCurrentDeadline(newDeadline);
            taskRepository.save(task);

            // Ghi Log: Status giữ nguyên (null), Deadline thay đổi
            logTaskEvent(taskId, reason, null, null, oldDeadline, newDeadline, staffId);
        }
    }

    // =========================================================================
    // UPDATE: GET ASSIGNMENT CHAIN (ACCOUNT FOR EXECUTORS)
    // =========================================================================

    /**
     * Lấy toàn bộ chuỗi giao việc.
     * Logic:
     * 1. Leo ngược lên tìm Root Task.
     * 2. Đệ quy xuống dưới để tìm Task con.
     * 3. Tại mỗi cấp, load luôn Executors (vì cấp cuối cùng sẽ giao cho Executors).
     */
    public TaskDTO getAssignmentChain(Long taskId) {
        // 1. Kiểm tra Task hiện tại có tồn tại không
        Optional<Task> currentOpt = taskRepository.findById(taskId);
        if (currentOpt.isEmpty()) return null;

        Task current = currentOpt.get();

        // 2. Tìm Task Gốc (Root Node) - Leo ngược lên trên
        Set<Long> visited = new HashSet<>();
        visited.add(current.getId());

        while (current.getParentTaskId() != null && current.getParentTaskId() != 0) {
            Optional<Task> parentOpt = taskRepository.findById(current.getParentTaskId());
            if (parentOpt.isEmpty()) break;

            current = parentOpt.get();
            // Phòng ngừa dữ liệu bị vòng lặp (A -> B -> A)
            if (!visited.add(current.getId())) break;
        }

        // Lúc này 'current' là Root Task. Convert sang DTO (đã bao gồm Executors của Root nếu có)
        TaskDTO rootDto = convertToDTO(current);

        // 3. Đệ quy xuôi dòng để lấy toàn bộ chuỗi con cháu
        populateChildRecursively(rootDto);

        return rootDto;
    }

    /**
     * Hàm đệ quy tìm Task con.
     * QUAN TRỌNG: Hàm này sử dụng convertToDTO, mà convertToDTO đã có logic lấy Executors.
     * Do đó, nếu Task con là điểm cuối (được giao cho nhân viên), field 'executors' của nó sẽ có dữ liệu.
     */
    private void populateChildRecursively(TaskDTO parentDto) {
        // Tìm task con (Theo logic 1-1: Một task cha giao xuống chỉ tạo 1 task con đại diện)
        List<Task> children = taskRepository.findByParentTaskId(parentDto.getId());

        if (!children.isEmpty()) {
            Task childEntity = children.get(0);

            // Convert sang DTO (Hàm này sẽ tự động query bảng TaskExecutor để lấy người thực hiện)
            TaskDTO childDto = convertToDTO(childEntity);

            // Gán vào cha
            parentDto.setChildTask(childDto);

            // Tiếp tục đệ quy (Nếu task con này lại giao tiếp cho unit cháu)
            populateChildRecursively(childDto);
        }
        // Nếu children empty -> Đây là nút lá về mặt Task.
        // Dữ liệu Executors đã có trong parentDto (do convertToDTO đã làm việc này).
    }

    // =========================================================================
    // CORE ASSEMBLER (Cần đảm bảo logic lấy Executors ở đây)
    // =========================================================================

    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();

        // --- 1. Map Basic Fields (Cũ + Mới) ---
        dto.setId(task.getId());
        dto.setUuid(task.getUuid());        // Mới
        dto.setName(task.getName());
        dto.setDescription(task.getDescription());
        dto.setMonth(task.getMonth());
        dto.setStatus(task.getStatus());
        dto.setProgress(task.getProgress()); // Mới

        // --- 2. Map Dates (Logic mới - Quản lý chặt chẽ) ---
        dto.setInitialStartDate(task.getInitialStartDate());
        dto.setActualStartDate(task.getActualStartDate());
        dto.setInitialDeadline(task.getInitialDeadline());
        dto.setCurrentDeadline(task.getCurrentDeadline());
        dto.setEndDate(task.getEndDate());
// --- START LOGIC MỚI VỀ PROGRESS ---
        // Gọi ActionService để xem có progress tính toán từ Action không
        Double calculatedProgress = actionService.calculateProgress(task.getId());

        if (calculatedProgress != null) {
            // Trường hợp 1: Có Action -> Dùng tiến độ tính toán (Ghi đè)
            dto.setProgress(calculatedProgress);
        } else {
            // Trường hợp 2: Không có Action -> Dùng tiến độ update thủ công trong DB
            dto.setProgress(task.getProgress());
        }
        // --- END LOGIC MỚI ---
        // --- 3. Map Plan (Logic cũ - Reference) ---
        if (task.getPlanId() != null) {
            planRepository.findById(task.getPlanId()).ifPresent(p -> {
                PlanDTO pDto = new PlanDTO();
                pDto.setId(p.getId());
                pDto.setName(p.getName());
                pDto.setStartMonth(p.getStartMonth());
                pDto.setEndMonth(p.getEndMonth());
                // (Optional) Có thể map thêm Unit cho Plan nếu cần
                dto.setPlan(pDto);
            });
        }

        // --- 4. Map Assigner / Unit Giao (Logic cũ) ---
        if (task.getAssignerId() != null) {
            unitRepository.findById(task.getAssignerId()).ifPresent(u -> dto.setAssigner(convertUnitToDTO(u)));
        }

        // --- 5. Map Assignee / Unit Nhận (Logic cũ) ---
        if (task.getAssigneeId() != null) {
            unitRepository.findById(task.getAssigneeId()).ifPresent(u -> dto.setAssignee(convertUnitToDTO(u)));
        }

        // --- 6. Map Parent Task (Logic cũ - Chỉ lấy Info cơ bản để tránh đệ quy ngược) ---
        if (task.getParentTaskId() != null) {
            taskRepository.findById(task.getParentTaskId()).ifPresent(parent -> {
                TaskDTO pDto = new TaskDTO();
                pDto.setId(parent.getId());
                pDto.setName(parent.getName());
                // Không map sâu hơn để tránh loop
                dto.setParentTask(pDto);
            });
        }

        // --- 7. Map Executors (QUAN TRỌNG CHO LOGIC CHUỖI GIAO VIỆC) ---
        // Tại bất kỳ cấp nào (đặc biệt là cấp cuối), ta luôn kiểm tra xem có nhân viên nào
        // được gán trực tiếp vào Task này không.
        List<TaskExecutor> links = taskExecutorRepository.findByTaskId(task.getId());
        if (!links.isEmpty()) {
            List<Long> staffIds = links.stream().map(TaskExecutor::getExecutorId).collect(Collectors.toList());

            List<Staff> staffs = staffRepository.findAllById(staffIds);

            dto.setExecutors(staffs.stream().map(this::convertStaffToDTO).collect(Collectors.toList()));
        }

        return dto;
    }
}