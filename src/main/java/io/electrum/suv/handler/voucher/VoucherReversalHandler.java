package io.electrum.suv.handler.voucher;

import io.electrum.suv.api.models.ErrorDetail;
import io.electrum.suv.api.models.ProvisionResponse;
import io.electrum.suv.handler.BaseHandler;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.util.RequestKey;
import io.electrum.suv.server.util.VoucherModelUtils;
import io.electrum.vas.model.BasicReversal;
import io.electrum.vas.model.TenderAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.concurrent.ConcurrentHashMap;

public class VoucherReversalHandler extends BaseHandler {

   private static final Logger log = LoggerFactory.getLogger(VoucherProvisionHandler.class);

   /** The UUID of this request */
   private String reversalUuid;
   /** The UUID identifying the request that this reversal relates to */
   private String voucherId;

   public VoucherReversalHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   public Response handle(BasicReversal reversal, UriInfo uriInfo) {
      try {
         Response rsp;

         reversalUuid = reversal.getId();
         voucherId = reversal.getRequestId();
         if (!VoucherModelUtils.isValidUuid(reversalUuid)) {
            return VoucherModelUtils.buildInvalidUuidErrorResponse(
                  reversalUuid,
                  null, // TODO Could overload method
                  username,
                  ErrorDetail.ErrorType.FORMAT_ERROR);
         } else if (!VoucherModelUtils.isValidUuid(voucherId)) {
            return VoucherModelUtils
                  .buildInvalidUuidErrorResponse(voucherId, null, username, ErrorDetail.ErrorType.FORMAT_ERROR);
         }

         // TODO check this in airtime
         rsp = VoucherModelUtils.canReverseVoucher(voucherId, reversalUuid, username, password);
         if (rsp != null) {
            if (rsp.getStatus() == 404) {
               // make sure to record the reversal in case we get the request late.
               addVoucherReversalToCache(reversal);
            }
            return rsp;
         }

         addVoucherReversalToCache(reversal);

         rsp = Response.accepted((reversal)).build(); // TODO Ask Casey if this is ok

         return rsp;

      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   private void addVoucherReversalToCache(BasicReversal basicReversal) {
      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords =
            SUVTestServerRunner.getTestServer().getVoucherReversalRecords();
      RequestKey reversalKey =
            new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, basicReversal.getRequestId());
      reversalRecords.put(reversalKey, basicReversal);
   }

   @Override
   protected String getRequestName() {
      return "Voucher Reversal";
   }
}
