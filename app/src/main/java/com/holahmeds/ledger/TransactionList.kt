package com.holahmeds.ledger

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.holahmeds.ledger.data.Transaction
import com.holahmeds.ledger.databinding.FragmentTransactionListBinding
import java.io.File

class TransactionList : Fragment() {
    private val viewModel: LedgerViewModel by activityViewModels()
    private var transactions: List<Transaction> = emptyList()

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.transaction_list_menu, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            when (menuItem.itemId) {
                R.id.export -> {
                    exportToFile()
                    return true
                }
                R.id.chart -> {
                    val navController = NavHostFragment.findNavController(this@TransactionList)
                    navController.navigate(R.id.chartFragment)
                }
                R.id.preferences -> {
                    val navController = NavHostFragment.findNavController(this@TransactionList)
                    navController.navigate(R.id.preferencesFragment)
                }
            }

            return false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner)

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

    private fun exportToFile() {
        val file = File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "transactions.json"
        )

        val transactionSerializer = TransactionSerializer()

        file.writeText(transactionSerializer.serializeList(transactions, true))

        Toast.makeText(context, "Exported to $file", Toast.LENGTH_LONG).show()
        Log.i("TransactionList", "Exported data to $file")
    }
}
