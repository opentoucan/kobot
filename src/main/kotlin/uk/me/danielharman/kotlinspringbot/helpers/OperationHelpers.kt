package uk.me.danielharman.kotlinspringbot.helpers
/**
 * Helper classes for services
 */
object OperationHelpers {
    /**
     * Class that encapsulates operation output
     * Avoids the passing of nulls if there is a failure and can provide error messages
     * @param <T> The type of the object to be returned
    </T> */
    @Deprecated("Use result class")
    class OperationResult<T> internal constructor(val value: T, val message: String, val success: Boolean){

        val failure: Boolean
            get() = !success

        companion object {
            /**
             * Factory method, returns a successful OperationResult
             * @param `object` The object
             * @param <T> The object type
             * @return A new OperationResult
            </T> */
            fun <T> successResult(value: T): OperationResult<T?> = OperationResult(value, "", true)

            /**
             * Factory method, returns a failure Operation result
             * @param message The error message
             * @param <T>
             * @return A new OperationResult
            </T> */
            fun <T> failResult(message: String): OperationResult<T?> = OperationResult(null, message, false)

        }
    }
}