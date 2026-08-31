package com.example.weatherapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.adapter.RecordAdapter
import com.example.weatherapp.databinding.ActivitySavedRecordsBinding
import com.example.weatherapp.databinding.DialogEditRecordBinding
import com.example.weatherapp.db.DatabaseHelper
import com.example.weatherapp.model.WeatherRecord
import com.google.firebase.auth.FirebaseAuth

/**
 * Displays the current user's saved weather records in a RecyclerView, and
 * lets them edit (update) or delete individual records — requirements #7 & #8.
 */
class SavedRecordsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavedRecordsBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: RecordAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        auth = FirebaseAuth.getInstance()

        adapter = RecordAdapter(
            onEdit = { record -> showEditDialog(record) },
            onDelete = { record -> confirmDelete(record) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }

        loadRecords()
    }

    override fun onResume() {
        super.onResume()
        loadRecords()
    }

    private fun loadRecords() {
        val userId = auth.currentUser?.uid ?: return
        val records = dbHelper.getRecordsForUser(userId)
        adapter.submitList(records)
        binding.tvEmpty.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (records.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun showEditDialog(record: WeatherRecord) {
        val dialogBinding = DialogEditRecordBinding.inflate(LayoutInflater.from(this))
        dialogBinding.etLocation.setText(record.locationName)
        dialogBinding.etCondition.setText(record.condition)

        AlertDialog.Builder(this)
            .setTitle("Edit Record")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { dialog, _ ->
                val newLocation = dialogBinding.etLocation.text?.toString()?.trim().orEmpty()
                val newCondition = dialogBinding.etCondition.text?.toString()?.trim().orEmpty()

                if (newLocation.isEmpty()) {
                    android.widget.Toast.makeText(this, "Location name can't be empty.", android.widget.Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val updated = record.copy(locationName = newLocation, condition = newCondition)
                dbHelper.updateRecord(updated)
                loadRecords()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun confirmDelete(record: WeatherRecord) {
        AlertDialog.Builder(this)
            .setTitle("Delete Record")
            .setMessage("Delete the saved weather record for \"${record.locationName}\"?")
            .setPositiveButton("Delete") { dialog, _ ->
                dbHelper.deleteRecord(record.id)
                loadRecords()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
