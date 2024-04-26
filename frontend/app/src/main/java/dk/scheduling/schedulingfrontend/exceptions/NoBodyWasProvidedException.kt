package dk.scheduling.schedulingfrontend.exceptions

import okhttp3.Response

class NoBodyWasProvidedException(s: String, response: Response) : Throwable()
