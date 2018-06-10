package com.holahmeds.ledger

import android.arch.persistence.room.*
import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import kotlinx.android.synthetic.main.transaction_card.view.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun DPToPixel(size: Int, resources: Resources): Int {
    return (size * resources.displayMetrics.density).toInt()
}

@Entity(tableName = "transaction_table")
class Transaction(
        @PrimaryKey(autoGenerate = true) var id: Int,
        val date: LocalDate,
        val amount: Long,
        val category: String,
        val transactee: String?
        //val tags: List<String> TODO
)

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transaction_table")
    fun getAll(): List<Transaction>

    @Insert
    fun add(transaction: Transaction)

    @Insert
    fun addAll(transactions: Collection<Transaction>)
}

class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val date: TextView = view.date
    val amount: TextView = view.amount
    val category: TextView = view.category
    val transactee: TextView = view.transactee
    val tags: FlexboxLayout = view.tag_list
}

class TransactionAdapter(private var data: List<Transaction>): RecyclerView.Adapter<TransactionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val transactionView = LayoutInflater.from(parent.context).inflate(R.layout.transaction_card, parent, false)
        return TransactionViewHolder(transactionView)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = data[position]

        holder.date.text = transaction.date.format(DateTimeFormatter.ISO_LOCAL_DATE)

        holder.amount.text = transaction.amount.toString()

        holder.category.text = transaction.category

        holder.transactee.run {
            if (transaction.transactee != null) {
                visibility = View.VISIBLE
                text = transaction.transactee
            } else {
                visibility = View.GONE
            }
        }

        /*
        holder.tags.run {
            removeAllViews()

            val tagLayoutParams = FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT)
            val margin = DPToPixel(4, resources)
            tagLayoutParams.setMargins(margin, margin, margin, margin)

            val padding = DPToPixel(4, resources)

            val borderedBackground = context.resources.getDrawable(R.drawable.rect)

            for (t in transaction.tags) {
                val newTag = TextView(context).apply {
                    layoutParams = tagLayoutParams
                    background = borderedBackground
                    setPadding(padding, padding, padding, padding)
                    text = t
                }
                holder.tags.addView(newTag)
            }
        }
        */
    }

    override fun getItemCount() = data.size
}
