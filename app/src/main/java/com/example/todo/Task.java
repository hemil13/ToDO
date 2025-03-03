package com.example.todo;

public class Task {
    private int id;
    private String taskText;
    private boolean isCompleted;

    public Task(int id, String taskText, boolean isCompleted) {
        this.id = id;
        this.taskText = taskText;
        this.isCompleted = isCompleted;
    }

    public int getId() {
        return id;
    }

    public String getTaskText() {
        return taskText;
    }

    public void setTaskText(String taskText) {
        this.taskText = taskText;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
