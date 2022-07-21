import androidx.fragment.app.testing.launchFragmentInContainer
import com.holahmeds.ledger.fragments.TransactionListMenu
import org.junit.Test

class TransactionListMenuTest {
    @Test
    fun testFragmentCanBeCreatedByFactory() {
        launchFragmentInContainer<TransactionListMenu>()
    }
}