package com.holahmeds.ledger

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.content.res.Resources
import android.os.Parcel
import android.os.Parcelable
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
): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            LocalDate.of(parcel.readInt(), parcel.readInt(), parcel.readInt()),
            parcel.readLong(),
            parcel.readString(),
            parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        with(date) {
            parcel.writeInt(year)
            parcel.writeInt(monthValue)
            parcel.writeInt(dayOfMonth)
        }
        parcel.writeLong(amount)
        parcel.writeString(category)
        parcel.writeString(transactee)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Transaction> {
        override fun createFromParcel(parcel: Parcel): Transaction {
            return Transaction(parcel)
        }

        override fun newArray(size: Int): Array<Transaction?> {
            return arrayOfNulls(size)
        }
    }
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transaction_table")
    fun getAll(): LiveData<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(transaction: Transaction)

    @Insert
    fun addAll(transactions: Collection<Transaction>)
}

class TransactionViewHolder(val transactionView: View): RecyclerView.ViewHolder(transactionView) {
    val date: TextView = transactionView.date
    val amount: TextView = transactionView.amount
    val category: TextView = transactionView.category
    val transactee: TextView = transactionView.transactee
    val tags: FlexboxLayout = transactionView.tag_list
}

class TransactionAdapter(private var data: List<Transaction>, private val onItemLongClick: (Transaction) -> Unit): RecyclerView.Adapter<TransactionViewHolder>() {
    fun setData(newData: List<Transaction>) {
        data = newData
        notifyDataSetChanged()
    }

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

        holder.transactionView.setOnLongClickListener({
            onItemLongClick(transaction)
            true
        })

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
