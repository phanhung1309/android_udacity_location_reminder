package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var database: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initializeRepository() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        remindersLocalRepository = RemindersLocalRepository(
            database.reminderDao(),
            Dispatchers.Main
        )
    }

    @After
    fun closeDatabase() = database.close()

    @Test
    fun saveReminder_getReminderById() = runBlocking {
        //GIVEN saving a reminder
        val reminderDTO = ReminderDTO(
            "GoldenGateBridge",
            "Golden gate bridge",
            "San Francisco, CA, USA",
            37.8199,
            -122.4783,
            "1"
        )
        remindersLocalRepository.saveReminder(reminderDTO)

        //WHEN getting the reminders by this id
        val reminder = remindersLocalRepository.getReminder(reminderDTO.id)

        //THEN will return this reminder
        assertThat(reminder, `is`(Result.Success(reminderDTO)))
    }

    @Test
    fun getReminderById_noReminderFoundWithId_returnError() {
        runBlocking {
            //GIVEN saving a reminder
            val reminderDTO = ReminderDTO(
                "GoldenGateBridge",
                "Golden gate bridge",
                "San Francisco, CA, USA",
                37.8199,
                -122.4783,
                "1"
            )
            remindersLocalRepository.saveReminder(reminderDTO)

            //WHEN getting the reminders by another id
            val reminder = remindersLocalRepository.getReminder("6")

            //THEN will return error
            assertThat(reminder, `is`(Result.Error("Reminder not found!")))
        }
    }
}