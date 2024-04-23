package dk.scheduling.schedulingfrontend.model

import dk.scheduling.schedulingfrontend.api.protocol.Event
import dk.scheduling.schedulingfrontend.api.protocol.Task

data class TaskEvent(
    val task: Task,
    val event: Event?,
) {
    fun hasEvent(): Boolean {
        return event != null
    }

    val duration: Long
        get() {
            return task.duration
        }
}
