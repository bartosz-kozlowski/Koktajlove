package com.example.cocktailapp

import android.app.Application
import android.content.Context
import android.os.CountDownTimer
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val timers = mutableMapOf<String, CountDownTimer>()
    private val _selectedMinutes = mutableStateMapOf<String, Int>()
    private val _selectedSeconds = mutableStateMapOf<String, Int>()
    private val _timeLeft = mutableStateMapOf<String, Long>()
    private val _isRunning = mutableStateMapOf<String, Boolean>()

    private val sharedPreferences = application.getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)

    val selectedDuration: (String) -> Long = { drinkId ->
        ((_selectedMinutes[drinkId] ?: 1) * 60_000L) + ((_selectedSeconds[drinkId] ?: 0) * 1000L)
    }

    fun getSelectedMinutes(drinkId: String) = _selectedMinutes[drinkId] ?: 1
    fun getSelectedSeconds(drinkId: String) = _selectedSeconds[drinkId] ?: 0
    fun getTimeLeft(drinkId: String) = _timeLeft[drinkId] ?: selectedDuration(drinkId)
    fun isRunning(drinkId: String) = _isRunning[drinkId] ?: false

    /*fun loadDurationForDrink(drinkId: String) {
        if (!_selectedMinutes.containsKey(drinkId)) {
            _selectedMinutes[drinkId] = 1
            _selectedSeconds[drinkId] = 0

            val savedTime = sharedPreferences.getLong("${drinkId}_timeLeft", -1L)
            val savedRunning = sharedPreferences.getBoolean("${drinkId}_isRunning", false)

            if (savedTime != -1L) {
                _timeLeft[drinkId] = savedTime
                _isRunning[drinkId] = savedRunning
            } else {
                _timeLeft[drinkId] = 60_000L
                _isRunning[drinkId] = false
            }
        }
    }*/
    /*
    fun loadDurationForDrink(drinkId: String, preparationTime: Int) {
        if (!_selectedMinutes.containsKey(drinkId)) {
            val preparationTime = preparationTime * 60
            // Ustawiamy minutnik na podstawie czasu przygotowania drinka
            _selectedMinutes[drinkId] = preparationTime / 60 // Zamiana minut
            _selectedSeconds[drinkId] = preparationTime % 60 // Zamiana sekund

            val savedTime = sharedPreferences.getLong("${drinkId}_timeLeft", -1L)
            val savedRunning = sharedPreferences.getBoolean("${drinkId}_isRunning", false)

            if (savedTime != -1L) {
                _timeLeft[drinkId] = savedTime
                //_isRunning[drinkId] = savedRunning
                _isRunning[drinkId] = false
            } else {
                _timeLeft[drinkId] = (preparationTime * 1000L) // Ustawiamy czas w milisekundach
                _isRunning[drinkId] = false
            }
        }
    }*/
    fun loadDurationForDrink(drinkId: String, preparationTime: Int) {
        if (!_selectedMinutes.containsKey(drinkId)) {
            val savedMinutes = sharedPreferences.getInt("${drinkId}_minutes", -1)
            val savedSeconds = sharedPreferences.getInt("${drinkId}_seconds", -1)

            if (savedMinutes != -1 && savedSeconds != -1) {
                _selectedMinutes[drinkId] = savedMinutes
                _selectedSeconds[drinkId] = savedSeconds
            } else {
                val preparationTimeSeconds = preparationTime * 60
                _selectedMinutes[drinkId] = preparationTimeSeconds / 60
                _selectedSeconds[drinkId] = preparationTimeSeconds % 60
            }

            val savedTime = sharedPreferences.getLong("${drinkId}_timeLeft", -1L)
            val savedRunning = sharedPreferences.getBoolean("${drinkId}_isRunning", false)

            if (savedTime != -1L) {
                _timeLeft[drinkId] = savedTime
                _isRunning[drinkId] = false
            } else {
                _timeLeft[drinkId] = selectedDuration(drinkId)
                _isRunning[drinkId] = false
            }
        }
    }


    fun resetToPreparationTime(drinkId: String, preparationTime: Int) {
        timers[drinkId]?.cancel()
        _isRunning[drinkId] = false
        val timeInMillis = preparationTime * 60 * 1000L
        _timeLeft[drinkId] = timeInMillis
        _selectedMinutes[drinkId] = preparationTime
        _selectedSeconds[drinkId] = 0
        saveTimerState(drinkId)
    }

    fun setMinutes(drinkId: String, minutes: Int) {
        _selectedMinutes[drinkId] = minutes
        _timeLeft[drinkId] = selectedDuration(drinkId)
        saveTimerState(drinkId)
    }

    fun setSeconds(drinkId: String, seconds: Int) {
        _selectedSeconds[drinkId] = seconds
        _timeLeft[drinkId] = selectedDuration(drinkId)
        saveTimerState(drinkId)
    }

    fun startTimer(drinkId: String) {
        timers[drinkId]?.cancel()

        val duration = getTimeLeft(drinkId)

        val timer = object : CountDownTimer(duration, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                _timeLeft[drinkId] = millisUntilFinished
                saveTimerState(drinkId)
            }

            override fun onFinish() {
                _isRunning[drinkId] = false
                _timeLeft[drinkId] = 0L
                saveTimerState(drinkId)
            }
        }
        timers[drinkId] = timer
        timer.start()
        _isRunning[drinkId] = true
        saveTimerState(drinkId)
    }

    fun pauseTimer(drinkId: String) {
        timers[drinkId]?.cancel()
        _isRunning[drinkId] = false
        saveTimerState(drinkId)
    }

    fun resetTimer(drinkId: String) {
        timers[drinkId]?.cancel()
        _isRunning[drinkId] = false
        _timeLeft[drinkId] = selectedDuration(drinkId)
        saveTimerState(drinkId)
    }

    private fun saveTimerState(drinkId: String) {
        sharedPreferences.edit()
            .putLong("${drinkId}_timeLeft", _timeLeft[drinkId] ?: 60_000L)
            .putBoolean("${drinkId}_isRunning", _isRunning[drinkId] ?: false)
            .putInt("${drinkId}_minutes", _selectedMinutes[drinkId] ?: 1)
            .putInt("${drinkId}_seconds", _selectedSeconds[drinkId] ?: 0)
            .apply()
    }

    override fun onCleared() {
        super.onCleared()
        timers.values.forEach { it.cancel() }
    }
}
