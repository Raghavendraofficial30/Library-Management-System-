package com.lms.main;

import com.lms.dao.BookDAO;
import com.lms.dao.IssueDAO;
import com.lms.dao.MemberDAO;
import com.lms.model.Book;
import com.lms.model.IssueRecord;
import com.lms.model.Member;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Main application class providing a console-based, menu-driven interface
 * for the Library Management System.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final BookDAO bookDAO = new BookDAO();
    private static final MemberDAO memberDAO = new MemberDAO();
    private static final IssueDAO issueDAO = new IssueDAO();

    // Regular expressions for validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,15}$");

    // Standard lending period and late fine rate
    private static final int ALLOWED_DAYS = 7;
    private static final double FINE_PER_DAY = 5.0; // ₹5 per day

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("       LIBRARY MANAGEMENT SYSTEM (CORE JAVA & JDBC) ");
        System.out.println("==================================================");

        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = readIntegerInput("Enter your choice (1-8): ");

            switch (choice) {
                case 1:
                    bookManagementSubmenu();
                    break;
                case 2:
                    memberManagementSubmenu();
                    break;
                case 3:
                    issueBook();
                    break;
                case 4:
                    returnBook();
                    break;
                case 5:
                    viewAllBooks();
                    break;
                case 6:
                    viewAllMembers();
                    break;
                case 7:
                    viewCurrentlyIssuedBooks();
                    break;
                case 8:
                    System.out.println("\nThank you for using Library Management System. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("\n[Error] Invalid choice! Please select an option between 1 and 8.");
            }
        }

        scanner.close();
    }

    private static void displayMainMenu() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("                   MAIN MENU                      ");
        System.out.println("--------------------------------------------------");
        System.out.println("1. Book Management (Add, Update, Delete, Search)");
        System.out.println("2. Member Management (Register, Update, Delete)");
        System.out.println("3. Issue a Book to Member");
        System.out.println("4. Return a Book (Calculate Fine)");
        System.out.println("5. View All Books");
        System.out.println("6. View All Members");
        System.out.println("7. View Currently Issued Books");
        System.out.println("8. Exit");
        System.out.println("--------------------------------------------------");
    }

    // =========================================================================
    // BOOK MANAGEMENT
    // =========================================================================

    private static void bookManagementSubmenu() {
        System.out.println("\n--- Book Management ---");
        System.out.println("1. Add New Book");
        System.out.println("2. Update Book Details");
        System.out.println("3. Delete Book by ID");
        System.out.println("4. Search Books");
        System.out.println("5. Back to Main Menu");
        int subChoice = readIntegerInput("Select option (1-5): ");

        switch (subChoice) {
            case 1:
                addNewBook();
                break;
            case 2:
                updateBook();
                break;
            case 3:
                deleteBook();
                break;
            case 4:
                searchBooks();
                break;
            case 5:
                return;
            default:
                System.out.println("[Error] Invalid option.");
        }
    }

    private static void addNewBook() {
        System.out.println("\n--- Add New Book ---");

        int id;
        while (true) {
            id = readIntegerInput("Enter Book ID: ");
            if (id <= 0) {
                System.out.println("[Validation Error] Book ID must be a positive integer.");
                continue;
            }
            if (bookDAO.isBookExists(id)) {
                System.out.println("[Validation Error] A book with ID " + id + " already exists. Use a unique ID.");
                continue;
            }
            break;
        }

        String title = readNonEmptyString("Enter Book Title: ");
        String author = readNonEmptyString("Enter Author Name: ");
        String category = readNonEmptyString("Enter Category: ");

        int quantity;
        while (true) {
            quantity = readIntegerInput("Enter Quantity (0 or more): ");
            if (quantity < 0) {
                System.out.println("[Validation Error] Quantity cannot be negative.");
                continue;
            }
            break;
        }

        Book book = new Book(id, title, author, category, quantity);
        if (bookDAO.addBook(book)) {
            System.out.println("\n[Success] Book added successfully!");
        } else {
            System.out.println("\n[Error] Failed to add book to the database.");
        }
    }

    private static void updateBook() {
        System.out.println("\n--- Update Book Details ---");
        int id = readIntegerInput("Enter Book ID to update: ");

        Book book = bookDAO.getBookById(id);
        if (book == null) {
            System.out.println("\n[Not Found] No book found with ID: " + id);
            return;
        }

        System.out.println("\nCurrent Book Details:");
        printBookTable(List.of(book));
        System.out.println("(Press Enter directly to keep the existing value)");

        System.out.print("Enter New Title [" + book.getTitle() + "]: ");
        String title = scanner.nextLine().trim();
        if (!title.isEmpty()) {
            book.setTitle(title);
        }

        System.out.print("Enter New Author [" + book.getAuthor() + "]: ");
        String author = scanner.nextLine().trim();
        if (!author.isEmpty()) {
            book.setAuthor(author);
        }

        System.out.print("Enter New Category [" + book.getCategory() + "]: ");
        String category = scanner.nextLine().trim();
        if (!category.isEmpty()) {
            book.setCategory(category);
        }

        while (true) {
            System.out.print("Enter New Quantity [" + book.getQuantity() + "]: ");
            String qtyInput = scanner.nextLine().trim();
            if (qtyInput.isEmpty()) {
                break;
            }
            try {
                int qty = Integer.parseInt(qtyInput);
                if (qty < 0) {
                    System.out.println("[Validation Error] Quantity cannot be negative.");
                    continue;
                }
                book.setQuantity(qty);
                break;
            } catch (NumberFormatException e) {
                System.out.println("[Validation Error] Please enter a valid number for quantity.");
            }
        }

        if (bookDAO.updateBook(book)) {
            System.out.println("\n[Success] Book updated successfully!");
        } else {
            System.out.println("\n[Error] Failed to update book.");
        }
    }

    private static void deleteBook() {
        System.out.println("\n--- Delete Book ---");
        int id = readIntegerInput("Enter Book ID to delete: ");

        Book book = bookDAO.getBookById(id);
        if (book == null) {
            System.out.println("\n[Not Found] No book found with ID: " + id);
            return;
        }

        printBookTable(List.of(book));
        System.out.print("Are you sure you want to delete this book? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("y") || confirm.equals("yes")) {
            if (bookDAO.deleteBook(id)) {
                System.out.println("\n[Success] Book deleted successfully!");
            } else {
                System.out.println("\n[Error] Failed to delete book. Check if it is currently referenced.");
            }
        } else {
            System.out.println("\n[Cancelled] Deletion cancelled.");
        }
    }

    private static void searchBooks() {
        System.out.println("\n--- Search Books ---");
        String keyword = readNonEmptyString("Enter search keyword (title, author, or category): ");
        List<Book> results = bookDAO.searchBooks(keyword);
        if (results.isEmpty()) {
            System.out.println("\n[Not Found] No books found matching: \"" + keyword + "\"");
        } else {
            printBookTable(results);
        }
    }

    private static void viewAllBooks() {
        System.out.println("\n--- All Books in Library ---");
        List<Book> books = bookDAO.getAllBooks();
        if (books.isEmpty()) {
            System.out.println("[Info] No books found in the library database.");
        } else {
            printBookTable(books);
        }
    }

    // =========================================================================
    // MEMBER MANAGEMENT
    // =========================================================================

    private static void memberManagementSubmenu() {
        System.out.println("\n--- Member Management ---");
        System.out.println("1. Register New Member");
        System.out.println("2. Update Member Details");
        System.out.println("3. Delete Member by ID");
        System.out.println("4. Search Members");
        System.out.println("5. Back to Main Menu");
        int subChoice = readIntegerInput("Select option (1-5): ");

        switch (subChoice) {
            case 1:
                registerMember();
                break;
            case 2:
                updateMember();
                break;
            case 3:
                deleteMember();
                break;
            case 4:
                searchMembers();
                break;
            case 5:
                return;
            default:
                System.out.println("[Error] Invalid option.");
        }
    }

    private static void registerMember() {
        System.out.println("\n--- Register New Member ---");

        int id;
        while (true) {
            id = readIntegerInput("Enter Member ID: ");
            if (id <= 0) {
                System.out.println("[Validation Error] Member ID must be a positive integer.");
                continue;
            }
            if (memberDAO.isMemberExists(id)) {
                System.out.println("[Validation Error] A member with ID " + id + " already exists.");
                continue;
            }
            break;
        }

        String name = readNonEmptyString("Enter Member Name: ");

        String email;
        while (true) {
            email = readNonEmptyString("Enter Email: ");
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                System.out.println("[Validation Error] Invalid email format! (e.g., user@domain.com)");
                continue;
            }
            if (memberDAO.isEmailExists(email, 0)) {
                System.out.println("[Validation Error] Email is already registered to another member.");
                continue;
            }
            break;
        }

        String phone;
        while (true) {
            phone = readNonEmptyString("Enter Phone Number (10-15 digits): ");
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                System.out.println("[Validation Error] Invalid phone format! Must contain 10 to 15 digits.");
                continue;
            }
            break;
        }

        Member member = new Member(id, name, email, phone);
        if (memberDAO.addMember(member)) {
            System.out.println("\n[Success] Member registered successfully!");
        } else {
            System.out.println("\n[Error] Failed to register member.");
        }
    }

    private static void updateMember() {
        System.out.println("\n--- Update Member Details ---");
        int id = readIntegerInput("Enter Member ID to update: ");

        Member member = memberDAO.getMemberById(id);
        if (member == null) {
            System.out.println("\n[Not Found] No member found with ID: " + id);
            return;
        }

        System.out.println("\nCurrent Member Details:");
        printMemberTable(List.of(member));
        System.out.println("(Press Enter directly to keep the existing value)");

        System.out.print("Enter New Name [" + member.getName() + "]: ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) {
            member.setName(name);
        }

        while (true) {
            System.out.print("Enter New Email [" + member.getEmail() + "]: ");
            String email = scanner.nextLine().trim();
            if (email.isEmpty()) {
                break;
            }
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                System.out.println("[Validation Error] Invalid email format!");
                continue;
            }
            if (memberDAO.isEmailExists(email, member.getId())) {
                System.out.println("[Validation Error] Email already used by another member.");
                continue;
            }
            member.setEmail(email);
            break;
        }

        while (true) {
            System.out.print("Enter New Phone [" + member.getPhone() + "]: ");
            String phone = scanner.nextLine().trim();
            if (phone.isEmpty()) {
                break;
            }
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                System.out.println("[Validation Error] Invalid phone format! Must contain 10-15 digits.");
                continue;
            }
            member.setPhone(phone);
            break;
        }

        if (memberDAO.updateMember(member)) {
            System.out.println("\n[Success] Member updated successfully!");
        } else {
            System.out.println("\n[Error] Failed to update member.");
        }
    }

    private static void deleteMember() {
        System.out.println("\n--- Delete Member ---");
        int id = readIntegerInput("Enter Member ID to delete: ");

        Member member = memberDAO.getMemberById(id);
        if (member == null) {
            System.out.println("\n[Not Found] No member found with ID: " + id);
            return;
        }

        printMemberTable(List.of(member));
        System.out.print("Are you sure you want to delete this member? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("y") || confirm.equals("yes")) {
            if (memberDAO.deleteMember(id)) {
                System.out.println("\n[Success] Member deleted successfully!");
            } else {
                System.out.println("\n[Error] Failed to delete member.");
            }
        } else {
            System.out.println("\n[Cancelled] Deletion cancelled.");
        }
    }

    private static void searchMembers() {
        System.out.println("\n--- Search Members ---");
        String keyword = readNonEmptyString("Enter search keyword (name, email, or phone): ");
        List<Member> results = memberDAO.searchMembers(keyword);
        if (results.isEmpty()) {
            System.out.println("\n[Not Found] No members found matching: \"" + keyword + "\"");
        } else {
            printMemberTable(results);
        }
    }

    private static void viewAllMembers() {
        System.out.println("\n--- All Registered Members ---");
        List<Member> members = memberDAO.getAllMembers();
        if (members.isEmpty()) {
            System.out.println("[Info] No members found in the database.");
        } else {
            printMemberTable(members);
        }
    }

    // =========================================================================
    // ISSUE & RETURN OPERATIONS
    // =========================================================================

    /**
     * Feature: Issues a book to a member with stock validation.
     */
    private static void issueBook() {
        System.out.println("\n--- Issue a Book ---");

        int bookId = readIntegerInput("Enter Book ID to issue: ");
        Book book = bookDAO.getBookById(bookId);
        if (book == null) {
            System.out.println("\n[Not Found] Book with ID " + bookId + " does not exist.");
            return;
        }

        // Stock check: Prevent issuing if out of stock
        if (book.getQuantity() <= 0) {
            System.out.println("\n[Stock Error] Book \"" + book.getTitle() + "\" is OUT OF STOCK (Available: 0). Cannot issue.");
            return;
        }

        int memberId = readIntegerInput("Enter Member ID: ");
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            System.out.println("\n[Not Found] Member with ID " + memberId + " does not exist.");
            return;
        }

        // Prevent duplicate issuance of the same book to the same member
        if (issueDAO.isBookAlreadyIssuedToMember(bookId, memberId)) {
            System.out.println("\n[Issue Error] Member \"" + member.getName() + "\" already has an active issue of \"" + book.getTitle() + "\".");
            System.out.println("The member must return the book first before borrowing it again.");
            return;
        }

        System.out.println("\nIssuing Book Details:");
        System.out.println("- Book: " + book.getTitle() + " (Current Stock: " + book.getQuantity() + ")");
        System.out.println("- Member: " + member.getName() + " (ID: " + member.getId() + ")");
        System.out.println("- Issue Date: " + LocalDate.now());
        System.out.println("- Lending Rules: Return within " + ALLOWED_DAYS + " days. Fine of ₹" + FINE_PER_DAY + "/day applies thereafter.");

        System.out.print("Confirm issuance? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("y") && !confirm.equals("yes")) {
            System.out.println("\n[Cancelled] Book issuance cancelled.");
            return;
        }

        boolean success = issueDAO.issueBook(bookId, memberId);
        if (success) {
            System.out.println("\n[Success] Book issued successfully! Stock reduced by 1.");
        } else {
            System.out.println("\n[Error] Failed to issue book. Transaction rolled back.");
        }
    }

    /**
     * Feature: Returns a book, calculates overdue fine (₹5/day after 7 days), and restores stock.
     */
    private static void returnBook() {
        System.out.println("\n--- Return a Book ---");
        System.out.println("1. Return by Issue ID");
        System.out.println("2. Return by Book ID & Member ID");
        int searchChoice = readIntegerInput("Select search method (1-2): ");

        IssueRecord issueRecord = null;
        if (searchChoice == 1) {
            int issueId = readIntegerInput("Enter Issue ID: ");
            issueRecord = issueDAO.getActiveIssueById(issueId);
        } else if (searchChoice == 2) {
            int bookId = readIntegerInput("Enter Book ID: ");
            int memberId = readIntegerInput("Enter Member ID: ");
            issueRecord = issueDAO.getActiveIssueByBookAndMember(bookId, memberId);
        } else {
            System.out.println("[Error] Invalid option.");
            return;
        }

        if (issueRecord == null) {
            System.out.println("\n[Not Found] No active (unreturned) issue record found for the provided details.");
            return;
        }

        // Determine return date: Default to today, or allow manual date entry for testing
        LocalDate returnDate = LocalDate.now();
        System.out.println("\nActive Issue Found:");
        System.out.println("- Issue ID: " + issueRecord.getIssueId());
        System.out.println("- Book: " + issueRecord.getBookTitle() + " (ID: " + issueRecord.getBookId() + ")");
        System.out.println("- Member: " + issueRecord.getMemberName() + " (ID: " + issueRecord.getMemberId() + ")");
        System.out.println("- Issue Date: " + issueRecord.getIssueDate());
        System.out.print("Press Enter to use today's date (" + returnDate + ") or enter simulated return date (YYYY-MM-DD): ");
        String customDateStr = scanner.nextLine().trim();

        if (!customDateStr.isEmpty()) {
            try {
                returnDate = LocalDate.parse(customDateStr);
                if (returnDate.isBefore(issueRecord.getIssueDate())) {
                    System.out.println("[Validation Error] Return date cannot be before issue date (" + issueRecord.getIssueDate() + ").");
                    return;
                }
            } catch (DateTimeParseException e) {
                System.out.println("[Validation Error] Invalid date format! Using today's date instead.");
                returnDate = LocalDate.now();
            }
        }

        // Calculate fine
        long totalDays = ChronoUnit.DAYS.between(issueRecord.getIssueDate(), returnDate);
        long overdueDays = 0;
        double fine = 0.0;

        if (totalDays > ALLOWED_DAYS) {
            overdueDays = totalDays - ALLOWED_DAYS;
            fine = overdueDays * FINE_PER_DAY;
        }

        System.out.println("\n--- Return & Fine Summary ---");
        System.out.println("Return Date:       " + returnDate);
        System.out.println("Total Days Held:   " + totalDays + " day(s)");
        System.out.println("Allowed Period:    " + ALLOWED_DAYS + " days");
        System.out.println("Overdue Days:      " + overdueDays + " day(s)");
        System.out.printf("Total Fine Due:    ₹%.2f (Rate: ₹%.2f/day after %d days)\n", fine, FINE_PER_DAY, ALLOWED_DAYS);

        System.out.print("\nConfirm book return? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("y") && !confirm.equals("yes")) {
            System.out.println("\n[Cancelled] Book return cancelled.");
            return;
        }

        boolean success = issueDAO.returnBook(issueRecord.getIssueId(), issueRecord.getBookId(), returnDate, fine);
        if (success) {
            System.out.println("\n[Success] Book returned successfully!");
            System.out.println("Book stock incremented by 1.");
            if (fine > 0) {
                System.out.printf("Please collect fine amount: ₹%.2f\n", fine);
            } else {
                System.out.println("No fine incurred (returned within allowable period).");
            }
        } else {
            System.out.println("\n[Error] Failed to process book return. Transaction rolled back.");
        }
    }

    /**
     * Feature: Views all currently issued books.
     */
    private static void viewCurrentlyIssuedBooks() {
        System.out.println("\n--- Currently Issued Books ---");
        List<IssueRecord> issued = issueDAO.getCurrentlyIssuedBooks();
        if (issued.isEmpty()) {
            System.out.println("[Info] No books are currently issued.");
        } else {
            String line = "+----------+---------+-------------------------+-----------+----------------------+------------+---------+";
            System.out.println(line);
            System.out.printf("| %-8s | %-7s | %-23s | %-9s | %-20s | %-10s | %-7s |\n",
                    "ISSUE ID", "BOOK ID", "BOOK TITLE", "MEMBER ID", "MEMBER NAME", "ISSUE DATE", "STATUS");
            System.out.println(line);

            for (IssueRecord r : issued) {
                System.out.printf("| %-8d | %-7d | %-23s | %-9d | %-20s | %-10s | %-7s |\n",
                        r.getIssueId(),
                        r.getBookId(),
                        truncate(r.getBookTitle(), 23),
                        r.getMemberId(),
                        truncate(r.getMemberName(), 20),
                        r.getIssueDate(),
                        r.getStatus());
            }
            System.out.println(line);
            System.out.println("Total active issues: " + issued.size());
        }
    }

    // =========================================================================
    // HELPER FORMATTING & INPUT METHODS
    // =========================================================================

    private static void printBookTable(List<Book> books) {
        String line = "+-------+-------------------------+----------------------+----------------------+----------+";
        System.out.println(line);
        System.out.printf("| %-5s | %-23s | %-20s | %-20s | %-8s |\n",
                "ID", "TITLE", "AUTHOR", "CATEGORY", "QUANTITY");
        System.out.println(line);

        for (Book b : books) {
            System.out.printf("| %-5d | %-23s | %-20s | %-20s | %-8d |\n",
                    b.getId(),
                    truncate(b.getTitle(), 23),
                    truncate(b.getAuthor(), 20),
                    truncate(b.getCategory(), 20),
                    b.getQuantity());
        }
        System.out.println(line);
        System.out.println("Total records: " + books.size());
    }

    private static void printMemberTable(List<Member> members) {
        String line = "+-------+----------------------+--------------------------------+-----------------+";
        System.out.println(line);
        System.out.printf("| %-5s | %-20s | %-30s | %-15s |\n",
                "ID", "NAME", "EMAIL", "PHONE");
        System.out.println(line);

        for (Member m : members) {
            System.out.printf("| %-5d | %-20s | %-30s | %-15s |\n",
                    m.getId(),
                    truncate(m.getName(), 20),
                    truncate(m.getEmail(), 30),
                    truncate(m.getPhone(), 15));
        }
        System.out.println(line);
        System.out.println("Total records: " + members.size());
    }

    private static int readIntegerInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("[Validation Error] Invalid input! Please enter a valid number.");
            }
        }
    }

    private static String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("[Validation Error] Field cannot be empty. Please enter a value.");
        }
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}

