package www.founded.com.service.payment.impl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import www.founded.com.dto.payment.*;
import www.founded.com.dto.payment.aba.PaywayCheckTxnResponse;
import www.founded.com.service.payment.PaywayClient;
import www.founded.com.utils.payment.aba.PaywayCrypto;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class PaywayClientImpl implements PaywayClient {

    private final RestTemplate restTemplate;

    @Value("${aba.payway.api.endpoint}")
    private String baseUrl;

    @Value("${aba.payway.merchantId}")
    private String merchantId;

    @Value("${aba.payway.apiKey}")
    private String apiKey;

    @Value("${aba.payway.publicKeyPem}")
    private String publicKeyPem;

    private static final DateTimeFormatter REQ_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private String nowUtc() {
        return ZonedDateTime.now(ZoneOffset.UTC).format(REQ_TIME);
    }

    /**
     * Purchase API
     * Endpoint: /api/payment-gateway/v1/payments/purchase (multipart/form-data) :contentReference[oaicite:8]{index=8}
     * Response: HTML (checkout page) :contentReference[oaicite:9]{index=9}
     */
    @Override
    public PaywayCheckoutResponse createCheckout(PaywayCheckoutRequest req) {

        String reqTime = nowUtc();

        // IMPORTANT:
        // PayWay's "tran_id" is YOUR unique order id. Use escrowId/projectId string.
        String tranId = String.valueOf(req.getEscrowId());

        // Minimal required fields for purchase (you can add more like items, return_url, etc.)
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("req_time", reqTime);
        form.add("merchant_id", merchantId);
        form.add("tran_id", tranId);
        form.add("amount", req.getAmount().setScale(2).toPlainString());
        form.add("currency", req.getCurrency());
        form.add("payment_option", "abapay"); // example, choose based on your UI
        form.add("return_url", req.getReturnUrl());
        form.add("cancel_url", req.getReturnUrl()); // you can separate cancel URL
        form.add("continue_success_url", req.getReturnUrl());

        // HASH:
        // PDF shows you concatenate parameters then sha512 with API key, output looks base64 :contentReference[oaicite:10]{index=10}
        // Build the same order as your posted parameters (keep it consistent).
        String hashData = reqTime
                + merchantId
                + tranId
                + req.getAmount().setScale(2).toPlainString()
                + req.getCurrency()
                + "abapay"
                + req.getReturnUrl()
                + req.getReturnUrl()
                + req.getReturnUrl();

        String hash = PaywayCrypto.hmacSha512Base64(hashData, apiKey);
        form.add("hash", hash);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(form, headers);

        String url = baseUrl + "/api/payment-gateway/v1/payments/purchase";
        ResponseEntity<String> resp = restTemplate.postForEntity(url, entity, String.class);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new IllegalStateException("PayWay purchase failed: HTTP " + resp.getStatusCode());
        }

        PaywayCheckoutResponse out = new PaywayCheckoutResponse();
        out.setTxnId(tranId);
        out.setCheckoutUrl(null);          // purchase returns HTML, not a URL
        out.setCheckoutHtml(resp.getBody()); // <-- ADD THIS FIELD in DTO
        return out;
    }

    /**
     * Check Transaction API
     * Endpoint: /api/payment-gateway/v1/payments/check-transaction-2 :contentReference[oaicite:11]{index=11}
     */
    public PaywayCheckTxnResponse checkTransaction(String tranId) {

        String reqTime = nowUtc();
        String hashData = reqTime + merchantId + tranId;
        String hash = PaywayCrypto.hmacSha512Base64(hashData, apiKey);

        var body = new java.util.HashMap<String, Object>();
        body.put("req_time", reqTime);
        body.put("merchant_id", merchantId);
        body.put("tran_id", tranId);
        body.put("hash", hash);

        String url = baseUrl + "/api/payment-gateway/v1/payments/check-transaction-2";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<PaywayCheckTxnResponse> resp =
                restTemplate.postForEntity(url, new HttpEntity<>(body, headers), PaywayCheckTxnResponse.class);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new IllegalStateException("PayWay checkTransaction failed: HTTP " + resp.getStatusCode());
        }
        return resp.getBody();
    }

    /**
     * Refund API
     * Endpoint: /api/merchant-portal/merchant-access/online-transaction/refund :contentReference[oaicite:12]{index=12}
     * merchant_auth = RSA_PUBLIC(merchant_id, tran_id, refund_amount)
     * hash = sha512(request_time + merchant_id + merchant_auth) + API_Keys :contentReference[oaicite:13]{index=13}
     */
    @Override
    public void refund(PaywayRefundRequest req) {

        String requestTime = nowUtc();

        // merchant_auth plaintext JSON as in doc table :contentReference[oaicite:14]{index=14}
        String merchantAuthPlain = String.format(
                "{\"mc_id\":\"%s\",\"tran_id\":\"%s\",\"refund_amount\":\"%s\"}",
                merchantId,
                String.valueOf(req.getEscrowId()),
                req.getAmount().setScale(2).toPlainString()
        );

        String merchantAuth = PaywayCrypto.rsaEncryptBase64(publicKeyPem, merchantAuthPlain);

        String hashData = requestTime + merchantId + merchantAuth;
        String hash = PaywayCrypto.hmacSha512Base64(hashData, apiKey);

        var body = new java.util.HashMap<String, Object>();
        body.put("request_time", requestTime);
        body.put("merchant_id", merchantId);
        body.put("merchant_auth", merchantAuth);
        body.put("hash", hash);

        String url = baseUrl + "/api/merchant-portal/merchant-access/online-transaction/refund";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> resp = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("PayWay refund failed: HTTP " + resp.getStatusCode() + " body=" + resp.getBody());
        }
    }

    /**
     * Payout API (withdrawal payout)
     * Endpoint: /api/payment-gateway/v2/direct-payment/merchant/payout :contentReference[oaicite:15]{index=15}
     *
     * NOTE: hashing format for payout must match PayWay payout doc exactly.
     * Here we keep a placeholder "hashData" pattern—replace according to your doc/account requirement.
     */
    @Override
    public void payout(PaywayPayoutRequest req) {

        String tranId = "W" + req.getWithdrawalId(); // your external id

        // beneficiaries field comes from PayWay (whitelist/beneficiary token) :contentReference[oaicite:16]{index=16}
        // You must store that value per freelancer bank account (not shown in your models yet).
        String beneficiaries = req.getBeneficiariesToken();

        String customFields = "{\"withdrawalId\":\"" + req.getWithdrawalId() + "\"}";

        // TODO: Confirm exact hash concatenation for payout from your PayWay payout docs.
        String hashData = merchantId + tranId + beneficiaries + req.getAmount().setScale(2).toPlainString()
                + req.getCurrency() + customFields;

        // Many PayWay calls use sha512 with API key; verify payout’s exact rule on your side. :contentReference[oaicite:17]{index=17}
        String hash = PaywayCrypto.hmacSha512Base64(hashData, apiKey);

        var body = new java.util.HashMap<String, Object>();
        body.put("merchant_id", merchantId);
        body.put("tran_id", tranId);
        body.put("beneficiaries", beneficiaries);
        body.put("amount", req.getAmount().setScale(2).doubleValue());
        body.put("currency", req.getCurrency());
        body.put("custom_fields", customFields);
        body.put("hash", hash);

        String url = baseUrl + "/api/payment-gateway/v2/direct-payment/merchant/payout";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> resp = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("PayWay payout failed: HTTP " + resp.getStatusCode() + " body=" + resp.getBody());
        }
    }
}