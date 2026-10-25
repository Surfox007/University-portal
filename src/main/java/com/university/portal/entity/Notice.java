package com.university.portal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notices")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 3000)
    private String content;

    @Column(nullable = false)
    private String category; // e.g. "Examination", "Academic", "General", "Urgent"

    @Column(nullable = false)
    private String authorName;

    @Column(nullable = false)
    private String authorRole; // "TEACHER" or "ADMIN"

    @Column(nullable = false)
    private LocalDateTime postedAt;

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("commentedAt ASC")
    private List<NoticeComment> comments = new ArrayList<>();

    public Notice() {
        this.postedAt = LocalDateTime.now();
    }

    public Notice(String title, String content, String category, String authorName, String authorRole) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.authorName = authorName;
        this.authorRole = authorRole;
        this.postedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorRole() {
        return authorRole;
    }

    public void setAuthorRole(String authorRole) {
        this.authorRole = authorRole;
    }

    public LocalDateTime getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(LocalDateTime postedAt) {
        this.postedAt = postedAt;
    }

    public List<NoticeComment> getComments() {
        return comments;
    }

    public void setComments(List<NoticeComment> comments) {
        this.comments = comments;
    }

    public void addComment(NoticeComment comment) {
        this.comments.add(comment);
        comment.setNotice(this);
    }

    public void removeComment(NoticeComment comment) {
        this.comments.remove(comment);
        comment.setNotice(null);
    }
}
