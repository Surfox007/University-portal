package com.university.portal.service;

import com.university.portal.entity.Notice;
import com.university.portal.entity.NoticeComment;
import com.university.portal.repository.NoticeCommentRepository;
import com.university.portal.repository.NoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeCommentRepository noticeCommentRepository;

    public NoticeService(NoticeRepository noticeRepository, NoticeCommentRepository noticeCommentRepository) {
        this.noticeRepository = noticeRepository;
        this.noticeCommentRepository = noticeCommentRepository;
    }

    @Transactional(readOnly = true)
    public List<Notice> getAllNotices() {
        return noticeRepository.findAllByOrderByPostedAtDesc();
    }

    @Transactional(readOnly = true)
    public Optional<Notice> getNoticeById(Long id) {
        return noticeRepository.findById(id);
    }

    public Notice createNotice(String title, String content, String category, String authorName, String authorRole) {
        Notice notice = new Notice(title, content, category, authorName, authorRole);
        return noticeRepository.save(notice);
    }

    public NoticeComment addComment(Long noticeId, String authorName, String authorRole, String content) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("Notice not found with ID: " + noticeId));

        NoticeComment comment = new NoticeComment(authorName, authorRole, content, notice);
        notice.addComment(comment);
        return noticeCommentRepository.save(comment);
    }

    public void deleteNotice(Long noticeId) {
        noticeRepository.deleteById(noticeId);
    }

    public void deleteComment(Long commentId) {
        noticeCommentRepository.deleteById(commentId);
    }
}
