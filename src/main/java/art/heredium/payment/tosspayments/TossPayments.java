package art.heredium.payment.tosspayments;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.payment.inf.PaymentService;
import art.heredium.payment.tosspayments.dto.request.TossPaymentsPayRequest;
import art.heredium.payment.tosspayments.dto.request.TossPaymentsRefundRequest;
import art.heredium.payment.tosspayments.dto.response.TossPaymentsPayResponse;
import art.heredium.payment.tosspayments.dto.response.TossPaymentsValidResponse;
import art.heredium.payment.tosspayments.feign.client.TossPaymentsClient;
import art.heredium.payment.type.PaymentType;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class TossPayments implements PaymentService<TossPaymentsValidResponse, TossPaymentsPayRequest> {

    private final TossPaymentsClient client;

    private final Environment environment;

    @Override
    public PaymentType getPaymentType(TossPaymentsPayRequest dto) {
        return dto.getType();
    }

    @Override
    public TossPaymentsValidResponse valid(Ticket ticket) {
        return new TossPaymentsValidResponse(ticket);
    }

    @Override
    public TossPaymentsPayResponse pay(TossPaymentsPayRequest dto, Long amount) {
        try {
            String authorization = getAuthorization(dto.getType());
            return client.pay(authorization, dto);
        } catch (FeignException e) {
            e.printStackTrace();
            throw new ApiException(ErrorCode.BAD_REQUEST, e.responseBody());
        }
    }

    @Override
    public void cancel(Ticket ticket, TossPaymentsPayRequest dto) {
        refund(ticket);
    }

    @Override
    public void refund(Ticket ticket) {
        TossPaymentsRefundRequest payloadMap = new TossPaymentsRefundRequest();
        payloadMap.setCancelReason("환불");

        try {
            client.refund(getAuthorization(ticket.getPayment()), ticket.getPgId(), payloadMap);
        } catch (FeignException e){
            e.printStackTrace();
            throw new ApiException(ErrorCode.BAD_REQUEST, e.responseBody());
        }
    }

    private String getAuthorization(PaymentType type) {
        String secretKey = environment.getProperty(type.getPropertyKeyName());
        return "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
    }
}
