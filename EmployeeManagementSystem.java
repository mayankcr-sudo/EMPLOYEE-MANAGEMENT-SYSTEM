import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;

/**
 * Console-based Employee Management System.
 * Data structure: custom singly linked list plus HashMap index by employee ID.
 * Persistence: employees.txt in the program's current working directory.
 */
public class EmployeeManagementSystem {
    private static final Path DATA_FILE = Paths.get("employees.txt");
    private static final Scanner INPUT = new Scanner(System.in);

    public static void main(String[] args) {
        EmployeeLinkedList employees = new EmployeeLinkedList();
        loadEmployees(employees, DATA_FILE);

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readMenuChoice();

            switch (choice) {
                case 1:
                    addEmployee(employees);
                    saveEmployees(employees, DATA_FILE);
                    break;
                case 2:
                    employees.displayAll();
                    break;
                case 3:
                    searchById(employees);
                    break;
                case 4:
                    deleteEmployee(employees);
                    saveEmployees(employees, DATA_FILE);
                    break;
                case 5:
                    employees.sortBySalaryDescending();
                    System.out.println("Employees sorted by salary from high to low.");
                    employees.displaySortedBySalary();
                    saveEmployees(employees, DATA_FILE);
                    break;
                case 6:
                    searchByName(employees);
                    break;
                case 7:
                    saveEmployees(employees, DATA_FILE);
                    System.out.println("Records saved. Exiting Employee Management System.");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Enter a number from 1 to 7.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n====================================");
        System.out.println("      EMPLOYEE MANAGEMENT SYSTEM");
        System.out.println("====================================");
        System.out.println("1. Add Employee");
        System.out.println("2. Display All Employees");
        System.out.println("3. Search Employee by ID");
        System.out.println("4. Delete Employee");
        System.out.println("5. Sort Employees by Salary (High to Low)");
        System.out.println("6. Search Employee by Name");
        System.out.println("7. Exit");
    }

    private static int readMenuChoice() {
        System.out.print("Enter your choice: ");
        String value = INPUT.nextLine().trim();
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private static String readRequiredText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = INPUT.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("This field cannot be empty. Please try again.");
        }
    }

    private static double readPositiveSalary() {
        while (true) {
            System.out.print("Salary: ");
            String value = INPUT.nextLine().trim();
            try {
                double salary = Double.parseDouble(value);
                if (Double.isFinite(salary) && salary > 0) {
                    return salary;
                }
                System.out.println("Salary must be a positive number greater than zero.");
            } catch (NumberFormatException ex) {
                System.out.println("Invalid salary. Enter a valid numeric value.");
            }
        }
    }

    private static void addEmployee(EmployeeLinkedList employees) {
        String name = readRequiredText("Name: ");
        String department = readRequiredText("Department: ");
        double salary = readPositiveSalary();

        Employee added = employees.addEmployee(name, department, salary);
        System.out.println("Employee added. ID: " + formatEmployee(added));
    }

    private static void searchById(EmployeeLinkedList employees) {
        String id = readRequiredText("Employee ID: ");
        Employee found = employees.searchById(id);
        if (found == null) {
            System.out.println("Employee " + id.toUpperCase(Locale.ROOT) + " not found.");
        } else {
            System.out.println("Employee Found:");
            System.out.println(formatEmployee(found));
        }
    }

    private static void searchByName(EmployeeLinkedList employees) {
        String name = readRequiredText("Employee Name: ");
        int matches = employees.searchByName(name);
        if (matches == 0) {
            System.out.println("No employee found matching: " + name);
        }
    }

    private static void deleteEmployee(EmployeeLinkedList employees) {
        String id = readRequiredText("Employee ID: ");
        boolean deleted = employees.deleteById(id);
        if (deleted) {
            System.out.println("Employee " + id.toUpperCase(Locale.ROOT)
                    + " deleted. Total employees: " + employees.size());
        } else {
            System.out.println("Employee " + id.toUpperCase(Locale.ROOT) + " not found.");
        }
    }

    private static void loadEmployees(EmployeeLinkedList employees, Path file) {
        if (!Files.exists(file)) {
            System.out.println("No existing employees.txt file found. Starting with an empty list.");
            return;
        }

        int loaded = 0;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (line.startsWith("#NEXT_ID\t")) {
                    try {
                        employees.setNextEmployeeNumber(Integer.parseInt(line.substring("#NEXT_ID\t".length()).trim()));
                    } catch (NumberFormatException ex) {
                        System.out.println("Ignoring invalid next-ID metadata at line " + lineNumber + ".");
                    }
                    continue;
                }
                String[] fields = line.split("\\t", -1);
                if (fields.length != 4) {
                    System.out.println("Skipping invalid record at line " + lineNumber + ".");
                    continue;
                }
                try {
                    double salary = Double.parseDouble(fields[3]);
                    boolean accepted = employees.addLoadedEmployee(
                            fields[0], fields[1], fields[2], salary);
                    if (accepted) {
                        loaded++;
                    } else {
                        System.out.println("Skipping invalid or duplicate record at line " + lineNumber + ".");
                    }
                } catch (NumberFormatException ex) {
                    System.out.println("Skipping invalid salary at line " + lineNumber + ".");
                }
            }
            System.out.println("Loaded " + loaded + " employee record(s) from employees.txt.");
        } catch (IOException ex) {
            System.out.println("Could not load employees.txt: " + ex.getMessage());
        }
    }

    private static void saveEmployees(EmployeeLinkedList employees, Path file) {
        try {
            employees.saveToFile(file);
            System.out.println("Employee records saved to " + file + ".");
        } catch (IOException ex) {
            System.out.println("Could not save employee records: " + ex.getMessage());
        }
    }

    private static String formatEmployee(Employee employee) {
        return employee.getId() + " | " + employee.getName() + " | "
                + employee.getDepartment() + " | " + formatSalary(employee.getSalary());
    }

    private static String formatSalary(double salary) {
        if (salary == Math.rint(salary)) {
            return String.format(Locale.ROOT, "%.0f", salary);
        }
        return String.format(Locale.ROOT, "%.2f", salary);
    }
}

class Employee {
    private final String id;
    private final String name;
    private final String department;
    private final double salary;

    Employee(String id, String name, String department, double salary) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.salary = salary;
    }

    String getId() {
        return id;
    }

    String getName() {
        return name;
    }

    String getDepartment() {
        return department;
    }

    double getSalary() {
        return salary;
    }
}

class Node {
    Employee data;
    Node next;

    Node(Employee data) {
        this.data = data;
        this.next = null;
    }
}

/** Custom singly linked list. java.util.LinkedList is not used. */
class EmployeeLinkedList {
    private Node head;
    private Node tail;
    private int count;
    private int nextEmployeeNumber = 101;
    private final HashMap<String, Employee> employeeIndex = new HashMap<>();

    Employee addEmployee(String name, String department, double salary) {
        String id = "E" + nextEmployeeNumber;
        nextEmployeeNumber++;
        Employee employee = new Employee(id, cleanField(name), cleanField(department), salary);
        appendNode(new Node(employee));
        employeeIndex.put(normalizeId(id), employee);
        return employee;
    }

    boolean addLoadedEmployee(String id, String name, String department, double salary) {
        String normalizedId = normalizeId(id);
        if (!normalizedId.matches("E[0-9]+") || name.trim().isEmpty()
                || department.trim().isEmpty() || !Double.isFinite(salary) || salary <= 0
                || employeeIndex.containsKey(normalizedId)) {
            return false;
        }

        Employee employee = new Employee(normalizedId, cleanField(name), cleanField(department), salary);
        appendNode(new Node(employee));
        employeeIndex.put(normalizedId, employee);
        try {
            int number = Integer.parseInt(normalizedId.substring(1));
            if (number >= nextEmployeeNumber) {
                nextEmployeeNumber = number + 1;
            }
        } catch (NumberFormatException ignored) {
            // An ID with an excessively large numeric suffix is ignored for next-ID calculation.
        }
        return true;
    }

    private void appendNode(Node node) {
        if (head == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        count++;
    }

    Employee searchById(String id) {
        return employeeIndex.get(normalizeId(id));
    }

    int searchByName(String searchText) {
        String query = searchText.trim().toLowerCase(Locale.ROOT);
        int matches = 0;
        Node current = head;
        while (current != null) {
            if (current.data.getName().toLowerCase(Locale.ROOT).contains(query)) {
                System.out.println(current.data.getId() + " | " + current.data.getName() + " | "
                        + current.data.getDepartment() + " | " + EmployeeManagementSystemFormat.salary(current.data.getSalary()));
                matches++;
            }
            current = current.next;
        }
        return matches;
    }

    boolean deleteById(String id) {
        String key = normalizeId(id);
        if (!employeeIndex.containsKey(key)) {
            return false;
        }

        Node previous = null;
        Node current = head;
        while (current != null && !normalizeId(current.data.getId()).equals(key)) {
            previous = current;
            current = current.next;
        }

        if (current == null) {
            return false;
        }
        if (previous == null) {
            head = current.next;
        } else {
            previous.next = current.next;
        }
        if (current == tail) {
            tail = previous;
        }
        if (head == null) {
            tail = null;
        }
        employeeIndex.remove(key);
        count--;
        return true;
    }

    void displayAll() {
        if (head == null) {
            System.out.println("No employee records available.");
            System.out.println("Total Employees: 0");
            return;
        }

        System.out.println("\n--- Employee Records ---");
        Node current = head;
        int position = 1;
        while (current != null) {
            System.out.println(position + ". " + current.data.getId() + " | "
                    + current.data.getName() + " | " + current.data.getDepartment() + " | "
                    + EmployeeManagementSystemFormat.salary(current.data.getSalary()));
            position++;
            current = current.next;
        }
        System.out.println("Total Employees: " + count);
    }

    void sortBySalaryDescending() {
        head = mergeSort(head);
        tail = null;
        Node current = head;
        while (current != null) {
            tail = current;
            current = current.next;
        }
    }

    void displaySortedBySalary() {
        if (head == null) {
            System.out.println("No employee records available.");
            return;
        }
        Node current = head;
        int position = 1;
        while (current != null) {
            System.out.println(position + ". " + current.data.getId() + " | "
                    + current.data.getName() + " | "
                    + EmployeeManagementSystemFormat.salary(current.data.getSalary()));
            position++;
            current = current.next;
        }
    }

    private Node mergeSort(Node start) {
        if (start == null || start.next == null) {
            return start;
        }
        Node middle = getMiddle(start);
        Node rightStart = middle.next;
        middle.next = null;
        Node left = mergeSort(start);
        Node right = mergeSort(rightStart);
        return mergeDescending(left, right);
    }

    private Node getMiddle(Node start) {
        Node slow = start;
        Node fast = start.next;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }

    private Node mergeDescending(Node left, Node right) {
        Node dummy = new Node(null);
        Node current = dummy;
        while (left != null && right != null) {
            if (left.data.getSalary() >= right.data.getSalary()) {
                current.next = left;
                left = left.next;
            } else {
                current.next = right;
                right = right.next;
            }
            current = current.next;
        }
        current.next = (left != null) ? left : right;
        return dummy.next;
    }

    int size() {
        return count;
    }

    void setNextEmployeeNumber(int value) {
        if (value >= 101 && value > nextEmployeeNumber) {
            nextEmployeeNumber = value;
        }
    }

    void saveToFile(Path file) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("#NEXT_ID\t" + nextEmployeeNumber);
            writer.newLine();
            Node current = head;
            while (current != null) {
                Employee employee = current.data;
                writer.write(employee.getId() + "\t" + cleanField(employee.getName()) + "\t"
                        + cleanField(employee.getDepartment()) + "\t" + employee.getSalary());
                writer.newLine();
                current = current.next;
            }
        }
    }

    private static String normalizeId(String id) {
        return id == null ? "" : id.trim().toUpperCase(Locale.ROOT);
    }

    private static String cleanField(String value) {
        return value.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ').trim();
    }
}

class EmployeeManagementSystemFormat {
    static String salary(double salary) {
        if (salary == Math.rint(salary)) {
            return String.format(Locale.ROOT, "%.0f", salary);
        }
        return String.format(Locale.ROOT, "%.2f", salary);
    }
}
