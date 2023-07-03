package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private val reminderList: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

    private var shouldReturnError = false

    fun setReturnError(shouldReturnError: Boolean) {
        this.shouldReturnError = shouldReturnError
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> =
        if (shouldReturnError) {
            Result.Error("[getReminders] Error retrieving reminders")
        } else {
            Result.Success(reminderList)
        }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> =
        if (!shouldReturnError) {
            val reminder = reminderList.find { it.id == id }
            if (reminder != null) {
                Result.Success(reminder)
            } else {
                Result.Error("[getReminder] Reminder not found!")
            }
        } else {
            Result.Error("[getReminder] Error retrieving reminder")
        }


    override suspend fun deleteAllReminders() {
        reminderList.clear()
    }


}