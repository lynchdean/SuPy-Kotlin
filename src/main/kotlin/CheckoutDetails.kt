import Main.logger
import java.io.FileInputStream
import java.util.*

class CheckoutDetails(propertiesPath: String) {

    var fullName: String
    var email: String
    var tel: String
    var address1: String
    var address2: String
    var address3: String
    var city: String
    var postcode: String
    var country: String
    var cardType: String
    var cardNumber: String
    var expiryMonth: String
    var expiryYear: String
    var cvv: String

    init {
        val properties = getProperties(propertiesPath)
        this.fullName = properties.getProperty("fullName")
        this.email = properties.getProperty("email")
        this.tel = properties.getProperty("tel")
        this.address1 = properties.getProperty("address1")
        this.address2 = properties.getProperty("address2")
        this.address3 = properties.getProperty("address3")
        this.city = properties.getProperty("city")
        this.postcode = properties.getProperty("postcode")
        this.country = properties.getProperty("country")
        this.cardType = properties.getProperty("cardType")
        this.cardNumber = properties.getProperty("cardNumber")
        this.expiryMonth = properties.getProperty("expiryMonth")
        this.expiryYear = properties.getProperty("expiryYear")
        this.cvv = properties.getProperty("cvv")
    }

    private fun getProperties(propertiesPath: String) : Properties {
        val properties = Properties()
        val input = FileInputStream(propertiesPath)
        properties.load(input)
        logger?.info("Details loaded from checkout properties files")
        return properties
    }
}
