package com.example.todo;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView taskRecyclerView;
    FloatingActionButton fab;
    TaskAdapter taskAdapter;
    List<Task> taskList = new ArrayList<>();
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = openOrCreateDatabase("todoApp.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS TASK(TASKID INTEGER PRIMARY KEY AUTOINCREMENT, TASK TEXT, STATUS INTEGER)");

        taskRecyclerView = findViewById(R.id.taskRecyclerView);
        fab = findViewById(R.id.fab);

        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(taskList, db);
        taskRecyclerView.setAdapter(taskAdapter);

        loadTasks();  // Load tasks from database

        fab.setOnClickListener(v -> showAddTaskDialog());

        setupSwipeActions();
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        EditText editTaskText = dialogView.findViewById(R.id.editTextTask);

        builder.setTitle("Add Task")
                .setPositiveButton("Add", (dialog, which) -> {
                    String task = editTaskText.getText().toString().trim();
                    if (!task.isEmpty()) {
                        addTask(task);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addTask(String task) {
        ContentValues values = new ContentValues();
        values.put("TASK", task);
        values.put("STATUS", 0);
        db.insert("TASK", null, values);
        loadTasks();
    }

    private void loadTasks() {
        taskList.clear();
        Cursor cursor = db.rawQuery("SELECT * FROM TASK", null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String taskText = cursor.getString(1);
            boolean isCompleted = cursor.getInt(2) == 1;
            taskList.add(new Task(id, taskText, isCompleted));
        }
        cursor.close();
        taskAdapter.notifyDataSetChanged();
    }

    private void setupSwipeActions() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Task task = taskList.get(position);

                if (direction == ItemTouchHelper.LEFT) {  // Delete Task
                    showDeleteConfirmationDialog(task, position);
                } else {  // Edit Task
                    showEditTaskDialog(task, position);
                }
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(taskRecyclerView);
    }

    private void showDeleteConfirmationDialog(Task task, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.delete("TASK", "TASKID=?", new String[]{String.valueOf(task.getId())});
                    taskList.remove(position);
                    taskAdapter.notifyItemRemoved(position);
                })
                .setNegativeButton("Cancel", (dialog, which) -> taskAdapter.notifyItemChanged(position))
                .show();
    }

    private void showEditTaskDialog(Task task, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        EditText editTaskText = dialogView.findViewById(R.id.editTextTask);
        editTaskText.setText(task.getTaskText());

        builder.setTitle("Edit Task")
                .setPositiveButton("Update", (dialog, which) -> {
                    String newTask = editTaskText.getText().toString().trim();
                    if (!newTask.isEmpty()) {
                        ContentValues values = new ContentValues();
                        values.put("TASK", newTask);
                        db.update("TASK", values, "TASKID=?", new String[]{String.valueOf(task.getId())});
                        taskList.get(position).setTaskText(newTask);
                        taskAdapter.notifyItemChanged(position);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> taskAdapter.notifyItemChanged(position))
                .show();
    }
}
