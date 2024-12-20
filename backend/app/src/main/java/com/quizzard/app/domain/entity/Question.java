package com.quizzard.app.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "questions")
public class Question extends BaseEntity {

    @ManyToOne
    private Topic topic;
    @Column(nullable = false)
    private String questionText;
    @Column
    private String imageUrl;
    @Column
    private String option1;
    @Column
    private String option2;
    @Column
    private String option3;
    @Column
    private String option4;
    @Column(nullable = false)
    private String correctAnswer;
}
