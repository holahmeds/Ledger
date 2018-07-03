package com.holahmeds.ledger.entities

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.os.Parcel
import android.os.Parcelable
import java.time.LocalDate

@Entity(tableName = "transaction_table")
class Transaction: Parcelable {
    @PrimaryKey(autoGenerate = true) var id: Int
    val date: LocalDate
    val amount: Long
    val category: String
    val transactee: String?
    val note: String?

    constructor(id: Int, date: LocalDate, amount: Long, category: String, transactee: String?, note: String?) {
        this.id = id
        this.date = date
        this.amount = amount
        this.category = category
        this.transactee = transactee
        this.note = note
    }

    constructor(parcel: Parcel) {
        this.id = parcel.readInt()
        this.date = LocalDate.of(parcel.readInt(), parcel.readInt(), parcel.readInt())
        this.amount = parcel.readLong()
        this.category = parcel.readString()
        this.transactee = parcel.readString()
        this.note = parcel.readString()
    }
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
        parcel.writeString(note)
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
