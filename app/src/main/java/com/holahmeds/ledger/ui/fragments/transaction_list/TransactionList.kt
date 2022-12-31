package com.holahmeds.ledger.ui.fragments.transaction_list

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.paging.LoadState
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.holahmeds.ledger.Error
import com.holahmeds.ledger.LedgerViewModel
import com.holahmeds.ledger.R
import com.holahmeds.ledger.data.Transaction
import com.holahmeds.ledger.databinding.FragmentTransactionListBinding
import com.holahmeds.ledger.ui.TransactionExporter
import com.holahmeds.ledger.ui.fragments.BannerFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.concurrent.locks.ReentrantLock

class TransactionList : Fragment() {
    private val numberFormatter: NumberFormat = NumberFormat.getInstance()

    private val viewModel: LedgerViewModel by activityViewModels()

    lateinit var transactionExporter: TransactionExporter

    private var jobInProgress = false
    private var pagerLoading = false
    private val progressBarLock = ReentrantLock()
    private var progressBar: LinearProgressIndicator? = null

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

    init {
        numberFormatter.minimumFractionDigits = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner)

        val binding = FragmentTransactionListBinding.inflate(inflater, container, false)

        val onSelectTransaction = { transaction: Transaction ->
            val dialog = TransactionListMenu()
            dialog.arguments = bundleOf(Pair("TRANSACTION_ID", transaction.id))
            dialog.show(parentFragmentManager, "transactionlistmenu")
        }

        viewModel.getBalance().observe(viewLifecycleOwner) { balance ->
            binding.balance.balanceView.text = if (balance == null) {
                ""
            } else {
                numberFormatter.format(balance)
            }
        }

//        val transactionAdapter = TransactionAdapter(onSelectTransaction)
//        val liveTransactions = viewModel.getTransactions()
//        liveTransactions.observe(viewLifecycleOwner) { list ->
//            transactionAdapter.setData(list)
//        }

        val transactionAdapter =
            TransactionPagingAdapter(onSelectTransaction)
        viewModel.getTransactionPages().observe(viewLifecycleOwner) { pagingData ->
            viewLifecycleOwner.lifecycleScope.launch {
                transactionAdapter.submitData(pagingData.map {
                    TransactionListItem.TransactionItem(it)
                }.insertSeparators { next, prev ->
                    when {
                        prev == null -> {
                            null
                        }
                        prev.transaction.date != next?.transaction?.date -> {
                            TransactionListItem.Subheader(prev.transaction.date)
                        }
                        else -> {
                            null
                        }
                    }
                })
            }
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

        this.progressBar = binding.progressBar
        viewModel.isJobInProgress().observe(viewLifecycleOwner) { isInProgress ->
            setJobStatus(isInProgress)
        }
        lifecycleScope.launch {
            transactionAdapter.loadStateFlow.collectLatest { loadStates ->
                setPagerStatus(loadStates.refresh is LoadState.Loading || loadStates.append is LoadState.Loading || loadStates.prepend is LoadState.Loading)
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

    private fun setJobStatus(inProgress: Boolean) {
        progressBarLock.lock()
        try {
            this.jobInProgress = inProgress
            updateProgressBarVisibility()
        } finally {
            progressBarLock.unlock()
        }
    }

    private fun setPagerStatus(inProgress: Boolean) {
        progressBarLock.lock()
        try {
            this.pagerLoading = inProgress
            updateProgressBarVisibility()
        } finally {
            progressBarLock.unlock()
        }
    }

    private fun updateProgressBarVisibility() {
        progressBar?.visibility = when (jobInProgress || pagerLoading) {
            true -> View.VISIBLE
            else -> View.INVISIBLE
        }
    }
}
