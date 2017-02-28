package com.edusoho.kuozhi.v3.model.bal.test;


/**
 * Created by howzhi on 14-9-23.
 */
public class MaterialQuestionTypeSeq extends QuestionTypeSeq {

    public QuestionTypeSeq parent;

    public MaterialQuestionTypeSeq(QuestionTypeSeq questionTypeSeq, QuestionTypeSeq parent)
    {
        this.parent = parent;

        this.id = questionTypeSeq.id;
        this.testId = questionTypeSeq.testId;
        this.seq = questionTypeSeq.seq;
        this.questionId = questionTypeSeq.questionId;
        this.questionType = questionTypeSeq.questionType;
        this.parentId = questionTypeSeq.parentId;
        this.score = questionTypeSeq.score;
        this.missScore = questionTypeSeq.missScore;
        this.question = questionTypeSeq.question;
        this.items = questionTypeSeq.items;
    }
}
