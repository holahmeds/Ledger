package com.holahmeds.ledger.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.holahmeds.ledger.Error
import com.holahmeds.ledger.LedgerViewModel
import com.holahmeds.ledger.R
import com.holahmeds.ledger.data.Transaction
import com.holahmeds.ledger.databinding.FragmentTransactionListBinding
import com.holahmeds.ledger.ui.TransactionAdapter
import com.holahmeds.ledger.ui.TransactionExporter

class TransactionList : Fragment() {
    private val viewModel: LedgerViewModel by activityViewModels()
    private var transactions: List<Transaction> = emptyList()

    lateinit var transactionExporter: TransactionExporter

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.transaction_list_menu, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            when (menuItem.itemId) {
                R.id.export -> {
                    transactionExporter.export()
                    return true
                }
                R.id.import_transactions -> {
                    transactionExporter.import()
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
            val dialog = TransactionListMenu()
            dialog.arguments = bundleOf(Pair("TRANSACTION_ID", transaction.id))
            dialog.show(parentFragmentManager, "transactionlistmenu")
        }

        val liveTransactions = viewModel.getTransactions()
        liveTransactions.observe(viewLifecycleOwner) { list ->
            transactions = list
            transactionAdapter.setData(list)
        }

        viewModel.getError().observe(viewLifecycleOwner) { error ->
            when (error) {
                is Error.None -> {
                    binding.newTransactionFab.show()
                    val bannerFragment = childFragmentManager.findFragmentById(R.id.banner)
                    if (bannerFragment != null) {
                        childFragmentManager.commit {
                            setReorderingAllowed(true)
                            remove(bannerFragment)
                        }
                    }
                }
                is Error.Some -> {
                    binding.newTransactionFab.hide()
                    childFragmentManager.commit {
                        setReorderingAllowed(true)
                        val existingBanner = childFragmentManager.findFragmentById(R.id.banner)
                        if (existingBanner != null) {
                            remove(existingBanner)
                        }
                        add<BannerFragment>(R.id.banner)
                    }
                }
            }
        }

        viewModel.isJobInProgress().observe(viewLifecycleOwner) { isInProgress ->
            binding.progressBar.visibility = when (isInProgress) {
                true -> View.VISIBLE
                else -> View.INVISIBLE
            }
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

        transactionExporter = TransactionExporter(
            requireContext(),
            requireActivity().activityResultRegistry,
            viewModel
        )
        lifecycle.addObserver(transactionExporter)

        return binding.root
    }

}
