package com.edusoho.kuozhi.homework.model;

import java.util.List;

/**
 * Created by howzhi on 15/10/15.
 */
public class HomeWorkQuestion {

    private int id;

    private String type;

    private String stem;

    private List<String> answer;

    private String analysis;

    private List<String> metas;

    private String difficulty;

    private List<HomeWorkQuestion> items;

    private HomeWorkQuestion parent;

    private HomeWorkItemResult result;

    public HomeWorkItemResult getResult() {
        return result;
    }

    public void setResult(HomeWorkItemResult result) {
        this.result = result;
    }

    public List<HomeWorkQuestion> getItems() {
        return items;
    }

    public void setItems(List<HomeWorkQuestion> items) {
        this.items = items;
    }

    public HomeWorkQuestion getParent() {
        return parent;
    }

    public void setParent(HomeWorkQuestion parent) {
        this.parent = parent;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public void setStem(String stem) {
        this.stem = stem;
    }

    public String getStem() {
        return this.stem;
    }

    public void setAnswer(List<String> answer) {
        this.answer = answer;
    }

    public List<String> getAnswer() {
        return this.answer;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public String getAnalysis() {
        return this.analysis;
    }

    public void setMetas(List<String> metas) {
        this.metas = metas;
    }

    public List<String> getMetas() {
        return this.metas;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDifficulty() {
        return this.difficulty;
    }
}
