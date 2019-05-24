import com.google.common.collect.ImmutableMap
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.Select
import java.util.concurrent.TimeUnit


object CheckoutAutoFill {

    private fun goToCheckout(driver: WebDriver) {
        // Redirects to homepage on first attempt to get to cart
        // Might be another way around this, but this brute force works
        driver.get("https://www.supremenewyork.com/checkout")
        driver.get("https://www.supremenewyork.com/checkout")
        while (driver.currentUrl != "https://www.supremenewyork.com/checkout") {
            driver.get("https://www.supremenewyork.com")
            driver.get("https://www.supremenewyork.com/checkout")
            TimeUnit.SECONDS.sleep((0.5).toLong())
        }
    }

    fun fill(driver: WebDriver, cod: CheckoutDetails) {
        val executor = driver as JavascriptExecutor
        goToCheckout(driver)


        val fieldValueMap = ImmutableMap.builder<String, String>()
            .put("order_billing_name", cod.fullName)
            .put("order_email", cod.email)
            .put("order_tel", cod.tel)
            .put("bo", cod.address1)
            .put("oba3", cod.address2)
            .put("order_billing_address_3", cod.address3)
            .put("order_billing_city", cod.city)
            .put("order_billing_zip", cod.postcode)
            .put("cnb", cod.cardNumber)
            .put("vval", cod.cvv)
            .build()

        // Country first as the cart may need to update if the country is changed
        val countryDropDown = Select(driver.findElement(By.id("order_billing_country")))
        countryDropDown.selectByVisibleText(cod.country)

        // SendKeys was too slow, JS executor seems like the faster solution for now
        val iterator = fieldValueMap.keys.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            driver.executeScript("""document.getElementById('$key').setAttribute('value', '${fieldValueMap[key]}')""")
        }

        val cardTypeDropDown = Select(driver.findElement(By.id("credit_card_type")))
        cardTypeDropDown.selectByVisibleText(cod.cardType)

        val cardMonthDropDown = Select(driver.findElement(By.id("credit_card_month")))
        cardMonthDropDown.selectByVisibleText(cod.expiryMonth)

        val cardYearDropDown = Select(driver.findElement(By.id("credit_card_year")))
        cardYearDropDown.selectByVisibleText(cod.expiryYear)

        val checkBox =
            driver.findElement(By.xpath("/html/body/div[2]/div[1]/form/div[2]/div[2]/fieldset/p/label/div/ins"))
        checkBox.click()
    }

    fun checkout(driver: WebDriver, coDetails: CheckoutDetails) {
        fill(driver, coDetails)
//        val buyButton = driver.findElement(By.xpath("/html/body/div[2]/div[1]/form/div[3]/div/input"))
//        buyButton.click()
    }
}