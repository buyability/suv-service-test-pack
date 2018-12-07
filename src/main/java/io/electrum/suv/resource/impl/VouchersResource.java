package io.electrum.suv.resource.impl;

import javax.ws.rs.Path;

import io.electrum.suv.api.IVouchersResource;
import io.electrum.suv.api.utils.Paths;
import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;

@Path(Paths.BASE_PATH)
@Api(description = "the SUV API", authorizations = { @Authorization("httpBasic") })
public class VouchersResource extends io.electrum.suv.api.VouchersResource {
   // --Commented out by Inspection (2018/12/07, 07:29):private static final Logger log = LoggerFactory.getLogger(VouchersResource.class);
   private static VouchersResourceImpl instance = null;

   @Override
   protected IVouchersResource getResourceImplementation() {
      if (instance == null) {
         instance = new VouchersResourceImpl();
      }
      return instance;
   }

}
