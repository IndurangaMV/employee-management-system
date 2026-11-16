package com.example.employeemanger.Controller;

import com.example.employeemanger.Entity.Employee;
import com.example.employeemanger.Service.EmployeeService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    @GetMapping("/")
    public String home() {
        return "redirect:/employees";
    }
    @GetMapping("/employees")
    public String listEmployees(Model model) {

        List<Employee> employees = employeeService.getAllEmployees();

        model.addAttribute("employees", employees);
        model.addAttribute("employee", new Employee());

        return "employees";
    }
    @PostMapping("/employees")
    public String saveEmployee(
            @Valid @ModelAttribute("employee") Employee employee,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Check whether the email address is already registered
        if (!result.hasErrors() &&
                employeeService.existsByEmail(employee.getEmail())) {

            result.rejectValue(
                    "email",
                    "duplicate",
                    "This email is already registered"
            );
        }

        // If validation fails, return to the employee page
        if (result.hasErrors()) {
            model.addAttribute("employees", employeeService.getAllEmployees());
            return "employees";
        }

        // Save the employee
        employeeService.saveEmployee(employee);

        // Display a success message after redirecting
        redirectAttributes.addFlashAttribute(
                "success",
                "Employee saved successfully"
        );

        return "redirect:/employees";
    }
    @GetMapping("/employees/{id}/edit")
    public String editEmployee(@PathVariable Long id, Model model) {

        Optional<Employee> employee = employeeService.getEmployeeById(id);

        if (employee.isPresent()) {
            model.addAttribute("employee", employee.get());
            model.addAttribute("employees", employeeService.getAllEmployees());

            return "edit-employee";
        }

        return "redirect:/employees";
    }
    @PostMapping("/employees/{id}/update")
    public String updateEmployee(
            @PathVariable Long id,
            @Valid @ModelAttribute("employee") Employee employee,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        Optional<Employee> existingEmployee = employeeService.getEmployeeById(id);

        if (existingEmployee.isEmpty()) {
            return "redirect:/employees";
        }

        // Check whether another employee already uses this email
        if (!result.hasErrors()) {
            Optional<Employee> employeeWithEmail =
                    employeeService.findByEmail(employee.getEmail());

            if (employeeWithEmail.isPresent() &&
                    !employeeWithEmail.get().getId().equals(id)) {

                result.rejectValue(
                        "email",
                        "duplicate",
                        "This email is already registered"
                );
            }
        }

        if (result.hasErrors()) {
            employee.setId(id);
            return "edit-employee";
        }

        employee.setId(id);

        employeeService.saveEmployee(employee);

        redirectAttributes.addFlashAttribute(
                "success",
                "Employee updated successfully"
        );

        return "redirect:/employees";
    }
    @PostMapping("/employees/{id}/delete")
    public String deleteEmployee(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        Optional<Employee> employee = employeeService.getEmployeeById(id);

        if (employee.isPresent()) {
            employeeService.deleteEmployee(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Employee deleted successfully"
            );
        } else {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Employee not found"
            );
        }

        return "redirect:/employees";
    }
}