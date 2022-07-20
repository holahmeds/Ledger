package com.holahmeds.ledger.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.holahmeds.ledger.LedgerViewModel
import com.holahmeds.ledger.R
import com.holahmeds.ledger.databinding.FragmentChartBinding
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class ChartFragment : Fragment() {
    private val viewModel: LedgerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentChartBinding.inflate(inflater, container, false)

        viewModel.getMonthlyTotals().observe(viewLifecycleOwner) { list ->
            val incomeEntries = mutableListOf<BarEntry>()
            val expenseEntries = mutableListOf<BarEntry>()

            for (transaction in list) {
                incomeEntries.add(BarEntry(transaction.month.let { it.year * 12 + it.monthValue - 1 }
                    .toFloat(), transaction.totalIncome.toFloat()))
                expenseEntries.add(BarEntry(transaction.month.let { it.year * 12 + it.monthValue - 1 }
                    .toFloat(), transaction.totalExpense.toFloat()))
            }

            // Entries need to be sorted by x
            // https://github.com/PhilJay/MPAndroidChart/issues/4131
            incomeEntries.sortBy { barEntry -> barEntry.x }
            expenseEntries.sortBy { barEntry -> barEntry.x }

            val incomeDataSet = BarDataSet(incomeEntries, "Income")
            incomeDataSet.setColors(intArrayOf(R.color.graphIncome), context)
            val expenseDataSet = BarDataSet(expenseEntries, "Expense")
            expenseDataSet.setColors(intArrayOf(R.color.graphExpense), context)

            val barData = BarData(incomeDataSet, expenseDataSet)

            binding.chart.apply {
                data = barData
                data.barWidth = 0.45f
                groupBars(data.xMin - 0.5f, 0.08f, 0.01f)
                invalidate()

                // start at the end of the graph
                val initPos = data.xMax
                // default zoom shows 12 entries
                val initScale = (data.xMax - data.xMin + 6) / 24f
                zoom(initScale, 1f, initPos, 0f, YAxis.AxisDependency.LEFT)

                setVisibleXRangeMaximum(12f)
            }
        }

        binding.chart.description = null
        binding.chart.isScaleYEnabled = false

        val xAxis = binding.chart.xAxis
        xAxis.valueFormatter = MonthFormatter()
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f

        binding.chart.axisRight.isEnabled = false
        val yAxis = binding.chart.axisLeft
        yAxis.axisMinimum = 0f
        yAxis.setDrawGridLines(false)

        return binding.root
    }
}

class MonthFormatter : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val month = YearMonth.of(value.toInt() / 12, (value.toInt() % 12) + 1)
        return month.format(DateTimeFormatter.ofPattern("MMM yy"))
    }
}
