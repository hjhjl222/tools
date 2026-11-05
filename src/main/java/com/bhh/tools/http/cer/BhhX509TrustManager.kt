package com.bhh.tools.http.cer

import android.content.Context
import com.bhh.tools.log.Logger
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class BhhX509TrustManager(private val content: Context) : X509TrustManager {
    private val log = Logger.get(BhhX509TrustManager::class)
    private val defaultTrustManager: X509TrustManager?
    private val localTrustManager: X509TrustManager?

    init {
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
            init(null as KeyStore?)
            defaultTrustManager = findX509TrustManager(this)
        }
        localTrustManager = createLocalTrustManager()
    }

    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<X509Certificate?>?, authType: String?) {
        throw UnsupportedOperationException()
    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<X509Certificate?>?, authType: String?) {
        try {
            log.info("checkServerTrusted: $authType")
            defaultTrustManager?.checkServerTrusted(chain, authType)
        } catch (ce: CertificateException) {
            localTrustManager?.checkServerTrusted(chain, authType)
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        log.info("getAcceptedIssuers:")
        return defaultTrustManager?.acceptedIssuers ?: arrayOf()
    }

    private fun findX509TrustManager(tmf: TrustManagerFactory): X509TrustManager? {
        val trustManagers: Array<TrustManager> = tmf.trustManagers
        for (tm in trustManagers) {
            if (tm is X509TrustManager) {
                return tm
            }
        }
        return null
    }

    @Throws(Exception::class)
    private fun createLocalTrustManager(): X509TrustManager? {
        val localTrustStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        localTrustStore.load(null, null)
        content.assets.open("https.cer").use { caInput ->
            val ca: Certificate = CertificateFactory.getInstance("X.509").generateCertificate(caInput)
            localTrustStore.setCertificateEntry("custom-cert", ca)
        }
        val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(localTrustStore)
        return findX509TrustManager(tmf)
    }
}
