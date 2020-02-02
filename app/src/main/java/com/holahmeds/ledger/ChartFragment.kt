package com.holahmeds.ledger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.android.synthetic.main.fragment_chart.view.*
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class ChartFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val fragment = inflater.inflate(R.layout.fragment_chart, container, false)

        val viewModel = ViewModelProvider(requireActivity()).get(LedgerViewModel::class.java)

        val currencyFormatter = CurrencyFormatter()
        viewModel.getMonthlyTotals().observe(viewLifecycleOwner, Observer { list ->
            val incomeEntries = mutableListOf<BarEntry>()
            val expenseEntries = mutableListOf<BarEntry>()

            for (transaction in list.take(12)) {
                if (transaction.totalIncome > 0) {
                    incomeEntries.add(BarEntry(transaction.month.let { it.year * 12 + it.monthValue - 1 }.toFloat(), transaction.totalIncome.toFloat()))
                }
                if (transaction.totalExpense > 0) {
                    expenseEntries.add(BarEntry(transaction.month.let { it.year * 12 + it.monthValue - 1 }.toFloat(), transaction.totalExpense.toFloat()))
                }
            }

            val incomeDataSet = BarDataSet(incomeEntries, "Income")
            incomeDataSet.setColors(intArrayOf(R.color.graphIncome), context)
            incomeDataSet.valueFormatter = currencyFormatter
            val expenseDataSet = BarDataSet(expenseEntries, "Expense")
            expenseDataSet.valueFormatter = currencyFormatter
            expenseDataSet.setColors(intArrayOf(R.color.graphExpense), context)

            val barData = BarData(incomeDataSet, expenseDataSet)

            fragment.chart.apply {
                data = barData
                invalidate()
            }
        })

        fragment.chart.description = null
        fragment.chart.setTouchEnabled(false)

        val xAxis = fragment.chart.xAxis
        xAxis.valueFormatter = MonthFormatter()
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        fragment.chart.axisRight.isEnabled = false
        val yAxis = fragment.chart.axisLeft
        yAxis.valueFormatter = currencyFormatter
        yAxis.axisMinimum = 0f

        return fragment
    }
}

class MonthFormatter : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val month = YearMonth.of(value.toInt() / 12, (value.toInt() % 12) + 1)
        return month.format(DateTimeFormatter.ofPattern("MMM yy"))
    }
}

class CurrencyFormatter : ValueFormatter() {
    override fun getBarLabel(barEntry: BarEntry?): String {
        return CurrencyAdapter.amountToString(barEntry?.y?.toLong() ?: 0L)
    }

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return CurrencyAdapter.amountToString(value.toLong())
    }
}
