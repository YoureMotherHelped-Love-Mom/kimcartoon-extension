package eu.kanade.tachiyomi.extension.all.kimcartoon

import eu.kanade.tachiyomi.network.interceptor.CloudflareInterceptor
import okhttp3.OkHttpClient

fun OkHttpClient.Builder.addCloudflareInterceptor(): OkHttpClient.Builder {
    return addInterceptor(CloudflareInterceptor(debug = true))
}