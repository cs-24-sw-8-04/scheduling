package dk.scheduling.schedulingfrontend.exceptions

import okhttp3.Response

class NoBodyWasProvidedException(message: String, val response: Response) : Throwable(message)
