package com.holahmeds.ledger.ui

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import com.google.android.material.textfield.TextInputEditText
import com.holahmeds.ledger.adapters.DateAdapter
import java.time.LocalDate

class DatePickerField(private val view: TextInputEditText, context: Context, initDate: LocalDate?) {
    private var date: LocalDate? = null

    init {
        if (initDate != null) {
            setDate(initDate)
        }

        view.setOnClickListener {
            val currentDate = date
            val dialog = if (currentDate != null) {
                DatePickerDialog(
                    context,
                    this::onDatePicked,
                    currentDate.year,
                    currentDate.monthValue - 1,
                    currentDate.dayOfMonth
                )
            } else {
                val d = DatePickerDialog(context)
                d.setOnDateSetListener(this::onDatePicked)
                d
            }
            dialog.show()
        }
    }

    fun getDate() = date

    private fun setDate(date: LocalDate) {
        this.date = date
        view.setText(DateAdapter.dateToString(date))
    }

    private fun onDatePicked(datePicker: DatePicker, year: Int, month: Int, day: Int) {
        val date = LocalDate.of(year, month + 1, day)
        setDate(date)
    }
}
