package com.university.portal.service;

import com.university.portal.entity.Notice;
import com.university.portal.entity.NoticeComment;
import com.university.portal.repository.NoticeCommentRepository;
import com.university.portal.repository.NoticeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private NoticeCommentRepository noticeCommentRepository;

    @InjectMocks
    private NoticeService noticeService;

    @Test
    void getAllNoticesReturnsRepositoryResults() {
        List<Notice> notices = List.of(new Notice("Exam", "Exam details", "Academic", "Admin", "ADMIN"));
        when(noticeRepository.findAllByOrderByPostedAtDesc()).thenReturn(notices);

        assertSame(notices, noticeService.getAllNotices());
    }

    @Test
    void addCommentAttachesAndPersistsComment() {
        Notice notice = new Notice("Exam", "Exam details", "Academic", "Admin", "ADMIN");
        when(noticeRepository.findById(7L)).thenReturn(Optional.of(notice));
        when(noticeCommentRepository.save(any(NoticeComment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NoticeComment comment = noticeService.addComment(7L, "Ada", "STUDENT", "When is the exam?");

        assertEquals("When is the exam?", comment.getContent());
        assertSame(notice, comment.getNotice());
        assertEquals(List.of(comment), notice.getComments());
        verify(noticeCommentRepository).save(comment);
    }

    @Test
    void addCommentRejectsUnknownNotice() {
        when(noticeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> noticeService.addComment(99L, "Ada", "STUDENT", "Question"));

        verify(noticeCommentRepository, never()).save(any(NoticeComment.class));
    }
}
