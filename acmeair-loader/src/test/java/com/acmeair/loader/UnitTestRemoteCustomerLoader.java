package com.acmeair.loader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Primary
@Component
@Profile({"test", "sit"})
class UnitTestRemoteCustomerLoader extends SpringCloudCustomerLoader {

    private final String remoteUrl;

    UnitTestRemoteCustomerLoader(@Value("${customer.service.address}") String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    @Override
    protected String customerServiceAddress() {
        return remoteUrl;
    }
}
