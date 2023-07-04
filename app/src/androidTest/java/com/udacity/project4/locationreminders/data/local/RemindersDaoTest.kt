package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initializeDatabase(){
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDatabase() = database.close()

    @Test
    fun saveReminder_getReminderById() = runBlockingTest {
        //GIVEN saving a reminder
        val reminderDTO = ReminderDTO(
            "GoldenGateBridge",
            "Golden gate bridge",
            "San Francisco, CA, USA",
            37.8199,
            -122.4783,
            "1"
        )
        database.reminderDao().saveReminder(reminderDTO)

        //WHEN getting the reminders by this id
        val savedReminderDTO = database.reminderDao().getReminderById(reminderDTO.id)

        //THEN will return this reminder
        assertThat<ReminderDTO>(savedReminderDTO, notNullValue())
        assertThat(savedReminderDTO?.id, `is`(reminderDTO.id))
        assertThat(savedReminderDTO?.title, `is`(reminderDTO.title))
        assertThat(savedReminderDTO?.description, `is`(reminderDTO.description))
        assertThat(savedReminderDTO?.location, `is`(reminderDTO.location))
        assertThat(savedReminderDTO?.latitude, `is`(reminderDTO.latitude))
        assertThat(savedReminderDTO?.longitude, `is`(reminderDTO.longitude))
        assertThat(savedReminderDTO?.id, `is`(reminderDTO.id))
    }

    @Test
    fun getReminderById_noReminderFoundWithId_returnNull() = runBlockingTest {
        //GIVEN saving a reminder
        val reminderDTO = ReminderDTO(
            "GoldenGateBridge",
            "Golden gate bridge",
            "San Francisco, CA, USA",
            37.8199,
            -122.4783,
            "1"
        )
        database.reminderDao().saveReminder(reminderDTO)

        //WHEN getting the reminders by another id
        val savedRemindersDTO = database.reminderDao().getReminderById("6")

        //THEN will return null
        assertThat(savedRemindersDTO, nullValue())
    }
}