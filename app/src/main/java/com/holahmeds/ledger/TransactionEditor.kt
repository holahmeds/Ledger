package com.holahmeds.ledger

import android.app.DatePickerDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import kotlinx.android.synthetic.main.fragment_transaction_editor.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TransactionEditor : Fragment() {
    var date: LocalDate = LocalDate.now()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dateView = date_view
        updateDateView()
        dateView.setOnClickListener {
            DatePickerDialog(
                    context,
                    { _: DatePicker, year: Int, month: Int, day: Int ->
                        date = LocalDate.of(year, month + 1, day)
                        updateDateView()
                    },
                    date.year,
                    date.monthValue - 1,
                    date.dayOfMonth
            ).show()
        }
    }

    private fun updateDateView() {
        date_view.setText(date.format(DateTimeFormatter.ISO_DATE))
    }
}
