package com.taskflow.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Task {

    public enum Status {
        TODO, IN_PROGRESS, DONE;

        public static Status fromString(String s) {
            return switch (s.toLowerCase()) {
                case "todo"        -> TODO;
                case "in_progress" -> IN_PROGRESS;
                case "done"        -> DONE;
                default -> throw new IllegalArgumentException("Unknown status: " + s);
            };
        }

        public String toDbValue() {
            return switch (this) {
                case TODO -> "todo";
                case IN_PROGRESS -> "in_progress";
                case DONE -> "done";
            };
        }
    }

    public enum Priority {
        LOW, MEDIUM, HIGH;

        public static Priority fromString(String s) {
            return switch (s.toLowerCase()) {
                case "low"    -> LOW;
                case "medium" -> MEDIUM;
                case "high"   -> HIGH;
                default -> throw new IllegalArgumentException("Unknown priority: " + s);
            };
        }

        public String toDbValue() {
            return switch (this) {
                case LOW -> "low";
                case MEDIUM -> "medium";
                case HIGH -> "high";
            };
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 20)
    @Convert(converter = StatusConverter.class)
    @Builder.Default
    private Status status = Status.TODO;

    @Column(nullable = false, length = 20)
    @Convert(converter = PriorityConverter.class)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
