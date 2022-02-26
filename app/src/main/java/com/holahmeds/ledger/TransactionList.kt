package com.holahmeds.ledger

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.holahmeds.ledger.adapters.BigDecimalAdapter
import com.holahmeds.ledger.adapters.DateAdapter
import com.holahmeds.ledger.database.entities.Transaction
import com.holahmeds.ledger.databinding.FragmentTransactionListBinding
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

class TransactionList : Fragment() {
    private val viewModel: LedgerViewModel by activityViewModels()
    private var transactions: List<Transaction> = emptyList()

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        setHasOptionsMenu(true)

        val binding = FragmentTransactionListBinding.inflate(inflater, container, false)

        val transactionAdapter = TransactionAdapter { transaction: Transaction ->
            val dialog = TransactionListMenu(object : TransactionListMenu.ItemSelectedListener {
                override fun onEditSelected() {
                    val action = TransactionListDirections.actionEditFromList()
                    action.transactionID = transaction.id
                    val navController = NavHostFragment.findNavController(this@TransactionList)
                    navController.navigate(action)
                }

                override fun onDeleteSelected() {
                    viewModel.deleteTransaction(transaction)
                }
            })

            dialog.show(parentFragmentManager, "transactionlistmenu")
        }

        val liveTransactions = viewModel.getTransactions()
        liveTransactions.observe(viewLifecycleOwner) { list ->
            transactions = list
            transactionAdapter.setData(list)
        }

        // Set the adapter
        with(binding.transactionList) {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 0) {
                        binding.newTransactionFab.hide()
                    } else if (dy < 0) {
                        binding.newTransactionFab.show()
                    }
                }
            })
        }


        binding.newTransactionFab.setOnClickListener {
            val navController = NavHostFragment.findNavController(this)
            navController.navigate(R.id.transactionEditor)
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.transaction_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.export -> {
                exportToFile()
                return true
            }
            R.id.chart -> {
                val navController = NavHostFragment.findNavController(this)
                navController.navigate(R.id.chartFragment)
            }
        }

        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    }

    private fun exportToFile() {
        val file = File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "transactions.json"
        )

        val moshi = Moshi.Builder()
            .add(BigDecimalAdapter())
            .add(DateAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()
        val type = Types.newParameterizedType(List::class.java, Transaction::class.java)
        val adapter = moshi.adapter<List<Transaction>>(type).indent("  ")

        file.writeText(adapter.toJson(transactions))

        Toast.makeText(context, "Exported to $file", Toast.LENGTH_LONG).show()
        Log.i("TransactionList", "Exported data to $file")
    }
}
