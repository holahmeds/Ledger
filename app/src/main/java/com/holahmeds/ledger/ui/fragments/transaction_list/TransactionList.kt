package com.holahmeds.ledger.ui.fragments.transaction_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
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
import com.holahmeds.ledger.Error
import com.holahmeds.ledger.LedgerViewModel
import com.holahmeds.ledger.R
import com.holahmeds.ledger.data.Transaction
import com.holahmeds.ledger.databinding.FragmentTransactionListBinding
import com.holahmeds.ledger.ui.TransactionExporter
import com.holahmeds.ledger.ui.fragments.BannerFragment
import com.holahmeds.ledger.ui.fragments.FilterDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.util.concurrent.locks.ReentrantLock

class TransactionList : Fragment() {
    private val numberFormatter: NumberFormat = NumberFormat.getInstance()

    private val viewModel: LedgerViewModel by activityViewModels()

    lateinit var binding: FragmentTransactionListBinding

    lateinit var transactionExporter: TransactionExporter

    private var jobInProgress = false
    private var pagerLoading = false
    private val progressBarLock = ReentrantLock()

    private var newestDate: LocalDate? = null

    private val menuProvider = object : MenuProvider {
        private var filterItem: MenuItem? = null
        private var filterActive = false

        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.transaction_list_menu, menu)
            filterItem = menu.findItem(R.id.filter)
            updateIcon()
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            when (menuItem.itemId) {
                R.id.filter -> {
                    val dialog = FilterDialog()
                    dialog.show(parentFragmentManager, "transactionlistmenu")
                    return true
                }

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

        fun setFilterActive(active: Boolean) {
            filterActive = active
            updateIcon()
        }

        private fun updateIcon() {
            val id = if (filterActive) {
                R.drawable.filter_on
            } else {
                R.drawable.filter_off
            }
            val drawable = ResourcesCompat.getDrawable(resources, id, null)
            filterItem?.icon = drawable
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

        binding = FragmentTransactionListBinding.inflate(inflater, container, false)

        transactionExporter = TransactionExporter(
            requireContext(),
            requireActivity().activityResultRegistry,
            viewModel
        )
        lifecycle.addObserver(transactionExporter)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFilterIcon()
        setupBalanceCard()
        setupErrorBanner()
        val transactionAdapter = createTransactionAdapter()
        setupProgressBar(transactionAdapter)
        setupTransactionList(transactionAdapter)
        setupFab()
    }

    private fun createTransactionAdapter(): TransactionPagingAdapter {
        val onSelectTransaction = { transaction: Transaction ->
            val dialog = TransactionListMenu()
            dialog.arguments = bundleOf(Pair("TRANSACTION_ID", transaction.id))
            dialog.show(parentFragmentManager, "transactionlistmenu")
        }

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
        return transactionAdapter
    }

    private fun setupTransactionList(transactionAdapter: TransactionPagingAdapter) {
        // Scroll to top if new item is inserted on top
        transactionAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                val firstItem = transactionAdapter.peek(0)
                if (firstItem is TransactionListItem.Subheader && firstItem.date != newestDate) {
                    newestDate = firstItem.date
                    binding.transactionList.scrollToPosition(0)
                }
            }
        })

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
    }

    private fun setupProgressBar(transactionAdapter: TransactionPagingAdapter) {
        viewModel.isJobInProgress().observe(viewLifecycleOwner) { isInProgress ->
            setJobStatus(isInProgress)
        }
        lifecycleScope.launch {
            transactionAdapter.loadStateFlow.collectLatest { loadStates ->
                setPagerStatus(loadStates.refresh is LoadState.Loading || loadStates.append is LoadState.Loading || loadStates.prepend is LoadState.Loading)
            }
        }
    }

    private fun setupFilterIcon() {
        viewModel.isFilterActive().observe(viewLifecycleOwner) { isActive ->
            menuProvider.setFilterActive(isActive)
        }
    }

    private fun setupBalanceCard() {
        viewModel.getBalance().observe(viewLifecycleOwner) { balance ->
            binding.balance.balanceView.text = if (balance == null) {
                ""
            } else {
                numberFormatter.format(balance)
            }
        }
    }

    private fun setupErrorBanner() {
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
    }

    private fun setupFab() {
        binding.newTransactionFab.setOnClickListener {
            val navController = NavHostFragment.findNavController(this)
            navController.navigate(R.id.transactionEditor)
        }
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
        binding.progressBar.visibility = when (jobInProgress || pagerLoading) {
            true -> View.VISIBLE
            else -> View.INVISIBLE
        }
    }
}
