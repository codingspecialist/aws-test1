package com.metacoding.web_project.report;


import com.metacoding.web_project.transaction.Transaction;
import com.metacoding.web_project.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Table(name = "report_tb")
@Getter
@Entity
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    private User reported;

    @Column(nullable = false)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    private Transaction transaction;

    // 0 : 처리 전 상태
    // 1 : (처리 완료) 관리자가 경매 취소함
    // 2 : (처리 완료) 관리자가 판매자에게 입찰금을 전달함
    @Column(nullable = false)
    private Integer status;

    @Builder
    public Report(Integer id, User reporter, User reported, String reason, Transaction transaction, Integer status) {
        this.id = id;
        this.reporter = reporter;
        this.reported = reported;
        this.reason = reason;
        this.transaction = transaction;
        this.status = status;
    }

    public void updateStatus(String method) {
        if (method.equals("cancel")) {
            status = 1;
        } else if (method.equals("seller")) {
            status = 2;
        }
    }
}