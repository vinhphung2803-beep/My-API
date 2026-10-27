package attendqr_api.controller;

import attendqr_api.entity.Employee;
import attendqr_api.repository.EmployeeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = {
    "https://vinhphung2803-beep.github.io",
    "http://localhost:5500",
    "http://127.0.0.1:5500",
    "http://localhost:8080"
})
public class EmployeeController {

    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Employee createEmployee(@RequestBody Employee employee) {
        validateEmployee(employee);

        if (employeeRepository.existsByEmployeeCodeIgnoreCase(employee.getEmployeeCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã nhân viên đã tồn tại.");
        }

        if (employee.getStatus() == null || employee.getStatus().isBlank()) {
            employee.setStatus("Đang làm việc");
        }

        if (employee.getCreatedAt() == null) {
            employee.setCreatedAt(OffsetDateTime.now());
        }

        return employeeRepository.save(employee);
    }

    @PutMapping("/{id}")
    public Employee updateEmployee(@PathVariable Integer id, @RequestBody Employee employee) {
        validateEmployee(employee);

        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy nhân viên."));

        if (employeeRepository.existsByEmployeeCodeIgnoreCaseAndIdNot(
                employee.getEmployeeCode(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã nhân viên đã tồn tại.");
        }

        existingEmployee.setFullName(employee.getFullName().trim());
        existingEmployee.setEmployeeCode(employee.getEmployeeCode().trim());
        existingEmployee.setGender(employee.getGender().trim());
        existingEmployee.setBirthDate(employee.getBirthDate());
        existingEmployee.setEmail(employee.getEmail().trim());
        existingEmployee.setPhone(employee.getPhone().trim());
        existingEmployee.setAddress(employee.getAddress());
        existingEmployee.setPosition(employee.getPosition().trim());
        existingEmployee.setDepartment(employee.getDepartment().trim());
        existingEmployee.setHourlyRate(employee.getHourlyRate());
        existingEmployee.setStartDate(employee.getStartDate());
        existingEmployee.setStatus(employee.getStatus() == null || employee.getStatus().isBlank()
                ? "Đang làm việc"
                : employee.getStatus().trim());

        return employeeRepository.save(existingEmployee);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEmployee(@PathVariable Integer id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy nhân viên."));

        try {
            employeeRepository.delete(employee);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa nhân viên vì đang có dữ liệu liên quan.",
                    exception);
        }
    }

    private void validateEmployee(Employee employee) {
        if (employee == null
                || employee.getFullName() == null || employee.getFullName().isBlank()
                || employee.getEmployeeCode() == null || employee.getEmployeeCode().isBlank()
                || employee.getGender() == null || employee.getGender().isBlank()
                || employee.getBirthDate() == null
                || employee.getEmail() == null || employee.getEmail().isBlank()
                || !employee.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || employee.getPhone() == null || employee.getPhone().isBlank()
                || employee.getPosition() == null || employee.getPosition().isBlank()
                || employee.getDepartment() == null || employee.getDepartment().isBlank()
                || employee.getHourlyRate() == null
                || employee.getHourlyRate().signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Vui lòng kiểm tra các thông tin nhân viên bắt buộc.");
        }

        employee.setFullName(employee.getFullName().trim());
        employee.setEmployeeCode(employee.getEmployeeCode().trim());
    }
}
