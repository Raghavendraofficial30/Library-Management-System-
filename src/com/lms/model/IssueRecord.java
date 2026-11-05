package com.lms.model;

import java.time.LocalDate;

/**
 * Model class representing a Book Issuance record.
 * Tracks book ID, member ID, issue date, return date, fine assessed, and status.
 */
public class IssueRecord {
    private int issueId;
    private int bookId;
    private int memberId;
    private LocalDate issueDate;
    private LocalDate returnDate;
    private double fine;
    private String status;

    // Display fields populated via SQL JOINs
    private String bookTitle;
    private String memberName;

    public IssueRecord() {
    }

    public IssueRecord(int issueId, int bookId, int memberId, LocalDate issueDate, 
                       LocalDate returnDate, double fine, String status) {
        this.issueId = issueId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.returnDate = returnDate;
        this.fine = fine;
        this.status = status;
    }

    // Getters and Setters
    public int getIssueId() {
        return issueId;
    }

    public void setIssueId(int issueId) {
        this.issueId = issueId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public double getFine() {
        return fine;
    }

    public void setFine(double fine) {
        this.fine = fine;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    @Override
    public String toString() {
        return "IssueRecord [IssueID=" + issueId + ", BookID=" + bookId + ", MemberID=" + memberId + 
               ", IssueDate=" + issueDate + ", ReturnDate=" + returnDate + ", Fine=" + fine + ", Status=" + status + "]";
    }
}

