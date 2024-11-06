package art.heredium.controller.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import org.apache.commons.lang3.NotImplementedException;

import art.heredium.ncloud.bean.HerediumAlimTalk;
import art.heredium.ncloud.type.AlimTalkTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlimTalkTestService {
  private final HerediumAlimTalk herediumAlimTalk;

  public void sendMessageToAlimTalk(String templateCode, String phone) {
    switch (templateCode) {
      case "HEREDIUM017":
        {
          this.sendMockedMembershipRegistrationMessageToAlimTalk(phone);
          break;
        }
      case "HEREDIUM018":
        {
          this.sendMockedCouponUsedMessageToAlimTalk(phone);
          break;
        }
      case "HEREDIUM019":
        {
          this.sendMockedCouponDeliveredMessageToAlimTalk(phone);
          break;
        }
      case "HEREDIUM020":
        {
          this.sendMockedMembershipExpiredMessageToAlimTalk(phone);
          break;
        }
      default:
        throw new NotImplementedException(
            "NotImplementedException for templateCode " + templateCode);
    }
  }

  private void sendMockedMembershipExpiredMessageToAlimTalk(final String toPhone) {
    log.info("Start sendMockedMembershipExpiredMessageToAlimTalk");
    Map<String, String> params = createMockedMembershipExpiredParams();
    try {
      this.herediumAlimTalk.sendAlimTalk(
          toPhone, params, AlimTalkTemplate.MEMBERSHIP_PACKAGE_HAS_EXPIRED);
    } catch (Exception e) {
      log.warn(
          "Sending message to AlimTalk failed: {}, message params: {}", e.getMessage(), params);
    } finally {
      log.info("End sendMockedMembershipExpiredMessageToAlimTalk");
    }
  }

  private Map<String, String> createMockedMembershipExpiredParams() {
    Map<String, String> variables = new HashMap<>();
    final String accountName = "TestAccountName";
    final String membershipName = "TestMembershipName";
    final String membershipStartDate = "2024-11-10";
    final String membershipEndDate = "2025-11-09";
    variables.put("accountName", accountName);
    variables.put("membershipName", membershipName);
    variables.put("startDate", membershipStartDate);
    variables.put("endDate", membershipEndDate);
    return variables;
  }

  private void sendMockedCouponDeliveredMessageToAlimTalk(String toPhone) {
    log.info("Start sendMockedCouponDeliveredMessageToAlimTalk");
    Map<String, String> variables = new HashMap<>();
    final String accountName = "TestAccountName";
    final String couponStartDate = "2024-11-06 15:30";
    final String couponEndDate = "2025-11-06 17:00";
    final String issuedCouponName = "TestCouponName";
    final String discountPercent = "100";
    variables.put("accountName", accountName);
    variables.put("couponName", issuedCouponName);
    variables.put("discountPercent", discountPercent + "%");
    variables.put("couponStartDate", couponStartDate);
    variables.put("couponEndDate", couponEndDate);
    variables.put("numberOfUses", "2");

    List<Map<String, String>> params = Arrays.asList(variables);
    try {
      this.herediumAlimTalk.sendAlimTalk(
          toPhone, params, AlimTalkTemplate.COUPON_HAS_BEEN_DELIVERED);
    } catch (Exception e) {
      log.warn(
          "Sending message to AlimTalk failed: {}, message params: {}", e.getMessage(), params);
    } finally {
      log.info("End sendMockedCouponDeliveredMessageToAlimTalk");
    }
  }

  private void sendMockedCouponUsedMessageToAlimTalk(String phone) {
    log.info("Start sendMockedCouponUsedMessageToAlimTalk");
    Map<String, String> params = new HashMap<>();
    final String accountName = "TestAccountName";
    final String membershipName = "TestMembershipName";
    final String issuedDate = "2024-11-06 15:30";
    final String issuedCouponName = "TestCouponName";
    final String remainedDetailCoupons = this.buildMockedCouponDetails();
    params.put("accountName", accountName);
    params.put("membershipName", membershipName);
    params.put("issuedDate", issuedDate);
    params.put("issuedCouponName", issuedCouponName);
    params.put("remainedDetailCoupons", remainedDetailCoupons);

    try {
      this.herediumAlimTalk.sendAlimTalk(phone, params, AlimTalkTemplate.COUPON_HAS_BEEN_USED);
    } catch (Exception e) {
      log.warn(
          "Sending message to AlimTalk failed: {}, message params: {}", e.getMessage(), params);
    } finally {
      log.info("End sendMockedCouponUsedMessageToAlimTalk");
    }
  }

  private void sendMockedMembershipRegistrationMessageToAlimTalk(String phone) {
    log.info("Start sendMockedMembershipRegistrationMessageToAlimTalk");
    final Map<String, String> params = new HashMap<>();
    final String accountName = "TestAccountName";
    final String membershipName = "TestMembershipName";
    final String startDate = "2024-11-06";
    final String endDate = "2025-11-05";
    final String detailCoupons = this.buildMockedCouponDetails();
    params.put("accountName", accountName);
    params.put("멤버십등급", membershipName);
    params.put("멤버십 등급", membershipName);
    params.put("startDate", startDate);
    params.put("endDate", endDate);
    params.put("detailCoupons", detailCoupons);
    try {
      this.herediumAlimTalk.sendAlimTalk(
          phone, params, AlimTalkTemplate.USER_REGISTER_MEMBERSHIP_PACKAGE);
    } catch (Exception e) {
      log.warn(
          "Sending message to AlimTalk failed: {}, message params: {}", e.getMessage(), params);
    }
    log.info("End sendMockedMembershipRegistrationMessageToAlimTalk");
  }

  private String buildMockedCouponDetails() {
    return String.format(" - %s, %s%% : %s", "coupon01", "100", 2);
  }
}
