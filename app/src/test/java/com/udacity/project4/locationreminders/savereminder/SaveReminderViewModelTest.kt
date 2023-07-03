package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.rule.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SaveReminderViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val fakeDataSource = FakeDataSource()
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun setup() {
        stopKoin()
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

    @Test
    fun check_loading() {
        val reminderDataItem = ReminderDataItem(
            "GoldenGateBridge",
            "Golden gate bridge",
            "San Francisco, CA, USA",
            37.8199,
            -122.4783,
            "1"
        )
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(reminderDataItem)
        var showLoading = saveReminderViewModel.showLoading.value
        assertThat(showLoading, `is`(true))
        mainCoroutineRule.resumeDispatcher()
        showLoading = saveReminderViewModel.showLoading.value
        assertThat(showLoading, `is`(false))
    }


    @Test
    fun shouldReturnError_noTitle() {
        val reminderDataItem = ReminderDataItem(
            null,
            "Golden gate bridge",
            "San Francisco, CA, USA",
            37.8199,
            -122.4783,
            "1"
        )
        assertThat(saveReminderViewModel.validateEnteredData(reminderDataItem), `is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.value, `is`(R.string.err_enter_title))
    }

    @Test
    fun shouldReturnError_noLocation() {
        val reminderDataItem = ReminderDataItem(
            "GoldenGateBridge",
            "Golden gate bridge",
            null,
            37.8199,
            -122.4783,
            "1"
        )
        assertThat(saveReminderViewModel.validateEnteredData(reminderDataItem), `is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.value, `is`(R.string.err_select_location))
    }
}