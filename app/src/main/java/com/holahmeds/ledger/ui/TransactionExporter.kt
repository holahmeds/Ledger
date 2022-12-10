package com.holahmeds.ledger.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.holahmeds.ledger.LedgerViewModel
import com.holahmeds.ledger.TransactionSerializer
import com.holahmeds.ledger.data.NewTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TransactionExporter(
    private val context: Context,
    private val registry: ActivityResultRegistry,
    private val viewModel: LedgerViewModel
) : DefaultLifecycleObserver {
    private lateinit var importTransactions: ActivityResultLauncher<Array<String>?>
    private lateinit var exportTransactions: ActivityResultLauncher<String?>

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        importTransactions = registry.register(
            "import_transactions",
            owner,
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri == null) {
                // no file selected
                return@register
            }

            val inputStream = context.applicationContext.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e("TransactionExporter", "Content Provider crashed")
                return@register
            }
            owner.lifecycleScope.launch {
                val json = withContext(Dispatchers.IO) {
                    inputStream.reader().use {
                        it.readText()
                    }
                }

                val transactionSerializer = TransactionSerializer()
                val transactionList = transactionSerializer.deserializeList(json).map { t ->
                    NewTransaction(t.date, t.amount, t.category, t.transactee, t.note, t.tags)
                }
                viewModel.insertAll(transactionList)

                Toast.makeText(
                    context,
                    "Found ${transactionList.size} transactions",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        exportTransactions = registry.register(
            "export_transactions",
            owner,
            ActivityResultContracts.CreateDocument("application/json")
        )
        { uri ->
            if (uri == null) {
                // no file selected
                return@register
            }

            val outputStream = context.applicationContext.contentResolver.openOutputStream(uri)
            if (outputStream == null) {
                Log.e("TransactionExporter", "Content Provider crashed")
                return@register
            }

            owner.lifecycleScope.launch {
                val transactions = viewModel.getTransactions()
                if (transactions == null) {
                    Toast.makeText(context, "Transactions not available", Toast.LENGTH_LONG).show()
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    outputStream.writer().use {
                        val transactionSerializer = TransactionSerializer()
                        it.write(transactionSerializer.serializeList(transactions, true))
                    }
                }

                Log.i("TransactionExporter", "Exported ${transactions.size} transactions to $uri")
                Toast.makeText(
                    context,
                    "Exported ${transactions.size} transactions",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun import() {
        importTransactions.launch(arrayOf("application/*"))
    }

    fun export() {
        exportTransactions.launch("transactions.json")
    }
}