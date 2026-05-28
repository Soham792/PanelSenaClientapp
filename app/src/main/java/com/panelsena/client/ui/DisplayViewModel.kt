package com.panelsena.client.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panelsena.client.data.model.AssignedDisplay
import com.panelsena.client.data.repository.DisplayContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DisplayViewModel @Inject constructor(
    private val repository: DisplayContentRepository
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = repository.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val displayContent: StateFlow<AssignedDisplay?> = repository.observeDisplayContent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    var clientId = ""
        private set

    init {
        viewModelScope.launch {
            clientId = repository.getClientId()
        }
    }

    val todayDate: String
        get() {
            val sdf = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
            return sdf.format(Date())
        }

    val weekDays: List<WeekDay>
        get() {
            val days = mutableListOf<WeekDay>()
            val calendar = Calendar.getInstance()
            val todayDay = calendar.get(Calendar.DAY_OF_MONTH)
            val sdfDay = SimpleDateFormat("EEE", Locale.getDefault())
            val sdfNum = SimpleDateFormat("d", Locale.getDefault())

            for (i in 0 until 7) {
                val dayName = sdfDay.format(calendar.time)
                val dayNum = sdfNum.format(calendar.time)
                val isToday = calendar.get(Calendar.DAY_OF_MONTH) == todayDay
                days.add(WeekDay(dayName, dayNum, isToday))
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            return days
        }
}

data class WeekDay(
    val shortName: String,
    val number: String,
    val isToday: Boolean
)
