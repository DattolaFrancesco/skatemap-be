package fra.skatemap.controllers;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import fra.skatemap.exceptions.BadRequestException;
import fra.skatemap.payloads.DonationRequestDTO;
import fra.skatemap.payloads.DonationResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/donations")
public class StripeController {
    private final String stripeSecretKey;
    private final String stripePublishableKey;

    public StripeController(@Value("${stripe.secret.key}")String stripeSecretKey,
                            @Value("${stripe.publishable.key}")String stripePublishableKey) {
        this.stripeSecretKey = stripeSecretKey;
        this.stripePublishableKey = stripePublishableKey;
    }
    @PostMapping
    public DonationResponseDTO donate(@RequestBody DonationRequestDTO donationRequestDTO){
       Stripe.apiKey = this.stripeSecretKey;
       PaymentIntentCreateParams params = PaymentIntentCreateParams
               .builder()
               .setAutomaticPaymentMethods(
                       PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                               .setEnabled(true).build())
               .setAmount(donationRequestDTO.amount() * 100L) // long
               .setCurrency("eur")
               .build();
        try {
            PaymentIntent intent = PaymentIntent.create(params);
            return new DonationResponseDTO(intent.getClientSecret(), this.stripePublishableKey);
        } catch (StripeException e) {
            throw new BadRequestException("Payment failed: " + e.getMessage());
        }
    }
}
