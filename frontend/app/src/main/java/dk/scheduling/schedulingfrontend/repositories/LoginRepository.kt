package dk.scheduling.schedulingfrontend.repositories

import dk.scheduling.schedulingfrontend.api.getApiClient
import dk.scheduling.schedulingfrontend.api.protocol.RegisterOrLoginRequest

class LoginRepository(
    private val accountDataSource: AccountDataSource,
) {
}
